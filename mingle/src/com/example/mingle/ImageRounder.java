package com.example.mingle;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ComposeShader;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Gravity;

public class ImageRounder{
	
	
	public static Drawable getProfileRoundedDrawable(Activity context, Drawable drawable, int pixels) {
		final int img_size = ImageRounder.getScreenWidth(context);
        Bitmap scaled = Bitmap.createScaledBitmap(((BitmapDrawable)drawable).getBitmap(),
        		img_size, img_size,
        		true);
        final Rect topRect = new Rect(0, 0, scaled.getWidth(), scaled.getHeight() - pixels);
        final Rect bottomRect = new Rect(0, pixels,scaled.getWidth(), scaled.getHeight());
        Bitmap rounded = getRoundedCornerBitmap(scaled, pixels, bottomRect, topRect);
        return new BitmapDrawable(context.getResources(), rounded);
        
	}
	
	public static Drawable getVoteRoundedDrawable(Activity context, Drawable drawable, int pixels, int width, int height) {
		
        Bitmap scaled = Bitmap.createScaledBitmap(((BitmapDrawable)drawable).getBitmap(),
        		width, height,
        		true);
        final Rect rightRect = new Rect(pixels, 0, width, height);
        final Rect leftRect = new Rect(0, 0, pixels + 40 , height);
        Bitmap rounded = getRoundedCornerBitmap(scaled, pixels,  rightRect,leftRect);
        return new BitmapDrawable(context.getResources(), rounded);
        
	}
	
	private static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels, final Rect fillRect, final Rect roundRect) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Config.ARGB_8888);
        
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
       
        final RectF topRectF = new RectF(roundRect);
       
        final RectF bottomRectF = new RectF(fillRect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRect(bottomRectF, paint);
        canvas.drawRoundRect(topRectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, fillRect,fillRect, paint);
        canvas.drawBitmap(bitmap, roundRect, roundRect, paint);

        return output;
    }
	
	public static int getScreenWidth(Activity context) { 
    	DisplayMetrics displaymetrics = new DisplayMetrics();
    	((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
    	int totalMargin = context.getResources().getDimensionPixelSize(R.dimen.option_margin) * 2 + 
    			context.getResources().getDimensionPixelSize(R.dimen.small_margin) * 2 + 20;
    	return displaymetrics.widthPixels - totalMargin;
    }
	
	
}



