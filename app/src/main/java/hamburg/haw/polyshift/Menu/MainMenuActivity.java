package hamburg.haw.polyshift.Menu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import hamburg.haw.polyshift.Adapter.LoginAdapter;
import hamburg.haw.polyshift.Game.TrainingActivity;
import hamburg.haw.polyshift.R;
import hamburg.haw.polyshift.Tools.AlertDialogs;
import hamburg.haw.polyshift.Tools.PasswordHash;

/**
 * Created by Andi on 12.03.2015.
 */
public class MainMenuActivity extends Activity {

    Button newGameButton;
    Button myGamesButton;
    Button tutorialButton;
    Button scoresButton;
    Button logoutButton;
    Button quitGameButton;
    private static Context context;
    private LoginAdapter loginAdapter;
    public static ProgressDialog dialog = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        context = getApplicationContext();
        loginAdapter = new LoginAdapter(context,MainMenuActivity.this);
        loginAdapter.handleSessionExpiration();
        Bundle data = getIntent().getExtras();

        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Holo_NoActionBar);
        setContentView(R.layout.activity_main_menu);

        newGameButton = (Button)findViewById(R.id.new_game_button);
        myGamesButton = (Button)findViewById(R.id.my_games_button);
        tutorialButton = (Button)findViewById(R.id.tutorial_button);
        scoresButton = (Button)findViewById(R.id.scores_button);
        logoutButton = (Button)findViewById(R.id.logout_button);
        quitGameButton = (Button)findViewById(R.id.quit_game_button);

        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(MainMenuActivity.this, "", "Gegner werden geladen", true);
                Intent intent = new Intent(v.getContext(), ChooseOpponentActivity.class);
                startActivity(intent);
                MainMenuActivity.this.finish();
            }
        });

        tutorialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), TrainingActivity.class);
                startActivity(intent);
                MainMenuActivity.this.finish();
            }
        });

        myGamesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(MainMenuActivity.this, "", "Spiele werden geladen", true);
                Intent intent = new Intent(v.getContext(), MyGamesActivity.class);
                startActivity(intent);
                MainMenuActivity.this.finish();
            }
        });

        scoresButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(MainMenuActivity.this, "", "Statistiken werden geladen", true);
                Intent intent = new Intent(v.getContext(), ScoresActivity.class);
                startActivity(intent);
                MainMenuActivity.this.finish();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HandleSharedPreferences.setUserCredentials(context,"","");
                Intent intent = new Intent(v.getContext(), WelcomeActivity.class);
                startActivity(intent);
                MainMenuActivity.this.finish();
                WelcomeActivity.userLogout();
            }
        });

        quitGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });

        if(data != null && data.getBoolean("error_occured")) {
            MainMenuActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainMenuActivity.this);
                    builder.setMessage("Bei der Übertragung ist ein Fehler aufgetreten.");
                    builder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    builder.show();
                }
            });
        }
    }
}