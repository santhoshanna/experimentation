package com.jci.portal.azure.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.jci.portal.azure.query.PaginationParam;

//import com.jci.po.azure.query.PaginationParam;

public class ResultSet {
	
	List<HashMap<String, Object>> series ;
	List<HashMap<String, String>> errorData = new ArrayList<HashMap<String, String>>() ;
	PaginationParam pagination ;
	public ResultSet() {
		
	}
	/*List<HashMap<String, JCIASTSampleEntity>> series;
		
	public List<HashMap<String, JCIASTSampleEntity>> getSeries() {
		return series;
	}

	public void setSeries(List<HashMap<String, JCIASTSampleEntity>> series) {
		this.series = series;
	}*/
	
	public ResultSet(List<HashMap<String, Object>> series1, PaginationParam pagination1) {
		this.series = series1 ;
		this.pagination = pagination1 ;
	}
	
	public List<HashMap<String, Object>> getSeries() {
		return series;
	}

	public void setSeries(List<HashMap<String, Object>> series) {
		this.series = series;
	}

	public PaginationParam getPagination() {
		return pagination;
	}

	public void setPagination(PaginationParam pagination) {
		this.pagination = pagination;
	}

	@Override
	public String toString() {
		return "ResultSet [series=" + series +"]";
	}
	
}
