package com.jci.ackmsplm.dao;

import com.jci.ackmsplm.domain.PLMPayloadTableEntity;

public interface PLMAckMSDao {

	public PLMPayloadTableEntity retrieveAzureTableEntity(String partitionkey, String rowKey);
	

}
