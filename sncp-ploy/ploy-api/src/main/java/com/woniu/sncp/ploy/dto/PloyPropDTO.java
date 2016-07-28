package com.woniu.sncp.ploy.dto;

import java.io.Serializable;

/**
 * 道具信息
 * @author chenyx
 *
 */
public class PloyPropDTO implements Serializable {

	private static final long serialVersionUID = -1031660161662573197L;

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

	public Long getPropsId() {
		return propsId;
	}

	public void setPropsId(Long propsId) {
		this.propsId = propsId;
	}

	public String getPropsCode() {
		return propsCode;
	}

	public void setPropsCode(String propsCode) {
		this.propsCode = propsCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getGameId() {
		return gameId;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getLimitCondition() {
		return limitCondition;
	}

	public void setLimitCondition(String limitCondition) {
		this.limitCondition = limitCondition;
	}
}
