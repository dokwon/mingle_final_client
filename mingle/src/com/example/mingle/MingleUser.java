package com.example.mingle;


import android.app.Application;
import android.os.Bundle;

import java.sql.Timestamp;
import java.util.*;

import android.graphics.*;
import android.graphics.drawable.Drawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Tempnote on 2014-06-02.
 */

class MingleUser extends MingleApplication {

    private ArrayList<String> photoPaths = new ArrayList<String>();;
    private String uid;
    private String sex;
    private int num;
    private String name;
    private float latitude;
    private float longitude;
    private int dist_lim;
    private String rid;
    
    private ArrayList<ChattableUser> chattable_users = new ArrayList<ChattableUser>();
    private ArrayList<ChattableUser> chatting_users = new ArrayList<ChattableUser>();
    
    private ArrayList<String> voted_users = new ArrayList<String>();

    private ArrayList<ArrayList<ChattableUser>> top_users = new ArrayList<ArrayList<ChattableUser>>();

    public void setAttributes(String uid_var, String sex_var, int num_var, String name_var, float latitude_var, float longitude_var, int dist_lim_var){
        setUid(uid_var);
        setSex(sex_var);
        setNum(num_var);
        setName(name_var);
        setLat(latitude_var);
        setLong(longitude_var);
        setDist(dist_lim_var);
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

    public String getUid(){
        return uid;
    }

    public String getSex(){
        return sex;
    }

    public int getNum(){
        return num;
    }

    public String getName(){
        return name;
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
    
    public void setUid(String uid_var){
        uid = uid_var;
    }

    public void setSex(String sex_var){
        sex = sex_var;
    }

    public void setNum(int num_var){
        num = num_var;
    }

    public void setName(String name_var){
        name = name_var;
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

    public void addChattableUser(ChattableUser user){
        chattable_users.add(user);
    }
    
    public void removeChattableUser(int pos){
    	chattable_users.remove(pos);
    }
    
    public int getChattableUserPos(String uid){
    	for(int i = 0; i < chattable_users.size(); i++){
    		ChattableUser cu= chattable_users.get(i);
    		if(cu.getUid().equals(uid)) return i;
    	}
    	return -1;
    }
    
    public ChattableUser getChattableUser(int pos){
        return chattable_users.get(pos);
    }
    
    public ChattableUser getChattableUser(String uid){
    	for(int i = 0; i < chattable_users.size(); i++){
    		ChattableUser cu= chattable_users.get(i);
    		if(cu.getUid().equals(uid)) return cu;
    	}
    	return null;
    }

    public ArrayList<ChattableUser> getChattableUserList(){
        return chattable_users;
    }
    
    public void addChattingUser(ChattableUser cu){
    	cu.createChatRoom(getUid());
    	chatting_users.add(cu);
    }
    
    public ChattableUser getChattingUser(String uid){
    	for(int i = 0; i < chatting_users.size(); i++){
    		ChattableUser cu= chatting_users.get(i);
    		if(cu.getUid().equals(uid)) return cu;
    	}
    	return null;
    }
    
    public ArrayList<ChattableUser> getChattingUserList(){
    	return chatting_users;
    }
    
    public ChattableUser getUser(String uid){
    	ChattableUser cu = getChattableUser(uid);
    	if(cu == null) cu = getChattingUser(uid);
    	return cu;
    }
    public void switchChattableToChatting(int index){
    	ChattableUser cu = getChattableUser(index);
    	addChattingUser(cu);
    	removeChattableUser(index);
    }
    
    public void addVotedUser(String uid){
    	voted_users.add(uid);
    }
    
    public boolean alreadyVoted(String uid){
    	for(int i = 0; i < voted_users.size(); i++){
    		if(voted_users.get(i).equals(uid)) return true;
    	}
    	return false;
    }
    
    public ArrayList<ArrayList<ChattableUser>> getTopList(){
    	return top_users;
    }
    
    public void addTopUsers(ChattableUser female_cu, ChattableUser male_cu){
    	ArrayList<ChattableUser> rank_list = new ArrayList<ChattableUser>();
    	rank_list.add(female_cu);
    	rank_list.add(male_cu);
    	top_users.add(rank_list);
    	System.out.println("size: " + rank_list.size() + "  total size: " + top_users.size());
    }
    
    public void emptyTopList(){
    	top_users.clear();
    }
    
    public ChattableUser getTopUser(String uid){
    	for(int i = 0; i < top_users.size(); i++){
    		ArrayList<ChattableUser> rank_list= top_users.get(i);
    		ChattableUser female_user = rank_list.get(0);
    		ChattableUser male_user = rank_list.get(1);
    		if(female_user != null && female_user.getUid().equals(uid)) return female_user;
    		if(male_user != null && rank_list.get(1).getUid().equals(uid)) return male_user;
    	}
    	return null;
    }
}