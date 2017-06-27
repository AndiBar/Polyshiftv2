package de.polyshift.polyshift.Menu;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;

import de.polyshift.polyshift.Tools.PHPConnector;
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

        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("device_id", android_id));
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
}
