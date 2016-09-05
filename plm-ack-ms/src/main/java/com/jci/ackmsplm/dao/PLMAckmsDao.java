package com.jci.ackmsplm.dao;

import com.jci.ackmsplm.domain.JCIASTSampleEntity;

public interface PLMAckmsDao {

	public JCIASTSampleEntity retrieveEntity(String partitionkey, String rowKey);

}
