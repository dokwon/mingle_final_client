package com.example.mingle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.app.ActionBar;
import android.widget.RelativeLayout.LayoutParams;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
public class HuntActivity extends FragmentActivity implements ActionBar.TabListener	 {
	public CandidateFragment candidateFragment;			//Fragment for list of candidates
	public ChoiceFragment choiceFragment;				//Fragment for list of choices
	public VoteFragment voteFragment;					//Fragment for list of popular users

	 
	//For GCM below
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private String SENDER_ID = "5292889580";
	private GoogleCloudMessaging gcm;
	private String regid;
	static final String TAG = "GCMDemo";
		
	private MingleApplication app;
	private ActionBar actionBar;
	private ArrayList<Integer> tabOnIcons; 
	private ArrayList<Integer> tabOffIcons;
	 
	private Context context;

	//Set up Action bar
	 private void customizeActionBar() {
		// Set up the action bar to show tabs.
	        actionBar = getActionBar();
	        
			View mCustomView = LayoutInflater.from(this).inflate(R.layout.custom_actionbar, null);
			LayoutParams layout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			mCustomView.setLayoutParams(layout);
	        actionBar.setCustomView(mCustomView);
	        
	        actionBar.setBackgroundDrawable(new ColorDrawable(0xFFFFFFFF));
	        actionBar.setDisplayShowTitleEnabled(false);
	        actionBar.setDisplayShowHomeEnabled(true);
	        View homeIcon = findViewById(android.R.id.home);
	        ((View)homeIcon.getParent()).setVisibility(View.GONE);
	        
	        actionBar.setDisplayHomeAsUpEnabled(false);

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
		
   
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunt);
	    initializeTabIconElems();  
        customizeActionBar();
        
        LocalBroadcastManager.getInstance(this).registerReceiver(userListReceiver,
      		  new IntentFilter(HttpHelper.HANDLE_CANDIDATE));
        
        LocalBroadcastManager.getInstance(this).registerReceiver(popListReceiver,
        		  new IntentFilter(HttpHelper.HANDLE_POP));
        
        LocalBroadcastManager.getInstance(this).registerReceiver(listUpdateReceiver,
      		  new IntentFilter(ImageDownloader.UPDATE_HUNT));
        
        LocalBroadcastManager.getInstance(this).registerReceiver(httpErrorReceiver,
      		  new IntentFilter(HttpHelper.HANDLE_HTTP_ERROR));
        
        app.socketHelper.connectSocket();
        
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
        
    }
    
	//Set up Tab icon images
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

    
	private ImageView getViewForIcon(int id) {
		BitmapDrawable icon = (BitmapDrawable)getResources().getDrawable(id);
        ImageView image = new ImageView(this);
        
        ActionBar.LayoutParams params = 
        		new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, 
        				ActionBar.LayoutParams.MATCH_PARENT, 0x10|0x01);
        
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
	        getMenuInflater().inflate(R.menu.setting_menu, menu);
	        return true;  
	    }
	  
	  @Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	      
	      // Handle item selection
		  System.out.println("Print a bunch of crap");
	      switch (item.getItemId()) {
	      	case R.id.setting_option_profile:
	      		final String profile_uid = ((MingleApplication) this.getApplication()).getMyUser().getUid();
            	Intent profile_intent = new Intent(this, ProfileActivity.class);
                profile_intent.putExtra(ProfileActivity.PROFILE_UID, profile_uid);
                profile_intent.putExtra(ProfileActivity.PROFILE_TYPE, "setting");
                this.startActivity(profile_intent);
	            break;
	            
	      	case R.id.setting_option_search:
				Intent setting_intent = new Intent(this, SearchSettingActivity.class);
	            	setting_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	            	this.startActivity(setting_intent);
	      		break;
	      	case R.id.setting_option_delete:
	      	//1. popup dialog to confirm deactivation
            	//2. send msg to server that the user deactivates, should get confirm msg
            	//	At server
            	//	1. clear all data except uid
            	//	2. handle other occasions and sync with client --> Must be shit. Hardest part maybe.
            	//3. clear data in the database
            	//4. cut socket and flush all data in mingleapplication
	      		final Activity curActivity = this;
            	AlertDialog.Builder popupBuilder = new AlertDialog.Builder(this)
															.setTitle("Mingle")
															.setCancelable(false)
															.setMessage("Your account will be deactivated.")
															.setIcon(R.drawable.mingle_logo)
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
	      		break;
	          default:
	              return super.onOptionsItemSelected(item);
	      }
	      return true;
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
	  
	    /* Broadcast Receiver for notification of receiving candidate list from the server*/
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
	  
	    /* Broadcast Receiver for notification of receiving popular list from the server*/
	    private BroadcastReceiver popListReceiver = new BroadcastReceiver() {
	    	@Override
	    	public void onReceive(Context context, Intent intent) {
	    	   // Extract data included in the Intent
	    	   String data = intent.getStringExtra(HttpHelper.POP_LIST);
	    	   Log.d("receiver", "Got message: " + data);
	    	   
	    	   try {
	    		   JSONObject pop_result = new JSONObject(data);
	    		   if(pop_result.getString("RESULT").equals("success")){
	    			   JSONArray pop_list_arr = new JSONArray(pop_result.getString("POP_LIST"));
	    			   handlePopList(pop_list_arr);
	    		   } else {
	   	    			Toast.makeText(getApplicationContext(), "Event not available yet!", Toast.LENGTH_SHORT).show();
 	    		   }
	    	   } catch (JSONException e) {
	    		   // TODO Auto-generated catch block
	    		   e.printStackTrace();
	    	   }
	    	   
	    	  }
	    };

	  /* TimerTask for load more when the server doesn't have more candidates */
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
	  
	
	  
	  /* If no more candidates, start TimeTask.
	   * Otherwise, save new candidates into candidate list and dispatch image downloader
	   */
	  public void handleCandidateList(JSONArray list_of_users){
		MingleApplication app = ((MingleApplication) this.getApplicationContext());
		if(list_of_users.length() == 0){
			app.noMoreCandidate();
			CandidateTimerTask ctt = new CandidateTimerTask(app);
			new Timer().schedule(ctt, 30000);
		} else {
		  
			//update candidate list and dispatch image downloader
			for(int i = 0 ; i < list_of_users.length(); i++) {
				try {
					JSONObject shownUser = list_of_users.getJSONObject(i);
					MingleUser candidate = app.getMingleUser(shownUser.getString("UID"));
					if(candidate == null){
						String sex_var = "M";
						if(app.getMyUser().getSex().equals("M")) sex_var = "F";
						candidate = new MingleUser(shownUser.getString("UID"), 
								shownUser.getString("COMM"), 
								Integer.valueOf(shownUser.getString("NUM")), 
								Integer.valueOf(shownUser.getString("PHOTO_NUM")), 
								(Drawable) this.getResources().getDrawable(app.blankProfileImage),
								sex_var);

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
	  
	  /* Save new popular list into popular list and dispatch image downloader */
	  public void handlePopList(JSONArray list_of_top){
		MingleApplication app = ((MingleApplication) this.getApplicationContext());
		    
		//get female and male popular list
	    ArrayList<String> female_list = new ArrayList<String>();
	    ArrayList<String> male_list = new ArrayList<String>();
	    for(int i = 0 ; i < list_of_top.length(); i++) {
	    	try {
	    			JSONObject shownUser = list_of_top.getJSONObject(i);
	    		MingleUser pop_user = app.getMingleUser(shownUser.getString("UID"));
	    		if(pop_user == null){
	    			MingleUser new_user = new MingleUser(shownUser.getString("UID"), 
	    					shownUser.getString("COMM"), 
	    					Integer.valueOf(shownUser.getString("NUM")), 
	    					Integer.valueOf(shownUser.getString("PHOTO_NUM")), 
	    					(Drawable) this.getResources().getDrawable(app.blankProfileImage),
	    					shownUser.getString("SEX"));
	    			app.addMingleUser(new_user);
	    			new ImageDownloader(this.getApplicationContext(), new_user.getUid(), -1).execute();
	    		}
	    	
	    	   	if(shownUser.getString("SEX").equals("M")) male_list.add(shownUser.getString("UID"));
	    	   	else female_list.add(shownUser.getString("UID"));
	    	    
	    	} catch (JSONException e){
	    		e.printStackTrace();
	    	}
	    }
	    		
	    app.emptyPopList();
	    
	  //add female and male pop users to list
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
	  
	  /* Broadcast Receiver for notification of need for lists update from the server*/
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
	  

	  public void candidateListUpdate(){
		  if(candidateFragment != null) candidateFragment.listDataChanged();
	  }
	  
	  public void choiceListUpdate(){
		  if(choiceFragment != null) choiceFragment.listDataChanged();
	  }
	  
	  public void popListUpdate(){
		  if(voteFragment != null) voteFragment.listDataChanged();
	  }
	  
	  /* Broadcast Receiver for notification of http error*/
	  private BroadcastReceiver httpErrorReceiver = new BroadcastReceiver() {
	    	@Override
	    	public void onReceive(Context context, Intent intent) {
	    		Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
	    	}
	  };
	  
	  @Override
	  public void onResume(){
	        super.onRestart();
	        candidateListUpdate();
	        choiceListUpdate();
	  }
	 
	  @Override
	  public void onDestroy(){
		  LocalBroadcastManager.getInstance(this).unregisterReceiver(userListReceiver);
		  LocalBroadcastManager.getInstance(this).unregisterReceiver(popListReceiver);  
		  LocalBroadcastManager.getInstance(this).unregisterReceiver(listUpdateReceiver);  
		  LocalBroadcastManager.getInstance(this).unregisterReceiver(httpErrorReceiver);  

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



