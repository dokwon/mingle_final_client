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
import android.widget.TextView.BufferType;
import android.widget.Toast;

public class ChatroomActivity extends ListActivity {

	
	Button btnSend;
	EditText txtSMS;
	//LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    ArrayList<String> listItems=new ArrayList<String>();

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    MsgAdapter adapter;
    
    String send_uid;
    String recv_uid;
    int msg_counter;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
     // Change the current activity to ChatroomActivity in HttpHelper
        ((MingleApplication) this.getApplication()).connectHelper.changeContext(this);
        
        
        Intent intent = getIntent();
        recv_uid = intent.getExtras().getString(AllChatFragment.USER_UID);
        
        //Set basic information required for this chat room
        send_uid = ((MingleApplication) this.getApplication()).currUser.getUid();
		//for testing purpose, set myself as receiver
		//recv_uid = send_uid;
		msg_counter = -1;
		
		//Associate this chat room's message list to adapter
		adapter=new MsgAdapter(this,
                R.layout.msg_row,
                ((MingleApplication) this.getApplication()).currUser.getChatRoom(recv_uid).getMsgList(), this);
		
        setListAdapter(adapter);
         		
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

		ArrayList<Message> list = ((MingleApplication) this.getApplication()).currUser.getChatRoom(recv_uid).getMsgList();

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
		((MingleApplication) this.getApplication()).currUser.addMsgToRoom(recv_uid, send_uid, (Drawable) getResources().getDrawable(R.drawable.ic_launcher), SMS, msg_counter, 0);
		adapter.notifyDataSetChanged();
		
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
    }

    public void recvMessage(JSONObject recv_msg_obj){
    	try {
			String msg_send_uid = recv_msg_obj.getString("send_uid");
			String msg = recv_msg_obj.getString("msg");
			String msg_ts = recv_msg_obj.getString("ts");

			((MingleApplication) this.getApplication()).currUser.addRecvMsgToRoom(msg_send_uid, (Drawable) getResources().getDrawable(R.drawable.ic_launcher), msg, msg_ts);
			// Save to local storage
			((MingleApplication) this.getApplication()).dbHelper.insertMessages(send_uid, send_uid, msg, msg_ts);
			adapter.notifyDataSetChanged();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
