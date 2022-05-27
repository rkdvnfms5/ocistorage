package com.poozim.web.exception;

import java.util.Map;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes{
	@Override
	public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
		Map<String, Object> map = super.getErrorAttributes(request, options);
		
		Throwable throwable = getError(request);
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
