package com.jci.subscriber.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.serviceBus.models.ReceiveMode;
import com.microsoft.windowsazure.services.servicebus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
import com.microsoft.windowsazure.services.servicebus.ServiceBusService;
import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveQueueMessageResult;

@Service
public class PLMSubscriberMSServiceImpl implements PLMSubscriberMSService {


	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Autowired
	RestTemplate restTemplate;

	private static String namespace = "";
	private static String sasPolicyKeyName = "";
	private static String sasPolicyKey = "";
	private static String serviceBusRootUri = "";
	private static String queueName = "";
	public static String completeXml = "";
	public static String payLoadType = "";
	public static String ecnNo="";
	public static int reProcess;

	public ServiceBusContract azureConnectionSetup() {
		Properties prop = new Properties();
		try {
			InputStream propertyStream = PLMSubscriberMSServiceImpl.class.getClassLoader()
					.getResourceAsStream("config.properties");
			if (propertyStream != null) {
				prop.load(propertyStream);
			} else {
				throw new RuntimeException();
			}
		} catch (IOException e) {
			System.out.println("\nFailed to load config.properties file.");
		} catch (RuntimeException e) {
			System.out.println("\nFailed to load config.properties file.");
			throw e;
		}
		namespace = prop.getProperty("namespace") == null ? "sbplmdev" : prop.getProperty("namespace");
		sasPolicyKeyName = prop.getProperty("sasPolicyKeyName") == null ? "RootManageSharedAccessKey"
				: prop.getProperty("sasPolicyKeyName");
		sasPolicyKey = prop.getProperty("sasPolicyKey") == null ? "Y3X4ESQe5yP6ZOmz7zB3rbTgRUSEPlvRyLy9LgTbGls="
				: prop.getProperty("sasPolicyKey");
		serviceBusRootUri = prop.getProperty("serviceBusRootUri") == null ? ".servicebus.windows.net"
				: prop.getProperty("serviceBusRootUri");
		queueName = prop.getProperty("queueName") == null ? "sbqplmdev" : prop.getProperty("queueName");
		Configuration config = ServiceBusConfiguration.configureWithSASAuthentication(namespace, sasPolicyKeyName,
				sasPolicyKey, serviceBusRootUri);
		ServiceBusContract service = ServiceBusService.create(config);
		return service;
	}

	public boolean azureMessagePublisher(ServiceBusContract service, String message) {

		try {
			BrokeredMessage brokeredMessage = new BrokeredMessage(message);
			service.sendQueueMessage(queueName, brokeredMessage);
		} catch (ServiceException e) {
			System.out.print("ServiceException encountered: ");
			System.out.println(e.getMessage());
			System.exit(-1);
		}
		return true;

	}
	
	

	public String azureMessageSubscriber(ServiceBusContract service) {

		try {

			ReceiveMessageOptions opts = ReceiveMessageOptions.DEFAULT;
		//	opts.setReceiveMode(ReceiveMode.PEEK_LOCK);
			service.getQueue(queueName).getValue().setMaxSizeInMegabytes((long) 1); 
			while (true) {
				ReceiveQueueMessageResult resultQM = service.receiveQueueMessage(queueName);
				BrokeredMessage message = resultQM.getValue();
				
				service.getQueue(queueName).getValue().setMaxSizeInMegabytes((long) 1); 
				StreamSource source = new StreamSource(message.getBody()) ;
				
					StringWriter outWriter = new StringWriter();
					StreamResult result = new StreamResult( outWriter );
					TransformerFactory tFactory = TransformerFactory.newInstance();
					Transformer transformer = tFactory.newTransformer();
					transformer.transform( source, result );  
					StringBuffer sb = outWriter.getBuffer(); 
					String finalstring = sb.toString();
					
					DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					InputSource src = new InputSource();
					src.setCharacterStream(new StringReader(finalstring));

					Document doc = builder.parse(src);
					String ecnNo = doc.getElementsByTagName("TransactionNumber").item(0).getTextContent();
					System.out.println("Ecno No find out at subscriber "+ ecnNo);
									
					//Sending the payload to Storage MS
					String storagePayload="http://localhost:9292/receiveXml";
					HashMap<String, Object> hashMap = new HashMap<String, Object>();
					hashMap.put("xml", finalstring.toString());
					hashMap.put("ecnNo",ecnNo);
					Map storage = restTemplate.postForObject( storagePayload,hashMap , Map.class);
                   
					//sending the payload to Payload Process MS				
					System.out.println(finalstring);
					String payloadProcess="http://localhost:9090/receiveXml";
					
					restTemplate.postForObject( payloadProcess, finalstring , String.class);
					System.out.println("Custom Property: " + message.getProperty("MyProperty"));

		} 
		}
			catch (Exception e) {
			e.printStackTrace();
			System.out.print("Generic exception encountered: ");
			System.out.println(e.getMessage());
			return "Transaction Complete";
		}

	}
	
	

}

