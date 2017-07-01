package de.polyshift.polyshift.Tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import de.polyshift.polyshift.Tools.GCM.HandleSharedPreferences;
import de.polyshift.polyshift.R;

/**
 * Führt die Benutzerauthentifizierung durch und stellt eine Methode zum Generieren eines
 * neuen Passworts zur Verfügung.
 *
 * @author nicolas
 *
 */
public class LoginTool {

    static private String encryptedPassword;
    public static String username;
    static private String GCMregId;
    final private Context context;
    static private Activity activity;

    public LoginTool(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        final SharedPreferences prefs = HandleSharedPreferences.getGcmPreferences(context);
        this.GCMregId = prefs.getString(HandleSharedPreferences.PROPERTY_REG_ID, "");
    }

    public void handleSessionExpiration(final Activity calling_activity) {
        class TestSessionThread extends Thread {
            public void run() {
                String response = PHPConnector.doRequest("test_session.php");
                if (response.equalsIgnoreCase("not logged in")) {
                    Log.i("Status Login", "Not logged in... using device id for login");
                    AuthManager.checkIfDeviceKnown(activity);
                } else if (response.endsWith("logged in")) {
                    Log.i("Status Login:", response);
                    username = response.split(" ")[0];
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
        /*Log.i("GCM REGID stored login",GCMregId);
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("username",username.toString().trim()));
        nameValuePairs.add(new BasicNameValuePair("password",encryptedPassword));
        nameValuePairs.add(new BasicNameValuePair("regid", GCMregId));

        final String response = PHPConnector.doRequest(nameValuePairs, "login_user.php");
        Log.d("resp","responselogin:" + response);
        if(response.equalsIgnoreCase(username + " has logged in successfully.")){
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity, activity.getString(R.string.successfully_logged_in, response.split(" ")[0]), Toast.LENGTH_SHORT).show();
                }
            });
            Intent intent;
            if(activity instanceof WelcomeActivity) {
                intent = new Intent(activity, MainMenuActivity.class);
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
            Intent intent;
            if(activity instanceof WelcomeActivity){
                intent = new Intent(activity, MainMenuActivity.class);
            }else {
                intent = new Intent(activity, activity.getClass());
            }
            activity.startActivity(intent);
            activity.finish();

        }else if(response.equalsIgnoreCase("No Such User Found")){
            if(WelcomeActivity.dialog!=null){WelcomeActivity.dialog.dismiss();};
            AlertDialogs.showAlert(activity, activity.getString(R.string.login_error_title), activity.getString(R.string.login_error));
        }else{
            if(WelcomeActivity.dialog!=null){WelcomeActivity.dialog.dismiss();};
            AlertDialogs.showAlert(activity, activity.getString(R.string.login_error_title), activity.getString(R.string.connection_error));
        }*/
    }

    public static void newUserLogin(final String newUsername, final String newEncryptedPassword, final Activity newActivity, String newGCMregId) {
        /*Log.i("GCM REGID new login",newGCMregId);
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
        }*/

    }
    public static void validateEmail(String email, final Activity newActivity){
        /*HashMap<String, String> user_map = new HashMap<String, String>();
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
            nameValuePairs.add(new BasicNameValuePair("text", newActivity.getString(R.string.new_password_text)));
            nameValuePairs.add(new BasicNameValuePair("footer", newActivity.getString(R.string.regards)));
            nameValuePairs.add(new BasicNameValuePair("sender", newActivity.getString(R.string.footer)));
            String random_password = generateRandomPassword(8);
            nameValuePairs.add(new BasicNameValuePair("password", random_password));
            nameValuePairs.add(new BasicNameValuePair("password_hash", PasswordHash.toHash(random_password)));

            final String response2 = PHPConnector.doRequest(nameValuePairs, "update_password.php");

            if(response2.equals("password updated.")){
                AlertDialogs.showAlert(newActivity, newActivity.getString(R.string.new_password), newActivity.getString(R.string.new_password_sent));
            }else{
                AlertDialogs.showAlert(newActivity, newActivity.getString(R.string.error), newActivity.getString(R.string.password_not_created));
            }
        }else{
            AlertDialogs.showAlert(newActivity, newActivity.getString(R.string.error), newActivity.getString(R.string.no_user_found));
        }*/
    }
}
