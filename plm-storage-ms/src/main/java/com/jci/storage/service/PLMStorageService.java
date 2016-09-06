package com.jci.storage.service;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.HashMap;

import com.microsoft.azure.storage.StorageException;

public interface PLMStorageService {

	public boolean insertPayloadXMLToBlob(HashMap<String, Object> map);

	public boolean hystrixCircuitBreaker();

	public boolean insertPayloadJSONToTable(HashMap<String, Object> map)
			throws InvalidKeyException, URISyntaxException, StorageException;
}
