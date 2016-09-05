package com.jci.ackmsplm.domain;

import com.microsoft.windowsazure.services.table.client.TableServiceEntity;

public class JCIASTSampleEntity extends TableServiceEntity {

	private long txnID;
	private Boolean ptcAck;
	private Boolean partPayloadProcessed;
	private Boolean bomPayloadProcessed;

	public JCIASTSampleEntity(String partitionKey, String rowKey) {
		this.partitionKey = "Payload";
		this.rowKey = rowKey;

	}

	public JCIASTSampleEntity() {
	}

	public Boolean getPtcAck() {
		return ptcAck;
	}

	public void setPtcAck(Boolean ptcAck) {
		this.ptcAck = ptcAck;
	}

	public Boolean getPartPayloadProcessed() {
		return partPayloadProcessed;
	}

	public void setPartPayloadProcessed(Boolean partPayloadProcessed) {
		this.partPayloadProcessed = partPayloadProcessed;
	}

	public Boolean getBomPayloadProcessed() {
		return bomPayloadProcessed;
	}

	public void setBomPayloadProcessed(Boolean bomPayloadProcessed) {
		this.bomPayloadProcessed = bomPayloadProcessed;
	}

	public long getTxnID() {
		return txnID;
	}

	public void setTxnID(long txnID) {
		this.txnID = txnID;
	}

}
