/**
 * 
 */
package com.jci.storage;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.jci.storage.dao.PLMStorageDao;
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


	//given by subcriber ms
	@Autowired
	private PLMStorageService storageservice;
	@Autowired
	PLMStorageDao storageDao;
		@RequestMapping(value = "/receiveXml", method = { RequestMethod.POST })
		public String recievedXmlFromSubscriber(@RequestBody HashMap<String, Object> xml) throws Exception {

		System.out.println("Data from Subcriber recived at storage");
		System.out.println("-------------------------------------------------------------");
		return storageservice.PutXmlPartBom(xml);

	}
		//given by PART-BOM ms
	@RequestMapping(value = "/sendJsonStorage", method = { RequestMethod.POST })
	public String recievedJson(@RequestBody HashMap<String, Object> jsonXml) throws Exception {

		System.out.println("Storage receiveFromPBMS triggered");

		System.out.println("Data reach at Storage ms from PART-BOM ms");
		System.out.println("===================PART=======================");
		System.out.println(jsonXml.get("part"));
		System.out.println("===================BOM=======================");
		System.out.println(jsonXml.get("bom"));
		

		return storageDao.PutjsonBom(jsonXml);

	}
	
	@RequestMapping(value = "/fallBack")
	public String hystrixCircuitBreaker(){
	storageservice.hystrixCircuitBreaker();
		return "Success";
	}
	
	
}
