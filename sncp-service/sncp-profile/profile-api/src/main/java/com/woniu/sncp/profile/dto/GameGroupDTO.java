package com.woniu.sncp.profile.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 游戏网络类型分组
 * @author fuzl
 *
 */
public class GameGroupDTO implements Serializable {

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

	private Long id;

	/**
	 * 组名称
	 */
	private String groupName;

	/**
	 * 游戏id
	 */
	private Long gameId;

	/**
	 * 组类型
	 */
	private String type;

	/**
	 * 排序字段
	 */
	private Long sequence;

	/**
	 * 创建时间
	 */
	private Date createDate;

	/**
	 * 关闭时间
	 */
	private Date closeDate;

	/**
	 * 状态
	 */
	private String state;
	
	//一对多
	private Set<GameAreaDTO> gameAreaSet = new HashSet<GameAreaDTO>();
	
	public GameGroupDTO() {
	}

	public GameGroupDTO(Long id, String groupName, Long gameId, String type,
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

	public Set<GameAreaDTO> getGameAreaSet() {
		return gameAreaSet;
	}

	public void setGameAreaSet(Set<GameAreaDTO> gameAreaSet) {
		this.gameAreaSet = gameAreaSet;
	}

}
