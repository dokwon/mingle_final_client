package com.example.mingle;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ChoiceFragment extends Fragment {
	public final static String USER_UID = "com.example.mingle.USER_SEL";	//Intent data to pass on when new Chatroom Activity started

	public ListView currentlychattinglistview;
	private ArrayList<String> choice_list;
	private ChoiceAdapter adapter;
	
	private Activity parent; 
	
	@Override
	  public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		 System.out.println("ongoing chatview create complete");
		parent = getActivity();
		
		View rootView = inflater.inflate(R.layout.choice_fragment, container, false);
		
		currentlychattinglistview= (ListView)(rootView.findViewById(R.id.mingling)) ;
		
        // Stores 
		choice_list = ((MingleApplication) parent.getApplication()).getChoiceList();
        adapter = new ChoiceAdapter(parent, R.layout.chatroom_row, choice_list, (MingleApplication)parent.getApplicationContext());
        adapter.notifyDataSetChanged();
        
        final Activity curActivity = parent;
        currentlychattinglistview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // TODO Auto-generated method stub        			
                Intent chat_intent = new Intent(curActivity, ChatroomActivity.class);
         		chat_intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                chat_intent.putExtra(USER_UID, choice_list.get(position));
                curActivity.startActivity(chat_intent);
            }

        });
        // Set the ArrayAdapter as the ListView's adapter.  
        currentlychattinglistview.setAdapter( adapter );        
        
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		parent.runOnUiThread(new Runnable() {
    		public void run() {
    			adapter.notifyDataSetChanged();
    		}
    	});
	}
	
	public void listDataChanged(){
		parent.runOnUiThread(new Runnable() {
	  		public void run() {
	  			adapter.notifyDataSetChanged();
	  		}
	  	});
	}
}
