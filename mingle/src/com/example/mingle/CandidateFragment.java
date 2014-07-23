package com.example.mingle;

import java.util.ArrayList;
import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.fortysevendeg.swipelistview.SwipeListView.OnLoadMoreListener;
import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.Intent;
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

public class CandidateFragment extends Fragment {
  public static final String ARG_SECTION_NUMBER = "placeholder_text";
  public final static String USER_UID = "com.example.mingle.USER_SEL";	//Intent data to pass on when new Chatroom Activity started

  public SwipeListView candidatelistview;	//Listview for chattable users
  private ArrayList<ChattableUser>user_list;
  private CandidateAdapter adapter;
  
  private boolean is_first_time = true;
  
  private Activity parent;	//Parent Activity: HuntActivity
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
	  parent = getActivity();

	  
	  View rootView = inflater.inflate(R.layout.candidate_fragment, container, false);
	  
	  candidatelistview=  (SwipeListView)(rootView.findViewById(R.id.All));
      user_list = ((MingleApplication) parent.getApplication()).currUser.getChattableUserList();
      adapter=new CandidateAdapter(parent, R.layout.candidate_row,user_list);
	  
      
      
      final Activity curActivity = parent;
      
      candidatelistview.setSwipeListViewListener(new BaseSwipeListViewListener() {
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
              candidatelistview.openAnimate(position); //when you touch front view it will open
              
              MingleUser currentUser = ((MingleApplication) parent.getApplication()).currUser;
              ChattableUser chat_user_obj = currentUser.getChattableUser(position);
                           
              currentUser.switchChattableToChatting(position);
              
              // Create chatroom in local sqlite
              //((MingleApplication) parent.getApplication()).dbHelper.insertNewUID(chat_user_uid);
             
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
  
              candidatelistview.closeAnimate(position);//when you touch back view it will close
          }
  
          @Override
          public void onDismiss(int[] reverseSortedPositions) {
  
          }
          
      });
      
      //These are the swipe listview settings. you can change these
      //setting as your requirement
      candidatelistview.setSwipeMode(SwipeListView.SWIPE_MODE_LEFT); // there are five swiping modes
      candidatelistview.setSwipeActionLeft(SwipeListView.SWIPE_ACTION_REVEAL); //there are four swipe actions
      candidatelistview.setSwipeActionRight(SwipeListView.SWIPE_ACTION_REVEAL);
      candidatelistview.setOffsetLeft(convertDpToPixel(0f)); // left side offset
      candidatelistview.setOffsetRight(convertDpToPixel(0f)); // right side offset
      candidatelistview.setAnimationTime(50); // animation time
      candidatelistview.setSwipeOpenOnLongPress(true); // enable or disable SwipeOpenOnLongPress
      
      candidatelistview.setAdapter(adapter);
     final ApplicationWrapper wrapper = new ApplicationWrapper(parent.getApplication());
      candidatelistview.setOnLoadMoreListener(new OnLoadMoreListener() {
          public void onLoadMore() {
          	new LoadDataTask(wrapper.curApp, 5).execute();
          }
      });
      
      //Load First Matches
      if(is_first_time) {
    	  is_first_time = false;
    	  loadNewMatches();
      }
      System.out.println("candidateFrag oncreate compleate");
    return rootView;
  }
  
  public void listDataChanged(){
	  parent.runOnUiThread(new Runnable() {
	  		public void run() {
	  			adapter.notifyDataSetChanged();
	  		}
	  });
  }
  
  public void loadNewMatches() {
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
			candidatelistview.onLoadMoreComplete();
			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
			// Notify the loading more operation has finished
			candidatelistview.onLoadMoreComplete();
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
  
  
}
