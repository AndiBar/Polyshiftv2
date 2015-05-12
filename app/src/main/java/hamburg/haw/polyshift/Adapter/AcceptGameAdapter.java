package hamburg.haw.polyshift.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import hamburg.haw.polyshift.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AcceptGameAdapter extends SimpleAdapter {
    private ArrayList<String>checked_game_ids;
    private LayoutInflater inflater;
    public ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String,String>>();

	public AcceptGameAdapter(Context context,
                             List<? extends Map<String, ?>> data, int resource, String[] from,
                             int[] to) {
		super(context, data, resource, from, to);
		this.inflater = LayoutInflater.from(context);
		this.checked_game_ids = new ArrayList<String>();
		this.data = (ArrayList<HashMap<String, String>>) data;
		// TODO Auto-generated constructor stub
	}

	public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.activity_opponent_item, null);
        }
        final CheckBox check_box = (CheckBox) convertView.findViewById(R.id.contactCheckBox);
        final TextView user_view = (TextView) convertView.findViewById(R.id.title);
        user_view.setText(data.get(position).get("opponent_name"));

		check_box.setOnClickListener(new View.OnClickListener() {
			    @Override
	        public void onClick(View v) {

	            if(checked_game_ids.contains(data.get(position).get("game_id"))){
	            	checked_game_ids.remove(data.get(position).get("game_id"));
	            }
                else{
                    checked_game_ids.add(data.get(position).get("game_id"));
                }
	        }
	    });
	       
	    return convertView; 
    }
	public ArrayList<String> getCheckedGameIDs(){
		return checked_game_ids;
	}
}
