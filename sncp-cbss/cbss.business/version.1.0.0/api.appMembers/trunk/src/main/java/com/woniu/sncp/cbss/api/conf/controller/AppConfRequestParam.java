package com.woniu.sncp.cbss.api.conf.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.woniu.sncp.cbss.core.model.request.ParamValueValidateException;
import com.woniu.sncp.cbss.core.model.request.RequestParam;

public class AppConfRequestParam extends RequestParam {

	private static final long serialVersionUID = 8672987272378909273L;
	private String type;
	private String osType;
	private int pageSize;
	private int pageNumber;

	private Long totalSize;//总条数
	
	private List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
	
	public List<Map<String, Object>> getList() {
		return list;
	}

	public void setList(List<Map<String, Object>> list) {
		this.list = list;
	}

	public Long getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(Long totalSize) {
		this.totalSize = totalSize;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOsType() {
		return osType;
	}

	public void setOsType(String osType) {
		this.osType = osType;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	@Override
	public boolean checkParamValueIn()
			throws ParamValueValidateException {
		String osType = getOsType();
		if(StringUtils.isBlank(osType)){
			return false;
		}
		int page = getPageSize();
		int number = getPageNumber();
		if(number > 0 && page <= 0){
			return false;
		}
		return true;
	}
}
