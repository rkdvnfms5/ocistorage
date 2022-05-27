package com.poozim.web.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.springframework.util.ResourceUtils;

public class PropertiesUtil {
	Properties props;
	private final String path = "classpath:datasource.properties";
	public PropertiesUtil() throws IOException {
		try {
			props = new Properties();
			
			File test = ResourceUtils.getFile(path);
			FileInputStream fis = new FileInputStream(test);
			props.load(fis);
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public String getProperty(String prop) {
		return props.getProperty(prop, "");
	}
}
