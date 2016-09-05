package com.jci.portal.dao;

import com.jci.portal.domain.MiscDataEntity;

public interface PLMWebPortalGraphDao {

	public MiscDataEntity getTotalProcessedEntities();

	public String insertData();

}
