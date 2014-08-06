package com.example.mingle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.example.mingle.MingleApplication;

import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
public class HuntActivity extends FragmentActivity implements ActionBar.TabListener	 {

	 public CandidateFragment candidateFragment;		//Fragment for list of chattable users
	 public ChoiceFragment choiceFragment;				//Fragment for list of users whom current user is chatting with
	 public VoteFragment voteFragment;					//Fragment for list of top male and female users
	 public SettingFragment settingFragment;					//Fragment for list of top male and female users

	 MingleApplication app;
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

		private boolean AppOnFirstTime() {
	    	DatabaseHelper db = ((MingleApplication) this.getApplication()).dbHelper;
	    	MingleApplication mingleApp = (MingleApplication) this.getApplication();
	    	if(db.isFirst()) {

	    		System.out.println("Saving for the first time!!");
	    		return true;
	    	}
	    	
	    	mingleApp.createMyUser(mingleApp.dbHelper.getUserData());
	    	ArrayList<ContentValues> chatters = mingleApp.dbHelper.getUserList();
	    	System.out.println(chatters.size() + " is the size of chatters");
	    	for(int i=0; i<chatters.size(); i++){
	    		System.out.println(chatters.get(i));
	    		ArrayList<Message> tempmsgs = mingleApp.dbHelper.getMsgList(chatters.get(i).getAsString("UID"));
	    		String sex_var = "M";
	    		if(((MingleApplication) this.getApplicationContext()).getMyUser().getSex() == "M") sex_var = "F";
	    		
	    		MingleUser newUser = new MingleUser(chatters.get(i).getAsString("UID"),
	    				chatters.get(i).getAsString("COMM"),
	    				(int) chatters.get(i).getAsInteger("NUM"),
	    				1,
	    				mingleApp.getResources().getDrawable(R.drawable.ic_launcher),
	    				sex_var);
	    		if(mingleApp.getChoicePos(newUser.getUid())==-1) {
	    			mingleApp.addMingleUser(newUser);
	    		
	    			mingleApp.addChoice(newUser.getUid());
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunt);
        
        app = ((MingleApplication) this.getApplication());
        MingleUser my_user = app.getMyUser();
        if(my_user == null) app.createDefaultMyUser();
        
        //Initialize HttpHelper that supports HTTP GET/POST requests and socket connection
        app.connectHelper = new HttpHelper(server_url, (MingleApplication)this.getApplication());

        // Initialize the database helper that manages local storage
        app.dbHelper = new DatabaseHelper(this);
     
        app.socketHelper = new Socket(server_url, app);

        // Set up the action bar to show tabs.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // for each of the sections in the app, add a tab to the action bar.
        actionBar.addTab(actionBar.newTab().setText(R.string.tab1title)
            .setTabListener(this).setTag(R.string.tab1title));
        actionBar.addTab(actionBar.newTab().setText(R.string.tab2title)
            .setTabListener(this).setTag(R.string.tab2title));
        actionBar.addTab(actionBar.newTab().setText(R.string.tab3title)
                .setTabListener(this).setTag(R.string.tab3title));
        actionBar.addTab(actionBar.newTab().setText(R.string.tab4title)
                .setTabListener(this).setTag(R.string.tab4title));
        
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        LocalBroadcastManager.getInstance(this).registerReceiver(userListReceiver,
      		  new IntentFilter(HttpHelper.HANDLE_CANDIDATE));
        
        LocalBroadcastManager.getInstance(this).registerReceiver(popListReceiver,
        		  new IntentFilter(HttpHelper.HANDLE_POP));

        ((MingleApplication) this.getApplication()).socketHelper.connectSocket();
        
        //GCM Setup here
        context = (Context)this;
        // Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerForNewUser();
            }
            
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
        
		app.setRid(regid);
        
        // If the app is not on for the first time, start HuntActivity
        // and populate it with data from local storage

        if(AppOnFirstTime()) {
        	//Start activity for new user
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra(MainActivity.MAIN_TYPE, "new");
            startActivity(i);
        }
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
	      /*switch (item.getItemId()) {
	      	case R.id.personal_settings:
	      		Intent intent = new Intent(this, SettingActivity.class);
	      	    startActivity(intent);
	            return true;
	          default:*/
	              return super.onOptionsItemSelected(item);
	      //}
	  }
	 
	  @Override
	  public void onTabSelected(ActionBar.Tab tab,
	      FragmentTransaction fragmentTransaction) {
		  System.out.println("ontabsel called");
		  // When the given tab is selected, show the tab contents in the
		  // container view.
		  if(tab.getTag().equals(R.string.tab1title)) {
			  if(voteFragment == null) voteFragment = new VoteFragment();
			  
			  getFragmentManager().beginTransaction()
		        .replace(R.id.fragment_container, voteFragment).commit();
		  }	else if(tab.getTag().equals(R.string.tab2title)) {
			  if(candidateFragment == null) candidateFragment  = new CandidateFragment();
			 
			  getFragmentManager().beginTransaction()
			        .replace(R.id.fragment_container, candidateFragment).commit();
			  
		  } else if(tab.getTag().equals(R.string.tab3title)) {
			  if(choiceFragment == null) choiceFragment = new ChoiceFragment();
			  
			  getFragmentManager().beginTransaction()
		        .replace(R.id.fragment_container, choiceFragment).commit();
		  }	else if(tab.getTag().equals(R.string.tab4title)) {
			  if(settingFragment == null) settingFragment = new SettingFragment();
			  
			  getFragmentManager().beginTransaction()
		        .replace(R.id.fragment_container, settingFragment).commit();
		  }
	  }
	  
	  
	  @Override
	  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    
	   
	  }
	  
	   private BroadcastReceiver userListReceiver = new BroadcastReceiver() {
	    	@Override
	    	public void onReceive(Context context, Intent intent) {
	    	   // Extract data included in the Intent
	    	   String data = intent.getStringExtra(HttpHelper.USER_LIST);
	    	   Log.d("receiver", "Got message: " + data);
	    	   
	    	   try {
	    		   JSONArray user_list_arr = new JSONArray(data);
	    		   handleCandidateList(user_list_arr);
	    	   } catch (JSONException e) {
	    		   // TODO Auto-generated catch block
	    		   e.printStackTrace();
	    	   }
	    	   
	    	  }
	    };
	  
	    private BroadcastReceiver popListReceiver = new BroadcastReceiver() {
	    	@Override
	    	public void onReceive(Context context, Intent intent) {
	    	   // Extract data included in the Intent
	    	   String data = intent.getStringExtra(HttpHelper.POP_LIST);
	    	   Log.d("receiver", "Got message: " + data);
	    	   
	    	   try {
	    		   JSONArray pop_list_arr = new JSONArray(data);
	    		   handlePopList(pop_list_arr);
	    	   } catch (JSONException e) {
	    		   // TODO Auto-generated catch block
	    		   e.printStackTrace();
	    	   }
	    	   
	    	  }
	    };

	  
	  class CandidateTimerTask extends TimerTask {
		  MingleApplication ma;
		  public CandidateTimerTask(MingleApplication ma){
			  this.ma = ma;
		  }
		  @Override
		  public void run() {
			  runOnUiThread(new Runnable(){
				  @Override
				  public void run() {
					  ma.moreCandidate();
				  }
			  });
		  }
	  }
	    
	  public void handleCandidateList(JSONArray list_of_users){
		  System.out.println(list_of_users.toString());
		MingleApplication app = ((MingleApplication) this.getApplicationContext());
		if(list_of_users.length() == 0){
			app.noMoreCandidate();
			System.out.println("app candidate bool: " +app.canGetMoreCandidate());
			CandidateTimerTask ctt = new CandidateTimerTask(app);
			new Timer().schedule(ctt, 30000);
		} else {
		  
			//update ChattableUser list and dispatch image downloader
			for(int i = 0 ; i < list_of_users.length(); i++) {
				try {
					JSONObject shownUser = list_of_users.getJSONObject(i);
					MingleUser candidate = app.getMingleUser(shownUser.getString("UID"));
					if(candidate == null){
						String sex_var = "M";
						if(app.getMyUser().getSex().equals("M")) sex_var = "F";
						candidate = new MingleUser(shownUser.getString("UID"), shownUser.getString("COMM"), Integer.valueOf(shownUser.getString("NUM")), Integer.valueOf(shownUser.getString("PHOTO_NUM")), (Drawable) this.getResources().getDrawable(R.drawable.ic_launcher),sex_var);

						app.addMingleUser(candidate);
					}
					if(!candidate.isPicAvail(0)) new ImageDownloader(this.getApplicationContext(), candidate.getUid(), 0).execute();
					
					app.addCandidate(shownUser.getString("UID"));
				} catch (JSONException e){
					e.printStackTrace();
				}
			}
			candidateListUpdate();
		}
		if(candidateFragment != null) candidateFragment.candidateLoadMoreComplete();
	  }
	  
	  public void handlePopList(JSONArray list_of_top){
		MingleApplication app = ((MingleApplication) this.getApplicationContext());
		    
		//update MingleUser list and dispatch image downloader
	    ArrayList<String> female_list = new ArrayList<String>();
	    ArrayList<String> male_list = new ArrayList<String>();
	    
	    for(int i = 0 ; i < list_of_top.length(); i++) {
	    	try {
	    			JSONObject shownUser = list_of_top.getJSONObject(i);
	    		MingleUser pop_user = app.getMingleUser(shownUser.getString("UID"));
	    		if(pop_user == null){
	    			MingleUser new_user = new MingleUser(shownUser.getString("UID"), shownUser.getString("COMM"), Integer.valueOf(shownUser.getString("NUM")), Integer.valueOf(shownUser.getString("PHOTO_NUM")), (Drawable) this.getResources().getDrawable(R.drawable.ic_launcher),shownUser.getString("SEX"));
	    			app.addMingleUser(new_user);
	    			new ImageDownloader(this.getApplicationContext(), new_user.getUid(), -1).execute();
	    		}
	    	
	    	   	if(shownUser.getString("SEX").equals("F")) female_list.add(shownUser.getString("UID"));
	    	   	else male_list.add(shownUser.getString("UID"));
	    	    
	    	} catch (JSONException e){
	    		e.printStackTrace();
	    	}
	    }
	    		
	    //add female and male pop users to list
	    app.emptyPopList();
	    for(int i = 0; i < female_list.size() || i < male_list.size(); i++){
	    	String female_uid = "";
	    	String male_uid = "";
	    	if(i < female_list.size()){
	    		female_uid = female_list.get(i);
	    	}
	    	if(i < male_list.size()){
	    		male_uid = male_list.get(i);
	    	}
	    	app.addPopUsers(female_uid, male_uid);
	    }
	    	
	    popListUpdate();
	  }
	  
	  public void allListsUpdate(){
		  candidateListUpdate();
		  choiceListUpdate();
		  popListUpdate();
	  }
	  
	  public void candidateListUpdate(){
		  if(candidateFragment != null) candidateFragment.listDataChanged();
	  }
	  
	  public void choiceListUpdate(){
		  if(choiceFragment != null) choiceFragment.listDataChanged();
	  }
	  
	  public void popListUpdate(){
		  if(voteFragment != null) voteFragment.listDataChanged();
	  }
	  
	  @Override
	  public void onRestart(){
	        super.onRestart();
	        candidateListUpdate();
	        choiceListUpdate();
	  }
	 
	  @Override
	  public void onDestroy(){
		  LocalBroadcastManager.getInstance(this).unregisterReceiver(userListReceiver);
		  LocalBroadcastManager.getInstance(this).unregisterReceiver(popListReceiver);  

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
		  new AsyncTask<Void, Void, String>() {	  
			  @Override
			  protected String doInBackground(Void... params) {
				  String msg;
				  try {
					  if (gcm == null) {
						  gcm = GoogleCloudMessaging.getInstance(context);
					  }
			  
					  regid = gcm.register(SENDER_ID);
					  // Persist the regID - no need to register again.
					  storeRegistrationId(context, regid);
				  } catch (IOException ex) {
					  ex.printStackTrace();
					  // If there is an error, don't just keep trying to register.
					  // Require the user to click a button again, or perform
					  // exponential back-off.
				  }
				  return "Registration done";
			  }
			  
			  @Override
			  protected void onPostExecute(String msg) {
				  System.out.println(msg);
			  }
		  }.execute();
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

