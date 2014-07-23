package com.example.mingle;

import java.util.ArrayList;
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

public class VoteAdapter extends ArrayAdapter {
	 
    List data;
    Context context;
    int layoutResID;

    public VoteAdapter(Context context, int layoutResourceId,List data) {
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

      	  holder.female_name = (TextView)row.findViewById(R.id.top_female_name);
    	  holder.female_pic=(ImageView)row.findViewById(R.id.top_female_image);
    	  holder.male_name = (TextView)row.findViewById(R.id.top_male_name);
      	  holder.male_pic=(ImageView)row.findViewById(R.id.top_male_image);
      	  
      	  row.setTag(holder);
        } else {
      	  holder = (NewsHolder)row.getTag();
        }

        ArrayList<ChattableUser> rank_list = (ArrayList<ChattableUser>) data.get(position);
        if(rank_list.get(0) != null){
        	holder.female_name.setText(rank_list.get(0).getName());
        	holder.female_pic.setImageDrawable(rank_list.get(0).getPic(0));
        }
        if(rank_list.get(1) != null){
        	holder.male_name.setText(rank_list.get(1).getName());
        	holder.male_pic.setImageDrawable(rank_list.get(1).getPic(0));
        }
        return row;

    }

    
    static class NewsHolder{
  	  ImageView female_pic;
  	  TextView female_name;
  	  ImageView male_pic;
  	  TextView male_name;
    }
}