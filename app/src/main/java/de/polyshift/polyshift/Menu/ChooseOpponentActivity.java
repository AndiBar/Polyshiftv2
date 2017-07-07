package de.polyshift.polyshift.Menu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.HashMap;

import de.polyshift.polyshift.Menu.Adapter.ChooseOpponentAdapter;
import de.polyshift.polyshift.Tools.LoginTool;
import de.polyshift.polyshift.Tools.Analytics.AnalyticsApplication;
import de.polyshift.polyshift.R;
import de.polyshift.polyshift.Tools.PHPConnector;
import rx.subscriptions.CompositeSubscription;

/**
 * Menü zum Auswählen eines Spielers aus der Gegner-Liste und zum Starten eines
 * neuen Spiels.
 *
 * @author helmsa
 *
 */

public class ChooseOpponentActivity extends ListActivity {

    public static ArrayList<HashMap<String, String>> friends_list;
    public static ArrayList<HashMap<String, String>> friends_attending_list;
    public static ChooseOpponentAdapter mAdapter;
    public static Activity activity;
    public static ProgressDialog dialog = null;
    private ListView settings;
    private int bell_number = 0;
    private static Context context;
    private LoginTool loginTool;
    private Menu menu;
    private Tracker mTracker = null;
    private Thread friends_thread;
    final CompositeSubscription compositeSubscription = new CompositeSubscription();

    public ChooseOpponentActivity() {
        // Empty constructor required for fragment subclasses
        activity = this;
    }
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.my_opponents));
        setContentView(R.layout.activity_choose_opponent);

        dialog = ProgressDialog.show(ChooseOpponentActivity.this, "", getString(R.string.opponents_are_loading), true);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        mTracker.setScreenName(getClass().getName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        friends_list = new ArrayList<HashMap<String,String>>();
        friends_attending_list = new ArrayList<HashMap<String,String>>();

        friends_thread = new FriendsThread();
        friends_thread.start();
    }

    @Override
    public void onResume(){
        super.onResume();

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        compositeSubscription.clear();
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
        friends_thread.interrupt();
        dialog.dismiss();
        final Intent intent = new Intent(this, MyGamesActivity.class);
        startActivity(intent);
        ChooseOpponentActivity.this.finish();
    }

    public class FriendsThread extends Thread{
        public void run(){
            String stringResponse = PHPConnector.doRequest("get_opponents.php");
            if(!stringResponse.equals("no opponents found")) {
                Log.d("test", "response: " + stringResponse);
                if(stringResponse.equals("not logged in.") || stringResponse.isEmpty()) {
                    context = getApplicationContext();
                    loginTool = new LoginTool(context, ChooseOpponentActivity.this);
                    compositeSubscription.add(loginTool.handleSessionExpiration(ChooseOpponentActivity.this).subscribe(
                            s -> {},
                            e -> {}
                    ));
                } else if(!stringResponse.equals("error")){
                    String[] data_unformatted = stringResponse.split(",");
                    friends_list = new ArrayList<HashMap<String, String>>();
                    for (String item : data_unformatted) {
                        HashMap<String, String> data_map = new HashMap<String, String>();
                        String[] data_array = item.split(":");
                        if(item.split(":").length != 1) {
                            data_map.put("ID", data_array[0]);
                            data_map.put("title", data_array[1]);
                            friends_list.add(data_map);
                        }else if(item.split(":").length == 1) {
                            data_map = friends_list.get(friends_list.size() - 1);
                            data_map.put("challenger", data_array[0]);
                            friends_list.set(friends_list.size() - 1, data_map);
                        }else if(stringResponse.equals("no opponents found")) {

                        } else {
                            showErrorDialog();
                        }
                    }
                }else{
                    showErrorDialog();
                }
            }
            stringResponse = PHPConnector.doRequest("get_opponents_attending.php");
            if(!stringResponse.equals("no opponents found")) {
                if(stringResponse.equals("not logged in.") || stringResponse == null){
                    context = getApplicationContext();
                    loginTool = new LoginTool(context, ChooseOpponentActivity.this);
                    compositeSubscription.add(loginTool.handleSessionExpiration(ChooseOpponentActivity.this).subscribe(
                            s -> {},
                            e -> {}
                    ));
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
                        }else if(stringResponse.equals("no opponents found")) {

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

    private void showErrorDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(R.string.error_loading_opponents);
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