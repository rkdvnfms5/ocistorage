package com.poozim.web.handler;

import static org.hamcrest.CoreMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.poozim.web.router.OciRouter;

import reactor.core.publisher.Mono;

@WebFluxTest(controllers = OciHandler.class)
@ContextConfiguration(classes = {OciRouter.class, OciHandler.class})
public class OciHandlerTest {

	@Autowired
	private WebTestClient client;
	
	@Test
	public void routeTest() {
		
		client.get().uri("/test").accept(MediaType.TEXT_PLAIN)
			.exchange()
			.expectStatus().isOk()
			.expectBody(String.class).isEqualTo("hi");
	}
}
