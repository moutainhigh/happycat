package com.woniu.sncp.ploy.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity()
@Table(name = "BUSINESS_LOG", schema = "SN_SALES")
@SequenceGenerator(name = "SEQ_GEN", sequenceName = "SN_SALES.BUSINESS_LOG_SQ", allocationSize = 1)
public class PloyBusinessLog implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id()
	@Column(name = "N_ID")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GEN")
	private Long id;

	/**
	 * 类型
	 */
	@Column(name = "S_TYPE_ID")
	private String typeId;

	/**
	 * 相关业务记录ID - N_RELATED_ID
	 */
	@Column(name = "N_RELATED_ID")
	private Long relatedId;

	/**
	 * 业务数据 - S_BUSINESS_DATA
	 */
	@Column(name = "S_BUSINESS_DATA")
	private String businessData;

	/**
	 * 相关帐号 - N_AID
	 */
	@Column(name = "N_AID")
	private Long userId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public Long getRelatedId() {
		return relatedId;
	}

	public void setRelatedId(Long relatedId) {
		this.relatedId = relatedId;
	}

	public String getBusinessData() {
		return businessData;
	}

	public void setBusinessData(String businessData) {
		this.businessData = businessData;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

}
