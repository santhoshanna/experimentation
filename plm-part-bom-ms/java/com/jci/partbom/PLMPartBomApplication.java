package com.jci.partbom;

import java.io.InputStreamReader;


import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.XML;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.jci.partbom.service.PLMPartBomService;

import freemarker.template.SimpleDate;

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
		private String  apigeePartUrl;
		
		@Value("${apigee.bom.url}")
		public  String apigeeBomUrl;
		
		@Value("${date.format}")
		public  String dateFormat;
		
		@Value("${storage.isprocessed}")
		private String  isprocessed;
		
		@Value("${storage.iserrored}")
		public  String iserrored;
		
		@Value("${storage.message}")
		public  String message;
		@Value("${storage.code}")
		private String  code;
		
		@Value("${storage.status}")
		public  String status;
		
		@Value("${storage.xmlbloblink}")
		public  String xmlbloblink;
		@Value("${storage.processdate}")
		private String  processdate;
		
		@Value("${storage.createddate}")
		public  String createddate;
		
		@Value("${storage.processby}")
		public  String processby;
		@Value("${storage.createdby}")
		private String  createdby;
		
		@Value("${storage.acknoledge}")
		public  String acknoledge;
		
		@Value("${storage.acknoledgestatus}")
		public  String acknoledgestatus;
		@Value("${storage.acknoledgecode}")
		private String  acknoledgecode;
		
		@Value("${storage.acknoledgemessagel}")
		public  String acknoledgemessage;
		
		@Value("${storage.acknoledgedate}")
		public  String acknoledgedate;
		@Value("${storage.acknoledgeby}")
		private String  acknoledgeby;
		
		@Value("${storage.uiprocessed}")
		public  String uiprocessed;
		
		@Value("${storage.uiprocessedby}")
		public  String uiprocessedby;
		
		
		@RequestMapping("/service-instances/{applicationName}")
		public List<ServiceInstance> serviceInstancesByApplicationName(@PathVariable String applicationName) {
			LOG.info("#####Starting PLMPartBomApplication.serviceInstancesByApplicationName#####");
			LOG.info("#####Ending PLMPartBomApplication.serviceInstancesByApplicationName#####");
			return this.discoveryClient.getInstances(applicationName);
		} 


	@Autowired
	private PLMPartBomService partbomservice;

	@RequestMapping(value = "/receiveJson", method = { RequestMethod.POST })
	public String jsonRecieveAndSend(@RequestBody HashMap<String, Object> jsonXml) throws Exception {
		try
		{
			LOG.info("#####Starting PLMPartBomApplication.jsonRecieveAndSend#####");

			LOG.info("Data reach at Bom ms from subcriber ms");
			LOG.info("===================PART=======================");
			LOG.info(jsonXml.get("part").toString());
			LOG.info("===================BOM=======================");
			LOG.info(jsonXml.get("bom").toString());

		Date date = new Date();
		DateFormat format= new SimpleDateFormat(dateFormat);
		
		LOG.info("Date   "+format.format(date));
		LOG.info("Apigee Part url   "+apigeePartUrl);
		LOG.info("Apigee Bom url    "+apigeeBomUrl);
		
		JSONParser parser = new JSONParser();
		URL git = new URL(apigeePartUrl); 
		Object obj = parser.parse(new InputStreamReader(git.openStream()));
		String jsonStr = obj.toString();
		JSONObject json= (JSONObject) obj;
		System.out.println(jsonStr);
		System.out.println(json.get("root"));
		JSONObject json1=(JSONObject) json.get("root");
		 
		String partJsonCode="200";
		String bomJsonCode="200";
		 if(partJsonCode=="200")
		 {
			 LOG.info("PArt if Executed");
			 if(bomJsonCode=="200")
			 {
				 LOG.info("BOM IF executed");
				 jsonXml.put(isprocessed,"True");
				 jsonXml.put(iserrored,"False");
				 jsonXml.put(message,json1.get("firstName"));
				 jsonXml.put(code,json1.get("lastName"));
				 jsonXml.put(status,json1.get("state"));
				 jsonXml.put(xmlbloblink,json1.get("city"));
				 jsonXml.put(processdate,format.format(date));
				 jsonXml.put(createddate,format.format(date));
				 jsonXml.put(processby,"SYSTEM");
				 jsonXml.put(createdby,"SYSTEM");
				 jsonXml.put(acknoledge,"YES");
				 jsonXml.put(acknoledgestatus,"YES");
				 jsonXml.put(acknoledgecode,"200");
				 jsonXml.put(acknoledgemessage,"ack msg ok");
				 jsonXml.put(acknoledgedate,format.format(date));
				 jsonXml.put(acknoledgeby,"ack SYSTEM");
				 jsonXml.put(uiprocessed,"1");
				 jsonXml.put(uiprocessedby,"ui SYSTEM");
			 }//bom if
			 else
			 {
				 LOG.info("BOM Else Executed");
				 jsonXml.put(isprocessed,"True");
				 jsonXml.put(iserrored,"True");
				 jsonXml.put(message,json1.get("firstName"));
				 jsonXml.put(code,json1.get("lastName"));
				 jsonXml.put(status,json1.get("state"));
				 jsonXml.put(xmlbloblink,json1.get("city"));
				 jsonXml.put(processdate,format.format(date));
				 jsonXml.put(createddate,format.format(date));
				 jsonXml.put(processby,"SYSTEM");
				 jsonXml.put(createdby,"SYSTEM");
				 jsonXml.put(acknoledge,"YES");
				 jsonXml.put(acknoledgestatus,"YES");
				 jsonXml.put(acknoledgecode,"500");
				 jsonXml.put(acknoledgemessage,"ack msg Not ok");
				 jsonXml.put(acknoledgedate,format.format(date));
				 jsonXml.put(acknoledgeby,"ack SYSTEM");
				 jsonXml.put(uiprocessed,"1");
				 jsonXml.put(uiprocessedby,"ui SYSTEM");
			 }//bom else
			 
		 }//part if
		 else
		 {
			 LOG.info("Part Else Executed");
			 jsonXml.put(isprocessed,"True");
			 jsonXml.put(iserrored,"True");
			 jsonXml.put(message,json1.get("firstName"));
			 jsonXml.put(code,json1.get("lastName"));
			 jsonXml.put(status,json1.get("state"));
			 jsonXml.put(xmlbloblink,json1.get("city"));
			 jsonXml.put(processdate,format.format(date));
			 jsonXml.put(createddate,format.format(date));
			 jsonXml.put(processby,"SYSTEM");
			 jsonXml.put(createdby,"SYSTEM");
			 jsonXml.put(acknoledge,"YES");
			 jsonXml.put(acknoledgestatus,"YES");
			 jsonXml.put(acknoledgecode,"500");
			 jsonXml.put(acknoledgemessage,"ack msg Not ok");
			 jsonXml.put(acknoledgedate,format.format(date));
			 jsonXml.put(acknoledgeby,"ack SYSTEM");
			 jsonXml.put(uiprocessed,"1");
			 jsonXml.put(uiprocessedby,"ui SYSTEM");
		 }//part else
		
		
		 LOG.info("#####Ending PLMPartBomApplication.jsonRecieveAndSend#####");
		 partbomservice.jsonSendToStorage(jsonXml);

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return " Successs fully send data to Storage Ms ";

	}
	@RequestMapping(value = "/fallBack")
	@ResponseBody
	public ResponseEntity<String> hystrixCircuitBreaker(){
		LOG.info("#####Starting PLMPartBomApplication.hystrixCircuitBreaker#####");
	
		if (partbomservice.hystrixCircuitBreaker())
			return new ResponseEntity<String>("success", HttpStatus.OK);
		else
			return new ResponseEntity<String>("failure", HttpStatus.OK);
	
	}
}
