package com.example.mingle;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;

public class ChattableUser {
	String user_uid;
	String comment;
	int num;
	ArrayList<Drawable> pics;
    

    public ChattableUser(String user_uid, String comment, int num) {
          super();
          this.user_uid = user_uid;
          this.comment = comment;
          this.num = num;
          this.pics = new ArrayList<Drawable>();
          
    }
    
    public void addPics(ArrayList<Drawable> friendPics) {
    	pics.addAll(friendPics);
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
  
    public Drawable getPic(int index) {
          return pics.get(index);
    }
    
    public boolean hasPic() {
    	return pics.size() > 0;
    }
    
    public void addpic(Drawable pic) {
          pics.add(pic);
    }

}
