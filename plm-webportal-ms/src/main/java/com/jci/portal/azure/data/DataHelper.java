package com.jci.portal.azure.data;

import java.util.Date;

public class DataHelper {
	
    private String partitionValue;
    private Date timestamp;
    private int  status;
    private String tableName;
    private String erpName;
    private boolean isErrorDataRequired;
    
    public String getPartitionValue() {
        return this.partitionValue;
    }

    public String getErpName() {
		return erpName;
	}

	public void setErpName(String erpName) {
		this.erpName = erpName;
	}

	public void setPartitionValue(String partitionValue) {
        this.partitionValue = partitionValue;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }


    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public boolean isErrorDataRequired() {
		return isErrorDataRequired;
	}

	public void setErrorDataRequired(boolean isErrorDataRequired) {
		this.isErrorDataRequired = isErrorDataRequired;
	}

	@Override
	public String toString() {
		return "DataHelper [partitionValue=" + partitionValue + ", timestamp=" + timestamp + ", status=" + status
				+ ", tableName=" + tableName + ", erpName=" + erpName + ", isErrorDataRequired=" + isErrorDataRequired
				+ "]";
	}




}
