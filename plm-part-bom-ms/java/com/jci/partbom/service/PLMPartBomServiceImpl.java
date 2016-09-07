package com.jci.partbom.service;

import java.net.URI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@Service
public class PLMPartBomServiceImpl implements PLMPartBomService {
	private static final Logger LOG = LoggerFactory.getLogger(PLMPartBomServiceImpl.class);
	
	@Bean
	@LoadBalanced
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
		@Autowired
		private DiscoveryClient discoveryClient;
		@Autowired
		RestTemplate restTemplate;
		@RequestMapping("/service-instances/{applicationName}")
		public List<ServiceInstance> serviceInstancesByApplicationName(@PathVariable String applicationName) {
			return this.discoveryClient.getInstances(applicationName);
		} 

	@Override
	public boolean jsonSendToStorage(HashMap<String, Object> jsonXml) {
		LOG.info("#####Started PLMPartBomServiceImpl.jsonSendToStorage#####");
		LOG.info("Data reach to PLMPartBomServiceImpl from PART-BOM ms");
		
					try 
					{
						String storageUri="http://localhost:9292/sendJsonStorage";	
						 Map result = restTemplate.postForObject( storageUri, jsonXml , Map.class); 
					}
				catch(Exception e)
					{
						e.printStackTrace();
						return false;
					}

					LOG.info("#####End PLMPartBomServiceImpl.jsonSendToStorage#####");
		return true;
	}

	@Override
	@HystrixCommand(fallbackMethod = "error")
	public boolean hystrixCircuitBreaker() {
		LOG.info("#####Starting PLMPartBomServiceImpl.hystrixCircuitBreaker#####");
		LOG.info("#####Ending PLMPartBomServiceImpl.hystrixCircuitBreaker#####");											

		return true;
	}

	public boolean error() {
		LOG.info("#####Starting PLMPartBomServiceImpl.error#####");
		LOG.info("#####Ending PLMPartBomServiceImpl.error#####");
		return true;
	}

	}

