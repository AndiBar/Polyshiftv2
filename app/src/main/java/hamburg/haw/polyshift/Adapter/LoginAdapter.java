package hamburg.haw.polyshift.Adapter;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import hamburg.haw.polyshift.Menu.HandleSharedPreferences;
import hamburg.haw.polyshift.Menu.MainMenuActivity;
import hamburg.haw.polyshift.Menu.WelcomeActivity;
import hamburg.haw.polyshift.R;
import hamburg.haw.polyshift.Tools.AlertDialogs;
import hamburg.haw.polyshift.Tools.PHPConnector;
import hamburg.haw.polyshift.Tools.PasswordHash;


/**
 * Created by Nicolas on 16.05.2015.
 */
public class LoginAdapter{

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

    public void handleSessionExpiration(final Activity calling_activity) {
        class TestSessionThread extends Thread {
            public void run() {
                String response = PHPConnector.doRequest("test_session.php");
                if (response.equalsIgnoreCase("not logged in") && (!encryptedPassword.equals("")) && (!username.equals(""))) {
                    Log.i("Status Login", "Not logged in... using stored credentials for login");
                    userLoginStoredCredentials();
                } else if (response.equalsIgnoreCase("logged in")) {
                    Log.i("Status Login:", response);
                } else {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, context.getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                        }
                    });
                    Intent intent = new Intent(activity, calling_activity.getClass());
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
        nameValuePairs.add(new BasicNameValuePair("regid", GCMregId));

        final String response = PHPConnector.doRequest(nameValuePairs, "login_user.php");
        if(response.equalsIgnoreCase(username + " has logged in successfully.")){
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity, activity.getString(R.string.successfully_logged_in, response.split(" ")[0]), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(activity, activity.getString(R.string.already_logged_in), Toast.LENGTH_SHORT).show();
                }
            });
            if(activity instanceof WelcomeActivity) {
                Intent intent = new Intent(activity, MainMenuActivity.class);
                activity.startActivity(intent);
                activity.finish();
            }
        }else if(response.equalsIgnoreCase("No Such User Found")){
            if(WelcomeActivity.dialog!=null){WelcomeActivity.dialog.dismiss();};
            AlertDialogs.showAlert(activity, activity.getString(R.string.login_error_title), activity.getString(R.string.login_error));
        }else{
            if(WelcomeActivity.dialog!=null){WelcomeActivity.dialog.dismiss();};
            AlertDialogs.showAlert(activity, activity.getString(R.string.login_error_title), activity.getString(R.string.connection_error));
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
                    Toast.makeText(newActivity, activity.getString(R.string.successfully_logged_in, response.split(" ")[0]), Toast.LENGTH_SHORT).show();
                }
            });

            Intent intent = new Intent(newActivity, MainMenuActivity.class);
            activity.startActivity(intent);
            activity.finish();
        }
        else if(response.equalsIgnoreCase(newUsername + " already logged in.")){
            newActivity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(newActivity,newActivity.getString(R.string.already_logged_in), Toast.LENGTH_SHORT).show();
                }
            });
            Intent intent = new Intent(newActivity, MainMenuActivity.class);
            newActivity.startActivity(intent);
            newActivity.finish();
        }else if(response.equalsIgnoreCase("No Such User Found")){
            if(WelcomeActivity.dialog!=null){WelcomeActivity.dialog.dismiss();};
            AlertDialogs.showAlert(newActivity, newActivity.getString(R.string.login_error_title), newActivity.getString(R.string.login_error));
        }else{
            if(WelcomeActivity.dialog!=null){WelcomeActivity.dialog.dismiss();};
            AlertDialogs.showAlert(newActivity, newActivity.getString(R.string.login_error_title), newActivity.getString(R.string.connection_error));
        }

    }
    public static void validateEmail(String email, final Activity newActivity){
        HashMap<String, String> user_map = new HashMap<String, String>();
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("email", email));
        final String response = PHPConnector.doRequest(nameValuePairs, "validate_email.php");

        if(response.split(";").length != 1) {
            String[] data_array = response.split(";");
            nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("user_id", data_array[0].split("=")[1]));
            nameValuePairs.add(new BasicNameValuePair("email", data_array[2].split("=")[1]));
            nameValuePairs.add(new BasicNameValuePair("title", newActivity.getString(R.string.email_title)));
            nameValuePairs.add(new BasicNameValuePair("header", "Hallo " + data_array[1].split("=")[1] + ","));
            nameValuePairs.add(new BasicNameValuePair("text", "Dein neues Polyshift Passwort lautet: "));
            String random_password = generateRandomPassword(8);
            nameValuePairs.add(new BasicNameValuePair("password", random_password));
            nameValuePairs.add(new BasicNameValuePair("password_hash", PasswordHash.toHash(random_password)));

            final String response2 = PHPConnector.doRequest(nameValuePairs, "update_password.php");

            if(response2.equals("password updated.")){
                AlertDialogs.showAlert(newActivity, "Neues Passwort", "Es wurde ein neues Passwort an die von dir angegebene E-Mail Adresse gesendet.");
            }else{
                AlertDialogs.showAlert(newActivity, "Fehler", "Es konnte kein neues Passwort erstellt werden.");
            }
        }else{
            AlertDialogs.showAlert(newActivity, "Fehler", "Es wurde kein Benutzer mit dieser E-Mail Adresse gefunden.");
        }
    }

    public static String generateRandomPassword(int password_length)
    {
        // Pick from some letters that won't be easily mistaken for each
        // other. So, for example, omit o O and 0, 1 l and L.
        String letters = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789+@";

        Random RANDOM = new SecureRandom();
        String pw = "";
        for (int i=0; i < password_length; i++)
        {
            int index = (int)(RANDOM.nextDouble()*letters.length());
            pw += letters.substring(index, index+1);
        }
        return pw;
    }
}
