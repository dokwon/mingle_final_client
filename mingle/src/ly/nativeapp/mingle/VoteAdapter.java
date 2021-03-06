package ly.nativeapp.mingle;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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


    private void setRankNumberView(int position, View row, NewsHolder holder) {
    
    	holder.user_rank = (TextView) row.findViewById(R.id.pop_user_rank);
    	/*//holder.male_rank = (ImageView) row.findViewById(R.id.top_male_rank);
    	switch (position) {
    		case 0:
    			holder.user_rank.setText("1위");
    			//holder.user_rank.setImageResource(R.drawable.female_ranking_number1);
    			//holder.male_rank.setImageResource(R.drawable.male_ranking_number_1);
    			break;
    		case 1:
    			holder.user_rank.setText("2위");
    			//holder.user_rank.setImageResource(R.drawable.female_ranking_number2);
    			//holder.male_rank.setImageResource(R.drawable.male_ranking_number_2);
    			break;
    		case 2:
    			holder.user_rank.setText("3위");
    			//holder.user_rank.setImageResource(R.drawable.female_ranking_number3);
    			//holder.male_rank.setImageResource(R.drawable.male_ranking_number_3);
    			break;
    		case 3:
    			holder.user_rank.setText("4위");
    			//holder.user_rank.setImageResource(R.drawable.female_ranking_number4);
    			//holder.male_rank.setImageResource(R.drawable.male_ranking_number_4);
    			break;
    		case 4:
    			holder.user_rank.setText("5위");
    			//holder.user_rank.setImageResource(R.drawable.female_ranking_number5);
    			//holder.male_rank.setImageResource(R.drawable.male_ranking_number_5);
    			break;
    	}*/
    }
    
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

  	  NewsHolder holder = null;
  	  View row = convertView;
  	
        if(row == null) {
      	  LayoutInflater inflater = ((Activity)context).getLayoutInflater();
      	  row = inflater.inflate(layoutResID, parent, false);

      	  holder = new NewsHolder();

      	  holder.pop_layout = (RelativeLayout)row.findViewById(R.id.pop_user_rl);
      	  holder.user_name = (TextView)row.findViewById(R.id.pop_user_name);
      	  holder.user_name.setTypeface(app.koreanTypeFace);
    	  holder.user_pic=(ImageView)row.findViewById(R.id.pop_user_image);
    	  holder.num_pic = (ImageView)row.findViewById(R.id.pop_user_num);
    	  holder.user_dist=(TextView)row.findViewById(R.id.pop_user_dist);      
      	  holder.user_dist.setTextColor(Color.GRAY);
      	  holder.user_dist.setTypeface(app.koreanTypeFace);
      	  holder.number_icon = (ImageView)row.findViewById(R.id.pop_user_num_icon);
    	  
    	  /*holder.male_layout = (FrameLayout)row.findViewById(R.id.male_vote_rl);
    	  holder.male_name = (TextView)row.findViewById(R.id.top_male_name);
    	  holder.male_name.setTypeface(app.koreanTypeFace);
      	  holder.male_pic=(ImageView)row.findViewById(R.id.top_male_image);*/
      	  setRankNumberView(position, row, holder);
      	  row.setTag(holder);
        } else {
      	  holder = (NewsHolder)row.getTag();
        }
       
        String pop_user_uid = (String) data.get(position);
        int corner_round = parent.getResources().getDimensionPixelSize(R.dimen.vote_corner_round);
        if(!(Integer.valueOf(android.os.Build.VERSION.SDK_INT) >= 14 && ViewConfiguration.get(parent.getContext()).hasPermanentMenuKey())) 
        	corner_round = parent.getResources().getDimensionPixelSize(R.dimen.vote_corner_round_small);
        
        MingleUser pop_user = app.getMingleUser(pop_user_uid);
    	holder.user_name.setText(pop_user.getName());
    	holder.user_pic.setImageDrawable(ImageRounder.getVoteRoundedDrawable((Activity) context, pop_user.getPic(-1), corner_round, 60, 67));
    	

    	int rank = pop_user.getRank();
    	holder.user_rank.setText(String.valueOf(rank)+"위");
    	if(rank <= 1) holder.user_rank.setTextColor(app.getResources().getColor(R.color.gold));
    	else if(rank <= 2) holder.user_rank.setTextColor(app.getResources().getColor(R.color.silver));
    	else if(rank <= 3) holder.user_rank.setTextColor(app.getResources().getColor(R.color.bronze));
    	else holder.user_rank.setTextColor(app.getResources().getColor(R.color.dark_gray));
    	
    	final String profile_uid = pop_user_uid;
    	holder.pop_layout.setOnClickListener(new OnClickListener()
        {
        	@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
        		Intent profile_intent = new Intent(context, ProfileActivity.class);
                profile_intent.putExtra(ProfileActivity.PROFILE_UID, profile_uid);
                profile_intent.putExtra(ProfileActivity.PROFILE_TYPE, "popular");
                context.startActivity(profile_intent);
			}
        });
    	
    	if(pop_user.getSex().equals("M")) holder.number_icon.setImageResource(R.drawable.male_numberpic);
        else holder.number_icon.setImageResource(R.drawable.female_numberpic);
    	holder.num_pic.setImageResource(app.memberNumRsId(pop_user.getNum(),pop_user.getSex()));
    	holder.user_dist.setText(Float.toString(pop_user.getDistance())+"km");
    	
        /*ArrayList<String> rank_list = (ArrayList<String>) data.get(position);
        String female_uid = rank_list.get(0);
        String male_uid = rank_list.get(1);
        int corner_round = parent.getResources().getDimensionPixelSize(R.dimen.vote_corner_round);
        if(!(Integer.valueOf(android.os.Build.VERSION.SDK_INT) >= 14 && ViewConfiguration.get(parent.getContext()).hasPermanentMenuKey())) 
        	corner_round = parent.getResources().getDimensionPixelSize(R.dimen.vote_corner_round_small);
	
        if(!female_uid.equals("")){
        	MingleUser female_pop = app.getMingleUser(female_uid);
        	holder.female_name.setText(female_pop.getName());
        	holder.female_pic.setImageDrawable(ImageRounder.getVoteRoundedDrawable((Activity) context, female_pop.getPic(-1), corner_round, 60, 67));
        	
        	final String profile_uid = female_uid;
        	holder.female_layout.setOnClickListener(new OnClickListener()
            {

            	@Override
    			public void onClick(View arg0) {
    				// TODO Auto-generated method stub
            		Intent profile_intent = new Intent(context, ProfileActivity.class);
                    profile_intent.putExtra(ProfileActivity.PROFILE_UID, profile_uid);
                    profile_intent.putExtra(ProfileActivity.PROFILE_TYPE, "popular");
                    context.startActivity(profile_intent);
    			}
            });
        }
        if(!male_uid.equals("")){
        	MingleUser male_pop = app.getMingleUser(male_uid);
        	holder.male_name.setText(male_pop.getName());
        	holder.male_pic.setImageDrawable(ImageRounder.getVoteRoundedDrawable((Activity) context, male_pop.getPic(-1), corner_round, 60, 67));
        	final String profile_uid = male_uid;
        	holder.male_layout.setOnClickListener(new OnClickListener()
            {

            	@Override
    			public void onClick(View arg0) {
    				// TODO Auto-generated method stub
            		Intent profile_intent = new Intent(context, ProfileActivity.class);
                    profile_intent.putExtra(ProfileActivity.PROFILE_UID, profile_uid);
                    profile_intent.putExtra(ProfileActivity.PROFILE_TYPE, "popular");
                    context.startActivity(profile_intent);
    			}
            });
        }*/
        return row;

    }

    
    static class NewsHolder {
    	ImageView number_icon;
    	ImageView num_pic;
    	TextView user_dist;
    	RelativeLayout pop_layout;
    	//FrameLayout male_layout;
    	TextView user_rank;
    	//ImageView male_rank;
  	  	ImageView user_pic;
  	  	TextView user_name;
  	  	//ImageView male_pic;
  	  	//TextView male_name;
    }
}
