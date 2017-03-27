package com.woniu.sncp.pay.repository.queue;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * 
 * <p>descrption: mysql任务队列表</p>
 * 
 * @author fuzl
 * @date   2016年12月12日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
@Entity
@Table(name = "QUE_MESSAGE_QUEUE", schema = "SN_QUEUE")
public class MessageQueue implements Serializable {
	
	/**
	 * 序列化对象使用
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 主键ID - N_ID
	 */
	@Id
	@Column(name = "N_ID")
	protected Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * 分站ID - N_SUBIDC_ID
	 */
	@Column(name = "N_SUBIDC_ID")
	private Long subdbId;
	
	/**
	 * 相关ID - N_RELATION_ID
	 */
	@Column(name = "N_RELATION_ID")
	private Long relationId;
	
	/**
	 * 相关数据 - S_BUSINESS_DATA
	 */
	@Column(name = "S_BUSINESS_DATA")
	private String businessData;
	
	/**
	 * - S_REMARK
	 */
	@Column(name = "S_REMARK")
	private String remark;
	
	/**
	 *  - N_PIECE_ID
	 */
	@Column(name = "N_PIECE_ID")
	private Long pieceId;
	
	/**
	 * 创建时间 - D_CREATE
	 */
	@Column(name = "D_CREATE")
	private Date createDate;
	
	/**
	 * 类型 - N_TASK_TYPE
	 */
	@Column(name = "N_TYPE")
	private Long taskType;
	
	/**
	 * 商户号 - N_MERCHANT_ID
	 */
	@Column(name = "N_MERCHANT_ID")
	private Long merchantId;

	public Long getSubdbId() {
		return subdbId;
	}

	public void setSubdbId(Long subdbId) {
		this.subdbId = subdbId;
	}

	public Long getRelationId() {
		return relationId;
	}

	public void setRelationId(Long relationId) {
		this.relationId = relationId;
	}

	public String getBusinessData() {
		return businessData;
	}

	public void setBusinessData(String businessData) {
		this.businessData = businessData;
	}

	public Long getPieceId() {
		return pieceId;
	}

	public void setPieceId(Long pieceId) {
		this.pieceId = pieceId;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Long getTaskType() {
		return taskType;
	}

	public void setTaskType(Long taskType) {
		this.taskType = taskType;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Long getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(Long merchantId) {
		this.merchantId = merchantId;
	}
	
}
