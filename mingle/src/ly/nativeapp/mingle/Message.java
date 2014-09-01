package ly.nativeapp.mingle;

public class Message {
	private String content;
	private int counter;
	private String timestamp;
	private int status;
	private boolean is_me;
	
	public Message(String content, int counter, String timestamp, int status, boolean is_me){
		this.content = content;
		this.counter = counter;
		this.timestamp = timestamp;
		this.status = status;
		this.is_me = is_me;
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
	
	public boolean isMyMsg(){
		return is_me;
	}
}
