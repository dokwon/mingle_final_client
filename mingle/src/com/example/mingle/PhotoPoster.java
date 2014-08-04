package com.example.mingle;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;

import android.graphics.Bitmap;

public class PhotoPoster {
	
	public static HttpResponse postPhoto(ArrayList<String> photoPaths, final String baseURL) throws Exception {
	    
		HttpClient client = new DefaultHttpClient();
	    client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, System.getProperty("http.agent"));
	    
	    HttpPost post = new HttpPost(baseURL);

	    MultipartEntityBuilder builder = MultipartEntityBuilder.create();        
	    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		
	    builder.addTextBody("uid", "uidman");
		for(int i = 0; i < photoPaths.size(); i++) { 
			InputStream is = new FileInputStream(new File(photoPaths.get(i)));
			byte[] data = IOUtils.toByteArray(is);
			
			String numString = Integer.toString(i + 1);
			InputStreamBody inputStreamBody = new InputStreamBody(new ByteArrayInputStream(data),
		    													  "photo_" + numString + ".png");

		    builder.addPart("photo_" + numString, inputStreamBody);
		}     
		
	    HttpEntity yourEntity = builder.build();

	    post.setEntity(yourEntity);
	    HttpResponse response = client.execute(post);      
	    
	    return response;

	} 
	
	private byte[] compressPicture(Bitmap pic) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        pic.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
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
