package com.jci.portal.domain.req;

import java.util.List;

public class PlmDetailsRequest {
	
	private String erpName;
	private String userName;
	private String globalId;
	private String comment;
	private Boolean isErrored;
	private List<String> ecnNumber;
	private List<String> uiprocessed;
	private List<String> uiprocessdate;
	public List<String> getUiprocessdate() {
		return uiprocessdate;
	}
	public void setUiprocessdate(List<String> uiprocessdate) {
		this.uiprocessdate = uiprocessdate;
	}
	public List<String> getUiprocessed() {
		return uiprocessed;
	}
	public void setUiprocessed(List<String> uiprocessed) {
		this.uiprocessed = uiprocessed;
	}
	public List<String> getUiprocessedby() {
		return uiprocessedby;
	}
	public void setUiprocessedby(List<String> uiprocessedby) {
		this.uiprocessedby = uiprocessedby;
	}
	private List<String> uiprocessedby;
	
	public List<String> getEcnNumber() {
		return ecnNumber;
	}
	public void setEcnNumber(List<String> ecnNumber) {
		this.ecnNumber = ecnNumber;
	}
	public String getErpName() {
		return erpName;
	}
	public void setErpName(String erpName) {
		this.erpName = erpName;
	}
	public Boolean getIsErrored() {
		return isErrored;
	}
	public void setIsErrored(Boolean isErrored) {
		this.isErrored = isErrored;
	}
	/*public List<String> getPoNo() {
		return poNo;
	}
	public void setPoNo(List<String> poNo) {
		this.poNo = poNo;
	}*/
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getGlobalId() {
		return globalId;
	}
	public void setGlobalId(String globalId) {
		this.globalId = globalId;
	}
	
	
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	@Override
	public String toString() {
		return "PoDetailsReq [erpName=" + erpName + ", userName=" + userName + ", globalId=" + globalId + ", comment="
				+ comment + ", ECNNumber=" + ecnNumber + "]";
	}



}
