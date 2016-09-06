package com.jci.storage.service;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.jci.storage.dao.PLMStorageDao;
import com.microsoft.azure.storage.StorageException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@Service
public class PLMStorageServiceImpl implements PLMStorageService {

	private static final Logger LOG = LoggerFactory.getLogger(PLMStorageServiceImpl.class);

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	PLMStorageDao plmStorageDao;

	public boolean insertPayloadXMLToBlob(HashMap<String, Object> xml) {
		return plmStorageDao.insertPayloadXMLToBlob(xml);
	}

	@Override
	@HystrixCommand(fallbackMethod = "error")
	public boolean hystrixCircuitBreaker() {
		LOG.info("#####Starting PLMStorageServiceImpl.hystrixCircuitBreaker#####");
		LOG.info("#####Ending PLMStorageServiceImpl.hystrixCircuitBreaker#####");
		return true;
	}

	public void error() {
		LOG.info("#####Starting PLMStorageServiceImpl.error fallback#####");
	}

	@Override
	public boolean insertPayloadJSONToTable(HashMap<String, Object> map)
			throws InvalidKeyException, URISyntaxException, StorageException {
		return plmStorageDao.insertPayloadJSONToTable(map);
	}

}
