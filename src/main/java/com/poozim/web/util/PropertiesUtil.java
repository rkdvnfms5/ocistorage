package com.poozim.web.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;

import com.poozim.web.exception.CustomException;
import com.poozim.web.exception.ErrorCode;

public class PropertiesUtil {
	Properties props;
	ClassPathResource classPathResource = new ClassPathResource("datasource.properties");
	
	public PropertiesUtil() throws IOException {
		try (InputStream is = classPathResource.getInputStream()){
			props = new Properties();
			props.load(is);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new CustomException(ErrorCode.PROPERTIES_READ_ERROR);
		}
	}
	
	public String getProperty(String prop) {
		return props.getProperty(prop, "");
	}
}
