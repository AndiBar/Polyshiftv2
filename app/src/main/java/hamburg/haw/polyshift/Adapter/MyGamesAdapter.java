package hamburg.haw.polyshift.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import hamburg.haw.polyshift.Game.PolyshiftActivity;
import hamburg.haw.polyshift.Menu.MyGamesActivity;
import hamburg.haw.polyshift.R;
import hamburg.haw.polyshift.Tools.PHPConnector;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MyGamesAdapter extends SimpleAdapter {
    private LayoutInflater inflater;
    private Context context;
    public ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String,String>>();
    public String response;

	public MyGamesAdapter(Context context,
                          List<? extends Map<String, ?>> data, int resource, String[] from,
                          int[] to) {
		super(context, data, resource, from, to);
		this.inflater = LayoutInflater.from(context);
		this.data = (ArrayList<HashMap<String, String>>) data;
        this.context = context;
        this.response = "";
		// TODO Auto-generated constructor stub
	}

	public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.activity_my_games_item, null);
        }
        if(!data.isEmpty()) {

            final TextView opponent_view = (TextView) convertView.findViewById(R.id.opponent);
            opponent_view.setText(data.get(position).get("opponent_name"));

            final TextView status_view = (TextView) convertView.findViewById(R.id.status);
            if (data.get(position).get("my_game").equals("yes") && data.get(position).get("game_accepted").equals("0")) {
                status_view.setText("Warten auf Annahme.");
            } else if ((data.get(position).get("opponents_turn").equals("0") && data.get(position).get("my_game").equals("yes")) || (data.get(position).get("opponents_turn").equals("1") && data.get(position).get("my_game").equals("no"))) {
                status_view.setText("Du bist dran!");
            } else {
                status_view.setText(data.get(position).get("opponent_name") + " ist dran.");
            }

            final TextView time_view = (TextView) convertView.findViewById(R.id.time);
            java.util.Date date= new java.util.Date();
            Timestamp current_time = new Timestamp(date.getTime());
            Timestamp round_time = Timestamp.valueOf(data.get(position).get("timestamp"));
            long diff_h = TimeUnit.MILLISECONDS.toHours(current_time.getTime()) - TimeUnit.MILLISECONDS.toHours(round_time.getTime());
            long diff_min = (TimeUnit.MILLISECONDS.toMinutes(current_time.getTime()) - TimeUnit.MILLISECONDS.toMinutes(round_time.getTime()));
            long diff_sec = (TimeUnit.MILLISECONDS.toSeconds(current_time.getTime()) - TimeUnit.MILLISECONDS.toSeconds(round_time.getTime()));
            if(diff_h > 24){
                long diff_d = TimeUnit.MILLISECONDS.toDays(current_time.getTime()) - TimeUnit.MILLISECONDS.toDays(round_time.getTime());
                time_view.setText(String.valueOf(diff_d) + " Tage");
            }
            else if(diff_min > 60){
                time_view.setText(String.valueOf(diff_h) + " Std");
            }
            else if(diff_sec > 60){
                time_view.setText(String.valueOf(diff_min) + " Min");
            }
            else{
                time_view.setText(String.valueOf(diff_sec) + " Sek");
            }

            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (data.get(position).get("game_accepted").equals("1")) {
                        class Update_Game_Thread extends Thread {
                            public void run() {
                                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                                nameValuePairs.add(new BasicNameValuePair("game", data.get(position).get("game_id")));
                                PHPConnector.doRequest(nameValuePairs, "update_game.php");
                            }
                        }
                        Thread update_game_thread = new Update_Game_Thread();
                        update_game_thread.start();
                        try {
                            long waitMillis = 10000;
                            while (update_game_thread.isAlive()) {
                                update_game_thread.join(waitMillis);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        final Intent intent = new Intent(context, PolyshiftActivity.class);
                        context.startActivity(intent);
                        Activity activity = (Activity) context;
                        activity.finish();
                    }
                }
            };
            status_view.setOnClickListener(onClickListener);
            time_view.setOnClickListener(onClickListener);
            opponent_view.setOnClickListener(onClickListener);
        }
	    return convertView; 
    }
    
}
