package com.jci.ackmsplm.domain;

import com.microsoft.windowsazure.services.table.client.TableServiceEntity;

public class ControllesPlmEntity extends TableServiceEntity {

	private String acknowledged;
	private String iserrored;
	private String isprocessed;
	private String Status;
	private String Message;

	public ControllesPlmEntity(String partitionKey, String rowKey) {
		this.partitionKey = "SYMIX_PLM";
		this.rowKey = rowKey;

	}

	public ControllesPlmEntity() {
	}

	public String getIserrored() {
		return iserrored;
	}

	public void setIserrored(String iserrored) {
		this.iserrored = iserrored;
	}

	public String getIsprocessed() {
		return isprocessed;
	}

	public void setIsprocessed(String isprocessed) {
		this.isprocessed = isprocessed;
	}

	public String getAcknowledged() {
		return acknowledged;
	}

	public void setAcknowledged(String acknowledged) {
		this.acknowledged = acknowledged;
	}

	public String getStatus() {
		return Status;
	}

	public void setStatus(String status) {
		Status = status;
	}

	public String getMessage() {
		return Message;
	}

	public void setMessage(String message) {
		Message = message;
	}

}
