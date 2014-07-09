package com.example.mingle;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

class ChatRoom{
	
	private ArrayList<JSONObject> msg_list;
	private String recv_uid;
	private boolean just_created;
	
	public ChatRoom(String uid){
		msg_list =  new ArrayList<JSONObject>();
		recv_uid = uid;
		just_created = true;
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
	
	public void addMsg(String send_uid, String msg, int msg_counter, int status){
		Date date= new Date();
		Timestamp timestamp = (new Timestamp(date.getTime()));
		JSONObject msg_obj = new JSONObject();
    	try {
    		msg_obj.put("send_uid", send_uid);
			msg_obj.put("msg", msg);
			msg_obj.put("msg_counter", msg_counter);
			msg_obj.put("ts", timestamp);
			msg_obj.put("status", 0);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		msg_list.add(msg_obj);
		Collections.sort(msg_list, new MsgComparator());
	}
	
	public void addRecvMsg(String send_uid, String msg, String timestamp){
		JSONObject msg_obj = new JSONObject();
    	try {
    		msg_obj.put("send_uid", send_uid);
			msg_obj.put("msg", msg);
			msg_obj.put("msg_counter", -1);
			msg_obj.put("ts", timestamp);
			msg_obj.put("status", 1);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		msg_list.add(msg_obj);
		Collections.sort(msg_list, new MsgComparator());
	}
	
	public ArrayList<JSONObject> getMsgList(){
		return msg_list;
	}
	
	public boolean updateMsg(String send_uid, int counter, String ts){
		for(JSONObject obj : msg_list){
			try {
				if(obj.getString("send_uid").equals(send_uid) && obj.getString("msg_counter").equals(new Integer(counter).toString())){
					obj.remove("status");
					obj.put("status", 1);
					obj.remove("ts");
					obj.put("ts", ts);
					Collections.sort(msg_list, new MsgComparator());
					return true;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
		return false;
	}
	
	public String getLastMsg(){
		try {
			if(msg_list.size() > 0) return msg_list.get(msg_list.size() - 1).getString("msg");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
}

class MsgComparator implements Comparator<JSONObject> {
    public int compare(JSONObject msg1, JSONObject msg2) {
    	try {
	        return msg1.getString("ts").compareTo(msg2.getString("ts"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return -1;
    }
}
