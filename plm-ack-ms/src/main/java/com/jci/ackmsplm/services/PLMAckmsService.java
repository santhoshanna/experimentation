package com.jci.ackmsplm.services;

import com.jci.ackmsplm.domain.JCIASTSampleEntity;

public interface PLMAckmsService {

	public JCIASTSampleEntity retrieveEntity(String partitionkey, String rowkey);

}
