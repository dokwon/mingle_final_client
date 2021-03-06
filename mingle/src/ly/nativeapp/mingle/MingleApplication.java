package ly.nativeapp.mingle;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;


import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.os.Bundle;

/**
 * Created by Tempnote on 2014-06-12.
 */

public class MingleApplication extends Application {
	public static final int PHOTO_COMPRESS_FACTOR = 2;

	public final static String UPDATE_MSG_LIST = "ly.nativeapp.mingle.UPDATE_MSG_LIST";
	private static final String server_url = "http://ec2-54-64-20-181.ap-northeast-1.compute.amazonaws.com:8080";
    public Typeface koreanTypeFace;
    public Typeface koreanBoldTypeFace;
    public int blankProfileImage;
    public int blankProfileImageSmall;
    private String theme_of_the_day;
    private String question_of_the_day;
    public MingleApplication app;
	
    public HttpHelper connectHelper;
    public Socket socketHelper;
    public DatabaseHelper dbHelper;
    
    private MingleUser my_user = null;
    
    private ArrayList<String> photoPaths = new ArrayList<String>();
    private String rid;
    
    private float latitude;
    private float longitude;
    private int dist_lim;
    private int first_match_num = 10;
    private int extra_match_num = 5;
    private boolean can_get_more_pop_list = true;
    private boolean can_get_more_candidate = true;
    private boolean notification_on = true;
    private boolean needRefresh = false;
    private boolean[] groupNumFilter = {true, true, true, true, true};
    private ConcurrentHashMap<String, MingleUser> user_map = new ConcurrentHashMap<String, MingleUser>();
    
    private ArrayList<String> candidates = new ArrayList<String>();
    private ArrayList<String> choices = new ArrayList<String>();
    private ArrayList<String> pop_users = new ArrayList<String>();
    //private ArrayList<ArrayList<String>> pop_users = new ArrayList<ArrayList<String>>();

    private Timer mActivityTransitionTimer;
    private TimerTask mActivityTransitionTimerTask;
    public boolean wasInBackground;
    public boolean isJustLaunched = false;
    private final int MAX_ACTIVITY_TRANSITION_TIME_MS = 2000;
    
  @Override
  public void onCreate() {
	  app = this;
	  registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks(){
		  @Override
		  public void onActivityStopped(Activity activity) {}
		  @Override
		  public void onActivityStarted(Activity activity) {}
		  @Override
		  public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
		  @Override
		  public void onActivityResumed(Activity activity) {
			  if(wasInBackground && !(activity instanceof MainActivity)){

				  if(my_user != null && my_user.getUid() != null && !my_user.getUid().equals("")) {
					  if(connectHelper == null){
						  connectHelper = new HttpHelper(server_url, app);
					  }
					  connectHelper.checkUidValidity(my_user.getUid());
				  }
				  
				  if(needRefresh) {
					  needRefresh = false;
					  if(!isJustLaunched) {
						  Intent start = new Intent(activity, SplashScreenActivity.class);
					  	  start.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
					  	  startActivity(start);
					  } else {
						  isJustLaunched = false;
						  Intent start = new Intent(activity, MainActivity.class);
					  	  start.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
						  start.putExtra(MainActivity.MAIN_TYPE, "new");  
					  	  startActivity(start);
					  }
				  }
				  isJustLaunched = false;
			  } 
			  
			  stopActivityTransitionTimer();
		  }
		  
		  @Override
		  public void onActivityPaused(Activity activity) {
			  if(!(activity instanceof SplashScreenActivity))
				  startActivityTransitionTimer();
			  else {
				  isJustLaunched = true;
				  wasInBackground = true;
			  }
		  }
		  
		  @Override
		  public void onActivityDestroyed(Activity activity) {}
		  @Override
		  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
	  });
  }
   
    
   public void initializeApplication(){
    	koreanTypeFace = Typeface.createFromAsset(getAssets(), "fonts/mingle-font-regular.otf");
    	koreanBoldTypeFace = Typeface.createFromAsset(getAssets(), "fonts/mingle-font-bold.otf");
    	blankProfileImage = R.drawable.blankprofilelarge;
    	blankProfileImageSmall = R.drawable.blankprofile;
    	dist_lim = 30;
    }

   
   public void createDefaultMyUser(){
   		my_user = new MingleUser("", "", 0, 0, null, "", 0, 0);
   }
   
   public Bitmap rotatedBitmap(Bitmap source, String photoPath) {
		Matrix matrix = new Matrix();
		ExifInterface ei = null;
		try {
			ei = new ExifInterface(photoPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
		switch(orientation) {
		    case ExifInterface.ORIENTATION_ROTATE_90:
		    	matrix.postRotate(90);
		        break;
		    case ExifInterface.ORIENTATION_ROTATE_180:
		    	matrix.postRotate(180);
		        break;
		    case ExifInterface.ORIENTATION_ROTATE_270:
		    	matrix.postRotate(270);
		    	break;
		    // etc.
		}
		//matrix.postScale(0.5f, 0.5f);
		return Bitmap.createBitmap(source , 0, 0, source .getWidth(), source .getHeight(), matrix, true);
	}	
   
   public void setThemeToday(String theme){
	   theme_of_the_day = theme;
   }
   
   public String getThemeToday(){
	   return theme_of_the_day;
   }
   
   public void setQuestionToday(String question){
	   question_of_the_day = question;
   }
   
   public String getQuestionToday(){
	   return question_of_the_day;
   }
   
   public void setMyUser(String uid, String name, int num, String sex){
	   if(uid != null) my_user.setUid(uid);
	   if(name != null) my_user.setName(name);
	   if(num != 0) my_user.setNum(num);
	   if(sex != null) my_user.setSex(sex);
	   
	   my_user.clearPics();
	   for(int i = 0; i < photoPaths.size(); i++){
		   if(!(new File(photoPaths.get(i))).exists()) my_user.addPic(this.getResources().getDrawable(blankProfileImage));
		   else {
			   Bitmap bm = rotatedBitmap(BitmapFactory.decodeFile(photoPaths.get(i), null), photoPaths.get(i));
			   Drawable user_pic= new BitmapDrawable(getResources(),bm);
			   my_user.addPic(user_pic);
		   }
	   }
   }
   
   public void setNotiFlag(boolean bool){
	   this.notification_on = bool;
   }
   
   public boolean[] getGroupNumFilter(){
	   return this.groupNumFilter;
   }
   
   public boolean getNotiFlag(){
	   return this.notification_on;
   }
   
    public MingleUser getMyUser(){
    	return my_user;
    }
    
    public int getFirstMatchNum(){
    	return first_match_num;
    }
    
    public int getExtraMatchNum(){
    	return extra_match_num;
    }
    
    public boolean canGetMoreCandidate(){
    	return can_get_more_candidate;
    }
    
    public void noMoreCandidate(){
    	can_get_more_candidate = false;
    }
    
    public void moreCandidate(){
    	can_get_more_candidate = true;
    }
    
    public boolean canGetMorePopList(){
    	return can_get_more_pop_list;
    }
    
    public void noMorePopList(){
    	can_get_more_pop_list = false;
    }
    
    public void morePopList(){
    	can_get_more_pop_list = true;
    }
    
    public void removePhotoPathAtIndex(int index) {
    	photoPaths.remove(index);
    }
    
    public void setPhotoPath(int index, String photoPath) {
    	if(photoPaths == null){
    		photoPaths = new ArrayList<String>();
    	}
    	if(photoPaths.size() <= index) photoPaths.add(photoPath);
    	else photoPaths.set(index, photoPath);
    }
    
    public void addPhotoPath(String photoPath) {
    	if(photoPaths == null){
    		photoPaths = new ArrayList<String>();
    	}
    	photoPaths.add(photoPath);
    }
    
    public Bitmap getPic(int num) {
    	if(photoPaths.size() >= num) {
    		Bitmap bm;
            BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
            bm = BitmapFactory.decodeFile( photoPaths.get(num), btmapOptions);
    		return bm;
    	} 
    	return null;
    }

    public float getLat(){
        return latitude;
    }

    public float getLong(){
        return longitude;
    }

    public int getDist(){
        return dist_lim;
    }

    public String getRid(){
    	return rid;
    }


    public void setDist(int dist_lim_var){
        dist_lim = dist_lim_var;
    }
    
    public void setLat(float lat){
    	latitude = lat;
    }
    
    public void setLong(float lon){
    	longitude = lon;
    }

    public void setRid(String rid_var){
    	rid = rid_var;
    }
    
    public String isValid(String my_name) {
        //check photo path validity
        if (photoPaths == null || photoPaths.size() == 0) return getResources().getText(R.string.no_photo_input).toString();
        for(int i = 0; i < photoPaths.size(); i++){
        	if(!(new File(photoPaths.get(i))).exists()) return getResources().getText(R.string.photo_input_invalid).toString();
        }
        
        if(my_name.length() != 5) return getResources().getText(R.string.name_input_wrong_length).toString();
        if(my_name.contains(" ")) return getResources().getText(R.string.name_input_invalid).toString();
        return null;
    }

    public ArrayList<String> getPhotoPaths(){
        return photoPaths;
    }

    public void addMingleUser(MingleUser user){
    	user_map.put(user.getUid(), user);
    }
    
    public MingleUser getMingleUser(String uid){
    	return user_map.get(uid);
    }
    
    public void addCandidate(String uid){
    	if(getCandidatePos(uid) >= 0) return;
        candidates.add(uid);
    }
    
    public void removeCandidate(int pos){
    	candidates.remove(pos);
    }
    
    public int getCandidatePos(String uid){
    	for(int i = 0; i < candidates.size(); i++){
    		if(candidates.get(i).equals(uid)) return i;
    	}
    	return -1;
    }
    
    public String getCandidate(int pos){
        return candidates.get(pos);
    }

    public ArrayList<String> getCandidateList(){
        return candidates;
    }
    
    public void addChoice(String uid){
    	choices.add(uid);
    }
    
    public int getChoicePos(String uid){
    	for(int i = 0; i < choices.size(); i++){
    		if(choices.get(i).equals(uid)) return i;
    	}
    	return -1;
    }
    
    public ArrayList<String> getChoiceList(){
    	return choices;
    }
    
    public void switchCandidateToChoice(int index){
    	String uid = candidates.get(index);
    	MingleUser user = this.getMingleUser(uid);
    	if(!user.isPicAvail(-1))
    		new ImageDownloader(this, uid, -1).execute();
    	choices.add(uid);
    	candidates.remove(index);
    }
    
    public ArrayList<String> getPopList(){
    	return pop_users;
    }
    
    public void addPopUser(String pop_uid){
    	pop_users.add(pop_uid);
    }
    
    /*public ArrayList<ArrayList<String>> getPopList(){
    	return pop_users;
    }
    
    public void addPopUsers(String female_uid, String male_uid){
    	ArrayList<String> rank_list = new ArrayList<String>();
    	rank_list.add(female_uid);
    	rank_list.add(male_uid);
    	pop_users.add(rank_list);
    }*/
    
    public void emptyPopList(){
    	pop_users.clear();
    }

    public void setNewUser(String uid, JSONObject new_user_data, String user_type){
    	String sex = "M";
		if(my_user.getSex().equals("M")) sex = "F";
		MingleUser new_user = this.getMingleUser(uid);
		try {
			new_user.setName(new_user_data.getString("COMM"));
			new_user.setNum(new_user_data.getInt("NUM"));
			for(int i = 0; i < new_user_data.getInt("PHOTO_NUM"); i++){
				new_user.addBlankPic(this.getResources().getDrawable(blankProfileImage));
			}
			new_user.setSex(sex);
			float distance = this.getDistance(new_user_data.getDouble("LOC_LAT"), new_user_data.getDouble("LOC_LONG"));
			new_user.setDistance(distance);
			new_user.setRank(Integer.valueOf(new_user_data.getString("RANK")));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		if(user_type.equals("choice")) {
			new ImageDownloader(this.getApplicationContext(), new_user.getUid(), -1).execute();
			dbHelper.insertNewUID(uid, new_user.getNum(), new_user.getName(), new_user.getDistance());		
		} else if(user_type.equals("candidate")){
			new ImageDownloader(this.getApplicationContext(), new_user.getUid(), 0).execute();
		}
	}
    
    public String getLocalTime(String timestamp){
    	timestamp = timestamp.replaceAll("T"," ");
    	timestamp = timestamp.replaceAll("\\..+","");
    	SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	sourceFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    	Date parsed = null;
		try {
			parsed = sourceFormat.parse(timestamp);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

    	SimpleDateFormat destFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	destFormat.setTimeZone(TimeZone.getDefault());

    	String result = destFormat.format(parsed);
    	return result;
    }
    
    public void handleIncomingMsg(JSONObject get_msg_obj){
		try {
			String chat_user_uid = get_msg_obj.getString("send_uid");

			MingleUser user = this.getMingleUser(chat_user_uid);
			
			if(user == null) {
				user = new MingleUser(chat_user_uid, "", 0, 0, this.getResources().getDrawable(blankProfileImageSmall), "", 0, 0);
				this.addMingleUser(user);
				this.addChoice(chat_user_uid);
				connectHelper.getNewUser(chat_user_uid, "choice");
			} else {
				int candidate_pos = this.getCandidatePos(chat_user_uid);
				if(candidate_pos >= 0){
					this.switchCandidateToChoice(candidate_pos);
					MingleUser currentMU = this.user_map.get(chat_user_uid);
					dbHelper.insertNewUID(chat_user_uid, currentMU.getNum(), currentMU.getName(), currentMU.getDistance());
				} else {
					int choice_pos = this.getChoicePos(chat_user_uid);
					if(choice_pos < 0){
						this.addChoice(chat_user_uid);
						MingleUser currentMU = this.user_map.get(chat_user_uid);
						dbHelper.insertNewUID(chat_user_uid, currentMU.getNum(), currentMU.getName(),currentMU.getDistance());
					}
				}
				
			}
			
			String msg = get_msg_obj.getString("msg");
			String msg_ts = getLocalTime(get_msg_obj.getString("ts"));
    		this.getMingleUser(chat_user_uid).recvMsg(msg, msg_ts);
    		// Save to local storage
    		dbHelper.insertMessages(chat_user_uid, false, msg, msg_ts);
    		
    		if(this.getMingleUser(chat_user_uid).isInChat()) {
				Intent dispatcher = new Intent(this, ChatroomActivity.class);
				dispatcher.setAction(UPDATE_MSG_LIST);
				LocalBroadcastManager.getInstance(this).sendBroadcast(dispatcher);
    		} else this.getMingleUser(chat_user_uid).incrNewMsgNum();
    		
    		Intent dispatcher = new Intent(this, HuntActivity.class);
			dispatcher.setAction(HuntActivity.NEW_MESSAGE);
			LocalBroadcastManager.getInstance(this).sendBroadcast(dispatcher);
        	
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void handleMsgConf(JSONObject msg_conf_obj){
    	try{
    		String msg_recv_uid = msg_conf_obj.getString("recv_uid");
    		int msg_recv_counter = msg_conf_obj.getInt("msg_counter");
    		String msg_ts = getLocalTime(msg_conf_obj.getString("ts"));
    		ArrayList<Message> msg_list = user_map.get(msg_recv_uid).getMsgList();
    		String msg="";
    		for(Message obj : msg_list){
    			if(obj.getCounter()==msg_recv_counter){
    				msg=obj.getContent();
    			}	
    		}
    		this.dbHelper.insertMessages(msg_recv_uid, true, msg, msg_ts);

    		this.getMingleUser(msg_recv_uid).updateMsgOnConf(msg_recv_counter, msg_ts);
    		
    	   	if(this.getMingleUser(msg_recv_uid).isInChat()) {
    			Intent dispatcher = new Intent(this, ChatroomActivity.class);
    			dispatcher.setAction(UPDATE_MSG_LIST);
    			LocalBroadcastManager.getInstance(this).sendBroadcast(dispatcher);
    		}
    	   	
    	} catch (JSONException e){
    		e.printStackTrace();
    	} 
    }
    
    public boolean isLocationEnabled() {
    	// Acquire a reference to the system Location Manager
    	LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    	// getting GPS status
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) return false;
        else return true;
    }
    
    public String getLocationProvider() {
    	// Acquire a reference to the system Location Manager
    	LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    	
    	if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    		return LocationManager.NETWORK_PROVIDER;
    	else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
    		return LocationManager.GPS_PROVIDER;
    	else return null;
    }
    
    public void deactivateApp(){
    	String uid = this.my_user.getUid();
    	this.connectHelper.requestDeactivation(uid);
    	this.socketHelper.disconnectSocket();
    	this.dbHelper.deleteAll();
        
        photoPaths.clear();
        user_map.clear();
        candidates.clear();
        choices.clear();
        pop_users.clear();
        morePopList();
        moreCandidate();
    	
        ((NotificationManager)this.getSystemService(NOTIFICATION_SERVICE)).cancelAll();
    	GcmIntentService.clearNotificationData();

        notification_on = true;
        for(int i = 0 ; i < 5 ; i++) groupNumFilter[i] = true;
        dist_lim = 30;
    }
    
    

  
    
    public int memberNumRsId(int numOfMembers, String sex) {
    	
    	int rval = -1; 
    	switch(numOfMembers) {
	    	case 1:
	    		if(sex.equals("M")) rval = R.drawable.male_membercount1;
	    		else rval = R.drawable.female_membercount1;
	    		break;
	    	case 2:
	    		if(sex.equals("M")) rval = R.drawable.male_membercount2;
	    		else rval = R.drawable.female_membercount2;
	    		break;
	    	case 3: 
	    		if(sex.equals("M")) rval = R.drawable.male_membercount3;
	    		else rval = R.drawable.female_membercount3;
	    		break;
	    	case 4: 
	    		if(sex.equals("M")) rval = R.drawable.male_membercount4;
	    		else rval = R.drawable.female_membercount4;
	    		break;
	    	case 5:
	    		if(sex.equals("M")) rval = R.drawable.male_membercount5;
	    		else rval = R.drawable.female_membercount5;
	    		break;
	    	case 6:
	    		if(sex.equals("M")) rval = R.drawable.male_membercount6;
	    		else rval = R.drawable.female_membercount6;
	    		break;
    		
    	}
    	return rval; 
    }
 
    public float getDistance(double double_lat, double double_long){
 		float latitude = (float)double_lat;
 		float longitude = (float)double_long;
 		
 		float dLat = deg2rad(latitude-this.latitude);
 		float dLng = deg2rad(longitude-this.longitude);
 		float a = (float)(Math.sin(dLat/2) * Math.sin(dLat/2) + 
 						  Math.cos(deg2rad(latitude)) * Math.cos(deg2rad(this.latitude))*
 						  Math.sin(dLng/2) * Math.sin(dLng/2));
 		float dist = (float) (2* Math.atan2(Math.sqrt(a), Math.sqrt(1-a)) * 6369);
 		
 		if((dist*10 - (int)(dist*10)) < 0.5) return (int)(dist *10)/(float)10.0;
 		else return (int)(dist*10 + 1)/(float)10.0;
 	}
 	
 	private float deg2rad(float deg) {
 		return (float)(deg * Math.PI / 180.0);
 	}
 	
 	private float rad2deg(float rad) {
 		return (float)(rad * 180 / Math.PI);
 	}
 	
 	public void setNeedRefreshAccount() {
 		this.needRefresh = true;
 	}

 	public void startActivityTransitionTimer() {
 		this.mActivityTransitionTimer = new Timer();
 		this.mActivityTransitionTimerTask = new TimerTask() {
 			public void run () {
 				MingleApplication.this.wasInBackground = true;
 			}
 		};
 		
 		this.mActivityTransitionTimer.schedule(mActivityTransitionTimerTask, MAX_ACTIVITY_TRANSITION_TIME_MS);
 	}
 	
 	public void stopActivityTransitionTimer() {
 		if(this.mActivityTransitionTimerTask != null)
 			this.mActivityTransitionTimerTask.cancel();
 		
 		if(this.mActivityTransitionTimer != null)
 			this.mActivityTransitionTimer.cancel();
 		
 		this.wasInBackground = false;
 	}
}

