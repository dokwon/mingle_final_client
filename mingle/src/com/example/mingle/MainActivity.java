package com.example.mingle;

import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.content.LocalBroadcastManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;

import com.example.mingle.HttpHelper;

import android.widget.*;
import android.widget.ImageView.ScaleType;
import android.widget.TextView.OnEditorActionListener;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity implements ActionBar.TabListener {
	public final static String MAIN_TYPE = "com.example.mingle.MAIN_TYPE";	//Intent data to pass on when new Profile Activity started
	
	private String name;
	private String sex;
	private int num; 
	private ArrayList<View> memberViewArr;
	private ArrayList<ImageView> photoViewArr;

	private String type;
	private Context context;
	private ProgressDialog proDialog;
	
	private MingleApplication app;

	public final static int SELECT_FILE = 0;
	static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICTURE_CROP = 3;
    private SharedPreferences prefs = null;
   
    private Uri imageUri;

	
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
				return photoView;
			}
		}
		return null; 
	}
	
	private String photoPath = "";
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	if (resultCode == RESULT_OK) {
    		
    		switch(requestCode) {
    			case REQUEST_IMAGE_CAPTURE :
    				getContentResolver().notifyChange(imageUri, null);
    				performCrop(imageUri);
    				break;
    			case SELECT_FILE :
    				imageUri = data.getData();
    				performCrop(imageUri);
    				break;
    			case PICTURE_CROP :
    				addPhotoAndDisplay();
    				break;
    		}
    		
    	}
    }
    
    private Bitmap rescaledBitmap(ImageView view) {
		    Bitmap bm = BitmapFactory.decodeFile(photoPath, null);
    		// Raw height and width of image
		    final int height = bm.getHeight();
		    final int width = bm.getWidth();
		    int inSampleSize = 1;
		    int reqHeight = view.getHeight();
		    int reqWidth = view.getWidth();
		    if (height > reqHeight || width > reqWidth) {
		
		        final int halfHeight = height / 2;
		        final int halfWidth = width / 2;
		
		        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
		        // height and width larger than the requested height and width.
		        while ((halfHeight / inSampleSize) > reqHeight
		                && (halfWidth / inSampleSize) > reqWidth) {
		            inSampleSize *= 2;
		        }
		    }
		    BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
	     	btmapOptions.inSampleSize = inSampleSize;
	     	return app.rotatedBitmap(BitmapFactory.decodeFile(photoPath, btmapOptions), photoPath);
		    
    }
    private void addPhotoAndDisplay() {
    	ImageView imageView = FindAppropriateImageView();
		imageView.setScaleType(ScaleType.FIT_XY);
    	app.addPhotoPath(photoPath);
     	imageView.setImageBitmap(rescaledBitmap(imageView));
    }
    	
    // Helper method to retrieve the filepath of selected image
    public String getPath(Uri uri, Activity activity) {
        String[] projection = { MediaColumns.DATA }; 
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
        cursor.moveToFirst(); 
        return cursor.getString(column_index);
    }
 
    public void SexOptionChanged(View v) {
    	if (sex.equals("M") && v.equals(findViewById(R.id.womanbutton))) {
    		// Change selection to woman
    		sex = "W";
    		Button manButton = (Button) findViewById(R.id.manbutton);
    		manButton.setBackgroundResource(R.drawable.genderchoice_notman);
    		
    		v.setBackgroundResource(R.drawable.genderchoice_woman);
    	} else if (sex.equals("W") && v.equals(findViewById(R.id.manbutton))) {
    		// Change selection to man
    		sex= "M";
    		Button womanButton = (Button) findViewById(R.id.womanbutton);
    		womanButton.setBackgroundResource(R.drawable.genderchoice_notwoman);
    		
    		v.setBackgroundResource(R.drawable.genderchoice_man);
    	}
    }
    
    public void MemberNumberSelected(View v) {
    	int num_selected = 0;
    	for(int i = 0; i < 6; i++){
    		if(v.equals(memberViewArr.get(i))) {
    			num_selected = i+1;
    		}
    	}
    	
    	num = num_selected;
    	for(int i = 0; i < num_selected; i++)
    		memberViewArr.get(i).setBackgroundResource(R.drawable.peoplenumberpicon);
    	for(int i = num_selected; i < 6; i++)
    		memberViewArr.get(i).setBackgroundResource(R.drawable.peoplenumberpicoff);

    }
    
    
    private void initializeUIViews() {
    	ActionbarController.customizeActionBar(R.layout.custom_actionbar, this, -30, 0);
    	
    	//Set Default Values
    	if(type.equals("new")){
    		name = "";
    		sex = "M";
    		num = 2;
    	} else {
    		name = app.getMyUser().getName();
    		sex = app.getMyUser().getSex();
    		num = app.getMyUser().getNum();
    	}
    	
    	//Set Pic ImageView
    	photoViewArr = new ArrayList<ImageView>();
    	photoViewArr.add((ImageView)findViewById(R.id.photoView1));
    	photoViewArr.add((ImageView)findViewById(R.id.photoView2));
    	photoViewArr.add((ImageView)findViewById(R.id.photoView3));

        for(ImageView view : photoViewArr ) {
        	view.setOnClickListener(new MinglePhotoClickListener( this, photoViewArr));
        }
        if(type.equals("update")){
			for (int i = 0; i < app.getMyUser().getPhotoNum(); i++){
        		FindAppropriateImageView().setImageDrawable(app.getMyUser().getPic(i));
        	}
        } 
 
        EditText editText = (EditText) findViewById(R.id.nicknameTextView);
        if(type.equals("update")) editText.setText(name);
        
        //Set Sex Button
        Button manButton = (Button) findViewById(R.id.manbutton);
        Button womanButton = (Button) findViewById(R.id.womanbutton);
        if (sex.equals("M")) {
    		manButton.setBackgroundResource(R.drawable.genderchoice_man);
    		womanButton.setBackgroundResource(R.drawable.genderchoice_notwoman);
    	} else {
    		manButton.setBackgroundResource(R.drawable.genderchoice_notman);
    		womanButton.setBackgroundResource(R.drawable.genderchoice_woman);
    	}
        
        //Set Num Button
        memberViewArr = new ArrayList<View>();
    	memberViewArr.add(findViewById(R.id.member_1));
    	memberViewArr.add(findViewById(R.id.member_2));
    	memberViewArr.add(findViewById(R.id.member_3));
    	memberViewArr.add(findViewById(R.id.member_4));
    	memberViewArr.add(findViewById(R.id.member_5));
    	memberViewArr.add(findViewById(R.id.member_6));
    	
        for(int i = num; i < 6; i++){
        	memberViewArr.get(i).setBackgroundResource(R.drawable.peoplenumberpicoff);
        }        
        
        //Hide unnecessary buttons
        if(type.equals("new")){
        	//hide modify button
        	RelativeLayout modify_button = (RelativeLayout) findViewById(R.id.modify_button);
        	modify_button.setVisibility(View.GONE);
        } else {
        	//hide delete, enter, and preview button
        	RelativeLayout enter_button = (RelativeLayout) findViewById(R.id.enter_button);
            Button preview_button = (Button) findViewById(R.id.preview_button);
            enter_button.setVisibility(View.GONE);
            preview_button.setVisibility(View.GONE);
        }
       KeyboardDismisser.setupKeyboardDismiss(findViewById(R.id.main_parent), this);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = ((MingleApplication) this.getApplication());
        prefs = getSharedPreferences("com.example.mingle", MODE_PRIVATE);

        if (prefs.getBoolean("firstrun", true)) {
            Intent introIntent = new Intent(this, IntroActivity.class);
            startActivity(introIntent);
            prefs.edit().putBoolean("firstrun", false).commit();
        }
        
        //check if custom title is supported BEFORE setting the content view!
        //boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
       
        setContentView(R.layout.activity_main);
        
        //if(customTitleSupported) getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title_bar);

        context = (Context)this;

        Intent intent = getIntent();
        type = intent.getExtras().getString(MainActivity.MAIN_TYPE);
        initializeUIViews();
      
       
        if(type.equals("new")){
        	LocalBroadcastManager.getInstance(context).registerReceiver(userRequestReceiver,
          		  new IntentFilter(HttpHelper.JOIN_MINGLE));
        } else {
            LocalBroadcastManager.getInstance(context).registerReceiver(userUpdateReceiver,
            		  new IntentFilter(HttpHelper.UPDATE_USER));
        }        
        
        LocalBroadcastManager.getInstance(this).registerReceiver(httpErrorReceiver,
        		  new IntentFilter(HttpHelper.HANDLE_HTTP_ERROR));
    }

    
    public void showPreview(View view){
    	name = ((EditText)findViewById(R.id.nicknameTextView)).getText().toString();
    	String valid_message = app.isValid(name);
    	if(valid_message == null){
    		app.setMyUser(null, name, num, sex);

    		Intent profile_intent = new Intent(context, ProfileActivity.class);
    		profile_intent.putExtra(ProfileActivity.PROFILE_TYPE, "preview");
    		context.startActivity(profile_intent);
    	} else {
    		showInvalidUserAlert(valid_message);
    	}
    }
    
    public void modifyUserData(View view){
    	name =((EditText) findViewById(R.id.nicknameTextView)).getText().toString();
    	//Check validity of user input and send user update request to server
    	String valid_message = app.isValid(name);
    	if(valid_message == null){
        	app.connectHelper.userUpdateRequest(app, name, sex, num);
      } else {
    	   showInvalidUserAlert(valid_message);
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
    
    private BroadcastReceiver userUpdateReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
     	   // Extract data included in the Intent
     	   String data = intent.getStringExtra(HttpHelper.USER_CONF);
 		   try {
 			   JSONObject user_conf_obj = new JSONObject(data);
 	    	   updateUser(user_conf_obj);
 		   } catch (JSONException e) {
 			   // TODO Auto-generated catch block
 			   e.printStackTrace();
 		   }
    	}
    };
    
    public void updateUser(JSONObject userData){
		app.setMyUser(null, name, num, sex);
		ArrayList<String> photo_array = app.getPhotoPaths();
		try {
			if(photo_array.size() < 1) userData.put("PIC_PATH_1", "");
			else userData.put("PIC_PATH_1", photo_array.get(0));
			if(photo_array.size() < 2) userData.put("PIC_PATH_2", "");
    		else userData.put("PIC_PATH_2", photo_array.get(1));
    		if(photo_array.size() < 3) userData.put("PIC_PATH_3", "");
    		else userData.put("PIC_PATH_3", photo_array.get(2));
    		userData.put("DIST_LIM", app.getDist());
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	app.dbHelper.setMyInfo(userData);
    	Toast.makeText(getApplicationContext(), getResources().getString(R.string.update_complete), Toast.LENGTH_SHORT).show();
    }
    
    static class GifView extends View {
        private Movie movie;

        public GifView(Context context) {
            super(context);
            movie = Movie.decodeStream(
                    context.getResources().openRawResource(
                            R.drawable.progress));
        }
        @Override
        protected void onDraw(Canvas canvas) {
        	
            if (movie != null) {
                movie.setTime(
                    (int) SystemClock.uptimeMillis() % movie.duration());
                int xPos = (canvas.getWidth() - movie.width()) / 2;
                int yPos = (canvas.getHeight() - movie.height()) / 2;
                movie.draw(canvas, xPos , yPos);
                invalidate();
            }
        }
    }
    
    //On user creation request, get user's info and send request to server
    public void userCreateButtonPressed(View view) {
    	name = ((EditText)findViewById(R.id.nicknameTextView)).getText().toString();
        //Check validity of user input and send user creation request to server
    	String valid_message = app.isValid(name);
    	if(valid_message == null){
        	proDialog = new ProgressDialog(this);
        	proDialog.setCancelable(false);
        	proDialog.setCanceledOnTouchOutside(false);
            proDialog.setIndeterminate(true);
            proDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            proDialog.getWindow().getAttributes().dimAmount = (float)0.5;
            proDialog.show(); 
            proDialog.setContentView(new GifView(this));
            
        	app.connectHelper.userCreateRequest(app, name, sex, num);
      } else {
    	   showInvalidUserAlert(valid_message);
       }
    }


    public void joinMingle(JSONObject userData) { 
			ArrayList<String> photo_array = app.getPhotoPaths();
    		try {
				if(photo_array.size() < 1) userData.put("PIC_PATH_1", "");
				else userData.put("PIC_PATH_1", photo_array.get(0));
				if(photo_array.size() < 2) userData.put("PIC_PATH_2", "");
	    		else userData.put("PIC_PATH_2", photo_array.get(1));
	    		if(photo_array.size() < 3) userData.put("PIC_PATH_3", "");
	    		else userData.put("PIC_PATH_3", photo_array.get(2));
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
      
            app.dbHelper.setMyInfo(userData);

            try {
        		app.setMyUser(userData.getString("UID"), name, num, sex);
				app.setDist(userData.getInt("DIST_LIM"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
        	proDialog.dismiss();
            
            //Start activity for Mingle Market
            Intent i = new Intent(this, HuntActivity.class);
            startActivity(i);
            finish();
    }

   
    
    
    public void takePicture() {
    	 Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
    	  File photo = new File(Environment.getExternalStorageDirectory(), 
    			  (new Timestamp(new Date().getTime())).toString() + "Pic.jpg");
    	
    	  intent.putExtra(MediaStore.EXTRA_OUTPUT,
    	            Uri.fromFile(photo));
    	 imageUri = Uri.fromFile(photo);
    	
    	 startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    
    
    
    private boolean deviceHasCropUtility(int size) { 
    	
    	return size > 0;
    }
    
    private void performCrop(Uri picUri) {
    	// Initialize intent
    	Intent intent = new Intent("com.android.camera.action.CROP");
    	// set data type to be sent
    	intent.setType("image/*");
    	// get croppers available in the phone
    	List<ResolveInfo> list = getPackageManager().queryIntentActivities( intent, 0 );
    	int size = list.size();
    	// handle the case if there's no cropper in the phone
    	if (!deviceHasCropUtility(size)) {
    		addPhotoAndDisplay();
    	    return;
    	} else {

    	
    	    // Send the Uri path to the cropper intent
    	    intent.setData(picUri); 
    	    intent.putExtra("crop", "true");
    	    intent.putExtra("outputX", 400);
    	    intent.putExtra("outputY", 400);
    	    intent.putExtra("aspectX", 1);
    	    intent.putExtra("aspectY", 1);         
    	    intent.putExtra("scale", true);
    	    // Here's my attempt to ask the intent to save output data as file
    	    File file = null;
    	    // This returns the file created
 	       file = new File(Environment.getExternalStorageDirectory(), 
				  (new Timestamp(new Date().getTime())).toString() + "cropped.jpg");
	        photoPath = file.getAbsolutePath();
	        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));            
	        intent.putExtra("output", Uri.fromFile(file));
    	    // --------------------------------------------------------------------
    	    // -----------> When changing this to false it worked <----------------
    	    // --------------------------------------------------------------------        
    	    intent.putExtra("return-data", false);
    	    // --------------------------------------------------------------------
    	    // --------------------------------------------------------------------
    	    //startActivityForResult(intent, PICTURE_CROP);
    	    // If there's only 1 Cropper in the phone (e.g. Gallery )
    	    //if (size == 1) {
    	            // get the cropper intent found
    	            Intent i        = new Intent(intent);
    	            ResolveInfo res = list.get(0);

    	            i.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

    	            startActivityForResult(i, PICTURE_CROP);
    	    //}
    	}
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

    private void showInvalidUserAlert(String error_msg) {
    	new AlertDialog.Builder(this)
        .setTitle(getResources().getString(R.string.invalid_input))
        .setMessage(error_msg)
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) { 
                // continue with delete
            }
         })
        .setIcon(R.drawable.icon_tiny)
         .show();	
    }
    
	  /* Broadcast Receiver for notification of http error*/
	  private BroadcastReceiver httpErrorReceiver = new BroadcastReceiver() {
	    	@Override
	    	public void onReceive(Context context, Intent intent) {
	        	proDialog.dismiss();
	    		Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
	    	}
	  };

	  @Override
	  public void onDestroy(){
		  if(type.equals("new")){
			  LocalBroadcastManager.getInstance(this).unregisterReceiver(userRequestReceiver);
		  } else {
			  LocalBroadcastManager.getInstance(this).unregisterReceiver(userUpdateReceiver);
		  }
		  LocalBroadcastManager.getInstance(this).unregisterReceiver(httpErrorReceiver);

		  super.onDestroy();
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

