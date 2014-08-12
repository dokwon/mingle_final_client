package com.example.mingle;
//package com.hmkcode.android;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

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
import java.io.UnsupportedEncodingException;
import java.lang.String;

/**
 * Created by Tempnote on 2014-06-02.
 */
public class HttpHelper extends AsyncTask<String, MingleUser, Integer>  {
	  public final static String USER_CONF = "com.example.mingle.USER_CONF";				//Intent data to pass on when user creation completes
	  public final static String JOIN_MINGLE = "com.example.mingle.JOIN_MINGLE";			//Indicator of user creation's complete
	  public final static String UPDATE_USER = "com.example.mingle.UPDATE_USER";			//Indicator of user update's complete
	  public final static String USER_LIST = "com.example.mingle.USER_LIST";				//Intent data to pass on when list of candidates are fetched
	  public final static String HANDLE_CANDIDATE = "com.example.mingle.HANDLE_CANDIDATE";	//Indicator of get candidates' complete
	  public final static String POP_LIST = "com.example.mingle.POP_LIST";					//Intent data to pass on when list of popular users are fetched
	  public final static String HANDLE_POP = "com.example.mingle.HANDLE_POP";				//Indicator of get popular users' complete
	  public final static String HANDLE_HTTP_ERROR = "com.example.mingle.HTTP_ERROR";		//Indicator of http error
	  public final static String VOTE_RESULT = "com.example.mingle.VOTE_RESULT";			//Intent data to pass on when vote request returns
	  public final static String HANDLE_VOTE_RESULT = "com.example.mingle.HANDLE_VOTE_RESULT";	//Indicator of vote complete
	  public final static String INIT_INFO = "com.example.mingle.INIT_INFO";				//Intent data to pass on when init info request returns
	  public final static String SET_INIT_INFO = "com.example.mingle.SET_INIT_INFO";		//Indicator of get init info complete

    private String server_url;				//URL of server
    private MingleApplication app;
    
    /* Constructor for HttpHelper */
    public HttpHelper(String url, MingleApplication curApp){
    	app = curApp; 
    	server_url = url+"/"; 
    }

    
    /* encode given string in utf-8 */
    private String encodeString(String str){
    	String encoded_str = "not available";
		try {
			encoded_str = URLEncoder.encode(str ,"utf-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return encoded_str;
    }
    
    /* Fetch data from HttpResponse */
    private String HttpResponseBody(HttpResponse response) { 
    	if(response == null) {
    		return "";
    	}
    	if(response.getStatusLine().getStatusCode() == 200)
        {
			HttpEntity entity = response.getEntity();
			assert (entity != null);
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
    
    /* Get vote question of the day*/
    public void getInitInfo() {
        String baseURL = server_url;
    	baseURL += "get_init_info";
    	
    	final String cps = baseURL;       
    	new Thread(new Runnable() {
    		public void run() {
    			HttpClient client = new DefaultHttpClient();
    	        HttpGet poster = new HttpGet(cps);
    	        HttpResponse response = null;
				try {
					response = client.execute(poster);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//handle http error
					Intent dispatcher = new Intent(app, SplashScreenActivity.class);
					dispatcher.setAction(HANDLE_HTTP_ERROR);
					LocalBroadcastManager.getInstance(app).sendBroadcast(dispatcher); 
					e.printStackTrace();
				}
				
				//Save question
				try {
					JSONObject init_obj = new JSONObject(HttpResponseBody(response));
					Intent dispatcher = new Intent(app, SplashScreenActivity.class);
					dispatcher.putExtra(INIT_INFO,init_obj.toString());
					dispatcher.setAction(SET_INIT_INFO);
					LocalBroadcastManager.getInstance(app).sendBroadcast(dispatcher); 
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
    		}
    	}).start();
    }
   
    /* Send login info along to the server, and receive UID */
    public void userCreateRequest( final MingleApplication app,String name, String sex, int num)  {
       	String baseURL = server_url.toString();
    	baseURL += "create_user?";
    	baseURL += "name=" + encodeString(name) + "&";
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
					HttpResponse response = PhotoPoster.postPhoto(app, cpy);
					JSONObject user_info = new JSONObject(HttpResponseBody(response));
					
					//join mingle
					Intent dispatcher = new Intent(app, MainActivity.class);
					dispatcher.putExtra(USER_CONF,user_info.toString());
					dispatcher.setAction(JOIN_MINGLE);
					LocalBroadcastManager.getInstance(app).sendBroadcast(dispatcher); 
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		
    		}
    	}).start();
    }
    
    /* Send update info to server */
    public void userUpdateRequest( final MingleApplication app,String name, String sex, int num)  {  	
    	String baseURL = server_url.toString();
    	baseURL += "update_user?";
    	baseURL += "uid=" + app.getMyUser().getUid() + "&";
    	baseURL += "name=" + encodeString(name) + "&";
    	baseURL += "sex=" + sex + "&";
    	baseURL += "num=" + (Integer.valueOf(num).toString()) + "&";
    	baseURL += "loc_long=" + (Float.valueOf(app.getLong())).toString() + "&";
    	baseURL += "loc_lat=" + (Float.valueOf(app.getLat())).toString() + "&";
    	baseURL += "photo_num=" + (Integer.valueOf(app.getPhotoPaths().size())).toString();
    	
    	final String cpy = baseURL;
    	
    	new Thread(new Runnable() {
    		public void run() {
    			try {
					
    				HttpResponse response = PhotoPoster.postPhoto(app, cpy);
    				HttpResponseBody(response);
    				
					//notify user for complete
					Intent dispatcher = new Intent(app, MainActivity.class);
					dispatcher.setAction(UPDATE_USER);
					LocalBroadcastManager.getInstance(app).sendBroadcast(dispatcher); 
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		
    		}
    	}).start();
    }
    
    /* Send vote request of uid to server */
    public void voteUser(String uid)  {
        String baseURL = server_url;
    	baseURL += "vote?";
    	baseURL += "uid=" + uid;
    	
    	final String voteURL = baseURL;
    	new Thread(new Runnable() {
    		public void run() {
    			HttpClient client = new DefaultHttpClient();
    	        HttpGet poster = new HttpGet(voteURL);
    	        HttpResponse response = null;
				try {
					response = client.execute(poster);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//handle http error
					Intent dispatcher = new Intent(app, ProfileActivity.class);
					dispatcher.setAction(HANDLE_HTTP_ERROR);
					LocalBroadcastManager.getInstance(app).sendBroadcast(dispatcher); 
					e.printStackTrace();				
				}
				
				try {
					JSONObject success_obj = new JSONObject(HttpResponseBody(response));
					//notify user of success
					Intent dispatcher = new Intent(app, ProfileActivity.class);
					dispatcher.putExtra(VOTE_RESULT,success_obj.getString("RESULT"));
					dispatcher.setAction(HANDLE_VOTE_RESULT);
					LocalBroadcastManager.getInstance(app).sendBroadcast(dispatcher); 
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}).start();
    }
    
    /* Request list of Candidates from Server */
	 public void requestUserList(String uid, final String sex, float latitude, float longitude, int dist_lim, int num_of_users, ArrayList<String> uid_list) {
        
        String baseURL = server_url;
    	baseURL += "get_list?";
    	baseURL += "sex=" + sex + "&";
    	baseURL += "dist_lim=" + (new Integer(dist_lim)).toString() + "&";
    	baseURL += "loc_long=" + (new Float(longitude)).toString() + "&";
    	baseURL += "loc_lat=" + (new Float(latitude)).toString() + "&";
    	baseURL += "list_num=" + (new Integer(num_of_users)).toString() + "&";
    	baseURL += "filter_2=" + (app.getGroupNumFilter()[0]? 1 : 0) + "&";
    	baseURL += "filter_3=" + (app.getGroupNumFilter()[1]? 1 : 0) + "&";
    	baseURL += "filter_4=" + (app.getGroupNumFilter()[2]? 1 : 0) + "&";
    	baseURL += "filter_5=" + (app.getGroupNumFilter()[3]? 1 : 0) + "&";
    	baseURL += "filter_6=" + (app.getGroupNumFilter()[4]? 1 : 0);

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
    			System.out.println("request candidate list: " +cps);
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
					//handle http error
					Intent dispatcher = new Intent(app, HuntActivity.class);
					dispatcher.setAction(HANDLE_HTTP_ERROR);
					LocalBroadcastManager.getInstance(app).sendBroadcast(dispatcher); 
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
    
    /* Request list of popular users from the server */
    public void requestVoteList() {
	 	String baseURL = server_url;
    	baseURL += "get_vote";
        
    	final String getVoteURL = baseURL;
       
    	new Thread(new Runnable() {
    		public void run() {
    			HttpClient client = new DefaultHttpClient();
    	        HttpGet poster = new HttpGet(getVoteURL);
    	        HttpResponse response = null;
				try {
					response = client.execute(poster);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//handle http error
					Intent dispatcher = new Intent(app, HuntActivity.class);
					dispatcher.setAction(HANDLE_HTTP_ERROR);
					LocalBroadcastManager.getInstance(app).sendBroadcast(dispatcher); 
					e.printStackTrace();
				}
				try {
					JSONObject vote_result = new JSONObject(HttpResponseBody(response));
		    		
		    		Intent dispatcher = new Intent(app, HuntActivity.class);
					dispatcher.putExtra(POP_LIST,vote_result.toString());
					dispatcher.setAction(HANDLE_POP);
					LocalBroadcastManager.getInstance(app).sendBroadcast(dispatcher);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}).start();
    }
    
    /* Request user's deactivation to the server */
    public void requestDeactivation(String uid){
    	String baseURL = server_url;
    	baseURL += "deactivate?";
    	baseURL += "uid=" + uid;
    	
    	final String deactURL = baseURL;
    	new Thread(new Runnable() {
    		public void run() {
    			HttpClient client = new DefaultHttpClient();
    	        HttpGet poster = new HttpGet(deactURL);
				try {
					HttpResponse response = client.execute(poster);
    				HttpResponseBody(response);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//handle http error
					Intent dispatcher = new Intent(app, HuntActivity.class);
					dispatcher.setAction(HANDLE_HTTP_ERROR);
					LocalBroadcastManager.getInstance(app).sendBroadcast(dispatcher); 
					e.printStackTrace();
				}
    		}
    	}).start();	
    }

    public void getNewUser(String uid) {
    	String baseURL = server_url;
    	baseURL += "get_user?";
    	baseURL += "uid=" + uid;
    	
    	final String getUserURL = baseURL;
    	final String new_uid = uid;
    	new Thread(new Runnable() {
    		public void run() {
    			HttpClient client = new DefaultHttpClient();
    	        HttpGet poster = new HttpGet(getUserURL);
    	        HttpResponse response = null;
    	        JSONObject success_obj = null;
				try {
					response = client.execute(poster);
					success_obj = new JSONObject(HttpResponseBody(response));
					app.createNewChoiceUser(new_uid, success_obj);
				} catch (Exception e) {
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

