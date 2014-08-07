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

  public SwipeListView candidatelistview;	//Listview for chattable users
  private ArrayList<String>candidate_list;
  private CandidateAdapter adapter;
  
  private boolean is_first_time = true;
  
  private Activity parent;	//Parent Activity: HuntActivity
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
	  System.out.println("cand frag create");
	  parent = getActivity();

	  
	  View rootView = inflater.inflate(R.layout.candidate_fragment, container, false);
	  
	  candidatelistview=  (SwipeListView)(rootView.findViewById(R.id.All));
	  candidatelistview.setDivider(null);
      candidate_list = ((MingleApplication) parent.getApplication()).getCandidateList();
      adapter=new CandidateAdapter(parent, R.layout.candidate_row,candidate_list, (MingleApplication)parent.getApplicationContext());
	  
      
      
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
              
              MingleApplication currentUser = ((MingleApplication) parent.getApplication());
                                         
              // Create chatroom in local sqlite
              //((MingleApplication) parent.getApplication()).dbHelper.insertNewUID(chat_user_uid);
           	  
              String user_uid = currentUser.getCandidate(position);
              MingleUser targetMU = currentUser.getMingleUser(user_uid);
              currentUser.dbHelper.insertNewUID(user_uid, targetMU.getNum(), targetMU.getName(), 0,0,0);
              
              currentUser.switchCandidateToChoice(position);
              ((HuntActivity)parent).candidateListUpdate();
              ((HuntActivity)parent).choiceListUpdate();

              Intent chat_intent = new Intent(curActivity, ChatroomActivity.class);
       		  chat_intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
              chat_intent.putExtra(ChatroomActivity.USER_UID, currentUser.getMingleUser(user_uid).getUid());
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
      candidatelistview.setOnLoadMoreListener(new OnLoadMoreListener() {
          public void onLoadMore() {
        	  loadNewMatches(((MingleApplication)parent.getApplication()).getExtraMatchNum());
          }
      });
      
      //Load more matches if first time or has only a few
      int match_num = candidate_list.size();
      System.out.println("is first time:" + is_first_time + " match_num_tot:"+match_num);
      if(is_first_time || match_num <  ((MingleApplication)parent.getApplication()).getFirstMatchNum()) {
    	  is_first_time = false;
    	  loadNewMatches( ((MingleApplication)parent.getApplication()).getFirstMatchNum());
      }
      
    return rootView;
  }
  
  public void listDataChanged(){
	  parent.runOnUiThread(new Runnable() {
	  		public void run() {
	  			adapter.notifyDataSetChanged();
	  		}
	  });
  }
  public void candidateLoadMoreComplete(){
	  candidatelistview.onLoadMoreComplete();
  }
  public void loadNewMatches(int num_of_matches) {
	  MingleApplication app = (MingleApplication) parent.getApplication();
	  if(app.canGetMoreCandidate()) {
		  ArrayList<String> combined_list = new ArrayList<String>();
		  combined_list.addAll(app.getCandidateList());
		  combined_list.addAll(app.getChoiceList());
		  app.connectHelper.requestUserList(app.getMyUser().getUid(), app.getMyUser().getSex(), 
					app.getLat(), app.getLong(), app.getDist(), num_of_matches, combined_list);
	  }
	  else candidateLoadMoreComplete();
  }
  
  public int convertDpToPixel(float dp) {
      DisplayMetrics metrics = getResources().getDisplayMetrics();
      float px = dp * (metrics.densityDpi / 160f);
      return (int) px;
  }

}
