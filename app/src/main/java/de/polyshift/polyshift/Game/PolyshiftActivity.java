package de.polyshift.polyshift.Game;

import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import de.polyshift.polyshift.Analytics.AnalyticsApplication;

import de.polyshift.polyshift.Adapter.LoginAdapter;
import de.polyshift.polyshift.Menu.MainMenuActivity;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import de.polyshift.polyshift.R;
import de.polyshift.polyshift.Menu.MyGamesActivity;
import de.polyshift.polyshift.Tools.PHPConnector;

import java.util.ArrayList;
import java.util.HashMap;


public class PolyshiftActivity extends GameActivity implements GameListener {

    Player player;
    Player player2;
    Polynomino poly;
    Renderer renderer;
    Simulation simulation;
    GameLoop gameLoop;
    private String response = "";
    private Menu menu;
    private HashMap<String,String> game_status;
    private Activity activity = this;
    private boolean downloaded = false;
    private boolean statusDownloaded = false;
    private boolean winnerIsAnnounced = false;
    public static boolean statusUpdated = true;
    public boolean gameUpdated = false;
    private Thread game_status_thread = new GameStatusThread();
    private String notificationReceiver = "";
    private String notificationMessage = "";
    private String notificationGameID =  "";
    private Context context;
    private LoginAdapter loginAdapter;
    private boolean onBackPressed = false;
    private boolean onDestroyed = false;
    public static ProgressDialog dialog = null;
    private boolean isSaving = false;
    private Tracker mTracker = null;
    public static boolean isActive = false;
    public String game_id = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = getApplicationContext();

        loginAdapter = new LoginAdapter(context,PolyshiftActivity.this);
        loginAdapter.handleSessionExpiration(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_OPTIONS_PANEL);

        super.onCreate(savedInstanceState);

        setGameListener(this);

        setTitle("Polyshift");

        Log.d("Polyshift", "Polyshift Spiel erstellt");

        dialog = ProgressDialog.show(PolyshiftActivity.this, "", getString(R.string.game_data_is_loading), true);

        game_id = getIntent().getExtras().getString("game_id");

        update_game_thread.start();
        try {
            long waitMillis = 10000;
            while (update_game_thread.isAlive()) {
                update_game_thread.join(waitMillis);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

    }

    public void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState( outState );
        Log.d( "Polyshift", "Polyshift Spielstand gespeichert" );
    }

    @Override
    public void onPause( )
    {
        super.onPause();
        isActive = false;
        dialog.dismiss();
        Log.d( "Polyshift", "Polyshift pausiert" );
    }

    @Override
    public void onResume( )
    {
        super.onResume();
        isActive = true;
        Log.d("Polyshift", "Polyshift wiederhergestellt");

        mTracker.setScreenName(getClass().getName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onDestroy( )
    {
        super.onDestroy();
        Log.d("Polyshift", "Polyshift beendet");
        onDestroyed = true;
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_status, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }
    public void onBackPressed() {
        if(!isSaving) {
            dialog = ProgressDialog.show(PolyshiftActivity.this, "", getString(R.string.game_is_closing), true);
            onBackPressed = true;
            final Intent intent = new Intent(this, MyGamesActivity.class);
            startActivity(intent);
            this.finish();
        }
    }

    @Override
    public void setup(GameActivity activity, GL10 gl) {

        if((!(simulation instanceof Simulation)) && !onDestroyed){
            game_status = getGameStatus();

            if(game_status.size() > 0) {

                gameLoop = new GameLoop(game_status.get("my_game"),activity);
                Object syncObj = new Object();

                if (game_status.get("new_game").equals("1")) {
                    simulation = new Simulation(activity);
                    gameLoop.setRandomPlayer();
                    GameSync.uploadSimulation(simulation);
                }

                simulation = GameSync.downloadSimulation();
                if (simulation == null) {
                    Log.d("crashed", "crashed while downloading simulation");
                    final Intent intent = new Intent(PolyshiftActivity.this, MainMenuActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    Bundle error = new Bundle();
                    error.putBoolean("error_downloading_game", true);
                    intent.putExtras(error);
                    startActivity(intent);
                    PolyshiftActivity.this.finish();
                }else{
                    renderer = new Renderer3D(activity, gl, simulation.objects);
                    renderer.enableCoordinates(gl, simulation.objects);
                    simulation.player.isLocked = true;
                    simulation.player2.isLocked = true;

                    updateGame(activity, gl);

                    dialog.dismiss();
                }
            }else if(!onBackPressed){
                Log.d("crashed","crashed while reading game status. game status: " + game_status.toString());
                final Intent intent = new Intent(PolyshiftActivity.this, MainMenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                Bundle error = new Bundle();
                error.putBoolean("error_occured", true);
                intent.putExtras(error);
                startActivity(intent);
                PolyshiftActivity.this.finish();
            }
        }

    }

    long start = System.nanoTime();
    int frames = 0;

    @Override
    public void mainLoopIteration(GameActivity activity, GL10 gl) {

        if(!onBackPressed && !onDestroyed && game_status.size() > 0 && simulation != null) {

            renderer.setPerspective(activity, gl);
            renderer.renderLight(gl);
            renderer.renderObjects(activity, gl, simulation.objects);
            simulation.update(activity);
            gameLoop.update(simulation, notificationReceiver, notificationMessage, notificationGameID);

            if (gameLoop.game_status_thread.isAlive() && !isSaving && !simulation.hasWinner) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MenuItem item = menu.findItem(R.id.action_game_status);
                        item.setTitle(R.string.game_is_saving);
                        isSaving = true;
                    }
                });
            }
            if (simulation.winner == null && !gameLoop.game_status_thread.isAlive()) {
                if (!statusUpdated || (game_status.get("opponents_turn").equals("1") && game_status.get("my_game").equals("yes")) || (game_status.get("opponents_turn").equals("0") && game_status.get("my_game").equals("no"))) {
                    if (!statusUpdated || System.nanoTime() - start > 1000000000) {
                        statusDownloaded = true;
                        statusUpdated = true;
                        gameUpdated = false;
                        game_status = getGameStatus();
                        updateGame(activity, gl);
                        start = System.nanoTime();
                        isSaving = false;
                    }
                }
            }else if(!winnerIsAnnounced && !gameLoop.game_status_thread.isAlive()){
                game_status = getGameStatus();
            }

            if (simulation.hasWinner && !winnerIsAnnounced) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(PolyshiftActivity.this);
                        if (simulation.winner.isPlayerOne && game_status.get("my_game").equals("yes")) {
                            builder.setMessage(getString(R.string.you_won));
                            builder.setCancelable(false);
                            builder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog = ProgressDialog.show(PolyshiftActivity.this, "", getString(R.string.game_is_closing), true);
                                            final Intent intent = new Intent(PolyshiftActivity.this, MyGamesActivity.class);
                                            startActivity(intent);
                                            PolyshiftActivity.this.finish();
                                            dialog.cancel();
                                            if(game_status.get("opponents_turn").equals("0")) {
                                                updateScores(false, game_status.get("opponent_id"), game_status.get("user_id"), game_status.get("my_user_name"));
                                            }
                                        }
                                    });
                        } else if (simulation.winner.isPlayerOne && game_status.get("my_game").equals("no")) {
                            builder.setMessage(game_status.get("challenger_name") + getString(R.string.has_won));
                            builder.setCancelable(false);
                            builder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            final Intent intent = new Intent(PolyshiftActivity.this, MyGamesActivity.class);
                                            startActivity(intent);
                                            PolyshiftActivity.this.finish();
                                            dialog.cancel();
                                            if(game_status.get("opponents_turn").equals("1")) {
                                                deleteGame();
                                            }
                                        }
                                    });
                        } else if (!simulation.winner.isPlayerOne && game_status.get("my_game").equals("no")) {
                            builder.setMessage(R.string.you_won);
                            builder.setCancelable(false);
                            builder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            final Intent intent = new Intent(PolyshiftActivity.this, MyGamesActivity.class);
                                            startActivity(intent);
                                            PolyshiftActivity.this.finish();
                                            dialog.cancel();
                                            if(game_status.get("opponents_turn").equals("1")) {
                                                updateScores(true, game_status.get("user_id"), game_status.get("opponent_id"), game_status.get("opponent_name"));
                                            }
                                        }
                                    });
                        } else if (!simulation.winner.isPlayerOne && game_status.get("my_game").equals("yes")) {
                            builder.setMessage(game_status.get("opponent_name") + getString(R.string.has_won));
                            builder.setCancelable(false);
                            builder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            final Intent intent = new Intent(PolyshiftActivity.this, MyGamesActivity.class);
                                            startActivity(intent);
                                            PolyshiftActivity.this.finish();
                                            dialog.cancel();
                                            if (game_status.get("opponents_turn").equals("0")) {
                                                deleteGame();
                                            }
                                        }
                                    });
                        }
                        builder.show();
                    }
                });
                winnerIsAnnounced = true;
            }


            frames++;
        }
    }

    private class GameStatusThread extends Thread{
        public void run(){
            response = PHPConnector.doRequest("get_game_status.php");
        }
    }
    private HashMap<String,String> getGameStatus(){
        HashMap<String,String> game_status = new HashMap<String, String>();
        game_status_thread = new GameStatusThread();
        game_status_thread.start();
        try {
            long waitMillis = 10000;
            while (game_status_thread.isAlive()) {
                game_status_thread.join(waitMillis);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(!onBackPressed && (response.equals("error") || response.split(":").length == 1 || response == null || response.equals(""))){
            Log.d("crashed","crashed while downloading game status. response: " + response);
            final Intent intent = new Intent(PolyshiftActivity.this, MainMenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            Bundle error = new Bundle();
            error.putBoolean("error_occured", true);
            intent.putExtras(error);
            startActivity(intent);
            PolyshiftActivity.this.finish();
        }
        else {
            String[] game = response.split(":");
            game_status.put("game_id", game[0]);
            game_status.put("opponent_id", game[1].split("=")[1]);
            game_status.put("opponent_name", game[2].split("=")[1]);
            game_status.put("game_accepted", game[3].split("=")[1]);
            game_status.put("opponents_turn", game[4].split("=")[1]);
            game_status.put("my_game", game[5].split("=")[1]);
            game_status.put("new_game", game[6].split("=")[1]);
            game_status.put("challenger_name", game[7].split("=")[1]);
            game_status.put("user_id", game[8].split("=")[1]);
            game_status.put("my_user_name", game[9].split("=")[1]);
        }
        notificationGameID = game_status.get("game_id");
        return game_status;
    }
    public void updateGame(final GameActivity game_activity, final GL10 game_gl){

        if(game_status != null && game_status.get("opponents_turn") != null && game_status.get("my_game") != null) {
            if (game_status.get("opponents_turn").equals("0") && game_status.get("my_game").equals("yes")) {  // my turn & my game
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (menu != null) {
                            MenuItem item = menu.findItem(R.id.action_game_status);
                            if (simulation.lastMovedObject instanceof Player && (!item.getTitle().equals(getString(R.string.move_token_or_player)) || !item.getTitle().equals("Bewege deinen Spieler.")) || simulation.lastMovedObject == null) {
                                item.setTitle(getString(R.string.move_token_or_player));
                            } else if (simulation.lastMovedObject instanceof Polynomino) {
                                item.setTitle(R.string.move_player);
                            }
                        }
                    }
                });
                gameLoop.PlayerOnesTurn = true;
                gameUpdated = true;
                if (!downloaded) {
                    Log.d("download", "court is being downloaded");
                    simulation = GameSync.downloadSimulation();
                    simulation.allLocked = false;
                    renderer = new Renderer3D(game_activity, game_gl, simulation.objects);
                    renderer.enableCoordinates(game_gl, simulation.objects);
                    downloaded = true;
                    notificationReceiver = game_status.get("opponent_id");
                    notificationMessage = game_status.get("my_user_name");
                }
            } else if (game_status.get("opponents_turn").equals("0") && game_status.get("my_game").equals("no")) { // my turn & not my game
                gameLoop.PlayerOnesTurn = true;
                simulation.allLocked = true;
                downloaded = false;
                notificationReceiver = game_status.get("user_id");
                notificationMessage = game_status.get("my_user_name");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    if (menu != null) {
                        MenuItem item = menu.findItem(R.id.action_game_status);
                        item.setTitle(game_status.get("challenger_name") + " ist dran.");
                    }
                    }
                });
            } else if (game_status.get("opponents_turn").equals("1") && game_status.get("my_game").equals("yes")) { //  not my turn & my game
                gameLoop.PlayerOnesTurn = false;
                simulation.allLocked = true;
                downloaded = false;
                notificationReceiver = game_status.get("opponent_id");
                notificationMessage = game_status.get("my_user_name");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (menu != null) {
                            MenuItem item = menu.findItem(R.id.action_game_status);
                            item.setTitle(getString(R.string.opponents_turn,game_status.get("opponent_name")));
                        }
                    }
                });
            } else { //  not my turn & not my game
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (menu != null) {
                            MenuItem item = menu.findItem(R.id.action_game_status);
                            if (simulation.lastMovedObject instanceof Player  && (!item.getTitle().equals(getString(R.string.move_token_or_player)) || !item.getTitle().equals("Bewege deinen Spieler.")) || simulation.lastMovedObject == null) {
                                item.setTitle(getString(R.string.move_token_or_player));
                            } else if (simulation.lastMovedObject instanceof Polynomino) {
                                item.setTitle(getString(R.string.move_player));
                            }
                        }
                    }
                });
                gameLoop.PlayerOnesTurn = false;
                gameUpdated = true;
                if (!downloaded) {
                    simulation = GameSync.downloadSimulation();
                    //simulation.player2.isLocked = true;
                    simulation.allLocked = false;
                    renderer = new Renderer3D(game_activity, game_gl, simulation.objects);
                    renderer.enableCoordinates(game_gl, simulation.objects);
                    downloaded = true;
                    notificationReceiver = game_status.get("user_id");
                    notificationMessage = game_status.get("my_user_name");
                }
            }
        }else{
            Log.d("crashed", "crashed while reading game status");
            final Intent intent = new Intent(PolyshiftActivity.this, MainMenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            Bundle error = new Bundle();
            error.putBoolean("error_occured", true);
            intent.putExtras(error);
            startActivity(intent);
            PolyshiftActivity.this.finish();
        }
    }
    public void deleteGame(){
        class DeleteGameThread extends Thread{
            public void run(){
                PHPConnector.doRequest("delete_game.php");
            }
        }
        Thread delete_game_thread = new DeleteGameThread();
        delete_game_thread.start();
        try {
            long waitMillis = 10000;
            while (delete_game_thread.isAlive()) {
                delete_game_thread.join(waitMillis);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void updateScores(final boolean PlayerOnesTurn, final String loserID, final String winnerID, final String winnerName){
        class UpdateScoresThread extends Thread{
            public void run(){
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("playerOnesTurn", "" + ((PlayerOnesTurn) ? 1 : 0)));
                nameValuePairs.add(new BasicNameValuePair("loser_id",loserID));
                nameValuePairs.add(new BasicNameValuePair("winner_id",winnerID));
                PHPConnector.doRequest(nameValuePairs,"update_scores.php");
                String msg = winnerName + PolyshiftActivity.this.getString(R.string.has_won);
                GameSync.SendChangeNotification(loserID, msg, notificationGameID, PolyshiftActivity.class.getName());
            }
        }
        Thread update_scores_thread = new UpdateScoresThread();
        update_scores_thread.start();
        try {
            long waitMillis = 10000;
            while (update_scores_thread.isAlive()) {
                update_scores_thread.join(waitMillis);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    class Update_Game_Thread extends Thread {
        public void run() {
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("game", game_id));
            PHPConnector.doRequest(nameValuePairs, "update_game.php");
        }
    }
    Thread update_game_thread = new Update_Game_Thread();
}
