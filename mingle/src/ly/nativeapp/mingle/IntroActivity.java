package ly.nativeapp.mingle;

import java.util.ArrayList;


import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ViewFlipper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class IntroActivity extends Activity {

	private ViewFlipper viewFlipper;
	private ArrayList<Integer> intro_arr;
    private float lastX;
    private Button startButton;
	
    
    private ArrayList<Bitmap> intro_bitmaps;
	
	private int current_viewing_pic_index;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);
		
	    viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
	    
	    
	    intro_arr = new ArrayList<Integer>();
	    intro_arr.add(R.drawable.intro1);
	    intro_arr.add(R.drawable.intro2);
	    intro_arr.add(R.drawable.intro3);
	    intro_arr.add(R.drawable.intro4);

	    intro_bitmaps = new ArrayList<Bitmap>();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		LayoutInflater inflater = getLayoutInflater();
		final BitmapFactory.Options options = new BitmapFactory.Options();
       	
       	current_viewing_pic_index = 0;
	    LinearLayout indicatorWrapper = (LinearLayout) findViewById(R.id.intro_indicators);
       	
       	
       	for(int i = 0; i < intro_arr.size(); i++){
       		addIndicator(i, indicatorWrapper);
       		RelativeLayout single_photo_layout = (RelativeLayout)inflater.inflate(R.layout.single_intro, null);
       		ImageView photo_view = (ImageView) single_photo_layout.findViewById(R.id.introImage);
       		options.inJustDecodeBounds = true;
       		BitmapFactory.decodeResource(getResources(), intro_arr.get(i), options);
       		options.inSampleSize = 2;
       		options.inJustDecodeBounds = false;
       		Bitmap bm = BitmapFactory.decodeResource(getResources(), intro_arr.get(i), options);
       		photo_view.setImageBitmap(bm);
       		intro_bitmaps.add(bm);
       		
       		startButton = (Button) single_photo_layout.findViewById(R.id.startButton);
    		if(i < intro_arr.size()-1) startButton.setVisibility(View.GONE);
    		else startButton.setVisibility(View.VISIBLE);
    		
       		viewFlipper.addView(single_photo_layout);
       	}
	}

	@Override
	public void onPause(){
		for(int i = 0; i < intro_bitmaps.size(); i++){
			intro_bitmaps.get(i).recycle();
		}
		intro_bitmaps.clear();
		super.onPause();
	}
	private void updatePhotoIndicators(int changeInIndex) {
		if(changeInIndex == 0) return; 
		LinearLayout indicatorWrapper = (LinearLayout) findViewById(R.id.intro_indicators);
		ImageView curIndicator = (ImageView) indicatorWrapper.getChildAt(current_viewing_pic_index);
		ImageView newIndicator = (ImageView) indicatorWrapper.getChildAt(current_viewing_pic_index + changeInIndex);
		curIndicator.setImageResource(R.drawable.profile_photo_notcurrent);
		newIndicator.setImageResource(R.drawable.profile_photo_current);
		current_viewing_pic_index += changeInIndex;
	}
	
	private void addIndicator(int i, LinearLayout indicatorWrapper) {
		ImageView indicator = new ImageView(this);
        int height = (int) getResources().getDimension(R.dimen.indicator_size);
        int width = (int) getResources().getDimension(R.dimen.indicator_size);
        indicator.setLayoutParams(new ViewGroup.LayoutParams(width, height));
        
        indicator.setPadding(8, 0, 8, 0);
        if(i == current_viewing_pic_index) {
        	indicator.setImageResource(R.drawable.profile_photo_current);
        } else {
        	indicator.setImageResource(R.drawable.profile_photo_notcurrent);
        }
        indicatorWrapper.addView(indicator);
	}
	
	public void removeGuide(View view){
		finish();
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
                            	 int curr_pos = viewFlipper.getDisplayedChild();
                            	 if (curr_pos == intro_arr.size()-1) startButton.setVisibility(View.GONE);
                                 if (curr_pos == 0)
                                     break;
                            	 updatePhotoIndicators(-1);

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
                            	 int curr_pos = viewFlipper.getDisplayedChild();
                            	 if (curr_pos == intro_arr.size()-2) startButton.setVisibility(View.VISIBLE);
                                 if (curr_pos == intro_arr.size()-1)
                                     break;
                            	 updatePhotoIndicators(1);
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
