package com.jci.portal.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jci.portal.domain.req.PlmDetailsRequest;
import com.microsoft.windowsazure.services.blob.client.CloudBlob;
import com.microsoft.windowsazure.services.blob.client.CloudBlobClient;
import com.microsoft.windowsazure.services.blob.client.CloudBlobContainer;
import com.microsoft.windowsazure.services.blob.client.ListBlobItem;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.core.storage.StorageException;

@Service
public class PLMWebPortalReprocessServiceImpl implements PLMWebPortalReprocessService {
	private static final Logger logger = LoggerFactory.getLogger(PLMWebPortalReprocessServiceImpl.class);
	CloudBlobContainer blobContainer = null;

	public CloudBlobClient getBlobClientReference()
			throws RuntimeException, IOException, IllegalArgumentException, URISyntaxException, InvalidKeyException {

		// Retrieve the connection string
		Properties prop = new Properties();
		try {
			InputStream propertyStream = PLMWebPortalGraphServiceImpl.class.getClassLoader()
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
			storageAccount = CloudStorageAccount.parse(prop.getProperty("azureBlobConnectionString"));
		} catch (IllegalArgumentException | URISyntaxException e) {
			System.out.println("\nConnection string specifies an invalid URI.");
			System.out.println("Please confirm the connection string is in the Azure connection string format.");
			throw e;
		} catch (InvalidKeyException e) {
			System.out.println("\nConnection string specifies an invalid key.");
			System.out.println("Please confirm the AccountName and AccountKey in the connection string are valid.");
			throw e;
		}

		return storageAccount.createCloudBlobClient();
	}

	/**
	 * This API will create a blob container if it does not exist
	 * 
	 * @param blobClient
	 * @param azureStorageBlobName
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	public boolean createAzureBlobIfNotExists(CloudBlobClient blobClient, String azureStorageBlobName)
			throws FileNotFoundException, IOException, StorageException, URISyntaxException {
		try {
			blobContainer = blobClient.getContainerReference(azureStorageBlobName);

		} catch (URISyntaxException | StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (blobContainer == null) {
			System.out.println("Created new blob container since it does not exist");
			try {
				blobContainer.createIfNotExist();
				return true;
			} catch (StorageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	@Override
	public HashMap<String, Object> errorProcess(PlmDetailsRequest request) {
		logger.info("### Starting PLMWebPortalReprocessServiceImpl.errorProcess ###");
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		List<String> ecn = request.getEcnNumber();
		System.out.println("ecn : " + ecn + "comment :" + request.getComment() + "erpName :" + request.getErpName());
		for (int i = 0; i < ecn.size(); i++) {
			File xmlFile = getXml();
			Reader fileReader;
			try {
				fileReader = new FileReader(xmlFile);

				BufferedReader bufReader = new BufferedReader(fileReader);
				StringBuilder sb = new StringBuilder();
				String line = bufReader.readLine();
				while (line != null) {
					sb.append(line).append("\n");
					line = bufReader.readLine();
				}
				String xml2String = sb.toString();
				// System.out.println("XML to String using BufferedReader : ");
				// System.out.println(xml2String);
				bufReader.close();
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document document = db.parse(xmlFile);

				document.getDocumentElement().normalize();

				String exp = "/COLLECTION/Release/Transaction/TransactionNumber";
				XPath xPath = XPathFactory.newInstance().newXPath();
				NodeList nodeList = (NodeList) xPath.compile(exp).evaluate(document, XPathConstants.NODESET);
				Node node = nodeList.item(0);
				Element element = (Element) node;
				String result = element.getFirstChild().getTextContent();

				int ecnNo = Integer.parseInt(result);
				String ecn1 = request.getEcnNumber().get(i);
				String uiprocessedby=request.getUiprocessedby().get(i);
				String uiprocessed=request.getUiprocessed().get(i);
				String uiprocessdate=request.getUiprocessdate().get(i);
				int ecnRep = Integer.parseInt(ecn1);
				/*
				 * System.out.println(ecnRep); System.out.println(ecnNo);
				 */
				if (ecnRep == ecnNo) {
					System.out.println("both ecn number are same");
					// System.err.println(xml2String);
					hashMap.put("EcnNo", ecnRep);
					hashMap.put("UIprocessed", uiprocessed);
					hashMap.put("UIprocessedby", uiprocessedby);
					hashMap.put("CompleteXml", xml2String);
					hashMap.put("UIprocessedDate", uiprocessdate);
				}
			} catch (XPathExpressionException | ParserConfigurationException | SAXException | IOException e) {

				e.printStackTrace();
			}

		}
		// System.out.println(hashMap);
		logger.info("### Ending PLMWebPortalReprocessServiceImpl.errorProcess ###");
		return hashMap;
	}

	public File getXml() {
		File file = null;
		try {
			CloudBlobClient cloudBlobClient = getBlobClientReference();
			createAzureBlobIfNotExists(cloudBlobClient, "erpconnblob");
			for (ListBlobItem blobItem : blobContainer.listBlobs()) {

				if (blobItem instanceof CloudBlob) {
					CloudBlob blob = (CloudBlob) blobItem;
					file = new File(blob.getName());
					FileOutputStream fos = new FileOutputStream(file);
					blob.download(fos);
				}
			}
		} catch (StorageException | IOException | InvalidKeyException | RuntimeException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return file;
	}

}
