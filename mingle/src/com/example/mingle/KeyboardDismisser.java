package com.example.mingle;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class KeyboardDismisser {
	public static void hideSoftKeyboard(Activity activity) {
	    InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
	    inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
	}
	
	public static void setupKeyboardDismiss(View view, final Activity activity) {

	    //Set up touch listener for non-text box views to hide keyboard.
	    if(!(view instanceof EditText)) {
	    	
	    	view.setOnTouchListener(new OnTouchListener(){

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					//if(v.getOnClickListener() != null)v.performClick();
					if(activity.getCurrentFocus() != null)
						hideSoftKeyboard(activity);
					return false;
				}
	    		
	    	});
	    	
	        
	    }

	    //If a layout container, iterate over children and seed recursion.
	    if (view instanceof ViewGroup) {

	        for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

	            View innerView = ((ViewGroup) view).getChildAt(i);

	            setupKeyboardDismiss(innerView, activity);
	        }
	    }
	}
}


