/**
 * 
 */
package com.jci.storage;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.jci.storage.service.PLMStorageService;

@SpringBootApplication
@RestController
@EnableHystrix
@EnableHystrixDashboard
@EnableCircuitBreaker
@EnableDiscoveryClient
@EnableEurekaClient
public class PLMStrorageApplication {

	public static void main(String[] args) {
		SpringApplication.run(PLMStrorageApplication.class, args);
	}

	private static final Logger LOG = LoggerFactory.getLogger(PLMStrorageApplication.class);

	@Autowired
	private PLMStorageService plmStorageService;

	@RequestMapping(value = "/processXML", method = { RequestMethod.POST })
	public ResponseEntity<String> recieveXMLFromSubscriber(@RequestBody HashMap<String, Object> map) throws Exception {
		LOG.info("#####Staring PLMStrorageApplication.recieveXMLFromSubscriber#####");
		if (plmStorageService.insertPayloadXMLToBlob(map)) {
			LOG.info("#####Ending PLMStrorageApplication.recieveXMLFromSubscriber#####");
			return new ResponseEntity<String>("success", HttpStatus.OK);
		} else {
			LOG.info("#####Ending PLMStrorageApplication.recieveXMLFromSubscriber#####");
			return new ResponseEntity<String>("fail", HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/processJSON", method = { RequestMethod.POST })
	public ResponseEntity<String> recieveJSON(@RequestBody HashMap<String, Object> map) throws Exception {
		LOG.info("#####Starting PLMStrorageApplication.recieveJSON#####");
		LOG.info("PART JSON" + map.get("part"));
		LOG.info("BOM JSON" + map.get("bom"));
		if (plmStorageService.insertPayloadJSONToTable(map)) {
			LOG.info("#####Ending PLMStrorageApplication.recieveJSON#####");
			return new ResponseEntity<String>("success", HttpStatus.OK);
		} else {
			LOG.info("#####Ending PLMStrorageApplication.recieveJSON#####");
			return new ResponseEntity<String>("fail", HttpStatus.OK);
		}
	}

	@RequestMapping(value = "/fallBack")
	public ResponseEntity<String> hystrixCircuitBreaker() {
		LOG.info("#####Starting PLMStrorageApplication.hystrixCircuitBreaker#####");
		if (plmStorageService.hystrixCircuitBreaker()) {
			LOG.info("#####Ending PLMStrorageApplication.hystrixCircuitBreaker#####");
			return new ResponseEntity<String>("Hysterix Fall Back Is Hit", HttpStatus.INTERNAL_SERVER_ERROR);
		} else {
			LOG.info("#####Ending PLMStrorageApplication.hystrixCircuitBreaker#####");
			return new ResponseEntity<String>("Hysterix Fall Back Is Hit", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
