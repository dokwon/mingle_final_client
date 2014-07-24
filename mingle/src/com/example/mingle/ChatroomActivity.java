package com.example.mingle;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

public class ChatroomActivity extends ListActivity {

	
	Button btnSend;
	EditText txtSMS;
	//LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    ArrayList<String> listItems=new ArrayList<String>();
    public final static String USER_UID = "com.example.mingle.USER_SEL";	//Intent data to pass on when new Chatroom Activity started
    
    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    MsgAdapter adapter;
    ListView msg_lv;
    
    String send_uid;
    String recv_uid;
    int msg_counter;
    MingleUser recv_user;
	
    @Override
    protected void onNewIntent(Intent intent){
    	setIntent(intent);
    }
    
    @Override
    protected void onResume(){
        super.onResume();
        
        Intent intent = getIntent();
        recv_uid = intent.getExtras().getString(USER_UID);
        
        //Set basic information required for this chat room
        send_uid = ((MingleApplication) this.getApplication()).getMyUser().getUid();
		//for testing purpose, set myself as receiver
		//recv_uid = send_uid;
        
        recv_user = ((MingleApplication) this.getApplication()).getMingleUser(recv_uid);
		msg_counter = -1;
		
		//Associate this chat room's message list to adapter
		adapter=new MsgAdapter(this,
                R.layout.msg_row, R.layout.my_msg_row,
                recv_user.getMsgList(), recv_user);
		
        setListAdapter(adapter);
        
        msg_lv = (ListView) findViewById(android.R.id.list);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        /*
        Intent intent = getIntent();
        recv_uid = intent.getExtras().getString(CandidateFragment.USER_UID);
        
        //Set basic information required for this chat room
        send_uid = ((MingleApplication) this.getApplication()).getMyUser().getUid();
		//for testing purpose, set myself as receiver
		//recv_uid = send_uid;
        
        recv_user = ((MingleApplication) this.getApplication()).getMingleUser(recv_uid);
		msg_counter = -1;
		
		//Associate this chat room's message list to adapter
		adapter=new MsgAdapter(this,
                R.layout.msg_row, R.layout.my_msg_row,
                recv_user.getMsgList(), recv_user);
		
        setListAdapter(adapter);
        
        msg_lv = (ListView) findViewById(android.R.id.list);
        */
        LocalBroadcastManager.getInstance(this).registerReceiver(msgConfReceiver,
      		  new IntentFilter(Socket.HANDLE_MSG_CONF));
        
        LocalBroadcastManager.getInstance(this).registerReceiver(getMsgReceiver,
        		  new IntentFilter(Socket.HANDLE_GET_MSG));
        
		setContentView(R.layout.activity_chatroom);
    }
    
    public void sendSMS(View v){
    	
    	txtSMS=(EditText) findViewById(R.id.txt_inputText);
    	
		// TODO Auto-generated method stub
		String SMS=txtSMS.getText().toString();
		
		msg_counter++;
		System.out.println("msg sent!");
		System.out.println(send_uid + " "+ recv_uid+" "+msg_counter);
		
		boolean response_msg = true;

		ArrayList<Message> list = recv_user.getMsgList();
		if(list.size() > 0 && list.get(list.size()-1).isMyMsg()){
			response_msg = false;
			DatabaseHelper db = ((MingleApplication) this.getApplication()).dbHelper;
			// Stores messages in DB
			db.insertMessages(recv_uid, send_uid,SMS , new Timestamp(System.currentTimeMillis()).toString());
		}
		
		//Send MSG to Server
		JSONObject msgObject = new JSONObject();
        try {
            msgObject.put("send_uid", send_uid);
            msgObject.put("recv_uid", recv_uid);
            msgObject.put("msg", SMS);
            msgObject.put("msg_counter", msg_counter);
            
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
		recv_user.addMsg(SMS, msg_counter, 0);
		updateMessageList();
	
		
        ((MingleApplication) this.getApplication()).socketHelper.sendMessageToServer(msgObject);
		
		txtSMS.setText("",BufferType.NORMAL);
	}
    
    public void updateMessageList(){
    	runOnUiThread(new Runnable() {
    		public void run() {
    			adapter.notifyDataSetChanged();
    		}
    	});
    	msg_lv.post(new Runnable() {
	        @Override
	        public void run() {
	            // Select the last row so it will scroll into view...
	        	System.out.println("msg last: "+String.valueOf(adapter.getCount()));
	            msg_lv.setSelection(adapter.getCount());
	        }
	    });
    }
     
    private BroadcastReceiver msgConfReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    	   // Extract data included in the Intent
    	   String data = intent.getStringExtra(Socket.MSG_CONF);
    	   Log.d("receiver", "Got message: " + data);
    	   
    	   try {
    		   JSONObject msg_conf_obj = new JSONObject(data);
    		   onMessageConf(msg_conf_obj);
    	   } catch (JSONException e) {
    		   // TODO Auto-generated catch block
    		   e.printStackTrace();
    	   }
    	   
    	  }
    };
    
    
    private BroadcastReceiver getMsgReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    	   // Extract data included in the Intent
    	   String data = intent.getStringExtra(Socket.GET_MSG);
    	   Log.d("receiver", "Got message: " + data);
    	   
    	   try {
    		   JSONObject msg_obj = new JSONObject(data);
    		   recvMessage(msg_obj);
    	   } catch (JSONException e) {
    		   // TODO Auto-generated catch block
    		   e.printStackTrace();
    	   }
    	   
    	  }
    };
    
    public void onMessageConf(JSONObject msg_conf_obj){
    	try{
    		String msg_recv_uid = msg_conf_obj.getString("recv_uid");
    		int msg_recv_counter = Integer.parseInt(msg_conf_obj.getString("msg_counter"));
    		String msg_ts = msg_conf_obj.getString("ts");

    		MingleApplication curr_user = ((MingleApplication)this.getApplication());
    		curr_user.getMingleUser(msg_recv_uid).updateMsgOnConf(msg_recv_counter, msg_ts);
    		
    	} catch (JSONException e){
    		e.printStackTrace();
    	}
    	updateMessageList();
    }
    
    public void recvMessage(JSONObject recv_msg_obj){
		try {
			String chat_user_uid = recv_msg_obj.getString("send_uid");
			MingleApplication curr_user = ((MingleApplication) this.getApplicationContext());
			
			MingleUser user = curr_user.getMingleUser(chat_user_uid);
			if(user == null){
				String sex = "M";
				if(((MingleApplication)this.getApplication()).getMyUser().getSex().equals("M")) sex = "F";
				MingleUser new_user = new MingleUser(chat_user_uid, "", 0, 1, (Drawable) this.getResources().getDrawable(R.drawable.ic_launcher),sex);
				
				curr_user.addMingleUser(new_user);
				curr_user.addChoice(chat_user_uid);
				
				new ImageDownloader(this.getApplicationContext(), new_user.getUid(), 0);
				
				//download profile also
			} else {
				int candidate_pos = curr_user.getCandidatePos(chat_user_uid);
				if(candidate_pos >= 0){
					curr_user.switchCandidateToChoice(candidate_pos);
				} else {
					int choice_pos = curr_user.getChoicePos(chat_user_uid);
					if(choice_pos < 0) curr_user.addChoice(chat_user_uid);
				}
			}
			
			String msg = recv_msg_obj.getString("msg");
			String msg_ts = recv_msg_obj.getString("ts");
    		curr_user.getMingleUser(chat_user_uid).recvMsg(msg, msg_ts);
    		// Save to local storage
			//((MingleApplication)currContext.getApplicationContext()).dbHelper.insertMessages(chat_user_uid, chat_user_uid, msg, msg_ts);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		updateMessageList();	
    }
    
    @Override
    public void onDestroy(){    	
    	LocalBroadcastManager.getInstance(this).unregisterReceiver(msgConfReceiver);
    	LocalBroadcastManager.getInstance(this).unregisterReceiver(getMsgReceiver);

    	super.onDestroy();
    }
}
