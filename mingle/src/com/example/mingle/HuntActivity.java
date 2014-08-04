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

import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
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

	 
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunt);

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
        ArrayList<String> choice_list = ((MingleApplication) this.getApplicationContext()).getChoiceList();
        for(int i=0; i<choice_list.size(); i++){
        	System.out.println("Choice #"+i+": "+choice_list.get(i));
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
					if(app.getMingleUser(shownUser.getString("UID")) == null){
						String sex_var = "M";
						if(app.getMyUser().getSex().equals("M")) sex_var = "F";
						MingleUser new_user = new MingleUser(shownUser.getString("UID"), shownUser.getString("COMM"), Integer.valueOf(shownUser.getString("NUM")), Integer.valueOf(shownUser.getString("PHOTO_NUM")), (Drawable) this.getResources().getDrawable(R.drawable.ic_launcher),sex_var);

						app.addMingleUser(new_user);
						new ImageDownloader(this.getApplicationContext(), new_user.getUid(), 0);
					}
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
	    			new ImageDownloader(this.getApplicationContext(), new_user.getUid(), 0);
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
}

