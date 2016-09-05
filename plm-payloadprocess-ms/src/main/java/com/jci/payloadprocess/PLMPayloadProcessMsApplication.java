package com.jci.payloadprocess;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

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
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jci.payloadprocess.service.PLMProcessPayloadService;


@SpringBootApplication
@EnableAutoConfiguration
@RestController
@EnableHystrix
@EnableHystrixDashboard
@EnableCircuitBreaker
@EnableDiscoveryClient
@EnableEurekaClient
@PropertySource("classpath:application.properties")
public class PLMPayloadProcessMsApplication {

	public static void main(String[] args) {
		SpringApplication.run(PLMPayloadProcessMsApplication.class, args);
	}
	
	
	
	@Value("${erp.mapper.json}")
	private String  erpMapperJsonLink;
	
	@Value("${erp.mapper.xsl.pre}")
	public  String erpMapperXslPre;
	
	@Value("${erp.mapper.xsl.post}")
	public  String erpMapperXslPost;
	
	
	
	
	
	
	@Autowired
	PLMProcessPayloadService process;
	
	
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
	@Autowired
	RestTemplate restTemplate;
	

		@Autowired
		private DiscoveryClient discoveryClient;
		
		
		@RequestMapping("/service-instances/{applicationName}")
		public List<ServiceInstance> serviceInstancesByApplicationName(@PathVariable String applicationName) {
			return this.discoveryClient.getInstances(applicationName);
		} 

		
	
	//the below method is called from subscriber ms 
			@RequestMapping(value = "/receiveXml", method = { RequestMethod.POST })
		    
			public  String  processPayload(@RequestBody String xmlPayload) 
			{
				try
				{
				
	//				System.out.println(xmlPayload);
					/*System.out.println("ERPJSon    "+erpMapperJsonLink);
					System.out.println("erpMapperXslPre    "+erpMapperXslPre);
					System.out.println("erpMapperXslPost    "+erpMapperXslPost);*/
					
				
				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				InputSource src = new InputSource();
				src.setCharacterStream(new StringReader(xmlPayload));

				Document doc = builder.parse(src);
				String ecnNo = doc.getElementsByTagName("TransactionNumber").item(0).getTextContent();
				System.out.println("Ecno No find out "+ ecnNo);
				
				process.processPayload(xmlPayload, ecnNo); //two
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				return "Success Node js";
			}
		
		
	

	
	//the below method is called from the UI (It would be a rest call)
		@RequestMapping(value = "/reprocess", method = { RequestMethod.POST })
		public  String  reprocessPayload(@RequestBody HashMap<String, String> hashMap) 
		{
		try
			{
			
				System.out.println("Reprocessing call");
				String completeXml=hashMap.get("CompleteXml");
				String ecnNo=hashMap.get("EcnNo");
				
				
				process.processPayload(completeXml, ecnNo);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}
		@RequestMapping(value = "/fallBack")
		public String hystrixCircuitBreaker(){
		
		String value=	process.hystrixCircuitBreaker();
		
			return "Success";
		}	
}
