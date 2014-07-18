package com.example.mingle;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;

public class ChattableUser {
	String user_uid;
	String comment;
	int num;
	ArrayList<Drawable> pics;
	ArrayList<Boolean> pics_bool;
	ChatRoom chat_room;
    

    public ChattableUser(String user_uid, String comment, int num, int photo_num, Drawable default_img) {
          super();
          this.user_uid = user_uid;
          this.comment = comment;
          this.num = num;
          this.pics = new ArrayList<Drawable>();
          this.pics_bool = new ArrayList<Boolean>();
          
          for (int i = 0; i < photo_num; i++){
        	  pics.add(default_img);
        	  pics_bool.add(false);
          }
    }
    
    
    public String getUid() {
        return user_uid;
    }
  
    public void setUid(String user_uid) {
        this.user_uid = user_uid;
  	}
  
    public String getComment() {
          return comment;
    }
    
    public void setComment(String comment) {
          this.comment = comment;
    }
    
    public int getNum() {
        return num;
    }
  
    public void setNum(int num) {
	  	this.num = num;
  	}
  
    public int getPhotoNum(){
    	return pics.size();
    }
    
    public Drawable getPic(int index) {
    	  if(pics.size() <= index || index < 0) return null;
          return pics.get(index);
    }
    
    public void setPic(int index, Drawable pic){
    	pics.set(index, pic);
    	pics_bool.set(index, true);
    }
    
    public boolean isPicAvail(int index){
    	return pics_bool.get(index);
    }
    
    public void createChatRoom(String my_uid){
    	this.chat_room = new ChatRoom(my_uid, user_uid);
    }
    
    public ChatRoom getChatRoom(){
    	return this.chat_room;
    }
    
    public void addMsgToChatRoom(String msg, int msg_counter, int status){
    	this.chat_room.addMsg(msg, msg_counter, status);
    }
    
    public void recvMsgToChatRoom(String msg, String msg_ts){
    	this.chat_room.addRecvMsg(getPic(0), msg, msg_ts);
    }
}
