package com.example.mingle;
//package com.hmkcode.android;

        
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.net.*;

import java.util.ArrayList;
import com.example.mingle.MingleUser;        

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.String;

/**
 * Created by Tempnote on 2014-06-02.
 */
public class HttpHelper extends AsyncTask<String, MingleUser, Integer>  {
	  public final static String USER_CONF = "com.example.mingle.USER_CONF";	//Intent data to pass on when new Chatroom Activity started
	  public final static String JOIN_MINGLE = "com.example.mingle.JOIN_MINGLE";	//Intent data to pass on when new Chatroom Activity started
	  public final static String USER_LIST = "com.example.mingle.USER_LIST";	//Intent data to pass on when new Chatroom Activity started
	  public final static String HANDLE_CANDIDATE = "com.example.mingle.HANDLE_CANDIDATE";	//Intent data to pass on when new Chatroom Activity started
	  public final static String POP_LIST = "com.example.mingle.POP_LIST";	//Intent data to pass on when new Chatroom Activity started
	  public final static String HANDLE_POP = "com.example.mingle.HANDLE_POP";	//Intent data to pass on when new Chatroom Activity started

    private String server_url;
    
    private MingleApplication app; 
    
    public HttpHelper(String url, MingleApplication curApp){
    	app = curApp; 
    	server_url = url;   
    }
   
    /*
    * Sends login info along to the server, and hopefully what will be returned
    * is the unique id of the user as well as some other useful information
    */
    public void userCreateRequest( final MingleApplication app,String name, String sex, int num)  {
        
    	
    	String baseURL = server_url.toString();
    	baseURL += "create_user?";
    	baseURL += "name=" + name + "&";
    	baseURL += "sex=" + sex + "&";
    	baseURL += "num=" + (Integer.valueOf(num).toString()) + "&";
    	baseURL += "loc_long=" + (Float.valueOf(app.getLong())).toString() + "&";
    	baseURL += "loc_lat=" + (Float.valueOf(app.getLat())).toString() + "&";
    	baseURL += "photo_num=" + (Integer.valueOf(app.getPhotoPaths().size())).toString() + "&";
    	baseURL += "rid=" + app.getRid();
    	
    	final String cpy = baseURL;
    	
    	new Thread(new Runnable() {
    		public void run() {
    			try {
					HttpResponse response = PhotoPoster.postPhoto(app.getPhotoPaths(), cpy);
					JSONObject user_info = new JSONObject(HttpResponseBody(response));
					System.out.println(user_info);
					
					//join mingle
					Intent dispatcher = new Intent(app, MainActivity.class);
					dispatcher.putExtra(USER_CONF,user_info.toString());
					dispatcher.setAction(JOIN_MINGLE);
					LocalBroadcastManager.getInstance(app).sendBroadcast(dispatcher); 
					Log.i("receiver", "dwem?");
					//app.startService(dispatcher);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		
    		}
    	}).start();
    }
    
    public void voteUser(String uid)  {
    	String baseURL = server_url.toString();
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

    public void requestUserList(String uid, final String sex, float latitude, float longitude, int dist_lim, int num_of_users, ArrayList<String> uid_list) {
        
    	String baseURL = server_url.toString();
    	baseURL += "get_list?";
    	baseURL += "sex=" + sex + "&";
    	baseURL += "dist_lim=" + (Integer.valueOf(dist_lim)).toString() + "&";
    	baseURL += "loc_long=" + (Float.valueOf(longitude)).toString() + "&";
    	baseURL += "loc_lat=" + (Float.valueOf(latitude)).toString() + "&";
    	baseURL += "list_num=" + (Integer.valueOf(num_of_users)).toString();
    	
    	//Add list of MingleUsers' uids to URL as parameter
        int uid_list_size = uid_list.size();
        if(uid_list_size > 0) baseURL += "&";
    	for (int i = 0; i < uid_list_size - 1; i++){
        	baseURL += "my_list["+i+"]=" + uid_list.get(i) + "&";
        }
        if(uid_list_size > 0) baseURL += "my_list["+uid_list_size+"]="+uid_list.get(uid_list_size-1);
        
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
				
				
				try {
					JSONArray list_of_users = new JSONArray(HttpResponseBody(response));
					//handle candidate
					Intent dispatcher = new Intent(app, HuntActivity.class);
					dispatcher.putExtra(USER_LIST,list_of_users.toString());
					dispatcher.setAction(HANDLE_CANDIDATE);
					LocalBroadcastManager.getInstance(app).sendBroadcast(dispatcher); 
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
    		}
    	}).start();
    }
    
 public void requestVoteList() {
        
    	String baseURL = server_url.toString();
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
				try {
					JSONArray list_of_top = new JSONArray(HttpResponseBody(response));
		    		
		    		Intent dispatcher = new Intent(app, HuntActivity.class);
					dispatcher.putExtra(POP_LIST,list_of_top.toString());
					dispatcher.setAction(HANDLE_POP);
					LocalBroadcastManager.getInstance(app).sendBroadcast(dispatcher);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}).start();
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