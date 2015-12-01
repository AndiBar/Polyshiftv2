package hamburg.haw.polyshift.Menu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import hamburg.haw.polyshift.Adapter.ScoresAdapter;
import hamburg.haw.polyshift.Game.BlockComparator;
import hamburg.haw.polyshift.R;
import hamburg.haw.polyshift.Tools.PHPConnector;

public class ScoresActivity extends ListActivity {

    public static ScoresAdapter mAdapter;
    private Activity activity;
    public static ArrayList<HashMap<String, String>> scores_list = new ArrayList<HashMap<String,String>>();

    public ScoresActivity(){
        activity = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);
        setTitle("Bestenliste");

        Thread scores_thread = new ScoresThread();
        scores_thread.start();
        try {
            long waitMillis = 10000;
            while (scores_thread.isAlive()) {
                scores_thread.join(waitMillis);
            }
            if(MainMenuActivity.dialog != null) {
                MainMenuActivity.dialog.dismiss();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mAdapter = new ScoresAdapter(this,
                scores_list,
                R.layout.activity_scores,
                new String[] {"title"},
                new int[] {R.id.title});

        ListView listView = getListView();
        setListAdapter(mAdapter);
        //listView.setClickable(true);
        listView.setFocusableInTouchMode(false);
        listView.setFocusable(false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }
    public void onBackPressed() {
        final Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
        this.finish();
    }

    public class ScoresThread extends Thread{
        public void run(){

            String stringResponse = PHPConnector.doRequest("get_scores.php");
            String[] data_unformatted = stringResponse.split(",");
            scores_list = new ArrayList<HashMap<String,String>>();
            if(!stringResponse.equals("no scores found")) {
                if(!(stringResponse.split(":").length == 1)) {
                    for (String item : data_unformatted) {
                        HashMap<String, String> data_map = new HashMap<String, String>();
                        String[] data_array = item.split(":");
                        data_map.put("user_id", data_array[0]);
                        data_map.put("username", data_array[1].split("=")[1]);
                        data_map.put("win", data_array[2].split("=")[1]);
                        data_map.put("loss", data_array[3].split("=")[1]);
                        Integer win = Integer.parseInt(data_array[2].split("=")[1]);
                        Integer loss = Integer.parseInt(data_array[3].split("=")[1]);
                        Integer score = 0;
                        if(loss != 0) {
                            score = (win / loss) * (win - loss);
                        }else{
                            score = win * (win + loss);
                        }
                        data_map.put("score", String.valueOf(score));
                        Log.d("Map", data_map.toString());
                        scores_list.add(data_map);
                    }
                    Collections.sort(scores_list, Collections.reverseOrder(new ScoreComparator()));
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setMessage("Beim Abrufen der Scores ist ein Fehler aufgetreten.");
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
