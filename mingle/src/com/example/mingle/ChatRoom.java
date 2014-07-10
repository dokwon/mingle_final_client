package com.example.mingle;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import android.graphics.drawable.Drawable;

class ChatRoom{
	
	private ArrayList<Message> msg_list;
	private String recv_uid;
	private boolean just_created;
	private Drawable user_pic;
	
	public ChatRoom(String uid, Drawable image){
		msg_list =  new ArrayList<Message>();
		recv_uid = uid;
		just_created = true;
		user_pic = image;
	}
	
	public boolean isJustCreated(){
		return just_created;
	}
	
	public void setChatActive(){
		just_created = false;
	}
	
	public String getRecvUid(){
		return recv_uid;
	}
	

	public void addMsg(String send_uid, Drawable image, String msg, int msg_counter, int status){
		Date date= new Date();
		Timestamp timestamp = (new Timestamp(date.getTime()));
		Message msg_obj = new Message(send_uid, image, msg, msg_counter, timestamp, 0);
		msg_list.add(msg_obj);
		Collections.sort(msg_list, new MsgComparator());
	}
	

	public void addRecvMsg(String send_uid, Drawable image, String msg, String msg_ts){
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
			Date parsedDate = dateFormat.parse(msg_ts);
			Timestamp timestamp = new Timestamp(parsedDate.getTime());
			
			Message msg_obj = new Message(send_uid, image, msg, -1, timestamp, 1);
			msg_list.add(msg_obj);
			Collections.sort(msg_list, new MsgComparator());
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<Message> getMsgList(){
		return msg_list;
	}
	
	public boolean updateMsg(String send_uid, int counter, String msg_ts){
		for(Message obj : msg_list){
			if(obj.getUid().equals(send_uid) && obj.getCounter()==counter){
				obj.setStatus(1);
        		try {
    				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
    				Date parsedDate = dateFormat.parse(msg_ts);
    				Timestamp timestamp = new Timestamp(parsedDate.getTime());
    				
    				obj.setTimestamp(timestamp);
    				Collections.sort(msg_list, new MsgComparator());
    			} catch (java.text.ParseException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
				return true;
			}	
		}
		return false;
	}
	
	public String getLastMsg(){
		if(msg_list.size() > 0) return msg_list.get(msg_list.size() - 1).getContent();
		return "";
	}
	
	public Drawable getPic(){
		return user_pic;
	}
	
}

class MsgComparator implements Comparator<Message> {
    public int compare(Message msg1, Message msg2) {
    	return (msg1.getTimestamp()).compareTo(msg2.getTimestamp());
    }
}
