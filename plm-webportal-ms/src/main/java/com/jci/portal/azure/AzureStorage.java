package com.jci.portal.azure;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Repository;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;

@Repository
public class AzureStorage {

	private static final Logger LOG = LoggerFactory.getLogger(AzureStorage.class);
	private static final String FORMAT = "DefaultEndpointsProtocol=%s;AccountName=%s;AccountKey=%s";

	private String protocol;
	private String accountName;
	private String accountKey;
	private String tableName;
	private String tableName1;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableName1() {
		return tableName1;
	}

	public void setTableName1(String tableName1) {
		this.tableName1 = tableName1;
	}

	public AzureStorage() {
		protocol = "https";
		accountName = "erpconnsample";
		accountKey = "GQZDOpTxJwebJU7n3kjT2VZP1mXCY6QXzVoCZGIsCdvU6rX7E8M5S24+Ki4aYqD2AwK1DnUh6ivlbaVKR7NOTQ==";
		tableName = "controlsplmpayloadtable";
		tableName1 = "MiscData";
	}

	public AzureStorage(String protocol, String accountName, String accountKey) {
		this.protocol = protocol;
		this.accountName = accountName;
		this.accountKey = accountKey;
	}

	public final String getStoregeConnectionString() {
		 LOG.info(" ### Starting Ending AzureStorage.getStoregeConnectionString ###");
		return String.format(FORMAT, protocol, accountName, accountKey);
	}

	public CloudTable getTable(String tableName) throws InvalidKeyException, URISyntaxException, StorageException {
		 LOG.info(" ### Starting Ending AzureStorage.getTable ### "+tableName);
		return CloudStorageAccount.parse(getStoregeConnectionString()).createCloudTableClient()
				.getTableReference(tableName);
	}

	public CloudTable getTables(String tableName1) throws InvalidKeyException, URISyntaxException, StorageException {
		 LOG.info(" ### Starting Ending AzureStorage.getTable ### "+tableName);
		return CloudStorageAccount.parse(getStoregeConnectionString()).createCloudTableClient()
				.getTableReference(tableName1);
	}

	public CloudTableClient getInstance() throws InvalidKeyException, URISyntaxException, StorageException {
		return CloudStorageAccount.parse(getStoregeConnectionString()).createCloudTableClient();
	}

}
