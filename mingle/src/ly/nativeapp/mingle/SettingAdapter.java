package ly.nativeapp.mingle;

import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SettingAdapter extends ArrayAdapter {
	 
    List data;
    Context context;
    int layoutResID;
    MingleApplication app;

    public SettingAdapter(Context context, int layoutResourceId,List data, MingleApplication currApp) {
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

      	  holder.setting_option = (TextView)row.findViewById(R.id.setting_option);
      	  
      	  row.setTag(holder);
        } else {
      	  holder = (NewsHolder)row.getTag();
        }

        String option = (String) data.get(position);
        holder.setting_option.setText(option);

        return row;

    }

    
    static class NewsHolder{
  	  TextView setting_option;
    }
}