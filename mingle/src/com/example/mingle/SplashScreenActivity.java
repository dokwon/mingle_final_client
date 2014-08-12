package com.example.mingle;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

public class SplashScreenActivity extends Activity {
	private MingleApplication app;
	private static final String server_url = "http://ec2-54-178-214-176.ap-northeast-1.compute.amazonaws.com:8080";
	private boolean all_set = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash_screen);
				
		app = ((MingleApplication) this.getApplication());
		app.koreanTypeFace = Typeface.createFromAsset(getAssets(), "fonts/UnGraphic.ttf");
		
        //Initialize HttpHelper that supports HTTP GET/POST requests and socket connection
        app.connectHelper = new HttpHelper(server_url, (MingleApplication)this.getApplication());
        
        // Initialize the database helper that manages local storage
	    app.dbHelper = DatabaseHelper.getInstance(this, app);
	    
        app.socketHelper = new Socket(server_url, app);
        
        LocalBroadcastManager.getInstance(this).registerReceiver(initInfoReceiver,
      		  new IntentFilter(HttpHelper.SET_INIT_INFO));
        
        LocalBroadcastManager.getInstance(this).registerReceiver(httpErrorReceiver,
        		  new IntentFilter(HttpHelper.HANDLE_HTTP_ERROR));
	}
	
	@Override
	protected void onResume(){
		super.onResume();
        final Context context = this;
        if(!app.isLocationEnabled()) {
        	AlertDialog.Builder popupBuilder = new AlertDialog.Builder(this)
													.setTitle("Mingle")
													.setMessage("GPS is not enabled. Do you want to go to settings menu?.")
													.setIcon(R.drawable.mingle_logo)
													.setPositiveButton("OK", new DialogInterface.OnClickListener() {
														@Override
														public void onClick(DialogInterface dialog, int id) {
															dialog.dismiss();
															Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
															startActivity(intent);
														}
													})
													.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
														@Override
														public void onClick(DialogInterface dialog, int id) {
															dialog.dismiss();
															finish();
														}
													});
        	AlertDialog popupDialog = popupBuilder.create();
        	popupDialog.show();
        } else {
        	Thread background = new Thread() {
        		public void run() {
        			try {
        				// Thread will sleep for 2 seconds
        				sleep(2000);

        				//Check whether location search is on and get location
        				app.getCurrentLocation();
                    
        				//Create default MyUser object. Will be modified later.
        				app.createDefaultMyUser();
            	    
        				// If the app is not on for the first time, start HuntActivity
        				// and populate it with data from local storage
        				if(AppOnFirstTime() && app.isLocationEnabled()) {
        					app.connectHelper.getInitInfo();         	         
        				} else if(app.isLocationEnabled()){
        					Intent i = new Intent(context, HuntActivity.class);
            	    		startActivity(i);
            	    		finish();
        				} else
            	    		finish();
        			} catch (Exception e) {
        				e.printStackTrace();
        			}
        		}
        	};
            background.start();
        }
	}
	
	private BroadcastReceiver initInfoReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		String data = intent.getStringExtra(HttpHelper.INIT_INFO);
    		try {
    			JSONObject init_info_obj = new JSONObject(data);
    			String update_required = init_info_obj.getString("UPDATE_REQUIRED");
    			if(update_required.equals("true")){
    				Toast.makeText(getApplicationContext(), "Update REQUIRED!", Toast.LENGTH_SHORT).show();
    			} else {
    				app.setQuestion(init_info_obj.getString("QUESTION"));
    				Intent i = new Intent(context, MainActivity.class);
       	         	i.putExtra(MainActivity.MAIN_TYPE, "new");  
       	         	startActivity(i);
    			}
   	         	finish();
	    	} catch (JSONException e) {
	    		e.printStackTrace();
	    	}
    	}
    };
    
    /* Broadcast Receiver for notification of http error*/
	  private BroadcastReceiver httpErrorReceiver = new BroadcastReceiver() {
	    	@Override
	    	public void onReceive(Context context, Intent intent) {
	    		Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
	    		finish();
	    	}
	  };
	

	//Check if current user is using the application for the first time, and if not bring data from local DB
	private boolean AppOnFirstTime() {
	   	DatabaseHelper db = app.dbHelper;
	   	if(db.isFirst()) {
	   		System.out.println("Saving for the first time!!");
	   		return true;
	   	}
	   	
	   	JSONObject user_data = app.dbHelper.getUserData();
	   	
	   	try {
	   		app.setMyUser(user_data.getString("UID"), user_data.getString("COMM"), 
										user_data.getInt("NUM"), user_data.getString("SEX"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	   	
	   	ArrayList<ContentValues> chatters = app.dbHelper.getUserList();
	   	for(int i=0; i<chatters.size(); i++){
	   		ArrayList<Message> tempmsgs = app.dbHelper.getMsgList(chatters.get(i).getAsString("UID"));
	   		String sex_var = "M";
	   		if(((MingleApplication) this.getApplicationContext()).getMyUser().getSex().equals("M")) sex_var = "F";
	   		
	   		MingleUser newUser = new MingleUser(chatters.get(i).getAsString("UID"),
	   				chatters.get(i).getAsString("COMM"),
	   				(int) chatters.get(i).getAsInteger("NUM"),
	   				1,
	   				app.getResources().getDrawable(R.drawable.ic_launcher),
	   				sex_var);
	   		if(app.getChoicePos(newUser.getUid())==-1) {
	   			app.addMingleUser(newUser);
	   		
	   			app.addChoice(newUser.getUid());
	   			new ImageDownloader(this.getApplicationContext(), newUser.getUid(), 0).execute();
	    		for(int j =0; j<tempmsgs.size(); j++){
	    			newUser.addMsgObj(tempmsgs.get(j));
	    		}
	    	}
	   	}	   	
	   	// Populate other fields with UID
	   	return false;
	}
	
	 @Override
	  public void onDestroy(){
		  LocalBroadcastManager.getInstance(this).unregisterReceiver(initInfoReceiver);

		  super.onDestroy();
	  }
}
