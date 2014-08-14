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
	public ListView currentlychattinglistview;		//Listview for choice mingle users
	private ChoiceAdapter adapter;					//Listview Adapter for currentlychattinglistview
	private ArrayList<String> choice_list;			//List of choices' uids
	
	private Activity parent; 
	
	@Override
	  public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		parent = getActivity();
		View rootView = inflater.inflate(R.layout.choice_fragment, container, false);
		
		//Initialize list view and adapter
		currentlychattinglistview= (ListView)(rootView.findViewById(R.id.mingling)) ;
		choice_list = ((MingleApplication) parent.getApplication()).getChoiceList();
        adapter = new ChoiceAdapter(parent, R.layout.chatroom_row, choice_list, (MingleApplication)parent.getApplicationContext());
        adapter.notifyDataSetChanged();
        
        //On click, open chat room
        final Activity curActivity = parent;
        currentlychattinglistview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // TODO Auto-generated method stub        			
                Intent chat_intent = new Intent(curActivity, ChatroomActivity.class);
         		chat_intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                chat_intent.putExtra(ChatroomActivity.USER_UID, choice_list.get(position));
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
		listDataChanged();
	}

	//Update candidate list	
	public void listDataChanged(){
		parent.runOnUiThread(new Runnable() {
	  		public void run() {
	  			adapter.notifyDataSetChanged();
	  		}
	  	});
	}
}
