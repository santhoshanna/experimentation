package com.jci.subscriber;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import com.jci.subscriber.service.PLMSubscriberMSService;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;

@SpringBootApplication
@RestController
@EnableEurekaClient
@Configuration
public class PLMSubscriberMSApplication {

	public static void main(String[] args) {
		SpringApplication.run(PLMSubscriberMSApplication.class, args);
	}

	private static final Logger LOG = LoggerFactory.getLogger(PLMSubscriberMSApplication.class);

	@Value("${azure.xml.payload.inputfile}")
	private String inputFile;

	@Autowired
	PLMSubscriberMSService plmAzureSBCService;

	@Autowired
	private DiscoveryClient discoveryClient;

	@RequestMapping("/service-instances/{applicationName}")
	public List<ServiceInstance> serviceInstancesByApplicationName(@PathVariable String applicationName) {
		return this.discoveryClient.getInstances(applicationName);
	}

	@RequestMapping("/azureSBXMLPost")
	public @ResponseBody String azureSBXMLPost()
			throws IOException, TransformerException, SAXException, ParserConfigurationException {
		LOG.info("###### Starting PLMSubscriberMSApplication.PLMSubscriberMSApplication");
		ServiceBusContract service = plmAzureSBCService.azureConnectionSetup();
		if (service != null) {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			InputStream inputStream = new FileInputStream(new File(inputFile));
			org.w3c.dom.Document doc = documentBuilderFactory.newDocumentBuilder().parse(inputStream);
			StringWriter stw = new StringWriter();
			Transformer serializer = TransformerFactory.newInstance().newTransformer();
			serializer.transform(new DOMSource(doc), new StreamResult(stw));
			if (plmAzureSBCService.azureMessagePublisher(service, stw.toString())) {
				if (plmAzureSBCService.azureMessageSubscriber(service)) {
					return "success";
				} else {
					return "false";
				}
			} else {
				return "publisher did not publish message to the queue in PLMSubscriberMSApplication.PLMSubscriberMSApplication";
			}
		} else {
			LOG.error(
					"Service object returned to controller was null in PLMSubscriberMSApplication.PLMSubscriberMSApplication. Please check connection properties in application.properties file");
			LOG.info("###### Ending PLMSubscriberMSApplication.PLMSubscriberMSApplication");
			return "Service object returned to controller was null";
		}
	}

//	@Scheduled(fixedDelayString = "${azure.servicebus.scheduler}")
	@RequestMapping("/getXML")
	public @ResponseBody void getXML()
			throws IOException, TransformerException, SAXException, ParserConfigurationException {
		LOG.info("###### Starting PLMSubscriberMSApplication.getXML");
		ServiceBusContract service = plmAzureSBCService.azureConnectionSetup();
		if (service != null) {
			plmAzureSBCService.azureMessageSubscriber(service);
		} else {
			LOG.error(
					"Service object returned to controller was null in PLMSubscriberMSApplication.getXML. Please check connection properties in application.properties file");
			LOG.info("###### Ending PLMSubscriberMSApplication.getXML");
		}
	}

}
