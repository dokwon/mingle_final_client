package ly.nativeapp.mingle;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class VoteFragment extends Fragment{

	public ListView top_list_view;
	private VoteAdapter top_adapter;
	
	/*private ImageView not_vote_time_view;
	private ImageView female_top_logo_view;
	private ImageView male_top_logo_view;*/
	private RelativeLayout question_wrapper;
	private Activity parent; 
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		parent = getActivity();
		MingleApplication ma = (MingleApplication) parent.getApplication();
		View rootView = inflater.inflate(R.layout.vote_fragment, container, false);
		
		top_list_view = (ListView)(rootView.findViewById(R.id.top_list));
		
		/*not_vote_time_view = (ImageView)(rootView.findViewById(R.id.vote_error_page));
		female_top_logo_view = (ImageView)(rootView.findViewById(R.id.femaleTopLogo));
		male_top_logo_view = (ImageView)(rootView.findViewById(R.id.maleTopLogo));
		*/
		
	    // Stores
	    if(ma.canGetMorePopList()) ma.connectHelper.requestVoteList();

	    top_adapter = new VoteAdapter(parent, R.layout.vote_row, ((MingleApplication) parent.getApplication()).getPopList(), (MingleApplication) parent.getApplicationContext());
	    top_adapter.notifyDataSetChanged();
	       
	    
	    // Set the ArrayAdapter as the ListView's adapter.  
	    top_list_view.setAdapter(top_adapter);  
	    
	    question_wrapper = (RelativeLayout) rootView.findViewById(R.id.question_wrapper);
    	TextView question_text_view = (TextView)rootView.findViewById(R.id.daily_question_vote);
    	question_text_view.setText(((MingleApplication) parent.getApplication()).getQuestionToday());
    	question_text_view.setTypeface(((MingleApplication) parent.getApplication()).koreanTypeFace);
	    
	    
	    /*female_top_logo_view.setVisibility(View.GONE);
    	male_top_logo_view.setVisibility(View.GONE);
    	top_list_view.setVisibility(View.GONE);
    	not_vote_time_view.setVisibility(View.GONE);
	    question_wrapper.setVisibility(View.GONE);*/
		return rootView;
	}
	/*
	public void noPopList(){
		female_top_logo_view.setVisibility(View.GONE);
    	male_top_logo_view.setVisibility(View.GONE);
    	top_list_view.setVisibility(View.GONE);
    	question_wrapper.setVisibility(View.GONE);
    	not_vote_time_view.setVisibility(View.VISIBLE);
	}

	public void showPopList(){
		question_wrapper.setVisibility(View.VISIBLE);
		female_top_logo_view.setVisibility(View.VISIBLE);
    	male_top_logo_view.setVisibility(View.VISIBLE);
    	top_list_view.setVisibility(View.VISIBLE);
    	not_vote_time_view.setVisibility(View.GONE);
	}*/
	
	  public void listDataChanged(){
		  parent.runOnUiThread(new Runnable() {
		  		public void run() {
		  			top_adapter.notifyDataSetChanged();
		  		}
		  });
	  }
}
