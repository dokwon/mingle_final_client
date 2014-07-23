package com.example.mingle;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
 
public class MsgAdapter extends ArrayAdapter {
	public final static String PROFILE_UID = "com.example.mingle.PROFILE_UID";	//Intent data to pass on when new Profile Activity started
 
      List data;
      Context context;
      int layoutResID;
      int my_layoutResID;
      Activity parent_act;
 
      public MsgAdapter(Context context, int layoutResourceId, int my_layoutResourceId, List data, Activity parent) {
    	  super(context, layoutResourceId, data);
 
    	  this.data=data;
    	  this.context=context;
    	  this.layoutResID=layoutResourceId;
    	  this.my_layoutResID=my_layoutResourceId;
    	  this.parent_act = parent; 
    	  // TODO Auto-generated constructor stub
      }
 
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
 
    	  NewsHolder holder = null;
    	  View row = convertView;
    	  
    	  boolean is_me = false;
          Message msg_data = (Message)data.get(position);
          if(msg_data.getUid().equals(((MingleApplication) parent_act.getApplication()).currUser.getUid())) is_me = true;
 
          LayoutInflater inflater = ((Activity)context).getLayoutInflater();
          
          if(is_me) row = inflater.inflate(my_layoutResID,parent,false);
          else row = inflater.inflate(layoutResID, parent, false);
 
          holder = new NewsHolder();
          holder.msg_view = (TextView)row.findViewById(R.id.msg);
          holder.timestamp_view =(TextView)row.findViewById(R.id.timestamp);
          if(!is_me){
        	  holder.pic=(ImageView)row.findViewById(R.id.sender_image);
        	  holder.name_view =(TextView)row.findViewById(R.id.name);
          }
        	  
          row.setTag(holder);
 
          RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.msg_view.getLayoutParams();
          holder.msg_view.setText(msg_data.getContent());
          holder.timestamp_view.setText(msg_data.getTimestamp().toString());
          if(!is_me){
        	  holder.pic.setImageDrawable(msg_data.getPic());
        	  final String profile_uid = msg_data.getUid();
        	  holder.pic.setOnClickListener(new OnClickListener()
              {
              	@Override
      			public void onClick(View arg0) {
      				// TODO Auto-generated method stub
              		Intent profile_intent = new Intent(context, ProfileActivity.class);
                    profile_intent.putExtra(PROFILE_UID, profile_uid);
                    context.startActivity(profile_intent);
      			}
              });
        	  holder.name_view.setText(msg_data.getName());
          }
          holder.msg_view.setLayoutParams(lp);
          
          return row;
      }
 
      
      static class NewsHolder{
    	  TextView msg_view;
    	  TextView timestamp_view;
    	  TextView name_view;
    	  ImageView pic;
      }
}
 
