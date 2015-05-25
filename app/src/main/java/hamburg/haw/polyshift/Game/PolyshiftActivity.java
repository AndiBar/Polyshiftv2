package hamburg.haw.polyshift.Game;

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
import android.view.Window;
import android.view.WindowManager;

import hamburg.haw.polyshift.Adapter.LoginAdapter;
import hamburg.haw.polyshift.Adapter.MyGamesAdapter;
import hamburg.haw.polyshift.Menu.MainMenuActivity;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import hamburg.haw.polyshift.R;
import hamburg.haw.polyshift.Menu.MyGamesActivity;
import hamburg.haw.polyshift.Tools.AlertDialogs;
import hamburg.haw.polyshift.Tools.PHPConnector;

import java.util.ArrayList;
import java.util.HashMap;


public class PolyshiftActivity extends GameActivity implements GameListener {

    Player player;
    Player player2;
    Polynomino poly;
    Renderer renderer;
    StartScreen startScreen;
    EndScreen endScreen;
    Simulation simulation;
    GameLoop gameLoop;
    private String response = "";
    private Menu menu;
    private HashMap<String,String> game_status;
    private Activity activity = this;
    private boolean downloaded = false;
    private boolean statusDownloaded = false;
    private boolean winnerIsAnnounced = false;
    private Thread game_status_thread = new GameStatusThread();
    private String notificationReceiver = "";
    private String notificationMessage = "";
    private Context context;
    private LoginAdapter loginAdapter;
    private boolean onBackPressed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = getApplicationContext();
        loginAdapter = new LoginAdapter(context,PolyshiftActivity.this);
        loginAdapter.handleSessionExpiration();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_OPTIONS_PANEL);

        super.onCreate(savedInstanceState);

        setGameListener(this);

        Log.d( "Polyshift", "Polyshift Spiel erstellt");
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
        Log.d( "Polyshift", "Polyshift pausiert" );
    }

    @Override
    public void onResume( )
    {
        super.onResume();
        Log.d( "Polyshift", "Polyshift wiederhergestellt" );
    }

    @Override
    public void onDestroy( )
    {
        super.onDestroy();
        Log.d( "Polyshift", "Polyshift beendet" );
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_status, menu);
        this.menu = menu;
        return true;
    }
    public void onBackPressed() {
        onBackPressed = true;
        final Intent intent = new Intent(this, MyGamesActivity.class);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void setup(GameActivity activity, GL10 gl) {

        if(!(simulation instanceof Simulation)){
            startScreen = new StartScreen(gl, activity);
            endScreen = new EndScreen(gl, activity);

            game_status = getGameStatus();

            gameLoop = new GameLoop(game_status.get("my_game"));

            if(game_status.get("new_game").equals("1")){
                simulation = new Simulation(activity);
                gameLoop.setRandomPlayer();
                GameSync.uploadSimulation(simulation);
            }

            simulation = GameSync.downloadSimulation();
            renderer = new Renderer3D(activity, gl, simulation.objects);
            renderer.enableCoordinates(gl, simulation.objects);
            simulation.player.isLocked = true;
            simulation.player2.isLocked = true;

            updateGame(activity, gl);

        }

    }

    long start = System.nanoTime();
    int frames = 0;

    @Override
    public void mainLoopIteration(GameActivity activity, GL10 gl) {

            renderer.setPerspective(activity, gl);
            renderer.renderLight(gl);
            renderer.renderObjects(activity, gl, simulation.objects);
            simulation.update(activity);
            gameLoop.update(simulation,notificationReceiver,notificationMessage);

            if(gameLoop.RoundFinished && simulation.winner == null && !onBackPressed) {
                if (System.nanoTime() - start > 1000000000){
                    game_status = getGameStatus();
                    updateGame(activity, gl);
                    statusDownloaded = true;
                    start = System.nanoTime();
                }
            }


            if(simulation.hasWinner && !winnerIsAnnounced){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(PolyshiftActivity.this);
                        if(simulation.winner.isPlayerOne && game_status.get("my_game").equals("yes")){
                            builder.setMessage("Glückwunsch! Du hast das Spiel gewonnen!");
                        }
                        else if(simulation.winner.isPlayerOne && game_status.get("my_game").equals("no")){
                            builder.setMessage(game_status.get("challenger_name") + " hat das Spiel gewonnen.");
                        }
                        else if(!simulation.winner.isPlayerOne && game_status.get("my_game").equals("no")){
                            builder.setMessage("Glückwunsch! Du hast das Spiel gewonnen!");
                        }
                        else if(!simulation.winner.isPlayerOne && game_status.get("my_game").equals("yes")){
                            builder.setMessage(game_status.get("opponent_name") + " hat das Spiel gewonnen.");
                        }
                        builder.setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        final Intent intent = new Intent(PolyshiftActivity.this, MyGamesActivity.class);
                                        startActivity(intent);
                                        PolyshiftActivity.this.finish();
                                        dialog.cancel();
                                        deleteGame();
                                    }
                                });
                        builder.show();
                    }
                });
                winnerIsAnnounced = true;

                endScreen.setWinner(simulation.winner);
                endScreen.render(gl, activity);
                endScreen.update(activity);

                /*if(activity.isTouched()){
                    activity.finish();
                    startActivity(activity.getIntent());
                }*/
            }



        frames++;
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
        if(response.equals("error") || response.split(":").length == 1){
            Log.d("crashed","crashed");
            MainMenuActivity.setCrashed();
            final Intent intent = new Intent(PolyshiftActivity.this, MainMenuActivity.class);
            startActivity(intent);
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
        return game_status;
    }
    public void updateGame(final GameActivity game_activity, final GL10 game_gl){

        if (game_status.get("opponents_turn").equals("0") && game_status.get("my_game").equals("yes")) {  // my turn & my game
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setTitle("Du bist dran!");
                }
            });
            gameLoop.PlayerOnesTurn = true;
            if(!downloaded){
                Log.d("download", "court is being downloaded");
                simulation = GameSync.downloadSimulation();
                simulation.allLocked = false;
                //simulation.player.isLocked = true;
                renderer = new Renderer3D(game_activity, game_gl, simulation.objects);
                renderer.enableCoordinates(game_gl, simulation.objects);
                downloaded = true;
                notificationReceiver = game_status.get("opponent_id");
                notificationMessage = game_status.get("my_user_name");
            }
        } else if (game_status.get("opponents_turn").equals("0") && game_status.get("my_game").equals("no")) { // my turn & not my game
            gameLoop.PlayerOnesTurn = true;
            //simulation = GameSync.downloadSimulation();
            simulation.allLocked = true;
            downloaded = false;
            notificationReceiver = game_status.get("user_id");
            notificationMessage = game_status.get("my_user_name");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setTitle(game_status.get("challenger_name") + " ist dran.");
                }
            });
        } else if (game_status.get("opponents_turn").equals("1") && game_status.get("my_game").equals("yes")) { //  not my turn & my game
            gameLoop.PlayerOnesTurn = false;
            //simulation = GameSync.downloadSimulation();
            simulation.allLocked = true;
            downloaded = false;
            notificationReceiver = game_status.get("opponent_id");
            notificationMessage = game_status.get("my_user_name");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setTitle(game_status.get("opponent_name") + " ist dran.");
                }
            });
        } else { //  not my turn & not my game
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setTitle("Du bist dran!");
                }
            });
            gameLoop.PlayerOnesTurn = false;
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


}
