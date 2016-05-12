package de.polyshift.polyshift.Menu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import de.polyshift.polyshift.Tools.LoginTool;
import de.polyshift.polyshift.Menu.Adapter.ScoresAdapter;
import de.polyshift.polyshift.Tools.Analytics.AnalyticsApplication;
import de.polyshift.polyshift.Menu.Comparators.ScoreComparator;
import de.polyshift.polyshift.R;
import de.polyshift.polyshift.Tools.PHPConnector;

/**
 * Zeigt die aktuelle Bestenliste mit Spielstatistik und Punkten an.
 *
 * @author helmsa
 *
 */

public class ScoresActivity extends ListActivity {

    public static ScoresAdapter mAdapter;
    private Activity activity;
    public static ArrayList<HashMap<String, String>> scores_list = new ArrayList<HashMap<String,String>>();
    public static ProgressDialog dialog = null;
    private Tracker mTracker = null;
    private Thread scores_thread;

    public ScoresActivity(){
        activity = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);
        setTitle(getString(R.string.highscores));

        dialog = ProgressDialog.show(ScoresActivity.this, "", getString(R.string.scores_are_loading), true);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName(getClass().getName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);

        scores_thread = new ScoresThread();
        scores_thread.start();
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
        scores_thread.interrupt();
        dialog.dismiss();
        final Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
        this.finish();
    }

    public class ScoresThread extends Thread{
        public void run(){
            String stringResponse = PHPConnector.doRequest("get_scores.php");
            if(!stringResponse.equals("not logged in.")) {
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
                            double win = Double.parseDouble(data_array[2].split("=")[1]);
                            double loss = Double.parseDouble(data_array[3].split("=")[1]);
                            int score = 0;
                            if(loss != 0) {
                                score = (int) (Math.round((win / loss) * (win - loss)) + win);
                                if(score < 0){
                                    score = 0;
                                }
                            }else{
                                score = (int) ((win * (win + loss)) + win);
                            }
                            data_map.put("score", String.valueOf(score));
                            Log.d("Map", data_map.toString());
                            scores_list.add(data_map);
                        }
                        Collections.sort(scores_list, Collections.reverseOrder(new ScoreComparator()));

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter = new ScoresAdapter(ScoresActivity.this,
                                        scores_list,
                                        R.layout.activity_scores,
                                        new String[]{"title"},
                                        new int[]{R.id.title});

                                ListView listView = getListView();
                                setListAdapter(mAdapter);
                                //listView.setClickable(true);
                                listView.setFocusableInTouchMode(false);
                                listView.setFocusable(false);

                                dialog.dismiss();
                            }
                        });
                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                builder.setMessage(R.string.error_loading_scores);
                                builder.setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                                builder.show();
                                dialog.dismiss();
                            }
                        });
                    }
                }
            }else{
                Context context = getApplicationContext();
                LoginTool loginTool = new LoginTool(context, ScoresActivity.this);
                loginTool.userLoginStoredCredentials();
            }
        }
    }
}
