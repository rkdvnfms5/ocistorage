package com.poozim.web.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.poozim.web.exception.CustomException;
import com.poozim.web.exception.ErrorCode;
import com.poozim.web.util.PropertiesUtil;

import reactor.core.publisher.Mono;

@Component
public class AclFilter implements WebFilter{
	private Logger log = LoggerFactory.getLogger(AclFilter.class);
	
	private String ACL_ID;
	private String ACL_PASSWD;
	
	public AclFilter() {
		PropertiesUtil propsUtil;
		try {
			propsUtil = new PropertiesUtil();
			this.ACL_ID = propsUtil.getProperty("ACL_ID");
			this.ACL_PASSWD = propsUtil.getProperty("ACL_PASSWD");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		// check ACL_ID, ACL_PASSWD Init
		if(ACL_ID == null || ACL_PASSWD == null) {
			log.error("Init ACL_ID or ACL_PASSWD ERROR");
			throw new CustomException(ErrorCode.INIT_SERVER_ERROR);
		}
		
		ServerHttpRequest request = exchange.getRequest();
		
		String id = request.getHeaders().getFirst("ACL-ID");
		String password = request.getHeaders().getFirst("ACL-PASSWD");
		
		//check ACL Essential Headers
		if(id == null || password == null) {
			log.error("ACL ID OR PASSWORD VALUE IS NULL");
			throw new CustomException(ErrorCode.REQUIRE_VALUE_ACL);
		}
		
		if(!id.equals(this.ACL_ID) || !password.equals(this.ACL_PASSWD)) {
			log.error("ID OR PASSWORD IS NOT MATCH \nID : " + id + "\nPASSWORD : " + password);
			throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED); 
		}
		
		return chain.filter(exchange);
	}

}
