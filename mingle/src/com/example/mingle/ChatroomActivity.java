package com.example.mingle;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

public class ChatroomActivity extends ListActivity implements ActionBar.TabListener{

	
	Button btnSend;
	EditText txtSMS;
	//LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    ArrayList<String> listItems=new ArrayList<String>();
    
    public final static String USER_UID = "com.example.mingle.USER_SEL";	//Intent data to pass on when new Chatroom Activity started
    public final static String FROM_GCM = "com.example.mingle.FROM_GCM";	//Intent data to pass on when Chatroom Activity is started by GCM notification

    
    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    MsgAdapter adapter;
    ListView msg_lv;
    
    String send_uid;
    String recv_uid;
    int msg_counter;
    MingleUser recv_user;
	
    @Override
    protected void onNewIntent(Intent intent){
    	Log.i("sktag", "catch intent -> " + intent.getExtras().getString("USER_ID"));
    	setIntent(intent);
    }
    
    @Override
    protected void onResume(){
        super.onResume();
        
        //Clear the notification from gcm.
        ((NotificationManager)this.getSystemService(NOTIFICATION_SERVICE)).cancelAll();
        
        MingleApplication app = ((MingleApplication) this.getApplication());
        
        Intent intent = getIntent();
        recv_uid = intent.getExtras().getString(USER_UID);
        
        //Set basic information required for this chat room
        send_uid = app.getMyUser().getUid();
		//for testing purpose, set myself as receiver
		//recv_uid = send_uid;
        
        recv_user = app.getMingleUser(recv_uid);
        recv_user.setInChat(true);
        
        
        ActionBar actionBar = getActionBar();
	    //actionBar.hide();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM  | ActionBar.DISPLAY_SHOW_HOME);
        //actionBar.setH
        //actionBar.setHomeAsUpIndicator(R.drawable.back_button);
        View titleView =  LayoutInflater.from(this).inflate(R.layout.chatactivity_title_custom_view, null);
        LayoutParams layout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        titleView.setLayoutParams(layout);
        actionBar.setCustomView(titleView);
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    TextView chatter_name = (TextView)findViewById(R.id.chatter_name_chat);
	    
	    chatter_name.setText(recv_user.getName());
	    ImageView member_num = (ImageView)findViewById(R.id.member_num_chat);
	    member_num.setImageResource(memberNumRsId(recv_user.getNum()));
	    actionBar.setBackgroundDrawable(new ColorDrawable(0xFFFFFFFF));
	    
	   
		
        //Should be fixed here!!!
        msg_counter = -1;
		
		//Associate this chat room's message list to adapter
		adapter=new MsgAdapter(this,
                R.layout.msg_row, R.layout.my_msg_row,
                recv_user.getMsgList(), recv_user);
		
        setListAdapter(adapter);
        
        msg_lv = (ListView) findViewById(android.R.id.list);
    }
    
    @Override
    protected void onPause() {
    	recv_user.setInChat(false);
    	super.onPause();
    }
    
    
   private int memberNumRsId(int numOfMembers) {
    	
    	int rval = -1; 
    	switch(numOfMembers) {
	    	case 2:
	    		rval = R.drawable.membercount2;
	    		break;
	    	case 3: 
	    		rval = R.drawable.membercount3;
	    		break;
	    	case 4: 
	    		rval = R.drawable.membercount4;
	    		break;
	    	case 5:
	    		rval = R.drawable.membercount5;
	    		break;
	    	case 6:
	    		rval = R.drawable.membercount6;
	    		break;
    		
    	}
    	return rval; 
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     
        LocalBroadcastManager.getInstance(this).registerReceiver(refListReceiver,
        		  new IntentFilter(MingleApplication.UPDATE_MSG_LIST));
        if(android.os.Build.VERSION.SDK_INT < 11) { 
		    requestWindowFeature(Window.FEATURE_NO_TITLE); 
		} 
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		setContentView(R.layout.activity_chatroom);
		
		
    }
    
    public void sendSMS(View v){
        MingleApplication app = ((MingleApplication) this.getApplication());
        
        app.socketHelper.connectSocket();

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
			db.insertMessages(recv_uid, false, SMS , new Timestamp(System.currentTimeMillis()).toString());
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
    
    private BroadcastReceiver refListReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    	   // Extract data included in the Intent
    	   Log.d("chat receiver", "Got intent: ");
   		   updateMessageList();
    	}
    };
    
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
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    
    
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}
}
