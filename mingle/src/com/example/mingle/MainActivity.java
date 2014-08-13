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
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import com.example.mingle.HttpHelper;






import android.widget.*;
import android.widget.TextView.OnEditorActionListener;






import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {
	public final static String MAIN_TYPE = "com.example.mingle.MAIN_TYPE";	//Intent data to pass on when new Profile Activity started
	
	private String name;
	private String sex;
	private int num; 
	private ArrayList<View> memberViewArr;
	private ArrayList<ImageView> photoViewArr;

	private String type;
	private Context context;
	private ProgressDialog proDialog;
	
	MingleApplication app;

	private static final int COMPRESS_PHOTO_FACTOR = 8;
	public final int SELECT_FILE = 0;
 	
	
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
	
	
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	String photoPath = "";
    	if (resultCode == RESULT_OK) {
    		if(requestCode != REQUEST_IMAGE_CAPTURE && requestCode != SELECT_FILE) return;
    		ImageView imageView = FindAppropriateImageView();
    		if (requestCode == REQUEST_IMAGE_CAPTURE) {   // If the user requested taking a photo
                getContentResolver().notifyChange(imageUri, null);
                photoPath = imageUri.getPath();
            }  else if (requestCode == SELECT_FILE) { // If the user wants to select a file
                imageUri = data.getData();
                photoPath = getPath(imageUri, MainActivity.this);
            } 
    		 app.addPhotoPath(photoPath);
             Bitmap bm;
             BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
             btmapOptions.inSampleSize = COMPRESS_PHOTO_FACTOR;
             bm = app.rotatedBitmap(BitmapFactory.decodeFile(photoPath, btmapOptions), photoPath);
             imageView.setImageBitmap(bm);
    	}
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
    	
    	if(num_selected == 1) Toast.makeText(getApplicationContext(), getResources().getString(R.string.member_num_small), Toast.LENGTH_SHORT).show();
    	else {
    		num = num_selected;
    		for(int i = 0; i < num_selected; i++){
    			memberViewArr.get(i).setBackgroundResource(R.drawable.peoplenumberpicon);
    		}
    		for(int i = num_selected; i < 6; i++){
    			memberViewArr.get(i).setBackgroundResource(R.drawable.peoplenumberpicoff);
    		}
    	}
    }
    
    
    private void initializeUIViews() {
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
        	ArrayList<String> photo_path_arr = app.getPhotoPaths();
        	for(int i = 0; i < photo_path_arr.size(); i++){
        		Bitmap bm;
        		BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
        		btmapOptions.inSampleSize = 16;
        		bm = app.rotatedBitmap(BitmapFactory.decodeFile(photo_path_arr.get(i), btmapOptions), photo_path_arr.get(i));
        		((ImageView) photoViewArr.get(i)).setImageBitmap(bm);
        	}
			for (int i = 0; i < app.getMyUser().getPhotoNum(); i++){
        		((ImageView) photoViewArr.get(i)).setImageDrawable(app.getMyUser().getPic(i));
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
        	Button modify_button = (Button) findViewById(R.id.modify_button);
        	modify_button.setVisibility(View.GONE);
        } else {
        	//hide delete, enter, and preview button
        	Button delete_button = (Button) findViewById(R.id.delete_button);
            Button enter_button = (Button) findViewById(R.id.enter_button);
            Button preview_button = (Button) findViewById(R.id.preview_button);
            delete_button.setVisibility(View.GONE);
            enter_button.setVisibility(View.GONE);
            preview_button.setVisibility(View.GONE);
        }
       
    }
    
    
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = ((MingleApplication) this.getApplication());
        
        //check if custom title is supported BEFORE setting the content view!
        boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        
        setContentView(R.layout.activity_main);
        
        if(customTitleSupported) getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title_bar);

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
    	//Check validity of user input and send user update request to server
    	String valid_message = app.isValid(name);
    	if(valid_message == null){
    		app.setMyUser(null, name, num, sex);
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
    	   updateUser();
    	}
    };
    
    public void updateUser(){
    	Toast.makeText(getApplicationContext(), getResources().getString(R.string.update_complete), Toast.LENGTH_SHORT).show();
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
            proDialog.getWindow().getAttributes().dimAmount = (float)0.8;
            proDialog.show(); 
            
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

    
    
   
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri imageUri;
    
    
    public void takePicture() {
    	 Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
    	  File photo = new File(Environment.getExternalStorageDirectory(), 
    			  (new Timestamp(new Date().getTime())).toString() + "Pic.jpg");
    	
    	  intent.putExtra(MediaStore.EXTRA_OUTPUT,
    	            Uri.fromFile(photo));
    	 imageUri = Uri.fromFile(photo);
    	
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

    private void showInvalidUserAlert(String error_msg) {
    	new AlertDialog.Builder(this)
        .setTitle(getResources().getString(R.string.invalid_input))
        .setMessage(error_msg)
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) { 
                // continue with delete
            }
         })
        .setIcon(android.R.drawable.ic_dialog_alert)
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
}
