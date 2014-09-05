package ly.nativeapp.mingle;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

public class ImageDownloader extends AsyncTask<Void, Integer, Void> {
	public final static String UPDATE_HUNT = "ly.nativeapp.mingle.UPDATE_HUNT";			//Indicator of image download complete for Hunt activity
	public final static String UPDATE_PROFILE = "ly.nativeapp.mingle.UPDATE_PROFILE";	//Indicator of image download complete for Profile activtiy
	public final static String PIC_INDEX = "ly.nativeapp.mingle.PIC_INDEX";				//Intent data to pass on for image download index

  	private Context context;
  	private String url;				//Image download URL
  	private String uid;				//UID of image owner
  	private int pic_index;			//Index of image
  	private Bitmap bm;
  	
  	public ImageDownloader(Context context, String uid, int pic_index) {
  		this.context = context;
  		String temp_url = "http://ec2-54-64-20-181.ap-northeast-1.compute.amazonaws.com:8000/photos/";
  		temp_url += uid;
  		if(pic_index < 0) temp_url += "/thumb.png";
  		else temp_url += "/photo_" + String.valueOf(pic_index+1) + ".png";
  		this.url = temp_url;
  		this.uid = uid;
  		this.pic_index = pic_index;
  	}
  	
  		@Override
  		protected void onPreExecute() {
  			super.onPreExecute();
  		}
  	
		@Override
		protected Void doInBackground(Void... params) {
			if (isCancelled()) {
				return null;
			}
			
			bm = getBitmapFromURL(url);

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			MingleApplication app= (MingleApplication) context;
			MingleUser user = app.getMingleUser(uid);

			//Save Image
			user.setPic(pic_index, (Drawable) new BitmapDrawable(context.getResources(),bm));

			//update lists on complete
			Intent dispatcher = new Intent(app, HuntActivity.class);
			dispatcher.putExtra(PIC_INDEX,pic_index);
			dispatcher.setAction(UPDATE_HUNT);
			LocalBroadcastManager.getInstance(app).sendBroadcast(dispatcher); 
			
			//update profile on complete
			dispatcher = new Intent(app, ProfileActivity.class);
			dispatcher.putExtra(PIC_INDEX,pic_index);
			dispatcher.setAction(UPDATE_PROFILE);
			LocalBroadcastManager.getInstance(app).sendBroadcast(dispatcher); 
			

			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
	    }
		
		//Download image from URL and return it as a Bitmap
		private Bitmap getBitmapFromURL(String link) {
	        try {
	            URL url = new URL(link);
	            HttpURLConnection connection = (HttpURLConnection) url
	                    .openConnection();
	            connection.setDoInput(true);
	            connection.connect();
	            InputStream input = connection.getInputStream();
	            Bitmap myBitmap = BitmapFactory.decodeStream(input);

	            return myBitmap;

	        } catch (IOException e) {
	            e.printStackTrace();
	            return null;
	        }
	    }
}
