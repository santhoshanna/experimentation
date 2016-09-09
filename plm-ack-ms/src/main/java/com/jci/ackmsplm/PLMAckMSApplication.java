package com.jci.ackmsplm;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.jci.ackmsplm.domain.PLMPayloadTableEntity;
import com.jci.ackmsplm.services.PLMAckMSService;

@EnableDiscoveryClient
@ComponentScan
@EnableAutoConfiguration
@SpringBootApplication
@EnableHystrix
@EnableHystrixDashboard
@EnableCircuitBreaker
@RestController
public class PLMAckMSApplication {

	private static final Logger LOG = LoggerFactory.getLogger(PLMAckMSApplication.class);

	public static void main(String[] args) {

		SpringApplication.run(PLMAckMSApplication.class, args);
		LOG.info("#####STARTING PLM ACK Microservice #####");
	}

	@Bean
	@LoadBalanced
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Autowired
	private DiscoveryClient discoveryClient;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private PLMAckMSService plmackService;

	@Value("${azure.storageTable.TransactionID}")
	private String TransactionID;

	@Value("${azure.storageTable.TableStatus}")
	private String TableStatus;

	@Value("${azure.storageTable.TableMessage}")
	private String TableMessage;

	@RequestMapping("/service-instances/{applicationName}")
	public List<ServiceInstance> serviceInstancesByApplicationName(@PathVariable String applicationName) {
		return this.discoveryClient.getInstances(applicationName);
	}

	// @Scheduled(fixedDelay=60000)
	@RequestMapping(value = "/getAzureStorageEntity", method = RequestMethod.GET, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
	public String getAzureStorageEntity(String partitionkey, String rowKey,
			HashMap<String, Object> retriveEntity) throws IOException {
		LOG.info("#####Starting PLMAckMSApplication.getAzureStorageEntity #####");
		partitionkey = "PLM_SYMIX";
		rowKey = "CN444";

		PLMPayloadTableEntity entity = plmackService.retrieveAzureTableEntity(partitionkey, rowKey);
		try {

			LOG.info("TransactionID========" + entity.getTransactionID().toString());
			LOG.info(entity.getStatus().toString());
			LOG.info(entity.getMessage());
			System.out.println(entity.toString());
			// PTC URL
			String plmPtcUrl = "https://username@password:jci-dev5.ptcmscloud.com/Windchill/servlet/IE/tasks/ext/jci/UpdateEvent.xml?transaction= “idtransaction”&status=”failed”(or” success”)&partoranotheritemid(e.g. RAC0012)=, JCI ERP Middleware Message:  XXXXXX:@message))";
			ResponseEntity<?> response = null;
			LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
			params.add("transactionid", entity.getTransactionID().toString());
			params.add("status", entity.getStatus().toString());
			params.add("partoranotheritemid", "NULL");
			params.add("jcierpmiddlewaremessage", entity.getMessage().toString());

			UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(plmPtcUrl).queryParams(params).build();
			URL ControlsplmURL = new URL(uriComponents.toUriString());
			response = restTemplate.exchange(ControlsplmURL.toString(), org.springframework.http.HttpMethod.POST, null,String.class);

			LOG.info("Response Code " + response.hashCode());
			LOG.info("Status Code " + response.getStatusCode());
			LOG.info("MEssage  Code " + response.getBody());
			LOG.info("MEssage  Code " + entity.toString());

		} catch (Exception e) {
			LOG.error("Exception in PLMAckMSApplication.getAzureStorageEntity", e);
			LOG.info("#####Ending PLMAckMSApplication.getAzureStorageEntity #####");
		} finally {
			LOG.info("#####Ending PLMAckMSApplication.getAzureStorageEntity #####");
			return entity.getMessage()+"\n"+entity.getTransactionID()+"\n"+entity.getStatus();
		}
		// return entity;
	}

}