package hamburg.haw.polyshift.Menu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import hamburg.haw.polyshift.R;
import hamburg.haw.polyshift.Tools.AlertDialogs;

/**
 * Created by Andi on 12.03.2015.
 */
public class MainMenuActivity extends Activity {

    Button newGameButton;
    Button myGamesButton;
    Button logoutButton;
    Button quitGameButton;
    private static boolean crashed = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        if(crashed){
            AlertDialogs.showAlert(this, "Fehler","Bei der Übertragung ist ein Fehler aufgetreten. Das Spiel wurde beendet.");
        }
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Holo_NoActionBar);
        setContentView(R.layout.activity_main_menu);

        newGameButton = (Button)findViewById(R.id.new_game_button);
        myGamesButton = (Button)findViewById(R.id.my_games_button);
        logoutButton = (Button)findViewById(R.id.logout_button);
        quitGameButton = (Button)findViewById(R.id.quit_game_button);

        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ChooseOpponentActivity.class);
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

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), WelcomeActivity.class);
                startActivity(intent);
                MainMenuActivity.this.finish();
                WelcomeActivity.userLogout();
            }
        });

        quitGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainMenuActivity.this.finish();
            }
        });
    }
    public static void setCrashed(){
        crashed = true;
    }
}