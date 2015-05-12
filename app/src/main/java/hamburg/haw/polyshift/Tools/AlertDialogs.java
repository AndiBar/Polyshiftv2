package hamburg.haw.polyshift.Tools;

import java.util.Calendar;

import hamburg.haw.polyshift.Menu.WelcomeActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;

public class AlertDialogs {
	
	public Activity activity;
	public String[] article;
	public String store;
	public String date;
	public Calendar new_date;
	static android.app.AlertDialog alert;
	
	public AlertDialogs(Activity activity){
		this.activity = activity;
		this.article = new String[3];
	}
	public static void showAlert(final Activity ParentActivity, final String title, final String message){
        ParentActivity.runOnUiThread(new Runnable() {
            public void run() {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ParentActivity);
                builder.setTitle(title);
                builder.setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                android.app.AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }
	public static void showLoginAgainAlert(final Activity ParentActivity){
        ParentActivity.runOnUiThread(new Runnable() {
            public void run() {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ParentActivity);
                builder.setTitle("Sitzung abgelaufen");
                builder.setMessage("Bitte erneut anmelden.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            	Intent intent = new Intent(ParentActivity, WelcomeActivity.class);
                                ParentActivity.startActivity(intent);
                            }
                        });
                alert = builder.create();
                alert.show();
            }
        });
    }
}
