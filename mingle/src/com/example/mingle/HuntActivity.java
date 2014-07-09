package com.example.mingle;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
public class HuntActivity extends FragmentActivity implements ActionBar.TabListener	 {
	
	/*
	//For GCM below
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private String SENDER_ID = "5292889580";
	
	private GoogleCloudMessaging gcm;
	private AtomicInteger msgId = new AtomicInteger();
	private SharedPreferences prefs;
	private Context context;
	private String regid;
	 
	static final String TAG = "GCMDemo";
	*/
	//Until here

	 public AllChatFragment allChatFragment;			//Fragment for list of chattable users
	 public OngoingChatFragment ongoingChatFragment;	//Fragment for list of users whom current user is chatting with
		
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunt);
        
        
        /*
        //GCM Setup here
        context = (Context)this;
        
        // Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }*/

        // Set up the action bar to show tabs.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // for each of the sections in the app, add a tab to the action bar.
        actionBar.addTab(actionBar.newTab().setText(R.string.tab1title)
            .setTabListener(this).setTag(R.string.tab1title));
        actionBar.addTab(actionBar.newTab().setText(R.string.tab2title)
            .setTabListener(this).setTag(R.string.tab2title));
        
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        System.out.println("hunt navigation set");
        // Change the current activity to HuntActivity in HttpHelper
        ((MingleApplication) this.getApplication()).connectHelper.changeContext(this);

        //Load first 10 chattable users
        allChatFragment.loadNewMatches(this);
        
        ((MingleApplication) this.getApplication()).connectHelper.connectSocket();
    }
	
	//?????
    public String getMyUid(){
    	return ((MingleApplication) this.getApplication()).currUser.getUid();
    }


	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub
		
	}
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	
	 @Override
	  public void onRestoreInstanceState(Bundle savedInstanceState) {
	    // Restore the previously serialized current tab position.
	    if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
	      getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
	    }
	  }

	  @Override
	  public void onSaveInstanceState(Bundle outState) {
	    // Serialize the current tab position.
	    outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
	        .getSelectedNavigationIndex());
	  }
	  
	  @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        // Inflate the menu; this adds items to the action bar if it is present.
	        super.onCreateOptionsMenu(menu);
	        getMenuInflater().inflate(R.menu.chat, menu);

	        return true;  
	    }
	  
	  @Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	      // Handle item selection
	      switch (item.getItemId()) {
	      	case R.id.personal_settings:
	      		Intent intent = new Intent(this, SettingActivity.class);
	      	    startActivity(intent);
	            return true;
	          default:
	              return super.onOptionsItemSelected(item);
	      }
	  }
	 
	  @Override
	  public void onTabSelected(ActionBar.Tab tab,
	      FragmentTransaction fragmentTransaction) {
		  System.out.println("ontabsel called");
		  // When the given tab is selected, show the tab contents in the
		  // container view.
		  if(tab.getTag().equals(R.string.tab1title)) {
			  if(allChatFragment == null) allChatFragment  = new AllChatFragment();
			 
			  System.out.println("chat fragment on view");
			  getFragmentManager().beginTransaction()
			        .replace(R.id.fragment_container, allChatFragment).commit();
			  
		  } else if(tab.getTag().equals(R.string.tab2title)) {
			  System.out.println("ongoing chat fragment on view");
			  if(ongoingChatFragment == null) ongoingChatFragment = new OngoingChatFragment();
			  
			  getFragmentManager().beginTransaction()
		        .replace(R.id.fragment_container, ongoingChatFragment).commit();
		  }	    
	  }
	  
	  //Update allChatFragment and ongoingChatFragment's lists
	  public void listsUpdate(){
		  if(allChatFragment != null) allChatFragment.listDataChanged();
		  if(ongoingChatFragment != null) ongoingChatFragment.listDataChanged();
	  }
	  
	  @Override
	  public void onRestart(){
	        super.onRestart();
	        // Change the current activity to HuntActivity in HttpHelper
	        System.out.println(this);
	        ((MingleApplication) this.getApplication()).connectHelper.changeContext(this);
	        listsUpdate();
	 }
	  
	  /*
	  
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
	      return getSharedPreferences(HuntActivity.class.getSimpleName(),
	              Context.MODE_PRIVATE);
	  }

	  
	  private static int getAppVersion(Context context) {
	      try {
	          PackageInfo packageInfo = context.getPackageManager()
	                  .getPackageInfo(context.getPackageName(), 0);
	          return packageInfo.versionCode;
	      } catch (NameNotFoundException e) {
	          // should never happen
	          throw new RuntimeException("Could not get package name: " + e);
	      }
	  }
	  
	  private void registerInBackground() {
	      new AsyncTask() {
	    	  @Override
	    	  protected String doInBackground() {
	              String msg = "";
	              try {
	                  if (gcm == null) {
	                      gcm = GoogleCloudMessaging.getInstance(context);
	                  }
	                  regid = gcm.register(SENDER_ID);
	                  msg = "Device registered, registration ID=" + regid;

	                  // You should send the registration ID to your server over HTTP,
	                  // so it can use GCM/HTTP or CCS to send messages to your app.
	                  // The request to your server should be authenticated if your app
	                  // is using accounts.
	                  sendRegistrationIdToBackend();

	                  // For this demo: we don't need to send it because the device
	                  // will send upstream messages to a server that echo back the
	                  // message using the 'from' address in the message.

	                  // Persist the regID - no need to register again.
	                  storeRegistrationId(context, regid);
	              } catch (IOException ex) {
	                  ex.printStackTrace();
	                  // If there is an error, don't just keep trying to register.
	                  // Require the user to click a button again, or perform
	                  // exponential back-off.
	              }
	              return msg;
	          }

	          protected void onPostExecute(String msg) {
	              System.out.println(msg);
	          }
	      }.execute(null, null, null);
	  }
	  
	  private void sendRegistrationIdToBackend() {
	      // Your implementation here.
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
*/
}


