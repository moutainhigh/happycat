package com.woniu.sncp.imprest.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


/**
 * 系统返点记录表 - SN_IMPREST.IMP_LARGESS_POINTS
 * 
 */
@Entity
@Table(name = "IMP_LARGESS_POINTS", schema = "SN_IMPREST")
@SequenceGenerator(name = "SEQ_GEN", sequenceName = "SN_IMPREST.IMP_LARGESS_POINTS_SQ")
public class LargessPoints implements Serializable {
	/**
	 * 序列化对象使用
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 主键ID - N_ID
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_GEN")
	@Column(name = "N_ID")
	private Long id;
	
    /**
     * 充入帐号ID - N_AID
     */
    @Column(name = "N_AID")
    private Long aid;

    /**
     * 充入币种 - S_CURRENCY
     */
    @Column(name = "S_CURRENCY")
    private String currency;

    /**
     * 充入点数 - N_AMOUNT
     */
    @Column(name = "N_AMOUNT")
    private Integer point;

    /**
     * 充入代点券 - N_TOKEN_AMOUNT
     */
    @Column(name = "N_TOKEN_AMOUNT")
    private Integer token;

    /**
     * 充入游戏分区ID - N_GAREA_ID 
     */
    @Column(name = "N_GAREA_ID")
    private Long gameAreaId;

    /**
     * 生成时间 - D_CREATE
     */
    @Column(name = "D_CREATE")
    private Date createDate;

    /**
     * 返点来源类型 - S_SOURCE_TYPE
     */
    @Column(name = "S_SOURCE_TYPE")
    private String sourceType;

    /**
     * 相关记录ID - N_RELATION_ID
     */
    @Column(name = "N_RELATION_ID")
    private Long relatedId;

    /**
     * 状态 - S_STATE
     */
    @Column(name = "S_STATE")
    private String state;

    public Long getAid() {
        return aid;
    }

    public void setAid(Long aid) {
        this.aid = aid;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getPoint() {
        return point;
    }

    public void setPoint(Integer point) {
        this.point = point;
    }

    public Integer getToken() {
        return token;
    }

    public void setToken(Integer token) {
        this.token = token;
    }

    public Long getGameAreaId() {
        return gameAreaId;
    }

    public void setGameAreaId(Long gameAreaId) {
        this.gameAreaId = gameAreaId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
    
}
