package com.woniu.sncp.pay.repository.pay;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2017年4月1日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
public class ConfigInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8143109219354620264L;

	//主键id
	private Long id;
	//序列起始值
	private Long beginNum;
	//序列结束值
	private Long endNum;
	//表后缀标识
	private String tableIndex;
	//数据库名称
	private Long dbName;
	//创建时间
	private Date create;
	//最后序列更新时间
	private Date endOpt;
	//是否开启
	private Integer enable;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getBeginNum() {
		return beginNum;
	}
	public void setBeginNum(Long beginNum) {
		this.beginNum = beginNum;
	}
	public Long getEndNum() {
		return endNum;
	}
	public void setEndNum(Long endNum) {
		this.endNum = endNum;
	}
	public String getTableIndex() {
		return tableIndex;
	}
	public void setTableIndex(String tableIndex) {
		this.tableIndex = tableIndex;
	}
	public Long getDbName() {
		return dbName;
	}
	public void setDbName(Long dbName) {
		this.dbName = dbName;
	}
	public Date getCreate() {
		return create;
	}
	public void setCreate(Date create) {
		this.create = create;
	}
	public Date getEndOpt() {
		return endOpt;
	}
	public void setEndOpt(Date endOpt) {
		this.endOpt = endOpt;
	}
	public Integer getEnable() {
		return enable;
	}
	public void setEnable(Integer enable) {
		this.enable = enable;
	}
}
