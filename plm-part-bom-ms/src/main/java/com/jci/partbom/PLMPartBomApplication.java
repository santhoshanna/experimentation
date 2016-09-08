package com.jci.partbom;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.jci.partbom.service.PLMPartBomService;

@SpringBootApplication
@EnableDiscoveryClient
@EnableEurekaClient
@EnableFeignClients
@RestController
@EnableHystrix
@EnableHystrixDashboard
@EnableCircuitBreaker
@Configuration
//@PropertySource("classpath:application.properties")
public class PLMPartBomApplication {
	public static void main(String[] args) {
		SpringApplication.run(PLMPartBomApplication.class, args);
	}

	private static final Logger LOG = LoggerFactory.getLogger(PLMPartBomApplication.class);

	@Autowired
	RestTemplate resttemplate;

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Autowired
	private DiscoveryClient discoveryClient;

	@Autowired
	RestTemplate restTemplate;

	@Value("${apigee.part.url}")
	private String apigeePartUrl;

	@Value("${apigee.bom.url}")
	public String apigeeBomUrl;

	@Value("${apigee.part.parametername.erp}")
	private String erpParameter;

	@Value("${apigee.part.parametername.region}")
	public String regionParameter;

	@Value("${apigee.part.parametername.plant}")
	private String plantParameter;

/*	@Value("${partbomms.key.isprocessed}")
	private String isProcessedKey;
	
	@Value("${partbomms.key.iserrored}")
	private String isErroredKey;
	
	@Value("${partbomms.key.code}")
	private String codeKey;
	
	@Value("${partbomms.key.message}")
	private String messageKey;
	
	@Value("${partbomms.key.status}")
	private String statusKey;
	
	@Value("${partbomms.key.processedDate}")
	private String processedDateKey;
	
	@Value("${partbomms.key.createdDate}")
	private String createdDateKey;
	
	@Value("${partbomms.key.processedBy}")
	private String processedByKey;
	
	@Value("${partbomms.key.ecnNo}")
	private String ecnNoKey;
	
	@Value("${partbomms.key.erp}")
	private String erpKey;
	
	@Value("${partbomms.key.region}")
	private String regionKey;
	
	@Value("${partbomms.key.plant}")
	private String plantKey;
	
	@Value("${partbomms.key.transactionid}")
	private String transactionIdKey;*/

	@RequestMapping("/service-instances/{applicationName}")
	public List<ServiceInstance> serviceInstancesByApplicationName(@PathVariable String applicationName) {
		return this.discoveryClient.getInstances(applicationName);
	}

	@Autowired
	private PLMPartBomService plmpartbomService;

	@SuppressWarnings({ "unused", "rawtypes" })
	@RequestMapping(value = "/processJSON", method = { RequestMethod.POST })
	public @ResponseBody ResponseEntity<String> processJSON(@RequestBody HashMap<String, Object> jsonPartBOM)
			throws Exception {
		LOG.info("#####Starting PLMPartBomApplication.processJSON #####");
		try {
			LOG.info("Data reach at Bom ms from subcriber ms");
			LOG.info("===================PART=======================");
			LOG.info("" + jsonPartBOM.get("part"));
			LOG.info("===================BOM=======================");
			LOG.info("" + jsonPartBOM.get("bom"));

			Date date = new Date();
			DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

			LOG.info("Date   " + format.format(date));
			LOG.info("Apigee Part url   " + apigeePartUrl);
			LOG.info("Apigee Bom url    " + apigeeBomUrl);
			MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
			params.add(erpParameter, "SYMIX");// Hard coding
			params.add(regionParameter, "NA");// Hard coding
			params.add(plantParameter, "RY1");// Hard coding
			UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(apigeePartUrl).queryParams(params).build();
			URL apigeePartPostURL = new URL(uriComponents.toUriString());
			URL apigeeBomPostURL = null;
			HttpEntity entity = new HttpEntity(jsonPartBOM.get("part"), new HttpHeaders());
			// ResponseEntity<String> partResponse =
			// restTemplate.exchange(apigeePartPostURL.toString(),HttpMethod.POST,
			// entity, String.class);
			// ResponseEntity<String> bomResponse = null;
			// if (partResponse.getStatusCode().is2xxSuccessful()) {
			if (true) {
				uriComponents = UriComponentsBuilder.fromHttpUrl(apigeeBomUrl).queryParams(params).build();
				apigeeBomPostURL = new URL(uriComponents.toUriString());
				// bomResponse =
				// restTemplate.postForEntity(apigeeBomPostURL.toString(),
				// jsonPartBOM.get("bom"), null);
				// if (bomResponse.getStatusCode().is2xxSuccessful()) {
				if (true) {
					//jsonPartBOM.put(isProcessedKey, 1);
					jsonPartBOM.put("isProcessed", 1);
				//	jsonPartBOM.put(isErroredKey, 0);
				//	jsonPartBOM.put(messageKey, "success from apigee");
					// jsonPartBOM.put("code",
					// bomResponse.getStatusCode().toString());
					/*jsonPartBOM.put(codeKey, 200);
					jsonPartBOM.put(statusKey, "success");
					jsonPartBOM.put(processedDateKey, format.format(date));
					jsonPartBOM.put(createdDateKey, format.format(date));
					jsonPartBOM.put(processedByKey, "SYSTEM");
					jsonPartBOM.put(ecnNoKey, jsonPartBOM.get("ecnNo"));
					jsonPartBOM.put(transactionIdKey, jsonPartBOM.get("transactionId"));
					jsonPartBOM.put(erpKey, jsonPartBOM.get("erp"));
					jsonPartBOM.put(regionKey, jsonPartBOM.get("region"));
					jsonPartBOM.put(plantKey, jsonPartBOM.get("plant"));
					jsonPartBOM.put(isErroredKey, 0);
					jsonPartBOM.put(messageKey, "success from apigee");*/
					// jsonPartBOM.put("code",
					// bomResponse.getStatusCode().toString());
					jsonPartBOM.put("isErrored", 0);
					jsonPartBOM.put("message", "success from apigee");
					jsonPartBOM.put("code", 200);
					jsonPartBOM.put("status", "success");
					jsonPartBOM.put("processedDate", format.format(date));
					jsonPartBOM.put("createdDate", format.format(date));
					jsonPartBOM.put("processedBy", "SYSTEM");
					jsonPartBOM.put("ecnNo", jsonPartBOM.get("ecnNo"));
					jsonPartBOM.put("transactionId", jsonPartBOM.get("transactionId"));
					jsonPartBOM.put("erp", jsonPartBOM.get("erp"));
					jsonPartBOM.put("region", jsonPartBOM.get("region"));
					jsonPartBOM.put("plant", jsonPartBOM.get("plant"));
				} else {
					//jsonPartBOM.put(isProcessedKey, 1);
					jsonPartBOM.put("isProcessed", 1);
					/*	jsonPartBOM.put(isErroredKey, 1);
					jsonPartBOM.put(messageKey, "failure from apigee");
					// jsonPartBOM.put("code",
					// bomResponse.getStatusCode().toString());
					jsonPartBOM.put(codeKey, 200);
					jsonPartBOM.put(statusKey, "failure");
					jsonPartBOM.put(processedDateKey, format.format(date));
					jsonPartBOM.put(createdDateKey, format.format(date));
					jsonPartBOM.put(processedByKey, "SYSTEM");
					jsonPartBOM.put(ecnNoKey, jsonPartBOM.get("ecnNo"));
					jsonPartBOM.put(transactionIdKey, jsonPartBOM.get("transactionId"));
					jsonPartBOM.put(erpKey, jsonPartBOM.get("erp"));
					jsonPartBOM.put(regionKey, jsonPartBOM.get("region"));
					jsonPartBOM.put(plantKey, jsonPartBOM.get("plant"));*/
					
					
					jsonPartBOM.put("isErrored", 1);
					jsonPartBOM.put("message", "failure from apigee");
					// jsonPartBOM.put("code",
					// bomResponse.getStatusCode().toString());
					jsonPartBOM.put("code", 200);
					jsonPartBOM.put("status", "failure");
					jsonPartBOM.put("processedDate", format.format(date));
					jsonPartBOM.put("createdDate", format.format(date));
					jsonPartBOM.put("processedBy", "SYSTEM");
					jsonPartBOM.put("ecnNo", jsonPartBOM.get("ecnNo"));
					jsonPartBOM.put("transactionId", jsonPartBOM.get("transactionId"));
					jsonPartBOM.put("erp", jsonPartBOM.get("erp"));
					jsonPartBOM.put("region", jsonPartBOM.get("region"));
					jsonPartBOM.put("plant", jsonPartBOM.get("plant"));
				}
			} else {
			//	jsonPartBOM.put(isProcessedKey, 1);
				jsonPartBOM.put("isProcessed", 1);
	/*			jsonPartBOM.put(isErroredKey, 1);
				jsonPartBOM.put(messageKey, "failure from apigee");
				// jsonPartBOM.put("code",
				// partResponse.getStatusCode().toString());
				jsonPartBOM.put(codeKey, 200);
				jsonPartBOM.put(statusKey, "failure");
				jsonPartBOM.put(processedDateKey, format.format(date));
				jsonPartBOM.put(createdDateKey, format.format(date));
				jsonPartBOM.put(processedByKey, "SYSTEM");
				jsonPartBOM.put(ecnNoKey, jsonPartBOM.get("ecnNo"));
				jsonPartBOM.put(transactionIdKey, jsonPartBOM.get("transactionId"));
				jsonPartBOM.put(erpKey, jsonPartBOM.get("erp"));
				jsonPartBOM.put(regionKey, jsonPartBOM.get("region"));
				jsonPartBOM.put(plantKey, jsonPartBOM.get("plant"));*/
				
				jsonPartBOM.put("isErrored", 1);
				jsonPartBOM.put("message", "failure from apigee");
				// jsonPartBOM.put("code",
				// bomResponse.getStatusCode().toString());
				jsonPartBOM.put("code", 200);
				jsonPartBOM.put("status", "failure");
				jsonPartBOM.put("processedDate", format.format(date));
				jsonPartBOM.put("createdDate", format.format(date));
				jsonPartBOM.put("processedBy", "SYSTEM");
				jsonPartBOM.put("ecnNo", jsonPartBOM.get("ecnNo"));
				jsonPartBOM.put("transactionId", jsonPartBOM.get("transactionId"));
				jsonPartBOM.put("erp", jsonPartBOM.get("erp"));
				jsonPartBOM.put("region", jsonPartBOM.get("region"));
				jsonPartBOM.put("plant", jsonPartBOM.get("plant"));
			}

			plmpartbomService.jsonSendToStorageMS(jsonPartBOM);

		} catch (Exception e) {
			LOG.error("#####Exception in PLMPartBomApplication.processJSON #####", e);
			LOG.info("#####Ending PLMPartBomApplication.processJSON #####");
			return new ResponseEntity<String>("failure", HttpStatus.OK);
		}
		return new ResponseEntity<String>("success", HttpStatus.OK);

	}

	@RequestMapping(value = "/fallBack")
	@ResponseBody
	public ResponseEntity<String> hystrixCircuitBreaker() {
		LOG.info("#####Starting PLMPartBomApplication.hystrixCircuitBreaker #####");
		if (plmpartbomService.hystrixCircuitBreaker()) {
			LOG.info("#####Starting PLMPartBomApplication.hystrixCircuitBreaker #####");
			return new ResponseEntity<String>("fail", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("fail", HttpStatus.OK);
		}
	}
}
