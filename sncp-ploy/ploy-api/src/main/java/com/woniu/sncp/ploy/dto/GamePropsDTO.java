package com.woniu.sncp.ploy.dto;

import java.io.Serializable;

/**
 * 游戏道具明细
 * 
 * @author chenyx
 *
 */
public class GamePropsDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;

	/**
	 * 所属游戏ID - N_GAME_ID
	 */
	private Long gameId;

	/**
	 * 道具编码 - S_PROP_CODE
	 */
	private String propsCode;

	/**
	 * 道具名称 - S_PROP_NAME
	 */
	private String name;

	/**
	 * 游戏中出售币种 - S_CURRENCY
	 */
	private String currency;

	/**
	 * 游戏中出售单价 - N_PRICE
	 */
	private Integer price;

	/**
	 * VIP商品标志 - S_VIP_FLAG
	 */
	private String vipFlag;

	/**
	 * 代币券可支付比例 - N_TOKEN_PERCENT
	 */
	private Double tokenPercent;

	/**
	 * 状态 - S_STATE
	 */
	private String state;

	/**
	 * 灰度服价格 - N_MIN_PRICE
	 */
	private Integer minPrice;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getGameId() {
		return gameId;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
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

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	public String getVipFlag() {
		return vipFlag;
	}

	public void setVipFlag(String vipFlag) {
		this.vipFlag = vipFlag;
	}

	public Double getTokenPercent() {
		return tokenPercent;
	}

	public void setTokenPercent(Double tokenPercent) {
		this.tokenPercent = tokenPercent;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Integer getMinPrice() {
		return minPrice;
	}

	public void setMinPrice(Integer minPrice) {
		this.minPrice = minPrice;
	}
}
