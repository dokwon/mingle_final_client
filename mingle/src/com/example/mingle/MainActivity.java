package com.example.mingle;

import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.Window;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.example.mingle.HttpHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.widget.*;
import android.widget.ImageView.ScaleType;

import com.example.mingle.MingleApplication;
import com.google.android.gms.gcm.GoogleCloudMessaging;

//import com.google.android.gms.maps.model.Location;





import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {
    //public static int REQUEST_CODE = 1;
    //private Bitmap taken_photo_bitmap;		//Bitmap to save photo just taken
    //private ArrayList<Bitmap> photo_list;	//List of user's photos
    private String sex_option;				//Sex identity of user
    private String name_option;			//Comment written by user
    private int num_option;					//Number of people with user
    
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
		ImageView imageView = (ImageView) findViewById(R.id.photoView1);
        if (imageView.getDrawable() != null) {
        	imageView = (ImageView) findViewById(R.id.photoView2);
        } 
        if (imageView.getDrawable() != null) {
        	imageView = (ImageView) findViewById(R.id.photoView3);
        }
        return imageView;
	}
	
	
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //InputStream stream = null;
    	if (resultCode == RESULT_OK) {
    		ImageView imageView = FindAppropriateImageView();
    		if (requestCode == REQUEST_IMAGE_CAPTURE) {   // If the user requested taking a photo
            	
            	Uri selectedImage = imageUri;
                getContentResolver().notifyChange(selectedImage, null);
                
                ContentResolver cr = getContentResolver();
                
                try {
                     Bitmap taken_photo_bitmap = android.provider.MediaStore.Images.Media
                     .getBitmap(cr, selectedImage);
                     
                     imageView.setImageBitmap(rotatedBitmap(taken_photo_bitmap, selectedImage.getPath()));
                    ((MingleApplication) this.getApplication()).addPhotoPath(selectedImage.getPath());
                    
                    Toast.makeText(this, selectedImage.toString(),
                            Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
                            .show();
                    System.out.println("Camera " + e.toString());
                    e.printStackTrace();
                }
            }  else if (requestCode == SELECT_FILE) { // If the user wants to select a file
                Uri selectedImageUri = data.getData();
 
                String tempPath = getPath(selectedImageUri, MainActivity.this);
                ((MingleApplication) this.getApplication()).addPhotoPath(tempPath);
                Bitmap bm;
                BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
                bm = BitmapFactory.decodeFile(tempPath, btmapOptions);
                imageView.setImageBitmap(bm);
            }
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
    
    
    private void initializeUIViews() {
    	// Sets up the radiobutton
        sex_option = "M";
        RadioGroup radioSexGroup = (RadioGroup) findViewById(R.id.radioSex);
        // get selected radio button from radioGroup
        radioSexGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            public void onCheckedChanged(RadioGroup group,
                    int checkedId) {
            	if(checkedId == R.id.radioFemale) sex_option="F";
            	else sex_option="M";
            }
        });
        
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
    	String UID = mingleApp.dbHelper.getMyUID();
    	
    	mingleApp.getMyUser().setUid(UID);
    	ArrayList<String> chatters = mingleApp.dbHelper.getUIDList();
    	
    	
    	Cursor msgs = mingleApp.dbHelper.getMsgList(UID);
    	// Populate other fields with UID
    	return false;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //check if custom title is supported BEFORE setting the content view!
        boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        
        setContentView(R.layout.activity_main);
        
       if(customTitleSupported) getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title_bar);
        
        MingleApplication mingleApp = (MingleApplication) this.getApplication();
        
        //Initialize HttpHelper that supports HTTP GET/POST requests and socket connection
        mingleApp.connectHelper = new HttpHelper(server_url, (MingleApplication)this.getApplication());

        // Initialize the database helper that manages local storage
        mingleApp.dbHelper = new DatabaseHelper(this);
     
        mingleApp.socketHelper = new Socket(server_url, (MingleApplication)this.getApplication());
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
        
		((MingleApplication)this.getApplication()).setRid(regid);

        // If the app is not on for the first time, start HuntActivity
        // and populate it with data from local storage

        if(AppOnFirstTime()) {
        	System.out.println("++++++++++++++++++1");
        	initializeUIViews();
        } else {
        	//Start activity for Mingle Market
        	System.out.println("++++++++++++++++++2");
           Intent i = new Intent(this, HuntActivity.class);
            startActivity(i);
        }        
    }

    
    
    
    //On user creation request, get user's info and send request to server
    public void userCreateButtonPressed(View view) {
        //hard coded. need to be replaced later on
    	name_option = "hi";
        num_option = 4;        

    	MingleApplication app = ((MingleApplication) this.getApplication());

        //Check validity of user input and send user creation request to server
        if (app.isValid()) {
        	
        	app.connectHelper.userCreateRequest(app.getPhotoPaths(), 
        												name_option, 
        												sex_option, 
        												num_option,
        												app.getLong(), app.getLat(), app.getRid());
      } else {
    	   showInvalidUserAlert();
           System.out.println("The user is not valid.");
       }
    }


    public void joinMingle(JSONObject userData) {
    		
    		  
        	try {
                System.out.println(userData.toString());
                ((MingleApplication) this.getApplication()).dbHelper.setMyUID(userData.getString("UID"));
                ((MingleApplication) this.getApplication()).createMyUser(userData.getString("UID"), userData.getString("SEX"), 
                																	userData.getInt("NUM"), userData.getString("COMM"),
                                                                                            (float)userData.getDouble("LOC_LAT"), 
                                                                                            (float)userData.getDouble("LOC_LONG"), 
                                                                                            userData.getInt("DIST_LIM"));
            } catch(JSONException e){
                e.printStackTrace();
            }
            
            //Start activity for Mingle Market
            Intent i = new Intent(this, HuntActivity.class);
            startActivity(i);
    }

    
    
   
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri imageUri;
    
    
    public void takePicture() {
    	 Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
    	  File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
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
		  new AsyncTask<Void, Void, String>() {	  
			  @Override
			  protected String doInBackground(Void... params) {
				  String msg;
				  try {
					  if (gcm == null) {
						  gcm = GoogleCloudMessaging.getInstance(context);
					  }
			  
					  regid = gcm.register(SENDER_ID);
					  // Persist the regID - no need to register again.
					  storeRegistrationId(context, regid);
				  } catch (IOException ex) {
					  ex.printStackTrace();
					  // If there is an error, don't just keep trying to register.
					  // Require the user to click a button again, or perform
					  // exponential back-off.
				  }
				  return "Registration done";
			  }
			  
			  @Override
			  protected void onPostExecute(String msg) {
				  System.out.println(msg);
			  }
		  }.execute();
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