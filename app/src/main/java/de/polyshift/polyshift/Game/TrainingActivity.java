package de.polyshift.polyshift.Game;

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

import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

import de.polyshift.polyshift.Tools.LoginTool;
import de.polyshift.polyshift.Tools.Analytics.AnalyticsApplication;
import de.polyshift.polyshift.Game.Interfaces.GameListener;
import de.polyshift.polyshift.Game.Logic.OfflineGameLoop;
import de.polyshift.polyshift.Game.Logic.Simulation;
import de.polyshift.polyshift.Game.Objects.Player;
import de.polyshift.polyshift.Game.Objects.Polynomino;
import de.polyshift.polyshift.Game.Renderer.Renderer;
import de.polyshift.polyshift.Game.Renderer.Renderer3D;
import de.polyshift.polyshift.Game.Sync.GameSync;
import de.polyshift.polyshift.Menu.MainMenuActivity;
import de.polyshift.polyshift.R;


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
    private boolean infoIsAnnounced = false;
    public static boolean statusUpdated = true;
    public boolean gameUpdated = false;
    private Context context;
    private LoginTool loginTool;
    private boolean onBackPressed = false;
    public static ProgressDialog dialog = null;
    private Tracker mTracker = null;
    private boolean show_rules = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = getApplicationContext();
        loginTool = new LoginTool(context,TrainingActivity.this);
        loginTool.handleSessionExpiration(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_OPTIONS_PANEL);

        super.onCreate(savedInstanceState);

        dialog = ProgressDialog.show(TrainingActivity.this, "", getString(R.string.game_data_is_loading), true);

        setGameListener(this);

        setTitle("Polyshift");

        Log.d("Polyshift", "Polyshift Spiel erstellt");

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
        dialog = ProgressDialog.show(TrainingActivity.this, "", getString(R.string.game_is_closing), true);
        onBackPressed = true;
        final Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void setup(GameActivity activity, GL10 gl) {

        if(!(simulation instanceof Simulation)){

            game_status = new HashMap<String,String>();
            game_status.put("opponent_name", getString(R.string.red));
            game_status.put("opponents_turn", "0");
            game_status.put("my_game", "yes");
            game_status.put("my_user_name", getString(R.string.blue));

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
            gameLoop.update(simulation);

            if(!statusUpdated){
                if(!gameLoop.PlayerOnesTurn) {
                    game_status.put("opponents_turn", "1");
                }else{
                    game_status.put("opponents_turn", "0");
                }
                updateGame(activity, gl);
            }

            if (simulation.hasWinner && !winnerIsAnnounced) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(TrainingActivity.this);
                        if (simulation.winner.isPlayerOne && game_status.get("my_game").equals("yes")) {
                            builder.setMessage(game_status.get("my_user_name") + getString(R.string.has_won));
                            builder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog = ProgressDialog.show(TrainingActivity.this, "", getString(R.string.game_is_closing), true);
                                            final Intent intent = new Intent(TrainingActivity.this, MainMenuActivity.class);
                                            startActivity(intent);
                                            TrainingActivity.this.finish();
                                            dialog.cancel();
                                        }
                                    });
                        } else if (!simulation.winner.isPlayerOne && game_status.get("my_game").equals("yes")) {
                            builder.setMessage(game_status.get("opponent_name") + getString(R.string.has_won));
                            builder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            final Intent intent = new Intent(TrainingActivity.this, MainMenuActivity.class);
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
            }

            if(gameLoop.RoundFinished && !infoIsAnnounced && show_rules){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(TrainingActivity.this);
                        if (gameLoop.roundCount == 0 && show_rules) {
                            show_rules = false;
                            builder.setMessage(R.string.rule_one);
                            builder.setPositiveButton(getString(R.string.yes),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            show_rules = true;
                                            dialog.cancel();
                                        }
                                    });
                            builder.setNegativeButton(getString(R.string.no),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            builder.show();
                        } else if (gameLoop.roundCount == 1) {
                            builder.setMessage(R.string.rule_two);
                            builder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            builder.show();
                        } else if (gameLoop.roundCount == 2) {
                            builder.setMessage(R.string.rule_three);
                            builder.setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                            builder.show();
                        } else if (gameLoop.roundCount == 3) {
                            builder.setMessage(R.string.rule_four);
                            builder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            builder.show();
                        } else if (gameLoop.roundCount == 4) {
                            builder.setMessage(R.string.rule_five);
                            builder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            builder.show();
                        } else if (gameLoop.roundCount == 5) {
                            builder.setMessage(R.string.rule_six);
                            builder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            builder.show();
                        }
                    }
                });
                infoIsAnnounced = true;
            }

            if(!gameLoop.RoundFinished){
                infoIsAnnounced = false;
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
                            if (simulation.lastMovedObject instanceof Player && (!item.getTitle().equals(getString(R.string.move_token_or_player)) || !item.getTitle().equals(getString(R.string.move_player))) || simulation.lastMovedObject == null) {
                                item.setTitle(R.string.blues_turn);
                            }
                        }
                    }
                });
                gameLoop.PlayerOnesTurn = true;
                gameUpdated = true;
            } else if (game_status.get("opponents_turn").equals("1") && game_status.get("my_game").equals("yes")) { //  not my turn & my game
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (menu != null) {
                            MenuItem item = menu.findItem(R.id.action_game_status);
                            item.setTitle(R.string.reds_turn);
                        }
                    }
                });
                gameLoop.PlayerOnesTurn = false;
                gameUpdated = true;
            }
        }
    }

}
