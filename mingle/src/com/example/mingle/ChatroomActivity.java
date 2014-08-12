package com.example.mingle;

import java.sql.Timestamp;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView.BufferType;

public class ChatroomActivity extends ListActivity {
    public final static String USER_UID = "com.example.mingle.USER_SEL";	//Intent data to pass on when new Chatroom Activity started
    public final static String FROM_GCM = "com.example.mingle.FROM_GCM";	//Intent data to pass on when Chatroom Activity is started by GCM notification

	Button btnSend;															//Button for sending a message
	EditText txtSMS;														//Field that holds message content    
	ListView msg_lv;														//Listview for messages
    MsgAdapter adapter;														//Listview adapter for msg_lv
    
    MingleUser recv_user;													//Mingle User currently chatting with
	
    @Override
    protected void onNewIntent(Intent intent){
    	Log.i("sktag", "catch intent -> " + intent.getExtras().getString("USER_ID"));
    	setIntent(intent);
    }
    
    @Override
    protected void onResume(){
        super.onResume();
        
        Intent intent = getIntent();
        String recv_uid = intent.getExtras().getString(USER_UID);
        
        //Set basic information required for this chat room
        MingleApplication app = ((MingleApplication) this.getApplication());
        recv_user = app.getMingleUser(recv_uid);
        
        //Clear the notification from gcm.
        ((NotificationManager)this.getSystemService(NOTIFICATION_SERVICE)).cancelAll();
        recv_user.setInChat(true);
		
		//Associate this chat room's message list to adapter
        msg_lv = (ListView) findViewById(android.R.id.list);
		adapter=new MsgAdapter(this,
                R.layout.msg_row, R.layout.my_msg_row,
                recv_user.getMsgList(), recv_user);
        setListAdapter(adapter);
        
    }
    
    @Override
    protected void onPause() {
    	recv_user.setInChat(false);
    	super.onPause();
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    
        LocalBroadcastManager.getInstance(this).registerReceiver(refListReceiver,
        		  new IntentFilter(MingleApplication.UPDATE_MSG_LIST));
        
		setContentView(R.layout.activity_chatroom);
    }
    
    /* Function to be called when message send button is clicked */
    public void sendSMS(View v){
        MingleApplication app = ((MingleApplication) this.getApplication());
        
        //Open socket if closed
        app.socketHelper.connectSocket();

		recv_user.incrementMsgCounter();;

		txtSMS=(EditText) findViewById(R.id.txt_inputText);
		String SMS=txtSMS.getText().toString();
		
		//Determine if my message is response
		boolean response_msg = true;
		if(!recv_user.isMsgListEmpty() && recv_user.getLastMsg().isMyMsg()){
			response_msg = false;
			DatabaseHelper db = ((MingleApplication) this.getApplication()).dbHelper;
			// Stores messages in DB
			db.insertMessages(recv_user.getUid(), false, SMS , new Timestamp(System.currentTimeMillis()).toString());
		}
		
		//Send MSG to Server
		JSONObject msgObject = new JSONObject();
        try {
            msgObject.put("send_uid", app.getMyUser().getUid());
            msgObject.put("recv_uid", recv_user.getUid());
            msgObject.put("msg", SMS);
            msgObject.put("msg_counter", recv_user.getMsgCounter());
            
            //If first message, identity = 2
            //If not first but response message, identity = 1
            //If not first and not response message, identity = 0
            if(recv_user.isMsgListEmpty()){
            	msgObject.put("identity", 2);
            } else if(response_msg){
            	msgObject.put("identity", 1);
            } else {
            	msgObject.put("identity", 0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
		
        //Save MSG and show it is in the process of getting sent.
		recv_user.addMsg(SMS, recv_user.getMsgCounter(), 0);
		updateMessageList();
        ((MingleApplication) this.getApplication()).socketHelper.sendMessageToServer(msgObject);
		
		txtSMS.setText("",BufferType.NORMAL);
	}
    
    /* Update message list */
    public void updateMessageList(){
    	runOnUiThread(new Runnable() {
    		public void run() {
    			adapter.notifyDataSetChanged();
    		}
    	});
    	
    	//Show the most recent message into view
    	msg_lv.post(new Runnable() {
	        @Override
	        public void run() {
	            msg_lv.setSelection(adapter.getCount());
	        }
	    });
    }    
    
    /* Broadcast Receiver for notification of receiving chat message from the server*/
    private BroadcastReceiver refListReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    	   // Extract data included in the Intent
    	   Log.d("chat receiver", "Got intent: ");
   		   updateMessageList();
    	}
    };
    
    /* If current activity is called from GCM not Hunt, start Hunt */
    @Override
    public void onBackPressed(){
    	if(this.isTaskRoot()){
    		Intent huntIntent = new Intent(this, HuntActivity.class);
    		startActivity(huntIntent);
    	}
    	super.onBackPressed();
    }
    
    @Override
    public void onDestroy(){    	
    	LocalBroadcastManager.getInstance(this).unregisterReceiver(refListReceiver);
    	super.onDestroy();
    }
}
