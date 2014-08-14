package com.example.mingle;

import java.util.ArrayList;

import com.example.mingle.MainActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class MinglePhotoClickListener implements OnClickListener{
	
	private MainActivity mainActivity;
	private ArrayList<ImageView> imgViews;
	public MinglePhotoClickListener(MainActivity mainActivity, ArrayList<ImageView> views) {
		
		this.mainActivity = mainActivity; 
		 this.imgViews = new ArrayList<ImageView>(views);
	}
	
	
	private void UpdatePhotoAddCancelOpt(int index) {
		ImageView[] optViews = {(ImageView) mainActivity.findViewById(R.id.add1),
		                        (ImageView) mainActivity.findViewById(R.id.add2),
		                        (ImageView) mainActivity.findViewById(R.id.add3)};
		optViews[index].setBackgroundResource(R.drawable.photo_plus);
	}
	
	private int numOfPhotos() {
		int counter = 0;
		for (ImageView view : imgViews) {
			if(view.getDrawable() != null) {
				counter++;
			}
		}
		return counter; 
	}
	
	private void RemovePhoto(ImageView targetView) {
		
		// Remove the image from imageview
		targetView.setImageDrawable(null);
		// Move image up the UI queue
		int indexOfRemoved = imgViews.indexOf(targetView);
		
		// Change photo option
		UpdatePhotoAddCancelOpt(numOfPhotos());
		
		for(ImageView view : imgViews) {
			int indexOfView = imgViews.indexOf(view);
			if(!view.equals(targetView) && indexOfView > indexOfRemoved) 
			{
				targetView.setImageDrawable(view.getDrawable());
				view.setImageDrawable(null);
				targetView = view;
			}
		}
		// Remove image from the Queue
		((MingleApplication) mainActivity.getApplication()).removePhotoPathAtIndex(indexOfRemoved);
		
	}
	
	private void ShowDeleteOption(final ImageView targetView) {
		final CharSequence[] items = { "Yes","Cancel" };
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
		builder.setTitle("Delete this Photo?").setIcon(R.drawable.icon_tiny);
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int item) {
		        if (items[item].equals("Yes")) {
		        	RemovePhoto(targetView);
		        }  else if (items[item].equals("Cancel")) {
		            dialog.dismiss();
		        }
		    }
		});
		builder.show();
	}
	
	
	private void getUserPhoto() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
        "Cancel" };

		AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
		builder.setTitle("Add Photo!").setIcon(R.drawable.icon_tiny);
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int item) {
		        if (items[item].equals("Take Photo")) {
		            mainActivity.takePicture();
		        } else if (items[item].equals("Choose from Library")) {
		            Intent intent = new Intent(
		                    Intent.ACTION_PICK,
		                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		            intent.setType("image/*");
		            mainActivity.startActivityForResult(
		                    Intent.createChooser(intent, "Select File"),
		                    mainActivity.SELECT_FILE);
		        } else if (items[item].equals("Cancel")) {
		            dialog.dismiss();
		        }
		    }
		});
		builder.show();
    }
	
	@Override
	public void onClick(View v) {
		if (((ImageView)v).getDrawable() == null) {
			getUserPhoto();
		} else {
			System.out.println("Photo is null!");
			ShowDeleteOption((ImageView) v);
		}
	}
}


