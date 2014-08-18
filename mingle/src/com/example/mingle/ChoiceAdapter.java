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
import android.widget.TextView;

public class ChoiceAdapter extends ArrayAdapter {
	 
    List data;
    Context context;
    int layoutResID;
    MingleApplication app;

    public ChoiceAdapter(Context context, int layoutResourceId,List data, MingleApplication currApp) {
  	  super(context, layoutResourceId, data);

  	  this.data=data;
  	  this.context=context;
  	  this.layoutResID=layoutResourceId;
  	  this.app = currApp;

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
      	  holder.user_pic=(RoundedImageView)row.findViewById(R.id.sender_image);
      	  holder.user_name = (TextView)row.findViewById(R.id.sender_name);
          holder.user_name.setTypeface(app.koreanTypeFace);
      	 
      	  row.setTag(holder);
        } else {
      	  holder = (NewsHolder)row.getTag();
        }

        //Set values for this row
       final String choice_uid = (String)data.get(position);
        MingleUser choice = app.getMingleUser(choice_uid);
        if(choice.getLastMsg() == null) holder.msg_view.setText("");
        else holder.msg_view.setText(choice.getLastMsg().getContent());
        holder.user_name.setText(choice.getName());
        holder.user_pic.setImageDrawable(choice.getPic(-1));
        holder.user_pic.setOnClickListener(new OnClickListener()
        {

        	@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
        		Intent profile_intent = new Intent(context, ProfileActivity.class);
                profile_intent.putExtra(ProfileActivity.PROFILE_UID, choice_uid);
                profile_intent.putExtra(ProfileActivity.PROFILE_TYPE, "choice");
                context.startActivity(profile_intent);
			}
        });
        return row;

    }

    
    static class NewsHolder{
  	  TextView msg_view;
  	  TextView user_name;
  	  RoundedImageView user_pic;
    }
}
