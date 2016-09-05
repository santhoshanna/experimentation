package com.jci.portal.service;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jci.portal.azure.data.DataHelper;
import com.jci.portal.azure.data.DataUtil;
import com.jci.portal.azure.data.ResultSet;
import com.jci.portal.azure.query.PaginationParam;
import com.jci.portal.azure.query.ScrollingParam;
import com.jci.portal.domain.MiscDataEntity;
import com.jci.portal.domain.req.SegmentedDetailRequest;
import com.jci.portal.domain.res.SegmentedDetailResponse;
import com.jci.portal.utils.AzureUtils;
import com.jci.portal.utils.Constants;
import com.jci.portal.utils.QueryBuilder;
import com.microsoft.azure.storage.ResultContinuation;
import com.microsoft.azure.storage.ResultSegment;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.DynamicTableEntity;
import com.microsoft.azure.storage.table.EntityProperty;
import com.microsoft.azure.storage.table.TableQuery;

@Service
@Transactional
@ComponentScan("com.jci.portal")
@RefreshScope
public class PLMWebPortalServiceImpl implements PLMWebPortalService {
	private static final Logger LOG = LoggerFactory.getLogger(PLMWebPortalServiceImpl.class);
	@Value("SYMIX")
	private String allErps;
	static int counter = 0;
	final int batchSize = 15;// Azure bad request need to solve

	@Autowired
	private com.jci.portal.azure.AzureStorage azureStorage;

	@Override
	public SegmentedDetailResponse getSegmentedResultSetData(SegmentedDetailRequest request)
			throws InvalidKeyException, URISyntaxException, StorageException {
		LOG.info("### Starting Ending PLMWebPortalServiceImpl.getSegmentedResultSet ### ");
		PaginationParam paginationParam = request.getPaginationParam();

		ScrollingParam param = new ScrollingParam();

		if (paginationParam != null) {
			param.setPartition(paginationParam.getNextPartition());
			param.setRow(paginationParam.getNextRow());
		}

		// For where condition
		param.setSize(request.getSize());

		DataHelper azureRequest = null;
		ResultSet resultSet = null;

		SegmentedDetailResponse response = new SegmentedDetailResponse();
		HashMap<String, ResultSet> resultSetMap = new HashMap<String, ResultSet>();
		HashMap<String, ResultSet> errorMap = new HashMap<String, ResultSet>();
		if (request.isFirstRequest()) {
			String[] erpArr = allErps.split(",");
			for (int i = 0; i < erpArr.length; i++) {
				azureRequest = new DataHelper();
				azureRequest.setErrorDataRequired(false);
				azureRequest.setErpName(erpArr[i]);
				azureRequest.setPartitionValue(AzureUtils.getPartitionKey(erpArr[i]));
				azureRequest.setTableName(Constants.TABLE_PLM_DETAILS);

				resultSet = getSegmentedResultSetData(param, azureRequest);
				resultSetMap.put(erpArr[i], resultSet);

				azureRequest.setTableName(Constants.TABLE_PLM_DETAILS);
				azureRequest.setErrorDataRequired(true);
				resultSet = getSegmentedResultSetData(param, azureRequest);
				errorMap.put(erpArr[i], resultSet);

			}
			response.setGraphData(getGraphData());
			response.setResultSet(resultSetMap);
			response.setErrorData(errorMap);

		} else {
			azureRequest = new DataHelper();
			azureRequest.setErpName(request.getErpName());
			azureRequest.setPartitionValue(request.getPartition());
			azureRequest.setTableName(request.getTableName());
			resultSet = getSegmentedResultSetData(param, azureRequest);
			resultSetMap.put(request.getErpName(), resultSet);
			response.setResultSet(resultSetMap);
		}
		response.setMessage(Constants.JSON_OK);

		// Remove this
		HashMap userData = new HashMap();
		userData.put("UserName", "Sunil Soni");
		userData.put("GlobalId", "csonisk");
		userData.put("Role", "Admin");

		response.setUserData(userData);

		LOG.info("### Ending Ending PLMWebPortalServiceImpl.getSegmentedResultSet ### ");

		return response;
	}

	@Override
	public ResultSet getSegmentedResultSetData(ScrollingParam param, DataHelper request)
			throws InvalidKeyException, URISyntaxException, StorageException {
		LOG.info("#### Starting PLMWebPortalServiceImpl.getSegmentedResultSet ###");
		ResultContinuation continuationToken = DataUtil.getContinuationToken(param);
		PaginationParam pagination = new PaginationParam();
		if (continuationToken != null) {
			pagination.setLastPartition(param.getPartition());
			pagination.setLastRow(param.getRow());
		}

		// Create the query
		String whereCondition = null;
		if (request.isErrorDataRequired()) {
			whereCondition = QueryBuilder.errorDataQuery(request.getPartitionValue());
		} else {
			whereCondition = QueryBuilder.partitionWhereCondition(request.getPartitionValue());
		}

		if (StringUtils.isBlank(whereCondition)) {
			return null;
		}
		TableQuery<DynamicTableEntity> query = TableQuery.from(DynamicTableEntity.class).where(whereCondition)
				.take(param.getSize());
		CloudTable table = azureStorage.getTable(request.getTableName());

		// segmented query
		ResultSegment<DynamicTableEntity> response = table.executeSegmented(query, continuationToken);

		// next continuation token
		continuationToken = response.getContinuationToken();
		if (continuationToken != null) {
			pagination.setNextPartition(continuationToken.getNextPartitionKey());
			pagination.setNextRow(continuationToken.getNextRowKey());
		}

		HashMap<String, Object> hashmap;

		List<HashMap<String, Object>> series = new ArrayList<HashMap<String, Object>>();
		DynamicTableEntity row;
		EntityProperty ep;

		Iterator<DynamicTableEntity> rows = response.getResults().iterator();
		while (rows.hasNext()) {
			row = rows.next();
			HashMap<String, EntityProperty> map = row.getProperties();
			hashmap = new HashMap<String, Object>();
			hashmap.put("id", row.getRowKey());
			hashmap.put("ECNNumber", row.getRowKey());
			for (String key : map.keySet()) {
				ep = map.get(key);
				hashmap.put(key, ep.getValueAsString());
			}
			series.add(hashmap);
		}
		LOG.info("#### Ending PLMWebPortalServiceImpl.getSegmentedResultSet ###");
		// ResultSet(series,getErrorData(request.getPartitionValue()),pagination)

		return new ResultSet(series, pagination);
	}

	@Override
	public SegmentedDetailResponse getErrorResultSetData(SegmentedDetailRequest request)
			throws InvalidKeyException, URISyntaxException, StorageException {
		LOG.info("### Starting Ending PLMWebPortalServiceImpl.getErrorResultSet ### ");
		PaginationParam paginationParam = request.getPaginationParam();

		ScrollingParam param = new ScrollingParam();

		if (paginationParam != null) {
			param.setPartition(paginationParam.getNextPartition());
			param.setRow(paginationParam.getNextRow());
		}

		// For where condition
		param.setSize(request.getSize());

		DataHelper azureRequest = null;
		ResultSet resultSet = null;

		SegmentedDetailResponse response = new SegmentedDetailResponse();
		HashMap<String, ResultSet> resultSetMap = new HashMap<String, ResultSet>();

		azureRequest = new DataHelper();
		azureRequest.setErrorDataRequired(true);
		azureRequest.setErpName(request.getErpName());
		azureRequest.setPartitionValue(request.getPartition());
		azureRequest.setTableName(request.getTableName());
		resultSet = getSegmentedResultSetData(param, azureRequest);

		resultSetMap.put(request.getErpName(), resultSet);
		response.setErrorData(resultSetMap);
		response.setMessage(Constants.JSON_OK);

		LOG.info("### Ending Ending PLMWebPortalServiceImpl.getErrorResultSet ### ");

		return response;
	}

	@Override
	public HashMap<String, ArrayList<Integer>> getGraphData()
			throws InvalidKeyException, URISyntaxException, StorageException {

		String query = QueryBuilder.graphQuery(Constants.PARTITION_KEY_MISCDATA, allErps);
		LOG.info("query : " + query);

		TableQuery<MiscDataEntity> partitionQuery = TableQuery.from(MiscDataEntity.class).where(query);
		CloudTable cloudTable = azureStorage.getTables(Constants.TABLE_MISC);

		HashMap<String, ArrayList<Integer>> graphData = new HashMap<String, ArrayList<Integer>>();
		ArrayList<Integer> list = null;

		for (MiscDataEntity entity : cloudTable.execute(partitionQuery)) {
			list = new ArrayList<Integer>();
			list.add(entity.getProcessedCount());
			list.add(entity.getErrorCount());
			graphData.put(entity.getRowKey(), list);
		}
		return graphData;
	}

}
