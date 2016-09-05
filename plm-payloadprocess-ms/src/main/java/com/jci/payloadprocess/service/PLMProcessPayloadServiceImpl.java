package com.jci.payloadprocess.service;

import java.io.BufferedWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.JsonPath;
import com.jci.payloadprocess.PLMPayloadProcessMsApplication;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import net.minidev.json.parser.JSONParser;

@Service
public class PLMProcessPayloadServiceImpl implements PLMProcessPayloadService{
	  

	
	@Bean
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

	private static int reprocess;
	@Override
	public String processPayload(String completeXml,String ecnNo)
	{
		try
		{
			System.out.println("processpayload() is executed . . . . . . .");
			System.out.println("value of ecnNo is    " + ecnNo);
			File file = new File("Payload.xml");
			if (!file.exists()) {
			file.createNewFile();
			}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(completeXml);
		bw.close();
		
		/*JSONParser parser = new JSONParser();
		URL git = new URL("https://raw.githubusercontent.com/adnapster/XslTJson/master/JSON.json");   
		Object obj = parser.parse(new InputStreamReader(git.openStream()));
		String jsonStr = obj.toString();
		String plant = "RY1"; // this value need to be take from xml
		List<String> values = JsonPath.read(jsonStr, String.format("$.data.mapping.[?(@.plant==%s)].erp", plant));
		List<String> values1 = JsonPath.read(jsonStr, String.format("$.data.mapping.[?(@.plant==%s)].region", plant));
		String erp = values.isEmpty() ? null : values.get(0);
		String region=values1.isEmpty() ? null : values1.get(0);
		
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(
			"https://raw.githubusercontent.com/adnapster/XslTJson/master/" + erp + ".xsl"));  // path need to be take from properties file
		transformer.transform(new javax.xml.transform.stream.StreamSource("Payload.xml"),
			new javax.xml.transform.stream.StreamResult(new FileOutputStream("Payload_XSLT_Out.xml")));
		*/
		
		JSONParser jp = new JSONParser();
        Object object = jp.parse( new FileReader("JSON.json" ));
        net.minidev.json.JSONObject jso = (net.minidev.json.JSONObject) object;
        
        String plant = "RY1"; // this value I can fetch from XML
		List<String> values = JsonPath.read(jso, String.format("$.data.mapping.[?(@.plant==%s)].erp", plant));
		List<String> values1 = JsonPath.read(jso, String.format("$.data.mapping.[?(@.plant==%s)].region", plant));
		String erp = values.isEmpty() ? null : values.get(0);
		String region=values1.isEmpty() ? null : values1.get(0);  
		
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(
			"input.xsl"));  		transformer.transform(new javax.xml.transform.stream.StreamSource("Payload.xml"),
			new javax.xml.transform.stream.StreamResult(new FileOutputStream("Payload_XSLT_Out.xml")));
		
			
		//converting XSLT Xml and Payload xml in string	
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		InputStream xsltinputStream = new FileInputStream(new File("Payload_XSLT_Out.xml"));
		InputStream payloadinputStream = new FileInputStream(new File("Payload.xml"));
		org.w3c.dom.Document xsltdoc = documentBuilderFactory.newDocumentBuilder().parse(xsltinputStream);
		org.w3c.dom.Document payloaddoc = documentBuilderFactory.newDocumentBuilder().parse(payloadinputStream);
		StringWriter xsltXML = new StringWriter();
		StringWriter payloadXML = new StringWriter();
		Transformer serializer = TransformerFactory.newInstance().newTransformer();
		serializer.transform(new DOMSource(xsltdoc), new StreamResult(xsltXML));
		serializer.transform(new DOMSource(payloaddoc), new StreamResult(payloadXML));
	
		org.json.JSONObject payloadJsonXml = XML.toJSONObject(xsltXML.toString()); 
		org.json.JSONObject collectionPayload = (JSONObject)payloadJsonXml.get("COLLECTION");
		
		//sending to part-bom ms
		/*List<ServiceInstance> serviceInstance1 = discoveryClient.getInstances("plm-part-bom-ms");
		ServiceInstance bomInstance1 = serviceInstance1.get(0);
		String urlString1 = "http://" + bomInstance1.getHost() + ":" + Integer.toString(bomInstance1.getPort())
		+ "/receiveJson";*/
		String urlString1="http://localhost:9191/receiveJson";
        HashMap<String, Object> mvm = new HashMap<String, Object>();
        mvm.put("bom", collectionPayload.get("BOMCOMPONENTS-BOMCOMPONENTS").toString()); 
        mvm.put("part", collectionPayload.get("PARTS-PARTS").toString());
        mvm.put("erp", erp);
        mvm.put("plant", plant);
        mvm.put("region", region);
        mvm.put("ecnNo", ecnNo);
        Map result = restTemplate.postForObject( urlString1, mvm , Map.class); //one
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
		
	}
	@Override
	@HystrixCommand(fallbackMethod = "error")
	public String hystrixCircuitBreaker() {
		
		System.out.println("Inseide() call before fallBack");												

		return "Successfully execute";
	}

	public String error() {
		System.out.println("Fall-back Method is Call");
		return "Fall-back Method is Call ";

	}
	
}
