package ly.nativeapp.mingle;
//package com.hmkcode.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import java.net.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.socket.*;

import org.json.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.String;

/**
 * Created by Tempnote on 2014-06-02.
 */
public class Socket extends AsyncTask<String, MingleApplication, Integer>  {

    private SocketIO socket = null;
    private MingleApplication app; 
    private String url;
	public final static String DEACT_USER = "ly.nativeapp.mingle.DEACT_USER";	//Intent data to pass on when chatting user has been deactivated
	public final static String NO_USER_NOTI = "ly.nativeapp.mingle.NO_USER_NOTI";
	
	public Lock newMsgLock = new ReentrantLock();
    public Condition newMsgFetched = newMsgLock.newCondition();

    public Socket(String url, MingleApplication currApp){
    	this.url = url;
        this.app=currApp;   
    }
    
    /*
     * Method for establishing socket connection
     * Contains actions required by client on different events
     */
    public void connectSocket(){
    	if(isSocketConnected()) return;
    	
    	else {
    		if(socket != null) socket.disconnect();
    		
    		//Set up default settings for socket communication with server
    		try {
    			socket = new SocketIO(url);
    		} catch (MalformedURLException e) {
    			e.printStackTrace();
    		}
        
    		if (socket == null) {
    			return;
    		}

    		socket.addHeader("Cookie", "cookie");
    	
    		IOCallback iocb  = new IOCallback() {
    			
    			@Override
    			public void onMessage(String data, IOAcknowledge ack) {
    			}

    			@Override
    			public void onError(SocketIOException socketIOException) {
    				socketIOException.printStackTrace();
    			}

    			@Override
    			public void onDisconnect() {
    				socket = null;
    			}

    			@Override
    			public void onConnect() {
    			}

    			@Override
    			public void on(String event, IOAcknowledge ack, Object... args) {                
    				//When server asks for UID of current user, return it with RID
    				if(event.equals("uid_query")){
    					String uid = app.getMyUser().getUid();
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
                	
    					app.handleMsgConf(msg_conf_obj);
                    
    					//When server delivers a message from other user, update ChatRoom's message list
    					//and send confirmation to server so that it knows client received the message
    				} else if(event.equals("msg_to_user")){
    					JSONObject get_msg_obj = (JSONObject) args[0];
    					try {
							if(app.getMyUser().getUid().equals(get_msg_obj.getString("recv_uid"))){
								newMsgLock.lock();
								app.handleIncomingMsg(get_msg_obj);
								newMsgFetched.signal();
								newMsgLock.unlock();
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    					socket.emit("msg_to_user_conf");
    				} else if(event.equals("no_user_exist")){				
						Intent dispatcher = new Intent(app, ChatroomActivity.class);
    					dispatcher.putExtra(DEACT_USER, args[0].toString());
    					dispatcher.setAction(NO_USER_NOTI);
    					LocalBroadcastManager.getInstance(app).sendBroadcast(dispatcher);	
    				}
    			}

    			@Override
    			public void onMessage(JSONObject json, IOAcknowledge ack) {
    			}
    		};
    		socket.connect(iocb);
    	}
    }
    
    public boolean isSocketConnected(){
    	return (socket != null && socket.isConnected());
    }
    
    
    public void disconnectSocket(){
    	if(socket != null)
    		socket.disconnect();
    	socket = null;
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
    
    //HttpHelper method to deliver user's message to server
    public void sendMessageToServer(JSONObject msgObject){
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
