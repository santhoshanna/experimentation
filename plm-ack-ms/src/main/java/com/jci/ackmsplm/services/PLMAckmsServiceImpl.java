package com.jci.ackmsplm.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jci.ackmsplm.dao.PLMAckmsDaoImpl;
import com.jci.ackmsplm.domain.JCIASTSampleEntity;

@Service
public class PLMAckmsServiceImpl implements PLMAckmsService {
	private static Logger logger = LoggerFactory.getLogger(PLMAckmsServiceImpl.class);
	@Autowired
	PLMAckmsDaoImpl pLMAckmsDaoImpl;

	@Override
	@Transactional
	public JCIASTSampleEntity retrieveEntity(String partitionkey, String rowKey) {
		logger.info("This is PLMAckmsServiceImpl  info message");
		return pLMAckmsDaoImpl.retrieveEntity(partitionkey, rowKey);
	}

}
