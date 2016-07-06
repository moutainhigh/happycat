package com.woniu.sncp.profile.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * 帐号赠送活动配置表 - PP_PRESENTS_PLOY
 * 
 * @author wujian
 * @since 1.0
 * @date 2010-2-4
 */
public class PassportPresentsPloyDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 状态 - 0 - 禁用
	 */
	public static final String STATE_INVALID = "0";
	
	/**
	 * 状态 - 1 - 编辑中
	 */
	public static final String STATE_EDIT = "1";
	
	/**
	 * 状态 - 2 - 审核中
	 */
	public static final String STATE_VERIFY = "2";
	
	/**
	 * 状态 - 3 - 启用
	 */
	public static final String STATE_VALID = "3";
	
	/**
	 * 主键ID - N_ID
	 */
	protected Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	/**
     * 活动名称 - S_PLOY_NAME
     */
    private String name;

    /**
     * 活动类型 - S_TYPE
     */
    private String type;

    /**
     * 活动描述 - S_PLOY_DESC
     */
    private String desc;

    /**
     * 限制游戏ID - S_LIMIT_GAME
     * 	限制活动游戏：
     * 	为空 – 不限制
     * 	其它：只有字段内游戏参加活动，以逗号分隔
     */
    private String limitGame;
    
    /**
     * 限制运营商ID - S_LIMIT_OPERATOR
     * 	为空 - 不限制帐号所属运营商参与活动
     * 	其它 - 限制列表内运营商，允许还是排除可由过程判断，以逗号分隔
     * 	(尽量使用允许，以防新加的也满足活动)		
     */
    private String limitIssuer;
    
    /**
     * 限制支付平台ID - S_LIMIT_AGENT
     * 	为空 - 不限制帐号所属运营商参与活动
     * 	其它 - 限制列表内运营商，允许还是排除可由过程判断，以逗号分隔
     * 	(尽量使用允许，以防新加的也满足活动)		
     */
    private String limitPaymentPlatform;
    
    /**
     * 其它限制内容 - S_LIMIT_CONTENT
     * 	格式为 名称:内容, 如： imprestMoney:100,
     * 	一次性充值、累计充值、累计消费	
     */
    private String otherLimitContent;

	/**
     * 是否限制时间 - S_IS_LIMIT
     * 	(活动是否有时间限制)
     */
    private String isLimitTime;

    /**
     * 活动开始时间 - D_START
     */
    private Date startDate;

    /**
     * 活动截止时间 - D_END
     */
    private Date endDate;
    
    /**
     * 备注说明 - S_NOTE
     */
    private String note;

    /**
     * 状态 - S_STATE
     */
    private String state;
    
    /**
     * 道具描述
     */
    private String propDesc;
    
    /**
     * 创建人 - N_CREATE
     */
    private Long creatorId;

    /**
     * 创建时间 - D_CREATE
     */
    private Date createDate;

    /**
     * 审核人 - N_AUDITOR
     */
    private Long auditorId;

    /**
     * 审核时间 - D_ATIME
     */
    private Date auditDate;

    public String getPropDesc() {
		return propDesc;
	}

	public void setPropDesc(String propDesc) {
		this.propDesc = propDesc;
	}

	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLimitGame() {
		return limitGame;
	}

	public void setLimitGame(String limitGame) {
		this.limitGame = limitGame;
	}

	public String getLimitIssuer() {
		return limitIssuer;
	}

	public void setLimitIssuer(String limitIssuer) {
		this.limitIssuer = limitIssuer;
	}

	public String getLimitPaymentPlatform() {
		return limitPaymentPlatform;
	}

	public void setLimitPaymentPlatform(String limitPaymentPlatform) {
		this.limitPaymentPlatform = limitPaymentPlatform;
	}

	public String getOtherLimitContent() {
		return otherLimitContent;
	}

	public void setOtherLimitContent(String otherLimitContent) {
		this.otherLimitContent = otherLimitContent;
	}

	public String getIsLimitTime() {
        return isLimitTime;
    }

    public void setIsLimitTime(String isLimitTime) {
    	this.isLimitTime = isLimitTime;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Long getAuditorId() {
        return auditorId;
    }

    public void setAuditorId(Long auditorId) {
        this.auditorId = auditorId;
    }

    public Date getAuditDate() {
        return auditDate;
    }

    public void setAuditDate(Date auditDate) {
        this.auditDate = auditDate;
    }

}