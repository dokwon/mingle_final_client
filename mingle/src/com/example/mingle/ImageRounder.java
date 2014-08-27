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
	
	
	public static Drawable getRoundedDrawable(Activity context, Drawable drawable, int pixels) {
		final int img_size = ImageRounder.getScreenWidth(context);
        Bitmap scaled = Bitmap.createScaledBitmap(((BitmapDrawable)drawable).getBitmap(),
        		img_size, img_size,
        		true);
        Bitmap rounded = getRoundedCornerBitmap(scaled, pixels);
        return new BitmapDrawable(context.getResources(), rounded);
        
	}
	private static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Config.ARGB_8888);
        
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect topRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight() - pixels);
        final RectF topRectF = new RectF(topRect);
        final Rect bottomRect = new Rect(0, pixels, bitmap.getWidth(), bitmap.getHeight());
        final RectF bottomRectF = new RectF(bottomRect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRect(bottomRectF, paint);
        canvas.drawRoundRect(topRectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, bottomRect, bottomRect, paint);
        canvas.drawBitmap(bitmap, topRect, topRect, paint);

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



