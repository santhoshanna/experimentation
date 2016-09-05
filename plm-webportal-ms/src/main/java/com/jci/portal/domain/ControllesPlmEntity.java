package com.jci.portal.domain;

import com.microsoft.azure.storage.table.TableServiceEntity;

public class ControllesPlmEntity extends TableServiceEntity {
		String isprocessed;
			String iserrored;
			String message;
			String code;
			String status;
			String erp;
			String region;
			String plant;
			String xmllink;
			String processeddate;
			String createddate;
			String processby;
			String createdby;
			String acknowledged;
			String acknowledgestatus;
			String acknowledgecode;
			String acknowledgemessage;
			String acknowledgedate;
			String acknowledgeby;
			String uiprocessed;
			String uiprocessedby;
			String uiprocessdate;
			public ControllesPlmEntity() {
				// TODO Auto-generated constructor stub
			}
			public ControllesPlmEntity(String TransactionId, String partitionKey) {
				this.partitionKey = partitionKey;
				this.rowKey =TransactionId ;
				
			}
			public String getIsprocessed() {
				return isprocessed;
			}
			public void setIsprocessed(String isprocessed) {
				this.isprocessed = isprocessed;
			}
			public String getIserrored() {
				return iserrored;
			}
			public void setIserrored(String iserrored) {
				this.iserrored = iserrored;
			}
			public String getMessage() {
				return message;
			}
			public void setMessage(String message) {
				this.message = message;
			}
			public String getCode() {
				return code;
			}
			public void setCode(String code) {
				this.code = code;
			}
			public String getStatus() {
				return status;
			}
			public void setStatus(String status) {
				this.status = status;
			}
			public String getErp() {
				return erp;
			}
			public void setErp(String erp) {
				this.erp = erp;
			}
			public String getRegion() {
				return region;
			}
			public void setRegion(String region) {
				this.region = region;
			}
			public String getPlant() {
				return plant;
			}
			public void setPlant(String plant) {
				this.plant = plant;
			}
			public String getXmllink() {
				return xmllink;
			}
			public void setXmllink(String xmllink) {
				this.xmllink = xmllink;
			}
			public String getProcesseddate() {
				return processeddate;
			}
			public void setProcesseddate(String processeddate) {
				this.processeddate = processeddate;
			}
			public String getCreateddate() {
				return createddate;
			}
			public void setCreateddate(String createddate) {
				this.createddate = createddate;
			}
			public String getProcessby() {
				return processby;
			}
			public void setProcessby(String processby) {
				this.processby = processby;
			}
			public String getCreatedby() {
				return createdby;
			}
			public void setCreatedby(String createdby) {
				this.createdby = createdby;
			}
			public String getAcknowledged() {
				return acknowledged;
			}
			public void setAcknowledged(String acknowledged) {
				this.acknowledged = acknowledged;
			}
			public String getAcknowledgestatus() {
				return acknowledgestatus;
			}
			public void setAcknowledgestatus(String acknowledgestatus) {
				this.acknowledgestatus = acknowledgestatus;
			}
			public String getAcknowledgecode() {
				return acknowledgecode;
			}
			public void setAcknowledgecode(String acknowledgecode) {
				this.acknowledgecode = acknowledgecode;
			}
			public String getAcknowledgemessage() {
				return acknowledgemessage;
			}
			public void setAcknowledgemessage(String acknowledgemessage) {
				this.acknowledgemessage = acknowledgemessage;
			}
			public String getAcknowledgedate() {
				return acknowledgedate;
			}
			public void setAcknowledgedate(String acknowledgedate) {
				this.acknowledgedate = acknowledgedate;
			}
			public String getAcknowledgeby() {
				return acknowledgeby;
			}
			public void setAcknowledgeby(String acknowledgeby) {
				this.acknowledgeby = acknowledgeby;
			}
			public String getUiprocessed() {
				return uiprocessed;
			}
			public void setUiprocessed(String uiprocessed) {
				this.uiprocessed = uiprocessed;
			}
			public String getUiprocessedby() {
				return uiprocessedby;
			}
			public void setUiprocessedby(String uiprocessedby) {
				this.uiprocessedby = uiprocessedby;
			}
			public String getUiprocessdate() {
				return uiprocessdate;
			}
			public void setUiprocessdate(String uiprocessdate) {
				this.uiprocessdate = uiprocessdate;
			} 

}
