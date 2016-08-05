package com.woniu.sncp.imprest.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CARD_TYPE", schema = "SN_CARD")
public class ImprestCardType implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public enum State {
		VALID,INVALID
	}
	
	

	@Id
	@Column(name = "N_ID")
	protected Long id;
	
    /**
     * 卡类型名称 - S_CTYPE_NAME
     */
    @Column(name = "S_CTYPE_NAME")
    private String name;

    /**
     * 检索码 - S_HELPCODE
     */
    @Column(name = "S_HELPCODE")
    private String  searchCode;

    /**
     * 所属游戏ID - N_GAME_ID
     */
    @Column(name = "N_GAME_ID")
    private Long gameId;

    /**
     * 卡充值币种 - S_CURRENCY
     */
    @Column(name = "S_CURRENCY")
    private String currency;

    /**
     * 卡充值金额 - N_MONEY
     */
    @Column(name = "N_MONEY")
    private Long point;

    /**
     * 人民币原价(面值) - N_PRICE
     */
    @Column(name = "N_PRICE")
    private Float price;

    /**
     * 最低折扣价 - N_MIN_PRICE
     */
    @Column(name = "N_MIN_PRICE")
    private Float discountPrice;

    /**
     * 特殊属性 - S_PROPERTIES
     */
    @Column(name = "S_PROPERTIES")
    private String properties;

    /**
     * 状态 - S_STATE
     */
    @Column(name = "S_STATE")
    private String state;

    /**
     * 创建时间 - D_CREATE
     */
    @Column(name = "D_CREATE")
    private Date createDate;
    
    /**
     * 订单金额币种 - 参照币种定义表 - S_PRICE_CURRENCY
     */
    @Column(name = "S_PRICE_CURRENCY")
    private String priceCurrency;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSearchCode() {
		return searchCode;
	}

	public void setSearchCode(String searchCode) {
		this.searchCode = searchCode;
	}

	public Long getGameId() {
		return gameId;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Long getPoint() {
		return point;
	}

	public void setPoint(Long point) {
		this.point = point;
	}

	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

	public Float getDiscountPrice() {
		return discountPrice;
	}

	public void setDiscountPrice(Float discountPrice) {
		this.discountPrice = discountPrice;
	}

	public String getProperties() {
		return properties;
	}

	public void setProperties(String properties) {
		this.properties = properties;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getPriceCurrency() {
		return priceCurrency;
	}

	public void setPriceCurrency(String priceCurrency) {
		this.priceCurrency = priceCurrency;
	}
    
    
}
