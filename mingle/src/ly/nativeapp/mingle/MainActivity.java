package ly.nativeapp.mingle;

import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MotionEventCompat;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ly.nativeapp.mingle.R;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
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
import android.graphics.Rect;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.ImageView.ScaleType;
import android.widget.TextView.OnEditorActionListener;

import org.json.JSONException;
import org.json.JSONObject;
public class MainActivity extends Activity implements ActionBar.TabListener {
	public final static String MAIN_TYPE = "ly.nativeapp.mingle.MAIN_TYPE";	//Intent data to pass on when new Profile Activity started
	
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
    private ArrayList<ImageView> photoViewOpts;
	
    
	private ImageView FindAppropriateImageView() {
		for (int i = 0; i < photoViewArr.size(); i ++) {
			ImageView photoView =  photoViewArr.get(i);
			if (photoView.getDrawable() == null) {
				return photoView;
			}
		}
		return null; 
	}
	
	private void updatePhotoViewOpt(ImageView v) {
		for(int i = 0; i < photoViewArr.size(); i ++) {
			if(photoViewArr.get(i).equals(v))
				photoViewOpts.get(i).setBackgroundResource(R.drawable.photo_delete);
		}
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
		    btmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
	     	btmapOptions.inSampleSize = inSampleSize;
	     	Bitmap temp = app.rotatedBitmap(BitmapFactory.decodeFile(photoPath, btmapOptions), photoPath);
	     	if(temp.getWidth() % 2 == 1) {
	     		temp = Bitmap.createScaledBitmap(temp, temp.getWidth() - 1, temp.getHeight(), true);
	     	} 
	     	return temp;
    }
    
    /*
    private void displayFaceNotFoundDialog() {
		AlertDialog.Builder popupBuilder = new AlertDialog.Builder(this)
			.setTitle(R.string.no_face_recognized_title)
			.setCancelable(false)
			.setMessage(getResources().getString(R.string.no_face_recognized_msg))
			.setIcon(R.drawable.icon_tiny)
			.setNeutralButton(this.getResources().getText(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});
		popupBuilder.show();

    }*/
    
    private void addPhotoAndDisplay() {
    	ImageView imageView = FindAppropriateImageView();
		imageView.setScaleType(ScaleType.FIT_XY);
    	Bitmap rescaled = rescaledBitmap(imageView);
    	
    	app.addPhotoPath(photoPath);
		updatePhotoViewOpt(imageView);
		imageView.setImageBitmap(rescaled);
		
		/*int face_num = findFaces(rescaled);
    	if(face_num <= 0) {
    		// No Faces Found
    		displayFaceNotFoundDialog();
    	} else {
    		app.addPhotoPath(photoPath);
    		updatePhotoViewOpt(imageView);
    		imageView.setImageBitmap(rescaled);
    	}*/
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
    
    private void updateMemberUI(int num_selected) {
    	num = num_selected;
    	for(int i = 0; i < num_selected; i++)
    		memberViewArr.get(i).setBackgroundResource(R.drawable.peoplenumberpicon);
    	for(int i = num_selected; i < 6; i++)
    		memberViewArr.get(i).setBackgroundResource(R.drawable.peoplenumberpicoff);

    }
    
    public void MemberNumberSelected(View v) {
    	int num_selected = 0;
    	for(int i = 0; i < 6; i++){
    		if(v.equals(memberViewArr.get(i))) {
    			num_selected = i+1;
    		}
    	}
    	updateMemberUI(num_selected);

    }
    
    
    private void initializeUIViews() {
    	ActionbarController.customizeActionBar(R.layout.custom_actionbar, this, -30, 0);
    	
    	TextView theme_text_view = (TextView)findViewById(R.id.daily_theme_individual);
    	theme_text_view.setText(app.getThemeToday());
        theme_text_view.setTypeface(app.koreanTypeFace);
        
    	
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
    	
    	
    	photoViewOpts= new ArrayList<ImageView>();
    	photoViewOpts.add((ImageView)findViewById(R.id.add1));
    	photoViewOpts.add((ImageView)findViewById(R.id.add2));
    	photoViewOpts.add((ImageView)findViewById(R.id.add3));
    	

        for(ImageView view : photoViewArr ) {
        	view.setOnClickListener(new MinglePhotoClickListener( this, photoViewArr));
        }
        if(type.equals("update")) {
			for (int i = 0; i < app.getMyUser().getPhotoNum(); i++){
        		FindAppropriateImageView().setImageDrawable(app.getMyUser().getPic(i));
        	}
        } 
 
        final EditText editText = (EditText) findViewById(R.id.nicknameTextView);
        
        if(type.equals("update")) editText.setText(name);
        editText.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_DONE ) 
					editName();
				return false;
			}
        	
        });
        
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
        
        editText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				editText.setSelection(editText.getText().length());
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if(editText.isFocused())
					imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
			}
        	
        });
    }
    

    private Rect mRect = new Rect();
	  @Override
	  public boolean dispatchTouchEvent(MotionEvent ev) {
		  editName();
	      final int action = MotionEventCompat.getActionMasked(ev);
	      EditText mEditText = (EditText) findViewById(R.id.nicknameTextView);
	      int[] location = new int[2];
	      mEditText.getLocationOnScreen(location);
	      mRect.left = location[0];
	      mRect.top = location[1];
	      mRect.right = location[0] + mEditText.getWidth();
	      mRect.bottom = location[1] + mEditText.getHeight();

	      int x = (int) ev.getX();
	      int y = (int) ev.getY();
	      if (action == MotionEvent.ACTION_DOWN && !mRect.contains(x, y) && mEditText.isFocused()) {
	    	  InputMethodManager input = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	    	  input.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
	      } 
	      return super.dispatchTouchEvent(ev);
	  }
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = ((MingleApplication) this.getApplication());
        prefs = getSharedPreferences("ly.nativeapp.mingle", MODE_PRIVATE);

        if (prefs.getBoolean("firstrun", true)) {
            Intent introIntent = new Intent(this, IntroActivity.class);
            startActivity(introIntent);
            prefs.edit().putBoolean("firstrun", false).commit();
        }
        
        setContentView(R.layout.activity_main);
        
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
    
    public void editName() {
    	EditText nickNameInput = (EditText) findViewById(R.id.nicknameTextView);
    	name =nickNameInput.getText().toString();
    	if(name.length() == 6) {
    		name = name.substring(0, 5);
    	}
    	nickNameInput.setText(name);
    }
    
    public void modifyUserData(View view){
    	
    	editName();
    	//Check validity of user input and send user update request to server
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
    	proDialog.dismiss();
    	finish();
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
    /*
    public int findFaces(Bitmap faceBitmap) {
    	int MAX_FACES = 6;
    	 FaceDetector fd;
    	 FaceDetector.Face [] faces = new FaceDetector.Face[MAX_FACES];
    	 
    	 int count = 0;
    	try {
			fd = new FaceDetector(faceBitmap.getWidth(), faceBitmap.getHeight(), MAX_FACES);
	    	 count = fd.findFaces(faceBitmap, faces);
    	 } catch (Exception e) {
    		 e.printStackTrace();
	    	 return -1;
    	 }
    	System.out.println("Returned!!!!" + Integer.valueOf(count));
    	return count;
    }*/
    
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
