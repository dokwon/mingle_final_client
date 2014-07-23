package com.example.mingle;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class AllChatAdapter extends ArrayAdapter {
	public final static String PROFILE_UID = "com.example.mingle.PROFILE_UID";	//Intent data to pass on when new Profile Activity started
	
    List data;
    Context context;
    int layoutResID;

    public AllChatAdapter(Context context, int layoutResourceId,List data) {
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

      	  holder.user_num = (TextView)row.findViewById(R.id.user_num);
      	  holder.user_name = (TextView)row.findViewById(R.id.user_name);
      	  holder.user_pic=(ImageView)row.findViewById(R.id.user_pic);
      	  
      	  //holder.button1=(Button)row.findViewById(R.id.swipe_button1);
      	  //holder.button2=(Button)row.findViewById(R.id.swipe_button2);
      	  //holder.button3=(Button)row.findViewById(R.id.swipe_button3);
      	  row.setTag(holder);
        } else {
      	  holder = (NewsHolder)row.getTag();
        }
        ChattableUser itemdata = ((ArrayList<ChattableUser>)data).get(position);
        holder.user_num.setText(Integer.toString(itemdata.getNum()) + "  " + Integer.toString(itemdata.getPhotoNum()));
        holder.user_name.setText(itemdata.getName());
        Drawable main_drawable = itemdata.getPic(0);
        holder.user_pic.setImageDrawable(main_drawable);
        
        final String profile_uid = itemdata.getUid();
        holder.user_pic.setOnClickListener(new OnClickListener()
        {

        	@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
        		Intent profile_intent = new Intent(context, ProfileActivity.class);
                profile_intent.putExtra(PROFILE_UID, profile_uid);
                context.startActivity(profile_intent);
			}
        });
        
        return row;

    }

    
    static class NewsHolder{
  	  TextView user_num;
  	  TextView user_name;
  	  ImageView user_pic;
  	  Button button1;
  	  Button button2;
  	  Button button3;
    }
}

