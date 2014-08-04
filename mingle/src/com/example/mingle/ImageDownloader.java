package com.example.mingle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

public class ImageDownloader extends AsyncTask<Void, Void, Void> {
  	
  	private Context context;
  	private String url;
  	private String uid;
  	private int pic_index;
  	private Bitmap bm;
  	
  	public ImageDownloader(Context context, String uid, int pic_index) {
  		this.context = context;
  		String temp_url = "http://ec2-54-178-214-176.ap-northeast-1.compute.amazonaws.com:8080/photos/";
  		temp_url += uid;
  		if(pic_index < 0) temp_url += "/thumb.png";
  		else temp_url += "/photo_" + String.valueOf(pic_index+1) + ".png";
  		System.out.println(temp_url);
  		this.url = temp_url;
  		this.uid = uid;
  		this.pic_index = pic_index;
  	}
  	
		@Override
		protected Void doInBackground(Void... params) {

			if (isCancelled()) {
				return null;
			}

			bm = ((MingleApplication) context).connectHelper.getBitmapFromURL(url);

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			MingleApplication app= (MingleApplication) context;
			MingleUser user = app.getMingleUser(uid);

			user.setPic(pic_index, (Drawable) new BitmapDrawable(context.getResources(),bm));
			
			if(context instanceof HuntActivity){
				((HuntActivity)context).listsUpdate();
				((HuntActivity)context).popListUpdate();
			}
			
			if(context instanceof ProfileActivity){
				((ProfileActivity)context).updateView(pic_index);
			}

			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
	    }
}