package com.example.mingle;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;


public class ActionbarController {
	
	//Set up Action bar
 	 @SuppressLint("NewApi")
 	public static ActionBar customizeActionBar(int id, Activity activity, int s5left, int s5right) {
 		// Set up the action bar to show tabs.
 		 
 	        ActionBar actionBar = activity.getActionBar();
 			View mCustomView = LayoutInflater.from(activity).inflate(id, null);
 			LayoutParams layout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
 			mCustomView.setLayoutParams(layout);
 	        actionBar.setCustomView(mCustomView);
 	        actionBar.setDisplayShowTitleEnabled(false);
 	        actionBar.setDisplayShowCustomEnabled(true);
 	        actionBar.setDisplayShowHomeEnabled(true);
 	      if(activity instanceof HuntActivity) {
	  	    	actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	  	    	 mCustomView.findViewById(R.id.preview_button).setVisibility(View.GONE);
	  	      } 
	        
 	        if(activity instanceof ChatroomActivity) {
 	        	actionBar.setDisplayHomeAsUpEnabled(true);
 	        } else {
 	  	        actionBar.setDisplayHomeAsUpEnabled(false);
 	  	        View homeIcon = activity.findViewById(android.R.id.home);
 	  	        homeIcon.setVisibility(View.GONE);
				ActionBar.LayoutParams params = (ActionBar.LayoutParams) actionBar.getCustomView().getLayoutParams();
				  	        if(Integer.valueOf(android.os.Build.VERSION.SDK_INT) >= 14 && ViewConfiguration.get(activity).hasPermanentMenuKey()) {
				  	        	params.setMargins(-100, 0, 0, 0);
				  	        } else { 
				  	        	params.setMargins(s5left, 0, s5right, 0);
				  	        	
				  	        }
				  	      actionBar.getCustomView().setLayoutParams(params);
 	        }
 	        return actionBar;
 	 }
}
