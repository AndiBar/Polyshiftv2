package de.polyshift.polyshift.Game;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.google.android.gms.analytics.Tracker;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import de.polyshift.polyshift.Game.Logic.AiGameLoop;
import de.polyshift.polyshift.Tools.LoginTool;
import de.polyshift.polyshift.Tools.Analytics.AnalyticsApplication;
import de.polyshift.polyshift.Game.Interfaces.GameListener;
import de.polyshift.polyshift.Game.Logic.Simulation;
import de.polyshift.polyshift.Game.Objects.Player;
import de.polyshift.polyshift.Game.Objects.Polynomino;
import de.polyshift.polyshift.Game.Renderer.Renderer;
import de.polyshift.polyshift.Game.Renderer.Renderer3D;
import de.polyshift.polyshift.Game.Sync.GameSync;
import de.polyshift.polyshift.Menu.MainMenuActivity;
import de.polyshift.polyshift.R;
import de.polyshift.polyshift.Tools.MapUtil;
import de.polyshift.polyshift.Tools.PHPConnector;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Diese Klasse ist für die Umsetzung des Tutorial-Spiels zuständig. Sie verbindet die
 * Klassen Renderer, Simulation und GameLoop. Sie rendert das in der Simulation gespeicherte
 * Spielfeld und stellt den aktuellen Spielstatus anhand des GameLoops dar.
 *
 * @author helmsa
 *
 */

public class AiPolyshiftActivity extends GameActivity implements GameListener {

    public static boolean statusUpdated = true;
    public boolean gameUpdated = false;
    public static ProgressDialog dialog = null;
    Renderer renderer;
    Simulation simulation;
    AiGameLoop gameLoop;
    private Menu menu;
    private HashMap<String,String> game_status;
    private Activity activity = this;
    private boolean downloaded = false;
    private boolean statusDownloaded = false;
    private boolean winnerIsAnnounced = false;
    private boolean infoIsAnnounced = false;
    private Context context;
    private LoginTool loginTool;
    private boolean onBackPressed = false;
    private Tracker mTracker = null;
    private boolean tutorial = true;
    private AlertDialog alertDialog;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = getApplicationContext();
        loginTool = new LoginTool(context,AiPolyshiftActivity.this);
        loginTool.handleSessionExpiration(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_OPTIONS_PANEL);

        super.onCreate(savedInstanceState);

        alertDialog = new AlertDialog.Builder(this).create();
        dialog = ProgressDialog.show(AiPolyshiftActivity.this, "", getString(R.string.game_data_is_loading), true);

        setGameListener(this);

        setTitle("Polyshift");

        Log.d("Polyshift", "Polyshift Spiel erstellt");

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        Intent intent = getIntent();
        tutorial = intent.getBooleanExtra("tutorial", true);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState( outState );
        Log.d( "Polyshift", "Polyshift Spielstand gespeichert" );
    }

    @Override
    public void onPause( )
    {
        /*String serializedSimulation = AiGameLoop.serializeSimulation(simulation);
        sharedPreferences.edit().putString("simulation", serializedSimulation).apply();
        if(game_status != null){
            sharedPreferences.edit().putString("game_status", MapUtil.mapToString(game_status)).apply();
        }*/

        super.onPause();
        Log.d( "Polyshift", "Polyshift pausiert" );
    }

    @Override
    public void onResume( )
    {
        super.onResume();
        /*if(!sharedPreferences.getString("simulation", "").isEmpty()) {
            simulation = AiGameLoop.deserializeSimulation(sharedPreferences.getString("simulation", ""));
        }
        String gamestatus = sharedPreferences.getString("game_status", "");
        if(!gamestatus.isEmpty() && gamestatus.length() > 1) {
            Log.d("test", "map: " + sharedPreferences.getString("game_status", ""));
            game_status = MapUtil.stringToMap(sharedPreferences.getString("game_status", ""));
        }*/
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
        menu.findItem(R.id.action_game_status).setTitle(getString(R.string.please_wait));

        MenuItem item = menu.findItem(R.id.action_game_status);
        item.setOnMenuItemClickListener(menuItem -> {
            sharedPreferences.edit().remove("simulation").apply();
            return false;
        });

        return super.onCreateOptionsMenu(menu);
    }
    public void onBackPressed() {
        dialog = ProgressDialog.show(AiPolyshiftActivity.this, "", getString(R.string.game_is_closing), true);
        onBackPressed = true;
        final Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void setup(GameActivity activity, GL10 gl) {

        if(simulation == null){

            game_status = new HashMap<String,String>();
            game_status.put("opponent_name", getString(R.string.red));
            if(tutorial) {
                game_status.put("opponents_turn", "0");
            }
            game_status.put("my_game", "yes");
            game_status.put("my_user_name", getString(R.string.blue));

            simulation = new Simulation(activity);


            /*if(sharedPreferences.getString("simulation", "").isEmpty()){
                String serializedSimulation = AiGameLoop.serializeSimulation(simulation);
                sharedPreferences.edit().putString("simulation", serializedSimulation).apply();
            }else{
                simulation = AiGameLoop.deserializeSimulation(sharedPreferences.getString("simulation", ""));
            }*/

            gameLoop = new AiGameLoop("yes");

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

            if (simulation.hasWinner && !winnerIsAnnounced && !alertDialog.isShowing()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(AiPolyshiftActivity.this);
                        if (simulation.winner.isPlayerOne && game_status.get("my_game").equals("yes")) {
                            if(tutorial || (LoginTool.username != null && !LoginTool.username.isEmpty())) {
                                builder.setMessage(getString(R.string.you_won));

                                builder.setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog = ProgressDialog.show(AiPolyshiftActivity.this, "", getString(R.string.game_is_closing), true);
                                                final Intent intent = new Intent(AiPolyshiftActivity.this, MainMenuActivity.class);
                                                startActivity(intent);
                                                AiPolyshiftActivity.this.finish();
                                                dialog.cancel();
                                            }
                                        });
                            }else{
                                builder.setMessage(getString(R.string.you_won) + "\n" + getString(R.string.add_user_name));
                                final EditText input = new EditText(AiPolyshiftActivity.this);
                                builder.setView(input);
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String username  = input.getText().toString();
                                        if(!username.isEmpty()){
                                            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                                            nameValuePairs.add(new BasicNameValuePair("username", username));
                                            PHPConnector.doObservableRequest(nameValuePairs, "update_user.php")
                                                    .subscribeOn(Schedulers.io())
                                                    .subscribe(new Subscriber<String>() {
                                                        @Override
                                                        public void onCompleted() {
                                                        }

                                                        @Override
                                                        public void onError(Throwable e) {

                                                        }

                                                        @Override
                                                        public void onNext(String s) {

                                                        }
                                                    });
                                        }
                                        final Intent intent = new Intent(AiPolyshiftActivity.this, MainMenuActivity.class);
                                        startActivity(intent);
                                        AiPolyshiftActivity.this.finish();
                                        dialog.cancel();
                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog = ProgressDialog.show(AiPolyshiftActivity.this, "", getString(R.string.game_is_closing), true);
                                        final Intent intent = new Intent(AiPolyshiftActivity.this, MainMenuActivity.class);
                                        startActivity(intent);
                                        AiPolyshiftActivity.this.finish();
                                        dialog.cancel();
                                    }
                                });
                            }
                            if(!tutorial){
                                updateScores(true);
                            }
                        } else if (!simulation.winner.isPlayerOne && game_status.get("my_game").equals("yes")) {
                            builder.setMessage(game_status.get("opponent_name") + getString(R.string.has_won));
                            builder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            final Intent intent = new Intent(AiPolyshiftActivity.this, MainMenuActivity.class);
                                            startActivity(intent);
                                            AiPolyshiftActivity.this.finish();
                                            dialog.cancel();
                                        }
                                    });
                            if(!tutorial){
                                updateScores(false);
                            }
                        }
                        alertDialog = builder.show();
                    }
                });
                winnerIsAnnounced = true;
            }

            if(gameLoop.RoundFinished && !infoIsAnnounced && tutorial && gameLoop.PlayerOnesTurn && !alertDialog.isShowing()){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(AiPolyshiftActivity.this);
                        if (gameLoop.roundCount == 0 && tutorial && !alertDialog.isShowing()) {
                            tutorial = false;
                            builder.setMessage(R.string.rule_one);
                            builder.setPositiveButton(getString(R.string.yes),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            tutorial = true;
                                            dialog.cancel();
                                        }
                                    });
                            builder.setNegativeButton(getString(R.string.no),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            alertDialog = builder.show();
                        } else if (gameLoop.roundCount == 1) {
                            builder.setMessage(R.string.rule_two);
                            builder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            alertDialog = builder.show();
                        } else if (gameLoop.roundCount == 2) {
                            builder.setMessage(R.string.rule_three);
                            builder.setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                            alertDialog = builder.show();
                        } else if (gameLoop.roundCount == 3) {
                            builder.setMessage(R.string.rule_four);
                            builder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            alertDialog = builder.show();
                        } else if (gameLoop.roundCount == 4) {
                            builder.setMessage(R.string.rule_five);
                            builder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            alertDialog = builder.show();
                        } else if (gameLoop.roundCount == 5) {
                            builder.setMessage(R.string.rule_six);
                            builder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            alertDialog = builder.show();
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

    private void updateScores(boolean winner) {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("winner", winner ? "true" : "false"));
        PHPConnector.doSingleRequest(nameValuePairs ,"update_ai_scores.php")
                .subscribeOn(Schedulers.io())
                .subscribe(new SingleSubscriber<String>() {
                    @Override
                    public void onSuccess(String value) {

                    }

                    @Override
                    public void onError(Throwable error) {

                    }
                });
    }

    public void updateGame(final GameActivity game_activity, final GL10 game_gl){

        if(game_status != null && game_status.get("opponents_turn") != null && game_status.get("my_game") != null) {
            if(gameLoop.aiRunning){
                runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                      MenuItem item = menu.findItem(R.id.action_game_status);
                      item.setTitle(getString(R.string.please_wait));
                  }
              });
            }else if (game_status.get("opponents_turn").equals("0") && game_status.get("my_game").equals("yes")) {  // my turn & my game
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
