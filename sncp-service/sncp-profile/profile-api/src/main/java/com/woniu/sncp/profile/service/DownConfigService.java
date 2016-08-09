package com.woniu.sncp.profile.service;

import com.woniu.sncp.profile.dto.PaginationTo;

public interface DownConfigService {

	/**
	 * 根据应用分类和系统类型查询分页数据
	 * 
	 * 排序字段n_sort,d_create
	 * 
	 * @param type 应用分类
	 * @param osType 系统类型
	 * @param pageSize 每页数据条数
	 * @param pageNumber 当前页码，从1开始
	 * @return PaginationTo
	 */
	PaginationTo query(String type,String osType,int pageSize,int pageNumber);
	
	/**
	 * 根据应用分类和系统类型查询分页数据
	 * 
	 * 排序字段n_sort,d_create
	 * 
	 * @param osType 系统类型
	 * @param pageSize 每页数据条数
	 * @param pageNumber 当前页码，从1开始
	 * @return PaginationTo
	 */
	PaginationTo query(String osType,int pageSize,int pageNumber);
}
