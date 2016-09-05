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
		
		
		@RequestMapping("/service-instances/{applicationName}")
		public List<ServiceInstance> serviceInstancesByApplicationName(@PathVariable String applicationName) {
			return this.discoveryClient.getInstances(applicationName);
		} 


	@Autowired
	private PLMPartBomService partbomservice;

	@RequestMapping(value = "/receiveJson", method = { RequestMethod.POST })
	public String jsonRecieveAndSend(@RequestBody HashMap<String, Object> jsonXml) throws Exception {
		try
		{

		System.out.println("Data reach at Bom ms from subcriber ms");
		System.out.println("===================PART=======================");
		System.out.println(jsonXml.get("part"));
		System.out.println("===================BOM=======================");
		System.out.println(jsonXml.get("bom"));

		Date date = new Date();
		DateFormat format= new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		System.out.println("Date   "+format.format(date));
		System.out.println("Apigee Part url   "+apigeePartUrl);
		System.out.println("Apigee Bom url    "+apigeeBomUrl);
		
		//from here hit the api and receive the response
		
					/*//part apigee
					JSONParser partParser = new JSONParser();
					URL partUrl = new URL("http://apidev1.jci.com:9055/jcibe/v1/suppliercollaboration/purchaseorders?erpname=SYMIX&region=ASIA&plant=RY1&ordernumber=**&ordercreationdate=**");
					Object partObj=partParser.parse(new InputStreamReader(partUrl.openStream()));
					 JSONObject partJsonObj = new JSONObject(partObj.toString());
					 
					 //bom apigee
					 JSONParser bomParser = new JSONParser();
						URL bomUrl = new URL("http://apidev1.jci.com:9055/jcibe/v1/suppliercollaboration/purchaseorders?erpname=SYMIX&region=ASIA&plant=RY1&ordernumber=**&ordercreationdate=**");
						Object bomObj=bomParser.parse(new InputStreamReader(bomUrl.openStream()));
						 JSONObject bomJsonObj = new JSONObject(bomObj.toString());
					 if(partJsonObj.get("code").toString()=="200")
					 {
						 if(bomJsonObj.get("code").toString()=="200")
						 {
							 jsonXml.put("isprocessed","1");
							 jsonXml.put("iserrored","1");
							 jsonXml.put("message",partJsonObj.get("message"));
							 jsonXml.put("code",partJsonObj.get("code"));
							 jsonXml.put("status",partJsonObj.get("status"));
							 jsonXml.put("xmlbloblink","www.xml.com");
							 jsonXml.put("processdate",format.format(date));
				 			 jsonXml.put("createddate",format.format(date));
							 jsonXml.put("processby","SYSTEM");
							 jsonXml.put("createdby","SYSTEM");
							 jsonXml.put("acknoledge","YES");
							 jsonXml.put("acknoledgestatus","YES");
							 jsonXml.put("acknoledgecode","200");
							 jsonXml.put("acknoledgemessage","ack msg ok");
							  jsonXml.put("acknoledgedate",format.format(date));
							 jsonXml.put("acknoledgeby","ack SYSTEM");
							 jsonXml.put("uiprocessed","1");
							 jsonXml.put("uiprocessedby","ui SYSTEM");
							 
							 
							 
						 }//bom if
						 else
						 {
							 jsonXml.put("isprocessed","1");
							 jsonXml.put("iserrored","1");
							 jsonXml.put("message",partJsonObj.get("message"));
							 jsonXml.put("code",partJsonObj.get("code"));
							 jsonXml.put("status",partJsonObj.get("status"));
							 jsonXml.put("xmlbloblink","www.xml.com");
							 jsonXml.put("processdate",format.format(date));
				 			 jsonXml.put("createddate",format.format(date));
							 jsonXml.put("processby","SYSTEM");
							 jsonXml.put("createdby","SYSTEM");
							 jsonXml.put("acknoledge","YES");
							 jsonXml.put("acknoledgestatus","YES");
							 jsonXml.put("acknoledgecode","500");
							 jsonXml.put("acknoledgemessage","ack msg not ok");
							 jsonXml.put("acknoledgedate",format.format(date));
							 jsonXml.put("acknoledgeby","ack SYSTEM");
							 jsonXml.put("acknoledgeby","ack SYSTEM");
							 jsonXml.put("uiprocessed","1");
							 jsonXml.put("uiprocessedby","ui SYSTEM");
						 }//bom else
						 
					 }//part if
					 else
					 {
						 jsonXml.put("isprocessed","1");
						 jsonXml.put("iserrored","1");
						 jsonXml.put("message",partJsonObj.get("message"));
						 jsonXml.put("code",partJsonObj.get("code"));
						 jsonXml.put("status",partJsonObj.get("status"));
						 jsonXml.put("xmlbloblink","www.xml.com");
						 jsonXml.put("processdate",format.format(date));
				         jsonXml.put("createddate",format.format(date));
						 jsonXml.put("processby","SYSTEM");
						 jsonXml.put("createdby","SYSTEM");
						 jsonXml.put("acknoledge","YES");
						 jsonXml.put("acknoledgestatus","YES");
						 jsonXml.put("acknoledgecode","500");
						 jsonXml.put("acknoledgemessage","ack msg not ok");
						 jsonXml.put("acknoledgedate",format.format(date));
						 jsonXml.put("acknoledgeby","ack SYSTEM");
						 jsonXml.put("uiprocessed","1");
						 jsonXml.put("uiprocessedby","ui SYSTEM");
					 }//part else
*/		
		
		 //uncomment for code  this hard code code  and proper run code 
		JSONParser parser = new JSONParser();
		URL git = new URL("http://johnsoncontroll-test.apigee.net/v1/hello_policies"); 
		Object obj = parser.parse(new InputStreamReader(git.openStream()));
		String jsonStr = obj.toString();
		JSONObject json= (JSONObject) obj;
		System.out.println(jsonStr);
		System.out.println(json.get("root"));
		JSONObject json1=(JSONObject) json.get("root");
		 
		String partJsonCode="200";
		String bomJsonCode="400";
		 if(partJsonCode=="200")
		 {
			 System.out.println("PArt if");
			 if(bomJsonCode=="200")
			 {
				 System.out.println("BOM IF");
				 jsonXml.put("isprocessed","True");
				 jsonXml.put("iserrored","False");
				 jsonXml.put("message",json1.get("firstName"));
				 jsonXml.put("code",json1.get("lastName"));
				 jsonXml.put("status",json1.get("state"));
				 jsonXml.put("xmlbloblink",json1.get("city"));
				 jsonXml.put("processdate",format.format(date));
				 jsonXml.put("createddate",format.format(date));
				 jsonXml.put("processby","SYSTEM");
				 jsonXml.put("createdby","SYSTEM");
				 jsonXml.put("acknoledge","YES");
				 jsonXml.put("acknoledgestatus","YES");
				 jsonXml.put("acknoledgecode","200");
				 jsonXml.put("acknoledgemessage","ack msg ok");
				 jsonXml.put("acknoledgedate",format.format(date));
				 jsonXml.put("acknoledgeby","ack SYSTEM");
				 jsonXml.put("uiprocessed","1");
				 jsonXml.put("uiprocessedby","ui SYSTEM");
				 
				 
				 
			 }//bom if
			 else
			 {
				 System.out.println("BOM Else");
				 jsonXml.put("isprocessed","True");
				 jsonXml.put("iserrored","True");
				 jsonXml.put("message","Success");
				 jsonXml.put("code","200");
				 jsonXml.put("status","unsuccessfull");
				 jsonXml.put("xmlbloblink","www.xml.com");
				 jsonXml.put("processdate",format.format(date));
				 jsonXml.put("createddate",format.format(date));
				 jsonXml.put("processby","SYSTEM");
				 jsonXml.put("createdby","SYSTEM");
				 jsonXml.put("acknoledge","YES");
				 jsonXml.put("acknoledgestatus","YES");
				 jsonXml.put("acknoledgecode","500");
				 jsonXml.put("acknoledgemessage","ack msg not ok");
				 jsonXml.put("acknoledgedate",format.format(date));
				 jsonXml.put("acknoledgeby","ack SYSTEM");
				 jsonXml.put("uiprocessed","1");
				 jsonXml.put("uiprocessedby","ui SYSTEM");
			 }//bom else
			 
		 }//part if
		 else
		 {
			 System.out.println("Part Else");
			 jsonXml.put("isprocessed","True");
			 jsonXml.put("iserrored","True");
			 jsonXml.put("message","Success");
			 jsonXml.put("code","200");
			 jsonXml.put("status","unsuccessfull");
			 jsonXml.put("xmlbloblink","www.xml.com");
			 jsonXml.put("processdate",format.format(date));
			 jsonXml.put("createddate",format.format(date));
			 jsonXml.put("processby","SYSTEM");
			 jsonXml.put("createdby","SYSTEM");
			 jsonXml.put("acknoledge","YES");
			 jsonXml.put("acknoledgestatus","YES");
			 jsonXml.put("acknoledgecode","500");
			 jsonXml.put("acknoledgemessage","ack msg not ok");
			 jsonXml.put("acknoledgedate",format.format(date));
			 jsonXml.put("acknoledgeby","ack SYSTEM");
			 jsonXml.put("uiprocessed","1");
			 jsonXml.put("uiprocessedby","ui SYSTEM");
		 }//part else
		
		
				
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
	public String hystrixCircuitBreaker(){
	
	String value=	partbomservice.hystrixCircuitBreaker();
	System.out.println("-------->Part Bom Get the Return from fallback    "+value);
	return "Success";
	}
	
	
	
}
