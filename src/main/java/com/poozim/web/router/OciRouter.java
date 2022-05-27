package com.poozim.web.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.poozim.web.handler.OciHandler;

@Configuration
public class OciRouter {

	@Bean
	public RouterFunction<ServerResponse> routeOci(OciHandler handler){
		
		return RouterFunctions.route()
					.GET("/test", request -> handler.test(request))
					.POST("/bucket", request -> handler.createBucket(request))
					.POST("/preauth", request -> handler.createPreAuth(request))
					.POST("/object", request -> handler.createObject(request))
					.GET("/object/{name}", request -> handler.getObject(request))
					.DELETE("/object/{name}", request -> handler.deleteBucket(request))
					.GET("/object/{name}/src", request -> handler.getObjectSrc(request))
					.build();
	}
}
