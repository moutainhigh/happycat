package com.woniu.sncp.ploy.dto;

import java.io.Serializable;

/**
 * 道具信息
 * @author chenyx
 *
 */
public class PloyPropsDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	//道具ID
	private Long propsId;
	
	//道具编码
	private String propsCode;
	
	//道具名称
	private String name;
	
	//所属游戏ID
	private Long gameId;
	
	//道具状态
	private String state;
	
	//道具金额
	private String amount;
	
	//限制条件
	private String limitCondition;
	
}
