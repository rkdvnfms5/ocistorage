package com.poozim.web.example;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonValue;
import com.nimbusds.jose.shaded.json.JSONObject;
import com.nimbusds.jose.shaded.json.JSONValue;

import io.netty.handler.codec.http.HttpRequest;

public class StorageApiUtil {

	private Logger log = LoggerFactory.getLogger(StorageApiUtil.class);
	
	private String ACL_ID;
	private String ACL_PASSWORD;
	private StringBuilder basicDomain = new StringBuilder("http://localhost/storage/");
	
	public StorageApiUtil(String ACL_ID, String ACL_PASSWORD) {
		this.ACL_ID = ACL_ID;
		this.ACL_PASSWORD = ACL_PASSWORD;
	}
	
	// Create Bucket API
	public void createBucket(String bucketName) throws ClientProtocolException, IOException {
		String uri = basicDomain.append("bucket").toString();
		
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost request = new HttpPost(uri);

		request.setHeader("Content-Type", "application/json");
		request.setHeader("Accept-Type", "application/json");
		request.setHeader("ACL-ID", ACL_ID);
		request.setHeader("ACL-PASSWD", ACL_PASSWORD);
		
		StorageRequest paramObject = new StorageRequest();
		paramObject.setBucketName(bucketName);
		
		StringEntity params = new StringEntity(paramObject.convertToJsonString(), "utf-8");
		request.setEntity(params);
		
		HttpResponse response = client.execute(request);
		
		HttpEntity responseEntity = response.getEntity();
		if(responseEntity != null && responseEntity.getContentLength() > 0) {
			Object responseObj = JSONValue.parse(responseEntity.getContent());
			JSONObject responseJsonObj = (JSONObject)responseObj;
			
			if(responseJsonObj.get("status").toString().equals("200")) {
				System.out.println(responseJsonObj.get("data"));
			} else {
				System.out.println(responseJsonObj.get("msg"));
			}
		}
	}
	
	public void createObject(String bucketName, MultipartFile file) throws IOException {
		String uri = basicDomain.append("object").toString();
		
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost request = new HttpPost(uri);

		request.setHeader("Accept-Type", "application/json");
		request.setHeader("X-BUCKET", bucketName);
		request.setHeader("ACL-ID", ACL_ID);
		request.setHeader("ACL-PASSWD", ACL_PASSWORD);
		
		HttpEntity entity = MultipartEntityBuilder.create()
			.addBinaryBody("files", 
					file.getBytes(), 
					ContentType.APPLICATION_OCTET_STREAM, 
					file.getOriginalFilename())
			.build();
		
		request.setEntity(entity);
		
		HttpResponse response = client.execute(request);
		
		HttpEntity responseEntity = response.getEntity();
		if(responseEntity != null && responseEntity.getContentLength() > 0) {
			Object responseObj = JSONValue.parse(responseEntity.getContent());
			JSONObject responseJsonObj = (JSONObject)responseObj;
			
			if(responseJsonObj.get("status").toString().equals("200")) {
				System.out.println(responseJsonObj.get("data"));
			} else {
				System.out.println(responseJsonObj.get("msg"));
			}
		}
			
	}
}
