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
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
public class HuntActivity extends FragmentActivity implements ActionBar.TabListener	 {

	 public CandidateFragment candidateFragment;		//Fragment for list of chattable users
	 public ChoiceFragment choiceFragment;				//Fragment for list of users whom current user is chatting with
	 public VoteFragment voteFragment;					//Fragment for list of top male and female users

	public ListView setting_list_view;
	private SettingAdapter setting_adapter;
		
	 MingleApplication app;
	 private static final String server_url = "http://ec2-54-178-214-176.ap-northeast-1.compute.amazonaws.com:8080";
	 private ActionBar actionBar;
	 
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
	 
	 private ArrayList<Integer> tabOnIcons; 
	 private ArrayList<Integer> tabOffIcons;
	 
	 
	 
	 
	 private void customizeActionBar() {
		// Set up the action bar to show tabs.
	        actionBar = getActionBar();
	        actionBar.setBackgroundDrawable(new ColorDrawable(0xFFFFFFFF));
	        actionBar.setDisplayShowTitleEnabled(false);
	        actionBar.setDisplayShowHomeEnabled(true);
	        View homeIcon = findViewById(android.R.id.home);
	        homeIcon.setVisibility(View.GONE);
	        actionBar.setDisplayHomeAsUpEnabled(false);
	        
			View mCustomView = LayoutInflater.from(this).inflate(R.layout.custom_actionbar, null);
	        
	        actionBar.setCustomView(mCustomView);
	      
	        actionBar.setDisplayShowCustomEnabled(true);
	        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	        
	        // for each of the sections in the app, add a tab to the action bar.
	        actionBar.addTab(actionBar.newTab().setCustomView(getViewForIcon(tabOffIcons.get(0)))
	            .setTabListener(this).setTag(R.string.tab1title));
	        actionBar.addTab(actionBar.newTab().setCustomView(getViewForIcon(tabOffIcons.get(1)))
	            .setTabListener(this).setTag(R.string.tab2title));
	        actionBar.addTab(actionBar.newTab().setCustomView(getViewForIcon(tabOffIcons.get(2)))
	            .setTabListener(this).setTag(R.string.tab3title));
	        
	 }

	@Override
    protected void onCreate(Bundle savedInstanceState) {
		app = ((MingleApplication) this.getApplication());
		
		app.initializeApplication();
        //Initialize HttpHelper that supports HTTP GET/POST requests and socket connection
        app.connectHelper = new HttpHelper(server_url, (MingleApplication)this.getApplication());
        app.connectHelper.getQuestionOfTheDay();
	       
	    // Initialize the database helper that manages local storage
	    app.dbHelper = new DatabaseHelper(this);

	    // If the app is not on for the first time, start HuntActivity
	    // and populate it with data from local storage
	    if(AppOnFirstTime()) {
	    	//Start activity for new user
		    app.createDefaultMyUser();
	        Intent i = new Intent(this, MainActivity.class);
	        i.putExtra(MainActivity.MAIN_TYPE, "new");
	        startActivity(i);
	        finish();
	    }
	        
	       
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunt);
	    initializeTabIconElems();


        app.socketHelper = new Socket(server_url, app);
        
         customizeActionBar();
        
        LocalBroadcastManager.getInstance(this).registerReceiver(userListReceiver,
      		  new IntentFilter(HttpHelper.HANDLE_CANDIDATE));
        
        LocalBroadcastManager.getInstance(this).registerReceiver(popListReceiver,
        		  new IntentFilter(HttpHelper.HANDLE_POP));
        
        LocalBroadcastManager.getInstance(this).registerReceiver(listUpdateReceiver,
      		  new IntentFilter(ImageDownloader.UPDATE_HUNT));
        
        //GCM Setup here
        context = (Context)this;
        // Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);
    		app.setRid(regid);

            if (regid.isEmpty()) {
                registerForNewUser();
            }           
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
        
       
        setting_list_view = (ListView) findViewById(R.id.setting_option_list);
        ArrayList<String> setting_list = new ArrayList<String>();
		setting_list.add("Profile");
		setting_list.add("Search Setting");
		setting_list.add("Delete");
		
	    setting_adapter = new SettingAdapter(this, R.layout.setting_row, setting_list, (MingleApplication) getApplicationContext());
	    setting_adapter.notifyDataSetChanged();
	    
	    final Activity curActivity = this;
	    setting_list_view.setOnItemClickListener(new OnItemClickListener() {
	    	@Override
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	    		// TODO Auto-generated method stub
	            if(position == 0){
	            	final String profile_uid = ((MingleApplication) curActivity.getApplication()).getMyUser().getUid();
	            	Intent profile_intent = new Intent(curActivity, ProfileActivity.class);
	                profile_intent.putExtra(ProfileActivity.PROFILE_UID, profile_uid);
	                profile_intent.putExtra(ProfileActivity.PROFILE_TYPE, "setting");
	                curActivity.startActivity(profile_intent);
	            } else if(position == 1){
	            
	            
	    		} else if(position == 2){
	            	//1. popup dialog to confirm deactivation
	            	//2. send msg to server that the user deactivates, should get confirm msg
	            	//	At server
	            	//	1. clear all data except uid
	            	//	2. handle other occasions and sync with client --> Must be shit. Hardest part maybe.
	            	//3. clear data in the database
	            	//4. cut socket and flush all data in mingleapplication
	            	
	            	AlertDialog.Builder popupBuilder = new AlertDialog.Builder(curActivity)
																.setTitle("Mingle")
																.setCancelable(false)
																.setMessage("Your account will be deactivated.")
																.setIcon(R.drawable.ic_launcher)
																.setPositiveButton("OK", new DialogInterface.OnClickListener() {
																	@Override
																	public void onClick(DialogInterface dialog, int id) {
																		dialog.dismiss();
																		((MingleApplication)curActivity.getApplication()).deactivateApp((Context)curActivity);
																        Intent backToMain = new Intent(curActivity, HuntActivity.class);
																        backToMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
																        startActivity(backToMain);
																	}
																})
																.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
																	@Override
																	public void onClick(DialogInterface dialog, int id) {
																		dialog.dismiss();
																	}
																});
	            	AlertDialog popupDialog = popupBuilder.create();
	        		popupDialog.show();
	            }	        	
	    	}
	    });
	    
	    // Set the ArrayAdapter as the ListView's adapter.  
	    setting_list_view.setAdapter(setting_adapter);    
	    setting_list_view.setBackgroundColor(Color.GRAY);
	    setting_list_view.setVisibility(View.GONE);
    }
    
	
	private void initializeTabIconElems() {
		tabOnIcons = new ArrayList<Integer>();
        tabOnIcons.add(R.drawable.vote_tab_on);
        tabOnIcons.add(R.drawable.chat_tab_on);
        tabOnIcons.add(R.drawable.choice_tab_on);
        
        tabOffIcons = new ArrayList<Integer>();
        tabOffIcons.add(R.drawable.vote_tab_off);
        tabOffIcons.add(R.drawable.chat_tab_off);
        tabOffIcons.add(R.drawable.choice_tab_off);
        
	}
    public void showProfile(View v) {
    	
    	System.out.println("show Profile called!!");
    }
    
	private ImageView getViewForIcon(int id) {
		BitmapDrawable icon = (BitmapDrawable)getResources().getDrawable(id);
        ImageView image = new ImageView(this);
        
        ActionBar.LayoutParams params = 
        		new ActionBar.LayoutParams(50, 
        				50, 0x10|0x01);
        
        params.setMargins(15, 15, 15, 15);
        
        image.setLayoutParams(params);
        image.setImageDrawable(icon);
        image.requestLayout();
        return image; 
	}
	
	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
		 BitmapDrawable icon = (BitmapDrawable)getResources().getDrawable(tabOffIcons.get(arg0.getPosition()));
		  ((ImageView)actionBar.getSelectedTab().getCustomView()).setImageDrawable(icon);
		
		
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
	        //menu.clear();
	        //getMenuInflater().in
	        //menu.
	        //getMenuInflater().inflate(R.menu.chat, menu);
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
		  }
		  BitmapDrawable icon = (BitmapDrawable)getResources().getDrawable(tabOnIcons.get(tab.getPosition()));
		  ((ImageView)actionBar.getSelectedTab().getCustomView()).setImageDrawable(icon);
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
	  
	  public void showSettingOptions(View v){
		  if(setting_list_view.getVisibility() == View.VISIBLE) setting_list_view.setVisibility(View.GONE);
		  else setting_list_view.setVisibility(View.VISIBLE);
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
	    	
	    	   	if(shownUser.getString("SEX").equals("M")) male_list.add(shownUser.getString("UID"));
	    	   	else female_list.add(shownUser.getString("UID"));
	    	    
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
	  
	  private BroadcastReceiver listUpdateReceiver = new BroadcastReceiver() {
	    	@Override
	    	public void onReceive(Context context, Intent intent) {
	    	   // Extract data included in the Intent
	    	   int pic_index = intent.getExtras().getInt(ImageDownloader.PIC_INDEX);
	    	   switch (pic_index) {
	    		case -1:
	    			popListUpdate();
	    			choiceListUpdate();
	    			break;
	    		case 0:
	    			candidateListUpdate();
	    	   }
	    	  }
	    };
	  
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
	  public void onResume(){
	        super.onRestart();
	        if(app.getMyUser().getUid() != null)
	        	app.socketHelper.connectSocket();
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



