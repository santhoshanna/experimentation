package com.jci.ackmsplm;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jci.ackmsplm.domain.JCIASTSampleEntity;
import com.jci.ackmsplm.services.PLMAckmsService;
@EnableDiscoveryClient
@ComponentScan
@EnableAutoConfiguration
@SpringBootApplication
@RestController
public class PLMAckMSApplication {
	private static Logger logger = LoggerFactory.getLogger(PLMAckMSApplication.class);

	public static void main(String[] args) {

		SpringApplication.run(PLMAckMSApplication.class, args);
		logger.debug("This is AckMicroservicesApplication debug message");
		logger.info("This is AckMicroservicesApplication info message");
		logger.warn("This is AckMicroservicesApplication warn message");
		logger.error("This is AckMicroservicesApplication error message");
	}
	
	@Autowired
	private PLMAckmsService service;

	
	@RequestMapping("/AzureST")
	public JCIASTSampleEntity azureST(
			@RequestParam(value = "patritionKey", defaultValue = "Payload", required = false) String partitionkey,
			@RequestParam(value = "rowKey", defaultValue = "0001", required = false) String rowKey) throws IOException {
		return service.retrieveEntity(partitionkey, rowKey);
	}

	
}
