package com.woniu.sncp.pay.common.utils;

import java.util.Map;

/**
 * 退款常量字符
 */
public class RefundmentConstant {
	/**
	 * 网银直充键值对
	 */
	private Map<String,String> webBankMap;
	
	/**
	 * 京东网银支付 对应银行编码 键值对
	 */
	private Map<String, Object> jdCyberBankMap;

	/**
	 * 常量字符串 - 官方运营商ID - 007
	 */
	public static final String OFFICIAL_ISSUER_ID = "007";
	
	/**
	 * 常量字符串 - 订单校验的支付IP
	 */
	public static final String PAYMENT_ORDER_CHECK_IP = "58.210.76.18";
	
	/**
	 * 常量字符串 - 支付订单POJO - imprestOrder
	 */
	public static final String PAYMENT_ORDER = "paymentOrder";
	
	/**
	 * 常量字符串 - 退款订单POJO - imprestOrder
	 */
	public static final String REFUNDMENT_ORDER = "refundmentOrder";
	/**
	 * 常量字符串 - 退款批次单POJO - payRefundBatch
	 */
	public static final String REFUNDMENT_BATCH = "refundBatch";
	/**
	 * 常量字符串 - 退款成功笔数- REFUND_SUCCESS_NUM
	 */
	public static final String REFUND_SUCCESS_NUM = "successnum";
	/**
	 * 常量字符串 - 退款结果集- REFUND_RESULT_DETAILS
	 */
	public static final String REFUND_RESULT_DETAILS = "resultdetails";
	
	/**
	 * 常量字符串 - 批次号 - batchno
	 */
	public static final String BATCHNO = "batchno";
	/**
	 * 常量字符串 - 业务方批次号 - partnerBatchNo
	 */
	public static final String PARTNER_BATCHNO = "partnerbatchno";
	
	/**
	 * 常量字符串 - 第三方批次号 - oppositeBatchNo
	 */
	public static final String OPPOSITE_BATCHNO = "oppositebatchno";
	
	/**
	 * 常量字符串 - 退款单状态标识 - REFUND_STATUS_FLAG
	 */
	public static final String REFUND_STATUS_FLAG ="statusFlag";
	
	/**
	 * 常量字符串 - 我方订单号 - orderNo
	 */
	public static final String ORDER_NO = "orderNo";
	
	/**
	 * 常量字符串 - 对方支付平台给我方分配的ID - merchant_id
	 */
	public static final String MERCHANT_ID = "merchant_id";
	
	/**
	 * 常量字符串 - 对方订单号 - oppositeOrderNo
	 */
	public static final String OPPOSITE_ORDERNO = "oppositeOrderNo";
	
	/**
	 * 常量字符串 - 对方传过来的交易金额 - oppositeMoney
	 */
	public static final String OPPOSITE_MONEY = "oppositeMoney";
	
	/**
	 * 常量字符串 - 对方支付服务器IP - payIp
	 */
	public static final String PAY_IP = "payIp";

	/**
	 * 常量字符串 - 客户端(玩家或购买人)IP - clientIp
	 */
	public static final String CLIENT_IP = "clientIp";
	
	public static final String HTTP_REQUEST = "request";
	
	/**
	 * 常量字符串 - 支付平台POJO - paymentPlatform
	 */
	public static final String PAYMENT_PLATFORM = "paymentPlatform";

//	/**
//	 * 常量字符串 - 实际支付平台 - imprestPayment
//	 */
//	public static final String PAYMENT = "payment";
//	
//	public static final String PAYMENT_MODE = "paymentMode";
//	
//	// =============================================
//	/**
//	 * 常量字符串 - 支付状态 - paymentState
//	 */
//	public static final String PAYMENT_STATE = "paymentState";
	
	
	/**
	 * 常量字符串 - 实际退款平台 - imprestRefundment
	 */
	public static final String REFUNDMENT = "refundment";
	
	public static final String REFUNDMENT_MODE = "refundmentMode";
	
	// =============================================
	/**
	 * 常量字符串 - 退款状态 - refundmentState
	 */
	public static final String REFUNDMENT_STATE = "refundstate";
	
	/**
	 * 支付结果(后台返回和订单校验使用) - 支付成功 - 1
	 */
	public static final String PAYMENT_STATE_PAYED = "1";

	/**
	 * 支付结果(后台返回和订单校验使用) - 支付失败 - 2
	 */
	public static final String PAYMENT_STATE_FAILED = "2";
	
	/**
	 * 支付结果(后台返回和订单校验使用) - 未支付 - 3
	 */
	public static final String PAYMENT_STATE_NOPAYED = "3";
	
	/**
	 * 支付结果(后台返回和订单校验使用) - 查询异常或验证失败 - 4
	 */
	public static final String PAYMENT_STATE_QUERY_ERR = "4";
	
	/**
	 * 退款结果 - 未退款  1
	 */
	public static final String PAYMENT_STATE_REFUND_INIT = "1";
	/**
	 * 退款结果 - 退款成功  2
	 */
	public static final String PAYMENT_STATE_REFUNDED = "2";
	/**
	 * 退款结果 - 退款处理中  3
	 */
	public static final String PAYMENT_STATE_REFUND_PRO = "3";
	/**
	 * 退款结果 - 退款失败
	 */
	public static final String PAYMENT_STATE_REFUND_FAILED = "4";
	
	/**
	 * 退款结果 - (后台返回和订单校验使用) - 查询异常或验证失败(退款表使用) - 6
	 */
	public static final String PAYMENT_STATE_REFUND_ERR = "6";
	
	
	
	/**
	 * 充值产品类型(提交订单时输入) - 神州行 - SZX
	 */
	public static final String PAYMENT_PRODUCTION_TYPE_SZX = "SZX";
	/**
	 * 充值产品类型(提交订单时输入) - 联通 - UNICOM
	 */
	public static final String PAYMENT_PRODUCTION_TYPE_UNICOM = "UNICOM";
	/**
	 * 充值产品类型(提交订单时输入) - 电信 - TELECOM
	 */
	public static final String PAYMENT_PRODUCTION_TYPE_TELECOM = "TELECOM";

	/**
	 * 交易处理类型，trade_refund 退款交易，trade_pay 支付交易
	 */
	public static final String TRADE_ACTION_REFUND = "trade_refund";
	public static final String TRADE_ACTION_PAY = "trade_pay";
	

	

	public Map<String, Object> getJdCyberBankMap() {
		return jdCyberBankMap;
	}

	public void setJdCyberBankMap(Map<String, Object> jdCyberBankMap) {
		this.jdCyberBankMap = jdCyberBankMap;
	}
	
	public Map<String, String> getWebBankMap() {
		return webBankMap;
	}

	public void setWebBankMap(Map<String, String> webBankMap) {
		this.webBankMap = webBankMap;
	}
}
