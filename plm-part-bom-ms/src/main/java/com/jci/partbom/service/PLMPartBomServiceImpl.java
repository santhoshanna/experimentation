package com.jci.partbom.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@Service
@Configuration
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

	@Value("${apigatewayms.name}")
	public String apigatewaymsName;

	@Value("${plmpayloadprocessms.resource}")
	public String plmpayloadprocessmsResource;

	@RequestMapping("/service-instances/{applicationName}")
	public List<ServiceInstance> serviceInstancesByApplicationName(@PathVariable String applicationName) {
		return this.discoveryClient.getInstances(applicationName);
	}

	@Override
	public boolean jsonSendToStorageMS(HashMap<String, Object> jsonXml) {
		LOG.info("#####Starting PLMPartBomServiceImpl.jsonSendToStorageMS #####");
		// sending to storage- ms
		try {
			List<ServiceInstance> apigatewaymsList = serviceInstancesByApplicationName(apigatewaymsName);
			ServiceInstance apigatewaymsInstance = apigatewaymsList.get(0);
			HttpEntity entity = new HttpEntity(jsonXml, new HttpHeaders());
/*			restTemplate.postForObject(apigatewaymsInstance.getUri().toString() + plmpayloadprocessmsResource, jsonXml,
					Map.class);*/
			restTemplate.postForObject("http://M2330338.asia.jci.com:8003/processJSON", jsonXml,
					Map.class);
/*			restTemplate.exchange("http://M2330338.asia.jci.com:8003/processJSON",
					HttpMethod.POST, entity,Map.class);*/
			
			/*response = restTemplate.exchange("http://plm-part-bom-ms:8002/processJSON",
					HttpMethod.POST, entity, String.class);*/
		} catch (Exception e) {
			LOG.error("#####Exception in PLMPartBomServiceImpl.jsonSendToStorageMS#####", e);
			LOG.info("#####Ending PLMPartBomServiceImpl.jsonSendToStorageMS#####");
			return false;
		}
		LOG.info("#####Ending PLMPartBomServiceImpl.jsonSendToStorageMS#####");
		return true;
	}

	@Override
	@HystrixCommand(fallbackMethod = "error")
	public boolean hystrixCircuitBreaker() {
		LOG.info("#####Starting PLMPartBomServiceImpl.hystrixCircuitBreaker #####");
		LOG.info("#####Ending PLMPartBomServiceImpl.hystrixCircuitBreaker #####");
		return true;
	}

	public void error() {
		LOG.info("#####Starting PLMPartBomServiceImpl.error #####");
		LOG.info("#####Ending PLMPartBomServiceImpl.error #####");
	}

}
