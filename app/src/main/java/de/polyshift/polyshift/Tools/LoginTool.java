package de.polyshift.polyshift.Tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import de.polyshift.polyshift.Tools.GCM.HandleSharedPreferences;
import de.polyshift.polyshift.R;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

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
    private boolean loggedIn;

    public LoginTool(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        final SharedPreferences prefs = HandleSharedPreferences.getGcmPreferences(context);
        this.GCMregId = prefs.getString(HandleSharedPreferences.PROPERTY_REG_ID, "");
    }

    public Observable<String> handleSessionExpiration(final Activity calling_activity) {
        return PHPConnector.doObservableRequest("test_session.php")
                .retry(5)
                .subscribeOn(Schedulers.io())
                .doOnError(error -> loggedIn = false)
                .map((Func1<String, String>) response -> {
                    if (response.equalsIgnoreCase("not logged in")) {
                        Log.i("Status Login", "Not logged in... using device id for login");
                        AuthManager.checkIfDeviceKnown(activity);
                        loggedIn = true;
                    } else if (response.endsWith("logged in")) {
                        Log.i("Status Login:", response);
                        username = response.split(" ")[0];
                        loggedIn = true;
                    } else {
                        loggedIn = false;
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(activity, context.getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                            }
                        });
                        Intent intent = new Intent(activity, calling_activity.getClass());
                        activity.startActivity(intent);
                        activity.finish();
                    }
                    return response;
                });
    }

    public void handleSessionExpirationBlocking(final Activity calling_activity) {
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

    public boolean isLoggedIn(){
        return loggedIn;
    }
}