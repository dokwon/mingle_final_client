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
	private boolean just_created;
	private String send_uid;
	private String recv_uid;
	
	public ChatRoom(String send_uid, String recv_uid){
		msg_list =  new ArrayList<Message>();
		just_created = true;
		this.send_uid = send_uid;
		this.recv_uid = recv_uid;
	}
	
	public boolean isJustCreated(){
		return just_created;
	}
	
	public void setChatActive(){
		just_created = false;
	}

	public void addMsg(String msg, int msg_counter, int status){
		Date date= new Date();
		Timestamp timestamp = (new Timestamp(date.getTime()));
		Message msg_obj = new Message(send_uid, null, msg, msg_counter, timestamp.toString(), 0);
		msg_list.add(msg_obj);
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
	

	public void addRecvMsg(Drawable image, String msg, String msg_ts){
		System.out.println("recv at: " + msg_ts);
		/*SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		Date parsedDate = dateFormat.parse(msg_ts);
		Timestamp timestamp = new Timestamp(parsedDate.getTime());
		*/
			
		Message msg_obj = new Message(recv_uid, image, msg, -1, msg_ts, 1);
		msg_list.add(msg_obj);
		Collections.sort(msg_list, new MsgComparator());

	}
	
	public ArrayList<Message> getMsgList(){
		return msg_list;
	}
	
	
	public String getLastMsg(){
		if(msg_list.size() > 0) return msg_list.get(msg_list.size() - 1).getContent();
		return "";
	}
	
}

class MsgComparator implements Comparator<Message> {
    public int compare(Message msg1, Message msg2) {
    	return (msg1.getTimestamp()).compareTo(msg2.getTimestamp());
    }
}
