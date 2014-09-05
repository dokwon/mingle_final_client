package ly.nativeapp.mingle;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import android.graphics.drawable.Drawable;

public class MingleUser {
	String uid;
	String name;
	int num;
	Drawable thumb;
	Boolean thumb_bool;
	ArrayList<Drawable> pics;
	ArrayList<Boolean> pics_bool;
	String sex;
	boolean voted;
	ArrayList<Message> msg_list;
	int my_msg_counter;
	int new_msg_num;
    boolean inChat;
    float distance;
    
    int rank;

    public MingleUser(String uid, String name, int num, int photo_num, Drawable drawable, String sex, float distance, int rank) {
          super();
          this.uid = uid;
          this.name = name;
          this.num = num;
          this.sex = sex;
          this.voted = false;
          this.msg_list = new ArrayList<Message>();
          this.my_msg_counter = -1;
          this.thumb = drawable;
          this.thumb_bool = false;
          this.pics = new ArrayList<Drawable>();
          this.pics_bool = new ArrayList<Boolean>();
          this.inChat = false;
          for (int i = 0; i < photo_num; i++){
        	  pics.add(drawable);
        	  pics_bool.add(false);
          }
          this.distance = distance;
          this.new_msg_num = 0;
          this.rank = rank;
    }

    public void setDistance(float dist){
    	this.distance = dist;
    }
    
    public float getDistance(){
    	return this.distance;
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
    
    public int getRank(){
    	return rank;
    }
    
    public void setRank(int rank){
    	this.rank = rank;
    }
    
    public boolean alreadyVoted(){
    	return voted;
    }
    
    public void setVoted(boolean voted){
    	this.voted = voted;
    }
  
    public int getPhotoNum(){
    	return pics.size();
    }
    
    public ArrayList<Message> getMsgList(){
    	return msg_list;
    }
    
    public int getMsgCounter(){
    	return my_msg_counter;
    }
    
    public void incrementMsgCounter(){
    	my_msg_counter++;
    }
    
    public Drawable getPic(int index) {
    	  if(pics.size() <= index || index < -1) return null;
    	  if(index == -1) return thumb;
          return pics.get(index);
    }
    
    public void setPic(int index, Drawable pic){
    	if(index == -1) {
    		thumb = pic;
    		thumb_bool = true;
    	} else {
    		pics.set(index, pic);
    		pics_bool.set(index, true);
    	}
    }
    
    public void removePic(int index){
    	if(pics.size() > index + 1) {
	    	pics.remove(index);
	    	pics_bool.remove(index);
    	}
    }
    
    public void addPic(Drawable pic){
    	pics.add(pic);
    	pics_bool.add(true);
    }
    
    public void addBlankPic(Drawable pic){
    	pics.add(pic);
    	pics_bool.add(false);
    }
    
    public void clearPics(){
    	pics.clear();
    	pics_bool.clear();
    }
    
    public boolean isPicAvail(int index){
    	if(index == -1) return thumb_bool;
    	return pics_bool.get(index);
    }
    
    public boolean isMsgListEmpty(){
    	if(msg_list.size() == 0) return true;
    	else return false;
    }
    
    public void addMsg(String msg, int msg_counter, int status){
    	Date date= new Date();
		Timestamp timestamp = (new Timestamp(date.getTime()));
		String curr_time = timestamp.toString().replaceAll("\\..+", "");
		Message msg_obj = new Message(msg, msg_counter, curr_time, 0, true);
		msg_list.add(msg_obj);
    }
    
    public void addMsgObj(Message msg){
    	msg_list.add(msg);
		Collections.sort(msg_list, new MsgComparator());
    }
    
    public boolean updateMsgOnConf(int counter, String msg_ts){
		for(Message obj : msg_list){
			if(obj.getCounter()==counter){
				obj.setStatus(1);    				
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
    
    public Message getLastMsg(){
		if(msg_list.size() > 0) return msg_list.get(msg_list.size() - 1);
		return null;
	}
    
    public void incrNewMsgNum(){
    	this.new_msg_num++;
    }
    
    public void zeroNewMsgNum(){
    	this.new_msg_num = 0;
    }
    
    public int getNewMsgNum(){
    	return this.new_msg_num;
    }
    class MsgComparator implements Comparator<Message> {
        public int compare(Message msg1, Message msg2) {
        	return (msg1.getTimestamp()).compareTo(msg2.getTimestamp());
        }
    }
}
