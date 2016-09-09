package com.jci.portal.service;

import java.util.HashMap;

import com.jci.portal.domain.PLMPayloadTableEntity;

public interface PLMWebPortalReprocessService {
	public HashMap<String, Object> errorProcess(PLMPayloadTableEntity request);

}
