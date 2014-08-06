package com.example.mingle;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class ProfileActivity extends Activity {
	public final static String PROFILE_UID = "com.example.mingle.PROFILE_UID";	//Intent data to pass on when new Profile Activity started
	public final static String PROFILE_TYPE = "com.example.mingle.PROFILE_TYPE";	//Intent data to pass on when new Profile Activity started

	private ViewFlipper viewFlipper;
    private float lastX;
    private int photo_num;
    private MingleUser user;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		//check if custom title is supported BEFORE setting the content view!
	    boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	        
	    setContentView(R.layout.activity_profile);
	        
	    if(customTitleSupported) getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.profile_title_bar);

	    viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
         
         Intent intent = getIntent();
         String uid = intent.getExtras().getString(ProfileActivity.PROFILE_UID);
         String type = intent.getExtras().getString(ProfileActivity.PROFILE_TYPE);
         System.out.println("profile activity: uid="+uid+" type="+type);
         MingleApplication app = ((MingleApplication) this.getApplication());
         if(type.equals("preview") || app.getMyUser().getUid().equals(uid)) user = app.getMyUser();
         else user = app.getMingleUser(uid);
         photo_num = user.getPhotoNum();
                  
         LayoutInflater inflater = getLayoutInflater();
         for(int i = 0; i < photo_num; i++){
        	 LinearLayout single_photo_layout = (LinearLayout) inflater
                 .inflate(R.layout.single_photo, null);
        	 ImageView photo_view = (ImageView) single_photo_layout.findViewById(R.id.photoView);
        	 Drawable photo_drawable = user.getPic(i);
        	 photo_view.setImageDrawable(photo_drawable);
        	 
        	 viewFlipper.addView(single_photo_layout);
         }
         
         TextView num_view = (TextView) findViewById(R.id.profile_user_num);
         num_view.setText(String.valueOf(user.getNum()));
         TextView name_view = (TextView) findViewById(R.id.profile_user_name);
         name_view.setText(user.getName());
         

         Button vote_button = (Button) findViewById(R.id.vote_button);
         Button chat_button = (Button) findViewById(R.id.chat_button);
         Button edit_profile_button = (Button) findViewById(R.id.edit_profile_button);
         
         if(type.equals("preview") || type.equals("setting")){
        	 vote_button.setVisibility(View.GONE);
         }
         if(!type.equals("candidate")){
        	 chat_button.setVisibility(View.GONE);
         }
         if(!type.equals("setting")){
        	 edit_profile_button.setVisibility(View.GONE);
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
                                 if (viewFlipper.getDisplayedChild() == photo_num)
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
    	
		if(!user.isPicAvail(photo_index)){
			System.out.println("download image!");
			new ImageDownloader(getApplication(), user.getUid(), photo_index).execute();
		}
	}
    
    public void updateView(int index){
    	LinearLayout curr_layout = (LinearLayout) viewFlipper.getCurrentView();
		ImageView photo_view = (ImageView) curr_layout.findViewById(R.id.photoView);

		Drawable pic_to_update = user.getPic(index);
		
    	photo_view.setImageDrawable(pic_to_update);
    }
    
    public void voteUser(View v){
    	String curr_uid = user.getUid();
    	if(!user.alreadyVoted()){
    		user.setVoted();
    		((MingleApplication) this.getApplication()).connectHelper.voteUser(curr_uid);
    	}
    }
    
    public void startChat(View v){
    	System.out.println("start chat");
    	MingleApplication app = ((MingleApplication) this.getApplication());
    	int candidate_pos = app.getCandidatePos(user.getUid());
    	if(candidate_pos >= 0) {
    		app.switchCandidateToChoice(candidate_pos);
    	}
    	app.dbHelper.insertNewUID(user.getUid(), user.getNum(), user.getName(), 0, 0, 0);
         // Create chatroom in local sqlite
         //((MingleApplication) parent.getApplication()).dbHelper.insertNewUID(chat_user_uid);
                 
        Intent chat_intent = new Intent(this, ChatroomActivity.class);
        chat_intent.putExtra(ChatroomActivity.USER_UID, user.getUid());
        startActivity(chat_intent);
    }
    
    public void modifyProfile(View v){
    	Intent i = new Intent(this, MainActivity.class);
        i.putExtra(MainActivity.MAIN_TYPE, "update");
        startActivity(i);
    }
}
