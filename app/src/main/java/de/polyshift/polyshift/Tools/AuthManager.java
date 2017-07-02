package de.polyshift.polyshift.Tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;

import de.polyshift.polyshift.R;
import de.polyshift.polyshift.Tools.GCM.HandleSharedPreferences;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Andi on 25.06.2017.
 */

public class AuthManager {

    private static final String TAG = AuthManager.class.getName();

    public static void checkIfDeviceKnown(Context context){
       String android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        String device = getDeviceName();

        final SharedPreferences prefs = HandleSharedPreferences.getGcmPreferences(context);
        String gcmId = prefs.getString(HandleSharedPreferences.PROPERTY_REG_ID, "");
        if(gcmId.isEmpty()){
            gcmId = getNewRegistrationID(context);
        }

        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("device_id", android_id));
        nameValuePairs.add(new BasicNameValuePair("device_name", device));
        nameValuePairs.add(new BasicNameValuePair("reg_id", gcmId));
        PHPConnector.doObservableRequest(nameValuePairs, "check_device.php")
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "device checked.");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String s) {

                    }
                });
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private static String getNewRegistrationID(Context context) {
        Log.d("doInBackground", "active");
        String msg = "";
        String regid = "";
        try {

            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);

            regid = gcm.register(context.getString(R.string.gcm_sender_id));
            msg = "Device registered, registration ID=" + regid;
            Log.d("regid success", msg);
            // You should send the registration ID to your server over HTTP, so it
            // can use GCM/HTTP or CCS to send messages to your app.

            // For this demo: we don't need to send it because the device will send
            // upstream messages to a server that echo back the message using the
            // 'from' address in the message.

            // Persist the regID - no need to register again.
            HandleSharedPreferences.setGcmPreferences(context, regid, getAppVersion(context));
        } catch (IOException ex) {
            msg = "Error :" + ex.getMessage();
            Log.d("regid error", msg);
            // If there is an error, don't just keep trying to register.
            // Require the user to click a button again, or perform
            // exponential back-off.
        }
        return regid;
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
}
