package hamburg.haw.polyshift.Menu;

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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import hamburg.haw.polyshift.Adapter.LoginAdapter;
import hamburg.haw.polyshift.R;
import hamburg.haw.polyshift.Tools.PHPConnector;

public class NewOpponentActivity extends Activity {
	
	private String username;
    private Context context;
    private LoginAdapter loginAdapter;
    private String response;
	private ArrayList users_list;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        context = getApplicationContext();
		loginAdapter = new LoginAdapter(context, NewOpponentActivity.this);
        loginAdapter.handleSessionExpiration(this);
        setContentView(R.layout.activity_new_opponent);
        setTitle(R.string.new_opponent);

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
				Intent intent = new Intent(NewOpponentActivity.this, ChooseOpponentActivity.class);
				startActivity(intent);
				NewOpponentActivity.this.finish();
            }
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
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
