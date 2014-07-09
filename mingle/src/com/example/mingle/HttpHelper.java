package com.example.mingle;
//package com.hmkcode.android;

        
        import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import java.net.*;
        import io.socket.*;
import java.util.ArrayList;
import java.util.Arrays;
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
//import com.hmkcode.android.vo.Person;
        import org.json.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.String;

/**
 * Created by Tempnote on 2014-06-02.
 */
public class HttpHelper extends AsyncTask<String, MingleUser, Integer>  {

    private SocketIO socket = null;
    private Context currContext = null;
    private ArrayList<ChatRoom> chat_room_list;
    
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
						uid_obj.put("rid", 42);
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
                		((MingleApplication) currContext.getApplicationContext()).currUser.updateMsgToRoom(msg_recv_uid, msg_recv_counter, msg_ts);
                	} catch (JSONException e){
                		e.printStackTrace();
                	}
                	if(currContext instanceof ChatroomActivity){
                		System.out.println("on chatroom activity updating list");
                		((ChatroomActivity)currContext).updateMessageList();
                	}
                	//
                
                //When server delivers a message from other user, update ChatRoom's message list
                //and send confirmation to server so that it knows client received the message
                } else if(event.equals("msg_to_user")){
                	JSONObject recv_msg_obj = (JSONObject) args[0];
					try {
						String chat_user_uid = recv_msg_obj.getString("send_uid");
						ChatRoom chatroom = ((MingleApplication) currContext.getApplicationContext()).currUser.getChatRoom(chat_user_uid);
						if(chatroom  == null){
							//Instantiate a chat room
							((MingleApplication) currContext.getApplicationContext()).currUser.addChatRoom(chat_user_uid);
							((MingleApplication) currContext.getApplicationContext()).currUser.getChatRoom(chat_user_uid).setChatActive();
						
							//Remove from User List if available
							int pos = ((MingleApplication) currContext.getApplicationContext()).currUser.getChattableUserPos(chat_user_uid);
							if(pos >= 0){
								((MingleApplication) currContext.getApplicationContext()).currUser.removeChattableUser(pos);
								if(currContext instanceof HuntActivity){
									((HuntActivity)currContext).listsUpdate();
								}
							}
						}
                		((MingleApplication) currContext.getApplicationContext()).currUser.getChatRoom(chat_user_uid).addRecvMsg(chat_user_uid,recv_msg_obj.getString("msg"),recv_msg_obj.getString("ts"));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(currContext instanceof ChatroomActivity){
                		System.out.println("on chatroom activity updating list");
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
    
    
    
    private void postPhoto(final ArrayList<String> photoPaths, final String UID) {

    	final String baseURL = "http://ec2-54-178-214-176.ap-northeast-1.compute.amazonaws.com:8080/";
    	
        new Thread(new Runnable() {
    		public void run() {
    			try {
					String response = PhotoPoster.postPhoto(photoPaths, UID, baseURL);
					System.out.println(response);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		
    		}
    	}).start();

    }
    
    /*
    * Sends login info along to the server, and hopefully what will be returned
    * is the unique id of the user as well as some other useful information
    */
    public void userCreateRequest(final ArrayList<String> photos, String comment, String sex, int number, float longitude, float latitude)  {
       
    	
    	String baseURL = "http://ec2-54-178-214-176.ap-northeast-1.compute.amazonaws.com:8080/";
    	baseURL += "user_create?";
    	baseURL += "comm=" +comment + "&";
    	baseURL += "sex=" + sex + "&";
    	baseURL += "num=" + (new Integer(number)).toString() + "&";
    	baseURL += "loc_long=" + (new Float(longitude)).toString() + "&";
    	baseURL += "loc_lat=" + (new Float(latitude)).toString();
    	
    	final String cpy = baseURL;
       
    	//Start Thread that receives HTTP Response
    	new Thread(new Runnable() {
    		public void run() {
    			HttpClient client = new DefaultHttpClient();
    	        HttpGet poster = new HttpGet(cpy);
    	        HttpResponse response = null;
				try {
					response = client.execute(poster);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try{
					 
		    		JSONObject user_info = new JSONObject(HttpResponseBody(response));
		    		postPhoto(photos, user_info.getString("UID"));
		    		((MingleApplication) ((MainActivity)currContext).getApplication()).dbHelper.setMyUID(user_info.getString("UID"));
		        	((MainActivity)currContext).joinMingle(user_info);
		    	} catch (JSONException je){
		    		je.printStackTrace();
		    	}
    		}
    	}).start();
    }
    
    //Fetch data from HttpResponse
    public String HttpResponseBody(HttpResponse response) { 
    	//System.out.println(Integer.valueOf(response.getStatusLine().getStatusCode()).toString());
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

    public void requestUserList(String uid, String sex, float latitude, float longitude, int dist_lim, int num_of_users) {
        
    	String baseURL = "http://ec2-54-178-214-176.ap-northeast-1.compute.amazonaws.com:8080/";
    	baseURL += "get_list?";
    	baseURL += "sex=" + sex + "&";
    	baseURL += "dist_lim=" + (new Integer(dist_lim)).toString() + "&";
    	baseURL += "loc_long=" + (new Float(longitude)).toString() + "&";
    	baseURL += "loc_lat=" + (new Float(latitude)).toString() + "&";
    	baseURL += "list_num=" + (new Integer(num_of_users)).toString();
    	
    	//Add list of ChattableUsers' uids to URL as parameter
    	ArrayList<ChattableUser> cu_list = ((MingleApplication) currContext.getApplicationContext()).currUser.getChattableUsers();
        int cu_list_size = cu_list.size();
        if(cu_list.size() > 0) baseURL += "&";
    	for (int i = 0; i < cu_list_size - 1; i++){
        	baseURL += "my_list["+i+"]=" + cu_list.get(i).getUid() + "&";
        }
        if(cu_list.size() > 0) baseURL += "my_list["+cu_list_size+"]="+cu_list.get(cu_list_size-1).getUid();
        else baseURL += "&my_list[0]=hello&my_list[1]=hi";
        
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
				
				try{
		    		final JSONArray list_of_users = new JSONArray(HttpResponseBody(response));
	                ((AllChatFragment)((HuntActivity)currContext).allChatFragment).updateList(list_of_users);
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
            
            ChatRoom curr_chatroom = ((MingleApplication) currContext.getApplicationContext()).currUser.getChatRoom(recv_uid);
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
}