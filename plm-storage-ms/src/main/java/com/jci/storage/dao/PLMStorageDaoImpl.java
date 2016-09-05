package com.jci.storage.dao;

import java.io.BufferedWriter;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.jci.storage.domain.PLMPayload;
import com.jci.storage.service.PLMStorageServiceImpl;
import com.microsoft.windowsazure.services.blob.client.CloudBlobClient;
import com.microsoft.windowsazure.services.blob.client.CloudBlobContainer;
import com.microsoft.windowsazure.services.blob.client.CloudBlockBlob;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.table.client.CloudTable;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.microsoft.windowsazure.services.table.client.TableOperation;
@Repository
public class PLMStorageDaoImpl implements PLMStorageDao {
	
	private static final Logger LOG = LoggerFactory.getLogger(PLMStorageDaoImpl.class);

	CloudBlobContainer blobContainer = null;
	CloudTableClient tableClient = null;

	
	public static final String storageConnectionString = "DefaultEndpointsProtocol=http;" + "AccountName=erpconnsample;"
			+ "AccountKey=GQZDOpTxJwebJU7n3kjT2VZP1mXCY6QXzVoCZGIsCdvU6rX7E8M5S24+Ki4aYqD2AwK1DnUh6ivlbaVKR7NOTQ==";

	public CloudTableClient getTableClientReference()
			throws RuntimeException, IOException, IllegalArgumentException, URISyntaxException, InvalidKeyException {

		// Retrieve the connection string
		Properties prop = new Properties();
		try {
			InputStream propertyStream = PLMStorageDaoImpl.class.getClassLoader()
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



	
	
	
	
	
	@Override
	public String PutXmlBom(HashMap<String, Object> xml) {
		try {

			File file = new File("PayloadForBlob.xml");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
		
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(xml.get("xml").toString());//getting the xml in string format from subscriber mS
			bw.close();
			String ecnNo=xml.get("ecnNo").toString();

			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			storageAccount.createCloudTableClient();

			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer container = blobClient.getContainerReference("plmcontainer2");
			container.createIfNotExist();
			String filePath = "PayloadForBlob.xml";
			CloudBlockBlob blob = container.getBlockBlobReference(ecnNo);
			java.io.File source = new java.io.File(filePath);
			java.io.FileInputStream fileInputStream = new java.io.FileInputStream(source);
			blob.upload(fileInputStream, source.length());
			
			LOG.info("XML File Stored In Azure Blob");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Xml File Successfully Stored in Azure Blob";
	}

	
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
	
	
	
	public String PutjsonBom(HashMap<String, Object> jsonXml) {
		
		try {
			
			System.out.println("reach to dao of storage at putjsonBom");

			CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
			// Create the table client.
			CloudTableClient tableClient = storageAccount.createCloudTableClient();

			// insert the json into azure table

			// Create the table if it doesn't exist.
			String tableName = "ApigeeData4";
			CloudTable cloudTable = tableClient.getTableReference(tableName);
			cloudTable.createIfNotExist();

			cloudTable = tableClient.getTableReference("ApigeeData4");

			//Setting the entity for azure table
			PLMPayload header = new PLMPayload(jsonXml.get("erp").toString(),jsonXml.get("ecnNo").toString());
			header.setIsprocessed(jsonXml.get("isprocessed").toString());
			header.setIserrored(jsonXml.get("iserrored").toString());
			header.setMessage(jsonXml.get("message").toString());
			header.setCode(jsonXml.get("code").toString());
			header.setStatus(jsonXml.get("status").toString());
			header.setErp(jsonXml.get("erp").toString());
			header.setRegion(jsonXml.get("region").toString());
			header.setPlant(jsonXml.get("plant").toString());
			header.setXmllink(jsonXml.get("xmlbloblink").toString());
			header.setProcesseddate(jsonXml.get("processdate").toString());
			header.setCreateddate(jsonXml.get("createddate").toString());
			header.setProcessby(jsonXml.get("processby").toString());
			header.setCreatedby(jsonXml.get("createdby").toString());
			header.setAcknowledged(jsonXml.get("acknoledge").toString());
			header.setAcknowledgestatus(jsonXml.get("acknoledgestatus").toString());
			header.setAcknowledgecode(jsonXml.get("acknoledgecode").toString());
			header.setAcknowledgemessage(jsonXml.get("acknoledgemessage").toString());
			header.setAcknowledgedate(jsonXml.get("acknoledgedate").toString());
			header.setAcknowledgeby(jsonXml.get("acknoledgeby").toString());
			header.setUiprocessed(jsonXml.get("uiprocessed").toString());
			header.setUiprocessedby(jsonXml.get("uiprocessedby").toString());
			
			TableOperation insertHeader = TableOperation.insertOrReplace(header);

			tableClient.execute(tableName, insertHeader);

		} catch (Exception e) {
			e.printStackTrace();
		}

		
		
		return null;
	}
	
}
