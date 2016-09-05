package com.jci.portal;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.jci.portal.domain.req.PlmDetailsRequest;
import com.jci.portal.domain.req.SegmentedDetailRequest;
import com.jci.portal.domain.res.SegmentedDetailResponse;
import com.jci.portal.service.PLMWebPortalGraphService;
import com.jci.portal.service.PLMWebPortalReprocessService;
import com.jci.portal.service.PLMWebPortalService;
import com.jci.portal.utils.AzureUtils;
import com.jci.portal.utils.Constants;

@EnableDiscoveryClient
@RestController
@SpringBootApplication
@ComponentScan("com.jci.portal")
public class PLMWebPortalApplication {
	private static final Logger LOG = LoggerFactory.getLogger(PLMWebPortalApplication.class);

	public static void main(String[] args) {

		SpringApplication.run(PLMWebPortalApplication.class, args);
	}

	@Autowired
	PLMWebPortalService service;
	@Autowired
	PLMWebPortalGraphService graphService;
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Autowired
	RestTemplate restTemplate;
	

	@RequestMapping(value = "/getSegmentedPlmDetails", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public SegmentedDetailResponse getSegmentedPlmDetails(@RequestBody SegmentedDetailRequest request)
			throws com.microsoft.azure.storage.StorageException {
		LOG.info("### Starting PLMWebPortalApplication.getSegmentedPlmDetails ###" + request);

		SegmentedDetailResponse response = new SegmentedDetailResponse();
		request.setPartition(AzureUtils.getPartitionKey(request.getErpName().toUpperCase()));
		request.setTableName(Constants.TABLE_PLM_DETAILS);

		try {
			response = service.getSegmentedResultSetData(request);

		} catch (InvalidKeyException | URISyntaxException
				| com.microsoft.windowsazure.services.core.storage.StorageException e) {
			response.setError(true);
			response.setMessage(e.getMessage());
			e.printStackTrace();
		}

		LOG.info("### Ending PLMWebPortalApplication.getSegmentedPlmDetails ###");
		return response;
	}

	@RequestMapping(value = "/getSegmentedErrorDetails", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public SegmentedDetailResponse getErrorDetails(@RequestBody SegmentedDetailRequest request) {
		LOG.info("### Starting PLMWebPortalApplication.getSegmentedErrorDetails ###" + request);

		SegmentedDetailResponse response = new SegmentedDetailResponse();
		request.setPartition(AzureUtils.getPartitionKey(request.getErpName().toUpperCase()));
		request.setTableName(Constants.TABLE_PLM_DETAILS);

		try {
			response = service.getErrorResultSetData(request);
		} catch (InvalidKeyException | URISyntaxException | com.microsoft.azure.storage.StorageException e) {
			response.setError(true);
			response.setMessage(e.getMessage());
			e.printStackTrace();
		}

		LOG.info("### Ending PLMWebPortalApplication.getSegmentedErrorDetails ###");
		return response;
	}

	@RequestMapping("/GraphDataStorage")
	public String setData() {
		LOG.info("### Starting PLMWebPortalApplication.setData ###");
		graphService.insertData();
		LOG.info("### Ending PLMWebPortalApplication.setData ###");
		return "successfully  data inserted";

	}

	@Autowired
	PLMWebPortalReprocessService repService;

	@RequestMapping(value = "/processErrorPos", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String payloadProcess(@RequestBody final PlmDetailsRequest request) throws Exception {
		LOG.info("### Starting PLMWebPortalApplication.sendToSubscriber ###" + request);

		HashMap<String, Object> serviceMap = repService.errorProcess(request);
		Object ecnNumber = serviceMap.get("EcnNo");
//		Object ecnNumber=request.getEcnNumber();
		Object completeXml = serviceMap.get("CompleteXml");
		Object uiprocessed=serviceMap.get("UIprocessed");
		Object uiprocessedby=serviceMap.get("UIprocessedby");
		Object uiprocessdate=serviceMap.get("UIprocessedDate");
	
		
		HashMap<String, String> map = new HashMap<>();
		map.put("EcnNo", ecnNumber.toString());
		map.put("CompleteXml", completeXml.toString());
		map.put("UIprocessed", uiprocessed.toString());
		map.put("UIprocessedby",uiprocessedby.toString() );
		map.put("UIprocessedDate", uiprocessdate.toString());
		System.out.println("map is "+map);
		
		String urlString="http://localhost:9090/reprocess";
		Map result=restTemplate.postForObject( urlString, map , Map.class);
		LOG.info("### Ending PLMWebPortalApplication.sendToSubscriber ###");
		return "Done ";

	}
}
