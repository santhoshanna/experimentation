package com.jci.portal.domain;

import com.microsoft.azure.storage.table.TableServiceEntity;

//import com.microsoft.windowsazure.services.table.client.TableServiceEntity;

public class MiscDataEntity   extends TableServiceEntity {

	public MiscDataEntity(String partitionKey, String rowKey) {
		this.partitionKey = partitionKey;
		this.rowKey = rowKey; 
	}
	public MiscDataEntity() {
		
	}
	private int processedCount;//2
	private int errorCount;//3 
	private String ecnNumber;
	private String comment;
	
	public int getProcessedCount() {
		return processedCount;
	}

	public void setProcessedCount(int processedCount) {
		this.processedCount = processedCount;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public void setErrorCount(int errorCount) {
		this.errorCount = errorCount;
	}
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getEcnNumber() {
		return ecnNumber;
	}

	public void setEcnNumber(String ecnNumber) {
		this.ecnNumber = ecnNumber;
	}

	@Override
	public String toString() {
		return "MiscDataEntity [processedCount=" + processedCount
				+ ", errorCount=" + errorCount + ", ECNNumber=" + ecnNumber +  "]";
	}
	
	
}
