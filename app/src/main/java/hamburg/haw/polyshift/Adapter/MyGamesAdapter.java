package hamburg.haw.polyshift.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import hamburg.haw.polyshift.Game.GameSync;
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
    public static ProgressDialog dialog = null;

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

	public View getView(final int position, View convertView, final ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.activity_my_games_item, null);
        }
        if(!data.isEmpty()) {

            final TextView opponent_view = (TextView) convertView.findViewById(R.id.opponent);
            opponent_view.setText(data.get(position).get("opponent_name"));

            final TextView status_view = (TextView) convertView.findViewById(R.id.status);
            if (data.get(position).get("my_game").equals("yes") && data.get(position).get("game_accepted").equals("0")) {
                status_view.setText(context.getString(R.string.awaiting_acception));
            } else if ((data.get(position).get("opponents_turn").equals("0") && data.get(position).get("my_game").equals("yes")) || (data.get(position).get("opponents_turn").equals("1") && data.get(position).get("my_game").equals("no"))) {
                status_view.setText(context.getString(R.string.your_turn));
            } else {
                status_view.setText(context.getString(R.string.opponents_turn,data.get(position).get("opponent_name")));
            }

            final TextView time_view = (TextView) convertView.findViewById(R.id.time);
            final LinearLayout item_layout = (LinearLayout) convertView.findViewById(R.id.item_layout);
            java.util.Date date= new java.util.Date();
            Timestamp current_time = new Timestamp(date.getTime());
            Timestamp round_time = Timestamp.valueOf(data.get(position).get("timestamp"));
            long diff_h = TimeUnit.MILLISECONDS.toHours(current_time.getTime()) - TimeUnit.MILLISECONDS.toHours(round_time.getTime());
            long diff_min = (TimeUnit.MILLISECONDS.toMinutes(current_time.getTime()) - TimeUnit.MILLISECONDS.toMinutes(round_time.getTime()));
            long diff_sec = (TimeUnit.MILLISECONDS.toSeconds(current_time.getTime()) - TimeUnit.MILLISECONDS.toSeconds(round_time.getTime()));
            if(diff_h > 24){
                if(diff_h > 99){
                    long diff_d = TimeUnit.MILLISECONDS.toDays(current_time.getTime()) - TimeUnit.MILLISECONDS.toDays(round_time.getTime());
                    time_view.setText(String.valueOf(diff_d) + " " + context.getString(R.string.days));
                    item_layout.setBackgroundColor(Color.parseColor("#DF4949"));
                }else {
                    long diff_d = TimeUnit.MILLISECONDS.toDays(current_time.getTime()) - TimeUnit.MILLISECONDS.toDays(round_time.getTime());
                    time_view.setText(String.valueOf(diff_d) + " " + context.getString(R.string.days));
                    item_layout.setBackgroundColor(Color.parseColor("#334D5C"));
                }
            }
            else if(diff_min > 60){
                time_view.setText(String.valueOf(diff_h) + " " + context.getString(R.string.hours));
                item_layout.setBackgroundColor(Color.parseColor("#E27A41"));
            }
            else if(diff_sec > 60){
                time_view.setText(String.valueOf(diff_min) + " " + context.getString(R.string.minutes));
                item_layout.setBackgroundColor(Color.parseColor("#EFC94C"));
            }
            else{
                time_view.setText(String.valueOf(diff_sec) + " " + context.getString(R.string.seconds));
                item_layout.setBackgroundColor(Color.parseColor("#47B09C"));
            }

            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    java.util.Date date= new java.util.Date();
                    Timestamp current_time = new Timestamp(date.getTime());
                    Timestamp round_time = Timestamp.valueOf(data.get(position).get("timestamp"));
                    long diff_h = TimeUnit.MILLISECONDS.toHours(current_time.getTime()) - TimeUnit.MILLISECONDS.toHours(round_time.getTime());
                    if (data.get(position).get("game_accepted").equals("1")) {
                        if(diff_h > 1 && (data.get(position).get("opponents_turn").equals("0") && data.get(position).get("my_game").equals("no")) || (data.get(position).get("opponents_turn").equals("1") && data.get(position).get("my_game").equals("yes"))){
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                            builder.setMessage("Möchtest du " + data.get(position).get("opponent_name") + " eine Erinnerung schicken?");
                            builder = builder.setPositiveButton(context.getString(R.string.yes),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            String msg = data.get(position).get("my_name") + " wartet auf einen Spielzug von dir";
                                            GameSync.SendChangeNotification(data.get(position).get("opponent_id"), msg, data.get(position).get("game_id"));
                                        }
                                    });
                            builder.setNegativeButton("Nein, Spiel starten",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            startGame(position,parent);
                                        }
                                    });
                            builder.show();
                        }else{
                            startGame(position,parent);
                        }
                    }else{
                        if(diff_h > 1 ){
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                            builder.setMessage("Möchtest du " + data.get(position).get("opponent_name") + " eine Erinnerung schicken?");
                            builder = builder.setPositiveButton(context.getString(R.string.yes),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            String msg = data.get(position).get("my_name") + " wartet auf eine Spielannahme von dir";
                                            GameSync.SendChangeNotification(data.get(position).get("opponent_id"), msg, data.get(position).get("game_id"));
                                        }
                                    });
                            builder.setNegativeButton("Nein",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            builder.show();
                        }
                    }
                }
            };
            status_view.setOnClickListener(onClickListener);
            time_view.setOnClickListener(onClickListener);
            opponent_view.setOnClickListener(onClickListener);
        }
	    return convertView; 
    }
    public void startGame(final int position, final ViewGroup parent) {
        dialog = ProgressDialog.show(parent.getContext(), "", "Spiel wird gestartet", true);
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
