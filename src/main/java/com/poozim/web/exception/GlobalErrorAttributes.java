package com.poozim.web.exception;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import com.poozim.web.util.TimeUtil;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes{
	
	private Logger log = LoggerFactory.getLogger(GlobalErrorAttributes.class);
	
	@Override
	public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
		Map<String, Object> map = super.getErrorAttributes(request, options);
		
		Throwable throwable = getError(request);
		
		// write error log
		if(throwable instanceof Throwable) {
			
			StringBuilder logStr = new StringBuilder("Request Info");
			logStr.append("\nTime : ")
				.append(TimeUtil.getDateTime())
				.append("\nURI : ")
				.append(request.uri())
				.append("\nAddress : ")
				.append(request.remoteAddress());
			
			log.error(logStr.toString());
		}
		
        if (throwable instanceof CustomException) {
        	CustomException ex = (CustomException) getError(request);
            map.put("exception", ex.getClass().getSimpleName());
            map.put("message", ex.getErrorCode().getMessage());
            map.put("status", ex.getErrorCode().getStatus());
            map.put("errorCode", ex.getErrorCode().getCode());
        }

		return map;
	}

	
}
