package hamburg.haw.polyshift.Adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;

import hamburg.haw.polyshift.Menu.HandleSharedPreferences;
import hamburg.haw.polyshift.Menu.MainMenuActivity;
import hamburg.haw.polyshift.Menu.WelcomeActivity;
import hamburg.haw.polyshift.Tools.AlertDialogs;
import hamburg.haw.polyshift.Tools.PHPConnector;


/**
 * Created by Nicolas on 16.05.2015.
 */
public class LoginAdapter{
    protected static ProgressDialog dialog = null;

    static private String encryptedPassword;
    static private String username;
    static private String GCMregId;
    final private Context context;
    static private Activity activity;

    public LoginAdapter(Context context,Activity activity) {
        this.context = context;
        this.activity = activity;
        this.username = HandleSharedPreferences.getUserCredentials(context, "user_name");
        this.encryptedPassword = HandleSharedPreferences.getUserCredentials(context, "password");
        final SharedPreferences prefs = HandleSharedPreferences.getGcmPreferences(context);
        this.GCMregId = prefs.getString(HandleSharedPreferences.PROPERTY_REG_ID, "");
    }

    public void handleSessionExpiration() {
        class TestSessionThread extends Thread {
            public void run() {
                String response = PHPConnector.doRequest("test_session.php");
                if (response.equalsIgnoreCase("not logged in") && (!encryptedPassword.equals("")) && (!username.equals(""))) {
                    Log.i("Status Login","Not logged in... using stored credentials for login");
                    userLoginStoredCredentials();
                } else if (response.equalsIgnoreCase("logged in")) {
                    Log.i("Status Login:", response);
                } else {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, "Sie sind nicht eingeloggt...", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Intent intent = new Intent(activity, WelcomeActivity.class);
                    activity.startActivity(intent);
                    activity.finish();
                }
            }
        }
        Thread test_session_thread = new TestSessionThread();
        test_session_thread.start();
        try {
            long waitMillis = 10000;
            while (test_session_thread.isAlive()) {
                test_session_thread.join(waitMillis);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void userLoginStoredCredentials(){
        Log.i("GCM REGID stored login",GCMregId);
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("username",username.toString().trim()));
        nameValuePairs.add(new BasicNameValuePair("password",encryptedPassword));
        nameValuePairs.add(new BasicNameValuePair("regid",GCMregId));

        final String response = PHPConnector.doRequest(nameValuePairs, "login_user.php");
        if(response.equalsIgnoreCase(username + " has logged in successfully.")){
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity, username + " wurde erfolgreich angemeldet.", Toast.LENGTH_SHORT).show();
                }
            });
            if(activity instanceof WelcomeActivity) {
                Intent intent = new Intent(activity, MainMenuActivity.class);
                activity.startActivity(intent);
                activity.finish();
            }
        }
        else if(response.equalsIgnoreCase(username + " already logged in.")){
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity,response, Toast.LENGTH_SHORT).show();
                }
            });
            if(activity instanceof WelcomeActivity) {
                Intent intent = new Intent(activity, MainMenuActivity.class);
                activity.startActivity(intent);
                activity.finish();
            }
        }else if(response.equalsIgnoreCase("No Such User Found")){
            dialog.dismiss();
            AlertDialogs.showAlert(activity, "Login Error", "User not found or password incorrect.");
        }else{
            dialog.dismiss();
            AlertDialogs.showAlert(activity, "Login Error", "Connection Error.");
        }
    }

    public static void newUserLogin(final String newUsername, final String newEncryptedPassword, final Activity newActivity, String newGCMregId) {
        Log.i("GCM REGID new login",newGCMregId);
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("username",newUsername.toString().trim()));
        nameValuePairs.add(new BasicNameValuePair("password",newEncryptedPassword));
        nameValuePairs.add(new BasicNameValuePair("regid",newGCMregId));

        final String response = PHPConnector.doRequest(nameValuePairs, "login_user.php");
        if(response.equalsIgnoreCase(newUsername + " has logged in successfully.")){
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(newActivity, newUsername + " wurde erfolgreich angemeldet.", Toast.LENGTH_SHORT).show();
                }
            });

            Intent intent = new Intent(newActivity, MainMenuActivity.class);
            activity.startActivity(intent);
            activity.finish();
        }
        else if(response.equalsIgnoreCase(newUsername + " already logged in.")){
            newActivity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(newActivity,response, Toast.LENGTH_SHORT).show();
                }
            });
            Intent intent = new Intent(newActivity, MainMenuActivity.class);
            newActivity.startActivity(intent);
            newActivity.finish();
        }else if(response.equalsIgnoreCase("No Such User Found")){
            dialog.dismiss();
            AlertDialogs.showAlert(newActivity, "Login Error", "User not found or password incorrect.");
        }else{
            dialog.dismiss();
            AlertDialogs.showAlert(newActivity, "Login Error", "Connection Error.");
        }

    }
}
