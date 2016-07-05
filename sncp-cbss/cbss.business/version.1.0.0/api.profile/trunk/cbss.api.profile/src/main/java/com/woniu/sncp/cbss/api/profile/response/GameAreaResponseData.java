package com.woniu.sncp.cbss.api.profile.response;

import java.io.Serializable;

/**
 * 
 * <p>
 * descrption: 游戏配置获取分区响应
 * </p>
 * 
 * @author fuzl
 * @date 2016年7月5日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public class GameAreaResponseData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long areaId;

	private String areaName;

	public Long getAreaId() {
		return areaId;
	}

	public void setAreaId(Long areaId) {
		this.areaId = areaId;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public GameAreaResponseData() {
	}

	public GameAreaResponseData(Long areaId, String areaName) {
		super();
		this.areaId = areaId;
		this.areaName = areaName;
	}

}
