package com.woniu.sncp.pay.repository.pay;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.woniu.sncp.pojo.MultiKeyPojo;

/**
 * 运营商区服对照表
 * 
 * @author yang.hao
 * @since 2011-12-15 下午5:42:46
 */
@Entity
@Table(name = "PP_ISSUER_COMPARISON", schema = "SN_PASSPORT")
public class IssuerComparison implements MultiKeyPojo {

	private static final long serialVersionUID = 1L;

	/**
	 * 1 - 分区对照(盛大充值分区转换)
	 */
	public static final String TYPE_AREA_MAPPING = "1";
	/**
	 * 2 - 分区所属运营商(运营商允许登录的分区)
	 */
	public static final String TYPE_AREA_ISSUER_MAPPING = "2";
	/**
	 * 3 - 卡类型对照(淘宝卡类型转换)
	 */
	public static final String TYPE_CARD_TYPE_ID_MAPPING = "3";
	/**
	 * 4 - 游戏对照(淘宝游戏转换)
	 */
	public static final String TYPE_GAME_TYPE_ID_MAPPING = "4";
	/**
	 * 5 - 道具卡类型对照(京东卡类型转换)
	 */
	public static final String TYPE_PROP_TYPE_ID_MAPPING = "5";

	/**
	 * 6 - 第三方充值,特殊卡类型
	 */
	public static final String TYPE_CARD_SPECIAL_MAPPING = "6";
	/**
	 * 状态 - 启用
	 */
	public static final String STATE_VALID = "1";

	public static final String TYPE_SNAIL_CARD_TYPE_MAPPING = "n";
	
	@Id
	private IssuerComparisonKey id;

	/**
	 * 我方标识
	 */
	@Column(name = "N_MARK")
	private Long ourMark;

	/**
	 * 状态
	 */
	@Column(name = "S_STATE")
	private String state;

	@Column(name = "S_OTHER_MARK")
	private String otherMark;
	
	public String getOtherMark() {
		return otherMark;
	}

	public void setOtherMark(String otherMark) {
		this.otherMark = otherMark;
	}

	public Long getOurMark() {
		return ourMark;
	}

	public void setOurMark(Long ourMark) {
		this.ourMark = ourMark;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public IssuerComparisonKey getId() {
		return id;
	}

	public void setId(IssuerComparisonKey id) {
		this.id = id;
	}

}
