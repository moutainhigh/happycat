package com.woniu.sncp.imprest.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 支付平台
 * @author chenyx
 *
 */
@Entity
@Table(name = "IMP_PAY_PLATFORM", schema = "SN_IMPREST")
public class PaymentPlatform implements Serializable {
	/**
	 * 序列化值
	 */
	private static final long serialVersionUID = 1L;
/**
     * 支付平台ID (手动添加.添加范围为:100-999)- N_ID
     */
    @Id
	@Column(name = "N_ID")
    private Long id;

    /**
     * 平台简称 - S_NAME
     */
    @Column(name = "S_NAME")
	private String name;

    /**
     * 公司名称 - S_COMPANY_NAME
     */
    @Column(name = "S_COMPANY_NAME")
	private String companyName;

    /**
     * 平台类型 - S_TYPE
     */
    @Column(name = "S_TYPE")
	private String type;

    /**
     * 通信密码 - S_PASSWD
     * 混合运营调用过程使用-由对方平台传给我们，我们调用过程时判断是否为对方平台
     */
    @Column(name = "S_PASSWD")
	private String password;

    /**
     * 可用预付余额 - N_BALANCE
     */
    @Column(name = "N_BALANCE")
	private Integer balance;

    /**
     * 对方系统我方商户号 - S_USER_ID
     */
    @Column(name = "S_USER_ID")
	private String merchantId;

    /**
     * 对方系统我方密码 - S_USER_PASSWD - 用于订单加密和后台验证加密校验
     */
    @Column(name = "S_USER_PASSWD")
	private String merchantPwd;

    /**
     * 提交订单至对方URL - S_PAY_URL
     */
    @Column(name = "S_PAY_URL")
	private String paymentUrl;

    /**
     * 提交订单校验KEY - S_PAY_KEY
     * orderNo + 该值md5后，提交对方平台（通过对方提供的自定义参数），再传回来验证
     * 如果平台不提供自定义参数，则放在后台返回地址的参数里面
     */
    @Column(name = "S_PAY_KEY")
	private String authKey;

    /**
     * 对方后台验证接口 - S_PAY_CHECK_URL 
     * 订单验证的对方地址
     */
    @Column(name = "S_PAY_CHECK_URL")
	private String payCheckUrl;

    /**
     * 我方前端接收处理URL - S_FRONT_URL
     */
    @Column(name = "S_FRONT_URL")
	private String frontUrl;

    /**
     * 我方后台接收处理URL - S_BEHIND_URL
     */
    @Column(name = "S_BEHIND_URL")
	private String behindUrl;

    /**
     * 调用我方接口限制IP - S_LIMIT_IP
     */
    @Column(name = "S_LIMIT_IP")
	private String legalIp;

    /**
     * 备注 - S_NOTE
     */
    @Column(name = "S_NOTE")
	private String note;

    /**
     * 联系电话 - S_PHONE
     */
    @Column(name = "S_PHONE")
	private String phone;

    /**
     * 联系人 - S_CONTACT
     */
    @Column(name = "S_CONTACT")
	private String contact;

    /**
     * 状态 - S_STATE
     */
    @Column(name = "S_STATE")
	private String state;

    /**
     * 对方管理页面URL - S_MANAGE_URL
     */
    @Column(name = "S_MANAGE_URL")
	private String manageUrl;

    /**
     * 对方管理用户名 - S_MANAGE_USER
     */
    @Column(name = "S_MANAGE_USER")
	private String manageUser;

    /**
     * 对方管理密码 - S_MANAGE_PWD
     */
    @Column(name = "S_MANAGE_PWD")
	private String managePwd;
    
    /**
     * 第三方异步通知密钥
     */
    @Column(name = "S_PAYPARTNER_PASSWD")
	private String partnerPwd;

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

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getBalance() {
		return balance;
	}

	public void setBalance(Integer balance) {
		this.balance = balance;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getMerchantPwd() {
		return merchantPwd;
	}

	public void setMerchantPwd(String merchantPwd) {
		this.merchantPwd = merchantPwd;
	}

	public String getPaymentUrl() {
		return paymentUrl;
	}

	public void setPaymentUrl(String paymentUrl) {
		this.paymentUrl = paymentUrl;
	}

	public String getAuthKey() {
		return authKey;
	}

	public void setAuthKey(String authKey) {
		this.authKey = authKey;
	}

	public String getPayCheckUrl() {
		return payCheckUrl;
	}

	public void setPayCheckUrl(String payCheckUrl) {
		this.payCheckUrl = payCheckUrl;
	}

	public String getFrontUrl() {
		return frontUrl;
	}

	public void setFrontUrl(String frontUrl) {
		this.frontUrl = frontUrl;
	}

	public String getBehindUrl() {
		return behindUrl;
	}

	public void setBehindUrl(String behindUrl) {
		this.behindUrl = behindUrl;
	}

	public String getLegalIp() {
		return legalIp;
	}

	public void setLegalIp(String legalIp) {
		this.legalIp = legalIp;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getManageUrl() {
		return manageUrl;
	}

	public void setManageUrl(String manageUrl) {
		this.manageUrl = manageUrl;
	}

	public String getManageUser() {
		return manageUser;
	}

	public void setManageUser(String manageUser) {
		this.manageUser = manageUser;
	}

	public String getManagePwd() {
		return managePwd;
	}

	public void setManagePwd(String managePwd) {
		this.managePwd = managePwd;
	}

	public String getPartnerPwd() {
		return partnerPwd;
	}

	public void setPartnerPwd(String partnerPwd) {
		this.partnerPwd = partnerPwd;
	}
    
    
}
