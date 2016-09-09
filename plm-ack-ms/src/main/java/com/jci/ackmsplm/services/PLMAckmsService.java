package com.jci.ackmsplm.services;

import com.jci.ackmsplm.domain.PLMPayloadTableEntity;

public interface PLMAckMSService {
	//public boolean hystrixCircuitBreaker();
	public PLMPayloadTableEntity retrieveAzureTableEntity(String partitionkey, String rowkey);

}
