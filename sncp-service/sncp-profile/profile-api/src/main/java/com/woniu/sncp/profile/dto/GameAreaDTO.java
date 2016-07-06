/**
 * 
 */
package com.woniu.sncp.profile.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * 游戏分区定义表 - GAME_AREA
 * @author fuzl
 *
 */
public class GameAreaDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8959590060046375833L;

	/**
	 * 官方运营
	 */
	public static final Long ISSUE_OFFICAL = 7L;

	/**
	 * 电信
	 */
	public static final String ISP_TELECOM = "1";

	/**
	 * 网通
	 */
	public static final String ISP_CNC = "2";

	/**
	 * 免费分区
	 */
	public static final String CHARGE_FREE = "0";

	/**
	 * 包月收费 (购买一段时长,期间任意玩)
	 */
	public static final String CHARGE_RENT = "1";

	/**
	 * 即时扣点收费 (玩多久就扣相应点数)
	 */
	public static final String CHARGE_BY_TIME = "2";
	
	/**
	 * 包月优先即时扣点混合收费
	 */
	public static final String CHARGE_BY_TIME_RENT_FIRST = "3";
	
	/**
	 * 禁用
	 */
	public static final String STATE_CLOSED = "0";
	
	/**
	 * 正常
	 */
	public static final String STATE_NORMAL = "1";
	
	/**
	 * 维护中
	 */
	public static final String STATE_SUSPEND = "2";
	
	/**
	 * 	合服
	 */
	public static final String STATE_MERGE = "3";
	
	/**
	 * 不需要单独激活(此时的激活方式参考游戏的激活方式)
	 */
	public static final String ACTIVATION_AUTO = "0";
	
	/**
	 * 须单独手动激活
	 */
	public static final String ACTIVATION_MANUAL = "1";
	
	/**
	 * 须单独激活码激活
	 */
	public static final String ACTIVATION_MANUAL_WITH_SN = "2";

    public GameAreaDTO(Long id,String name,Long gameId){
        this.id = id ;
        this.name = name;
        this.gameId = gameId;
    };
	
	private Long id;
	
	/**
	 * 分区名称 - S_GAREA_NAME
	 */
	private String name;
	
	/**
	 * 所属游戏ID - N_GAME_ID
	 */
	private Long gameId;
	
	/**
	 * 所属分站DB_ID - N_SUBDB_ID
	 */
	private Long subdbId;
	
	/**
	 * 所属区域运营商ID - N_ISSUER_ID
	 */
	private Long issuerId;
	
	/**
	 * 收费类型 - S_CHARGE_TYPE
	 */
	private String chargeType;
	
	/**
	 * 激活方式 - S_ACTIVE_MODE
	 */
	private String activeMode;
	
	/**
	 * 移民限制 - S_EMIGRATE_LIMIT
	 */
	private String emigrateLimit;
	
	/**
	 * 网络类型 - S_NET_TYPE
	 */
	private String isp;
	
	/**
	 * 创建时间 - D_CREATE
	 */
	private Date createDate;
	
	/**
	 * 禁用时间 - D_CLOSE
	 */
	private Date closeDate;
	
	/**
	 * 状态 - S_STATE
	 */
	private String state;
	
	/**
	 * 组ID 
	 */
	private Long groupId;
	
	/**
	 * 合入分区ID
	 */
	private Long mergeId;

	public GameAreaDTO() {
	}

	public GameAreaDTO(Long id, String name, Long gameId, Long subdbId,
			Long issuerId, String state) {
		this.id = id;
		this.name = name;
		this.gameId = gameId;
		this.subdbId = subdbId;
		this.issuerId = issuerId;
		this.state = state;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getActiveMode() {
		return activeMode;
	}

	public void setActiveMode(String activeMode) {
		this.activeMode = activeMode;
	}

	public String getChargeType() {
		return chargeType;
	}

	public void setChargeType(String chargeType) {
		this.chargeType = chargeType;
	}

	public String getEmigrateLimit() {
		return emigrateLimit;
	}

	public void setEmigrateLimit(String emigrateLimit) {
		this.emigrateLimit = emigrateLimit;
	}

	public Long getGameId() {
		return gameId;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}

	public String getIsp() {
		return isp;
	}

	public void setIsp(String isp) {
		this.isp = isp;
	}

	public Long getIssuerId() {
		return issuerId;
	}

	public void setIssuerId(Long issuerId) {
		this.issuerId = issuerId;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Long getSubdbId() {
		return subdbId;
	}

	public void setSubdbId(Long subdbId) {
		this.subdbId = subdbId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getCloseDate() {
		return closeDate;
	}

	public void setCloseDate(Date closeDate) {
		this.closeDate = closeDate;
	}
	
	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	public Long getMergeId() {
		return mergeId;
	}

	public void setMergeId(Long mergeId) {
		this.mergeId = mergeId;
	}

	@Override
	public String toString() {
		return this.getId() + "=" + this.getName();
	}
}
