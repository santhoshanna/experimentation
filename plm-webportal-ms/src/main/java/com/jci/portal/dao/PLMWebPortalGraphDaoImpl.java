package com.jci.portal.dao;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jci.portal.domain.ControllesPlmEntity;
import com.jci.portal.domain.MiscDataEntity;
import com.jci.portal.utils.Constants;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableQuery;
import com.microsoft.azure.storage.table.TableQuery.QueryComparisons;
import com.microsoft.windowsazure.services.table.client.TableConstants;

@Repository
public class PLMWebPortalGraphDaoImpl implements PLMWebPortalGraphDao {
	private static final Logger logger = LoggerFactory.getLogger(PLMWebPortalGraphDaoImpl.class);

	@Autowired
	private com.jci.portal.azure.AzureStorage azureStorage;

	@Override
	public MiscDataEntity getTotalProcessedEntities() {
		int isProcessedcount = 0;
		int isErroredcount = 0;
		logger.info("### Starting PLMWebPortalGraphDaoImpl.getTotalProcessedEntities ###");
		MiscDataEntity dataEntity = new MiscDataEntity("TOTAL_COUNT", "SYMIX");
		try {
			CloudTable cloudTable = azureStorage.getTable(Constants.TABLE_PLM_DETAILS);

			String partitionFilter = TableQuery.generateFilterCondition(TableConstants.PARTITION_KEY,
					QueryComparisons.EQUAL, "SYMIX_PLM");

			TableQuery<ControllesPlmEntity> query = TableQuery.from(ControllesPlmEntity.class).where(partitionFilter);

			for (ControllesPlmEntity entity : cloudTable.execute(query)) {
				String p1=entity.getIsprocessed();
				Boolean isprocessed=Boolean.parseBoolean(p1);
				System.out.println("processed boolean............"+isprocessed);
				String e1=entity.getIserrored();
				Boolean iserrored=Boolean.parseBoolean(e1);
				if (isprocessed == true && iserrored == false) {
					isProcessedcount++;
				}
				if (isprocessed == true && iserrored == true) {
					isErroredcount++;
				} 
			}

			dataEntity.setErrorCount(isErroredcount);
			dataEntity.setProcessedCount(isProcessedcount);
			System.out.println("Sum of  IsProcessed :" + isProcessedcount);
			System.out.println("Sum of  IsErrored :" + isErroredcount);

		} catch (Exception e) {
			e.printStackTrace();

		}
		logger.info("### Ending PLMWebPortalGraphDaoImpl.getTotalProcessedEntities ###");
		return dataEntity;

	}

	@Override
	public String insertData() {
		logger.info("### Starting PLMWebPortalGraphDaoImpl.insertData ###");
		CloudTable cloudTable;
		try {
			cloudTable = azureStorage.getTables(Constants.TABLE_MISC);

			MiscDataEntity dataEntity= getTotalProcessedEntities();
			System.out.println(dataEntity);
			System.out.println(dataEntity.getErrorCount());
			System.out.println(dataEntity.getProcessedCount());
			dataEntity.setErrorCount(dataEntity.getErrorCount());
			dataEntity.setProcessedCount(dataEntity.getProcessedCount()); 
			TableOperation insert = TableOperation.insertOrReplace(dataEntity);
			cloudTable.execute(insert);

		} catch (InvalidKeyException | URISyntaxException | StorageException e) {
			
		
			e.printStackTrace();
		}
		
		logger.info("### Ending PLMWebPortalGraphDaoImpl.insertData ###");
		return "done";
	}

}
