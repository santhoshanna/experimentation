package com.jci.partbom.service;

import java.util.HashMap;

public interface PLMPartBomService {

	public boolean jsonSendToStorageMS(HashMap<String, Object> jsonXml);

	public boolean hystrixCircuitBreaker();

}
