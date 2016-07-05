package com.woniu.sncp.profile.po;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * 游戏服务器定义
 * @author fuzl
 *
 */
@Entity
@Table(name = "GAME_SERVER", schema = "SN_PROFILE")
//@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class GameServerPo implements Serializable{
	/**
	 * 序列化对象使用
	 */
	private static final long serialVersionUID = 1L;
	
    /**
     *  禁用
     */
	public static final String SERVER_STATE_DISABLED = "0";

    /**
     * 正常
     */
	public static final String SERVER_STATE_USUAL = "1";

    /**
     * 维护中
     */
	public static final String SERVER_STATE_FIXING = "2";

    @Id
    @Column(name = "N_GSERVER_ID")
    private Long id;
    
    /**
     * 服务器名称 - S_GSERVER_NAME
     */
    @Column(name = "S_GSERVER_NAME")
    private String name;

    /**
     * 所属游戏ID - N_GAME_ID
     */
    @Column(name = "N_GAME_ID")
    private Long gameId;

    /**
     * 所属分区ID - N_GSERVER_ID
     */
    @Column(name = "N_GAREA_ID")
    private Long gameAreaId;

    /**
     * 服务器IP - S_IP
     */
    @Column(name = "S_IP")
    private String ip;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getGameAreaId() {
		return gameAreaId;
	}

	public void setGameAreaId(Long gameAreaId) {
		this.gameAreaId = gameAreaId;
	}

	public Long getGameId() {
		return gameId;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}


}
