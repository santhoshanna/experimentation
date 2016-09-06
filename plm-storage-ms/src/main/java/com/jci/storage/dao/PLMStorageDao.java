package com.jci.storage.dao;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.HashMap;

import com.microsoft.azure.storage.StorageException;

public interface PLMStorageDao {

	public boolean insertPayloadXMLToBlob(HashMap<String, Object> map);

	public boolean insertPayloadJSONToTable(HashMap<String, Object> map)
			throws InvalidKeyException, URISyntaxException, StorageException;

}
