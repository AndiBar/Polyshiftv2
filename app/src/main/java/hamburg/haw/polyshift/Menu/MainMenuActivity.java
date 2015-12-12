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
import android.widget.ImageView;
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
    Button scoresButton;
    Button logoutButton;
    Button quitGameButton;
    ImageView backgroundLogo;
    public static ProgressDialog dialog = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Bundle data = getIntent().getExtras();

        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Holo_NoActionBar);
        setContentView(R.layout.activity_main_menu);

        if (HandleSharedPreferences.checkfirstStart(MainMenuActivity.this)) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainMenuActivity.this);
            builder.setMessage(R.string.first_time);
            builder = builder.setPositiveButton(MainMenuActivity.this.getString(R.string.yes),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(MainMenuActivity.this, TrainingActivity.class);
                            startActivity(intent);
                            MainMenuActivity.this.finish();
                        }
                    });
            builder.setNegativeButton("Nein",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainMenuActivity.this);
                            builder.setMessage(R.string.start_tutorial_later);
                            builder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            builder.show();
                        }
                    });
            builder.show();

        }

        newGameButton = (Button) findViewById(R.id.new_game_button);
        myGamesButton = (Button) findViewById(R.id.my_games_button);
        scoresButton = (Button) findViewById(R.id.scores_button);
        logoutButton = (Button) findViewById(R.id.logout_button);
        quitGameButton = (Button) findViewById(R.id.quit_game_button);
        backgroundLogo = (ImageView) findViewById(R.id.background_logo);

        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ChooseOpponentActivity.class);
                startActivity(intent);
                MainMenuActivity.this.finish();
            }
        });

        backgroundLogo.setOnClickListener(new View.OnClickListener() {
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
                Intent intent = new Intent(v.getContext(), MyGamesActivity.class);
                startActivity(intent);
                MainMenuActivity.this.finish();
            }
        });

        scoresButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ScoresActivity.class);
                startActivity(intent);
                MainMenuActivity.this.finish();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HandleSharedPreferences.setUserCredentials(MainMenuActivity.this, "", "");
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

        if (data != null && data.getBoolean("error_occured")) {
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