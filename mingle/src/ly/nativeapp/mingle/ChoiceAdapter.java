package ly.nativeapp.mingle;

import java.util.List;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
      	  holder.msg_view.setTypeface(app.koreanTypeFace);
      	  holder.user_pic=(RoundedImageView)row.findViewById(R.id.sender_image);
      	  holder.user_name = (TextView)row.findViewById(R.id.sender_name);
      	  holder.new_msg_num = (TextView)row.findViewById(R.id.new_msg_num);
          holder.user_name.setTypeface(app.koreanBoldTypeFace);
      	  holder.num_pic = (ImageView)row.findViewById(R.id.choice_member_num);
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
        int new_msg_num = choice.getNewMsgNum();
        if(new_msg_num > 0) holder.new_msg_num.setText(String.valueOf(new_msg_num));
        else holder.new_msg_num.setVisibility(View.GONE);
        holder.user_pic.setImageDrawable(choice.getPic(-1));
        

  	  holder.num_pic.setImageResource(app.memberNumRsId(choice.getNum()));
  	  
        
        return row;

    }

    
    static class NewsHolder{
    	ImageView num_pic;
  	  TextView msg_view;
  	  TextView user_name;
  	  TextView new_msg_num;
  	  RoundedImageView user_pic;
    }
}
