package com.woniu.sncp.pay.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Pageable<T> implements Serializable {

	private static final long serialVersionUID = -8495626896878269681L;

	private static final int DEFAULT_PAGE_SIZE = 10;

	public Pageable() {
	}

	public Pageable(int totalCount, int pageSize, int currentPage) {
		this.totalCount = totalCount;
		this.pageSize = pageSize;
		this.currentPage = currentPage;
	}

	// 总条数
	private int totalCount;

	// 总页数
	private int totalPage;

	// 每页条数
	private int pageSize = DEFAULT_PAGE_SIZE;

	// 起始条数，默认从0开始
	private int startIndex = 0;

	// 当前页
	private int currentPage;

	// 上一页
	private int nextPage;

	// 下一页
	private int priviousPage;

	// 当前页对象集合
	private List<T> items = new ArrayList<T>();

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getPageSize() {
		if (pageSize == 0) {
			// Default to 10, if pageSize == 0
			pageSize = DEFAULT_PAGE_SIZE;
		}
		// if pageSize < 0, it means that query all data
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getCurrentPage() {
		if (currentPage <= 0) {
			return 1;
		}
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public List<T> getItems() {
		return items;
	}

	public void setItems(List<T> items) {
		this.items = items;
	}

	public int getTotalPage() {
		if (totalCount > 0) {
			if (getPageSize() > 0) {
				totalPage = totalCount / getPageSize();
				if ((totalCount % getPageSize()) > 0) {
					totalPage++;
				}
			} else {
				totalPage = 1;
			}
		} else {
			totalPage = 0;
		}
		return totalPage;
	}

	public int getStartIndex() {
		if (getCurrentPage() > 0) {
			if (getPageSize() > 0) {
				startIndex = (getCurrentPage() - 1) * getPageSize();
			} else {
				// query all data, index from 0
				startIndex = 0;
			}
		} else {
			startIndex = 0;
		}
		return startIndex;
	}

	public int getNextPage() {
		if (getCurrentPage() == getTotalPage()) {
			nextPage = getTotalPage();
		} else {
			if (getTotalPage() == 0) {
				nextPage = 1;
			} else {
				nextPage = getCurrentPage() + 1;
			}

		}
		return nextPage;
	}

	public int getPriviousPage() {
		if (getCurrentPage() <= 1) {
			priviousPage = 1;
		} else {
			priviousPage = getCurrentPage() - 1;
		}
		return priviousPage;
	}
}
