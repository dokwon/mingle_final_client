package com.example.mingle;
//package com.hmkcode.android;

        
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import java.net.*;

import io.socket.*;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.example.mingle.MingleUser;        

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.String;

/**
 * Created by Tempnote on 2014-06-02.
 */
public class HttpHelper extends AsyncTask<String, MingleUser, Integer>  {

    private SocketIO socket = null;
    private Context currContext = null;

    
    public HttpHelper(String url, Context context){
    	
    	//Set up default settings for socket communication with server
        try {
           //SocketIO.setDefaultSSLSocketFactory(SSLContext.getDefault());
            socket = new SocketIO(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } /*catch (NoSuchAlgorithmException f) {
            f.printStackTrace();
        }*/

        if (socket == null) {
            System.out.println("Socket is not availiable");
            return;
        }

        socket.addHeader("Cookie", "cookie");
        currContext = context;

        
    }
    private String BitmapToString(Bitmap bmp) {
    	ByteArrayOutputStream stream = new ByteArrayOutputStream();
    	bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
    	
    	return new String(stream.toByteArray());
    
    }
    
    /*
     * Method for establishing socket connection
     * Contains actions required by client on different events
     */
    public void connectSocket(){
    	IOCallback iocb  = new IOCallback() {

            @Override
            public void onMessage(String data, IOAcknowledge ack) {
                System.out.println("Server said: " + data);
            }

            @Override
            public void onError(SocketIOException socketIOException) {
                System.out.println("an Error occured");

                socketIOException.printStackTrace();
            }

            @Override
            public void onDisconnect() {
                System.out.println("Connection terminated.");
            }

            @Override
            public void onConnect() {
                System.out.println("Connection established");        
            }

            @Override
            public void on(String event, IOAcknowledge ack, Object... args) {
                System.out.println("Server triggered event '" + event + "'");
                
                //When server asks for UID of current user, return it with RID
                if(event.equals("uid_query")){
                	System.out.println("uid_query_recved");
                	String uid = ((HuntActivity)currContext).getMyUid();
                	JSONObject uid_obj = new JSONObject();
                	try {
						uid_obj.put("uid", uid);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                	socket.emit("uid_query_result", uid_obj);
                
                //When server sends confirmation that the message sent by user is received,
                //update ChatRoom's message list
                } else if(event.equals("msg_from_user_conf")){
                	JSONObject msg_conf_obj = (JSONObject) args[0];
                	try{
                		String msg_recv_uid = msg_conf_obj.getString("recv_uid");
                		int msg_recv_counter = Integer.parseInt(msg_conf_obj.getString("msg_counter"));
                		String msg_ts = msg_conf_obj.getString("ts");

                		MingleUser curr_user = ((MingleApplication) currContext.getApplicationContext()).currUser;
                		curr_user.getChattingUser(msg_recv_uid).getChatRoom().updateMsgOnConf(msg_recv_counter, msg_ts);
                		
                	} catch (JSONException e){
                		e.printStackTrace();
                	}
                	if(currContext instanceof ChatroomActivity){
                		System.out.println("msg_from_user_conf: on chatroom activity updating list");
                		((ChatroomActivity)currContext).updateMessageList();
                	}
                	//
                
                //When server delivers a message from other user, update ChatRoom's message list
                //and send confirmation to server so that it knows client received the message
                } else if(event.equals("msg_to_user")){
                	JSONObject recv_msg_obj = (JSONObject) args[0];
					try {
						String chat_user_uid = recv_msg_obj.getString("send_uid");
						MingleUser curr_user = ((MingleApplication) currContext.getApplicationContext()).currUser;
						ChattableUser cu = curr_user.getChattableUser(chat_user_uid);
						if(cu == null){
							cu = curr_user.getChattingUser(chat_user_uid);
							
							//first message from none existing user
							if(cu == null){
								System.out.println("new user's message!");
								ChattableUser new_user = new ChattableUser(chat_user_uid, "", 0, 1, (Drawable) currContext.getResources().getDrawable(R.drawable.ic_launcher));
		    	              
								curr_user.addChattingUser(new_user);
								
								downloadPic(currContext.getApplicationContext(), new_user.getUid(), 0, false);
								
								//download profile also
							}
						
						//move user from chattable list to chatting list
						} else {
							curr_user.switchChattableToChatting(curr_user.getChattableUserPos(chat_user_uid));
							cu = curr_user.getChattingUser(chat_user_uid);
							cu.getChatRoom().setChatActive();
						}

						System.out.println("Http helper recv uid: " +chat_user_uid);
						String msg = recv_msg_obj.getString("msg");
						String msg_ts = recv_msg_obj.getString("ts");
                		curr_user.getChattingUser(chat_user_uid).recvMsgToChatRoom(msg, msg_ts);
                		// Save to local storage
            			//((MingleApplication)currContext.getApplicationContext()).dbHelper.insertMessages(chat_user_uid, chat_user_uid, msg, msg_ts);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(currContext instanceof HuntActivity){
						((HuntActivity)currContext).listsUpdate();
					}
					
					if(currContext instanceof ChatroomActivity){
						System.out.println("msg to user: on chatroom activity updating list");
						((ChatroomActivity)currContext).updateMessageList();
                	}
                	socket.emit("msg_to_user_conf");
                }
            }

            @Override
            public void onMessage(JSONObject json, IOAcknowledge ack) {
                try {
                    System.out.println("Server said:" + json.toString(2));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        socket.connect(iocb);
    }

    /*
    * Sends login info along to the server, and hopefully what will be returned
    * is the unique id of the user as well as some other useful information
    */
    public void userCreateRequest(final ArrayList<String> photos, String name, String sex, int number, float longitude, float latitude, String rid)  {
       
    	
    	String baseURL = "http://ec2-54-178-214-176.ap-northeast-1.compute.amazonaws.com:8080/";
    	baseURL += "create_user?";
    	baseURL += "name=" + name + "&";
    	baseURL += "sex=" + sex + "&";
    	baseURL += "num=" + (new Integer(number)).toString() + "&";
    	baseURL += "loc_long=" + (new Float(longitude)).toString() + "&";
    	baseURL += "loc_lat=" + (new Float(latitude)).toString() + "&";
    	baseURL += "photo_num=" + (new Integer(photos.size())).toString() + "&";
    	baseURL += "rid=" + rid;
    	
    	final String cpy = baseURL;
    	
    	new Thread(new Runnable() {
    		public void run() {
    			try {
					HttpResponse response = PhotoPoster.postPhoto(photos, cpy);
					JSONObject user_info = new JSONObject(HttpResponseBody(response));
					System.out.println(user_info);
					((MingleApplication) ((MainActivity)currContext).getApplication()).dbHelper.setMyUID(user_info.getString("UID"));
		        	((MainActivity)currContext).joinMingle(user_info);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		
    		}
    	}).start();
    }
    
    public void voteUser(String uid)  {
    	String baseURL = "http://ec2-54-178-214-176.ap-northeast-1.compute.amazonaws.com:8080/";
    	baseURL += "vote?";
    	baseURL += "uid=" + uid;
    	
    	final String voteURL = baseURL;
    	new Thread(new Runnable() {
    		public void run() {
    			System.out.println(voteURL);
    			HttpClient client = new DefaultHttpClient();
    	        HttpGet poster = new HttpGet(voteURL);
    	        HttpResponse response = null;
				try {
					response = client.execute(poster);
					System.out.println(response.toString());
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}).start();
    }
    
    //Fetch data from HttpResponse
    public String HttpResponseBody(HttpResponse response) { 
    	//System.out.println(Integer.valueOf(response.getStatusLine().getStatusCode()).toString());
    	if(response == null) {
    		return "";
    	}
    	if(response.getStatusLine().getStatusCode() == 200)
        {
			HttpEntity entity = response.getEntity();
			assert (entity != null);
			System.out.println("Entity:"+entity);
			if (entity != null) {
				String responseBody = "";
				try {
					responseBody = EntityUtils.toString(entity);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return responseBody.toString();
            }
        }
    	return null;
    }
    
    /* Method Name: changeContext
     * Should be called whenever the context changes.
     */
    public void changeContext (Context context){
        this.currContext = context;
    }
    
    public Bitmap getBitmapFromURL(String link) {
        /*--- this method downloads an Image from the given URL, 
         *  then decodes and returns a Bitmap object
         ---*/
        try {
            URL url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);

            return myBitmap;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void requestUserList(String uid, String sex, float latitude, float longitude, int dist_lim, int num_of_users) {
        
    	String baseURL = "http://ec2-54-178-214-176.ap-northeast-1.compute.amazonaws.com:8080/";
    	baseURL += "get_list?";
    	baseURL += "sex=" + sex + "&";
    	baseURL += "dist_lim=" + (new Integer(dist_lim)).toString() + "&";
    	baseURL += "loc_long=" + (new Float(longitude)).toString() + "&";
    	baseURL += "loc_lat=" + (new Float(latitude)).toString() + "&";
    	baseURL += "list_num=" + (new Integer(num_of_users)).toString();
    	
    	//Add list of ChattableUsers' uids to URL as parameter
    	ArrayList<ChattableUser> cu_list = ((MingleApplication) currContext.getApplicationContext()).currUser.getChattableUserList();
        int cu_list_size = cu_list.size();
        if(cu_list.size() > 0) baseURL += "&";
    	for (int i = 0; i < cu_list_size - 1; i++){
        	baseURL += "my_list["+i+"]=" + cu_list.get(i).getUid() + "&";
        }
        if(cu_list.size() > 0) baseURL += "my_list["+cu_list_size+"]="+cu_list.get(cu_list_size-1).getUid();
        
    	final String cps = baseURL;
       
    	//Start Thread that receives HTTP Response
    	new Thread(new Runnable() {
    		public void run() {
    			System.out.println(cps);
    			HttpClient client = new DefaultHttpClient();
    	        HttpGet poster = new HttpGet(cps);
    	        HttpResponse response = null;
				try {
					response = client.execute(poster);
					System.out.println(response.toString());
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//update ChattableUser list and dispatch image downloader
				try{
		    		JSONArray list_of_users = new JSONArray(HttpResponseBody(response));
		    		System.out.println(list_of_users.toString());
		    		for(int i = 0 ; i < list_of_users.length(); i++) {
		    	          try {
		    	              JSONObject shownUser = list_of_users.getJSONObject(i);    	             
		    	              ChattableUser new_user = new ChattableUser(shownUser.getString("UID"), shownUser.getString("COMM"), Integer.valueOf(shownUser.getString("NUM")), Integer.valueOf(shownUser.getString("PHOTO_NUM")), (Drawable) currContext.getResources().getDrawable(R.drawable.ic_launcher));

		    	              ((MingleApplication) currContext.getApplicationContext()).currUser.addChattableUser(new_user);
		    	              
		    	              downloadPic(currContext.getApplicationContext(), new_user.getUid(), 0, false);
		    	          } catch (JSONException e){
		    	              e.printStackTrace();
		    	          }
		    	    }
		    		if(currContext instanceof HuntActivity){
		    			System.out.println("update");
						((HuntActivity)currContext).listsUpdate();
					}
		    	} catch (JSONException je){
		    		je.printStackTrace();
		    	}
    		}
    	}).start();
    }
    
 public void requestVoteList() {
        
    	String baseURL = "http://ec2-54-178-214-176.ap-northeast-1.compute.amazonaws.com:8080/";
    	baseURL += "get_vote";
        
    	final String getVoteURL = baseURL;
       
    	//Start Thread that receives HTTP Response
    	new Thread(new Runnable() {
    		public void run() {
    			System.out.println(getVoteURL);
    			HttpClient client = new DefaultHttpClient();
    	        HttpGet poster = new HttpGet(getVoteURL);
    	        HttpResponse response = null;
				try {
					response = client.execute(poster);
					System.out.println(response.toString());
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//update ChattableUser list and dispatch image downloader
				try{
		    		JSONArray list_of_top = new JSONArray(HttpResponseBody(response));
		    		System.out.println("vote list: " +list_of_top.toString());
		    		ArrayList<ChattableUser> female_list = new ArrayList<ChattableUser>();
		    		ArrayList<ChattableUser> male_list = new ArrayList<ChattableUser>();
		    		for(int i = 0 ; i < list_of_top.length(); i++) {
		    	          try {
		    	              JSONObject shownUser = list_of_top.getJSONObject(i);    	             
		    	              ChattableUser new_user = new ChattableUser(shownUser.getString("UID"), shownUser.getString("COMM"), Integer.valueOf(shownUser.getString("NUM")), Integer.valueOf(shownUser.getString("PHOTO_NUM")), (Drawable) currContext.getResources().getDrawable(R.drawable.ic_launcher));

		    	              if(shownUser.getString("SEX").equals("F")) female_list.add(new_user);
		    	              else male_list.add(new_user);
		    	              
		    	          } catch (JSONException e){
		    	              e.printStackTrace();
		    	          }
		    	    }
		    		
		    		//add female and male top users to list
		    		((MingleApplication) currContext.getApplicationContext()).currUser.emptyTopList();
		    		for(int i = 0; i < female_list.size() || i < male_list.size(); i++){
		    			ChattableUser female_cu = null;
		    			ChattableUser male_cu = null;
		    			if(i < female_list.size()){
		    				female_cu = female_list.get(i);
		    				System.out.println("female in"+i + " "+female_cu.getUid());
		    			}
		    			if(i < male_list.size()){
		    				male_cu = male_list.get(i);
		    				System.out.println("male in"+i +" "+male_cu.getUid());
		    			}
		    			((MingleApplication) currContext.getApplicationContext()).currUser.addTopUsers(female_cu, male_cu);
		    			if(female_cu != null) downloadPic(currContext.getApplicationContext(), female_cu.getUid(), 0, true);
		    			if(male_cu != null) downloadPic(currContext.getApplicationContext(), male_cu.getUid(), 0, true);

		    		}
		    		
		    		if(currContext instanceof HuntActivity){
						((HuntActivity)currContext).topListUpdate();
					}
		    	} catch (JSONException je){
		    		je.printStackTrace();
		    	}
    		}
    	}).start();
    }
    
    //HttpHelper method to deliver user's message to server
    public void sendMessageToServer(String send_uid, String recv_uid, String msg, int msg_counter, boolean response_msg){
    	JSONObject msgObject = new JSONObject();
        try {
            msgObject.put("send_uid", send_uid);
            msgObject.put("recv_uid", recv_uid);
            msgObject.put("msg", msg);
            msgObject.put("msg_counter", msg_counter);
            
            ChatRoom curr_chatroom = ((MingleApplication) currContext.getApplicationContext()).currUser.getChattingUser(recv_uid).getChatRoom();
            if(curr_chatroom.isJustCreated()){
            	msgObject.put("identity", 2);
            	curr_chatroom.setChatActive();
            } else if(response_msg){
            	msgObject.put("identity", 1);
            } else {
            	msgObject.put("identity", 0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("msg_from_user", msgObject);
    }
    
    public void downloadPic(Context context, String uid, int photo_index, boolean top_list){
    	new ImageDownloader(context, uid, photo_index, top_list).execute();
    }

    //@Override
    protected Integer doInBackground(String... urls) {
        return 0;
    }

    //@Override
    protected void onProgressUpdate(Integer... progress) {
       // setProgressPercent(progress[0]);
    }

    //Override
    protected void onPostExecute(Long result) {
        //showDialog("Downloaded " + result + " bytes");
    }
    
    
  private class ImageDownloader extends AsyncTask<Void, Void, Void> {
	  	
	  	private Context context;
	  	private String url;
	  	private String uid;
	  	private int pic_index;
	  	private Bitmap bm;
	  	private boolean top_list;
	  	
	  	public ImageDownloader(Context context, String uid, int pic_index, boolean top_list) {
	  		this.context = context;
	  		String temp_url = "http://ec2-54-178-214-176.ap-northeast-1.compute.amazonaws.com:8080/photos/";
	  		temp_url += uid;
	  		if(pic_index < 0) temp_url += "/thumb.png";
	  		else temp_url += "/photo_" + String.valueOf(pic_index+1) + ".png";
	  		System.out.println(temp_url);
	  		this.url = temp_url;
	  		this.uid = uid;
	  		this.pic_index = pic_index;
	  		this.top_list = top_list;
	  	}
	  	
			@Override
			protected Void doInBackground(Void... params) {

				if (isCancelled()) {
					return null;
				}

				bm = ((MingleApplication) context).connectHelper.getBitmapFromURL(url);
	
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				MingleUser currUser = ((MingleApplication) context).currUser;
				ChattableUser cu;
				System.out.println("recev" + uid);
				if(top_list) cu = currUser.getTopUser(uid);
				else cu = currUser.getUser(uid);
				cu.setPic(pic_index, (Drawable) new BitmapDrawable(currContext.getResources(),bm));
				
				if(currContext instanceof HuntActivity){
					((HuntActivity)currContext).listsUpdate();
					((HuntActivity)currContext).topListUpdate();
				}
				
				if(currContext instanceof ProfileActivity){
					((ProfileActivity)currContext).updateView(pic_index);
				}

				super.onPostExecute(result);
			}

			@Override
			protected void onCancelled() {
		    }
  }
}