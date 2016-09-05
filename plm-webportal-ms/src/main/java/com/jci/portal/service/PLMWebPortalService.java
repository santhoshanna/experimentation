package com.jci.portal.service;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;

import com.jci.portal.azure.data.DataHelper;
import com.jci.portal.azure.data.ResultSet;
import com.jci.portal.azure.query.ScrollingParam;
import com.jci.portal.domain.req.SegmentedDetailRequest;
import com.jci.portal.domain.res.SegmentedDetailResponse;
import com.microsoft.azure.storage.StorageException;

public interface PLMWebPortalService {

	public SegmentedDetailResponse getSegmentedResultSetData(SegmentedDetailRequest request)
			throws InvalidKeyException, URISyntaxException, StorageException, com.microsoft.windowsazure.services.core.storage.StorageException;

	ResultSet getSegmentedResultSetData(ScrollingParam param, DataHelper request)
			throws InvalidKeyException, URISyntaxException, StorageException, com.microsoft.windowsazure.services.core.storage.StorageException;

	SegmentedDetailResponse getErrorResultSetData(SegmentedDetailRequest request)
			throws InvalidKeyException, URISyntaxException, StorageException;

	HashMap<String, ArrayList<Integer>> getGraphData() throws InvalidKeyException, URISyntaxException, StorageException;

}
