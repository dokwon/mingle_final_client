package ly.nativeapp.mingle;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.net.*;
import java.util.ArrayList;


import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.String;

/**
 * Created by Tempnote on 2014-06-02.
 */
public class HttpHelper extends AsyncTask<String, MingleUser, Integer>  {
	  public final static String USER_CONF = "ly.nativeapp.mingle.USER_CONF";				//Intent data to pass on when user creation completes
	  public final static String JOIN_MINGLE = "ly.nativeapp.mingle.JOIN_MINGLE";			//Indicator of user creation's complete
	  public final static String UPDATE_USER = "ly.nativeapp.mingle.UPDATE_USER";			//Indicator of user update's complete
	  public final static String USER_LIST = "ly.nativeapp.mingle.USER_LIST";				//Intent data to pass on when list of candidates are fetched
	  public final static String HANDLE_CANDIDATE = "ly.nativeapp.mingle.HANDLE_CANDIDATE";	//Indicator of get candidates' complete
	  public final static String POP_LIST = "ly.nativeapp.mingle.POP_LIST";					//Intent data to pass on when list of popular users are fetched
	  public final static String HANDLE_POP = "ly.nativeapp.mingle.HANDLE_POP";				//Indicator of get popular users' complete
	  public final static String HANDLE_HTTP_ERROR = "ly.nativeapp.mingle.HTTP_ERROR";		//Indicator of http error
	  public final static String VOTE_RESULT = "ly.nativeapp.mingle.VOTE_RESULT";			//Intent data to pass on when vote request returns
	  public final static String HANDLE_VOTE_RESULT = "ly.nativeapp.mingle.HANDLE_VOTE_RESULT";	//Indicator of vote complete
	  public final static String INIT_INFO = "ly.nativeapp.mingle.INIT_INFO";				//Intent data to pass on when init info request returns
	  public final static String SET_INIT_INFO = "ly.nativeapp.mingle.SET_INIT_INFO";		//Indicator of get init info complete

    private String server_url;				//URL of server
    private MingleApplication app;
    
    private Lock initLock = new ReentrantLock();
    private Condition initDataFetched = initLock.newCondition();
    private Lock newUserLock = new ReentrantLock();
    private Condition newUserFetched = newUserLock.newCondition();
    private Lock deactLock = new ReentrantLock();
    private Condition deactResult = deactLock.newCondition();
    private Lock uidLock = new ReentrantLock();
    private Condition uidChecked = uidLock.newCondition();
    private Lock voteLock = new ReentrantLock();
    private Condition voteResult = voteLock.newCondition();
    private Lock voteListLock = new ReentrantLock();
    private Condition voteListResult = voteListLock.newCondition();
    
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
    	initLock.lock();
        String baseURL = server_url;
    	baseURL += "get_init_info";
    	
    	final String cps = baseURL;       
    	new Thread(new Runnable() {
    		public void run() {
    			initLock.lock();
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
				initDataFetched.signal();
				initLock.unlock();
    		}
    	}).start();
    	
    	initDataFetched.awaitUninterruptibly();
    	initLock.unlock();
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
					JSONObject user_info = new JSONObject(HttpResponseBody(response));
    				
					//notify user for complete
					Intent dispatcher = new Intent(app, MainActivity.class);
					dispatcher.putExtra(USER_CONF,user_info.toString());
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
    	voteLock.lock();
        String baseURL = server_url;
    	baseURL += "vote?";
    	baseURL += "voted_uid=" + uid + "&";
    	baseURL += "uid=" + app.getMyUser().getUid();
    	
    	final String voteURL = baseURL;
    	new Thread(new Runnable() {
    		public void run() {
    			voteLock.lock();
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
				voteResult.signal();
				voteLock.unlock();
    		}
    	}).start();
    	voteResult.awaitUninterruptibly();
    	voteLock.unlock();
    }
    
    /* Request list of Candidates from Server */
	 public void requestUserList(String uid, final String sex, int num, float latitude, float longitude, int dist_lim, int num_of_users, ArrayList<String> uid_list) {
        
        String baseURL = server_url;
    	baseURL += "get_list?";
    	baseURL += "sex=" + sex + "&";
    	baseURL += "num=" + (new Integer(num)).toString() + "&";
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
    	voteListLock.lock();
	 	String baseURL = server_url;
    	baseURL += "get_vote";
        
    	final String getVoteURL = baseURL;
       
    	new Thread(new Runnable() {
    		public void run() {
    			voteListLock.lock();
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
				voteListResult.signal();
				voteListLock.unlock();
    		}
    	}).start();
    	voteListResult.awaitUninterruptibly();
    	voteListLock.unlock();
    }
    
    /* Request user's deactivation to the server */
    public void requestDeactivation(String uid){
    	deactLock.lock();
    	String baseURL = server_url;
    	baseURL += "deactivate?";
    	baseURL += "uid=" + uid;
    	
    	final String deactURL = baseURL;
    	new Thread(new Runnable() {
    		public void run() {
    			deactLock.lock();
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
				deactResult.signal();
				deactLock.unlock();
    		}
    	}).start();	
    	deactResult.awaitUninterruptibly();
    	deactLock.unlock();
    }

    public void getNewUser(String uid, final String user_type) {
    	newUserLock.lock();
    	String baseURL = server_url;
    	baseURL += "get_user?";
    	baseURL += "uid=" + uid;
    	
    	final String getUserURL = baseURL;
    	final String new_uid = uid;
    	new Thread(new Runnable() {
    		public void run() {
    			newUserLock.lock();
    			HttpClient client = new DefaultHttpClient();
    	        HttpGet poster = new HttpGet(getUserURL);
    	        HttpResponse response = null;
    	        JSONObject success_obj = null;
				try {
					response = client.execute(poster);
					success_obj = new JSONObject(HttpResponseBody(response));
					app.setNewUser(new_uid, success_obj, user_type);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				newUserFetched.signal();
    			newUserLock.unlock();
    		}
    	}).start();	
    	newUserFetched.awaitUninterruptibly();
    	newUserLock.unlock();
    }
    
    public void checkUidValidity(String uid) {
    	uidLock.lock();
    	String baseURL = server_url;
    	baseURL += "check_validity?";
    	baseURL += "uid=" + uid;
    	
    	final String checkValidityURL = baseURL;
    	new Thread(new Runnable() {
    		public void run() {
    			uidLock.lock();
    			HttpClient client = new DefaultHttpClient();
    	        HttpGet poster = new HttpGet(checkValidityURL);
    	        HttpResponse response = null;
    	        
				try {
					response = client.execute(poster);
					String responseString = HttpResponseBody(response);
					if(responseString.equals("false")) {
						app.deactivateApp();
						app.setNeedRefreshAccount();
					}
						
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				uidChecked.signal();
    			uidLock.unlock();
    		}
    	}).start();	
    	uidChecked.awaitUninterruptibly();
    	uidLock.unlock();
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

