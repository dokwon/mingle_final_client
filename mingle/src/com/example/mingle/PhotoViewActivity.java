package com.example.mingle;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

public class PhotoViewActivity extends Activity {
    private ViewFlipper viewFlipper;
    private float lastX;
    private int photo_num;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
                 super.onCreate(savedInstanceState);
                 requestWindowFeature(Window.FEATURE_NO_TITLE);
                 getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                     WindowManager.LayoutParams.FLAG_FULLSCREEN);
                 setContentView(R.layout.activity_photo_view);
                 viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
                 
                 Intent intent = getIntent();
                 int index = intent.getExtras().getInt(AllChatAdapter.INDEX);
                 ChattableUser cu = ((MingleApplication) this.getApplication()).currUser.getChattableUser(index);
                 photo_num = cu.getPhotoNum();
                 uid = cu.getUid();
                 
                 LayoutInflater inflater = getLayoutInflater();
                 for(int i = 0; i < photo_num; i++){
                	 LinearLayout single_photo_layout = (LinearLayout) inflater
                         .inflate(R.layout.single_photo, null);
                	 ImageView photo_view = (ImageView) single_photo_layout.findViewById(R.id.photoView);
                	 Drawable photo_drawable = cu.getPic(i);
                	 if(photo_drawable == null) photo_view.setImageDrawable((Drawable) getResources().getDrawable(R.drawable.ic_launcher));
                	 else photo_view.setImageDrawable(photo_drawable);
                	 
                	 viewFlipper.addView(single_photo_layout);
                 }
    }

    
                
    // Method to handle touch event like left to right swap and right to left swap
    public boolean onTouchEvent(MotionEvent touchevent) 
    {
                 switch (touchevent.getAction())
                 {
                        // when user first touches the screen to swap
                         case MotionEvent.ACTION_DOWN: 
                         {
                             lastX = touchevent.getX();
                             break;
                        }
                         case MotionEvent.ACTION_UP: 
                         {
                             float currentX = touchevent.getX();
                             
                             // if left to right swipe on screen
                             if (lastX < currentX) 
                             {
                                  // If no more View/Child to flip
                                 if (viewFlipper.getDisplayedChild() == 0)
                                     break;
                                 
                                 // set the required Animation type to ViewFlipper
                                 // The Next screen will come in form Left and current Screen will go OUT from Right 
                                 viewFlipper.setInAnimation(this, R.anim.in_from_left);
                                 viewFlipper.setOutAnimation(this, R.anim.out_to_right);
                              
                                 // Show the next Screen
                                 viewFlipper.showNext();
                             }
                             
                             // if right to left swipe on screen
                             if (lastX > currentX)
                             {
                                 if (viewFlipper.getDisplayedChild() == 1)
                                     break;
                                 // set the required Animation type to ViewFlipper
                                 // The Next screen will come in form Right and current Screen will go OUT from Left 
                                 viewFlipper.setInAnimation(this, R.anim.in_from_right);
                                 viewFlipper.setOutAnimation(this, R.anim.out_to_left);
                                 getNewImage(viewFlipper.getDisplayedChild()/2+1);
                                 // Show The Previous Screen
                                 viewFlipper.showPrevious();
                             }
                             break;
                         }
                 }
                 return false;
    }
    
    public void getNewImage(int photo_index){
    	System.out.println("photo index: " + photo_index);
    	if(photo_index < 0 || photo_index >= photo_num) return;
    	
    	MingleUser currUser = ((MingleApplication) getApplication()).currUser;
		ChattableUser cu = currUser.getChattableUser(uid);
   	 	Drawable photo_drawable = cu.getPic(photo_index);
   	 	if(photo_drawable != null) return;
    	
    	String url = "http://ec2-54-178-214-176.ap-northeast-1.compute.amazonaws.com:8080/photos/";
        url += uid;
        url += "/photo_" + String.valueOf(photo_index+1) + ".png";
        System.out.println(url);
        new ImageDownloader(getApplication(), url, photo_index).execute();
    }
    
private class ImageDownloader extends AsyncTask<Void, Void, Void> {
	  	
	  	private Application curApp;
	  	private String url;
	  	private int photo_index;
	  	private Bitmap bm;
	  	
	  	public ImageDownloader(Application app, String url, int photo_index) {
	  		curApp = app;
	  		this.url = url;
	  		this.photo_index = photo_index;
	  	}
	  	
			@Override
			protected Void doInBackground(Void... params) {

				if (isCancelled()) {
					return null;
				}

				bm = ((MingleApplication) curApp).connectHelper.getBitmapFromURL(url);
				

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				
				LinearLayout curr_layout = (LinearLayout) viewFlipper.getCurrentView();
				ImageView photo_view = (ImageView) curr_layout.findViewById(R.id.photoView);
				
				MingleUser currUser = ((MingleApplication) curApp).currUser;
				ChattableUser cu = currUser.getChattableUser(uid);
				cu.addpic((Drawable) new BitmapDrawable(getResources(),bm));
           	 	Drawable photo_drawable = cu.getPic(photo_index);
           	 	if(photo_drawable == null) photo_view.setImageDrawable((Drawable) getResources().getDrawable(R.drawable.ic_launcher));
           	 	else photo_view.setImageDrawable(photo_drawable);
				super.onPostExecute(result);
			}

			@Override
			protected void onCancelled() {
		    }
  }

}