package com.jci.payloadprocess.service;

public interface PLMProcessPayloadService {
	
	public String processPayload(String completeXml,String ecnNo);

	public String hystrixCircuitBreaker();
	
	//public String processPayload(String completeXml,String ecnNo);


}
