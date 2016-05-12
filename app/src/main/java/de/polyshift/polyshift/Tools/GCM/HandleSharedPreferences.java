package de.polyshift.polyshift.Tools.GCM;

/**
 * Created by Nicolas on 09.05.2015.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class HandleSharedPreferences {

    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_USER_NAME = "user_name";
    public static final String PROPERTY_PASSWORD = "password";
    public static final String PROPERTY_APP_VERSION = "appVersion";

    public static void setUserCredentials(Context context, String username, String password){
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(PROPERTY_USER_NAME, username);
        editor.putString(PROPERTY_PASSWORD, password);
        editor.commit();
    }

    public static String getUserCredentials(Context context, String key){
        Log.d("SaveSharedPreference", PreferenceManager.getDefaultSharedPreferences(context).getString(key, ""));
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, "");
    }

    public static SharedPreferences getSharedPreferences(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SharedPreferences getGcmPreferences(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setGcmPreferences(Context context, String regId, int appVersion){
        final SharedPreferences prefs = getGcmPreferences(context);
        Log.i("GCM", "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
    public static boolean checkfirstStart(Context context){
        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("firstrun", true)){
            Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putBoolean("firstrun", false);
            editor.commit();
            return true;
        }else{
            return false;
        }
    }

    //public static void setRegid(Context context,)
}
