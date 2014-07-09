package com.example.mingle;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatRoomAdapter extends ArrayAdapter {
	 
    List data;
    Context context;
    int layoutResID;

    public ChatRoomAdapter(Context context, int layoutResourceId,List data) {
  	  super(context, layoutResourceId, data);

  	  this.data=data;
  	  this.context=context;
  	  this.layoutResID=layoutResourceId;

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
      	  holder.user_pic=(ImageView)row.findViewById(R.id.sender_image);
      	  
      	  row.setTag(holder);
        } else {
      	  holder = (NewsHolder)row.getTag();
        }

        ChatRoom room_data = (ChatRoom)data.get(position);
        holder.msg_view.setText(room_data.getLastMsg());
        holder.user_pic.setImageDrawable(room_data.getPic());
       
        return row;

    }

    
    static class NewsHolder{
  	  TextView msg_view;
  	  ImageView user_pic;
    }
}