package com.example.mingle;


import java.sql.Timestamp;

import android.graphics.drawable.Drawable;


public class Message {
	private String uid;
	private Drawable pic;
	private String content;
	private int counter;
	private String timestamp;
	private int status;
	
	public Message(String uid, Drawable pic, String content, int counter, String timestamp, int status){
		this.uid = uid;
		this.pic = pic;
		this.content = content;
		this.counter = counter;
		this.timestamp = timestamp;
		this.status = status;
	}
	
	public String getUid(){
		return uid;
	}
	
	public Drawable getPic(){
		return pic;
	}
	
	public String getContent(){
		return content;
	}
	
	public int getCounter(){
		return counter;
	}
	
	public void setTimestamp(String timestamp){
		this.timestamp = timestamp;
	}
	
	public String getTimestamp(){
		return timestamp;
	}
	
	public void setStatus(int status){
		this.status = status;
	}
	
	public int getStatus(){
		return status;
	}
}
