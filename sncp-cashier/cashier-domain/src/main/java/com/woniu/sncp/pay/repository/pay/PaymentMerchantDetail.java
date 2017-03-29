package com.woniu.sncp.pay.repository.pay;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;


/**
 * 支付业务申请渠道表
 * 
 * @author luzz
 *
 */
@Entity
@Table(name = "PAY_MERCHANT_DTL", schema = "SN_PAY")
public class PaymentMerchantDetail implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6140708026380050662L;

	/**
	 * 第三方支付平台
	 */
	public final static String TYPE_THIRD = "T";

	public final static String TYPE_ANDROID = "A";//android
	public final static String TYPE_IOD = "I";//ios
	
	/**
	 * 借记卡
	 */
	public final static String TYPE_DEBIT = "D";
	/**
	 * 信用卡
	 */
	public final static String TYPE_CREDIT = "C";
	/**
	 * WAP
	 */
	public final static String TYPE_WAP = "W";
	
	/**
	 * 银行卡
	 */
	public final static String TYPE_BANK = "B";
	
	/**
	 * 信用卡分期
	 */
	public final static String TYPE_CREDIT_STAGE = "S";
	
	/**
	 * 兔兔币支付,pc/wap/android/ios
	 */
	public final static String TYPE_TTB_PC = "qp";
	public final static String TYPE_TTB_WAP = "qi";
	public final static String TYPE_TTB_ANDROID = "qa";
	public final static String TYPE_TTB_IOS = "qw";
	
	/**
	 * 游戏充值卡
	 */
	public final static String TYPE_YX_CARD = "G";
	
	/**
	 * 手机充值卡
	 */
	public final static String TYPE_YX_MOBILE = "M";
	
	/**
	 * 快钱快捷wap-借记卡
	 */
	public final static String TYPE_QUICK_BANK_DEBIT = "Y";
	
	/**
	 * 快钱快捷wap-信用卡
	 */
	public final static String TYPE_QUICK_BANK_CREDIT = "Z";
	
	/**
	 * 游戏充值卡wap
	 */
	public final static String TYPE_WN_GAME_WAP_CARD = "GW";
	
	/**
	 * 手机充值卡wap
	 */
	public final static String TYPE_WN_MOBILE_WAP_CARD = "MW";

	/**
	 * 蜗牛充值卡wap
	 */
	public final static String TYPE_WN_QMOBILE_WAP_CARD = "QW";
	
	/**
	 * 蜗牛移动充值卡
	 */
	public final static String TYPE_WN_MOBILE_SPEC_CARD = "QM";
	
	/**
	 * 余额支付（WEB）
	 */
	public final static String TYPE_WEB_YUE = "E";
	
	/**
	 * 余额支付（WAP）
	 */
	public final static String TYPE_WAP_YUE = "F";
	
	/**
	 * 余额支付（ISO）
	 */
	public final static String TYPE_IOS_YUE = "BI";
	
	/**
	 * 余额支付（安卓）
	 */
	public final static String TYPE_AND_YUE = "BA";
	
	/**
	 * 主键
	 */
	@Id
    @Column(name = "N_ID")
	private Long id;
	/**
	 * 支付业务申请号
	 */
	@Column(name="N_MERCHANT_ID")
	private long merchantId;
	/**
	 * 平台类型  D借记卡/C信用卡/T第三方支付 
	 */
	@Column(name="S_TYPE")
	private String type;
	/**
	 * 银行名称或支付平台名称 
	 */
	@Column(name="S_NAME")
	private String name;
	/**
	 * 银行编码 或 渠道编号(支付平台id)
	 */
	@Column(name="S_CONTENT")
	private String content;
	/**
	 * 银行对应平台id，逗号分隔160,172
	 */
	@Column(name="S_BANK_PLATFORM_ID")
	private String bankPlatformId;
	
	/**
	 * 显示标记, N 标识new
	 */
	@Column(name="S_DISP_FLAG")
	private String dispFlag;

	/**
	 * 状态 
	 */
	@Column(name="S_STATUS")
	private String status;
	
	
	/**
	 * 页面显示 平台id
	 */
	@Transient
	private String platformId;
	
	/**
	 * 借记卡支持的支付类型(网银、快捷支付)
	 */
	@Column(name="S_DEBIT_PAY_TYPE")
	private String debitPayType;
	
	/**
	 * 信用卡支持的支付类型(网银、快捷支付)
	 */
	@Column(name="S_CREDIT_PAY_TYPE")
	private String creditPayType;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(long merchantId) {
		this.merchantId = merchantId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getBankPlatformId() {
		return bankPlatformId;
	}
	public void setBankPlatformId(String bankPlatformId) {
		this.bankPlatformId = bankPlatformId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getPlatformId() {
		return platformId;
	}
	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}
	public String getDispFlag() {
		return dispFlag;
	}
	public void setDispFlag(String dispFlag) {
		this.dispFlag = dispFlag;
	}
	public String getDebitPayType() {
		return debitPayType;
	}
	public void setDebitPayType(String debitPayType) {
		this.debitPayType = debitPayType;
	}
	public String getCreditPayType() {
		return creditPayType;
	}
	public void setCreditPayType(String creditPayType) {
		this.creditPayType = creditPayType;
	}

}
