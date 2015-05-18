package hamburg.haw.polyshift.Menu;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
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

import hamburg.haw.polyshift.Adapter.LoginAdapter;
import hamburg.haw.polyshift.Adapter.MyGamesAdapter;
import hamburg.haw.polyshift.R;
import hamburg.haw.polyshift.Tools.AlertDialogs;
import hamburg.haw.polyshift.Tools.PHPConnector;

/**
 * Created by Andi on 12.03.2015.
 */
public class MyGamesActivity extends ListActivity {
    public static ArrayList<HashMap<String, String>> games_list = new ArrayList<HashMap<String,String>>();
    public static ArrayList<HashMap<String, String>> games_attending_list = new ArrayList<HashMap<String,String>>();
    private ListView settings;
    public static MyGamesAdapter mAdapter;
    private int bell_number = 0;
    public static Activity activity;
    private Context context;
    private LoginAdapter loginAdapter;

    public MyGamesActivity() {
        // Empty constructor required for fragment subclasses
        activity = this;
    }
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        loginAdapter = new LoginAdapter(context,MyGamesActivity.this);
        loginAdapter.handleSessionExpiration();


        setTitle("Meine Spiele");
        setContentView(R.layout.activity_my_games);

        Toast.makeText(activity,"Zum Starten auf ein Spiel klicken.", Toast.LENGTH_SHORT).show();

        Thread friends_thread = new GamesThread();
        friends_thread.start();
        try {
            long waitMillis = 10000;
            while (friends_thread.isAlive()) {
                friends_thread.join(waitMillis);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mAdapter = new MyGamesAdapter(this,
                games_list,
                R.layout.activity_my_games,
                new String[] {"title"},
                new int[] {R.id.title});

        ListView listView = getListView();
        setListAdapter(mAdapter);
        //listView.setClickable(true);
        listView.setFocusableInTouchMode(false);
        listView.setFocusable(false);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*SaveValue.setSelectedFriendName(friends_list.get(position).get("title"));
                Log.i("ContactsFragment", "Friend: " + friends_list.get(position).get("title"));
                Fragment fragment = new ContactsDetailFragment();
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();*/
            }
        });
    }
    // Action Bar Button
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_game, menu);

        final View menu_hotlist = menu.findItem(R.id.action_attending_contacts).getActionView();

        MenuItem bell_button = menu.findItem(R.id.action_attending_contacts);
        TextView ui_bell = (TextView) menu_hotlist.findViewById(R.id.hotlist_hot);
        bell_number = games_attending_list.size();
        if (bell_number == 0) {
            bell_button.setVisible(false);
        } else {
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

        return super.onCreateOptionsMenu(menu);
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
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        final Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
        this.finish();
    }

    public class GamesThread extends Thread{
        public void run(){

            String stringResponse = PHPConnector.doRequest("get_games.php");
            String[] data_unformatted = stringResponse.split(",");
            games_list = new ArrayList<HashMap<String,String>>();
            if(!stringResponse.equals("no games found")) {
                for (String item : data_unformatted) {
                    HashMap<String, String> data_map = new HashMap<String, String>();
                    String[] data_array = item.split(":");
                    data_map.put("game_id", data_array[0]);
                    data_map.put("opponent_id", data_array[1].split("=")[1]);
                    data_map.put("opponent_name", data_array[2].split("=")[1]);
                    data_map.put("game_accepted", data_array[3].split("=")[1]);
                    data_map.put("opponents_turn", data_array[4].split("=")[1]);
                    data_map.put("my_game", data_array[5].split("=")[1]);
                    Log.d("Map", data_map.toString());
                    games_list.add(data_map);
                }
            }
            stringResponse = PHPConnector.doRequest("get_games_attending.php");
            data_unformatted = stringResponse.split(",");
            games_attending_list = new ArrayList<HashMap<String,String>>();
            if(!stringResponse.equals("no games found")){
                for(String item : data_unformatted){
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
            }
        }
    }
}