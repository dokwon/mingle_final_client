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
import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.view.MenuItem;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
public class HuntActivity extends FragmentActivity implements ActionBar.TabListener	 {

	 public CandidateFragment candidateFragment;		//Fragment for list of chattable users
	 public ChoiceFragment ongoingChatFragment;	//Fragment for list of users whom current user is chatting with
	 public VoteFragment voteFragment;					//Fragment for list of top male and female users
	 
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
        
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        System.out.println("hunt navigation set");
        // Change the current activity to HuntActivity in HttpHelper
        
        ((MingleApplication) this.getApplication()).connectHelper.changeContext(this);

        //Load first 10 chattable users
        //allChatFragment.loadNewMatches(this);
        
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
			  System.out.println("vote fragment on view");
			  if(voteFragment == null) voteFragment = new VoteFragment();
			  
			  getFragmentManager().beginTransaction()
		        .replace(R.id.fragment_container, voteFragment).commit();
		  }	else if(tab.getTag().equals(R.string.tab2title)) {
			  if(candidateFragment == null) candidateFragment  = new CandidateFragment();
			 
			  System.out.println("chat fragment on view");
			  getFragmentManager().beginTransaction()
			        .replace(R.id.fragment_container, candidateFragment).commit();
			  
		  } else if(tab.getTag().equals(R.string.tab3title)) {
			  System.out.println("ongoing chat fragment on view");
			  if(ongoingChatFragment == null) ongoingChatFragment = new ChoiceFragment();
			  
			  getFragmentManager().beginTransaction()
		        .replace(R.id.fragment_container, ongoingChatFragment).commit();
		  }	
	  }
	  
	  //Update allChatFragment and ongoingChatFragment's lists
	  public void listsUpdate(){
		  if(candidateFragment != null) candidateFragment.listDataChanged();
		  else System.out.println("allchatfrag null");
		  if(ongoingChatFragment != null) ongoingChatFragment.listDataChanged();
		  else System.out.println("ongoingchatfrag null");
	  }
	  
	  public void topListUpdate(){
		  if(voteFragment != null) voteFragment.listDataChanged();
	  }
	  
	  @Override
	  public void onRestart(){
	        super.onRestart();
	        // Change the current activity to HuntActivity in HttpHelper
	        System.out.println(this);
	        ((MingleApplication) this.getApplication()).connectHelper.changeContext(this);
	        listsUpdate();
	 }
}

