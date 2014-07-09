package com.example.mingle;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.fortysevendeg.swipelistview.SwipeListView.OnLoadMoreListener;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A dummy fragment representing a section of the app
 */

public class AllChatFragment extends Fragment {
  public static final String ARG_SECTION_NUMBER = "placeholder_text";
  public final static String USER_UID = "com.example.mingle.USER_SEL";	//Intent data to pass on when new Chatroom Activity started

  public SwipeListView allchatlistview;	//Listview for chattable users
  private ArrayList<ChattableUser>user_list;
  private AllChatAdapter adapter;
  
  private Activity parent;	//Parent Activity: HuntActivity
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
	  parent = getActivity();

	  
	  View rootView = inflater.inflate(R.layout.fragment_all_chat, container, false);
	  
	  allchatlistview=  (SwipeListView)(rootView.findViewById(R.id.All));
      user_list = ((MingleApplication) parent.getApplication()).currUser.getChattableUsers();
      adapter=new AllChatAdapter(parent, R.layout.allchat_row,user_list);
	  
      
      
      final Activity curActivity = parent;
      
      allchatlistview.setSwipeListViewListener(new BaseSwipeListViewListener() {
          @Override
          public void onOpened(int position, boolean toRight) {
          }
  
          @Override
          public void onClosed(int position, boolean fromRight) {
          }
  
          @Override
          public void onListChanged() {
          }
  
          @Override
          public void onMove(int position, float x) {
          }
  
          @Override
          public void onStartOpen(int position, int action, boolean right) {
              Log.d("swipe", String.format("onStartOpen %d - action %d", position, action)); 
              allchatlistview.openAnimate(position); //when you touch front view it will open
              
              MingleUser currentUser = ((MingleApplication) parent.getApplication()).currUser;
              ChattableUser chat_user_obj = currentUser.getChattableUser(position);
              String chat_user_uid = chat_user_obj.getUid();
              
             
              
              //Instantiate a chat room
              currentUser.addChatRoom(chat_user_uid);
              // Create chatroom in local sqlite
              ((MingleApplication) parent.getApplication()).dbHelper.insertNewUID(chat_user_uid);
             
           	  //Remove selected user from ChattableUser list
           	  ((MingleApplication) parent.getApplication()).currUser.removeChattableUser(position);
           	  ((HuntActivity)parent).listsUpdate();
              
              Intent chat_intent = new Intent(curActivity, ChatroomActivity.class);
              chat_intent.putExtra(USER_UID, chat_user_obj.getUid());
              curActivity.startActivity(chat_intent);
              
          }
  
          @Override
          public void onStartClose(int position, boolean right) {
              Log.d("swipe", String.format("onStartClose %d", position));
          }
  
          @Override
          public void onClickFrontView(int position) {
              Log.d("swipe", String.format("onClickFrontView %d", position));

          }
  
          @Override
          public void onClickBackView(int position) {
              Log.d("swipe", String.format("onClickBackView %d", position));
  
              allchatlistview.closeAnimate(position);//when you touch back view it will close
          }
  
          @Override
          public void onDismiss(int[] reverseSortedPositions) {
  
          }
          
      });
      
      //These are the swipe listview settings. you can change these
      //setting as your requirement
      allchatlistview.setSwipeMode(SwipeListView.SWIPE_MODE_LEFT); // there are five swiping modes
      allchatlistview.setSwipeActionLeft(SwipeListView.SWIPE_ACTION_REVEAL); //there are four swipe actions
      allchatlistview.setSwipeActionRight(SwipeListView.SWIPE_ACTION_REVEAL);
      allchatlistview.setOffsetLeft(convertDpToPixel(0f)); // left side offset
      allchatlistview.setOffsetRight(convertDpToPixel(0f)); // right side offset
      allchatlistview.setAnimationTime(50); // animation time
      allchatlistview.setSwipeOpenOnLongPress(true); // enable or disable SwipeOpenOnLongPress
      
      allchatlistview.setAdapter(adapter);
     final ApplicationWrapper wrapper = new ApplicationWrapper(parent.getApplication());
      allchatlistview.setOnLoadMoreListener(new OnLoadMoreListener() {
          public void onLoadMore() {
          	new LoadDataTask(wrapper.curApp, 5).execute();
          }
      });
      
      System.out.println("AllchatFrag oncreate compleate");
    return rootView;
  }
  
  public void listDataChanged(){
	  parent.runOnUiThread(new Runnable() {
	  		public void run() {
	  			adapter.notifyDataSetChanged();
	  		}
	  });
  }
  
  public void loadNewMatches(Activity par) {
	  parent = par;
	  new LoadDataTask(parent.getApplication(), 10).execute();
  }
  
  private class LoadDataTask extends AsyncTask<Void, Void, Void> {
  	
  	private Application curApp;
  	
  	private int load_num;
  	
  	public LoadDataTask(Application app, int load_num) {
  		curApp = app;
  		this.load_num = load_num;
  	}
  	
		@Override
		protected Void doInBackground(Void... params) {

			if (isCancelled()) {
				return null;
			}

	        
			MingleUser currUser = ((MingleApplication) curApp).currUser;
			((MingleApplication) curApp).connectHelper.requestUserList(currUser.getUid(), currUser.getSex(), 
					currUser.getLat(), currUser.getLong(), currUser.getDist(), load_num);

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			// We need notify the adapter that the data have been changed
			adapter.notifyDataSetChanged();

			// Call onLoadMoreComplete when the LoadMore task, has finished
			allchatlistview.onLoadMoreComplete();
			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
			// Notify the loading more operation has finished
			allchatlistview.onLoadMoreComplete();
		}
	}
  
  private class ApplicationWrapper {
		Application curApp;
		public ApplicationWrapper(Application app) {
			curApp = app;
		}
	}
	
  public int convertDpToPixel(float dp) {
      DisplayMetrics metrics = getResources().getDisplayMetrics();
      float px = dp * (metrics.densityDpi / 160f);
      return (int) px;
  }
  
  public void updateUserList(){
  	parent.runOnUiThread(new Runnable() {
  		public void run() {
  			adapter.notifyDataSetChanged();
  		}
  	});
  }
  
  /* 
   * Retrieve data from HttpHelper. Content of data is the list of nearby users of opposite sex.
   * This method also appends the new data to the list and shows them on the screen.
   */
  public void updateList(JSONArray listData){
  	System.out.println("showlist!");
  	System.out.println(listData.toString());
      for(int i = 0 ; i < listData.length(); i++) {
          try {
              JSONObject shownUser = listData.getJSONObject(i);
             
              ChattableUser new_user = new ChattableUser(shownUser.getString("UID"), shownUser.getString("COMM"), Integer.valueOf(shownUser.getString("NUM")));
             
              for(int j = 1; j <= 3; j++) {
            	  if(shownUser.has("PIC" + Integer.toString(j))) {
            		  byte[] picArr = shownUser.getString("PIC" + Integer.toString(j)).getBytes();
            		  BitmapDrawable picmap = new BitmapDrawable(parent.getApplication().getResources(), 
            				  									BitmapFactory.decodeByteArray(picArr , 0,
            				  											picArr.length)); 
            		  new_user.addpic(picmap);
            	  }
              }
              
              ((MingleApplication) parent.getApplication()).currUser.addChattableUser(new_user);
          } catch (JSONException e){
              e.printStackTrace();
          }
      }
    updateUserList();
  }
  
}
