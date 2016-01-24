package hamburg.haw.polyshift.Menu;

/**
 * Created by Nicolas on 07.05.2015.
 */
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import hamburg.haw.polyshift.Game.PolyshiftActivity;
import hamburg.haw.polyshift.R;
import hamburg.haw.polyshift.Tools.PHPConnector;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }
    public static final String TAG = "GCM Service";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        String messageTmp = extras.getString("message");
        String message = "";
        try {
            message = URLDecoder.decode(messageTmp, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String title = extras.getString("title");
        String gameID = extras.getString("game_id");
        String className = extras.getString("class_name");
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + message, "Error", "", "");
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " + message, "Error", "", "");
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Post notification of received message.
                sendNotification(message, title, gameID, className);
                Log.i(TAG, "Received: " + message);
                Log.i(TAG, "title: " + message);
                Log.i(TAG, "Game_ID" + gameID);
                Log.i(TAG, "class_name" + className);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg, String title, String gameID, String className) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent;
        if(className.equals(OpponentsAttendingActivity.class.getName())) {
            contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, OpponentsAttendingActivity.class), 0);
        }else if(className.equals(ChooseOpponentActivity.class.getName())){
            contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ChooseOpponentActivity.class), 0);
        }else if(className.equals(MyGamesActivity.class.getName())){
            contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MyGamesActivity.class), 0);
        }else if(className.equals(GamesAttendingActivity.class.getName())){
            contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, GamesAttendingActivity.class), 0);
        }else if(className.equals(PolyshiftActivity.class.getName())){
            setGameID(gameID);
            contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, PolyshiftActivity.class), 0);
        }else{
            contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainMenuActivity.class), 0);
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg)
                        .setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void setGameID(final String gameID){
        class Update_Game_Thread extends Thread {
            public void run() {
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("game", gameID));
                PHPConnector.doRequest(nameValuePairs, "update_game.php");
            }
        }
        Thread update_game_thread = new Update_Game_Thread();
        update_game_thread.start();
        try {
            long waitMillis = 10000;
            while (update_game_thread.isAlive()) {
                update_game_thread.join(waitMillis);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
