package com.jci.partbom.service;

import java.util.HashMap;

public interface PLMPartBomService {
	
	String bomApiCallInApigee(); 
	String partApiCallInApigee();
	
	String jsonSendToStorage(HashMap<String,Object> jsonXml);
	
	public String hystrixCircuitBreaker();


	
	
	
	
}
