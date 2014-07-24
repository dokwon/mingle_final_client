package com.example.mingle;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {
	
	public static int NOTIFICATION_ID = -1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    static final String TAG = "GCM Demo";


    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
   

            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Log.i(TAG, "Error: " + extras.toString());
                //Maybe show error message?
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                Log.i(TAG, "Deleted: " + extras.toString());
                //Maybe show error message?
           
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Log.i(TAG, "Received: " + extras.toString());
            	sendNotification(extras);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(Bundle data) {
        mNotificationManager = (NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);
   
		Intent chat_intent = new Intent(this, ChatroomActivity.class);
		chat_intent.putExtra(ChatroomActivity.USER_UID, data.getString("send_uid"));		
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, chat_intent, PendingIntent.FLAG_UPDATE_CURRENT);
        CharSequence tickerTxt = (CharSequence)("Mingle: " + data.getString("msg"));
        
		builder = new NotificationCompat.Builder(this)
				       .setSmallIcon(R.drawable.ic_launcher)
				       .setContentTitle("Mingle")
				       .setContentText(data.getString("msg"))
				       .setTicker(tickerTxt)
				       .setDefaults(Notification.DEFAULT_ALL)
				       .setAutoCancel(true);	
		
		builder.setContentIntent(contentIntent);
		NOTIFICATION_ID = (NOTIFICATION_ID + 1) % 10;
		mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}