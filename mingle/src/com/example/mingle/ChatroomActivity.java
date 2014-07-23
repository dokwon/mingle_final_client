package com.example.mingle;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateFormat;
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

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    MsgAdapter adapter;
    ListView msg_lv;
    
    String send_uid;
    String recv_uid;
    int msg_counter;
    ChattableUser recv_user;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
     // Change the current activity to ChatroomActivity in HttpHelper
        ((MingleApplication) this.getApplication()).connectHelper.changeContext(this);
        
        
        Intent intent = getIntent();
        recv_uid = intent.getExtras().getString(CandidateFragment.USER_UID);
        
        //Set basic information required for this chat room
        send_uid = ((MingleApplication) this.getApplication()).currUser.getUid();
		//for testing purpose, set myself as receiver
		//recv_uid = send_uid;
        
        recv_user = ((MingleApplication) this.getApplication()).currUser.getChattingUser(recv_uid);
		msg_counter = -1;
		
		//Associate this chat room's message list to adapter
		adapter=new MsgAdapter(this,
                R.layout.msg_row, R.layout.my_msg_row,
                recv_user.getChatRoom().getMsgList(), this);
		
        setListAdapter(adapter);
        
        msg_lv = (ListView) findViewById(android.R.id.list);
         		
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

		ArrayList<Message> list = recv_user.getChatRoom().getMsgList();

		String last_msg_uid;
		if(list.size() <= 0) last_msg_uid = "";
		else last_msg_uid = list.get(list.size()-1).getUid();

		if(last_msg_uid.equals(((MingleApplication) this.getApplication()).currUser.getUid())){
			response_msg = false;
			DatabaseHelper db = ((MingleApplication) this.getApplication()).dbHelper;
			// Stores messages in DB
			db.insertMessages(recv_uid, send_uid,SMS , new Timestamp(System.currentTimeMillis()).toString());
		}
			
		//Save MSG and show it is in the process of getting sent.
		recv_user.addMsgToChatRoom(SMS, msg_counter, 0);
		updateMessageList();
		
		//Send MSG to Server
		((MingleApplication) this.getApplication()).connectHelper.sendMessageToServer(send_uid, recv_uid, SMS, msg_counter,response_msg);
		
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
}
