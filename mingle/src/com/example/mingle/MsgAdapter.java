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
      List data;
      Context context;
      int layoutResID;
      int my_layoutResID;
      MingleUser user;
      MingleApplication app; 
 
      public MsgAdapter(Context context, int layoutResourceId, int my_layoutResourceId, List data, MingleUser receiver) {
    	  super(context, layoutResourceId, data);
    	  this.app = (MingleApplication) ((Activity) context).getApplication();
    	  this.data=data;
    	  this.context=context;
    	  this.layoutResID=layoutResourceId;
    	  this.my_layoutResID=my_layoutResourceId;
    	  this.user = receiver; 
    	  // TODO Auto-generated constructor stub
      }
 
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
 
    	  NewsHolder holder = null;
    	  View row = convertView;
    	  
          Message msg_data = (Message)data.get(position);
          
          LayoutInflater inflater = ((Activity)context).getLayoutInflater();
          
          if(msg_data.isMyMsg()) row = inflater.inflate(my_layoutResID,parent,false);
          else row = inflater.inflate(layoutResID, parent, false);
          
          holder = new NewsHolder();
          holder.msg_view = (TextView)row.findViewById(R.id.msg);
          //holder.timestamp_view =(TextView)row.findViewById(R.id.timestamp);
          if(!msg_data.isMyMsg()){
        	  holder.pic=(RoundedImageView)row.findViewById(R.id.sender_image);
        	  holder.name_view =(TextView)row.findViewById(R.id.name);
          }
        	  
          row.setTag(holder);
 
          RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.msg_view.getLayoutParams();
          holder.msg_view.setText(msg_data.getContent());
          //holder.timestamp_view.setText(msg_data.getTimestamp().toString());
          if(!msg_data.isMyMsg()){
        	  holder.pic.setImageDrawable(user.getPic(0));
        	  final String profile_uid = user.getUid();
        	  holder.pic.setOnClickListener(new OnClickListener()
              {
              	@Override
      			public void onClick(View arg0) {
      				// TODO Auto-generated method stub
              		Intent profile_intent = new Intent(context, ProfileActivity.class);
                    profile_intent.putExtra(ProfileActivity.PROFILE_UID, profile_uid);
                    profile_intent.putExtra(ProfileActivity.PROFILE_TYPE, "choice");
                    context.startActivity(profile_intent);
      			}
              });
        	  holder.name_view.setText(user.getName());
          }
          holder.msg_view.setLayoutParams(lp);
          
          return row;
      }
 
      
      static class NewsHolder{
    	  TextView msg_view;
    	  //TextView timestamp_view;
    	  TextView name_view;
    	  RoundedImageView pic;
      }
}
 
