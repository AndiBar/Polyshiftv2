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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import de.polyshift.polyshift.Game.AiPolyshiftActivity;
import de.polyshift.polyshift.Tools.LoginTool;
import de.polyshift.polyshift.Menu.Adapter.MyGamesAdapter;
import de.polyshift.polyshift.Tools.Analytics.AnalyticsApplication;
import de.polyshift.polyshift.Game.PolyshiftActivity;
import de.polyshift.polyshift.Menu.Comparators.GameComparator;
import de.polyshift.polyshift.R;
import de.polyshift.polyshift.Tools.PHPConnector;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Zeigt die aktuellen Spiele an und gibt dem Spieler die Möglichkeit ein Spiel zu starten.
 *
 * @author helmsa
 *
 */

public class MyGamesActivity extends ListActivity {
    public static ArrayList<HashMap<String, String>> games_list = new ArrayList<HashMap<String, String>>();
    public static ArrayList<HashMap<String, String>> games_attending_list = new ArrayList<HashMap<String, String>>();
    private ListView settings;
    public static MyGamesAdapter mAdapter;
    private int bell_number = 0;
    public static Activity activity;
    private Context context;
    private LoginTool loginTool;
    public static ProgressDialog dialog = null;
    private Menu menu;
    private Thread my_games_thread;
    private Tracker mTracker = null;
    private final CompositeSubscription compositeSubscription = new CompositeSubscription();

    public MyGamesActivity() {
        // Empty constructor required for fragment subclasses
        activity = this;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.my_games));
        setContentView(R.layout.activity_my_games);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);

        mTracker.setScreenName(getClass().getName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void showEnterUserNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.enter_name);
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String username  = input.getText().toString();
                if(!username.isEmpty()){
                    ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("username", username));
                    PHPConnector.doObservableRequest(nameValuePairs, "update_user.php")
                            .subscribeOn(Schedulers.io())
                            .subscribe(new Subscriber<String>() {
                                @Override
                                public void onCompleted() {
                                }

                                @Override
                                public void onError(Throwable e) {

                                }

                                @Override
                                public void onNext(String s) {

                                }
                            });
                }
                dialog.cancel();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final Intent intent = new Intent(MyGamesActivity.this, MainMenuActivity.class);
                startActivity(intent);
                MyGamesActivity.this.finish();
                dialog.cancel();
            }
        });
        builder.show();
    }

    @Override
    public void onResume(){
        super.onResume();

        loginTool = new LoginTool(getApplicationContext(), MyGamesActivity.this);
        compositeSubscription.add(loginTool.handleSessionExpiration(activity)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        if(loginTool.isLoggedIn() && (LoginTool.username == null || LoginTool.username.isEmpty())){
                            showEnterUserNameDialog();
                        }
                        Log.d("test", "spiele geladen");
                        games_list = new ArrayList<HashMap<String, String>>();
                        games_attending_list = new ArrayList<HashMap<String, String>>();

                        dialog = ProgressDialog.show(MyGamesActivity.this, "", getString(R.string.game_are_loading), true);

                        if(my_games_thread == null || !my_games_thread.isAlive()) {
                            my_games_thread = new GamesThread();
                            my_games_thread.start();
                        }

                        if (PolyshiftActivity.dialog != null) {
                            try {
                                PolyshiftActivity.dialog.dismiss();
                            } catch (IllegalArgumentException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("test", "error");
                    }

                    @Override
                    public void onNext(String s) {

                    }
                }));
    }

    // Action Bar Button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_game, menu);
        MenuItem bell_button = menu.findItem(R.id.action_attending_contacts);
        MenuItem add_game_button = menu.findItem(R.id.action_new_game);
        add_game_button.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent intent = new Intent(MyGamesActivity.this, ChooseOpponentActivity.class);
                startActivity(intent);
                return false;
            }
        });
        add_game_button.setVisible(true);
        bell_button.setVisible(false);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }
    public void setMenuItems(){
        if(menu != null) {

            final View menu_hotlist = menu.findItem(R.id.action_attending_contacts).getActionView();

            MenuItem bell_button = menu.findItem(R.id.action_attending_contacts);
            TextView ui_bell = (TextView) menu_hotlist.findViewById(R.id.hotlist_hot);
            bell_number = games_attending_list.size();
            if (bell_number == 0) {
                bell_button.setVisible(false);
            } else if(bell_number > 0) {
                bell_button.setVisible(true);
                ui_bell.setText(Integer.toString(bell_number));
            }

            new MyMenuItemStuffListener(menu_hotlist, "Show hot message") {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), GamesAttendingActivity.class);
                    startActivity(intent);
                    MyGamesActivity.this.finish();
                }
            };
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        compositeSubscription.clear();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
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

        @Override
        abstract public void onClick(View v);

        @Override
        public boolean onLongClick(View v) {
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
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        final Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
        this.finish();
    }

    public class GamesThread extends Thread {
        public void run() {

            String stringResponse = PHPConnector.doRequest("get_games.php");
            String[] data_unformatted = stringResponse.split(",");
            MyGamesActivity.games_list = new ArrayList<HashMap<String, String>>();
            Log.d("test", "response: " + stringResponse);
            if (!stringResponse.equals("no games found")) {
                if (!(stringResponse.split(";").length == 1)) {
                    for (String item : data_unformatted) {
                        HashMap<String, String> data_map = new HashMap<String, String>();
                        String[] data_array = item.split(";");
                        data_map.put("game_id", data_array[0]);
                        data_map.put("opponent_id", data_array[1].split("=")[1]);
                        data_map.put("opponent_name", data_array[2].split("=")[1]);
                        data_map.put("game_accepted", data_array[3].split("=")[1]);
                        data_map.put("opponents_turn", data_array[4].split("=")[1]);
                        data_map.put("my_game", data_array[5].split("=")[1]);
                        data_map.put("timestamp", data_array[6].split("=")[1]);
                        data_map.put("my_name", data_array[7].split("=")[1]);
                        Log.d("Map", data_map.toString());
                        games_list.add(data_map);
                    }
                } else if(stringResponse.equals("not logged in.")) {
                    compositeSubscription.add(loginTool.handleSessionExpiration(MyGamesActivity.this).subscribe(
                            s -> {},
                            e -> {}
                    ));

                }else if(stringResponse.equals("no games found")) {

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setMessage(R.string.error_loading_games);
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
            stringResponse = PHPConnector.doRequest("get_games_attending.php");
            data_unformatted = stringResponse.split(",");
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
                        games_attending_list.add(data_map);
                    }
                } else if(stringResponse.equals("not logged in.")){
                    context = getApplicationContext();
                    loginTool = new LoginTool(context, MyGamesActivity.this);
                    compositeSubscription.add(loginTool.handleSessionExpiration(MyGamesActivity.this).subscribe(
                            s -> {},
                            e -> {}
                    ));

                }else if(stringResponse.equals("no games found")) {

                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //show games attending in menu bar
                    setMenuItems();

                    Collections.sort(games_list, Collections.reverseOrder(new GameComparator()));

                    mAdapter = new MyGamesAdapter(MyGamesActivity.this,
                            games_list,
                            R.layout.activity_my_games,
                            new String[]{"title"},
                            new int[]{R.id.title});

                    ListView listView = getListView();
                    setListAdapter(mAdapter);
                    //listView.setClickable(true);
                    listView.setFocusableInTouchMode(false);
                    listView.setFocusable(false);

                    dialog.dismiss();
                    Toast.makeText(activity, R.string.click_game_to_start, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
