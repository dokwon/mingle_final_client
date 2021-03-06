package ly.nativeapp.mingle;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.NotificationManager;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class ProfileActivity extends Activity implements ActionBar.TabListener {
	public final static String PROFILE_UID = "ly.nativeapp.mingle.PROFILE_UID";	//Intent data to pass on when new Profile Activity started
	public final static String PROFILE_TYPE = "ly.nativeapp.mingle.PROFILE_TYPE";	//Intent data to pass on when new Profile Activity started

	private ViewFlipper viewFlipper;
    private float lastX;
    private int photo_num;
    private MingleUser user;
    private Typeface koreanTypeFace;
    private MingleApplication app;
    
    @Override
    protected void onNewIntent(Intent intent) {
    	super.onNewIntent(intent);
    	setIntent(intent);
    }
   
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		
	    setContentView(R.layout.activity_profile);
	    ActionbarController.customizeActionBar(R.layout.profile_title_bar, this, -30, 0);
	    
	    //Support Korean Language
	    koreanTypeFace = Typeface.createFromAsset(getAssets(), "fonts/mingle-font-regular.otf");
	    viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
         
         LocalBroadcastManager.getInstance(this).registerReceiver(imageUpdateReceiver,
        		  new IntentFilter(ImageDownloader.UPDATE_PROFILE));
         
         LocalBroadcastManager.getInstance(this).registerReceiver(voteResultReceiver,
        		  new IntentFilter(HttpHelper.HANDLE_VOTE_RESULT));
         
         LocalBroadcastManager.getInstance(this).registerReceiver(httpErrorReceiver,
         		  new IntentFilter(HttpHelper.HANDLE_HTTP_ERROR));
         
         LocalBroadcastManager.getInstance(this).registerReceiver(rankInfoReceiver,
         		  new IntentFilter(HttpHelper.SET_RANK_INFO));
         
         
         final LinearLayout prof_elems = (LinearLayout) findViewById(R.id.profile_elems);
         ViewTreeObserver vto = prof_elems.getViewTreeObserver(); 
         vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() { 
			
			@Override
			public void onGlobalLayout() {
				RelativeLayout shadow = (RelativeLayout) findViewById(R.id.shadow_wrapper);
		         shadow.getLayoutParams().height = prof_elems.getMeasuredHeight() + 20;
			} 
         });
        
	}
	
	private int corner_round; 
	
	
	public void onResume() {
		super.onResume();
		
		
		Intent intent = getIntent();
        String uid = intent.getExtras().getString(ProfileActivity.PROFILE_UID);
        String type = intent.getExtras().getString(ProfileActivity.PROFILE_TYPE);
        
        app = ((MingleApplication) this.getApplication());
        corner_round = (int) app.getResources().getDimension(R.dimen.profile_corner_round);
        int id = GcmIntentService.getNotificationId(uid);
        if(id != -1)
       	 		((NotificationManager)this.getSystemService(NOTIFICATION_SERVICE)).cancel(id);
        
        if(type.equals("preview") || type.equals("setting") || uid.equals(app.getMyUser().getUid())) user = app.getMyUser();
        else user = app.getMingleUser(uid);
        photo_num = user.getPhotoNum();
                 
        app.connectHelper.getRank(uid);
        final LayoutInflater inflater = getLayoutInflater();
        final Activity temp = this;
        viewFlipper.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
        	 @SuppressLint("NewApi")
        	 @SuppressWarnings("deprecation")
        	 @Override
        	  public void onGlobalLayout() {        	 
        		 
        	   for(int i = 0; i < photo_num; i++){
      	       	 LinearLayout single_photo_layout = (LinearLayout) inflater
      	                .inflate(R.layout.single_photo, null);
      	       	 
      	       	 ResizableImageView photo_view = (ResizableImageView) single_photo_layout.findViewById(R.id.photoView);
      	       	 Drawable photo_drawable = user.getPic(i);
      	       	 
      	        photo_view.setImageDrawable(ImageRounder.getProfileRoundedDrawable(temp,photo_drawable, corner_round));
      	       	 viewFlipper.addView(single_photo_layout);
              }
        	   
        	if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
        	    viewFlipper.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        	   else
        	    viewFlipper.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        	  }
        	 });
        
        
	    Typeface koreanBoldTypeFace = Typeface.createFromAsset(getAssets(), "fonts/mingle-font-bold.otf");

        ImageView num_view = (ImageView) findViewById(R.id.profile_member_num);
        num_view.setImageResource(app.memberNumRsId(user.getNum(),user.getSex()));
        TextView name_view = (TextView) findViewById(R.id.profile_user_name);
        name_view.setTypeface(koreanBoldTypeFace);
        name_view.setText(user.getName());
        TextView dist_view = (TextView) findViewById(R.id.profile_user_dist);
        dist_view.setTypeface(koreanTypeFace);
        dist_view.setText(Float.toString(user.getDistance())+"km");
        dist_view.setTextColor(Color.GRAY);
        ImageView num_icon_view = (ImageView) findViewById(R.id.profile_member_num_icon);
        if(user.getSex().equals("M")) num_icon_view.setImageResource(R.drawable.male_numberpic);
        else num_icon_view.setImageResource(R.drawable.female_numberpic);
        TextView rank_view = (TextView) findViewById(R.id.profile_user_rank);
        String rankstr = String.valueOf(user.getRank());
        if(user.getRank() <= 1) rank_view.setTextColor(app.getResources().getColor(R.color.gold));
    	else if(user.getRank() <= 2) rank_view.setTextColor(app.getResources().getColor(R.color.silver));
    	else if(user.getRank() <= 3) rank_view.setTextColor(app.getResources().getColor(R.color.bronze));
    	else rank_view.setTextColor(app.getResources().getColor(R.color.dark_gray));
        if(user.getRank() == -1) rankstr = "";
        rank_view.setText(rankstr+"��");
        
        
        RelativeLayout vote_button = (RelativeLayout) findViewById(R.id.vote_button);
        RelativeLayout chat_button = (RelativeLayout) findViewById(R.id.chat_button);
        Button edit_profile_button = (Button) findViewById(R.id.edit_profile_button);
        
        if(type.equals("preview") || type.equals("setting") || user.getUid().equals(app.getMyUser().getUid())){
       	 vote_button.setVisibility(View.GONE);
       	 dist_view.setVisibility(View.GONE);
        } else {
       	 getNewImage(0);
        }
        /*if (type.equals("popular")) {
       	 vote_button.setVisibility(View.GONE);
        }*/
        if(!type.equals("candidate") || type.equals("choice")){
       	 chat_button.setVisibility(View.GONE);
        }
        if(!type.equals("setting")){
       	 edit_profile_button.setVisibility(View.GONE);
        }
        
	    photo_num = user.getPhotoNum();
	    current_viewing_pic_index = 0;
	    LinearLayout photoCounterWrapper = (LinearLayout) findViewById(R.id.photo_indicators);
	    if(((ViewGroup)photoCounterWrapper).getChildCount() != 0)
	      	 ((ViewGroup) findViewById(R.id.photo_indicators)).removeAllViews();
	    initializePhotoIndicators();
	    
	}
	
	private int current_viewing_pic_index = 0;
	
	
	private void initializePhotoIndicators() { 
		if(photo_num == 1) return;
		LinearLayout photoCounterWrapper = (LinearLayout) findViewById(R.id.photo_indicators);
		for(int i = 0; i < photo_num; i++) {
            ImageView indicator = new ImageView(this);
            int height = (int) getResources().getDimension(R.dimen.indicator_size);
            int width = (int) getResources().getDimension(R.dimen.indicator_size);
            indicator.setLayoutParams(new ViewGroup.LayoutParams(width, height));
            
            indicator.setPadding(8, 0, 8, 0);
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
    
    private BroadcastReceiver rankInfoReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		String data = intent.getStringExtra(HttpHelper.RANK_INFO);
    		user.setRank(Integer.parseInt(data));
    		TextView rank_view = (TextView) findViewById(R.id.profile_user_rank);
            String rankstr = Integer.valueOf(user.getRank()).toString();
            if(user.getRank() <= 1) rank_view.setTextColor(app.getResources().getColor(R.color.gold));
        	else if(user.getRank() <= 2) rank_view.setTextColor(app.getResources().getColor(R.color.silver));
        	else if(user.getRank() <= 3) rank_view.setTextColor(app.getResources().getColor(R.color.bronze));
        	else rank_view.setTextColor(app.getResources().getColor(R.color.dark_gray));
            if(user.getRank() == -1) rankstr = "";
            rank_view.setText(rankstr+"��");
    	}
    };
    
    
    
    
    public void updateView(int index){
    	if(index != viewFlipper.getDisplayedChild()) return;
    	LinearLayout curr_layout = (LinearLayout) viewFlipper.getCurrentView();
		ResizableImageView photo_view = (ResizableImageView) curr_layout.findViewById(R.id.photoView);

		Drawable pic_to_update = user.getPic(index);
		photo_view.setImageDrawable(ImageRounder.getProfileRoundedDrawable(this,pic_to_update, corner_round));
    }
    
    public void voteUser(View v){
    	String curr_uid = user.getUid();
    	if(!user.alreadyVoted()){
    		user.setVoted(true);
    		((MingleApplication) this.getApplication()).connectHelper.voteUser(curr_uid);
    	} else {
    		Toast.makeText(getApplicationContext(),  getResources().getString(R.string.vote_impossible), Toast.LENGTH_SHORT).show();
    	}
    }
    
    public void startChat(View v){
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
        finish();
    }
    
    public void modifyProfile(View v){
    	Intent i = new Intent(this, MainActivity.class);
        i.putExtra(MainActivity.MAIN_TYPE, "update");
        startActivity(i);
        finish();
    }
    
    /* Broadcast Receiver for notification of vote result*/
	  private BroadcastReceiver voteResultReceiver = new BroadcastReceiver() {
	    	@Override
	    	public void onReceive(Context context, Intent intent) {
	    		String result = intent.getExtras().getString(HttpHelper.VOTE_RESULT);
	    		if(result.equals("success")){
		    		Toast.makeText(getApplicationContext(),  getResources().getString(R.string.vote_success), Toast.LENGTH_SHORT).show();
	    		} else {
	    			user.setVoted(false);
		    		Toast.makeText(getApplicationContext(),  getResources().getString(R.string.vote_fail), Toast.LENGTH_SHORT).show();
	    		}
	    	}
	  };
	  
	  /* Broadcast Receiver for notification of http error*/
	  private BroadcastReceiver httpErrorReceiver = new BroadcastReceiver() {
	    	@Override
	    	public void onReceive(Context context, Intent intent) {
	    		user.setVoted(false);
	    		Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
	    	}
	  };
    
	  /* If current activity is called from GCM not Hunt, start Hunt */
	    @Override
	    public void onBackPressed(){
	    	if(this.isTaskRoot()){
	    		Intent huntIntent = new Intent(this, HuntActivity.class);
	        	huntIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	    		startActivity(huntIntent);
	    	}
	    	super.onBackPressed();
	    }
	  
	  @Override
	  public void onDestroy(){
		  LocalBroadcastManager.getInstance(this).unregisterReceiver(imageUpdateReceiver);
		  LocalBroadcastManager.getInstance(this).unregisterReceiver(httpErrorReceiver);
		  LocalBroadcastManager.getInstance(this).unregisterReceiver(voteResultReceiver);
		  LocalBroadcastManager.getInstance(this).unregisterReceiver(rankInfoReceiver);
		  
		  super.onDestroy();
	  }


	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}
}
