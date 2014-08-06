package com.example.mingle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.mingle.HttpHelper;
import com.example.mingle.MingleUser.MsgComparator;

/**
 * Created by Tempnote on 2014-06-12.
 */

public class MingleApplication extends Application {
    public final static String UPDATE_MSG_LIST = "com.example.mingle.UPDATE_MSG_LIST";
	
    public HttpHelper connectHelper;
    public Socket socketHelper;
    public DatabaseHelper dbHelper;
    
    private MingleUser my_user;
    
    private ArrayList<String> photoPaths = new ArrayList<String>();
    private String uid;
    private String sex;
    private int num;
    private String name;
    private String rid;
    
    private float latitude;
    private float longitude;
    private int dist_lim;
    private int first_match_num = 10;
    private int extra_match_num = 5;
    private boolean can_get_more_candidate = true;
    
    private HashMap<String, MingleUser> user_map = new HashMap<String, MingleUser>();
    
    private ArrayList<String> candidates = new ArrayList<String>();
    private ArrayList<String> choices = new ArrayList<String>();
    private ArrayList<ArrayList<String>> pop_users = new ArrayList<ArrayList<String>>();
    
    private ArrayList<String> setting_list = new ArrayList<String>();

   public void createMyUser(JSONObject userData){
    	
    	try {
	    	my_user = new MingleUser(userData.getString("UID"), 
	    			userData.getString("COMM"), 
	    			userData.getInt("NUM"), 
	    			photoPaths.size(), null, 
	    			userData.getString("SEX"));
	        setLat((float)userData.getDouble("LOC_LAT"));
	        setLong((float)userData.getDouble("LOC_LONG"));
	        setDist(userData.getInt("DIST_LIM"));
    	} catch(JSONException e) {
    		e.printStackTrace();
    	}
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

    public void setLat(float latitude_var){
        latitude = latitude_var;
    }

    public void setLong(float longitude_var){
        longitude = longitude_var;
    }

    public void setDist(int dist_lim_var){
        dist_lim = dist_lim_var;
    }

    public void setRid(String rid_var){
    	rid = rid_var;
    }
    
     public boolean isValid(int my_num, String my_name) {
        if (photoPaths == null) {
            photoPaths = new ArrayList<String>();
        }
        
        if (my_num == -1 ||  photoPaths.size() == 0 || my_name == "")
            return false;

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
    
    public ArrayList<String> getSettingList(){
    	return setting_list;
    }
    
    public void handleIncomingMsg(JSONObject get_msg_obj){
		try {
			//MingleApplication curr_user = ((MingleApplication) this.getApplicationContext());
			String chat_user_uid = get_msg_obj.getString("send_uid");

			MingleUser user = this.getMingleUser(chat_user_uid);
			if(user == null){
				String sex = "M";
				if(my_user.getSex().equals("M")) sex = "F";
				MingleUser new_user = new MingleUser(chat_user_uid, "", 0, 1, (Drawable) this.getResources().getDrawable(R.drawable.ic_launcher),sex);
				
				this.addMingleUser(new_user);
				this.addChoice(chat_user_uid);
				
				new ImageDownloader(this.getApplicationContext(), new_user.getUid(), 0);
				MingleUser currentMU = this.user_map.get(chat_user_uid);
				dbHelper.insertNewUID(chat_user_uid, currentMU.getNum(), currentMU.getName(), 0, 0, 0);
				
				//download profile also
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
			String msg_ts = get_msg_obj.getString("ts");
    		this.getMingleUser(chat_user_uid).recvMsg(msg, msg_ts);
    		// Save to local storage
			//((MingleApplication)currContext.getApplicationContext()).dbHelper.insertMessages(chat_user_uid, chat_user_uid, msg, msg_ts);
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
    		String msg_ts = msg_conf_obj.getString("ts");
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
    
}

