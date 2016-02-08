package de.polyshift.polyshift.Menu;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import de.polyshift.polyshift.Adapter.LoginAdapter;
import de.polyshift.polyshift.Analytics.AnalyticsApplication;
import de.polyshift.polyshift.Game.GameSync;
import de.polyshift.polyshift.R;
import de.polyshift.polyshift.Tools.PHPConnector;

public class NewOpponentActivity extends Activity {
	
	private String username;
    private Context context;
    private LoginAdapter loginAdapter;
    private String response;
	private ArrayList users_list;
	private boolean sending_success;
	private Tracker mTracker = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        context = getApplicationContext();
		loginAdapter = new LoginAdapter(context, NewOpponentActivity.this);
        loginAdapter.handleSessionExpiration(this);
		sending_success = false;
        setContentView(R.layout.activity_new_opponent);
        setTitle(R.string.new_opponent);
		AnalyticsApplication application = (AnalyticsApplication) getApplication();
		mTracker = application.getDefaultTracker();

		mTracker.setScreenName(getClass().getName());
		mTracker.send(new HitBuilders.ScreenViewBuilder().build());

		Thread users_thread = new UsersThread();
		users_thread.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.add_opponent, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_addopponent:
	        		Thread thread = new Thread(new Runnable(){
						@Override
						public void run() {
				        	ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
							nameValuePairs.add(new BasicNameValuePair("opponent",username));
							response = PHPConnector.doRequest(nameValuePairs, "add_opponent.php");
						}
	                      		});
	        		thread.start();
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

            if(response.equals("kein Gegner gefunden")) {
                builder.setMessage(R.string.user_not_found);
            }else if(response.equals("gleiche Person")){
                builder.setMessage(R.string.do_not_add_yourself);
            }else if(response.equals("bereits verschickt")){
                builder.setMessage(context.getString(R.string.already_sent, username));
			}else if(response.equals("bereits befeindet")){
				builder.setMessage(context.getString(R.string.already_opponent, username));
            }else{
                builder.setMessage(context.getString(R.string.invitation_sent, username));
				sending_success = true;
				if(response.split(":").length > 2){
					GameSync.SendChangeNotification(response.split(":")[1], getString(R.string.request_sent, response.split(":")[2]), "", OpponentsAttendingActivity.class.getName());
				}
            }
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
							if(sending_success) {
								Intent intent = new Intent(NewOpponentActivity.this, ChooseOpponentActivity.class);
								startActivity(intent);
								NewOpponentActivity.this.finish();
							}
                        }
                    });
            builder.show();
            return true;

	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
    public void onBackPressed() {
        final Intent intent = new Intent(this, ChooseOpponentActivity.class);
        startActivity(intent);
        this.finish();
    }

	public class UsersThread extends Thread {
		public void run() {

			String stringResponse = PHPConnector.doRequest("get_users.php");
			String[] data_unformatted = stringResponse.split(",");
			users_list = new ArrayList<String>();
			if (!stringResponse.equals("no users found")) {
				if (!(stringResponse.split(";").length == 1)) {
					for (String item : data_unformatted) {
						HashMap<String, String> data_map = new HashMap<String, String>();
						String[] data_array = item.split(";");
						users_list.add(data_array[2].split("=")[1]);

						runOnUiThread(new Runnable() {
							@Override
							public void run() {

								AutoCompleteTextView autocomplete = (AutoCompleteTextView)
										findViewById(R.id.new_opponent_view);

								ArrayAdapter<String> adapter = new ArrayAdapter<String>
										(NewOpponentActivity.this,R.layout.activity_new_opponent_item, users_list);

								autocomplete.setThreshold(1);
								autocomplete.setAdapter(adapter);

								EditText usernameTextfield = (EditText)findViewById(R.id.new_opponent_view);
								usernameTextfield.addTextChangedListener(new TextWatcher() {
									@Override
									public void afterTextChanged(Editable arg0) {
										username = arg0.toString();
									}

									@Override
									public void beforeTextChanged(CharSequence arg0, int arg1,
																  int arg2, int arg3) {
										// TODO Auto-generated method stub

									}

									@Override
									public void onTextChanged(CharSequence arg0, int arg1, int arg2,
															  int arg3) {
										// TODO Auto-generated method stub

									}
								});

							}
						});
					}
				} else {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							AlertDialog.Builder builder = new AlertDialog.Builder(NewOpponentActivity.this);
							builder.setMessage("Beim Abrufen der Benutzer ist ein Fehler aufgetreten.");
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
	}
}
