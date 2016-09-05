package com.jci.portal.domain.res;

import java.util.ArrayList;
import java.util.HashMap;
import com.jci.portal.azure.data.ResultSet;

public class SegmentedDetailResponse {

	private boolean isError;
	private String message;// OK

	private HashMap<String, ResultSet> resultSet;

	HashMap<String, ArrayList<Integer>> graphData;
	HashMap<String, ResultSet> errorData;

	HashMap<String, String> userData;

	public boolean isError() {
		return isError;
	}

	public void setError(boolean isError) {
		this.isError = isError;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public HashMap<String, ResultSet> getResultSet() {
		return resultSet;
	}

	public void setResultSet(HashMap<String, ResultSet> resultSet) {
		this.resultSet = resultSet;
	}

	public HashMap<String, ArrayList<Integer>> getGraphData() {
		return graphData;
	}

	public void setGraphData(HashMap<String, ArrayList<Integer>> graphData) {
		this.graphData = graphData;
	}

	public HashMap<String, ResultSet> getErrorData() {
		return errorData;
	}

	public void setErrorData(HashMap<String, ResultSet> errorData) {
		this.errorData = errorData;
	}

	public HashMap<String, String> getUserData() {
		return userData;
	}

	public void setUserData(HashMap<String, String> userData) {
		this.userData = userData;
	}

	@Override
	public String toString() {
		return "SegmentedDetailRes [isError=" + isError + ", message=" + message + ", resultSet=" + resultSet
				+ ", graphData=" + graphData + ", errorData=" + errorData + ", userData=" + userData + "]";
	}

}
