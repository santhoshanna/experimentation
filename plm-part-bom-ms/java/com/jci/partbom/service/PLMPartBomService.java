package com.jci.partbom.service;

import java.util.HashMap;

public interface PLMPartBomService {
	
	
	boolean jsonSendToStorage(HashMap<String,Object> jsonXml);
	
	public boolean hystrixCircuitBreaker();


	
	
	
	
}
