package com.example.mingle;

import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.content.LocalBroadcastManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.example.mingle.HttpHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.widget.*;
import android.widget.TextView.OnEditorActionListener;

import com.example.mingle.MingleApplication;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {
    //public static int REQUEST_CODE = 1;
    //private Bitmap taken_photo_bitmap;		//Bitmap to save photo just taken
    //private ArrayList<Bitmap> photo_list;	//List of user's photos
	private String name;
	private String sex;
	private int num; 
    
    //For GCM below
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private String SENDER_ID = "5292889580";
	
	private GoogleCloudMessaging gcm;
	private AtomicInteger msgId = new AtomicInteger();
	private SharedPreferences prefs;
	private Context context;
	private String regid;
	 
	private ProgressDialog proDialog;
	
	MingleApplication app;
	
	static final String TAG = "GCMDemo";

	//Until here
    
    //Server Address
    private static final String server_url = "http://ec2-54-178-214-176.ap-northeast-1.compute.amazonaws.com:8080";
	public final int SELECT_FILE = 0;
    
	private Bitmap rotatedBitmap(Bitmap source, String photoPath) {
		Matrix matrix = new Matrix();
		ExifInterface ei = null;
		try {
			ei = new ExifInterface(photoPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
		System.out.println(Integer.toString(orientation));
		switch(orientation) {
		    case ExifInterface.ORIENTATION_ROTATE_90:
		    	matrix.postRotate(90);
		        break;
		    case ExifInterface.ORIENTATION_ROTATE_180:
		    	matrix.postRotate(180);
		        break;
		    case ExifInterface.ORIENTATION_ROTATE_270:
		    	matrix.postRotate(270);
		    	break;
		    // etc.
		}
		matrix.postScale(0.5f, 0.5f);
		return Bitmap.createBitmap(source , 0, 0, source .getWidth(), source .getHeight(), matrix, true);
	}	
	
	private ImageView FindAppropriateImageView() {
		
		ImageView[] views = {(ImageView) findViewById(R.id.photoView1),
		                   (ImageView) findViewById(R.id.photoView2),
		                   (ImageView) findViewById(R.id.photoView3)};
		for (int i = 0; i < views.length; i ++) {
			ImageView photoView = views[i];
			
			if (photoView.getDrawable() == null) {
				
				switch(i) {
					case 0:
						((ImageView) findViewById(R.id.add1))
						.setBackgroundResource(R.drawable.photo_delete);
						break;
					case 1:
						((ImageView) findViewById(R.id.add2))
						.setBackgroundResource(R.drawable.photo_delete);
						break;
						
					default:
						((ImageView) findViewById(R.id.add3))
						.setBackgroundResource(R.drawable.photo_delete);
						
				}
				System.out.println("1");
				return photoView;
			}
		}
		System.out.println("2");
		return null; 
	}
	
	
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //InputStream stream = null;
    	ImageView imageView = FindAppropriateImageView();
    	String photoPath = "";
    	if (resultCode == RESULT_OK) {
    		if (requestCode == REQUEST_IMAGE_CAPTURE) {   // If the user requested taking a photo
    			System.out.println(imageUri);
                getContentResolver().notifyChange(imageUri, null);
                photoPath = imageUri.getPath();
            }  else if (requestCode == SELECT_FILE) { // If the user wants to select a file
                imageUri = data.getData();
                photoPath = getPath(imageUri, MainActivity.this);
            }
    		 ((MingleApplication) this.getApplication()).addPhotoPath(photoPath);
             Bitmap bm;
             BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
             btmapOptions.inSampleSize = 16;
             bm = rotatedBitmap(BitmapFactory.decodeFile(photoPath, btmapOptions), photoPath);
             imageView.setImageBitmap(bm);
    	}
    }
    
    private BroadcastReceiver userRequestReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    	   // Extract data included in the Intent
    	   String data = intent.getStringExtra(HttpHelper.USER_CONF);
    	   Log.d("receiver", "Got message: " + data);
    	   
    	   try {
    		   JSONObject user_conf_obj = new JSONObject(data);
    		   joinMingle(user_conf_obj);
    	   } catch (JSONException e) {
    		   // TODO Auto-generated catch block
    		   e.printStackTrace();
    	   }
    	   
    	}
    };
    
    // Helper method to retrieve the filepath of selected image
    public String getPath(Uri uri, Activity activity) {
        String[] projection = { MediaColumns.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
        cursor.moveToFirst(); 
        return cursor.getString(column_index);
    }
 

    // Get the users one-time location. Code available below to register for updates
    private void getCurrentLocation() {
    	
    	// Acquire a reference to the system Location Manager
    	LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    	Criteria criteria = new Criteria();
    	String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider);
        float lat = 0;
        float lon = 0;
        if(location != null){
        	lat =(float) location.getLatitude();
        	lon =(float) location.getLongitude();
        } 
        ((MingleApplication) this.getApplication()).setLat(lat);
    	((MingleApplication) this.getApplication()).setLong(lon);
     
    	
    	// In case we want to register for location updates
    	/*
    	// Define a listener that responds to location updates
    	LocationListener locationListener = new LocationListener() {
    	    public void onLocationChanged(Location location) {
    	      // Called when a new location is found by the network location provider.
    	      //makeUseOfNewLocation(location);
    	    }

    	    public void onStatusChanged(String provider, int status, Bundle extras) {}

    	    public void onProviderEnabled(String provider) {}

    	    public void onProviderDisabled(String provider) {}
    	  };

    	// Register the listener with the Location Manager to receive location updates
    	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);*/
      
    }
    
    public void SexOptionChanged(View v) {
    	
    	if (sex == "M" && v.equals(findViewById(R.id.womanbutton))) {
    		// Change selection to woman
    		sex = "W";
    		Button manButton = (Button) findViewById(R.id.manbutton);
    		manButton.setBackgroundResource(R.drawable.genderchoice_notman);
    		
    		v.setBackgroundResource(R.drawable.genderchoice_woman);
    	} else if (sex == "W" && v.equals(findViewById(R.id.manbutton))) {
    		// Change selection to man
    		sex= "M";
    		Button manButton = (Button) findViewById(R.id.womanbutton);
    		manButton.setBackgroundResource(R.drawable.genderchoice_notwoman);
    		
    		v.setBackgroundResource(R.drawable.genderchoice_man);
    	}
    }
    
    public void MemberNumberSelected(View v) {
    	ArrayList<View> memberViewArr = new ArrayList<View>();
    	memberViewArr.add(findViewById(R.id.member_1));
    	memberViewArr.add(findViewById(R.id.member_2));
    	memberViewArr.add(findViewById(R.id.member_3));
    	memberViewArr.add(findViewById(R.id.member_4));
    	memberViewArr.add(findViewById(R.id.member_5));
    	memberViewArr.add(findViewById(R.id.member_6));
    	boolean numFound = false;
    	for(int i = 0; i < 6; i ++) {
    		if(numFound) {
    			memberViewArr.get(i).setBackgroundResource(R.drawable.peoplenumberpicoff);
    		} else {
    			memberViewArr.get(i).setBackgroundResource(R.drawable.peoplenumberpicon);
    		}
    		if(v.equals(memberViewArr.get(i))) {
    			num = i;
    			numFound = true;
    		}
    	}
    	
    }
    
    
    private void initializeUIViews() {
        sex = "M";
        num = -1;

        /*
        EditText editText = (EditText) findViewById(R.id.nicknameTextView);
        editText.setOnEditorActionListener(new OnEditorActionListener() {
           
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				 boolean handled = false;
	                if (actionId == EditorInfo.IME_ACTION_DONE) {
	                	String name_str = (String) v.getText();
	                	
	                	
	                	if(name_str.length() > 1) {
	                		name = name_str;
	                		handled = true;
	                	}
	                }
	                return handled;
			}
        });*/
        
        ImageView photoView1 = (ImageView) findViewById(R.id.photoView1);
        ImageView photoView2 = (ImageView) findViewById(R.id.photoView2);
        ImageView photoView3 = (ImageView) findViewById(R.id.photoView3);
        ArrayList<ImageView> views = new ArrayList<ImageView>();
        views.add(photoView1);
        views.add(photoView2);
        views.add(photoView3);

        for(ImageView view : views ) {
        	view.setOnClickListener(new MinglePhotoClickListener( this, views));
        }
       
    }
    
    
    
    
    private boolean AppOnFirstTime() {
    	DatabaseHelper db = ((MingleApplication) this.getApplication()).dbHelper;
    	MingleApplication mingleApp = (MingleApplication) this.getApplication();
    	if(db.isFirst()) {

    		System.out.println("Saving for the first time!!");
    		return true;
    	}
    	
    	mingleApp.createMyUser(mingleApp.dbHelper.getUserData());
    	ArrayList<ContentValues> chatters = mingleApp.dbHelper.getUserList();
    	System.out.println(chatters.size() + " is the size of chatters");
    	for(int i=0; i<chatters.size(); i++){
    		System.out.println(chatters.get(i));
    		ArrayList<Message> tempmsgs = mingleApp.dbHelper.getMsgList(chatters.get(i).getAsString("UID"));
    		String sex_var = "M";
    		if(((MingleApplication) this.getApplicationContext()).getMyUser().getSex() == "M") sex_var = "F";
    		
    		MingleUser newUser = new MingleUser(chatters.get(i).getAsString("UID"),
    				chatters.get(i).getAsString("COMM"),
    				(int) chatters.get(i).getAsInteger("NUM"),
    				1,
    				mingleApp.getResources().getDrawable(R.drawable.ic_launcher),
    				sex_var);
    		if(mingleApp.getChoicePos(newUser.getUid())==-1) {
    			mingleApp.addMingleUser(newUser);
    		
    			mingleApp.addChoice(newUser.getUid());
    			new ImageDownloader(this.getApplicationContext(), newUser.getUid(), 0);
	    		for(int j =0; j<tempmsgs.size(); j++){
	    			newUser.addMsgObj(tempmsgs.get(j));
	    		}
	    	}
    	}
    	
    	// Populate other fields with UID
    	return false;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        app = ((MingleApplication) this.getApplication());
        
        //check if custom title is supported BEFORE setting the content view!
        boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        
        setContentView(R.layout.activity_main);
        
        if(customTitleSupported) getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title_bar);
        
        //MingleApplication mingleApp = (MingleApplication) this.getApplication();
        
        //Initialize HttpHelper that supports HTTP GET/POST requests and socket connection
        app.connectHelper = new HttpHelper(server_url, (MingleApplication)this.getApplication());

        // Initialize the database helper that manages local storage
        app.dbHelper = new DatabaseHelper(this);
     
        app.socketHelper = new Socket(server_url, app);
        // Get the user's current location
        getCurrentLocation();
                
        //GCM Setup here
        context = (Context)this;
        
        LocalBroadcastManager.getInstance(context).registerReceiver(userRequestReceiver,
        		  new IntentFilter(HttpHelper.JOIN_MINGLE));
        
        // Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerForNewUser();
            }
            
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
        
		app.setRid(regid);

        // If the app is not on for the first time, start HuntActivity
        // and populate it with data from local storage

        if(AppOnFirstTime()) {
        	initializeUIViews();
        } else {
        	//Start activity for Mingle Market
        	Intent i = new Intent(this, HuntActivity.class);
            startActivity(i);
            finish();
        }        
    }

    
    
    
    //On user creation request, get user's info and send request to server
    public void userCreateButtonPressed(View view) {

    	name = ((EditText)findViewById(R.id.nicknameTextView)).getText().toString();
        //Check validity of user input and send user creation request to server
        if (app.isValid(num, name)) {
        	proDialog = new ProgressDialog(this);
            proDialog.setIndeterminate(true);
            proDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            proDialog.getWindow().getAttributes().dimAmount = (float)0.8;
            proDialog.show(); 
            
        	app.connectHelper.userCreateRequest(app, name, sex, num);
      } else {
    	   showInvalidUserAlert();
           System.out.println("The user is not valid.");
       }
    }


    public void joinMingle(JSONObject userData) {
  
        
            System.out.println(userData.toString());
                
            app.dbHelper.setMyInfo(userData);
            ((MingleApplication) this.getApplication()).createMyUser(userData);
            
        	proDialog.dismiss();
            
            //Start activity for Mingle Market
            Intent i = new Intent(this, HuntActivity.class);
            startActivity(i);
            finish();
    }

    
    
   
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri imageUri;
    
    
    public void takePicture() {
    	 Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
    	  File photo = new File(Environment.getExternalStorageDirectory(), 
    			  (new Timestamp(new Date().getTime())).toString() + "Pic.jpg");
    	  System.out.println((new Timestamp(new Date().getTime())).toString());
    	  intent.putExtra(MediaStore.EXTRA_OUTPUT,
    	            Uri.fromFile(photo));
    	 imageUri = Uri.fromFile(photo);
    	 System.out.println(imageUri.toString());
    	 startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings )
            return true;

        return super.onOptionsItemSelected(item);
    }

    private void showInvalidUserAlert() {
    	new AlertDialog.Builder(this)
        .setTitle("Invalid User")
        .setMessage("You need at least one photo and specify how many you are before mingling.")
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) { 
                // continue with delete
            }
         })
        .setIcon(android.R.drawable.ic_dialog_alert)
         .show();	
    }
    
	  //For GCM here
	  private boolean checkPlayServices() {
	      int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	      if (resultCode != ConnectionResult.SUCCESS) {
	          if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	              GooglePlayServicesUtil.getErrorDialog(resultCode, this,
	                      PLAY_SERVICES_RESOLUTION_REQUEST).show();
	          } else {
	              Log.i(TAG, "This device is not supported.");
	              finish();
	          }
	          return false;
	      }
	      return true;
	  }

	  
	  private String getRegistrationId(Context context) {
	      final SharedPreferences prefs = getGCMPreferences(context);
	      String registrationId = prefs.getString(PROPERTY_REG_ID, "");
	      if (registrationId.isEmpty()) {
	          Log.i(TAG, "Registration not found.");
	          return "";
	      }
	      // Check if app was updated; if so, it must clear the registration ID
	      // since the existing regID is not guaranteed to work with the new
	      // app version.
	      int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	      int currentVersion = getAppVersion(context);
	      if (registeredVersion != currentVersion) {
	          Log.i(TAG, "App version changed.");
	          return "";
	      }
	      return registrationId;
	  }
	  
	 
	  private SharedPreferences getGCMPreferences(Context context) {
	      // This sample app persists the registration ID in shared preferences, but
	      // how you store the regID in your app is up to you.
	      return getSharedPreferences(HuntActivity.class.getSimpleName(), Context.MODE_PRIVATE);
	  }

	  
	  private static int getAppVersion(Context context) {
	      try {
	          PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
	          return packageInfo.versionCode;
	      } catch (NameNotFoundException e) {
	          // should never happen
	          throw new RuntimeException("Could not get package name: " + e);
	      }
	  }
	  
	  private void registerForNewUser() {
		  new AsyncTask<MingleApplication, Void, String>() {	  
			  @Override
			  protected String doInBackground(MingleApplication... params) {
				  try {
					  if (gcm == null) {
						  gcm = GoogleCloudMessaging.getInstance(context);
					  }
			  
					  regid = gcm.register(SENDER_ID);
					  // Persist the regID - no need to register again.
					  storeRegistrationId(context, regid);
					  params[0].setRid(regid);
				  } catch (IOException ex) {
					  ex.printStackTrace();
					  // If there is an error, don't just keep trying to register.
					  // Require the user to click a button again, or perform
					  // exponential back-off.
					  return "";
				  }
				  return "Registration done";
			  }
			  
			  @Override
			  protected void onPostExecute(String msg) {
				  System.out.println(msg);
			  }
		  }.execute(((MingleApplication)this.getApplication()));
	  }

	  private void storeRegistrationId(Context context, String regId) {
	      final SharedPreferences prefs = getGCMPreferences(context);
	      int appVersion = getAppVersion(context);
	      Log.i(TAG, "Saving regId on app version " + appVersion);
	      SharedPreferences.Editor editor = prefs.edit();
	      editor.putString(PROPERTY_REG_ID, regId);
	      editor.putInt(PROPERTY_APP_VERSION, appVersion);
	      editor.commit();
	  }
	  
	  @Override
	  public void onDestroy(){
		  LocalBroadcastManager.getInstance(this).unregisterReceiver(userRequestReceiver);
		  super.onDestroy();
	  }
}
