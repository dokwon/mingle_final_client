package com.example.mingle;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;

public class PhotoPoster {
	
	public static HttpResponse postPhoto(MingleApplication app, final String baseURL) {
		ArrayList<String> photoPaths = app.getPhotoPaths();

		HttpClient client = new DefaultHttpClient();
	    client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, System.getProperty("http.agent"));
	    
	    HttpPost post = new HttpPost(baseURL);

	    MultipartEntityBuilder builder = MultipartEntityBuilder.create();        
	    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		
	    builder.addTextBody("uid", "uidman");
		for(int i = 0; i < photoPaths.size(); i++) { 
			
			BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
            btmapOptions.inSampleSize = 8;
            String photoPath = photoPaths.get(i);
			Bitmap bm = app.rotatedBitmap(BitmapFactory.decodeFile(photoPath, btmapOptions), photoPath);
			ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
			bm.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, bos); 
			    
			ByteArrayInputStream bs = new ByteArrayInputStream(bos.toByteArray());

			String numString = Integer.toString(i + 1);
			InputStreamBody inputStreamBody = new InputStreamBody(bs,
		    													  "photo_" + numString + ".png");

		    builder.addPart("photo_" + numString, inputStreamBody);
		}     
		
	    HttpEntity yourEntity = builder.build();

	    post.setEntity(yourEntity);
	    HttpResponse response = null;
		try {
			response = client.execute(post);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//handle http error
			Intent dispatcher = new Intent(app, MainActivity.class);
			dispatcher.setAction(HttpHelper.HANDLE_HTTP_ERROR);
			LocalBroadcastManager.getInstance(app).sendBroadcast(dispatcher); 
			e.printStackTrace();
		}      
	    
	    return response;

	} 
	

	public static String getContent(HttpResponse response) throws IOException {
	    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	    String body = "";
	    String content = "";

	    while ((body = rd.readLine()) != null) 
	    {
	        content += body + "\n";
	    }
	    return content.trim();
	}

}
