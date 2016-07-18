package com.woniu.sncp.profile.dto;

import java.io.Serializable;
import java.util.List;

/**
 * <p>descrption: 所有活动对象</p>
 * 
 * @author fuzl
 * @date   2016年7月7日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public class AllActivityDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 活动信息列表
	 */
	List<PassportPresentsPloyDTO> ploys;
	
	public List<PassportPresentsPloyDTO> getPloys() {
		return ploys;
	}
	public void setPloys(List<PassportPresentsPloyDTO> ploys) {
		this.ploys = ploys;
	}

}
