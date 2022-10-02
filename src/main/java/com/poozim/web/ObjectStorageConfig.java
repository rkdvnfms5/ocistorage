package com.poozim.web;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.ConfigFileReader.ConfigFile;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.poozim.web.util.PropertiesUtil;
import com.poozim.web.example.StorageApiUtil;

@Configuration
public class ObjectStorageConfig {

	private Logger log = LoggerFactory.getLogger(ObjectStorageConfig.class);
	
	@Bean
	public ObjectStorage client() throws IOException {
		ConfigFile config = ConfigFileReader.parse("~/ocikey/config", "DEFAULT");
		AuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(config);
		ObjectStorage client = new ObjectStorageClient(provider);
		client.setRegion(Region.AP_SEOUL_1);
		return client;
	}
	
	/*
	 * 예시용 Api 유틸 Bean 설정
	 */
	@Bean
	public StorageApiUtil storageApiUtil() {
		PropertiesUtil propsUtil;
		String ACL_ID;
		String ACL_PASSWD;
		
		try {
			propsUtil = new PropertiesUtil();
			ACL_ID = propsUtil.getProperty("ACL_ID");
			ACL_PASSWD = propsUtil.getProperty("ACL_PASSWD");
			
			return new StorageApiUtil(ACL_ID, ACL_PASSWD);
		} catch (IOException e) {
			log.error("Read ACL Properties ERROR : Init Api Util Bean");
			e.printStackTrace();
		}
		return null;
	}
}
