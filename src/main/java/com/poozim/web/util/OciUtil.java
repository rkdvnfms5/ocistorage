package com.poozim.web.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.oracle.bmc.model.BmcException;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.model.Bucket;
import com.oracle.bmc.objectstorage.model.CreateBucketDetails;
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails;
import com.oracle.bmc.objectstorage.model.ListObjects;
import com.oracle.bmc.objectstorage.model.ObjectSummary;
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails.AccessType;
import com.oracle.bmc.objectstorage.requests.CreateBucketRequest;
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.GetBucketRequest;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.requests.ListObjectsRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.CreateBucketResponse;
import com.oracle.bmc.objectstorage.responses.CreatePreauthenticatedRequestResponse;
import com.oracle.bmc.objectstorage.responses.DeleteBucketResponse;
import com.oracle.bmc.objectstorage.responses.DeleteObjectResponse;
import com.oracle.bmc.objectstorage.responses.GetBucketResponse;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import com.oracle.bmc.objectstorage.responses.ListObjectsResponse;
import com.oracle.bmc.objectstorage.transfer.DownloadConfiguration;
import com.oracle.bmc.objectstorage.transfer.DownloadManager;
import com.oracle.bmc.objectstorage.transfer.UploadConfiguration;
import com.oracle.bmc.objectstorage.transfer.UploadManager;
import com.oracle.bmc.objectstorage.transfer.UploadManager.UploadRequest;
import com.oracle.bmc.objectstorage.transfer.UploadManager.UploadResponse;
import com.poozim.web.exception.CustomException;
import com.poozim.web.exception.ErrorCode;
import com.poozim.web.handler.OciHandler;
import com.poozim.web.model.ObjectVO;
import com.poozim.web.model.OciResponse;

@Component
public class OciUtil {
	private Logger log = LoggerFactory.getLogger(OciUtil.class);
	
	public static PropertiesUtil propsUtil;
	public static String compartmentId;
	public static String namespaceName;
	
	public static String host = "https://objectstorage.ap-seoul-1.oraclecloud.com";
	
	private static ObjectStorage client;
	
	@Autowired
	public OciUtil(ObjectStorage client) throws IOException {
		this.client = client;
		
		propsUtil = new PropertiesUtil();
		this.compartmentId = propsUtil.getProperty("compartment_ocid");
		this.namespaceName = propsUtil.getProperty("storage_namespace");
	}
	
	/**
	 * 버킷 가져오는 메서드
	 * 
	 * @param 가져올 버킷 이름
	 * @return 버킷
	 */
	public static OciResponse<Bucket> getBucket(String bucketName) {
		GetBucketRequest request =
	        		GetBucketRequest.builder()
	        			.namespaceName(namespaceName)
	        			.bucketName(bucketName)
	        			.build();
	        
        GetBucketResponse response = client.getBucket(request);
	        
        Bucket bucket = response.getBucket();
        
        OciResponse<Bucket> result = OciResponse.<Bucket>builder()
        								.status((bucket == null? 500 : 200))
        								.success((bucket == null? false : true))
        								.data(bucket)
        								.msg((bucket == null? "Not Founded Bucket" : ""))
        								.build();
        return result;
	}
	
	/**
	 * 버킷 생성 메서드
	 * 
	 * @param 생성할 버킷 이름
	 * @return 생성된 버킷 이름
	 * @throws IOException
	 */
	public static OciResponse<String> createBucket(String bucketName) throws IOException {
		//Bucket Check
		if(checkBucketExist(bucketName)) {
			OciResponse<String> result = OciResponse.<String>builder()
					.status(500)
					.success(false)
					.data(null)
					.msg("Already bucket Exist")
					.build();
	        
			return result;
		}
		
		CreateBucketDetails createBucketDetails =
        		CreateBucketDetails.builder()
        			.compartmentId(compartmentId)
        			.name(bucketName)
        			.build();
        
        CreateBucketRequest request = 
        		CreateBucketRequest.builder()
        			.namespaceName(namespaceName)
        			.createBucketDetails(createBucketDetails)
        			.build();
        			
        
        CreateBucketResponse response = client.createBucket(request);
        
        Bucket bucket = response.getBucket();
        
        OciResponse<String> result = OciResponse.<String>builder()
				.status((bucket == null? 500 : 200))
				.success((bucket == null? false : true))
				.data(bucket.getName())
				.msg((bucket == null? "Fail Create Bucket " : ""))
				.build();
        
		return result;
	}
	
	/**
	 * 사전인증 생성 메서드
	 * 
	 * @param 사전인증 등록할 버킷이름
	 * @param 사전인증 만료 날짜
	 * @return Access URI Code
	 * @throws IOException
	 */
	public static OciResponse<String> createPreAuth(String bucketName, Date expireDate) throws IOException {
		//Bucket Check
		if(!checkBucketExist(bucketName)) {
			OciResponse<String> result = OciResponse.<String>builder()
					.status(500)
					.success(false)
					.data(null)
					.msg("Can Not Found Bucket")
					.build();
	        
			return result;
		}
		
		CreatePreauthenticatedRequestDetails details = 
        		CreatePreauthenticatedRequestDetails.builder()
        			.accessType(AccessType.AnyObjectRead)
        			.name(bucketName + "_preAuth")
        			.timeExpires(expireDate)
        			.build();
        
        CreatePreauthenticatedRequestRequest request =
        		CreatePreauthenticatedRequestRequest.builder()
        			.namespaceName(namespaceName)
        			.bucketName(bucketName)
        			.createPreauthenticatedRequestDetails(details)
        			.build();
        
        CreatePreauthenticatedRequestResponse response = client.createPreauthenticatedRequest(request);
        
        String accessUri = response.getPreauthenticatedRequest().getAccessUri();
        String preAuthId = response.getPreauthenticatedRequest().getId();
        
        String data = null;
        if(accessUri != null) {
        	data = accessUri.substring(accessUri.indexOf("/p/") + 3, accessUri.indexOf("/n/"));
        }
        
        
        OciResponse<String> result = OciResponse.<String>builder()
				.status((data == null? 500 : 200))
				.success((data == null? false : true))
				.data(data)
				.msg((data == null? "Fail Create PreAuth " : ""))
				.build();
        
		return result;
	}
	
	/**
	 * 오브젝트 업로드 메서드
	 * 
	 * @param bucketName
	 * @param file
	 * @return 성공 1, 실패 0
	 * @throws IOException 
	 * @throws IllegalStateException 
	 */
	public static int createObject(String bucketName, MultipartFile file, String objectName) throws IllegalStateException, IOException {
		//Bucket Check
		
		UploadConfiguration uploadConfiguration =
                UploadConfiguration.builder()
                        .allowMultipartUploads(true)
                        .allowParallelUploads(true)
                        .build();
		
		UploadManager uploadManager = new UploadManager(client, uploadConfiguration);
		
        Map<String, String> metadata = null;
        String contentType = file.getContentType();
        
        String contentEncoding = null;
        String contentLanguage = null;
        
        if(objectName == null || objectName.equals("")) {
        	objectName = file.getOriginalFilename();
        }
        String ext = objectName.substring(objectName.lastIndexOf(".") + 1);
        
        String tempDir = "/temp/";
        
        //File object = new File(tempDir + file.getName());
        //File object = File.createTempFile(file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf(".")), "."+ext);
        File object = File.createTempFile(objectName.substring(0, objectName.lastIndexOf(".")), "."+ext);
        
        FileUtils.copyInputStreamToFile(file.getInputStream(), object);
        file.getInputStream().close();
    	file.transferTo(object);
        
        PutObjectRequest request =
	                PutObjectRequest.builder()
	                        .bucketName(bucketName)
	                        .namespaceName(namespaceName)
	                        .objectName(objectName)
	                        .contentType(contentType)
	                        .contentLanguage(contentLanguage)
	                        .contentEncoding(contentEncoding)
	                        .opcMeta(metadata)
	                        .build();
		
		UploadRequest uploadDetails =
	                UploadRequest.builder(object).allowOverwrite(true).build(request);
		
		UploadResponse response = uploadManager.upload(uploadDetails);
		
		int res = (response == null? 0 : 1);
        return res;
	}
	
	/**
	 * 오브젝트 업로드 메서드
	 * 
	 * @param bucketName
	 * @param file
	 * @return 성공 1, 실패 0
	 * @throws IOException 
	 * @throws IllegalStateException 
	 */
	public static OciResponse<String[]> createObjectList(String bucketName, List<Part> fileList) throws IllegalStateException, IOException {
		//Bucket Check
		if(!checkBucketExist(bucketName)) {
			OciResponse<String[]> result = OciResponse.<String[]>builder()
					.status(500)
					.success(false)
					.data(null)
					.msg("Can Not Found Bucket")
					.build();
	        
			return result;
		}
		
		String[] res = new String[fileList.size()];
		
		UploadConfiguration uploadConfiguration =
                UploadConfiguration.builder()
                        .allowMultipartUploads(true)
                        .allowParallelUploads(true)
                        .build();
		
		UploadManager uploadManager = new UploadManager(client, uploadConfiguration);
		
        Map<String, String> metadata = null;
        
        for(int i=0; i<fileList.size(); i++) {
        	FilePart file = (FilePart) fileList.get(i);
        	
        	StringBuilder sb = new StringBuilder(file.filename());
        	sb.insert(file.filename().lastIndexOf("."), "_" + TimeUtil.getDateTimeString());
        	
        	String objectName = sb.toString();
        	String ext = objectName.substring(objectName.lastIndexOf(".") + 1); 
        	File object = File.createTempFile(objectName.substring(0, objectName.lastIndexOf(".")), "."+ext);
        	file.transferTo(object).subscribe();
        	
        	PutObjectRequest request =
	                PutObjectRequest.builder()
	                        .bucketName(bucketName)
	                        .namespaceName(namespaceName)
	                        .objectName(objectName)
	                        .contentType(file.headers().getContentType().toString())
	                        .contentLanguage(null)
	                        .contentEncoding(null)
	                        .opcMeta(metadata)
	                        .build();
        	
        	UploadRequest uploadDetails =
	                UploadRequest.builder(object).allowOverwrite(true).build(request);
		
			UploadResponse response = uploadManager.upload(uploadDetails);
			
			res[i] = objectName;
			
			if(response == null) {
				throw new CustomException(ErrorCode.OBJECT_UPLOAD_ERROR);
			}
			
        }
        
        OciResponse<String[]> result = OciResponse.<String[]>builder()
				.status(200)
				.success(true)
				.data(res)
				.msg("")
				.build();
        
        return result;
	}
	
	/**
	 * 오브젝트 다운로드 메서드
	 * 
	 * @param bucketName 버킷명
	 * @param objectName 파일명
	 * @param saveFileName 저장할 파일 경로와 이름
	 * @throws Exception
	 */
	public static void downloadObject(String bucketName, String objectName, String saveFileName) throws Exception {
		//Bucket && Object Check
		if(!checkObjectExist(bucketName, objectName, true)) {
			return;
		}
		
		DownloadConfiguration downloadConfiguration =
                DownloadConfiguration.builder()
                        .parallelDownloads(3)
                        .maxRetries(3)
                        .multipartDownloadThresholdInBytes(6 * 1024 * 1024)
                        .partSizeInBytes(4 * 1024 * 1024)
                        .build();

        DownloadManager downloadManager = new DownloadManager(client, downloadConfiguration);
		
        GetObjectRequest request =
                GetObjectRequest.builder()
                        .namespaceName(namespaceName)
                        .bucketName(bucketName)
                        .objectName(objectName)
                        .build();
        
        // download request and print result
        GetObjectResponse response = downloadManager.getObject(request);
        System.out.println("Content length: " + response.getContentLength() + " bytes");

        // use the stream contents; make sure to close the stream, e.g. by using try-with-resources
        InputStream stream = response.getInputStream();
        OutputStream outputStream = new FileOutputStream("/www/"+saveFileName);
        try {
        	// use fileStream
            byte[] buf = new byte[8192];
            int bytesRead;
            while ((bytesRead = stream.read(buf)) > 0) {
                outputStream.write(buf, 0, bytesRead);
            }
        } // try-with-resources automatically closes streams
        finally {
        	stream.close();
        	outputStream.close();
		}
        
     // or even simpler, if targetting a file:
        response = downloadManager.downloadObjectToFile(request, new File(saveFileName));
        client.close();
	}
	
	/**
	 * 오브젝트 url 가져오는 메서드
	 * 
	 * @param preAuth 사전인증 access code
	 * @param bucketName 버킷 이름
	 * @param objectName 오브젝트 이름
	 * @return
	 */
	public static String getObjectSrc(String preAuth ,String bucketName, String objectName) {
		//Bucket && Object Check
		if(!checkObjectExist(bucketName, objectName, true)) {
			return null;
		}
		
		//Get Bucket
//		ListObjectsRequest request =
//        		ListObjectsRequest.builder()
//        			.namespaceName(namespaceName)
//        			.bucketName(bucketName)
//        			.fields("size, md5, timeCreated, timeModified")
//        			.prefix(objectName)
//        			.build();
//        
//        ListObjectsResponse response = client.listObjects(request);
//        
//        ListObjects list = response.getListObjects();
//        List<ObjectSummary> objectList = list.getObjects();
//        ObjectVO data = null;
//        
//        if(!objectList.isEmpty()) {
//        	data = new ObjectVO().convertFromObjectSummary(objectList.get(0));
//        }
//        //End Get Bucket
//		
//        if(data == null) {
//        	return null;
//        }
        
		StringBuilder src = new StringBuilder(host);
		src.append("/p/").append(preAuth)
			.append("/n/").append(namespaceName)
			.append("/b/").append(bucketName)
			.append("/o/").append(objectName);
		
		return src.toString();
	}
	
	/**
	 * 오브젝트 삭제 메서드
	 * 
	 * @param bucketName 버킷 이름
	 * @param objectName 오브젝트 이름
	 * @return
	 */
	public static OciResponse<String> deleteObject(String bucketName, String objectName) {
		// Check Bucket
		if(!checkBucketExist(bucketName)) {
			OciResponse<String> result = OciResponse.<String>builder()
					.status(500)
					.success(false)
					.data(null)
					.msg("Can Not Found Bucket")
					.build();
	        
			return result;
		}
		
		// Check Object
		if(!checkObjectExist(bucketName, objectName, false)) {
			OciResponse<String> result = OciResponse.<String>builder()
					.status(500)
					.success(false)
					.data(null)
					.msg("Can Not Found Object")
					.build();
	        
			return result;
		}
		
		DeleteObjectRequest request = 
        		DeleteObjectRequest.builder()
        			.bucketName(bucketName)
        			.namespaceName(namespaceName)
        			.objectName(objectName)
        			.build();
        
		DeleteObjectResponse response = client.deleteObject(request);
		
//		int res = (response == null? 0 : 1);
		
		OciResponse<String> result = OciResponse.<String>builder()
					.status((response == null? 500 : 200))
					.success((response == null? false : true))
					.data(objectName)
					.msg((response == null? "Fail Delete Object " : ""))
					.build();
		 
        return result;
	}
	
	/**
	 * 오브젝트 리스트 가져오는 메서드
	 * 
	 * @param bucketName 버킷 이름
	 * @param prefix 오브젝트 이름 시작 문자열
	 * @return List<ObjectVO>
	 */
	public static OciResponse<List<ObjectVO>> getObjectList(String bucketName, String prefix, int limit) {
		// Check Bucket
		if(!checkBucketExist(bucketName)) {
			OciResponse<List<ObjectVO>> result = OciResponse.<List<ObjectVO>>builder()
					.status(500)
					.success(false)
					.data(null)
					.msg("Can Not Found Bucket")
					.build();
	        
			return result;
		}
		
		ListObjectsRequest request =
        		ListObjectsRequest.builder()
        			.namespaceName(namespaceName)
        			.bucketName(bucketName)
        			.fields("size, md5, timeCreated, timeModified")
        			.prefix(prefix)
        			.limit(limit)
        			.build();
        
        ListObjectsResponse response = client.listObjects(request);
        
        ListObjects list = response.getListObjects();
        List<ObjectSummary> objectList = list.getObjects();
        List<ObjectVO> objectVOList = new ArrayList<ObjectVO>();
        
        //Conver to VO
        if(objectList != null && !objectList.isEmpty()) {
        	for(int i=0; i<objectList.size(); i++) {
            	objectVOList.add(new ObjectVO().convertFromObjectSummary(objectList.get(i)));
            }
        }
        
        OciResponse<List<ObjectVO>> result = OciResponse.<List<ObjectVO>>builder()
				.status((objectVOList == null || objectVOList.isEmpty( )? 404 : 200))
				.success((objectVOList == null || objectVOList.isEmpty() ? false : true))
				.data(objectVOList)
				.msg((objectVOList == null || objectVOList.isEmpty() ? 
							"Not Found " + prefix + "in " + bucketName : 
								"Found " + objectVOList.size() + " Objects"))
				.build();
        
		return result;
	}
	
	/**
	 * 오브젝트 1개 가져오는 메서드
	 * 
	 * @param bucketName 버킷 이름
	 * @param objectName 오브젝트 이름
	 * @return ObjectVO
	 */
	public static OciResponse<ObjectVO> getObjectOne(String bucketName, String objectName) {
		// Check Bucket
		if(!checkBucketExist(bucketName)) {
			OciResponse<ObjectVO> result = OciResponse.<ObjectVO>builder()
					.status(500)
					.success(false)
					.data(null)
					.msg("Can Not Found Bucket")
					.build();
	        
			return result;
		}
		
		ListObjectsRequest request =
        		ListObjectsRequest.builder()
        			.namespaceName(namespaceName)
        			.bucketName(bucketName)
        			.fields("size, md5, timeCreated, timeModified")
        			.prefix(objectName)
        			.build();
        
        ListObjectsResponse response = client.listObjects(request);
        
        ListObjects list = response.getListObjects();
        List<ObjectSummary> objectList = list.getObjects();
        ObjectVO data = null;
        
        if(!objectList.isEmpty()) {
        	data = new ObjectVO().convertFromObjectSummary(objectList.get(0));
        }
        
        OciResponse<ObjectVO> result = OciResponse.<ObjectVO>builder()
				.status((data == null ? 404 : 200))
				.success((data == null ? false : true))
				.data(data)
				.msg((data == null ? "Not Found " + objectName + "in " + bucketName : ""))
				.build();
        
		return result;
	}
	
	/**
	 * 다운로드 오브젝트 인풋스트림 가져오는 메서드
	 * 
	 * @param bucketName 버킷명
	 * @param objectName 파일명
	 * @throws Exception
	 */
	public static InputStream getDownloadInputStream(String bucketName, String objectName) throws Exception {
		DownloadConfiguration downloadConfiguration =
                DownloadConfiguration.builder()
                        .parallelDownloads(3)
                        .maxRetries(3)
                        .multipartDownloadThresholdInBytes(6 * 1024 * 1024)
                        .partSizeInBytes(4 * 1024 * 1024)
                        .build();

        DownloadManager downloadManager = new DownloadManager(client, downloadConfiguration);
		
        GetObjectRequest request =
                GetObjectRequest.builder()
                        .namespaceName(namespaceName)
                        .bucketName(bucketName)
                        .objectName(objectName)
                        .build();
        
        // download request and print result
        GetObjectResponse response = downloadManager.getObject(request);
        System.out.println("Content length: " + response.getContentLength() + " bytes");

        // use the stream contents; make sure to close the stream, e.g. by using try-with-resources
        InputStream stream = response.getInputStream();
        return stream;
	}
	
	/**
	 * 버킷 존재 확인 메서드
	 * 
	 * @param bucketName 버킷명
	 */
	public static boolean checkBucketExist(String bucketName) {
		GetBucketRequest request =
        		GetBucketRequest.builder()
        			.namespaceName(namespaceName)
        			.bucketName(bucketName)
        			.build();
        try {
        	GetBucketResponse response = client.getBucket(request);
        	
        	Bucket bucket = response.getBucket();
    		return bucket != null;
        } catch (BmcException e) {
			return false;
		}
	    
	}
	
	/**
	 * 오브젝트 존재 확인 메서드
	 * 
	 * @param bucketName 버킷명
	 * @param objectName 파일명
	 * @param bucketCheck 버킷 체크할지 여부
	 * @throws Exception
	 */
	public static boolean checkObjectExist(String bucketName, String objectName, boolean bucketCheck) {
		if(bucketCheck) {
			if(!checkBucketExist(bucketName)) {
				return false;
			}
		}
		
		ListObjectsRequest request =
        		ListObjectsRequest.builder()
        			.namespaceName(namespaceName)
        			.bucketName(bucketName)
        			.fields("size, md5, timeCreated, timeModified")
        			.prefix(objectName)
        			.build();
        
        ListObjectsResponse response = client.listObjects(request);
        
        ListObjects list = response.getListObjects();
        List<ObjectSummary> objectList = list.getObjects();
        
        if(objectList == null || objectList.isEmpty()) {
        	return false;
        }
        
        if(objectList.get(0).getName().compareTo(objectName) != 0) {
        	return false;
        }
        
		return true;
	}
}
