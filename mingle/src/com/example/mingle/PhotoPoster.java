package com.example.mingle;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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

public class PhotoPoster {
	
	public static String postPhoto(ArrayList<String> photoPaths, String UID, final String baseURL) throws Exception {
	    
		HttpClient client = new DefaultHttpClient();
	    client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, System.getProperty("http.agent"));
	    
	    HttpPost post = new HttpPost(baseURL + "add_photo");

	    MultipartEntityBuilder builder = MultipartEntityBuilder.create();        
	    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		
	    builder.addTextBody("uid", UID);
		for(int i = 0; i < photoPaths.size(); i++) { 
			InputStream is = new FileInputStream(new File(photoPaths.get(i)));
			byte[] data = IOUtils.toByteArray(is);
			
			String numString = Integer.toString(i + 1);
			InputStreamBody inputStreamBody = new InputStreamBody(new ByteArrayInputStream(data),
		    													  "photo_" + numString + ".png");

		    builder.addPart("photo_" + numString, inputStreamBody);
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
