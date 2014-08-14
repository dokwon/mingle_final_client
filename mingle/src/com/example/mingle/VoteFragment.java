package com.example.mingle;

import java.util.Calendar;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

public class VoteFragment extends Fragment{

	public ListView top_list_view;
	private VoteAdapter top_adapter;
	
	private Activity parent; 
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		parent = getActivity();
		
		View rootView = inflater.inflate(R.layout.vote_fragment, container, false);
		
		top_list_view = (ListView)(rootView.findViewById(R.id.top_list));
		
		ImageView not_vote_time_view = (ImageView)(rootView.findViewById(R.id.vote_error_page));
		
		
	    // Stores 
	    ((MingleApplication) parent.getApplication()).connectHelper.requestVoteList();

	    top_adapter = new VoteAdapter(parent, R.layout.vote_row, ((MingleApplication) parent.getApplication()).getPopList(), (MingleApplication) parent.getApplicationContext());
	    top_adapter.notifyDataSetChanged();
	       
	    
	    // Set the ArrayAdapter as the ListView's adapter.  
	    top_list_view.setAdapter(top_adapter);  
	        
	    if(isVoteTime()) {
	    	not_vote_time_view.setVisibility(View.GONE);
	    } else {
	    	rootView.findViewById(R.id.femaleTopLogo).setVisibility(View.GONE);
	    	rootView.findViewById(R.id.maleTopLogo).setVisibility(View.GONE);
	    	top_list_view.setVisibility(View.GONE);
	    }
	    
		return rootView;
	}
	
	private boolean isVoteTime() {
		int curHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		if( curHour < 21 || curHour > 1) {
			return false;
		}
		return true;
	}
	
	  public void listDataChanged(){
		  parent.runOnUiThread(new Runnable() {
		  		public void run() {
		  			top_adapter.notifyDataSetChanged();
		  		}
		  });
	  }
}
