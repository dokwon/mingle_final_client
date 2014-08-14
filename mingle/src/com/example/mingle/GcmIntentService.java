package com.example.mingle;

import java.util.Set;

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
import android.support.v4.content.LocalBroadcastManager;
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
	public final static String DATA_BUNDLE = "com.example.mingle.DATA_BUNDLE";


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
                String send_uid = extras.getString("send_uid");
                MingleApplication app = ((MingleApplication)this.getApplicationContext());
                
                //If socket is not connected, then update chat list with GCM msg, 
                //and reconnect the socket for further use.
                if(!app.socketHelper.isSocketConnected() ) {
                	JSONObject msg_obj = new JSONObject();
                	Set<String> keys = extras.keySet();
                	for (String key : keys) {
                	    try {
                	    	if(key != "android.support.content.wakelockid")
                	    		msg_obj.put(key, extras.getString(key));
                	    } catch(JSONException e) {
                	    	e.printStackTrace();
                	    }
                	}
                	
                	app.handleIncomingMsg(msg_obj);
                	app.socketHelper.connectSocket();
                }
 
                //If the user is looking at the chat room now, we need not show notification.
                if(!app.getMingleUser(send_uid).isInChat() && app.getNotiFlag()) {
                	openPopupActivity(extras);
                	sendNotification(extras);
                }
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
       
		Intent chat_intent = new Intent((MingleApplication)this.getApplicationContext(), ChatroomActivity.class);
 		chat_intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		chat_intent.putExtra(ChatroomActivity.USER_UID, data.getString("send_uid"));
		chat_intent.putExtra(ChatroomActivity.FROM_GCM, true);
		
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, chat_intent, PendingIntent.FLAG_UPDATE_CURRENT);
        CharSequence tickerTxt = (CharSequence)("Mingle: " + data.getString("msg"));
        
		builder = new NotificationCompat.Builder(this)
				       .setSmallIcon(((MingleApplication)this.getApplicationContext()).blankProfileImageSmall)
				       .setContentTitle("Mingle")
				       .setContentText(data.getString("msg"))
				       .setTicker(tickerTxt)
				       .setDefaults(Notification.DEFAULT_ALL)
				       .setAutoCancel(true);	
		
		builder.setContentIntent(contentIntent);
		NOTIFICATION_ID = (NOTIFICATION_ID + 1) % 10;
		mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }
    
    private void openPopupActivity(Bundle data){
    	Intent dispatcher = new Intent(this, TransparentActivity.class);
    	dispatcher.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | 
    							Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	dispatcher.putExtra(DATA_BUNDLE, data);
		startActivity(dispatcher);
    }
}