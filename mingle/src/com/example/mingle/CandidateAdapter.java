package com.example.mingle;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;



import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;


import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;


public class CandidateAdapter extends ArrayAdapter {

    List data;				// List of candidate mingle users' uid
    Context context;		// Hunt Activity context
    int layoutResID;		// Layout for row
    MingleApplication app;

    public CandidateAdapter(Context context, int layoutResourceId,List data, MingleApplication currApp) {
  	  super(context, layoutResourceId, data);

  	  this.data=data;
  	  this.context=context;
  	  this.layoutResID=layoutResourceId;
  	  this.app = currApp;

  	  // TODO Auto-generated constructor stub
    }

    
    /* Return drawable id of selected member num */

    
    private int convertToPx(int dp) {
        // Get the screen's density scale
        final float scale = context.getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (dp * scale + 0.5f);
    }
    
    
    
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

  	  NewsHolder holder = null;
  	  View row = convertView;

        if(row == null) {
      	  LayoutInflater inflater = ((Activity)context).getLayoutInflater();
      	  row = inflater.inflate(layoutResID, parent, false);
      	 
      	  holder = new NewsHolder();
      	
      	  holder.user_num = (ImageView)row.findViewById(R.id.member_num);
      	  holder.user_name = (TextView)row.findViewById(R.id.user_name);
          holder.user_name.setTypeface(app.koreanBoldTypeFace);
      	  holder.user_pic=(ImageView)row.findViewById(R.id.user_pic);
      	  holder.user_dist=(TextView)row.findViewById(R.id.user_dist);      
      	  holder.user_dist.setTextColor(Color.GRAY);
      	  holder.user_dist.setTypeface(app.koreanTypeFace);
      	  row.setTag(holder);
        } else {
      	  holder = (NewsHolder)row.getTag();
        }
        
        //Set values for this row
        String candidate_uid = ((ArrayList<String>)data).get(position);
        MingleUser candidate = app.getMingleUser(candidate_uid);
        
        final int num_pic_id = app.memberNumRsId(candidate.getNum());
        holder.user_num.setImageResource(num_pic_id);
        holder.user_name.setText(candidate.getName());
        if(app.getLat() == 0.0 || app.getLong() == 0.0) holder.user_dist.setVisibility(View.GONE);
        else holder.user_dist.setText(Float.toString(candidate.getDistance())+"km");
        Drawable main_drawable = candidate.getPic(0);
        
        holder.user_pic.setImageDrawable(ImageRounder.getRoundedDrawable((Activity)context,
        		main_drawable, 13));
        
        //If user's pic is clicked, show his profile
        final String profile_uid = candidate.getUid();
        holder.user_pic.setOnClickListener(new OnClickListener()
        {

        	@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
        		Intent profile_intent = new Intent(context, ProfileActivity.class);
                profile_intent.putExtra(ProfileActivity.PROFILE_UID, profile_uid);
                profile_intent.putExtra(ProfileActivity.PROFILE_TYPE, "candidate");
                context.startActivity(profile_intent);
			}
        });
        
        return row;

    }

    
    static class NewsHolder{
    	ImageView user_num;
    	TextView user_name;
  	  	ImageView user_pic;
  	  	TextView user_dist;
    }
}
