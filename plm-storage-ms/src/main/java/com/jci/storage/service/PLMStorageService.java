package com.jci.storage.service;

import java.util.HashMap;

public interface PLMStorageService {

	String PutXmlPartBom(HashMap<String, Object> mvm);  //xml from Subscriber
	
//	String PutJsonBom(String json);  //json from bom

	String hystrixCircuitBreaker();
}
