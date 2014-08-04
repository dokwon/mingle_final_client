package com.example.mingle;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

	    setting_adapter = new SettingAdapter(parent, R.layout.setting_row, ((MingleApplication) parent.getApplication()).getSettingList(), (MingleApplication) parent.getApplicationContext());
	    setting_adapter.notifyDataSetChanged();
	       
	    final Activity curActivity = parent;
	    setting_list_view.setOnItemClickListener(new OnItemClickListener() {
	    	@Override
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	    		// TODO Auto-generated method stub
	            if(position == 0){
	            	Intent chat_intent = new Intent(curActivity,ProfileActivity.class);
		            chat_intent.putExtra(ProfileActivity.PROFILE_UID, ((MingleApplication) curActivity.getApplication()).getMyUser().getUid());
		            curActivity.startActivity(chat_intent);
	            }
	        	
	        }

	    });
	    // Set the ArrayAdapter as the ListView's adapter.  
	    setting_list_view.setAdapter(setting_adapter);  
	        
		return rootView;
	}
	
}
