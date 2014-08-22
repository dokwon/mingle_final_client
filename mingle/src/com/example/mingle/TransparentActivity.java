package com.example.mingle;

import java.lang.ref.WeakReference;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class TransparentActivity extends Activity {

	public final static String DATA_BUNDLE = "com.example.mingle.DATA_BUNDLE";
	
	static final int TIME_OUT = 2500;
    static final int MSG_DISMISS_DIALOG = 0;
	private AlertDialog popupDialog;
	private TimeoutHandler mHandler;
	private Context context;
	private MingleApplication app;
	
	static class TimeoutHandler extends Handler {
		WeakReference<AlertDialog> dialog;
		WeakReference<Activity> currActivity;
		
		public TimeoutHandler(AlertDialog dialog, Activity activity){
			this.dialog = new WeakReference<AlertDialog>(dialog);
			this.currActivity = new WeakReference<Activity>(activity);
		}
		
		@Override
		public void handleMessage(android.os.Message msg){
			AlertDialog popupDialog = dialog.get();
			Activity activity = currActivity.get();
			if(msg.what == MSG_DISMISS_DIALOG){
				if(popupDialog != null && popupDialog.isShowing()){
					popupDialog.dismiss();
					activity.finish();
					activity.overridePendingTransition(0, 0);
				}
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transparent);
		context = this;
		app = (MingleApplication)(this.getApplication());
	}
	
	@Override
	protected void onResume(){
		super.onResume();

		Bundle data = getIntent().getBundleExtra(GcmIntentService.DATA_BUNDLE);
		final String uid = data.getString("send_uid");
		MingleUser send_user = app.getMingleUser(uid);
		
		View layout = getLayoutInflater().inflate(R.layout.dialog, null);
		
		RoundedImageView user_pic=(RoundedImageView)layout.findViewById(R.id.noti_sender_image);
		user_pic.setImageDrawable(send_user.getPic(-1));
		TextView user_name = (TextView)layout.findViewById(R.id.noti_sender_name);
		user_name.setText(send_user.getName());
        user_name.setTypeface(app.koreanTypeFace);
        TextView msg_view = (TextView)layout.findViewById(R.id.noti_msg);
    	msg_view.setText(data.getString("msg"));
    	
    	layout.setOnClickListener(new View.OnClickListener() {
    		@Override
    		public void onClick(View view){
    			Intent chat_intent = new Intent(context, ChatroomActivity.class);
				chat_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				chat_intent.putExtra(ChatroomActivity.USER_UID, uid);
				startActivity(chat_intent);
				popupDialog.dismiss();
				finish();
				overridePendingTransition(0, 0);
    		}
    	});
    	
		AlertDialog.Builder popupBuilder = new AlertDialog.Builder(this)
												.setCancelable(true)
												.setOnCancelListener(new DialogInterface.OnCancelListener() {
													@Override
													public void onCancel(DialogInterface dialog) {
														finish();
														overridePendingTransition(0, 0);
													}
												})
												.setView(layout);
										
		popupDialog = popupBuilder.create();
		popupDialog.setCanceledOnTouchOutside(true);
		
	    Window window = popupDialog.getWindow();
		WindowManager.LayoutParams wlp = window.getAttributes();
		wlp.gravity = Gravity.TOP;
		wlp.y += 300;
		window.setAttributes(wlp);

        popupDialog.show();

		mHandler = new TimeoutHandler(popupDialog, this);
		mHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, TIME_OUT);
	}
	
	@Override
	public void onDestroy(){
		mHandler.removeMessages(MSG_DISMISS_DIALOG);
		if(popupDialog != null && popupDialog.isShowing())
			popupDialog.dismiss();

		super.onDestroy();
	}
}
								
