package com.woniu.sncp.profile.po;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


/**
 * 游戏类型定义表 - GAME_TYPE
 * @author fuzl
 *
 */
@Entity
@Table(name = "GAME_TYPE", schema = "SN_PROFILE")
//@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, include = "all")
public class GameTypePo implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 免费游戏
	 */
	public static final String CHARGE_FREE = "0";
	/**
	 * 包月收费 (购买一段时长,期间任意玩)
	 */
	public static final String CHARGE_RENT = "1";
	/**
	 * 包月收费 (购买一段时长,期间任意玩)
	 */
	public static final String CHARGE_BY_TIME = "2";
	/**
	 * 包月收费 (购买一段时长,期间任意玩)
	 */
	public static final String CHARGE_BY_TIME_RENT_FIRST = "3";
	/**
	 * 不需要单独激活(此时的激活方式参考游戏的激活方式)
	 */
	public static final String ACT_MODE_AUTO = "0";
	/**
	 * 须单独手动激活
	 */
	public static final String ACT_MODE_MANUAL = "1";
	/**
	 * 须单独激活码激活
	 */
	public static final String ACT_MODE_MANUAL_WITH_SN = "2";
	/**
	 * MMORPG
	 */
	public static final String CATEGORY_MMORPG = "1";
	/**
	 * 休闲游戏
	 */
	public static final String CATEGORY_LEISURE = "2";
	/**
	 * 网页游戏
	 */
	public static final String CATEGORY_WEB = "3";
	/**
	 * 自主研发
	 */
	public static final String VENDER_TYPE_SELF = "1";
	/**
	 * 合作研发
	 */
	public static final String VENDER_TYPE_COOP = "2";
	/**
	 * 代理产品
	 */
	public static final String VENDER_TYPE_AGENT = "3";
	/**
	 * 游戏状态启用
	 */
	public static final String GAME_STATE_OPEN = "1";
	/**
	 * 游戏状态关闭
	 */
	public static final String GAME_STATE_CLOSE = "0";

	@Id
	@Column(name = "N_GAME_ID")
	private Long id;
	
	public GameTypePo() {
	}

	public GameTypePo(Long id, String gameName, String dynamicTableName, String currency, String chargeType, String activationMode) {
		this.id = id;
		this.name = gameName;
		this.keyName = dynamicTableName;
		this.currency = currency;
		this.chargeType = chargeType;
		this.activeMode = activationMode;
	}
	/**
	 * 游戏名称 - S_GAME_NAME
	 */
	@Column(name = "S_GAME_NAME")
	private String name;
	/**
	 * 表名关键字 - S_TABLE_NAME
	 */
	@Column(name = "S_TABLE_NAME")
	private String keyName;
	/**
	 * 计费币种 - S_CURRENCY
	 */
	@Column(name = "S_CURRENCY")
	private String currency;
	/**
	 * 收费类型 - S_CHARGE_TYPE
	 */
	@Column(name = "S_CHARGE_TYPE")
	private String chargeType;
	/**
	 * 激活方式 - S_ACTIVE_MODE
	 */
	@Column(name = "S_ACTIVE_MODE")
	private String activeMode;
	/**
	 * 游戏类别 - S_SORT
	 */
	@Column(name = "S_SORT")
	private String sort;
	/**
	 * 游戏来源 - S_FROM
	 */
	@Column(name = "S_FROM")
	private String sourceType;
	/**
	 * 游戏描述 - S_GAME_DESC
	 */
	@Column(name = "S_GAME_DESC")
	private String desc;
	/**
	 * 状态 - S_STATE
	 */
	@Column(name = "S_STATE")
	private String state;

	public String getChargeType() {
		return chargeType;
	}

	public void setChargeType(String chargeType) {
		this.chargeType = chargeType;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	public String getActiveMode() {
		return activeMode;
	}

	public void setActiveMode(String activeMode) {
		this.activeMode = activeMode;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
}
