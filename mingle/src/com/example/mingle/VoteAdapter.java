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
    MingleApplication app;

    public VoteAdapter(Context context, int layoutResourceId,List data, MingleApplication currApp) {
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

      	  holder.female_name = (TextView)row.findViewById(R.id.top_female_name);
    	  holder.female_pic=(ImageView)row.findViewById(R.id.top_female_image);
    	  holder.male_name = (TextView)row.findViewById(R.id.top_male_name);
      	  holder.male_pic=(ImageView)row.findViewById(R.id.top_male_image);
      	  
      	  row.setTag(holder);
        } else {
      	  holder = (NewsHolder)row.getTag();
        }

        ArrayList<String> rank_list = (ArrayList<String>) data.get(position);
        String female_uid = rank_list.get(0);
        String male_uid = rank_list.get(1);
        
        if(!female_uid.equals("")){
        	MingleUser female_pop = app.getMingleUser(female_uid);
        	holder.female_name.setText(female_pop.getName());
        	holder.female_pic.setImageDrawable(female_pop.getPic(0));
        }
        if(!male_uid.equals("")){
        	MingleUser male_pop = app.getMingleUser(male_uid);
        	holder.male_name.setText(male_pop.getName());
        	holder.male_pic.setImageDrawable(male_pop.getPic(0));
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