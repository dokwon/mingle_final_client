package com.example.mingle;

import java.util.ArrayList;

import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.fortysevendeg.swipelistview.SwipeListView.OnLoadMoreListener;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * A dummy fragment representing a section of the app
 */

public class CandidateFragment extends Fragment {
  public static final String ARG_SECTION_NUMBER = "placeholder_text";

  public SwipeListView candidatelistview;	//Listview for candidate mingle users
  private CandidateAdapter adapter;			//Listview Adapter for candidatelistview 
  private Activity parent;					//Parent Activity: HuntActivity
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
	  parent = getActivity();
	  View rootView = inflater.inflate(R.layout.candidate_fragment, container, false);
	  
	  //Initialize list view and adapter
	  candidatelistview=  (SwipeListView)(rootView.findViewById(R.id.All));
	  candidatelistview.setDivider(null);
      ArrayList<String> candidate_list = ((MingleApplication) parent.getApplication()).getCandidateList();
      adapter=new CandidateAdapter(parent, R.layout.candidate_row,candidate_list, (MingleApplication)parent.getApplicationContext());
	    
      //Initialize candidatelistview as swipelistview
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
              String user_uid = currentUser.getCandidate(position);
              
              //Save selected user as candidate in local DB
              MingleUser targetMU = currentUser.getMingleUser(user_uid);
              currentUser.dbHelper.insertNewUID(user_uid, targetMU.getNum(), targetMU.getName(), 0);
              
              //Switch selected user from candidate to choice
              currentUser.switchCandidateToChoice(position);
              ((HuntActivity)parent).candidateListUpdate();
              ((HuntActivity)parent).choiceListUpdate();

              //Open Chat room
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
      
      candidatelistview.setSwipeMode(SwipeListView.SWIPE_MODE_LEFT); // there are five swiping modes
      candidatelistview.setSwipeActionLeft(SwipeListView.SWIPE_ACTION_REVEAL); //there are four swipe actions
      candidatelistview.setSwipeActionRight(SwipeListView.SWIPE_ACTION_REVEAL);
      candidatelistview.setOffsetLeft(convertDpToPixel(0f)); // left side offset
      candidatelistview.setOffsetRight(convertDpToPixel(0f)); // right side offset
      candidatelistview.setAnimationTime(50); // animation time
      candidatelistview.setSwipeOpenOnLongPress(true); // enable or disable SwipeOpenOnLongPress
      
      //When scroll all the way to the bottom, load more options
      candidatelistview.setAdapter(adapter);
      candidatelistview.setOnLoadMoreListener(new OnLoadMoreListener() {
          public void onLoadMore() {
        	  if(((MingleApplication)parent.getApplication()).canGetMoreCandidate())
            	  loadNewMatches(((MingleApplication)parent.getApplication()).getExtraMatchNum());
        	  else {
        		  Toast.makeText(parent.getApplicationContext(), getResources().getString(R.string.no_more_candidates), Toast.LENGTH_SHORT).show();
        		  candidateLoadMoreComplete();
        	  }
          }
      });
      
      //Load more matches if has only a few candidates in current list
      int match_num = candidate_list.size();
      if(match_num <  ((MingleApplication)parent.getApplication()).getFirstMatchNum()) {
    	  if(((MingleApplication)parent.getApplication()).canGetMoreCandidate())
    		  loadNewMatches( ((MingleApplication)parent.getApplication()).getFirstMatchNum());
      }
    return rootView;
  }
  
  //Update candidate list
  public void listDataChanged(){
	  parent.runOnUiThread(new Runnable() {
	  		public void run() {
	  			adapter.notifyDataSetChanged();
	  		}
	  });
  }
  
  //Complete when more candidates are loaded
  public void candidateLoadMoreComplete(){
	  candidatelistview.onLoadMoreComplete();
  }
  
  //Load num_of_matches more candidates to the list if there are more candidates available at server
  public void loadNewMatches(int num_of_matches) {
	  MingleApplication app = (MingleApplication) parent.getApplication();
	  ArrayList<String> combined_list = new ArrayList<String>();
	  combined_list.addAll(app.getCandidateList());
	  combined_list.addAll(app.getChoiceList());
	  app.connectHelper.requestUserList(app.getMyUser().getUid(), app.getMyUser().getSex(), app.getMyUser().getNum(),
					app.getLat(), app.getLong(), app.getDist(), num_of_matches, combined_list);
  }
  
  public int convertDpToPixel(float dp) {
      DisplayMetrics metrics = getResources().getDisplayMetrics();
      float px = dp * (metrics.densityDpi / 160f);
      return (int) px;
  }

}
