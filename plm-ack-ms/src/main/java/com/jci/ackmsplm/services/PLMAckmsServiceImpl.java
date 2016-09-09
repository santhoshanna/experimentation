package com.jci.ackmsplm.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jci.ackmsplm.dao.PLMAckMSDao;
import com.jci.ackmsplm.dao.PLMAckMSDaoImpl;
import com.jci.ackmsplm.domain.PLMPayloadTableEntity;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@Service
public class PLMAckMSServiceImpl implements PLMAckMSService {
	
	private static final Logger LOG = LoggerFactory.getLogger(PLMAckMSServiceImpl.class);

	
	@Autowired
	PLMAckMSDao plmackDao;

	@Override
	@Transactional
	public PLMPayloadTableEntity retrieveAzureTableEntity(String partitionkey, String rowKey) {
		LOG.info("#####Starting PLMAckMSServiceImpl.getAzureStorageEntity #####");
		
		LOG.info("#####Ending PLMAckMSServiceImpl.retrieveAzureTableEntity #####");
		return plmackDao.retrieveAzureTableEntity(partitionkey, rowKey);
	}
}
