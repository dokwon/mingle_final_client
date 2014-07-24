package com.example.mingle;
//package com.hmkcode.android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import java.net.*;

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
	
    public final static String MSG_CONF = "com.example.mingle.MSG_CONF";	//Intent data to pass on when new Chatroom Activity started
    public final static String HANDLE_MSG_CONF = "com.example.mingle.HANDLE_MSG_CONF";	//Intent data to pass on when new Chatroom Activity started
    public final static String GET_MSG = "com.example.mingle.GET_MSG";	//Intent data to pass on when new Chatroom Activity started
    public final static String HANDLE_GET_MSG = "com.example.mingle.HANDLE_GET_MSG";	//Intent data to pass on when new Chatroom Activity started
    
    public Socket(String url, MingleApplication currApp){
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
        app=currApp;

        
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
                	
					Intent dispatcher = new Intent(app, ChatroomActivity.class);
					dispatcher.putExtra(MSG_CONF, msg_conf_obj.toString());
					dispatcher.setAction(HANDLE_MSG_CONF);
					LocalBroadcastManager.getInstance(app).sendBroadcast(dispatcher);
					               
                //When server delivers a message from other user, update ChatRoom's message list
                //and send confirmation to server so that it knows client received the message
                } else if(event.equals("msg_to_user")){
                	JSONObject get_msg_obj = (JSONObject) args[0];

					Intent dispatcher = new Intent(app, ChatroomActivity.class);
					dispatcher.putExtra(GET_MSG,get_msg_obj.toString());
					dispatcher.setAction(HANDLE_GET_MSG);
					LocalBroadcastManager.getInstance(app).sendBroadcast(dispatcher);
					
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