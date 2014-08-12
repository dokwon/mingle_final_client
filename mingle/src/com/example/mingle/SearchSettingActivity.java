package com.example.mingle;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
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
	private ArrayList<CheckBox> numFilter = new ArrayList<CheckBox>();
	private MingleApplication app;
	
	private boolean locChanged = false;
	private boolean filterChanged = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_setting);
		
		app = (MingleApplication)this.getApplication();
		distanceSetting();
		locationSetting();
		notificationSetting();
		numberFilterSetting();
	}
	
	private void distanceSetting(){
		distBar = (SeekBar)findViewById(R.id.distBar);
		distText = (TextView)findViewById(R.id.distText);
		
		distText.setText(String.valueOf(app.getDist()));
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
				distText.setText(String.valueOf(progress + 1));
			}
		});
	}
	
	private void locationSetting(){
		locButton = (Button)findViewById(R.id.refreshLoc);
		locButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				locChanged = true;
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
	
	private void numberFilterSetting() {
		CheckBox box_1 = (CheckBox)findViewById(R.id.numFilter1);
		CheckBox box_2 = (CheckBox)findViewById(R.id.numFilter2);
		CheckBox box_3 = (CheckBox)findViewById(R.id.numFilter3);
		CheckBox box_4 = (CheckBox)findViewById(R.id.numFilter4);
		CheckBox box_5 = (CheckBox)findViewById(R.id.numFilter5);
		
		numFilter.add(box_1);
		numFilter.add(box_2);
		numFilter.add(box_3);
		numFilter.add(box_4);
		numFilter.add(box_5);
		
		for(int i = 0; i < 5; i++) {
			CheckBox box = numFilter.get(i);
			if(app.getGroupNumFilter()[i])
				box.setChecked(true);
			else box.setChecked(false);
			
			final int index = i;
			box.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					filterChanged = true;
					if(((CheckBox)v).isChecked()) app.getGroupNumFilter()[index] = true;
					else app.getGroupNumFilter()[index] = false;
				}
			});
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
}
