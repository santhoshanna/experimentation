package com.jci.ackmsplm.dao;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.jci.ackmsplm.domain.PLMPayloadTableEntity;
import com.jci.ackmsplm.services.PLMAckMSServiceImpl;
import com.microsoft.windowsazure.services.blob.client.CloudBlobContainer;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.table.client.CloudTable;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.microsoft.windowsazure.services.table.client.TableConstants;
import com.microsoft.windowsazure.services.table.client.TableOperation;
import com.microsoft.windowsazure.services.table.client.TableQuery;
import com.microsoft.windowsazure.services.table.client.TableQuery.Operators;
import com.microsoft.windowsazure.services.table.client.TableQuery.QueryComparisons;

@Repository
public class PLMAckMSDaoImpl implements PLMAckMSDao {

	private static final Logger LOG = LoggerFactory.getLogger(PLMAckMSServiceImpl.class);

	CloudTableClient tableClient = null;
	final String ACK_KEY = "Acknowledged";
	
	public CloudTableClient getTableClientReference()
			throws RuntimeException, IOException, IllegalArgumentException, URISyntaxException, InvalidKeyException {
		LOG.info("#####Starting PLMAckMSDaoImpl.getTableClientReference #####");
		Properties prop = new Properties();
		try {

			InputStream propertyStream = PLMAckMSDaoImpl.class.getClassLoader()
					.getResourceAsStream("application.properties");
			if (propertyStream != null) {
				prop.load(propertyStream);

			} else {
				throw new RuntimeException();
			}
		} catch (RuntimeException | IOException e) {
			throw e;
		}

		CloudStorageAccount storageAccount;
		try {
			storageAccount = CloudStorageAccount.parse(prop.getProperty("azureStorageTableConnectionString"));
		} catch (IllegalArgumentException | URISyntaxException e) {
			throw e;
		} catch (InvalidKeyException e) {
			LOG.info("#####Connection string specifies an invalid key #####");
			throw e;
		}
		LOG.info("#####Ending PLMAckMSDaoImpl.getTableClientReference #####");
		return storageAccount.createCloudTableClient();

	}

	@SuppressWarnings("null")
	public boolean createAzureTableIfNotExists(CloudTableClient tableClient, String azureStorageTableName) {
		LOG.info("#####Starting PLMAckMSDaoImpl.createAzureTableIfNotExists #####");
		CloudTable table = null;
		try {
			table = tableClient.getTableReference(azureStorageTableName);
		} catch (URISyntaxException | StorageException e) {
			e.printStackTrace();
		}
		if (table == null) {
			try {
				table.createIfNotExist();
				return true;
			} catch (StorageException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			LOG.info("Table already exists");
		}

		LOG.info("#####Ending PLMAckMSDaoImpl.createAzureTableIfNotExists #####");

		return true;

	}

	public boolean readAzureTableEntityList(CloudTableClient tableClient, String tableName) {
		LOG.info("#####Starting PLMAckMSDaoImpl.readAzureTableEntityList #####");

		try {
			String partitionFilter = TableQuery.generateFilterCondition(TableConstants.PARTITION_KEY,
					QueryComparisons.EQUAL, "SYMIX_PLM");
			String ackFilter = TableQuery.generateFilterCondition(ACK_KEY, QueryComparisons.EQUAL, "false");
			String filter = TableQuery.combineFilters(partitionFilter, Operators.AND, ackFilter);
			TableQuery<PLMPayloadTableEntity> query = TableQuery.from(tableName, PLMPayloadTableEntity.class).where(filter);

			for (PLMPayloadTableEntity plmEntity : tableClient.execute(query)) {

				int isprocessed = plmEntity.getIsProcessed();
				int iserrored = plmEntity.getIsProcessed();
				if (isprocessed == 1 && iserrored ==1) {
					System.out.println("..Successfully Processed..");
					LOG.info("READING ENTITY SUCCESSFULLY PROCESSED");
				} else if (isprocessed == 1 && iserrored == 1) {
					LOG.info("READING ENTITY SUCCESSFULLY NOT PROCESSED");
				} else if (isprocessed == 0 && iserrored == 1) {
					LOG.info("READING ENTITY SUCCESSFULLY NOT PROCESSED");

				} else if (isprocessed == 0 && iserrored == 0) {
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		LOG.info("#####Ending PLMAckMSDaoImpl.readAzureTableEntityList #####");
		return true;

	}


	@SuppressWarnings("unused")
	private PLMPayloadTableEntity retrieveAzureTableEntity(CloudTableClient tableClient, String tableName,
			String patritionkey, String rowKey) {
		LOG.info("#####Starting PLMAckMSDaoImpl.retrieveAzureTableEntity #####");

		TableOperation findSampleEntity = TableOperation.retrieve(patritionkey, rowKey, PLMPayloadTableEntity.class);
		
		PLMPayloadTableEntity sampleEntity = null;

		try {
			sampleEntity = tableClient.execute(tableName, findSampleEntity).getResultAsType();

		if (sampleEntity != null) {
			}
		} catch (StorageException e) {
			e.printStackTrace();
		}
		LOG.info("#####Ending PLMAckMSDaoImpl.retrieveAzureTableEntity #####");
		return sampleEntity;
	}

	



	@Override
	public PLMPayloadTableEntity retrieveAzureTableEntity(String partitionkey, String rowKey) {

		LOG.info("#####Starting PLMAckMSDaoImpl.retrieveAzureTableEntity #####");

		PLMPayloadTableEntity sampleEntity = null;
		CloudTableClient tableClient = null;
		try {
			tableClient = getTableClientReference();

			if (tableClient != null) {

				boolean createTable = createAzureTableIfNotExists(tableClient, "controlsplmpayloadtable");
				if (createTable) {

					readAzureTableEntityList(tableClient, "controlsplmpayloadtable");
				} else {

				}
				sampleEntity = retrieveAzureTableEntity(tableClient, "controlsplmpayloadtable", partitionkey, rowKey);
			}

		}

		catch (InvalidKeyException | RuntimeException | URISyntaxException | IOException e) {

			e.printStackTrace();
		}
		LOG.info("#####Ending PLMAckMSDaoImpl.retrieveAzureTableEntity #####");

		return sampleEntity;

	}
}
