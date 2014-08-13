package com.example.mingle;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.app.ProgressDialog;
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
import android.view.WindowManager;

import com.example.mingle.HttpHelper;

/**
 * Created by Tempnote on 2014-06-12.
 */

public class MingleApplication extends Application {
    public final static String UPDATE_MSG_LIST = "com.example.mingle.UPDATE_MSG_LIST";
    public Typeface koreanTypeFace;

    private String question_of_the_day;
	
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
    private boolean can_get_more_candidate = true;
    private boolean notification_on = true;
    private boolean[] groupNumFilter = {true, true, true, true, true};
    
    private HashMap<String, MingleUser> user_map = new HashMap<String, MingleUser>();
    
    private ArrayList<String> candidates = new ArrayList<String>();
    private ArrayList<String> choices = new ArrayList<String>();
    private ArrayList<ArrayList<String>> pop_users = new ArrayList<ArrayList<String>>();
   
   public void createDefaultMyUser(){
   		my_user = new MingleUser("", "", 0, 0, null, "");
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
		System.out.println(Integer.toString(orientation));
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
		matrix.postScale(0.5f, 0.5f);
		return Bitmap.createBitmap(source , 0, 0, source .getWidth(), source .getHeight(), matrix, true);
	}	
   
   public void setQuestion(String question){
	   question_of_the_day = question;
   }
   
   public String getQuestion(String question){
	   return question_of_the_day;
   }
   
   public void setMyUser(String uid, String name, int num, String sex){
	   if(uid != null) {
		   Log.i("sktag", "uid with quitation mark...?");
		   if(uid.startsWith("\"")) uid = uid.substring(1);
		   if(uid.endsWith("\"")) uid = uid.substring(0, uid.length() - 2);
		   my_user.setUid(uid);
	   }
	   if(name != null) my_user.setName(name);
	   if(num != 0) my_user.setNum(num);
	   if(sex != null) my_user.setSex(sex);
	   
	   my_user.clearPics();
	   for(int i = 0; i < photoPaths.size(); i++){
		   if(!(new File(photoPaths.get(i))).exists()) my_user.addPic(this.getResources().getDrawable(R.drawable.ic_launcher));
		   else {
			   Bitmap bm;
			   BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
			   btmapOptions.inSampleSize = 16;
			   bm = rotatedBitmap(BitmapFactory.decodeFile(photoPaths.get(i), btmapOptions), photoPaths.get(i));
			   Drawable user_pic;
			   user_pic = new BitmapDrawable(getResources(),bm);
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
    
    public void removePhotoPathAtIndex(int index) {
    	photoPaths.remove(index);
    }
    
    public void addPhotoPath(String photoPath) {
    	if(photoPaths == null){
    		photoPaths = new ArrayList<String>();
    	}
    	photoPaths.add(photoPath);
    }
    
    /*public Bitmap getPic(int num) {
    	if(photoPaths.size() >= num) {
    		Bitmap bm;
            BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
            bm = BitmapFactory.decodeFile( photoPaths.get(num), btmapOptions);
    		return bm;
    	} 
    	return null;
    }*/

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
    
    public void setLat(float loc_lat){
    	this.latitude = loc_lat;
    }
    
    public void setLong(float loc_long){
    	this.longitude = loc_long;
    }
    
    public void setDist(int dist_lim_var){
        dist_lim = dist_lim_var;
    }

    public void setRid(String rid_var){
    	rid = rid_var;
    }
    
     public boolean isValid(String my_name) {
        //check photo path validity
        if (photoPaths == null || photoPaths.size() == 0) return false;
        for(int i = 0; i < photoPaths.size(); i++){
        	if(!(new File(photoPaths.get(i))).exists()) return false;
        }
        
        if (my_name.length() < 5) return false;

        return true;
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
    	Log.i("Cand debug", "befre add" + candidates.size());
        candidates.add(uid);
    	Log.i("Cand debug", "after add " + uid + "->" + candidates.size());
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
    	Log.i("Cand debug", "curr size->" + candidates.size());
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
    	choices.add(uid);
    	candidates.remove(index);
    }
    
    public ArrayList<ArrayList<String>> getPopList(){
    	return pop_users;
    }
    
    public void addPopUsers(String female_uid, String male_uid){
    	ArrayList<String> rank_list = new ArrayList<String>();
    	rank_list.add(female_uid);
    	rank_list.add(male_uid);
    	pop_users.add(rank_list);
    }
    
    public void emptyPopList(){
    	pop_users.clear();
    }

    public void createNewChoiceUser(String uid, JSONObject new_user_data){
    	String sex = "M";
		if(my_user.getSex().equals("M")) sex = "F";
		MingleUser new_user = null;
		try {
			new_user = new MingleUser(uid, new_user_data.getString("COMM"), new_user_data.getInt("NUM"), 
											new_user_data.getInt("PHOTO_NUM"), (Drawable) this.getResources().getDrawable(R.drawable.ic_launcher), sex);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		this.addMingleUser(new_user);
		this.addChoice(uid);
		
		new ImageDownloader(this.getApplicationContext(), new_user.getUid(), -1).execute();
		dbHelper.insertNewUID(uid, new_user.getNum(), new_user.getName(), 0, 0, 0);
		
		//download profile also
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
			//MingleApplication curr_user = ((MingleApplication) this.getApplicationContext());
			String chat_user_uid = get_msg_obj.getString("send_uid");

			MingleUser user = this.getMingleUser(chat_user_uid);
			if(user == null) {
				connectHelper.getNewUser(chat_user_uid);				
			} else {
				int candidate_pos = this.getCandidatePos(chat_user_uid);
				if(candidate_pos >= 0){
					this.switchCandidateToChoice(candidate_pos);
					MingleUser currentMU = this.user_map.get(chat_user_uid);
					dbHelper.insertNewUID(chat_user_uid, currentMU.getNum(), currentMU.getName(), 0, 0, 0);
				} else {
					int choice_pos = this.getChoicePos(chat_user_uid);
					if(choice_pos < 0){
						this.addChoice(chat_user_uid);
						MingleUser currentMU = this.user_map.get(chat_user_uid);
						dbHelper.insertNewUID(chat_user_uid, currentMU.getNum(), currentMU.getName(), 0, 0, 0);
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
    		}
        	
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void handleMsgConf(JSONObject msg_conf_obj){
    	try{
    		String msg_recv_uid = msg_conf_obj.getString("recv_uid");
    		int msg_recv_counter = Integer.parseInt(msg_conf_obj.getString("msg_counter"));
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
    
    public void deactivateApp(Context context){
    	ProgressDialog proDialog = new ProgressDialog(context);
        proDialog.setIndeterminate(true);
        proDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        proDialog.getWindow().getAttributes().dimAmount = (float)0.8;
        proDialog.show();

        this.socketHelper.disconnectSocket();
        this.dbHelper.deleteAll();
        this.connectHelper.requestDeactivation(this.my_user.getUid());
        
        photoPaths.clear();
        user_map.clear();
        candidates.clear();
        choices.clear();
        pop_users.clear();
        
        notification_on = true;
        for(int i = 0 ; i < 5 ; i++) groupNumFilter[i] = true;
        dist_lim = 3;
        
        proDialog.dismiss();
    }
}

