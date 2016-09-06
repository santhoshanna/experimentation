package com.jci.payloadprocess;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.jci.payloadprocess.service.PLMProcessPayloadService;

@SpringBootApplication
@EnableAutoConfiguration
@RestController
@EnableHystrix
@EnableHystrixDashboard
@EnableCircuitBreaker
@EnableDiscoveryClient
@EnableEurekaClient
@Configuration
@PropertySource("classpath:application.properties")
public class PLMPayloadProcessMsApplication {

	public static void main(String[] args) {
		SpringApplication.run(PLMPayloadProcessMsApplication.class, args);
	}

	private static final Logger LOG = LoggerFactory.getLogger(PLMPayloadProcessMsApplication.class);

	@Value("${xml.input.xmltags.ecnno}")
	private String xmltagsECNNo;

	@Value("${xml.input.xmltags.transactionno}")
	public String xmltagsTransactionNo;

	@Value("${xml.input.xmltags.destination}")
	public String xmltagsDestination;

	@Autowired
	PLMProcessPayloadService plmProcessPayloadService;

	@Bean
	@LoadBalanced
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private DiscoveryClient discoveryClient;

	@RequestMapping("/service-instances/{applicationName}")
	public List<ServiceInstance> serviceInstancesByApplicationName(@PathVariable String applicationName) {
		LOG.info("#####Starting PLMPayloadProcessMsApplication.serviceInstancesByApplicationName#####");
		LOG.info("#####Ending PLMPayloadProcessMsApplication.serviceInstancesByApplicationName#####");
		return this.discoveryClient.getInstances(applicationName);
	}

	// the below method is called from subscriber ms
	@RequestMapping(value = "/processXML", method = { RequestMethod.POST })
	public ResponseEntity<String> processPayload(@RequestBody String xmlPayload) {
		LOG.info("#####Starting PLMPayloadProcessMsApplication.processPayload#####");
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource src = new InputSource();
			src.setCharacterStream(new StringReader(xmlPayload));

			Document doc = builder.parse(src);
			String ecnNo = doc.getElementsByTagName(xmltagsECNNo).item(0).getTextContent();
			String transactionId = doc.getElementsByTagName(xmltagsTransactionNo).item(0).getTextContent();
			String plant = doc.getElementsByTagName(xmltagsDestination).item(0).getTextContent();
			LOG.info("Ecno No find out " + ecnNo);
			LOG.info("transactionId  find out " + transactionId);
			LOG.info("Destination find out " + plant);

			if (plmProcessPayloadService.processPayload(xmlPayload, ecnNo, transactionId, plant)) {
				LOG.info("#####Ending PLMPayloadProcessMsApplication.processPayload#####");
				return new ResponseEntity<String>("success", HttpStatus.OK);
			} else {
				LOG.info("#####Ending PLMPayloadProcessMsApplication.processPayload#####");
				return new ResponseEntity<String>("failure", HttpStatus.OK);
			}
		} catch (Exception e) {
			LOG.error("#####Exception in PLMPayloadProcessMsApplication.processPayload#####" + e);
			return new ResponseEntity<String>("failure", HttpStatus.OK);
		}
	}

	// the below method is called from the UI (It would be a rest call)
	@RequestMapping(value = "/reprocessXML", method = { RequestMethod.POST })
	public ResponseEntity<String> reprocessPayload(@RequestBody HashMap<String, String> hashMap) {
		LOG.info("#####Starting PLMPayloadProcessMsApplication.reprocessPayload#####");
		try {
			LOG.info("Reprocessing call");
			String completeXml = hashMap.get("CompleteXml");
			String ecnNo = hashMap.get("EcnNo");
			String transactionId = hashMap.get("transactionId");
			String plant = hashMap.get("Destination");

			plmProcessPayloadService.processPayload(completeXml, ecnNo, transactionId, plant);
		} catch (Exception e) {
			LOG.error("#####Exception while reporcessing xml in PLMPayloadProcessMsApplication.reprocessPayload#####",
					e);
			LOG.info("#####Ending PLMPayloadProcessMsApplication.reprocessPayload#####");
			return new ResponseEntity<String>("failure", HttpStatus.OK);
		}
		LOG.info("#####Ending PLMPayloadProcessMsApplication.reprocessPayload#####");
		return new ResponseEntity<String>("success", HttpStatus.OK);
	}

	@RequestMapping(value = "/fallBack")
	public ResponseEntity<String> hystrixCircuitBreaker() {
		if (plmProcessPayloadService.hystrixCircuitBreaker())
			return new ResponseEntity<String>("success", HttpStatus.OK);
		else
			return new ResponseEntity<String>("failure", HttpStatus.OK);
	}
}
