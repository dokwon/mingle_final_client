package ly.nativeapp.mingle;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SearchSettingActivity extends Activity implements ActionBar.TabListener{

	private SeekBar distBar;
	private TextView distText;
	private MingleApplication app;
	
	private boolean locChanged = false;
	private boolean filterChanged = false;
	static final int MSG_TIME_OUT = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(android.os.Build.VERSION.SDK_INT < 11) { 
		    requestWindowFeature(Window.FEATURE_NO_TITLE); 
		} 
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		setContentView(R.layout.activity_search_setting);
		
		ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM  | ActionBar.DISPLAY_SHOW_HOME);
        View titleView =  LayoutInflater.from(this).inflate(R.layout.searchactivity_title_custom_view, null);
        LayoutParams layout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        titleView.setLayoutParams(layout);
        actionBar.setCustomView(titleView);
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setBackgroundDrawable(new ColorDrawable(0xFFFFFFFF));
		
		app = (MingleApplication)this.getApplication();

		distanceSetting();
		notificationSetting();
	}
	
	private void distanceSetting(){
		distBar = (SeekBar)findViewById(R.id.distBar);
		distText = (TextView)findViewById(R.id.distText);
		
		distText.setText(String.valueOf(app.getDist()) + " km");
		distBar.setProgress(app.getDist() - 1);
		distBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
				app.setDist(progress + 1);
				distText.setText(String.valueOf(progress + 1) + " km");
			}
		});
	}
    
    public void turnNotiOn(View v){
    	app.setNotiFlag(true);
    	((Button)findViewById(R.id.pushonbutton)).setBackgroundResource(R.drawable.selecton);
		((Button)findViewById(R.id.pushoffbutton)).setBackgroundResource(R.drawable.unselectoff);
    }
    
    public void turnNotiOff(View v){
    	app.setNotiFlag(false);
    	((Button)findViewById(R.id.pushonbutton)).setBackgroundResource(R.drawable.unselecton);
		((Button)findViewById(R.id.pushoffbutton)).setBackgroundResource(R.drawable.selectoff);
    }
    
	private void notificationSetting(){		
		if(app.getNotiFlag()){
			((Button)findViewById(R.id.pushonbutton)).setBackgroundResource(R.drawable.selecton);
			((Button)findViewById(R.id.pushoffbutton)).setBackgroundResource(R.drawable.unselectoff);
		} else {
			((Button)findViewById(R.id.pushonbutton)).setBackgroundResource(R.drawable.unselecton);
			((Button)findViewById(R.id.pushoffbutton)).setBackgroundResource(R.drawable.selectoff);
		}
	}
	
	@Override
	protected void onPause(){
		if(locChanged)
			app.connectHelper.userUpdateRequest(app, app.getMyUser().getName(), app.getMyUser().getSex(), app.getMyUser().getNum());
		
		if(filterChanged) {
			for(int i = 0; i < app.getCandidateList().size(); i++) {
				MingleUser user = app.getMingleUser(app.getCandidateList().get(i));
				if(!app.getGroupNumFilter()[user.getNum()-2]) {
					app.getCandidateList().remove(i);
					i--;
				}
			}
		}
		super.onPause();
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            onBackPressed();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

 
    
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}
}
