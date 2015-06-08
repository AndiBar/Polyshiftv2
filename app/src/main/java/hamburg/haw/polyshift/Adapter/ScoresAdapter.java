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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoresAdapter extends SimpleAdapter {
    private LayoutInflater inflater;
    private Context context;
    public ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String,String>>();
    public String response;

    public ScoresAdapter(Context context,
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
            convertView = inflater.inflate(R.layout.activity_scores_item, null);
        }
        if(!data.isEmpty()) {

            final TextView username_view = (TextView) convertView.findViewById(R.id.username);
            username_view.setText(position + 1 + ". " + data.get(position).get("username"));

            final TextView win_view = (TextView) convertView.findViewById(R.id.win);
            win_view.setText(data.get(position).get("win"));

            final TextView loss_view = (TextView) convertView.findViewById(R.id.loss);
            loss_view.setText(data.get(position).get("loss"));

            final TextView score_view = (TextView) convertView.findViewById(R.id.score);
            score_view.setText(data.get(position).get("score"));

        }
        return convertView;
    }

}
