package com.example.mingle;


import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

public class SplashScreenActivity extends Activity {
	private MingleApplication app;
	private static final String server_url = "http://ec2-54-178-214-176.ap-northeast-1.compute.amazonaws.com:8080";
	
	//For GCM below
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private String SENDER_ID = "5292889580";
	private GoogleCloudMessaging gcm;
	private String regid;
	static final String TAG = "GCMDemo";
	
	private Context context;
	private LocationManager locationManager;
	private boolean findCurLoc = false;
	static final int MSG_TIME_OUT = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
		
        app = ((MingleApplication) this.getApplication());
        app.initializeApplication();
		
        //Initialize HttpHelper that supports HTTP GET/POST requests
        app.connectHelper = new HttpHelper(server_url, (MingleApplication)this.getApplication());
        
        // Initialize the database helper that manages local storage
	    app.dbHelper = DatabaseHelper.getInstance(this, app);
	    
        // Initialize the socket helper that manages socket connection with the server
        app.socketHelper = new Socket(server_url, app);
        
        LocalBroadcastManager.getInstance(this).registerReceiver(initInfoReceiver,
      		  new IntentFilter(HttpHelper.SET_INIT_INFO));
        
        LocalBroadcastManager.getInstance(this).registerReceiver(httpErrorReceiver,
        		  new IntentFilter(HttpHelper.HANDLE_HTTP_ERROR));
        
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        
        //GCM Setup here
        context = (Context)this;
        
        // Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) registerForNewUser();
            else app.setRid(regid);
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
        
        //Now move on to next phase
		//Create default MyUser object. Will be modified later.
		app.createDefaultMyUser();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		
		if(AppOnFirstTime()) {
			if(!app.isLocationEnabled()) {
				AlertDialog.Builder popupBuilder = new AlertDialog.Builder(this)
														.setTitle(getResources().getString(R.string.gps_location_setting))
														.setMessage(getResources().getString(R.string.gps_disabled_alert))
														.setIcon(R.drawable.icon_tiny)
														.setPositiveButton(getResources().getString(R.string.allow), new DialogInterface.OnClickListener() {
															@Override
															public void onClick(DialogInterface dialog, int id) {
																dialog.dismiss();
																Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
																startActivity(intent);
															}
														})
														.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
															@Override
															public void onClick(DialogInterface dialog, int id) {
																dialog.dismiss();
																finish();
															}
														});
		       	AlertDialog popupDialog = popupBuilder.create();
		       	popupDialog.show();
		    } else {
		       	getCurrentLocation();
		    }

		} else {
			Thread launcherThread = new Thread() {
				@Override
				public void run() {
					try {
						sleep(3*1000);
						Intent i = new Intent(context, HuntActivity.class);
						i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
						startActivity(i);
						finish();
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			launcherThread.start();
		} 
	}
	
	// Get the users one-time location. Code available below to register for updates
    private void getCurrentLocation() {
    	Criteria criteria = new Criteria();
    	String provider = locationManager.getBestProvider(criteria, true);
        locationManager.requestLocationUpdates(provider, 1000, 0, locationListener);
        initHandler.sendEmptyMessageDelayed(MSG_TIME_OUT, 3*1000);
    }
    
	private LocationListener locationListener = new LocationListener() {
	    public void onLocationChanged(Location location) {
	        app.setLat((float)location.getLatitude());
	        app.setLong((float)location.getLongitude());
	        Log.i("loc", Float.toString(app.getLat()) + " " + Float.toString(app.getLong()));
	        findCurLoc = true;
	    }		
	    public void onStatusChanged(String provider, int status, Bundle extras) {}
	    public void onProviderEnabled(String provider) {}
	    public void onProviderDisabled(String provider) {}
	};
	
	private Handler initHandler = new Handler() {
    	@Override
    	public void handleMessage(android.os.Message msg) {
    		switch(msg.what) {		
    			//After 3seconds, check whether the device finds current location.
    			case MSG_TIME_OUT:
    				locationManager.removeUpdates(locationListener);

    				//If not check if whether previous data exists
    				if(!findCurLoc) {
    					Log.i("loc", "use prev loc");
    					Criteria criteria = new Criteria();
    			    	String provider = locationManager.getBestProvider(criteria, true);
    			    	Location location = locationManager.getLastKnownLocation(provider);
    			    	//If so, use previous data.
    			    	if(location != null) {
    			    		app.setLat((float)location.getLatitude());
    			    		app.setLong((float)location.getLongitude());
    			    		
    			    	//If no, kill the app for now. should be modified.
    			    	} else
    			    		((Activity)context).finish();
    				}                	     
    				app.connectHelper.getInitInfo();
    				break;
    				
    			default:
    				break;
    		}
    	}
    };
    
	private BroadcastReceiver initInfoReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		String data = intent.getStringExtra(HttpHelper.INIT_INFO);
    		try {
    			JSONObject init_info_obj = new JSONObject(data);
    			String update_required = init_info_obj.getString("UPDATE_REQUIRED");
    			if(update_required.equals("true")){
    				Toast.makeText(getApplicationContext(), getResources().getString(R.string.application_update_required), Toast.LENGTH_SHORT).show();
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
	   		JSONArray pic_path_arr = new JSONArray(user_data.getString("PIC_PATH_ARR"));
	   		for(int i = 0; i < pic_path_arr.length(); i++){
	   			String pic_path = pic_path_arr.getString(i);
	   			app.setPhotoPath(i, pic_path);
	   		}
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
	   				app.getResources().getDrawable(app.blankProfileImage),
	   				sex_var, (int) chatters.get(i).getAsInteger("DIST"));
	   		if(app.getChoicePos(newUser.getUid())==-1) {
	   			app.addMingleUser(newUser);
	   			app.addChoice(newUser.getUid());
	    		for(int j =0; j<tempmsgs.size(); j++){
	    			newUser.addMsgObj(tempmsgs.get(j));
	    		}
	    	}
	   		if(!newUser.isPicAvail(-1)) new ImageDownloader(this.getApplicationContext(), newUser.getUid(), -1).execute();

	   	}	   	
	   	// Populate other fields with UID
	   	return false;
	}
	
	 @Override
	  public void onDestroy(){
		  LocalBroadcastManager.getInstance(this).unregisterReceiver(initInfoReceiver);

		  super.onDestroy();
	  }
	 
	//For GCM here
	  private boolean checkPlayServices() {
	      int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	      if (resultCode != ConnectionResult.SUCCESS) {
	          if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	              GooglePlayServicesUtil.getErrorDialog(resultCode, this,
	                      PLAY_SERVICES_RESOLUTION_REQUEST).show();
	          } else {
	              Log.i(TAG, "This device is not supported.");
	              finish();
	          }
	          return false;
	      }
	      return true;
	  }

	  
	  private String getRegistrationId(Context context) {
	      final SharedPreferences prefs = getGCMPreferences(context);
	      String registrationId = prefs.getString(PROPERTY_REG_ID, "");
	      if (registrationId.isEmpty()) {
	          Log.i(TAG, "Registration not found.");
	          return "";
	      }
	      // Check if app was updated; if so, it must clear the registration ID
	      // since the existing regID is not guaranteed to work with the new
	      // app version.
	      int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	      int currentVersion = getAppVersion(context);
	      if (registeredVersion != currentVersion) {
	          Log.i(TAG, "App version changed.");
	          return "";
	      }
	      return registrationId;
	  }
	  
	 
	  private SharedPreferences getGCMPreferences(Context context) {
	      // This sample app persists the registration ID in shared preferences, but
	      // how you store the regID in your app is up to you.
	      return getSharedPreferences(HuntActivity.class.getSimpleName(), Context.MODE_PRIVATE);
	  }

	  
	  private static int getAppVersion(Context context) {
	      try {
	          PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
	          return packageInfo.versionCode;
	      } catch (NameNotFoundException e) {
	          // should never happen
	          throw new RuntimeException("Could not get package name: " + e);
	      }
	  }
	  
	  private void registerForNewUser() {
		  new AsyncTask<MingleApplication, Void, String>() {	  
			  @Override
			  protected String doInBackground(MingleApplication... params) {
				  try {
					  if (gcm == null) {
						  gcm = GoogleCloudMessaging.getInstance(context);
					  }
			  
					  regid = gcm.register(SENDER_ID);
					  // Persist the regID - no need to register again.
					  storeRegistrationId(context, regid);
					  params[0].setRid(regid);
				  } catch (IOException ex) {
					  ex.printStackTrace();
					  // If there is an error, don't just keep trying to register.
					  // Require the user to click a button again, or perform
					  // exponential back-off.
					  return "";
				  }
				  return "Registration done";
			  }
			  
			  @Override
			  protected void onPostExecute(String msg) {
				  System.out.println(msg);
			  }
		  }.execute(((MingleApplication)this.getApplication()));
	  }

	  private void storeRegistrationId(Context context, String regId) {
	      final SharedPreferences prefs = getGCMPreferences(context);
	      int appVersion = getAppVersion(context);
	      Log.i(TAG, "Saving regId on app version " + appVersion);
	      SharedPreferences.Editor editor = prefs.edit();
	      editor.putString(PROPERTY_REG_ID, regId);
	      editor.putInt(PROPERTY_APP_VERSION, appVersion);
	      editor.commit();
	  }
}
