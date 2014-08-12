package com.example.mingle;

import java.lang.ref.WeakReference;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;

public class TransparentActivity extends Activity {

	public final static String DATA_BUNDLE = "com.example.mingle.DATA_BUNDLE";
	
	static final int TIME_OUT = 3000;
    static final int MSG_DISMISS_DIALOG = 0;
	private AlertDialog popupDialog;
	private TimeoutHandler mHandler;
	
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
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		Bundle data = getIntent().getBundleExtra(GcmIntentService.DATA_BUNDLE);
		
		AlertDialog.Builder popupBuilder = new AlertDialog.Builder(this)
												.setTitle("Mingle")
												.setCancelable(false)
												.setMessage(data.getString("msg"))
												.setIcon(R.drawable.ic_launcher)
												.setNeutralButton("OK", new DialogInterface.OnClickListener() {
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
