package com.poozim.web.example;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Data
public class StorageRequest {
	private String bucketName;
	private String preAuth;
	private String objectName;
	private String expireDate;
	private int limit;
	private MultipartFile file;
	
	public String convertToJsonString() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValueAsString(this);
		return mapper.writeValueAsString(this);
	}
}
