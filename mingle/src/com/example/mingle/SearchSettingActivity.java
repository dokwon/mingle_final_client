package com.example.mingle;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SearchSettingActivity extends Activity implements ActionBar.TabListener{

	private SeekBar distBar;
	private TextView distText;
	private Button locButton;
	private RadioGroup notiRadio;
	private ArrayList<CheckBox> numFilter = new ArrayList<CheckBox>();
	private MingleApplication app;
	
	private boolean locChanged = false;
	private boolean filterChanged = false;
	private Context context;
	private LocationManager locationManager;
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
		context = this;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		distanceSetting();
		//locationSetting();
		notificationSetting();
		//numberFilterSetting();
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
	
	/*private void locationSetting(){
		locButton = (Button)findViewById(R.id.refreshLoc);
		locButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				if(!app.isLocationEnabled()) {
                	AlertDialog.Builder popupBuilder = new AlertDialog.Builder(context)
															.setTitle(getResources().getString(R.string.gps_location_setting))
															.setCancelable(false)
															.setMessage(getResources().getString(R.string.gps_disabled_alert))
															.setIcon(R.drawable.mingle_logo)
															.setPositiveButton(getResources().getString(R.string.allow), new DialogInterface.OnClickListener() {
																@Override
																public void onClick(DialogInterface dialog, int id) {
																	dialog.dismiss();
																	Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
																	context.startActivity(intent);
																}
															})
															.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
																@Override
																public void onClick(DialogInterface dialog, int id) {
																	dialog.dismiss();
																}
															});
                	AlertDialog popupDialog = popupBuilder.create();
                	popupDialog.show();
                } else {
                	getCurrentLocation();
                }
			}
		});
	}
	
	// Get the users one-time location. Code available below to register for updates
    private void getCurrentLocation() {
    	Criteria criteria = new Criteria();
    	String provider = locationManager.getBestProvider(criteria, true);
        locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
        locHandler.sendEmptyMessageDelayed(MSG_TIME_OUT, 3*1000);
    }
    
	private LocationListener locationListener = new LocationListener() {
	    public void onLocationChanged(Location location) {
	        app.setLat((float)location.getLatitude());
	        app.setLong((float)location.getLongitude());
	        locChanged = true;
	    }		
	    public void onStatusChanged(String provider, int status, Bundle extras) {}
	    public void onProviderEnabled(String provider) {}
	    public void onProviderDisabled(String provider) {}
	};
	
	private Handler locHandler = new Handler() {
    	@Override
    	public void handleMessage(android.os.Message msg) {
    		switch(msg.what) {		
    			//After 3seconds, check whether the device finds current location.
    			case MSG_TIME_OUT:
    				locationManager.removeUpdates(locationListener);
    				
    				//If not notify the user the device cannot find current location
    				if(!locChanged) {
    					AlertDialog.Builder popupBuilder = new AlertDialog.Builder(context)
																.setTitle(getResources().getString(R.string.gps_location_setting))
																.setCancelable(false)
																.setMessage(getResources().getString(R.string.gps_cannot_find_location))
																.setIcon(R.drawable.mingle_logo)
																.setNeutralButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
																	@Override
																	public void onClick(DialogInterface dialog, int id) {
																		dialog.dismiss();
																	}
																});
						
    					popupBuilder.show();
    				}
    				break;
    				
    			default:
    				break;
    		}
    	}
    };*/
	
    
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
	
	/*private void numberFilterSetting() {
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
	}*/
	
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
