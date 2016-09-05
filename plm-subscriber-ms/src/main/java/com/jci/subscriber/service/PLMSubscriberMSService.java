package com.jci.subscriber.service;

import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;


public interface PLMSubscriberMSService {

	public ServiceBusContract azureConnectionSetup();

	public boolean azureMessagePublisher(ServiceBusContract service, String message);
	public String azureMessageSubscriber(ServiceBusContract service);

}
