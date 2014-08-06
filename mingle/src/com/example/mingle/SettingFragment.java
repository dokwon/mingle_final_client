package com.example.mingle;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
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
	            }
	        	
	        }

	    });
	    // Set the ArrayAdapter as the ListView's adapter.  
	    setting_list_view.setAdapter(setting_adapter);  
	        
		return rootView;
	}
	
}
