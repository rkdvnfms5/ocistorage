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
					.GET("/storage/test", request -> handler.test(request))
					.POST("/storage/bucket", request -> handler.createBucket(request))
					.POST("/storage/preauth", request -> handler.createPreAuth(request))
					.POST("/storage/object", request -> handler.createObject(request))
					.GET("/storage/object/{name}/one", request -> handler.getObject(request))
					.GET("/storage/object/{name}/list", request -> handler.getObjectList(request))
					.GET("/storage/object/{name}/src", request -> handler.getObjectSrc(request))
					.DELETE("/storage/object/{name}", request -> handler.deleteObject(request))
					.build();
	}
}
