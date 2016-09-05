package com.jci.ackmsplm.dao;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.jci.ackmsplm.domain.JCIASTSampleEntity;
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
public class PLMAckmsDaoImpl implements PLMAckmsDao {

	private static final Logger logger = LoggerFactory.getLogger(PLMAckmsDaoImpl.class);
	CloudBlobContainer blobContainer = null;
	CloudTableClient tableClient = null;
	final String ACK_KEY = "PtcAck";

	/**
	 * Validates the connection string and returns the storage table client. The
	 * connection string must be in the Azure connection string format.
	 *
	 * @return The newly created CloudTableClient object
	 *
	 * @throws RuntimeException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws IllegalArgumentException
	 * @throws InvalidKeyException
	 */
	public CloudTableClient getTableClientReference()
			throws RuntimeException, IOException, IllegalArgumentException, URISyntaxException, InvalidKeyException {

		// Retrieve the connection string
		Properties prop = new Properties();
		try {

			InputStream propertyStream = PLMAckmsDaoImpl.class.getClassLoader()
					.getResourceAsStream("config.properties");
			if (propertyStream != null) {
				prop.load(propertyStream);

			} else {
				throw new RuntimeException();
			}
		} catch (RuntimeException | IOException e) {
			System.out.println("\nFailed to load config.properties file.");
			throw e;
		}

		CloudStorageAccount storageAccount;
		try {
			storageAccount = CloudStorageAccount.parse(prop.getProperty("azureStorageTableConnectionString"));
		} catch (IllegalArgumentException | URISyntaxException e) {
			System.out.println("\nConnection string specifies an invalid URI.");
			System.out.println("Please confirm the connection string is in the Azure connection string format.");
			throw e;
		} catch (InvalidKeyException e) {
			System.out.println("\nConnection string specifies an invalid key.");
			System.out.println("Please confirm the AccountName and AccountKey in the connection string are valid.");
			throw e;
		}

		return storageAccount.createCloudTableClient();

	}

	/**
	 * This API will create a table if doesnot exist
	 * 
	 * @param tableClient
	 * @param azureStorageTableName
	 * @return
	 */

	@SuppressWarnings("null")
	public boolean createAzureTableIfNotExists(CloudTableClient tableClient, String azureStorageTableName) {
		CloudTable table = null;
		try {
			table = tableClient.getTableReference(azureStorageTableName);
		} catch (URISyntaxException | StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (table == null) {
			System.out.println("Created new table since it exist");
			try {
				table.createIfNotExist();
				return true;
			} catch (StorageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		} else {
			System.out.println("Table already exists");
		}
		return true;

	}

	/**
	 * This api will read entities from table
	 * 
	 * @param tableClient
	 * @param tableName
	 * @return
	 */
	public boolean readAzureTableEntityList(CloudTableClient tableClient, String tableName) {

		try {
			// Create a filter condition where partition key is “Payload”
			String partitionFilter = TableQuery.generateFilterCondition(TableConstants.PARTITION_KEY,
					QueryComparisons.EQUAL, "Payload");
			// Create a filter condition where PTCACk is “false”
			String ackFilter = TableQuery.generateFilterCondition(ACK_KEY, QueryComparisons.EQUAL, false);
			// combine both filter using AND operator
			String filter = TableQuery.combineFilters(partitionFilter, Operators.AND, ackFilter);
			TableQuery<JCIASTSampleEntity> query = TableQuery.from(tableName, JCIASTSampleEntity.class).where(filter);
			// Iterate over the results
			for (JCIASTSampleEntity entity : tableClient.execute(query)) {

				boolean bomPayloadProcessed = entity.getBomPayloadProcessed();
				boolean partPayloadProcessed = entity.getPartPayloadProcessed();

				// case-I
				if ((bomPayloadProcessed == true) && (partPayloadProcessed == true)) {
					System.out.println("....successfully updated...");
				}
				// case-II
				else if ((bomPayloadProcessed == true) && (partPayloadProcessed == false)) {
					System.out.println("....Bom is not updated suceessfully and Part is Updated..");
				}
				// case-III
				else if ((bomPayloadProcessed == false) && (partPayloadProcessed == true)) {
					System.out.println("....Part is not updated suceessfully and Bom is Updated...");
				}
				// case-IV
				else if ((bomPayloadProcessed == false) && (partPayloadProcessed == false)) {
					System.out.println("...Both are fail...");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;

	}

	/* Retrieve single entity */

	private JCIASTSampleEntity retrieveAzureTableEntity(CloudTableClient tableClient, String tableName,
			String patritionkey, String rowKey) {

		TableOperation findSampleEntity = TableOperation.retrieve(patritionkey, rowKey, JCIASTSampleEntity.class);

		JCIASTSampleEntity sampleEntity = null;

		try {
			sampleEntity = tableClient.execute(tableName, findSampleEntity).getResultAsType();

			System.out.println("Retrieve Single Entity");
			if (sampleEntity != null) {

				System.out.println("PartitionKey:--" + sampleEntity.getPartitionKey() + "" + "RowKey:--"
						+ sampleEntity.getRowKey() + "" + "" + "PTCAck:--" + sampleEntity.getPtcAck() + " Txnid:--"
						+ sampleEntity.getTxnID() + "PartPayload:--" + sampleEntity.getPartPayloadProcessed());

			}
		} catch (StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sampleEntity;
	}

	@Override
	public JCIASTSampleEntity retrieveEntity(String partitionkey, String rowKey) {

		JCIASTSampleEntity sampleEntity = null;
		CloudTableClient tableClient = null;
		try {
			tableClient = getTableClientReference();

			if (tableClient != null) {

				boolean createTable = createAzureTableIfNotExists(tableClient, "ControllesPlmPayload");
				if (createTable) {

					readAzureTableEntityList(tableClient, "ControllesPlmPayload");
					System.out.println("table entities read successfully");
				} else {

					System.out.println("table entities read failed");
				}

				// retrieving a entity
				sampleEntity = retrieveAzureTableEntity(tableClient, "ControllesPlmPayload", partitionkey, rowKey);

			}

		}

		catch (InvalidKeyException | RuntimeException | URISyntaxException | IOException e) {

			e.printStackTrace();
		}

		logger.info("get Acknowledgmenet successfull");

		return sampleEntity;

	}

}
