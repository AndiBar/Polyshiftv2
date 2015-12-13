package hamburg.haw.polyshift.Menu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import hamburg.haw.polyshift.Adapter.ChooseOpponentAdapter;
import hamburg.haw.polyshift.Adapter.LoginAdapter;
import hamburg.haw.polyshift.R;
import hamburg.haw.polyshift.Tools.PHPConnector;

/**
 * Created by Andi on 12.03.2015.
 */
public class ChooseOpponentActivity extends ListActivity {
    public static ArrayList<HashMap<String, String>> friends_list = new ArrayList<HashMap<String,String>>();
    public static ArrayList<HashMap<String, String>> friends_attending_list = new ArrayList<HashMap<String,String>>();
    private ListView settings;
    public static ChooseOpponentAdapter mAdapter;
    private int bell_number = 0;
    public static Activity activity;
    private static Context context;
    private LoginAdapter loginAdapter;
    public static ProgressDialog dialog = null;
    private boolean error_shown = false;
    private Menu menu;

    public ChooseOpponentActivity() {
        // Empty constructor required for fragment subclasses
        activity = this;
    }
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Meine Gegner");
        setContentView(R.layout.activity_choose_opponent);

        dialog = ProgressDialog.show(ChooseOpponentActivity.this, "", "Gegner werden geladen", true);

        Thread friends_thread = new FriendsThread();
        friends_thread.start();
    }
    // Action Bar Button
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_opponent, menu);
        MenuItem bell_button = menu.findItem(R.id.action_attending_contacts);
        bell_button.setVisible(false);
        return super.onCreateOptionsMenu(menu);

    }
    public void setMenuItems(){
        final View menu_hotlist = menu.findItem(R.id.action_attending_contacts).getActionView();

        MenuItem bell_button = menu.findItem(R.id.action_attending_contacts);
        TextView ui_bell = (TextView) menu_hotlist.findViewById(R.id.hotlist_hot);
        bell_number = friends_attending_list.size();
        if (bell_number == 0) {
            bell_button.setVisible(false);
        } else {
            bell_button.setVisible(true);
            ui_bell.setText(Integer.toString(bell_number));
        }

        new MyMenuItemStuffListener(menu_hotlist, "Show hot message") {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), OpponentsAttendingActivity.class);
                startActivity(intent);
                ChooseOpponentActivity.this.finish();
            }
        };
    }

    static abstract class MyMenuItemStuffListener implements View.OnClickListener, View.OnLongClickListener {
        private String hint;
        private View view;

        MyMenuItemStuffListener(View view, String hint) {
            this.view = view;
            this.hint = hint;
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override abstract public void onClick(View v);

        @Override public boolean onLongClick(View v) {
            final int[] screenPos = new int[2];
            final Rect displayFrame = new Rect();
            view.getLocationOnScreen(screenPos);
            view.getWindowVisibleDisplayFrame(displayFrame);
            final Context context = view.getContext();
            final int width = view.getWidth();
            final int height = view.getHeight();
            final int midy = screenPos[1] + height / 2;
            final int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            Toast cheatSheet = Toast.makeText(context, hint, Toast.LENGTH_SHORT);
            if (midy < displayFrame.height()) {
                cheatSheet.setGravity(Gravity.TOP | Gravity.RIGHT,
                        screenWidth - screenPos[0] - width / 2, height);
            } else {
                cheatSheet.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, height);
            }
            cheatSheet.show();
            return true;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_new_contacts:
                Intent intent = new Intent(this, NewOpponentActivity.class);
                startActivity(intent);
                ChooseOpponentActivity.this.finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void onBackPressed() {
        final Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
        ChooseOpponentActivity.this.finish();
    }

    public class FriendsThread extends Thread{
        public void run(){
            String stringResponse = PHPConnector.doRequest("get_opponents.php");
            if(!stringResponse.equals("no opponents found")) {
                if(stringResponse.equals("not logged in.") || stringResponse == null) {
                    context = getApplicationContext();
                    loginAdapter = new LoginAdapter(context, ChooseOpponentActivity.this);
                    loginAdapter.userLoginStoredCredentials();
                } else {
                    String[] data_unformatted = stringResponse.split(",");
                    friends_list = new ArrayList<HashMap<String, String>>();
                    for (String item : data_unformatted) {
                        HashMap<String, String> data_map = new HashMap<String, String>();
                        String[] data_array = item.split(":");
                        if(item.split(":").length != 1){
                            data_map.put("ID", data_array[0]);
                            data_map.put("title", data_array[1]);
                            friends_list.add(data_map);
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                    builder.setMessage("Beim Abrufen der Gegner ist ein Fehler aufgetreten.");
                                    builder.setPositiveButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });
                                    builder.show();
                                }
                            });
                            error_shown = true;
                        }
                    }
                }
            }
            stringResponse = PHPConnector.doRequest("get_opponents_attending.php");
            if(!stringResponse.equals("no opponents found")) {
                if(stringResponse.equals("not logged in.") || stringResponse == null){
                    context = getApplicationContext();
                    loginAdapter = new LoginAdapter(context, ChooseOpponentActivity.this);
                    loginAdapter.userLoginStoredCredentials();
                } else {
                    String[] data_unformatted = stringResponse.split(",");
                    data_unformatted = stringResponse.split(",");
                    friends_attending_list = new ArrayList<HashMap<String, String>>();
                    for (String item : data_unformatted) {
                        HashMap<String, String> data_map = new HashMap<String, String>();
                        String[] data_array = item.split(":");
                        if(item.split(":").length != 1){
                            data_map.put("ID", data_array[0]);
                            data_map.put("title", data_array[1]);
                            friends_attending_list.add(data_map);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //set opponents attenting in menu bar
                                    setMenuItems();
                                }
                            });
                        } else if(!error_shown){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                    builder.setMessage("Beim Abrufen der Gegner ist ein Fehler aufgetreten.");
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    mAdapter = new ChooseOpponentAdapter(ChooseOpponentActivity.this,
                            friends_list,
                            R.layout.activity_choose_opponent,
                            new String[]{"title"},
                            new int[]{R.id.title});

                    ListView listView = getListView();
                    setListAdapter(mAdapter);
                    //listView.setClickable(true);
                    listView.setFocusableInTouchMode(false);
                    listView.setFocusable(false);

                    dialog.dismiss();
                    Toast.makeText(activity, R.string.how_to_start_game, Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
}