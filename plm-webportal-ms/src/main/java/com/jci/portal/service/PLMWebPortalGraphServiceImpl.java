package com.jci.portal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jci.portal.dao.PLMWebPortalGraphDao;
import com.jci.portal.domain.MiscDataEntity;

@Service
@Transactional
@ComponentScan("com.jci.portal")
public class PLMWebPortalGraphServiceImpl implements PLMWebPortalGraphService {
	private static final Logger logger = LoggerFactory.getLogger(PLMWebPortalGraphServiceImpl.class);
	@Autowired
	PLMWebPortalGraphDao dao;

	@Override
	public MiscDataEntity getTotalProcessedEntities() {
		logger.info("### Starting PLMWebPortalGraphServiceImpl.getTotalProcessedEntities ###");
		return dao.getTotalProcessedEntities();

	}
	/*
	 * @Override public String setEntity() { dao.setEntity(); return "done"; }
	 */

	@Override
	public String insertData() {
		logger.info("### Starting PLMWebPortalGraphServiceImpl.insertData ###");
		return dao.insertData();
	}

}