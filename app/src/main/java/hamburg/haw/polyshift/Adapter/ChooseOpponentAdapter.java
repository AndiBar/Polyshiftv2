package hamburg.haw.polyshift.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import hamburg.haw.polyshift.Game.GameSync;
import hamburg.haw.polyshift.Menu.MyGamesActivity;
import hamburg.haw.polyshift.R;
import hamburg.haw.polyshift.Tools.PHPConnector;

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

	public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.activity_choose_opponent_item, null);
        }

        final TextView user_view = (TextView) convertView.findViewById(R.id.title);
        user_view.setText(data.get(position).get("title"));

        user_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                builder.setMessage(data.get(position).get("title") + " herausfordern?");
                builder = builder.setPositiveButton("Ja",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                                        nameValuePairs.add(new BasicNameValuePair("opponent_id", data.get(position).get("ID")));
                                        GameSync.SendChangeNotification(data.get(position).get("ID"),"Sie wurden herausgefordert!","");
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
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if(response.equals("game created"))
                                {
                                    Log.d("res:", response);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setMessage("Spiel wurde erstellt. Warte auf Bestätigung durch " + data.get(position).get("title") + ".");
                                    builder.setPositiveButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
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
                                    builder.setMessage("Du spielst bereits gegen " + data.get(position).get("title") + ".");
                                    builder.setPositiveButton("OK",
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
                                    builder.setMessage("Es ist ein Fehler aufgetreten. Das Spiel konnte nicht erstellt wertden.");
                                    builder.setPositiveButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });
                                    builder.show();
                                }
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
        });
	       
	    return convertView; 
    }
}
