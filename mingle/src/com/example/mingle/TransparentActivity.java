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
import android.view.Menu;
import android.view.MenuItem;

public class TransparentActivity extends Activity {

	public final static String DATA_BUNDLE = "com.example.mingle.DATA_BUNDLE";
	
	static final int TIME_OUT = 3000;
    static final int MSG_DISMISS_DIALOG = 0;
	private AlertDialog popupDialog;
	private TimeoutHandler mHandler;
	private Context context;
	
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
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		Bundle data = getIntent().getBundleExtra(GcmIntentService.DATA_BUNDLE);
		final String uid = data.getString("send_uid");
		AlertDialog.Builder popupBuilder = new AlertDialog.Builder(this)
												.setTitle("Mingle")
												.setCancelable(false)
												.setMessage(data.getString("msg"))
												.setIcon(R.drawable.mingle_logo)
												.setPositiveButton("View", new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int id) {
														Intent chat_intent = new Intent(context, ChatroomActivity.class);
														chat_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
														chat_intent.putExtra(ChatroomActivity.USER_UID, uid);
														startActivity(chat_intent);
														dialog.dismiss();
														finish();
														overridePendingTransition(0, 0);
													}
												})
												.setNegativeButton("Close", new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int id) {
														dialog.dismiss();
														finish();
														overridePendingTransition(0, 0);
													}
												});
		
		popupDialog = popupBuilder.create();
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
