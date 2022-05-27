package com.poozim.web.exception;

import java.util.Map;

import org.springframework.boot.autoconfigure.web.WebProperties.Resources;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.http.codec.ServerCodecConfigurer;
import reactor.core.publisher.Mono;

@Component
//@Order(-2)
public class GlobalErrorExceptionHandler extends AbstractErrorWebExceptionHandler{

	public GlobalErrorExceptionHandler(GlobalErrorAttributes globalErrorAttributes, 
			ApplicationContext applicationContext,
			ServerCodecConfigurer serverCodecConfigurer) {
		super(globalErrorAttributes, new Resources(), applicationContext);
		super.setMessageReaders(serverCodecConfigurer.getReaders());
		super.setMessageWriters(serverCodecConfigurer.getWriters());
	}

	@Override
	protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
		
		return RouterFunctions.route(RequestPredicates.all(), request -> this.renderErrorResponse(request));
	}

	private Mono<ServerResponse> renderErrorResponse(ServerRequest request){
		Map<String, Object> errorProperties = getErrorAttributes(request, ErrorAttributeOptions.defaults());
		
		return ServerResponse.status(Integer.parseInt(errorProperties.get("status").toString()))
					.contentType(MediaType.APPLICATION_JSON)
					.body(BodyInserters.fromValue(errorProperties));
	}
	
}
