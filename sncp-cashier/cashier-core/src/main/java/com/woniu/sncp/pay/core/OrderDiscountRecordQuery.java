package com.woniu.sncp.pay.core;

 
import java.util.Date;
  
public class OrderDiscountRecordQuery  {

 	private int pageNumber=1;
 	private int pageSize=10;
 
	protected Long id;

	/**
	 * 收银台使用，支付申请号
	 */
 
	private Long discountId;

	/**
	 * 收银台使用，支付申请号
	 */
 	private Long merchantId;

	/**
	 * 收银台渠道商户号
	 */
 	private Long paymentId;

	/**
	 * 充值订单号 - S_ORDER_NO
	 */
 	private String orderNo;

//	/**
//	 * 对方订单号 - S_OTHER_ORDER_NO
//	 */
// 	private String payPlatformOrderId;

	/**
	 * 第三方订单号 S_PAYPARTNER_OTHER_ORDER_NO
	 */
 	private String partnerOrderNo;
 
 
	/**
	 * 订单生成时间 D_CREATE
	 */
 
    private Date createStartDate;
 
    private Date createEndDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getDiscountId() {
		return discountId;
	}

	public void setDiscountId(Long discountId) {
		this.discountId = discountId;
	}

	public Long getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(Long merchantId) {
		this.merchantId = merchantId;
	}

	public Long getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(Long paymentId) {
		this.paymentId = paymentId;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getPartnerOrderNo() {
		return partnerOrderNo;
	}

	public void setPartnerOrderNo(String partnerOrderNo) {
		this.partnerOrderNo = partnerOrderNo;
	}

 

	public Date getCreateStartDate() {
		return createStartDate;
	}

	public void setCreateStartDate(Date createStartDate) {
		this.createStartDate = createStartDate;
	}

	public Date getCreateEndDate() {
		return createEndDate;
	}

	public void setCreateEndDate(Date createEndDate) {
		this.createEndDate = createEndDate;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

 
    
}