package com.poozim.web.handler;

import java.io.File;
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
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.oracle.bmc.audit.model.Data;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.model.ObjectSummary;
import com.poozim.web.aop.Bucket;
import com.poozim.web.aop.PreAuth;
import com.poozim.web.exception.GlobalErrorAttributes;
import com.poozim.web.model.ObjectVO;
import com.poozim.web.model.OciResponse;
import com.poozim.web.util.OciUtil;

import io.vavr.Tuple2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class OciHandler {
	private Logger log = LoggerFactory.getLogger(OciHandler.class);
	
	public Mono<ServerResponse> test (ServerRequest request){
		return ServerResponse.ok().contentType(MediaType.TEXT_PLAIN)
				.body(Mono.just("hi").log(), String.class);
	}
	
	public Mono<ServerResponse> createBucket(ServerRequest request) {
		//JSON인 경우
		if(request.headers().contentType().get().compareTo(MediaType.APPLICATION_JSON) == 0) {
			return createBucketJson(request);
		}
		
		//JSON 아닌 경우
		return request.formData().flatMap(data -> {
			String bucketName = Optional.ofNullable(data.getFirst("bucketName").toString().trim()).orElse("");
			
			if(bucketName.length() < 1) {
				throw new BadRequestException("Empty Bucket's Name");
			}
				
			try {
				return ServerResponse.ok().body(Mono.just(OciUtil.createBucket(bucketName)), OciResponse.class);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
			return null;
		});
	}
	
	public Mono<ServerResponse> createBucketJson(ServerRequest request) {
		return request.bodyToMono(Map.class).flatMap(data -> {
			String bucketName = Optional.ofNullable(data.get("bucketName").toString().trim()).orElse("");
			
			if(bucketName.length() < 1) {
				throw new BadRequestException("Empty Bucket's Name");
			}
				
			try {
				return ServerResponse.ok().body(Mono.just(OciUtil.createBucket(bucketName)), OciResponse.class);
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
		if(request.headers().contentType().orElse(MediaType.APPLICATION_JSON).compareTo(MediaType.APPLICATION_JSON) == 0) { //JSON이면
			return createPreAuthJson(request);
		}
		
		return request.formData().flatMap(data -> {
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
				return ServerResponse.ok().body(Mono.just(OciUtil.createPreAuth(bucketName, expireDate)), OciResponse.class);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
			return null;
		}).log();
	}
	
	@Bucket
	public Mono<ServerResponse> createPreAuthJson(ServerRequest request) {
		return request.bodyToMono(Map.class).flatMap(data -> {
			String bucketName = request.headers().firstHeader("X-BUCKET");
			String expireDateStr = Optional.ofNullable(data.get("expireDate").toString().trim()).orElse("");
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
				return ServerResponse.ok().body(Mono.just(OciUtil.createPreAuth(bucketName, expireDate)), OciResponse.class);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
			return null;
		});
	}
	
	@Bucket
	public Mono<ServerResponse> createObject(ServerRequest request) {
		return request.multipartData().flatMap(data -> {
			//Check BucketName
			String bucketName = request.headers().firstHeader("X-BUCKET");
			
			if(bucketName.length() < 1) {
				throw new BadRequestException("Empty Bucket's Name");
			}
			
			//Check Files
			List<Part> fileList = data.get("files");
			
			if(fileList.size() < 1) {
				throw new BadRequestException("Empty Files");
			}
			
			try {
				return ServerResponse.ok().body( Mono.just(OciUtil.createObjectList(bucketName, fileList)), OciResponse.class);
			} catch (IllegalStateException e) {
				log.error(e.getMessage(), e);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
			
			return null;
		});
	}
	
	@Bucket
	public Mono<ServerResponse> deleteObject(ServerRequest request) {
		String bucketName = request.headers().firstHeader("X-BUCKET");
		String objectName = Optional.ofNullable(request.pathVariable("name")).orElse("").trim();
		
		if(objectName.length() < 1) {
			throw new BadRequestException("Empty Object's Name");
		}
		
//		int res = OciUtil.deleteObject(bucketName, objectName);
//		boolean result = (res > 0? true : false);
		
		return ServerResponse.ok().body(Mono.just(OciUtil.deleteObject(bucketName, objectName)), OciResponse.class);
	}
	
	@Bucket
	public Mono<ServerResponse> getObject(ServerRequest request) {
		String bucketName = request.headers().firstHeader("X-BUCKET");
		String prefix = Optional.ofNullable(request.pathVariable("name")).orElse("").trim();
		
		if(prefix.length() < 1) {
			throw new BadRequestException("Empty Object's Name");
		}
		
		return ServerResponse.ok().body(Mono.just(OciUtil.getObjectOne(bucketName, prefix)), OciResponse.class);
	}
	
	@Bucket
	public Mono<ServerResponse> getObjectList(ServerRequest request) {
		String bucketName = request.headers().firstHeader("X-BUCKET");
		String prefix = Optional.ofNullable(request.pathVariable("name")).orElse("").trim();
		int limit = Integer.parseInt(request.queryParam("limit").orElse("10"));
		
		if(prefix.length() < 1) {
			throw new BadRequestException("Empty Object's Name");
		}
		
		return ServerResponse.ok().body(Mono.just(OciUtil.getObjectList(bucketName, prefix, limit)), OciResponse.class);
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
		
		OciResponse<String> result = OciResponse.<String>builder()
										.status((src == null || src.length() < 1? 500 : 200))
										.success((src == null || src.length() < 1? false : true))
										.data(src)
										.msg((src == null || src.length() < 1? "Not Founded Bucket Or ObjectName" : ""))
										.build();
		
		return ServerResponse.ok().body(Mono.just(result), OciResponse.class);
	}
}
