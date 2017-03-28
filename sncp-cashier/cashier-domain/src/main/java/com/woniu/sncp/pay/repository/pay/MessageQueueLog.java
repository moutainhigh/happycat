package com.woniu.sncp.pay.repository.pay;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * 
 * <p>descrption: 
 * 异步任务记录表 - SN_QUEUE.QUE_MESSAGE_LOG</p>
 * 
 * @author fuzl
 * @date   2016年12月12日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
@Entity
@Table(name = "QUE_MESSAGE_LOG", schema = "SN_PAY")
public class MessageQueueLog implements Serializable {

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
     * 业务类型 - S_TASK_TYPE
     */
    @Column(name = "N_TYPE")
    private Long type;
    
    /**
     * 帐号Id
     */
    @Column(name = "N_AID")
    private Long accountId;
    
    /**
     * 相关业务记录ID - N_OPERATION_ID
     */
    @Column(name = "N_RELATION_ID")
    private Long relatedId;

    /**
     * 业务所需数据 - S_TASK_OBJ
     */
    @Column(name = "S_BUSINESS_DATA")
    private String taskObj;

    /**
     * 创建时间 - D_CREATE
     */
    @Column(name = "D_CREATE")
    private Date createDate;

    /**
     * 客户端IP
     */
    @Column(name = "N_IP")
    private Long clientIP;
    
    /**
	 * 商户号 - N_MERCHANT_ID
	 */
	@Column(name = "N_MERCHANT_ID")
	private Long merchantId;
	
	public Long getType() {
		return type;
	}

	public void setType(Long type) {
		this.type = type;
	}

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public Long getRelatedId() {
		return relatedId;
	}

	public void setRelatedId(Long relatedId) {
		this.relatedId = relatedId;
	}

	public String getTaskObj() {
		return taskObj;
	}

	public void setTaskObj(String taskObj) {
		this.taskObj = taskObj;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Long getClientIP() {
		return clientIP;
	}

	public void setClientIP(Long clientIP) {
		this.clientIP = clientIP;
	}

	public Long getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(Long merchantId) {
		this.merchantId = merchantId;
	}
}
