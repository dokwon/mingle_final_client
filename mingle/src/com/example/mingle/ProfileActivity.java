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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class ProfileActivity extends Activity {
	public final static String USER_UID = "com.example.mingle.USER_SEL";	//Intent data to pass on when new Chatroom Activity started

	private ViewFlipper viewFlipper;
    private float lastX;
    private int photo_num;
    private ChattableUser cu;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_profile);
         viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
         
         Intent intent = getIntent();
         String uid = intent.getExtras().getString(CandidateAdapter.PROFILE_UID);
         cu = ((MingleApplication) this.getApplication()).currUser.getUser(uid);
         photo_num = cu.getPhotoNum();
         uid = cu.getUid();
         
         ((MingleApplication) this.getApplication()).connectHelper.changeContext(this);
         
         LayoutInflater inflater = getLayoutInflater();
         for(int i = 0; i < photo_num; i++){
        	 LinearLayout single_photo_layout = (LinearLayout) inflater
                 .inflate(R.layout.single_photo, null);
        	 ImageView photo_view = (ImageView) single_photo_layout.findViewById(R.id.photoView);
        	 Drawable photo_drawable = cu.getPic(i);
        	 photo_view.setImageDrawable(photo_drawable);
        	 
        	 viewFlipper.addView(single_photo_layout);
         }
         
         TextView num_view = (TextView) findViewById(R.id.profile_user_num);
         num_view.setText(String.valueOf(cu.getNum()));
         TextView name_view = (TextView) findViewById(R.id.profile_user_name);
         name_view.setText(cu.getName());
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
    	
		if(!cu.isPicAvail(photo_index)){
			System.out.println("download image!");
			((MingleApplication) this.getApplication()).connectHelper.downloadPic(getApplication(), cu.getUid(), photo_index, false);
		}
	}
    
    public void updateView(int index){
    	LinearLayout curr_layout = (LinearLayout) viewFlipper.getCurrentView();
		ImageView photo_view = (ImageView) curr_layout.findViewById(R.id.photoView);

		Drawable pic_to_update = cu.getPic(index);
		
    	photo_view.setImageDrawable(pic_to_update);
    }
    
    public void voteUser(View v){
    	System.out.println("vote user");
    	MingleUser curr_user = ((MingleApplication) this.getApplication()).currUser;
    	String curr_uid = cu.getUid();
    	if(!curr_user.alreadyVoted(curr_uid)){
    		curr_user.addVotedUser(curr_uid);
    		((MingleApplication) this.getApplication()).connectHelper.voteUser(curr_uid);
    	}
    }
    
    public void startChat(View v){
    	System.out.println("start chat");
    	MingleUser curr_user = ((MingleApplication) this.getApplication()).currUser;
    	int position = curr_user.getChattableUserPos(cu.getUid());
    	if(position >= 0) {
    		curr_user.switchChattableToChatting(position);
    	}
         // Create chatroom in local sqlite
         //((MingleApplication) parent.getApplication()).dbHelper.insertNewUID(chat_user_uid);
                 
        Intent chat_intent = new Intent(this, ChatroomActivity.class);
        chat_intent.putExtra(USER_UID, cu.getUid());
        startActivity(chat_intent);
    }
}
