package de.polyshift.polyshift.Menu;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import de.polyshift.polyshift.Menu.Adapter.AcceptGameAdapter;

import de.polyshift.polyshift.Tools.LoginTool;
import de.polyshift.polyshift.Tools.Analytics.AnalyticsApplication;
import de.polyshift.polyshift.Game.Sync.GameSync;
import de.polyshift.polyshift.Game.PolyshiftActivity;
import de.polyshift.polyshift.R;
import de.polyshift.polyshift.Tools.AlertDialogs;
import de.polyshift.polyshift.Tools.PHPConnector;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


public class GamesAttendingActivity extends ListActivity {

    public static AcceptGameAdapter mAdapter;
    public static ProgressDialog dialog = null;
    public String response = "";
    private static Context context;
    private static String opponentId  = "";
    private static String opponentName = "";
    private LoginTool loginTool;
    private Tracker mTracker = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	   super.onCreate(savedInstanceState);
        context = getApplicationContext();
        loginTool = new LoginTool(context,GamesAttendingActivity.this);
        loginTool.handleSessionExpiration(this);


       setContentView(R.layout.activity_opponents_attending);
       setTitle(getString(R.string.new_game));

       if (MyGamesActivity.games_attending_list == null || MyGamesActivity.games_attending_list.size() == 0) {
           Thread my_games_thread = new GamesAttendingThread();
           my_games_thread.start();
           try {
               long waitMillis = 10000;
               while (my_games_thread.isAlive()) {
                   my_games_thread.join(waitMillis);
               }
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
       }
       if (PolyshiftActivity.dialog != null) {
           try {
               PolyshiftActivity.dialog.dismiss();
           } catch (IllegalArgumentException e) {
               e.printStackTrace();
           }
       }


       mAdapter = new AcceptGameAdapter(this,
                MyGamesActivity.games_attending_list,
                 R.layout.activity_choose_opponent_item,
                 new String[] {"opponent_name"},
                 new int[] {R.id.title});

       ListView listView = this.getListView();
       setListAdapter(mAdapter);
       listView.setFocusableInTouchMode(false);
       listView.setFocusable(false);
       listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(GamesAttendingActivity.this, MyGamesActivity.class);
                startActivity(intent);
                GamesAttendingActivity.this.finish();
            }
        });


    AnalyticsApplication application = (AnalyticsApplication) getApplication();
    mTracker = application.getDefaultTracker();
    mTracker.setScreenName(getClass().getName());
    mTracker.send(new HitBuilders.ScreenViewBuilder().build());
	}

	// Action Bar Button
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_opponents_attending, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent intent;
        switch (item.getItemId()) {
            case R.id.action_accept:
                dialog = ProgressDialog.show(GamesAttendingActivity.this, "",getString(R.string.crearing_game), true);

                Thread add_games_thread = new AddGamesThread();
                add_games_thread.start();
                try {
                    long waitMillis = 10000;
                    while (add_games_thread.isAlive()) {
                        add_games_thread.join(waitMillis);
                    }
                    dialog.dismiss();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (response.equals("game accepted")) {
                    Log.d("res:", response);
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.challenge_accepted);
                    builder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog = ProgressDialog.show(GamesAttendingActivity.this, "", getString(R.string.game_is_starting), true);
                                    final Intent intent = new Intent(GamesAttendingActivity.this, PolyshiftActivity.class);
                                    startActivity(intent);
                                    GamesAttendingActivity.this.finish();
                                    dialog.cancel();
                                }
                    });
                    builder.show();

                } else {
                    AlertDialogs.showAlert(GamesAttendingActivity.this, getString(R.string.error), getString(R.string.game_could_not_be_started));
                }
                break;

	        case R.id.action_decline:
                new Thread(
                    new Runnable(){
                        public void run(){
                            for(String game_id: mAdapter.getCheckedGameIDs()) {
                                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                                nameValuePairs.add(new BasicNameValuePair("game", game_id));
                                PHPConnector.doRequest(nameValuePairs, "decline_game.php");
                            }
                        }
                    }
                ).start();
                 intent = new Intent(GamesAttendingActivity.this, MainMenuActivity.class);
                startActivity(intent);
                GamesAttendingActivity.this.finish();
                break;

                default:
                break;

        }
	          return true;

    }
    public void onBackPressed() {
        final Intent intent = new Intent(this, MyGamesActivity.class);
        startActivity(intent);
        this.finish();
    }

    public class AddGamesThread extends Thread{
        public void run() {
            for (String game_id : mAdapter.getCheckedGameIDs()) {
                Log.d("game",game_id);
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("game", game_id));
                for(HashMap<String,String> map : MyGamesActivity.games_attending_list){
                    if(map.get("game_id").equals(game_id)){
                        opponentName = map.get("my_user_name");
                        opponentId = map.get("opponent_id");
                        break;
                    }
                }
                response = PHPConnector.doRequest(nameValuePairs, "accept_game.php");
                if (response.equals("game accepted")) {
                    GameSync.SendChangeNotification(opponentId, opponentName + getString(R.string.accepted_your_challenge), game_id, PolyshiftActivity.class.getName());
                }
            }
        }
    }
    public class GamesAttendingThread extends Thread{
        public void run() {
            String stringResponse = PHPConnector.doRequest("get_games_attending.php");
            String[] data_unformatted = stringResponse.split(",");
            MyGamesActivity.games_attending_list = new ArrayList<HashMap<String, String>>();
            if (!stringResponse.equals("no games found")) {
                if (!(stringResponse.split(":").length == 1)) {
                    for (String item : data_unformatted) {
                        HashMap<String, String> data_map = new HashMap<String, String>();
                        String[] data_array = item.split(":");
                        data_map.put("game_id", data_array[0]);
                        data_map.put("opponent_id", data_array[1].split("=")[1]);
                        data_map.put("opponent_name", data_array[2].split("=")[1]);
                        data_map.put("game_accepted", data_array[3].split("=")[1]);
                        data_map.put("opponents_turn", data_array[4].split("=")[1]);
                        data_map.put("my_game", data_array[5].split("=")[1]);
                        data_map.put("my_user_name", data_array[6].split("=")[1]);
                        MyGamesActivity.games_attending_list.add(data_map);
                    }
                } else if (stringResponse.equals("not logged in.")) {
                    context = getApplicationContext();
                    loginTool = new LoginTool(context, GamesAttendingActivity.this);
                    loginTool.userLoginStoredCredentials();

                } else if (stringResponse.equals("no games found")) {

                }
            }
        }
    }
}
