package com.example.mingle;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.example.mingle.HttpHelper;

/**
 * Created by Tempnote on 2014-06-12.
 */

public class MingleApplication extends Application {
    public HttpHelper connectHelper;
    public Socket socketHelper;
    public DatabaseHelper dbHelper;
    
    private MingleUser my_user;
    
    private ArrayList<String> photoPaths = new ArrayList<String>();;
    private String uid;
    private String sex;
    private int num;
    private String name;
    private String rid;
    
    private float latitude;
    private float longitude;
    private int dist_lim;
    
    private HashMap<String, MingleUser> user_map = new HashMap<String, MingleUser>();
    
    private ArrayList<String> candidates = new ArrayList<String>();
    private ArrayList<String> choices = new ArrayList<String>();
    private ArrayList<ArrayList<String>> pop_users = new ArrayList<ArrayList<String>>();

    public void createMyUser(String uid_var, String sex_var, int num_var, String name_var, float latitude_var, float longitude_var, int dist_lim_var){
    	my_user = new MingleUser(uid_var, name_var, num_var, 0, null, sex_var);
        setLat(latitude_var);
        setLong(longitude_var);
        setDist(dist_lim_var);
    }
    
    public MingleUser getMyUser(){
    	return my_user;
    }
    
    public void removePhotoPathAtIndex(int index) {
    	photoPaths.remove(index);
    }
    
    public void addPhotoPath(String photoPath) {
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
    
    public boolean isValid() {
        if (photoPaths == null) {
            photoPaths = new ArrayList<String>();
        }
        
        if (/*num == -1 ||  */photoPaths.size() == 0)
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
}
