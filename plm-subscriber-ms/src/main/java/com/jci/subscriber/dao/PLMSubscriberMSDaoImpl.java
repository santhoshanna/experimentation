package com.jci.subscriber.dao;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

@Service
@Configuration
public class PLMSubscriberMSDaoImpl implements PLMSubscriberMSDao {

	private static final Logger LOG = LoggerFactory.getLogger(PLMSubscriberMSDaoImpl.class);

	@Value("${azure.storage.connectionstring}")
	private String connectionString;

	@Value("${azure.storage.blobname}")
	private String blobName;
	
	@Value("${hashmap.key.ecnnumber}")
	private String ecnNumberKey;

	@Value("${hashmap.key.xml}")
	private String xmlKey;

	@SuppressWarnings("null")
	@Override
	public boolean insertPayloadXMLToBlob(HashMap<String, Object> xml) {
		LOG.info("#####Staring PLMSubscriberMSDaoImpl.insertPayloadXMLToBlob#####");
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer blobContainer = blobClient.getContainerReference(blobName);
			// blobContainer.createIfNotExists();
			boolean tableExistsOrNOt = true;
			if (blobContainer == null) {
				tableExistsOrNOt = blobContainer.createIfNotExists();
			}
			if (tableExistsOrNOt) {
				CloudBlockBlob blob = blobContainer.getBlockBlobReference(xml.get(ecnNumberKey).toString());
				try {
					InputStream inputStream = new ByteArrayInputStream(
							xml.get(xmlKey).toString().getBytes(StandardCharsets.UTF_8));
					blob.upload(inputStream, inputStream.available());
				} catch (Exception e) {
					LOG.error("Exception while inserting xml to blob in PLMSubscriberMSDaoImpl.insertPayloadXMLToBlob",
							e);
					LOG.info("#####Ending PLMSubscriberMSDaoImpl.insertPayloadXMLToBlob#####");
					return false;
				}
			}
		} catch (Exception e) {
			LOG.error("Exception while writing xml to blob in PLMSubscriberMSDaoImpl.insertPayloadXMLToBlob", e);
			LOG.info("#####Ending PLMSubscriberMSDaoImpl.insertPayloadXMLToBlob#####");
			return false;
		}
		LOG.info("#####Ending PLMSubscriberMSDaoImpl.insertPayloadXMLToBlob#####");
		return true;
	}

}
