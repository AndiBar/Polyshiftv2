package hamburg.haw.polyshift.Game;

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

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

import hamburg.haw.polyshift.Adapter.LoginAdapter;
import hamburg.haw.polyshift.Menu.MainMenuActivity;
import hamburg.haw.polyshift.Menu.MyGamesActivity;
import hamburg.haw.polyshift.R;
import hamburg.haw.polyshift.Tools.PHPConnector;


public class TrainingActivity extends GameActivity implements GameListener {

    Player player;
    Player player2;
    Polynomino poly;
    Renderer renderer;
    Simulation simulation;
    OfflineGameLoop gameLoop;
    private String response = "";
    private Menu menu;
    private HashMap<String,String> game_status;
    private Activity activity = this;
    private boolean downloaded = false;
    private boolean statusDownloaded = false;
    private boolean winnerIsAnnounced = false;
    public static boolean statusUpdated = true;
    public boolean gameUpdated = false;
    private String notificationReceiver = "";
    private String notificationMessage = "";
    private String notificationGameID =  "";
    private Context context;
    private LoginAdapter loginAdapter;
    private boolean onBackPressed = false;
    public static ProgressDialog dialog = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = getApplicationContext();
        loginAdapter = new LoginAdapter(context,TrainingActivity.this);
        loginAdapter.handleSessionExpiration();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_OPTIONS_PANEL);

        super.onCreate(savedInstanceState);

        dialog = ProgressDialog.show(TrainingActivity.this, "", "Spieldaten werden geladen", true);

        setGameListener(this);

        setTitle("Polyshift");

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
        return super.onCreateOptionsMenu(menu);
    }
    public void onBackPressed() {
        dialog = ProgressDialog.show(TrainingActivity.this, "", "Spiel wird beendet", true);
        onBackPressed = true;
        final Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void setup(GameActivity activity, GL10 gl) {

        if(!(simulation instanceof Simulation)){

            game_status = new HashMap<String,String>();
            game_status.put("game_id", "1");
            game_status.put("opponent_id", "2");
            game_status.put("opponent_name", "Test");
            game_status.put("game_accepted", "1");
            game_status.put("opponents_turn", "0");
            game_status.put("my_game", "1");
            game_status.put("challenger_name", "Test");
            game_status.put("user_id", "1");
            game_status.put("my_user_name", "Test");

            gameLoop = new OfflineGameLoop("yes");

            simulation = new Simulation(activity);

            for(int i = 0; i < simulation.objects.length; i++) {
                for (int j = 0; j < simulation.objects[0].length; j++) {
                    if (simulation.objects[i][j] instanceof Polynomino) {
                        simulation.objects[i][j].colors = GameSync.recreateColor();
                    }
                }
            }

            gameLoop.setRandomPlayer();
            renderer = new Renderer3D(activity, gl, simulation.objects);
            renderer.enableCoordinates(gl, simulation.objects);

            updateGame(activity, gl);

            dialog.dismiss();
        }

    }

    long start = System.nanoTime();
    int frames = 0;

    @Override
    public void mainLoopIteration(GameActivity activity, GL10 gl) {

        if(!onBackPressed && game_status.size() > 0) {

            renderer.setPerspective(activity, gl);
            renderer.renderLight(gl);
            renderer.renderObjects(activity, gl, simulation.objects);
            simulation.update(activity);
            gameLoop.update(simulation, notificationReceiver, notificationMessage, notificationGameID);

            updateGame(activity, gl);

            if (simulation.hasWinner && !winnerIsAnnounced) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(TrainingActivity.this);
                        if (simulation.winner.isPlayerOne && game_status.get("my_game").equals("yes")) {
                            builder.setMessage("Glückwunsch! Du hast das Spiel gewonnen!");
                            builder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog = ProgressDialog.show(TrainingActivity.this, "", "Spiel wird beendet", true);
                                            final Intent intent = new Intent(TrainingActivity.this, MyGamesActivity.class);
                                            startActivity(intent);
                                            TrainingActivity.this.finish();
                                            dialog.cancel();
                                        }
                                    });
                        } else if (simulation.winner.isPlayerOne && game_status.get("my_game").equals("no")) {
                            builder.setMessage(game_status.get("challenger_name") + " hat das Spiel gewonnen.");
                            builder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            final Intent intent = new Intent(TrainingActivity.this, MyGamesActivity.class);
                                            startActivity(intent);
                                            TrainingActivity.this.finish();
                                            dialog.cancel();
                                        }
                                    });
                        } else if (!simulation.winner.isPlayerOne && game_status.get("my_game").equals("no")) {
                            builder.setMessage("Glückwunsch! Du hast das Spiel gewonnen!");
                            builder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            final Intent intent = new Intent(TrainingActivity.this, MyGamesActivity.class);
                                            startActivity(intent);
                                            TrainingActivity.this.finish();
                                            dialog.cancel();
                                        }
                                    });
                        } else if (!simulation.winner.isPlayerOne && game_status.get("my_game").equals("yes")) {
                            builder.setMessage(game_status.get("opponent_name") + " hat das Spiel gewonnen.");
                            builder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            final Intent intent = new Intent(TrainingActivity.this, MyGamesActivity.class);
                                            startActivity(intent);
                                            TrainingActivity.this.finish();
                                            dialog.cancel();
                                        }
                                    });
                        }
                        builder.show();
                    }
                });
                winnerIsAnnounced = true;

                /*if(activity.isTouched()){
                    activity.finish();
                    startActivity(activity.getIntent());
                }*/
            }


            frames++;
        }
    }

    public void updateGame(final GameActivity game_activity, final GL10 game_gl){

        if(game_status != null && game_status.get("opponents_turn") != null && game_status.get("my_game") != null) {
            if (game_status.get("opponents_turn").equals("0") && game_status.get("my_game").equals("yes")) {  // my turn & my game
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (menu != null) {
                            MenuItem item = menu.findItem(R.id.action_game_status);
                            if (simulation.lastMovedObject instanceof Player && (!item.getTitle().equals("Bewege einen Spielstein oder deinen Spieler.") || !item.getTitle().equals("Bewege deinen Spieler.")) || simulation.lastMovedObject == null) {
                                item.setTitle("Bewege einen Spielstein oder deinen Spieler.");
                            } else if (simulation.lastMovedObject instanceof Polynomino) {
                                item.setTitle("Bewege deinen Spieler.");
                            }
                        }
                    }
                });
                gameLoop.PlayerOnesTurn = true;
                gameUpdated = true;
            } else if (game_status.get("opponents_turn").equals("0") && game_status.get("my_game").equals("no")) { // my turn & not my game
                gameLoop.PlayerOnesTurn = true;
                simulation.allLocked = true;
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (menu != null) {
                            MenuItem item = menu.findItem(R.id.action_game_status);
                            item.setTitle(game_status.get("opponent_name") + " ist dran.");
                        }
                    }
                });
            } else { //  not my turn & not my game
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (menu != null) {
                            MenuItem item = menu.findItem(R.id.action_game_status);
                            if (simulation.lastMovedObject instanceof Player  && (!item.getTitle().equals("Bewege einen Spielstein oder deinen Spieler.") || !item.getTitle().equals("Bewege deinen Spieler.")) || simulation.lastMovedObject == null) {
                                item.setTitle("Bewege einen Spielstein oder deinen Spieler.");
                            } else if (simulation.lastMovedObject instanceof Polynomino) {
                                item.setTitle("Bewege deinen Spieler.");
                            }
                        }
                    }
                });
                gameLoop.PlayerOnesTurn = false;
                gameUpdated = true;
            }
        }
    }

}
