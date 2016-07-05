package com.woniu.sncp.profile.po;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * 
 * @author fuzl
 *
 */
@Entity
@Table(name = "GAME_GROUP", schema = "SN_PROFILE")
public class GameGroupPo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6913098840935672743L;

	/**
	 * 禁用
	 */
	public static final String STATE_CLOSED = "0";

	/**
	 * 正常
	 */
	public static final String STATE_NORMAL = "1";

	/**
	 * 测试
	 */
	public static final String STATE_TEST = "2";

	@Id
	@Column(name = "n_group_id")
	private Long id;

	/**
	 * 组名称
	 */
	@Column(name = "s_group_name")
	private String groupName;

	/**
	 * 游戏id
	 */
	@Column(name = "n_game_id")
	private Long gameId;

	/**
	 * 组类型
	 */
	@Column(name = "s_type")
	private String type;

	/**
	 * 排序字段
	 */
	@Column(name = "n_sequence")
	private Long sequence;

	/**
	 * 创建时间
	 */
	@Column(name = "d_create")
	private Date createDate;

	/**
	 * 关闭时间
	 */
	@Column(name = "d_close")
	private Date closeDate;

	/**
	 * 状态
	 */
	@Column(name = "s_state")
	private String state;
	
	//一对多
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "gameGroupPo")
	private Set<GameAreaPo> gameAreaSet = new HashSet<GameAreaPo>();
	
	public GameGroupPo() {
	}

	public GameGroupPo(Long id, String groupName, Long gameId, String type,
			Long sequence, Date createDate, Date closeDate, String state) {
		super();
		this.id = id;
		this.groupName = groupName;
		this.gameId = gameId;
		this.type = type;
		this.sequence = sequence;
		this.createDate = createDate;
		this.closeDate = closeDate;
		this.state = state;
	}


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Long getGameId() {
		return gameId;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getSequence() {
		return sequence;
	}

	public void setSequence(Long sequence) {
		this.sequence = sequence;
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

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return this.getId() + "=" + this.getGroupName();
	}

	public Set<GameAreaPo> getGameAreaSet() {
		return gameAreaSet;
	}

	public void setGameAreaSet(Set<GameAreaPo> gameAreaSet) {
		this.gameAreaSet = gameAreaSet;
	}
	
}
