package com.woniu.sncp.vip.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * 通行证帐号VIP信息表-主键
 * 
 * @author wujian
 * @since 1.0
 */
@Embeddable
public class PassportVipPK implements Serializable {

	/**
	 * 序列化值
	 */
	private static final long serialVersionUID = 1L;
	
	/**
     * 帐号ID - N_AID
     */
    @Column(name = "N_AID")
    private Long aid;

    /**
     * 游戏ID - N_GAME_ID
     */
    @Column(name = "N_GAME_ID")
    private Long gameId;

    public Long getAid() {
        return aid;
    }

    public void setAid(Long aid) {
        this.aid = aid;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }
}
