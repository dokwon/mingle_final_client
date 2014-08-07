package com.example.mingle;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SearchSettingActivity extends Activity {

	private SeekBar distBar;
	private TextView distText;
	private Button locButton;
	private RadioGroup notiRadio;
	private MingleApplication app;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_setting);
		
		app = (MingleApplication)this.getApplication();
		distanceSetting();
		locationSetting();
		notificationSetting();
	}
	
	private void distanceSetting(){
		distBar = (SeekBar)findViewById(R.id.distBar);
		distText = (TextView)findViewById(R.id.distText);
		
		distText.setText(String.valueOf(app.getDist()));
		distBar.setProgress(app.getDist());
		distBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
				app.setDist(progress);
				distText.setText(String.valueOf(progress));
			}
		});
	}
	
	private void locationSetting(){
		locButton = (Button)findViewById(R.id.refreshLoc);
		locButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				app.getCurrentLocation();
			}
		});
	}
	
	private void notificationSetting(){
		notiRadio = (RadioGroup)findViewById(R.id.notification_setup);
		
		if(app.getNotiFlag())
			notiRadio.check(R.id.notification_on);
		else
			notiRadio.check(R.id.notification_off);
		
		notiRadio.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId == R.id.notification_on)	
					app.setNotiFlag(true);
				else
					app.setNotiFlag(false);
			}
		});
	}
}
