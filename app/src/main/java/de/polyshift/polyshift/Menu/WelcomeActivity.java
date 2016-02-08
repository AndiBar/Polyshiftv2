package de.polyshift.polyshift.Menu;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import de.polyshift.polyshift.Adapter.LoginAdapter;
import de.polyshift.polyshift.Analytics.AnalyticsApplication;
import de.polyshift.polyshift.R;
import de.polyshift.polyshift.Tools.PasswordHash;
import de.polyshift.polyshift.Tools.PHPConnector;

public class WelcomeActivity extends Activity {
	
	Button loginButton;
    Button forgotButton;
    EditText editUsername,editPassword;
    TextView debugText;
    HttpPost httppost;
    StringBuffer buffer;
    HttpResponse response;
    HttpClient httpclient;
    List<NameValuePair> nameValuePairs;
    public static ProgressDialog dialog = null;
    private static Context context;
    private static Activity activity;
    private LoginAdapter loginAdapter;

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private Tracker mTracker = null;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */

    /**
     * Tag used on log messages.
     */
    static final String TAG = WelcomeActivity.class.getName();

    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    public static String regid;

//    public WelcomeActivity(){
//    	Log.d("WelcomeActivity","constructor");
//    }
    
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context=getApplicationContext();
        activity=this;
        this.loginAdapter = new LoginAdapter(context,WelcomeActivity.this);
		setContentView(R.layout.activity_welcome);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName(getClass().getName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        // Check for auto login credentials
        // Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        if (checkPlayServices()) {
            loginButton = (Button)findViewById(R.id.LoginButton);
            forgotButton = (Button)findViewById(R.id.forgotButton);
            editUsername = (EditText)findViewById(R.id.EditUsername);
            editPassword= (EditText)findViewById(R.id.EditPassword);
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);
            if(regid.equals("")){
                new Thread(
                        new Runnable() {
                            public void run() {
                                regid = getNewRegistrationID();
                            }
                        }
                ).start();
            }
            Log.d("Reg","reg:" + regid);
            final String username = HandleSharedPreferences.getUserCredentials(context, "user_name");
            final String password = HandleSharedPreferences.getUserCredentials(context,"password");
            if(!(username.equals("")) && (!(password.equals("")))){
                dialog = ProgressDialog.show(activity, "", getString(R.string.login_in_progress), true);
                Log.i("Autologin", "Login läuft..."+ username +" "+password);
                new Thread(
                        new Runnable(){
                            public void run(){
                                loginAdapter.userLoginStoredCredentials();
                            }
                        }
                ).start();
            }

        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }


        loginButton.setOnClickListener(
    		new OnClickListener() {
	            @Override
	            public void onClick(View v) {
	                dialog = ProgressDialog.show(WelcomeActivity.this, "",getString(R.string.login_in_progress), true);
                    final SharedPreferences prefs = HandleSharedPreferences.getGcmPreferences(context);
                    HandleSharedPreferences.setUserCredentials(context, editUsername.getText().toString().trim(), PasswordHash.toHash(editPassword.getText().toString().trim()));	//	username und pw werden gespeichert, damit beim nächsten Mal kein Login notwendig ist
                    new Thread(
	                		new Runnable(){
	                			public void run(){
	                				loginAdapter.newUserLogin(editUsername.getText().toString().trim(), PasswordHash.toHash(editPassword.getText().toString().trim()), WelcomeActivity.this, regid);
	                			}
	                		}
	                ).start();
	            }
    		}
    	);

        forgotButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(WelcomeActivity.this);
                        final EditText edittext = new EditText(WelcomeActivity.this);
                        alert.setMessage(R.string.please_enter_email);
                        alert.setTitle(R.string.email_address);

                        alert.setView(edittext);

                        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                final String email = edittext.getText().toString();
                                new Thread(
                                        new Runnable() {
                                            public void run() {
                                                LoginAdapter.validateEmail(email, WelcomeActivity.this);
                                            }
                                        }
                                ).start();

                            }
                        });

                        alert.show();
                    }
                }
        );
	}


    public void userSignup(View view){
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
        this.finish();
    }

    public static void userLogout() {
        new Thread(
                new Runnable() {
                    public void run() {
                        PHPConnector.doRequest("logout_user.php");
                    }
                }
        ).start();
    }



    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = HandleSharedPreferences.getGcmPreferences(context);
        String registrationId = prefs.getString(HandleSharedPreferences.PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(HandleSharedPreferences.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }

        return registrationId;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private String getNewRegistrationID() {
        Log.d("doInBackground", "active");
        String msg = "";
        String regid = "";
        try {
            if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(context);
            }
            regid = gcm.register(getString(R.string.gcm_sender_id));
            msg = "Device registered, registration ID=" + regid;
            Log.d("regid success", msg);
            // You should send the registration ID to your server over HTTP, so it
            // can use GCM/HTTP or CCS to send messages to your app.

            // For this demo: we don't need to send it because the device will send
            // upstream messages to a server that echo back the message using the
            // 'from' address in the message.

            // Persist the regID - no need to register again.
            HandleSharedPreferences.setGcmPreferences(context, regid, WelcomeActivity.getAppVersion(context));
        } catch (IOException ex) {
            msg = "Error :" + ex.getMessage();
            Log.d("regid error", msg);
            // If there is an error, don't just keep trying to register.
            // Require the user to click a button again, or perform
            // exponential back-off.
        }
        return regid;
    }

}