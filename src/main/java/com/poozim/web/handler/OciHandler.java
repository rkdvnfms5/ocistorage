package com.poozim.web.handler;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.BadRequestException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
			String bucketName = data.getFirst("bucketName").toString().trim();
			
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
	
	public Mono<ServerResponse> createPreAuth(ServerRequest request) {
		
		return ServerResponse.ok().body(null);
	}
	
	public Mono<ServerResponse> createObject(ServerRequest request) {
		
		return ServerResponse.ok().body(null);
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
