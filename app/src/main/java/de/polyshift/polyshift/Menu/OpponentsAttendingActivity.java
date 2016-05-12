package de.polyshift.polyshift.Menu;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import de.polyshift.polyshift.Menu.Adapter.AcceptOpponentAdapter;
import de.polyshift.polyshift.Tools.LoginTool;
import de.polyshift.polyshift.Tools.Analytics.AnalyticsApplication;
import de.polyshift.polyshift.Game.Sync.GameSync;
import de.polyshift.polyshift.R;
import de.polyshift.polyshift.Tools.AlertDialogs;
import de.polyshift.polyshift.Tools.PHPConnector;

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

/**
 * Zeigt unbestätigte Gegner an und gibt dem Spieler die Möglichkeit diese zu Bestätigen oder
 * abzulehnen.
 *
 * @author helmsa
 *
 */

public class OpponentsAttendingActivity extends ListActivity {
	
    public static ArrayList<HashMap<String, String>> opponents_attending_list = new ArrayList<HashMap<String,String>>();
    public static AcceptOpponentAdapter mAdapter;
    public static ProgressDialog dialog = null;
    public String response = "";
    private Context context;
    private LoginTool loginTool;
    private Tracker mTracker = null;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
   	    super.onCreate(savedInstanceState);
        context = getApplicationContext();
        loginTool = new LoginTool(context,OpponentsAttendingActivity.this);
        loginTool.handleSessionExpiration(this);

        setContentView(R.layout.activity_opponents_attending);
        setTitle(R.string.opponents_attending_title);

        Thread friends_thread = new OpponentsThread();
        friends_thread.start();
        try {
        	long waitMillis = 10000;
        	while (friends_thread.isAlive()) {
        	   friends_thread.join(waitMillis);
        	}
        } catch (InterruptedException e) {
        	e.printStackTrace();
        }
        
       if (opponents_attending_list != null && opponents_attending_list.size() != 0) {
       
	        mAdapter = new AcceptOpponentAdapter(this,
                    opponents_attending_list,
	        		 R.layout.activity_choose_opponent_item,
	                 new String[] {"title"},
	                 new int[] {R.id.title});
	        
	       ListView listView = this.getListView();
	       setListAdapter(mAdapter);
	       //listView.setClickable(true);
	       listView.setFocusableInTouchMode(false);
	       listView.setFocusable(false);
	       listView.setOnItemClickListener(new OnItemClickListener() {
	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	            	
	            	Intent intent = new Intent(OpponentsAttendingActivity.this, MainMenuActivity.class);
		        	int selected = 3;
		        	intent.putExtra("drawerPosition", selected);
		            startActivity(intent);
	            }
	        });
        
       }
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
                //dialog = ProgressDialog.show(OpponentsAttendingActivity.this, "", getString(R.string.dialog_opponent_handling), true);

                Thread add_opponents_thread = new AddOpponentsThread();
                add_opponents_thread.start();
                try {
                    long waitMillis = 10000;
                    while (add_opponents_thread.isAlive()) {
                        add_opponents_thread.join(waitMillis);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (response.split(":").length > 1 && response.split(":")[0].equals("opponent accepted by")) {
                    Log.d("res:", response);
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                    builder.setMessage(R.string.opponents_saved);
                    builder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    final Intent intent = new Intent(OpponentsAttendingActivity.this, ChooseOpponentActivity.class);
                                    startActivity(intent);
                                    OpponentsAttendingActivity.this.finish();
                                    dialog.cancel();
                                }
                            });
                    builder.show();

                } else {
                    AlertDialogs.showAlert(OpponentsAttendingActivity.this, "Fehler", getString(R.string.at_least_one_opponent_not_saved));
                }
                break;

	        case R.id.action_decline:
                new Thread(
                    new Runnable(){
                        public void run(){
                            for(String user: mAdapter.getCheckedUserIDs()) {
                                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                                nameValuePairs.add(new BasicNameValuePair("opponent", user));
                                PHPConnector.doRequest(nameValuePairs, "decline_opponent.php");
                            }
                        }
                    }
                ).start();
                 intent = new Intent(OpponentsAttendingActivity.this, ChooseOpponentActivity.class);
                startActivity(intent);
                OpponentsAttendingActivity.this.finish();
                break;

                default:
                break;

        }
	          return true;
    }
    public void onBackPressed() {
        final Intent intent = new Intent(this, ChooseOpponentActivity.class);
        startActivity(intent);
        this.finish();
    }


	public class OpponentsThread extends Thread{
	  	  public void run(){
	  		String stringResponse = PHPConnector.doRequest("get_opponents_attending.php");
			String[] data_unformatted = stringResponse.split(",");
            opponents_attending_list = new ArrayList<HashMap<String,String>>();
			if(!stringResponse.equals("no opponents found")){
			    for(String item : data_unformatted){
			    	HashMap<String, String>data_map = new HashMap<String, String>();
			    	String[] data_array = item.split(":");
			    	data_map.put("ID", data_array[0]);
			    	data_map.put("title", data_array[1]);
                    opponents_attending_list.add(data_map);
			    }
			}
	  	  }
		}

    public class AddOpponentsThread extends Thread{
        public void run() {
            for (String user : mAdapter.getCheckedUserIDs()) {
                Log.d("user", user);
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("opponent", user));
                response = PHPConnector.doRequest(nameValuePairs, "accept_opponent.php");
                if(response.split(":").length > 1) {
                    GameSync.SendChangeNotification(user, getString(R.string.you_have_been_added, response.split(":")[1]), "", ChooseOpponentActivity.class.getName());
                }
            }
        }
    }
}
