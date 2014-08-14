package com.example.mingle;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

public class IntroActivity extends Activity {

	private ViewFlipper viewFlipper;
    private float lastX;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);
		
	    viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
	    LayoutInflater inflater = getLayoutInflater();
        for(int i = 0; i < 3; i++){
       	 	LinearLayout single_photo_layout = (LinearLayout)inflater.inflate(R.layout.single_photo, null);
       	 	ImageView photo_view = (ImageView) single_photo_layout.findViewById(R.id.photoView);
       	 	Drawable photo_drawable = getResources().getDrawable(R.drawable.launch_image);
       	 	photo_view.setImageDrawable(photo_drawable);
       	 
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
                              
                                 // Show the previous Screen
                                 viewFlipper.showPrevious();
                             }
                             
                             // if right to left swipe on screen
                             if (lastX > currentX)
                             {
                                 if (viewFlipper.getDisplayedChild() == 2)
                                     break;
                                 // set the required Animation type to ViewFlipper
                                 // The Next screen will come in form Right and current Screen will go OUT from Left 
                                 viewFlipper.setInAnimation(this, R.anim.in_from_right);
                                 viewFlipper.setOutAnimation(this, R.anim.out_to_left);
                                 // Show The Next Screen
                                 viewFlipper.showNext();
                             }
                             break;
                         }
                 }
                 return false;
    }
}
