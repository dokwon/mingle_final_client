package com.example.mingle;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;


public class SettingFragment extends Fragment{

	public ListView setting_list_view;
	private SettingAdapter setting_adapter;
	private Activity parent; 
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		parent = getActivity();
		
		View rootView = inflater.inflate(R.layout.setting_fragment, container, false);
		setting_list_view = (ListView)(rootView.findViewById(R.id.setting_list));

		ArrayList<String> setting_list = new ArrayList<String>();
		setting_list.add("Profile");
		setting_list.add("Setting");
		setting_list.add("Delete");
		
	    setting_adapter = new SettingAdapter(parent, R.layout.setting_row, setting_list, (MingleApplication) parent.getApplicationContext());
	    setting_adapter.notifyDataSetChanged();
	       
	    final Activity curActivity = parent;
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
	            	Intent setting_intent = new Intent(curActivity, SearchSettingActivity.class);
	            	setting_intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            	curActivity.startActivity(setting_intent);
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
	        
		return rootView;
	}
	
}
