package com.example.mingle;


import android.app.Application;
import android.os.Bundle;

import java.util.*;

import android.graphics.*;

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
    private String comment;
    private float latitude;
    private float longitude;
    private int dist_lim;

    private ArrayList<ChattableUser> users = new ArrayList<ChattableUser>();
    private HashMap<String,ChatRoom> chat_room_map = new HashMap<String,ChatRoom>();

    public void setAttributes(String uid_var, String sex_var, int num_var, String comment_var, float latitude_var, float longitude_var, int dist_lim_var){
        setUid(uid_var);
        setSex(sex_var);
        setNum(num_var);
        setComm(comment_var);
        setLat(latitude_var);
        setLong(longitude_var);
        setDist(dist_lim_var);
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

    public String getComm(){
        return comment;
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

    public void setUid(String uid_var){
        uid = uid_var;
    }

    public void setSex(String sex_var){
        sex = sex_var;
    }

    public void setNum(int num_var){
        num = num_var;
    }

    public void setComm(String comment_var){
        comment = comment_var;
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
        users.add(user);
    }
    
    public void removeChattableUser(int pos){
    	users.remove(pos);
    }
    
    public int getChattableUserPos(String uid){
    	for(int i = 0; i < users.size(); i++){
    		ChattableUser cu= users.get(i);
    		if(cu.getUid().equals(uid)) return i;
    	}
    	return -1;
    }
    
    public ChattableUser getChattableUser(int pos){
        return users.get(pos);
    }

    public ArrayList<ChattableUser> getChattableUsers (){
        return users;
    }
    
    public void addMsgToRoom(String recv_uid, String send_uid, String msg, int msg_counter, int status){
    	chat_room_map.get(recv_uid).addMsg(send_uid, msg, msg_counter, status);
    }
    
    public void updateMsgToRoom(String recv_uid, int msg_counter, String ts){
    	boolean update_success = chat_room_map.get(recv_uid).updateMsg(getUid(), msg_counter, ts);
    	if(!update_success) System.out.println("msg doesn't exist??!?!?!");
    }
    
    public void addRecvMsgToRoom(String send_uid, String msg, String ts){
    	chat_room_map.get(send_uid).addRecvMsg(send_uid, msg, ts);
    }
    
    public void addChatRoom(String uid){
    	ChatRoom cr = new ChatRoom(uid);
    	chat_room_map.put(uid, cr);
    }
    
    public ChatRoom getChatRoom(String uid){
    	return chat_room_map.get(uid);
    }
    
    public ArrayList<ChatRoom> getChatRoomList(){
    	return new ArrayList<ChatRoom>(chat_room_map.values());
    }
}