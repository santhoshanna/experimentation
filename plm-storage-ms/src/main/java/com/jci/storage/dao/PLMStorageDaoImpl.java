package com.jci.storage.dao;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;

import com.jci.storage.domain.PLMPayloadTableEntity;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableEntity;
import com.microsoft.azure.storage.table.TableOperation;

@Repository
@Configuration
public class PLMStorageDaoImpl implements PLMStorageDao {

	private static final Logger LOG = LoggerFactory.getLogger(PLMStorageDaoImpl.class);

	@Value("${azure.storage.connectionstring}")
	private String connectionString;

	@Value("${azure.storage.blobname}")
	private String blobName;

	@Value("${azure.storage.plmpayloadtablename}")
	private String plmPayloadTableName;

	@Value("${azure.storage.partionkey.plmpayload}")
	private String plmPayloadPartitionKey;

	@Value("${hashmap.key.ecnnumber}")
	private String ecnNumberKey;

	@Value("${hashmap.key.xml}")
	private String xmlKey;

	@Value("${hashmap.key.erp}")
	private String erpKey;

	@Value("${hashmap.key.region}")
	private String regionKey;

	@Value("${hashmap.key.plant}")
	private String plantKey;

	@Value("${hashmap.key.transactionid}")
	private String transactionIdKey;

	@Value("${hashmap.key.isprocessed}")
	private String isprocessedKey;

	@Value("${hashmap.key.iserrored}")
	private String iserroredKey;

	@Value("${hashmap.key.message}")
	private String messageKey;

	@Value("${hashmap.key.code}")
	private String codeKey;

	@Value("${hashmap.key.status}")
	private String statusKey;

	@Value("${hashmap.key.processeddate}")
	private String processedDateKey;

	@Value("${hashmap.key.createddate}")
	private String createdDateKey;

	@Value("${hashmap.key.processedby}")
	private String processedByKey;

	@SuppressWarnings("null")
	@Override
	public boolean insertPayloadXMLToBlob(HashMap<String, Object> xml) {
		LOG.info("#####Staring PLMStorageDaoImpl.insertPayloadXMLToBlob#####");
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer blobContainer = blobClient.getContainerReference(blobName);
			//blobContainer.createIfNotExists();
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
					LOG.error("Exception while inserting xml to blob in PLMStorageDaoImpl.insertPayloadXMLToBlob", e);
					LOG.info("#####Ending PLMStorageDaoImpl.insertPayloadXMLToBlob#####");
					return false;
				}
			}
		} catch (Exception e) {
			LOG.error("Exception while writing xml to blob in PLMStorageDaoImpl.insertPayloadXMLToBlob", e);
			LOG.info("#####Ending PLMStorageDaoImpl.insertPayloadXMLToBlob#####");
			return false;
		}
		LOG.info("#####Ending PLMStorageDaoImpl.insertPayloadXMLToBlob#####");
		return true;
	}

	@SuppressWarnings("null")
	public boolean insertPayloadJSONToTable(HashMap<String, Object> map)
			throws InvalidKeyException, URISyntaxException, StorageException {
		LOG.info("#####Staring PLMStorageDaoImpl.insertPayloadJSONToTable#####");
		CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);
		CloudTableClient tableClient = storageAccount.createCloudTableClient();
		CloudTable cloudTable = tableClient.getTableReference(plmPayloadTableName);
		cloudTable.createIfNotExists();
		boolean tableExistsOrNOt = true;
		if (cloudTable == null) {
			tableExistsOrNOt = cloudTable.createIfNotExists();
		}
		if (tableExistsOrNOt) {
			PLMPayloadTableEntity plmPayloadTableEntity = new PLMPayloadTableEntity(
					plmPayloadPartitionKey + "_" + map.get(erpKey), map.get(ecnNumberKey).toString());
			plmPayloadTableEntity.setECNNumber(map.get(ecnNumberKey).toString());
			plmPayloadTableEntity.setTransactionID(map.get(transactionIdKey).toString());
			plmPayloadTableEntity.setErp(map.get(erpKey).toString());
			plmPayloadTableEntity.setRegion(map.get(regionKey).toString());
			plmPayloadTableEntity.setPlant(map.get(plantKey).toString());
			plmPayloadTableEntity.setIsProcessed(Integer.parseInt(map.get(isprocessedKey).toString()));
			plmPayloadTableEntity.setIsErrored(Integer.parseInt(map.get(iserroredKey).toString()));
			plmPayloadTableEntity.setMessage(map.get(messageKey).toString());
			plmPayloadTableEntity.setCode(Integer.parseInt(map.get(codeKey).toString()));
			plmPayloadTableEntity.setStatus(map.get(statusKey).toString());
			plmPayloadTableEntity.setProcessedDate(map.get(processedDateKey).toString());
			plmPayloadTableEntity.setCreatedDate(map.get(createdDateKey).toString());
			plmPayloadTableEntity.setProcessedBy(map.get(processedByKey).toString());
			TableOperation insert = TableOperation.insertOrReplace((TableEntity) plmPayloadTableEntity);
			try {
				cloudTable.execute(insert);
			} catch (Exception e) {
				LOG.error(
						"Exception while inserting payload json into azure storage tables in PLMStorageDaoImpl.insertPayloadJSONToTable");
				LOG.info("#####Ending PLMStorageDaoImpl.insertPayloadJSONToTable#####");
				return false;
			}
		}
		LOG.info("#####Ending PLMStorageDaoImpl.insertPayloadJSONToTable#####");
		return true;
		// plmPayloadTableEntity.setIsProcessed(1);
		// plmPayloadTableEntity.setIsErrored(null);
		// plmPayloadTableEntity.setMessage(null);
		// plmPayloadTableEntity.setCode(null);//
		// plmPayloadTableEntity.setStatus(null);
		// plmPayloadTableEntity.setProcessedDate(map.get("processdate").toString());
		// plmPayloadTableEntity.setProcessedBy("system");
		// plmPayloadTableEntity.setIsAcknowledged(0);
		// plmPayloadTableEntity.setAcknowledgementStatus(map.get("message").toString());
		// plmPayloadTableEntity.setAcknowledgementCode(null);
		// plmPayloadTableEntity.setAcknowledgementMessage("");
		// plmPayloadTableEntity.setAcknowledgementDate(map.get("message").toString());
		// plmPayloadTableEntity.setUIProcessed(0);
		// plmPayloadTableEntity.setUIProcessedBy(null);
		// plmPayloadTableEntity.setUIProcessedDate(null);

	}
}
