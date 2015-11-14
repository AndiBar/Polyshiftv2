package hamburg.haw.polyshift.Menu;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import hamburg.haw.polyshift.Adapter.AcceptGameAdapter;

import hamburg.haw.polyshift.Adapter.LoginAdapter;
import hamburg.haw.polyshift.Game.GameSync;
import hamburg.haw.polyshift.Game.PolyshiftActivity;
import hamburg.haw.polyshift.R;
import hamburg.haw.polyshift.Tools.AlertDialogs;
import hamburg.haw.polyshift.Tools.PHPConnector;

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


public class GamesAttendingActivity extends ListActivity {

    public static AcceptGameAdapter mAdapter;
    public static ProgressDialog dialog = null;
    public String response = "";
    private static Context context;
    private static String opponentId  = "";
    private static String opponentName = "";
    private LoginAdapter loginAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	   super.onCreate(savedInstanceState);
        context = getApplicationContext();
        loginAdapter = new LoginAdapter(context,GamesAttendingActivity.this);
        loginAdapter.handleSessionExpiration();


       setContentView(R.layout.activity_opponents_attending);
       setTitle(getString(R.string.new_game));

       if (MyGamesActivity.games_attending_list != null && MyGamesActivity.games_attending_list.size() != 0) {

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
	            	SaveValue.setSelectedFriendName(MyGamesActivity.games_attending_list.get(position).get("opponent_name"));
	            	Intent intent = new Intent(GamesAttendingActivity.this, MyGamesActivity.class);
		            startActivity(intent);
                    GamesAttendingActivity.this.finish();
	            }
	        });

       }
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
                dialog = ProgressDialog.show(GamesAttendingActivity.this, "","Spiel wird erstellt", true);

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
                    builder.setMessage("Herausforderung wurde angenommen. Spiel wird gestartet.");
                    builder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog = ProgressDialog.show(GamesAttendingActivity.this, "", "Spiel wird gestartet", true);
                                    final Intent intent = new Intent(GamesAttendingActivity.this, PolyshiftActivity.class);
                                    startActivity(intent);
                                    GamesAttendingActivity.this.finish();
                                    dialog.cancel();
                                }
                    });
                    builder.show();

                } else {
                    AlertDialogs.showAlert(GamesAttendingActivity.this, "Fehler", "Das Spiel konnte nicht gestartet werden.");
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
               GameSync.SendChangeNotification(opponentId,opponentName + " hat deine Herausforderung angenommen","");
            }
        }
    }
}
