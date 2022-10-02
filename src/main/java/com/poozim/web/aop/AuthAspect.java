package com.poozim.web.aop;

import java.util.Optional;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import com.poozim.web.exception.CustomException;
import com.poozim.web.exception.ErrorCode;

@Aspect
@Component
public class AuthAspect {
	private final Logger log = LoggerFactory.getLogger(AuthAspect.class);

	@Before("@annotation(com.poozim.web.aop.PreAuth)")
	public void checkPreAuth(JoinPoint joinPoint) throws Exception {
		ServerRequest request = (ServerRequest) joinPoint.getArgs()[0];
		
		String preAuth = Optional.ofNullable(request.headers().firstHeader("X-AUTH")).orElse("");
		
		if(preAuth.length() == 0) {
			throw new CustomException(ErrorCode.REQUIRE_VALUE_PREAUTH);
		}
		
//		log.info("X-AUTH Value -> " + preAuth);
		
	}
	
	@Before("@annotation(com.poozim.web.aop.Bucket)")
	public void checkBucket(JoinPoint joinPoint) throws Exception {
		ServerRequest request = (ServerRequest) joinPoint.getArgs()[0];
		
		String bucketName = Optional.ofNullable(request.headers().firstHeader("X-BUCKET")).orElse("");
		
		if(bucketName.length() == 0) {
			throw new CustomException(ErrorCode.REQUIRE_VALUE_BUCKET);
		}
		
//		log.info("X-BUCKET Value -> " + bucketName);
	}
}
