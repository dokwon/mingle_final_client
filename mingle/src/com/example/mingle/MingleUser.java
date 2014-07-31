package com.example.mingle;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.drawable.Drawable;

public class MingleUser {
	String uid;
	String name;
	int num;
	ArrayList<Drawable> pics;
	ArrayList<Boolean> pics_bool;
	String sex;
	boolean voted;
	ArrayList<Message> msg_list;
    boolean inChat;

    public MingleUser(String uid, String name, int num, int photo_num, Drawable default_img, String sex) {
          super();
          this.uid = uid;
          this.name = name;
          this.num = num;
          this.sex = sex;
          this.voted = false;
          this.msg_list = new ArrayList<Message>();
          this.pics = new ArrayList<Drawable>();
          this.pics_bool = new ArrayList<Boolean>();
          this.inChat = false;
          for (int i = 0; i < photo_num; i++){
        	  pics.add(default_img);
        	  pics_bool.add(false);
          }
          
    }
    
    public void setInChat(boolean inChat){
    	this.inChat = inChat;
    }
    
    public boolean isInChat(){
    	return inChat;
    }
    
    public String getUid() {
        return uid;
    }
  
    public void setUid(String uid) {
        this.uid = uid;
  	}
  
    public String getName() {
          return name;
    }
    
    public void setName(String name) {
          this.name = name;
    }
    
    public int getNum() {
        return num;
    }
  
    public void setNum(int num) {
	  	this.num = num;
  	}
    
    public String getSex(){
    	return sex;
    }
    
    public void setSex(String sex){
    	this.sex = sex;
    }
    
    public boolean alreadyVoted(){
    	return voted;
    }
    
    public void setVoted(){
    	voted = true;
    }
  
    public int getPhotoNum(){
    	return pics.size();
    }
    
    public ArrayList<Message> getMsgList(){
    	return msg_list;
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
    
    public boolean isMsgListEmpty(){
    	if(msg_list.size() == 0) return true;
    	else return false;
    }
    
    public void addMsg(String msg, int msg_counter, int status){
    	Date date= new Date();
		Timestamp timestamp = (new Timestamp(date.getTime()));
		Message msg_obj = new Message(msg, msg_counter, timestamp.toString(), 0, true);
		msg_list.add(msg_obj);
		Collections.sort(msg_list, new MsgComparator());
    }
    
    public void addMsgObj(Message msg){
    	msg_list.add(msg);
		Collections.sort(msg_list, new MsgComparator());
    }
    
    public boolean updateMsgOnConf(int counter, String msg_ts){
		for(Message obj : msg_list){
			if(obj.getCounter()==counter){
				obj.setStatus(1);

        		System.out.println("msg conf at: " + msg_ts);
    				
    			obj.setTimestamp(msg_ts);
    			Collections.sort(msg_list, new MsgComparator());

				return true;
			}	
		}
		return false;
	}
    
    public void recvMsg(String msg, String msg_ts){
    	Message msg_obj = new Message(msg, -1, msg_ts, 1, false);
		msg_list.add(msg_obj);
		Collections.sort(msg_list, new MsgComparator());
    }
    
    public String getLastMsg(){
		if(msg_list.size() > 0) return msg_list.get(msg_list.size() - 1).getContent();
		return "";
	}
    
    class MsgComparator implements Comparator<Message> {
        public int compare(Message msg1, Message msg2) {
        	return (msg1.getTimestamp()).compareTo(msg2.getTimestamp());
        }
    }
}
