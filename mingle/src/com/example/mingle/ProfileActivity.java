package com.example.mingle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class ProfileActivity extends Activity {
	public final static String PROFILE_UID = "com.example.mingle.PROFILE_UID";	//Intent data to pass on when new Profile Activity started
	public final static String PROFILE_TYPE = "com.example.mingle.PROFILE_TYPE";	//Intent data to pass on when new Profile Activity started

	private ViewFlipper viewFlipper;
    private float lastX;
    private int photo_num;
    private MingleUser user;
    private Typeface koreanTypeFace;
    
    @SuppressLint("NewApi")
	private void resizeProfilePic() {
    	final Display display = getWindowManager().getDefaultDisplay();
    
    	@SuppressWarnings("deprecation")
		int height = display.getHeight();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
        	final Point point = new Point();
        	getWindowManager().getDefaultDisplay().getSize(point);
            height = point.y;
        } 
    	RelativeLayout layout = (RelativeLayout) findViewById(R.id.profile_elems);
    	
    	RelativeLayout.LayoutParams params =(RelativeLayout.LayoutParams) layout.getLayoutParams();
    	params.height = (int)(height * 0.6);
    	layout.setLayoutParams(params);
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
    	setIntent(intent);
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		//check if custom title is supported BEFORE setting the content view!
	    boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	        
	    setContentView(R.layout.activity_profile);
	        
	    if(customTitleSupported) getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.profile_title_bar);

	    //Support Korean Language
	    koreanTypeFace = Typeface.createFromAsset(getAssets(), "fonts/UnGraphic.ttf");
	             
         LocalBroadcastManager.getInstance(this).registerReceiver(imageUpdateReceiver,
        		  new IntentFilter(ImageDownloader.UPDATE_PROFILE));
         
         LocalBroadcastManager.getInstance(this).registerReceiver(voteResultReceiver,
        		  new IntentFilter(HttpHelper.HANDLE_VOTE_RESULT));
         
         LocalBroadcastManager.getInstance(this).registerReceiver(httpErrorReceiver,
         		  new IntentFilter(HttpHelper.HANDLE_HTTP_ERROR));
	}
	
	@Override
	protected void onResume(){
		 super.onResume();
		 Intent intent = getIntent();
         String uid = intent.getExtras().getString(ProfileActivity.PROFILE_UID);
         String type = intent.getExtras().getString(ProfileActivity.PROFILE_TYPE);
         
         MingleApplication app = ((MingleApplication) this.getApplication());
         ((NotificationManager)this.getSystemService(NOTIFICATION_SERVICE)).cancel(GcmIntentService.getNotificationId(uid));

         if(type.equals("preview") || type.equals("setting")) user = app.getMyUser();
         else user = app.getMingleUser(uid);
         photo_num = user.getPhotoNum();
         
         viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
         LayoutInflater inflater = getLayoutInflater();
         for(int i = 0; i < photo_num; i++){
        	 LinearLayout single_photo_layout = (LinearLayout) inflater
                 .inflate(R.layout.single_photo, null);
        	 ImageView photo_view = (ImageView) single_photo_layout.findViewById(R.id.photoView);
        	 Drawable photo_drawable = user.getPic(i);
        	 photo_view.setImageDrawable(photo_drawable);
        	 
        	 viewFlipper.addView(single_photo_layout);
         }
         
         ImageView num_view = (ImageView) findViewById(R.id.profile_member_num);
         num_view.setImageResource(app.memberNumRsId(user.getNum()));
         TextView name_view = (TextView) findViewById(R.id.profile_user_name);
         name_view.setTypeface(koreanTypeFace);
         name_view.setText(user.getName());
         TextView dist_view = (TextView) findViewById(R.id.profile_user_dist);
         dist_view.setText(Float.toString(user.getDistance())+"km");

         Button vote_button = (Button) findViewById(R.id.vote_button);
         Button chat_button = (Button) findViewById(R.id.chat_button);
         Button edit_profile_button = (Button) findViewById(R.id.edit_profile_button);
        
         if(type.equals("preview") || type.equals("setting")){
        	 vote_button.setVisibility(View.GONE);
        	 dist_view.setVisibility(View.GONE);
         } else {
        	 getNewImage(0);
         }
         if (type.equals("popular")) {
        	 vote_button.setVisibility(View.GONE);
         }
         if(!type.equals("candidate") || type.equals("choice")){
        	 chat_button.setVisibility(View.GONE);
         }
         if(!type.equals("setting")){
        	 edit_profile_button.setVisibility(View.GONE);
         }
         initializePhotoIndicators();
         //resizeProfilePic();
	}
	
	
	private int current_viewing_pic_index = 0;
	
	
	private void initializePhotoIndicators() { 
		if(photo_num == 1) return;
		LinearLayout photoCounterWrapper = (LinearLayout) findViewById(R.id.photo_indicators);
		for(int i = 0; i < photo_num; i++) {
            ImageView indicator = new ImageView(this);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(R.dimen.indicator_size,R.dimen.indicator_size);
            indicator.setLayoutParams(params);
            indicator.setPadding(8, 0, 8, 15);
            if(i == 0) {
            	indicator.setImageResource(R.drawable.profile_photo_current);
            } else {
            	indicator.setImageResource(R.drawable.profile_photo_notcurrent);
            }
            photoCounterWrapper.addView(indicator);
        }
	}
	
	private void updatePhotoIndicators(int changeInIndex) {
		if(changeInIndex == 0) return; 
		LinearLayout photoCounterWrapper = (LinearLayout) findViewById(R.id.photo_indicators);
		ImageView curIndicator = (ImageView) photoCounterWrapper.getChildAt(current_viewing_pic_index);
		ImageView newIndicator = (ImageView) photoCounterWrapper.getChildAt(current_viewing_pic_index + changeInIndex);
		curIndicator.setImageResource(R.drawable.profile_photo_notcurrent);
		newIndicator.setImageResource(R.drawable.profile_photo_current);
		current_viewing_pic_index += changeInIndex;
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
                                 if (viewFlipper.getDisplayedChild() == photo_num-1)
                                     break;
                                 updatePhotoIndicators(1);
                                 // set the required Animation type to ViewFlipper
                                 // The Next screen will come in form Right and current Screen will go OUT from Left 
                                 viewFlipper.setInAnimation(this, R.anim.in_from_right);
                                 viewFlipper.setOutAnimation(this, R.anim.out_to_left);
                                 getNewImage(viewFlipper.getDisplayedChild()+1);
                                 // Show The Next Screen
                                 viewFlipper.showNext();
                             }
                             break;
                         }
                 }
                 return false;
    }
    
    public void getNewImage(int photo_index){
    	if(photo_index < 0 || photo_index >= photo_num) return;
    	
		if(!user.isPicAvail(photo_index)){
			System.out.println("download image!");
			new ImageDownloader(getApplication(), user.getUid(), photo_index).execute();
		}
	}
    
    private BroadcastReceiver imageUpdateReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    	   // Extract data included in the Intent
    	   int pic_index = intent.getExtras().getInt(ImageDownloader.PIC_INDEX);
    	   updateView(pic_index);
    	  }
    };
    
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
    	app.dbHelper.insertNewUID(user.getUid(), user.getNum(), user.getName(), user.getDistance());
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
    
    /* Broadcast Receiver for notification of vote result*/
	  private BroadcastReceiver voteResultReceiver = new BroadcastReceiver() {
	    	@Override
	    	public void onReceive(Context context, Intent intent) {
	    		String result = intent.getExtras().getString(HttpHelper.VOTE_RESULT);
	    		if(result.equals("success")){
		    		Toast.makeText(getApplicationContext(), "Vote success!", Toast.LENGTH_SHORT).show();
	    		} else {
		    		Toast.makeText(getApplicationContext(), "Can't vote yet!", Toast.LENGTH_SHORT).show();
	    		}
	    	}
	  };
	  
	  /* Broadcast Receiver for notification of http error*/
	  private BroadcastReceiver httpErrorReceiver = new BroadcastReceiver() {
	    	@Override
	    	public void onReceive(Context context, Intent intent) {
	    		Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
	    	}
	  };
    
	    /* If current activity is called from GCM not Hunt, start Hunt */
	    @Override
	    public void onBackPressed(){
	    	if(this.isTaskRoot()){
	    		Intent huntIntent = new Intent(this, HuntActivity.class);
	    		startActivity(huntIntent);
	    	}
	    	super.onBackPressed();
	    }
	  
	  @Override
	  public void onDestroy(){
		  LocalBroadcastManager.getInstance(this).unregisterReceiver(imageUpdateReceiver);
		  LocalBroadcastManager.getInstance(this).unregisterReceiver(httpErrorReceiver);

		  super.onDestroy();
	  }
}
