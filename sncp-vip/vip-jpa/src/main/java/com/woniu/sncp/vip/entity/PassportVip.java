package com.woniu.sncp.vip.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * 通行证帐号VIP信息表 - SN_PASSPORT.PP_VIP
 */
@Entity
@Table(name = "PP_VIP", schema = "SN_PASSPORT")
public class PassportVip implements Serializable {

	/**
	 * 序列化值
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 复合主键
	 */
	@Id
	private PassportVipPK id;

	/**
	 * 充值VIP级别 - S_VIP_LEVEL
	 */
	@Column(name = "S_VIP_LEVEL")
	private String vipLevel;

	/**
	 * 本月累计充值金额 - N_CUMULATE_MONEY
	 */
	@Column(name = "N_CUMULATE_MONEY")
	private Integer cumulateMoney;

	/**
	 * 当前月份 - N_MONTH
	 */
	@Column(name = "N_MONTH")
	private Integer month;

	/**
	 * 累计成长值 - N_CUMULATE_SCORE
	 */
	@Column(name = "N_CUMULATE_SCORE")
	private Integer cumulateScore;

	/**
	 * 赠送人ID-N_SEND_AID
	 */
	@Column(name = "N_SEND_AID")
	private Long sendAid;

	/**
	 * 赠送人所在服务器ID-N_SERVER_ID
	 */
	@Column(name = "N_SERVER_ID")
	private Long serverId;

	/**
	 * 赠送VIP级别-S_SEND_LEVEL
	 */
	@Column(name = "S_SEND_LEVEL")
	private String sendVipLevel;

	/**
	 * 赠送时间-D_SEND_TIME
	 */
	@Column(name = "D_SEND_TIME")
	private Date sendTime;

	/**
	 * 创建时间 - D_CREATE
	 */
	@Column(name = "D_CREATE")
	private Date createTime;

	/**
	 * 状态 - S_STATE
	 */
	@Column(name = "S_STATE")
	private String state;

	public PassportVipPK getId() {
		return id;
	}

	public void setId(PassportVipPK id) {
		this.id = id;
	}

	public String getVipLevel() {
		return vipLevel;
	}

	public void setVipLevel(String vipLevel) {
		this.vipLevel = vipLevel;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public Integer getCumulateScore() {
		return cumulateScore;
	}

	public void setCumulateScore(Integer cumulateScore) {
		this.cumulateScore = cumulateScore;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Integer getCumulateMoney() {
		return cumulateMoney;
	}

	public void setCumulateMoney(Integer cumulateMoney) {
		this.cumulateMoney = cumulateMoney;
	}

	public Long getSendAid() {
		return sendAid;
	}

	public void setSendAid(Long sendAid) {
		this.sendAid = sendAid;
	}

	public Long getServerId() {
		return serverId;
	}

	public void setServerId(Long serverId) {
		this.serverId = serverId;
	}

	public String getSendVipLevel() {
		return sendVipLevel;
	}

	public void setSendVipLevel(String sendVipLevel) {
		this.sendVipLevel = sendVipLevel;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

}
