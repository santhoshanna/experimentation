package com.jci.subscriber.dao;

import java.util.HashMap;

public interface PLMSubscriberMSDao {

	public boolean insertPayloadXMLToBlob(HashMap<String, Object> xml);

}
