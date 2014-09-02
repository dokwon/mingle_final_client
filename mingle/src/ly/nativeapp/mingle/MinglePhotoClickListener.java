package ly.nativeapp.mingle;

import java.util.ArrayList;
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
		((MingleApplication) mainActivity.getApplication()).getMyUser().removePic(indexOfRemoved);
		
	}
	
	AlertDialog deleteDialog;
	
	private void ShowDeleteOption(final ImageView targetView) {
		if( deleteDialog != null && deleteDialog.isShowing() ) return;
		final CharSequence[] items = {(String) mainActivity.getResources().getText(R.string.ok),
				(String) mainActivity.getResources().getText(R.string.cancel) };
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
		builder.setTitle((String) mainActivity.getResources().getText(R.string.photo_delete)).setIcon(R.drawable.icon_tiny);
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int item) {
		        if (items[item].equals(items[0])) {
		        	RemovePhoto(targetView);
		        	 
		        }  else if (items[item].equals(items[1])) {
		        	dialog.dismiss();
		        }
		        System.out.println("Will not fucking dismiss");
		        
		    }
		});
		deleteDialog = builder.create();
		deleteDialog.show();
	}
	
	
	AlertDialog photoOptionDialog;
	
	private void getUserPhoto() {
		if( photoOptionDialog != null && photoOptionDialog.isShowing() ) return;
		final CharSequence[] items = {(String) mainActivity.getResources().getText(R.string.photo_option_camera),
        		(String) mainActivity.getResources().getText(R.string.photo_option_library),
        		(String) mainActivity.getResources().getText(R.string.cancel)};

		AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
		builder.setTitle(R.string.photo_option_title).setIcon(R.drawable.icon_tiny);
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int item) {
		        if (items[item].equals(items[0])) {
		            mainActivity.takePicture();
		        } else if (items[item].equals(items[1])) {
		            Intent intent = new Intent(
		                    Intent.ACTION_PICK,
		                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		            intent.setType("image/*");
		            mainActivity.startActivityForResult(
		                    Intent.createChooser(intent, (String) mainActivity.getResources().getText(R.string.select_file)),
		                    MainActivity.SELECT_FILE);
		        } else if (items[item].equals(items[2])) {
		            dialog.dismiss();
		        }
		    }
		});
		photoOptionDialog = builder.create();
		photoOptionDialog.show();
    }
	
	@Override
	public void onClick(View v) {
		if (((ImageView)v).getDrawable() == null) {
			getUserPhoto();
		} else {
			ShowDeleteOption((ImageView) v);
		}
	}
}


