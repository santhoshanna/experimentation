package com.jci.payloadprocess.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringWriter;
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
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.JsonPath;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@Service
@Configuration
public class PLMProcessPayloadServiceImpl implements PLMProcessPayloadService {

	private static final Logger LOG = LoggerFactory.getLogger(PLMProcessPayloadServiceImpl.class);

	@Value("${xml.payload.filename}")
	private String xmlPayloadFileName;

	@Value("${xml.output.filename}")
	private String xmlOutputFileName;

	@Value("${xml.output.xmltags.collection}")
	private String xmltagsCollection;

	@Value("${xml.output.xmltags.bomcomponents}")
	private String xmltagsBOMComponents;

	@Value("${xml.output.xmltags.partcomponents}")
	private String xmltagsPartComponents;

	@Value("${xsl.output.filename}")
	private String xslOutputFileName;

	@Value("${json.input.filename}")
	private String jsonInputFileName;

	@Value("${json.input.jsonpath.erp}")
	private String jsonpathERP;

	@Value("${json.input.jsonpath.region}")
	private String jsonpathRegion;

	@Value("${partbomms.url.parameter.bom}")
	private String urlparamBOM;

	@Value("${partbomms.url.parameter.part}")
	private String urlparamPart;

	@Value("${partbomms.url.parameter.erp}")
	private String urlparamERP;

	@Value("${partbomms.url.parameter.plant}")
	private String urlparamPlant;

	@Value("${partbomms.url.parameter.region}")
	private String urlparamRegion;

	@Value("${partbomms.url.parameter.ecnno}")
	private String urlparamECNNo;

	@Value("${partbomms.url.parameter.transactionid}")
	private String urlparamTransactionID;
	
	@Value("${apigatewayms.name}")
	private String apigatewaymsName;

	@Value("${plmpartbomms.resource}")
	private String plmpartbommsResource;

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
		LOG.info("#####Starting PLMProcessPayloadServiceImpl.serviceInstancesByApplicationName#####");
		LOG.info("#####Ending PLMProcessPayloadServiceImpl.serviceInstancesByApplicationName#####");
		return this.discoveryClient.getInstances(applicationName);
	}

	@Override
	public boolean processPayload(String completeXml, String ecnNo, String transactionId, String plant) {
		LOG.info("#####Starting PLMProcessPayloadServiceImpl.processPayload#####");
		try {
			List<ServiceInstance> apigatewaymsInstanceList = discoveryClient.getInstances(apigatewaymsName);
			ServiceInstance apigatewaymsInstance = apigatewaymsInstanceList.get(0);
			LOG.info("processpayload() is executed . . . . . . .");
			LOG.info("value of ecnNo is    " + ecnNo);
			LOG.info("value of transactionId is    " + transactionId);
			LOG.info("value of plant is    " + plant);
			File file = new File(xmlPayloadFileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(completeXml);
			bw.close();

			JSONParser jp = new JSONParser();
			Object object = jp.parse(new FileReader(jsonInputFileName));
//			JSONObject jso = (JSONObject) object; // now all object are of type Simple Json
			org.json.simple.JSONObject jso = (org.json.simple.JSONObject) object; //add by anand

			List<String> values = JsonPath.read(jso, String.format(jsonpathERP, plant));
			List<String> values1 = JsonPath.read(jso, String.format(jsonpathRegion, plant));
			String erp = values.isEmpty() ? null : values.get(0);
			String region = values1.isEmpty() ? null : values1.get(0);

			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory
					.newTransformer(new javax.xml.transform.stream.StreamSource(xslOutputFileName));
			transformer.transform(new javax.xml.transform.stream.StreamSource(xmlPayloadFileName),
					new javax.xml.transform.stream.StreamResult(new FileOutputStream(xmlOutputFileName)));

			// converting XSLT Xml and Payload xml in string
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			InputStream xsltinputStream = new FileInputStream(new File(xmlOutputFileName));
			InputStream payloadinputStream = new FileInputStream(new File(xmlPayloadFileName));
			org.w3c.dom.Document xsltdoc = documentBuilderFactory.newDocumentBuilder().parse(xsltinputStream);
			org.w3c.dom.Document payloaddoc = documentBuilderFactory.newDocumentBuilder().parse(payloadinputStream);
			StringWriter xsltXML = new StringWriter();
			StringWriter payloadXML = new StringWriter();
			Transformer serializer = TransformerFactory.newInstance().newTransformer();
			serializer.transform(new DOMSource(xsltdoc), new StreamResult(xsltXML));
			serializer.transform(new DOMSource(payloaddoc), new StreamResult(payloadXML));

			JSONObject payloadJsonXml = XML.toJSONObject(xsltXML.toString());
			JSONObject collectionPayload = (JSONObject) payloadJsonXml.get(xmltagsCollection);

			// sending to part-bom ms
			// String urlString1 = "http://localhost:9191/receiveJson";
			HashMap<String, Object> mvm = new HashMap<String, Object>();
			mvm.put(urlparamBOM, collectionPayload.get(xmltagsBOMComponents).toString());
			mvm.put(urlparamPart, collectionPayload.get(xmltagsPartComponents).toString());
			mvm.put(urlparamERP, erp);
			mvm.put(urlparamPlant, plant);
			mvm.put(urlparamRegion, region);
			mvm.put(urlparamECNNo, ecnNo);
			mvm.put(urlparamTransactionID, transactionId);

			restTemplate.postForObject(
					apigatewaymsInstance.getUri().toString() + plmpartbommsResource, mvm,
					Map.class);
		} catch (Exception e) {
			LOG.error("Exception in PLMProcessPayloadServiceImpl.processPayload", e);
			return false;
		}
		LOG.info("#####Ending PLMProcessPayloadServiceImpl.processPayload#####");
		return true;
	}

	@Override
	@HystrixCommand(fallbackMethod = "error")
	public boolean hystrixCircuitBreaker() {
		LOG.info("#####Starting PLMProcessPayloadServiceImpl.hystrixCircuitBreaker#####");
		LOG.info("#####Ending PLMProcessPayloadServiceImpl.hystrixCircuitBreaker#####");
		return true;
	}

	public boolean error() {
		LOG.info("#####Starting PLMProcessPayloadServiceImpl.error#####");
		LOG.info("#####Ending PLMProcessPayloadServiceImpl.error#####");
		return true;
	}

}
