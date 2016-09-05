package com.jci.subscriber;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import com.jci.subscriber.service.PLMSubscriberMSService;
import com.jci.subscriber.service.PLMSubscriberMSServiceImpl;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;


@SpringBootApplication
@RestController
@EnableEurekaClient
@EnableDiscoveryClient
public class PLMSubscriberMSApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(PLMSubscriberMSApplication.class, args);
	}

	@Autowired
	PLMSubscriberMSService azureSBCService;
	
	

	@RequestMapping("/AzureSB")
	
	public String azureSB() throws IOException, TransformerException, SAXException, ParserConfigurationException {
		ServiceBusContract service = azureSBCService.azureConnectionSetup();
		if (service != null) {
			
			
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			InputStream inputStream = new FileInputStream(
					new File("Payload.xml"));
			org.w3c.dom.Document doc = documentBuilderFactory.newDocumentBuilder().parse(inputStream);
			StringWriter stw = new StringWriter();
			Transformer serializer = TransformerFactory.newInstance().newTransformer();
			serializer.transform(new DOMSource(doc), new StreamResult(stw));
			
			if (azureSBCService.azureMessagePublisher(service, stw.toString())) 
			{
				return azureSBCService.azureMessageSubscriber(service);     
				
			} 
			else 
			{
				return "publisher did not publish message to the queue";
			}

		} else {
			System.out.println("Service object returned to controller was null");
			return "Service object returned to controller was null";
		}

	}
	
	
}
