/**
 * 
 */
package com.woniu.sncp.profile.po;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * 游戏分区定义表 - GAME_AREA
 * @author fuzl
 *
 */
@Entity
@Table(name="GAME_AREA",schema="SN_PROFILE")
//@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, include = "all")
public class GameAreaPo implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7141594791393309011L;

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

    public GameAreaPo(Long id,String name,Long gameId){
        this.id = id ;
        this.name = name;
        this.gameId = gameId;
    };
	
	@Id
	@Column(name = "N_GAREA_ID")
	private Long id;
	
	/**
	 * 分区名称 - S_GAREA_NAME
	 */
	@Column(name = "S_GAREA_NAME")
	private String name;
	
	/**
	 * 所属游戏ID - N_GAME_ID
	 */
	@Column(name = "N_GAME_ID")
	private Long gameId;
	
	/**
	 * 所属分站DB_ID - N_SUBDB_ID
	 */
	@Column(name = "N_SUBDB_ID")
	private Long subdbId;
	
	/**
	 * 所属区域运营商ID - N_ISSUER_ID
	 */
	@Column(name = "N_ISSUER_ID")
	private Long issuerId;
	
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
	 * 移民限制 - S_EMIGRATE_LIMIT
	 */
	@Column(name = "S_EMIGRATE_LIMIT")
	private String emigrateLimit;
	
	/**
	 * 网络类型 - S_NET_TYPE
	 */
	@Column(name = "S_NET_TYPE")
	private String isp;
	
	/**
	 * 创建时间 - D_CREATE
	 */
	@Column(name = "D_CREATE")
	private Date createDate;
	
	/**
	 * 禁用时间 - D_CLOSE
	 */
	@Column(name = "D_CLOSE")
	private Date closeDate;
	
	/**
	 * 状态 - S_STATE
	 */
	@Column(name = "S_STATE")
	private String state;
	
	/**
	 * 组ID 
	 */
//	@Column(name = "n_group_id")
//	private Long groupId;
	
	/**
	 * 合入分区ID
	 */
	@Column(name = "N_MERGE_ID")
	private Long mergeId;
	
//	/**
//     * @ManyToOne：多对一,cascade：级联,
//      * fetch = FetchType.LAZY,延迟加载策略,如果不想延迟加载可以用FetchType.EAGER
//     */
    
    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name="n_group_id")
	private GameGroupPo gameGroupPo;
	
	public GameAreaPo() {
	}

	public GameAreaPo(Long id, String name, Long gameId, Long subdbId,
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
	
//	public Long getGroupId() {
//		return groupId;
//	}
//
//	public void setGroupId(Long groupId) {
//		this.groupId = groupId;
//	}

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

	public GameGroupPo getGameGroupPo() {
		return gameGroupPo;
	}

	public void setGameGroupPo(GameGroupPo gameGroupPo) {
		this.gameGroupPo = gameGroupPo;
	}
	
}
