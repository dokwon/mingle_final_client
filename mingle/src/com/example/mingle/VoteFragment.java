package com.example.mingle;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
		
	    // Stores 
	    ((MingleApplication) parent.getApplication()).connectHelper.requestVoteList();

	    top_adapter = new VoteAdapter(parent, R.layout.vote_row, ((MingleApplication) parent.getApplication()).getPopList(), (MingleApplication) parent.getApplicationContext());
	    top_adapter.notifyDataSetChanged();
	       
	    
	    // Set the ArrayAdapter as the ListView's adapter.  
	    top_list_view.setAdapter(top_adapter);  
	        
		return rootView;
	}
	
	  public void listDataChanged(){
		  parent.runOnUiThread(new Runnable() {
		  		public void run() {
		  			top_adapter.notifyDataSetChanged();
		  		}
		  });
	  }
}
