package de.polyshift.polyshift.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import de.polyshift.polyshift.Game.GameSync;
import de.polyshift.polyshift.Menu.GamesAttendingActivity;
import de.polyshift.polyshift.Menu.MyGamesActivity;
import de.polyshift.polyshift.R;
import de.polyshift.polyshift.Tools.PHPConnector;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChooseOpponentAdapter extends SimpleAdapter {
    private LayoutInflater inflater;
    private Context context;
    public ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String,String>>();
    public String response;
    public static ProgressDialog dialog = null;

	public ChooseOpponentAdapter(Context context,
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
            convertView = inflater.inflate(R.layout.activity_choose_opponent_item, null);
        }

        final TextView user_view = (TextView) convertView.findViewById(R.id.title);
        user_view.setText(data.get(position).get("title"));

        user_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                builder.setMessage(context.getString(R.string.challenge, data.get(position).get("title")));
                builder = builder.setPositiveButton(context.getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog = ProgressDialog.show(context, "",context.getString(R.string.game_is_created), true);
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                                        nameValuePairs.add(new BasicNameValuePair("opponent_id", data.get(position).get("ID")));
                                        response = PHPConnector.doRequest(nameValuePairs, "add_game.php");
                                    }
                                });
                                thread.start();
                                dialog.cancel();
                                try {
                                    long waitMillis = 10000;
                                    while (thread.isAlive()) {
                                        thread.join(waitMillis);
                                    }
                                    dialog.dismiss();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if(response.equals("game created"))
                                {
                                    Log.d("res:", response);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setMessage(context.getString(R.string.game_created, data.get(position).get("title")));
                                    builder.setPositiveButton(context.getString(R.string.OK),
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    GameSync.SendChangeNotification(data.get(position).get("ID"),context.getString(R.string.challenged),"", GamesAttendingActivity.class.getName());
                                                    dialog.cancel();
                                                    Intent intent = new Intent(context, MyGamesActivity.class);
                                                    context.startActivity(intent);
                                                    Activity activity = (Activity) context;
                                                    activity.finish();
                                                }
                                            });
                                    builder.show();

                                }
                                else if(response.equals("game exists"))
                                {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setMessage(context.getString(R.string.game_exists, data.get(position).get("title")));
                                    builder.setPositiveButton(context.getString(R.string.OK),
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });
                                    builder.show();
                                }
                                else
                                {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setMessage(context.getString(R.string.game_could_not_be_created));
                                    builder.setPositiveButton(context.getString(R.string.OK),
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });
                                    builder.show();
                                }
                            }
                        });
                builder.setNegativeButton(context.getString(R.string.No),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                builder.show();
            }
        });
	       
	    return convertView; 
    }
}
