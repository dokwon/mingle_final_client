package com.example.mingle;

import java.util.List;









import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
 
public class MsgAdapter extends ArrayAdapter {
 
      List data;
      Context context;
      int layoutResID;
      Activity parent_act;
 
      public MsgAdapter(Context context, int layoutResourceId,List data, Activity parent) {
    	  super(context, layoutResourceId, data);
 
    	  this.data=data;
    	  this.context=context;
    	  this.layoutResID=layoutResourceId;
    	  this.parent_act = parent;
 
    	  // TODO Auto-generated constructor stub
      }
 
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
 
    	  NewsHolder holder = null;
    	  View row = convertView;
 
          if(row == null) {
        	  LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        	  row = inflater.inflate(layoutResID, parent, false);
 
        	  holder = new NewsHolder();
 
        	  holder.msg_view = (TextView)row.findViewById(R.id.msg);
        	  holder.timestamp_view =(TextView)row.findViewById(R.id.timestamp);
        	  holder.icon=(ImageView)row.findViewById(R.id.sender_image);
        	  
        	  row.setTag(holder);
          } else {
        	  holder = (NewsHolder)row.getTag();
          }
 
          LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) holder.msg_view.getLayoutParams();
          boolean is_me = false;
          try {
              JSONObject msg_data = (JSONObject)data.get(position);
              if(msg_data.getString("send_uid").equals(((MingleApplication) parent_act.getApplication()).currUser.getUid())) is_me = true;
              holder.msg_view.setText(msg_data.getString("msg"));
              holder.timestamp_view.setText(msg_data.getString("ts"));
          } catch (JSONException e) {
        	  // TODO Auto-generated catch block
        	  e.printStackTrace();
          }
          
          if(is_me) lp.gravity = Gravity.RIGHT;
          else lp.gravity = Gravity.LEFT;
          holder.msg_view.setLayoutParams(lp);
          
          /*
          holder.button1.setOnClickListener(new View.OnClickListener() { 
        	  @Override
              public void onClick(View v) {
        		  // TODO Auto-generated method stub
        		  Toast.makeText(context, "Button 1 Clicked",Toast.LENGTH_SHORT).show();
              }
          });
 
          holder.button2.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
            	  // TODO Auto-generated method stub
            	  Toast.makeText(context, "Button 2 Clicked",Toast.LENGTH_SHORT).show();
              }
          });
 
          holder.button3.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
            	  // TODO Auto-generated method stub
                  Toast.makeText(context, "Button 3 Clicked",Toast.LENGTH_SHORT).show();
              }
          });
 			*/
          return row;
 
      }
 
      
      static class NewsHolder{
    	  TextView msg_view;
    	  TextView timestamp_view;
    	  ImageView icon;
      }
}
 
