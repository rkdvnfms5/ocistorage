package com.poozim.web.handler;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.oracle.bmc.audit.model.Data;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.poozim.web.aop.Bucket;
import com.poozim.web.aop.PreAuth;
import com.poozim.web.exception.GlobalErrorAttributes;
import com.poozim.web.util.OciUtil;

import reactor.core.publisher.Mono;

@Component
public class OciHandler {
	private Logger log = LoggerFactory.getLogger(OciHandler.class);
	
	public Mono<ServerResponse> test (ServerRequest request){
		return ServerResponse.ok().contentType(MediaType.TEXT_PLAIN)
				.body(Mono.just("hi").log(), String.class);
	}
	
	public Mono<ServerResponse> createBucket(ServerRequest request) {
		//JSON 데이터 
		Mono<MultiValueMap<String, String>> res = request.bodyToMono(MultiValueMap.class);
		
		if(request.headers().contentType().get().compareTo(MediaType.APPLICATION_JSON) > 0) { //JSON이 아니면
			res = request.formData();
		}
		
		return res.flatMap(data -> {
			String bucketName = Optional.ofNullable(data.getFirst("bucketName").toString().trim()).orElse("");
			
			if(bucketName.length() < 1) {
				throw new BadRequestException("Empty Bucket's Name");
			}
				
			try {
				return ServerResponse.ok().body(Mono.just(OciUtil.createBucket(bucketName)), String.class);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
			return null;
		});
	}
	
	public Mono<ServerResponse> deleteBucket(ServerRequest request) {
		
		return ServerResponse.ok().body(null);
	}
	
	@Bucket
	public Mono<ServerResponse> createPreAuth(ServerRequest request) {
		Mono<MultiValueMap<String, String>> res = request.bodyToMono(MultiValueMap.class);
		
		if(request.headers().contentType().orElse(MediaType.APPLICATION_JSON).compareTo(MediaType.APPLICATION_JSON) > 0) { //JSON이 아니면
			res = request.formData();
		}
		
		return res.flatMap(data -> {
			String bucketName = request.headers().firstHeader("X-BUCKET");
			String expireDateStr = Optional.ofNullable(data.getFirst("expireDate").toString().trim()).orElse("");
			Date expireDate;
			
			//exception BucketName Param
			if(bucketName.length() < 1) {
				throw new BadRequestException("Empty Bucket's Name");
			}
				
			//Set ExpireDate Param
			if(expireDateStr.length() < 1) {
				//Default ExpireDate is 5 Years
				expireDate = java.sql.Date.valueOf(LocalDate.now().plusYears(5));
			} else {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				try {
					expireDate = format.parse(expireDateStr);
				} catch (ParseException e) {
					throw new BadRequestException("Incorret ExpireDate Type. It Must Be yyyy-MM-dd");
				}
			}
			
			try {
				return ServerResponse.ok().body(Mono.just(OciUtil.createPreAuth(bucketName, expireDate)), String.class);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
			return null;
		}).log();
	}
	
	@Bucket
	public Mono<ServerResponse> createObject(ServerRequest request) {
		return request.multipartData().flatMap(data -> {
			//Check BucketName
			String bucketName = request.headers().firstHeader("X-BUCKET");
			
			if(bucketName.length() < 1) {
				throw new BadRequestException("Empty Bucket's Name");
			}
			
			//Check ObjectName
			List<String> objectNameList = data.get("objectName").stream()
											.map(part -> part.toString())
											.collect(Collectors.toList());
			
			if(objectNameList.size() < 1) {
				throw new BadRequestException("Empty Object's Name");
			}
			
			//Check Files
			List<Part> fileList = data.get("files");
			
			if(fileList.size() < 1) {
				throw new BadRequestException("Empty Files");
			}
			
			if(fileList.size() != objectNameList.size()) {
				throw new BadRequestException("Incorret Size Files and Object Names");
			}
			
			//Upload Object to Storage
			try {
				return ServerResponse.ok().body(Mono.just(OciUtil.createObjectList(bucketName, fileList, objectNameList)), Arrays.class);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
			return null;
		});
	}
	
	public Mono<ServerResponse> deleteObject(ServerRequest request) {
		
		return ServerResponse.ok().body(null);
	}
	
	public Mono<ServerResponse> getObject(ServerRequest request) {
		
		return ServerResponse.ok().body(null);
	}
	
	public Mono<ServerResponse> getObjectList(ServerRequest request) {
		
		return ServerResponse.ok().body(null);
	}
	
	@PreAuth
	@Bucket
	public Mono<ServerResponse> getObjectSrc(ServerRequest request) {
		String preAuth = request.headers().firstHeader("X-AUTH");
		String bucketName = request.headers().firstHeader("X-BUCKET");
		
		String objectName = Optional.ofNullable(request.pathVariable("name")).orElse("").trim();
		
		if(objectName.length() < 1) {
			throw new BadRequestException("Empty Object's Name");
		}
		
		String src = OciUtil.getObjectSrc(preAuth, bucketName, objectName);
		
		return ServerResponse.ok().body(Mono.just(src), String.class);
	}
}
