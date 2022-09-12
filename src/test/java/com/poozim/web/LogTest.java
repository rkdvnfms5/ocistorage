package com.poozim.web;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogTest {

	private Logger log = LoggerFactory.getLogger(LogTest.class);
	
	@Test
	public void testLogBack() {
		log.debug("debug");
		log.info("info");
		log.warn("warn");
		log.error("error");
		log.trace("emart trace");
	}
}
