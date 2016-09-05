package com.jci.portal.service;

import java.util.HashMap;

import com.jci.portal.domain.req.PlmDetailsRequest;

public interface PLMWebPortalReprocessService {
	public HashMap<String, Object> errorProcess(PlmDetailsRequest request);

}
