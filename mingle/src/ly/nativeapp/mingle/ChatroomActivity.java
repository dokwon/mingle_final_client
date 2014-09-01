package ly.nativeapp.mingle;

import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.AlertDialog;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.BufferType;


public class ChatroomActivity extends ListActivity implements ActionBar.TabListener{

	//LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    ArrayList<String> listItems=new ArrayList<String>();
    
    public final static String USER_UID = "ly.nativeapp.mingle.USER_SEL";	//Intent data to pass on when new Chatroom Activity started
    public final static String FROM_GCM = "ly.nativeapp.mingle.FROM_GCM";	//Intent data to pass on when Chatroom Activity is started by GCM notification

	Button btnSend;															//Button for sending a message
	EditText txtSMS;														//Field that holds message content    
	ListView msg_lv;														//Listview for messages
    MsgAdapter adapter;														//Listview adapter for msg_lv
    
    MingleUser recv_user;													//Mingle User currently chatting with
	
    @Override
    protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
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
        ((NotificationManager)this.getSystemService(NOTIFICATION_SERVICE)).cancel(GcmIntentService.NOTIFICATION_MSG_ID);
        GcmIntentService.resetNumMsgNotification();
        
        recv_user.setInChat(true);
        recv_user.zeroNewMsgNum();
		Intent dispatcher = new Intent(this, HuntActivity.class);
		dispatcher.setAction(HuntActivity.NEW_MESSAGE);
		LocalBroadcastManager.getInstance(this).sendBroadcast(dispatcher);
        
        ActionbarController.customizeActionBar(R.layout.chatactivity_title_custom_view, this, 0, 0);
	    TextView chatter_name = (TextView)findViewById(R.id.chatter_name_chat);
	    
	    chatter_name.setText(recv_user.getName());
	    ImageView member_num = (ImageView)findViewById(R.id.member_num_chat);
	    member_num.setImageResource(app.memberNumRsId(recv_user.getNum()));
 
		//Associate this chat room's message list to adapter
        msg_lv = (ListView) findViewById(android.R.id.list);
		adapter=new MsgAdapter(this,
                R.layout.msg_row, R.layout.my_msg_row,
                recv_user.getMsgList(), recv_user);
        setListAdapter(adapter);
        msg_lv.setSelection(msg_lv.getAdapter().getCount()-1);
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
        

        LocalBroadcastManager.getInstance(this).registerReceiver(noUserReceiver,
        		  new IntentFilter(Socket.NO_USER_NOTI));

        if(android.os.Build.VERSION.SDK_INT < 11) { 
		    requestWindowFeature(Window.FEATURE_NO_TITLE); 
		} 
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		setContentView(R.layout.activity_chatroom);
		KeyboardDismisser.setupKeyboardDismiss(findViewById(R.id.chatroom_parent), this);
		
    }
    
    /* Function to be called when message send button is clicked */
    public void sendSMS(View v){
    	
    	txtSMS=(EditText) findViewById(R.id.txt_inputText);
		String SMS=txtSMS.getText().toString();
		
		if(SMS.length() == 0) return;
    	
        MingleApplication app = ((MingleApplication) this.getApplication());
        
        //Open socket if closed
        app.socketHelper.connectSocket();

		recv_user.incrementMsgCounter();

		//Determine if my message is response
		boolean response_msg = true;
		if(!recv_user.isMsgListEmpty() && recv_user.getLastMsg().isMyMsg()){
			response_msg = false;
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
    		@Override
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
   		   updateMessageList();
    	}
    };
    
	 private BroadcastReceiver noUserReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    	   showNoUserPopup();
    	}
    };
    
    private void showNoUserPopup(){
    	AlertDialog.Builder popupBuilder = new AlertDialog.Builder(this)
												.setTitle(getResources().getString(R.string.message_error))
												.setCancelable(false)
												.setMessage(getResources().getString(R.string.user_no_exist_alert))
												.setIcon(R.drawable.icon_tiny)
												.setNeutralButton("OK", new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int id) {
														dialog.dismiss();
													}
												});
    	popupBuilder.show();
    	
    }

    @Override
    public void onBackPressed(){
    	Intent huntIntent = new Intent(this, HuntActivity.class);
    	huntIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	huntIntent.putExtra(HuntActivity.FROM_CHATROOM, true);
    	startActivity(huntIntent);
    	super.onBackPressed();
    }
    
    @Override
    public void onDestroy(){    	
    	LocalBroadcastManager.getInstance(this).unregisterReceiver(refListReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(noUserReceiver);
    	super.onDestroy();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            onBackPressed();
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
