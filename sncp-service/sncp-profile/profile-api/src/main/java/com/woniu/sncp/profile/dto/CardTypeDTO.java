package com.woniu.sncp.profile.dto;

import java.io.Serializable;
import java.util.List;

/**
 * <p>descrption: 卡类型对象</p>
 * 
 * @author fuzl
 * @date   2016年7月7日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public class CardTypeDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 面值大类
	 */
	List<CardValueDTO> value;

	public List<CardValueDTO> getValue() {
		return value;
	}

	public void setValue(List<CardValueDTO> value) {
		this.value = value;
	}
	
}
