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
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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

@Controller
@SpringBootApplication
@EnableDiscoveryClient
@EnableEurekaClient
@EnableFeignClients
@RestController
@EnableHystrix
@EnableHystrixDashboard
@EnableCircuitBreaker
@PropertySource("classpath:application.properties")
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

	@RequestMapping("/service-instances/{applicationName}")
	public List<ServiceInstance> serviceInstancesByApplicationName(@PathVariable String applicationName) {
		return this.discoveryClient.getInstances(applicationName);
	}

	@Autowired
	private PLMPartBomService plmpartbomService;

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

			ResponseEntity<?> partResponse = restTemplate.postForEntity(apigeePartPostURL.toString(),
					jsonPartBOM.get("part"), null);
			ResponseEntity<?> bomResponse = null;
			if (Integer.parseInt(partResponse.getStatusCode().toString()) == 200) {
				uriComponents = UriComponentsBuilder.fromHttpUrl(apigeeBomUrl).queryParams(params).build();
				apigeeBomPostURL = new URL(uriComponents.toUriString());
				bomResponse = restTemplate.postForEntity(apigeeBomPostURL.toString(), jsonPartBOM.get("bom"), null);
				if (Integer.parseInt(bomResponse.getStatusCode().toString()) == 200) {
					jsonPartBOM.put("isprocessed", "True");
					jsonPartBOM.put("iserrored", "False");
					jsonPartBOM.put("message", "success");
					jsonPartBOM.put("code", bomResponse.getStatusCode().toString());
					jsonPartBOM.put("status", "");
					jsonPartBOM.put("xmlbloblink", "");
					jsonPartBOM.put("processdate", format.format(date));
					jsonPartBOM.put("createddate", format.format(date));
					jsonPartBOM.put("processby", "SYSTEM");
					jsonPartBOM.put("ecnNo", "1112");
					jsonPartBOM.put("transactionId", "111234");
					jsonPartBOM.put("erp", "SYMIX");
					jsonPartBOM.put("region", "NA");
					jsonPartBOM.put("plant", "RY1");
				} else {
					jsonPartBOM.put("isprocessed", "True");
					jsonPartBOM.put("iserrored", "True");
					jsonPartBOM.put("message", "failed");
					jsonPartBOM.put("code", bomResponse.getStatusCode().toString());
					jsonPartBOM.put("status", "");
					jsonPartBOM.put("processdate", format.format(date));
					jsonPartBOM.put("processby", "SYSTEM");
				}
			} else {
				jsonPartBOM.put("isprocessed", "True");
				jsonPartBOM.put("iserrored", "True");
				jsonPartBOM.put("message", "failed");
				jsonPartBOM.put("code", partResponse.getStatusCode().toString());
				jsonPartBOM.put("status", "");
				jsonPartBOM.put("processdate", format.format(date));
				jsonPartBOM.put("processby", "SYSTEM");
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
