package com.jci.subscriber.service;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.servicebus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
import com.microsoft.windowsazure.services.servicebus.ServiceBusService;
import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveQueueMessageResult;

@Service
@Configuration
public class PLMSubscriberMSServiceImpl implements PLMSubscriberMSService {

	private static final Logger LOG = LoggerFactory.getLogger(PLMSubscriberMSServiceImpl.class);

	@Value("${azure.storage.connectionstring}")
	private String connectionString;

	@Value("${azure.storage.namespace}")
	private String nameSpace;

	@Value("${azure.storage.saspolicykeyname}")
	private String sasPolicyKeyName;

	@Value("${azure.storage.saspolicykey}")
	private String sasPolicyKey;

	@Value("${azure.storage.servicebusrooturi}")
	private String serviceBusRootURI;

	@Value("${azure.storage.queuename}")
	private String queueName;

	@Value("${hashmap.key.ecnnumber}")
	private String ecnNumberKey;

	@Value("${hashmap.key.xml}")
	private String xmlKey;

	@Value("${azure.xml.payload.subribedfile.xmltag}")
	private String xmlTransactionIDTag;

	@Value("${azure.storagems.name}")
	private String storageMS;

	@Value("${azure.storagems.resource}")
	private String storageMSResource;

	@Value("${azure.payloadprocessms.name}")
	private String payloadMSProcessMS;

	@Value("${azure.payloadprocessms.resource}")
	private String payloadProcessResource;

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private DiscoveryClient discoveryClient;

	public List<ServiceInstance> serviceInstancesByApplicationName(String applicationName) {
		return this.discoveryClient.getInstances(applicationName);
	}

	public ServiceBusContract azureConnectionSetup() {
		LOG.info("###### Starting PLMSubscriberMSServiceImpl.azureConnectionSetup");
		com.microsoft.windowsazure.Configuration config = ServiceBusConfiguration
				.configureWithSASAuthentication(nameSpace, sasPolicyKeyName, sasPolicyKey, serviceBusRootURI);
		ServiceBusContract service = ServiceBusService.create(config);
		LOG.info("###### Ending PLMSubscriberMSServiceImpl.azureConnectionSetup");
		return service;
	}

	public boolean azureMessagePublisher(ServiceBusContract service, String message) {
		LOG.info("###### Starting PLMSubscriberMSServiceImpl.azureMessagePublisher");
		try {
			BrokeredMessage brokeredMessage = new BrokeredMessage(message);
			service.sendQueueMessage(queueName, brokeredMessage);
		} catch (ServiceException e) {
			LOG.error(
					"ServiceException encountered in PLMSubscriberMSServiceImpl.azureMessagePublisher while sending messages to queue: ",
					e);
			return false;
		}
		LOG.info("###### Ending PLMSubscriberMSServiceImpl.azureMessagePublisher");
		return true;
	}

	public boolean azureMessageSubscriber(ServiceBusContract service) {
		LOG.info("###### Starting PLMSubscriberMSServiceImpl.azureMessageSubscriber");
		try {

			// ReceiveMessageOptions opts = ReceiveMessageOptions.DEFAULT;
			// opts.setReceiveMode(ReceiveMode.PEEK_LOCK);
			// We are setting max size of the xml file as 64 KB
			service.getQueue(queueName).getValue().setMaxSizeInMegabytes((long) 1);
			ReceiveQueueMessageResult resultQM = service.receiveQueueMessage(queueName);
			BrokeredMessage message = resultQM.getValue();
			StreamSource source = null;
			try {
				source = new StreamSource(message.getBody());
			} catch (Exception e) {
				LOG.error("No messasge in queue in PLMSubscriberMSServiceImpl.azureMessageSubscriber", e);
				return false;
			}
			StringWriter outWriter = new StringWriter();
			StreamResult result = new StreamResult(outWriter);
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			transformer.transform(source, result);
			StringBuffer sb = outWriter.getBuffer();
			String finalstring = sb.toString();

			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource src = new InputSource();
			src.setCharacterStream(new StringReader(finalstring));

			Document doc = builder.parse(src);
			String ecnNo = doc.getElementsByTagName(xmlTransactionIDTag).item(0).getTextContent();

			try {
				// Sending the payload to Storage MS
				LOG.info("########Starting Posting messages to Storage MS block########");
				List<ServiceInstance> serviceInstanceStorageMS = discoveryClient.getInstances(storageMS);
				ServiceInstance storageMSServiceInstance = serviceInstanceStorageMS.get(0);

				HashMap<String, Object> hashMap = new HashMap<String, Object>();
				hashMap.put(xmlKey, finalstring.toString());
				hashMap.put(ecnNumberKey, ecnNo);
				restTemplate.postForObject(storageMSServiceInstance.getUri().toString() + ":"
						+ storageMSServiceInstance.getPort() + storageMSResource, hashMap, Map.class);
				LOG.info("########Ending Posting messages to Storage MS block########");
			} catch (Exception e) {
				LOG.error(
						"Exception during posting XML to storage MS in PLMSubscriberMSServiceImpl.azureMessageSubscriber",
						e);
			}

			try {
				// sending the payload to PayloadProcess MS
				LOG.info("########Starting Posting messages to PayloadProcess MS block########");
				List<ServiceInstance> serviceInstancePayloadProcessMS = discoveryClient
						.getInstances(payloadMSProcessMS);
				ServiceInstance payloadProcessMSServiceInstance = serviceInstancePayloadProcessMS.get(0);

				restTemplate.postForObject(
						payloadProcessMSServiceInstance.getUri().toString() + ":"
								+ payloadProcessMSServiceInstance.getPort() + payloadProcessResource,
						finalstring, String.class);
				LOG.info("########Ending Posting messages to PayloadProcess MS block########");
			} catch (Exception e) {
				LOG.error(
						"Exception during posting JSON to PayloadProcess MS in PLMSubscriberMSServiceImpl.azureMessageSubscriber",
						e);
			}

		} catch (Exception e) {
			LOG.error("Generic exception encountered: ", e);
			LOG.info("###### Ending PLMSubscriberMSServiceImpl.azureMessageSubscriber");
			return false;
		}
		return true;

	}

}
