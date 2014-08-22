package com.example.mingle;

import java.util.HashMap;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {
	
	public static int NOTIFICATION_MSG_ID = -1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    static final String TAG = "GCM Demo";
	public final static String DATA_BUNDLE = "com.example.mingle.DATA_BUNDLE";

	private static int NEXT_NOTIFICATION_ID = 0;
	private static int numMsgNotification = 0;

	private static HashMap<String, Integer> notificationMap = new HashMap<String, Integer>();
	
	/*TODO clear notification map */
	
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
                String type = extras.getString("gcm_type");
                if(type.equals("vote")){
                    sendVoteNotification(extras);
                } else {
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
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendVoteNotification(Bundle data) {
        mNotificationManager = (NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);
        MingleApplication app = (MingleApplication)this.getApplicationContext();
        String user_type = "candidate";
        String voter_uid = data.getString("voter_uid");
        MingleUser user = app.getMingleUser(voter_uid);
		if(user == null) {
			user = new MingleUser(voter_uid, "", 0, 0, (Drawable) app.getResources().getDrawable(app.blankProfileImageSmall), "", 0);
			app.addMingleUser(user);
			app.addCandidate(voter_uid);
			app.connectHelper.getNewUser(voter_uid, "candidate");
		} else {
			int candidate_pos = app.getCandidatePos(voter_uid);
			if(candidate_pos < 0){
				int choice_pos = app.getChoicePos(voter_uid);
				if(choice_pos < 0) {
					app.addCandidate(voter_uid);
					user_type = "choice";
				} else user_type = "popular";
			}
		}
        
		Intent profile_intent = new Intent(app, ProfileActivity.class);
 		profile_intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		profile_intent.putExtra(ProfileActivity.PROFILE_UID, data.getString("voter_uid"));
        profile_intent.putExtra(ProfileActivity.PROFILE_TYPE, user_type);
        
        PendingIntent contentIntent = PendingIntent.getActivity(this, 1, profile_intent, PendingIntent.FLAG_UPDATE_CURRENT);
        CharSequence tickerTxt = (CharSequence)(data.getString("name") + "님이 당신을 투표하였습니다.");
        
		builder = new NotificationCompat.Builder(this)
				       .setSmallIcon(R.drawable.icon_tiny)
				       .setLargeIcon(((BitmapDrawable)app.getResources().getDrawable(R.drawable.icon_tiny)).getBitmap())
				       .setContentTitle("인기투표")
				       .setContentText(tickerTxt)
				       .setTicker(tickerTxt)
				       .setDefaults(Notification.DEFAULT_ALL)
				       .setAutoCancel(true);	
		
		builder.setContentIntent(contentIntent);
		mNotificationManager.notify(NEXT_NOTIFICATION_ID, builder.build());
		notificationMap.put(voter_uid, NEXT_NOTIFICATION_ID);
		NEXT_NOTIFICATION_ID++;
    }
    
    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(Bundle data) {
        mNotificationManager = (NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);
        MingleUser send_user = ((MingleApplication)this.getApplication()).getMingleUser(data.getString("send_uid"));
        
		Intent chat_intent = new Intent((MingleApplication)this.getApplicationContext(), ChatroomActivity.class);
 		chat_intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		chat_intent.putExtra(ChatroomActivity.USER_UID, data.getString("send_uid"));
		chat_intent.putExtra(ChatroomActivity.FROM_GCM, true);
		
        PendingIntent contentIntent = PendingIntent.getActivity(this, 1, chat_intent, PendingIntent.FLAG_UPDATE_CURRENT);
        CharSequence tickerTxt = (CharSequence)(send_user.getName() + ": " + data.getString("msg"));
        
		builder = new NotificationCompat.Builder(this)
				       .setSmallIcon(R.drawable.icon_tiny)
				       .setLargeIcon(((BitmapDrawable)send_user.getPic(-1)).getBitmap())
				       .setContentTitle(send_user.getName())
				       .setContentText(data.getString("msg"))
				       .setTicker(tickerTxt)
				       .setDefaults(Notification.DEFAULT_ALL)
				       .setAutoCancel(true);	
		
		numMsgNotification ++;
		if(numMsgNotification > 1)
			builder.setContentInfo(Integer.toString(numMsgNotification));
		
		builder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_MSG_ID, builder.build());
    }
    
    private void openPopupActivity(Bundle data){
    	Intent dispatcher = new Intent(this, TransparentActivity.class);
    	dispatcher.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | 
    							Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	dispatcher.putExtra(DATA_BUNDLE, data);		
		startActivity(dispatcher);
    }

    public static int getNotificationId(String uid) {
    	return (notificationMap.get(uid) == null? -1 : notificationMap.get(uid));
    }
       
    public static void resetNumMsgNotification() {
    	numMsgNotification = 0;
    }
    
    public static void clearNotificationData() {
    	notificationMap.clear();
    	numMsgNotification = 0;
    }
}