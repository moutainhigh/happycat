package com.woniu.sncp.profile.dto;

import java.io.Serializable;
import java.util.List;

public class PaginationTo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2235769526291863977L;
	
	private Long totalSize;//总条数
	private int pageNumber;//当前页
	private int pageSize;//每页数据量
	
	private List<DownConfigTo> downConfigList;
	
	public Long getTotalSize() {
		return totalSize;
	}
	public void setTotalSize(Long totalSize) {
		this.totalSize = totalSize;
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
	
	public List<DownConfigTo> getDownConfigList() {
		return downConfigList;
	}
	public void setDownConfigList(List<DownConfigTo> downConfigList) {
		this.downConfigList = downConfigList;
	}
	
}
