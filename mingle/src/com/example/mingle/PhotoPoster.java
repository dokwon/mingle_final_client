package com.example.mingle;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;



public class PhotoPoster {
	
	public static String postPhoto(ArrayList<String> photoPaths, String UID, final String baseURL) throws Exception {
	    
		InputStream is_1 = new FileInputStream(new File(photoPaths.get(0)));
		byte[] data_1 = IOUtils.toByteArray(is_1);
		
	    HttpClient client = new DefaultHttpClient();
	    client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, System.getProperty("http.agent"));
	    
	    HttpPost post = new HttpPost(baseURL + "add_photo");

	    //File name format :photo_# (i.e.photo_1.png);
	    InputStreamBody inputStreamBody_1 = new InputStreamBody(new ByteArrayInputStream(data_1), "photo_1.png");    
	    
	    MultipartEntityBuilder builder = MultipartEntityBuilder.create();        
	    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.addTextBody("uid", UID);

	    builder.addPart("photo_1", inputStreamBody_1);
	    
		if(photoPaths.size() > 1) {
			InputStream is_2 = new FileInputStream(new File(photoPaths.get(1)));
			byte[] data_2 = IOUtils.toByteArray(is_2);
		    InputStreamBody inputStreamBody_2 = new InputStreamBody(new ByteArrayInputStream(data_2), "photo_2.png");
		    builder.addPart("photo_2", inputStreamBody_2);
		}	

	    final HttpEntity yourEntity = builder.build();

	    post.setEntity(yourEntity);
	    HttpResponse response = client.execute(post);        

	    return getContent(response);

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
