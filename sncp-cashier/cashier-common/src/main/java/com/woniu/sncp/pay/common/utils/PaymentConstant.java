package com.woniu.sncp.pay.common.utils;

import java.util.Map;

/**
 * 支付常量字符
 */
public class PaymentConstant {
	
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
	
	/**
	 * 常量字符串 - 支付平台POJO - paymentPlatform
	 */
	public static final String PAYMENT_PLATFORM = "paymentPlatform";

	/**
	 * 常量字符串 - 实际支付平台 - imprestPayment
	 */
	public static final String PAYMENT = "payment";
	
	public static final String PAYMENT_MODE = "paymentMode";
	
	// =============================================
	/**
	 * 常量字符串 - 支付状态 - paymentState
	 */
	public static final String PAYMENT_STATE = "paymentState";
	
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
	 * 
	 * 已退款
	 */
	public static final String PAYMENT_STATE_QUERY_ERR = "4";
	
	/**
	 * 支付结果(后台返回和订单校验使用) - 已经退款 - 5
	 */
	public static final String PAYMENT_STATE_REIMBURSE = "5";
	
	/**
	 * 支付结果 支付成功 回调业务地址返回失败
	 */
	public static final String PAYMENT_STATE_CALL_FAILED = "6";
	
	/**
	 * 取消订单
	 */
	public static final String PAYMENT_STATE_CANCEL = "7";
	
	/**
	 * 支付订单已提交,处理中
	 */
	public static final String PAYMENT_STATE_PROCESS = "8";
	
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
	
	//转账
	/**
	 * 转账订单
	 */
	public static final String TRANSFER_ORDER = "transferOrder";
	
	/**
	 * 转账状态
	 */
	public static final String TRANSFER_STATE = "transferState";
	/**
	 * 转账成功失败原因
	 */
	public static final String TRANSFER_STATE_MESSAGE = "transferStateMsg";
	
	public static final String TRANSFER_ACCOUNT = "transferAccount";
	
	/**
	 * 消息推送任务详情
	 */
	public static final String MESSAGE_PUSH_TASK_OBJ = "taskObj";
	
	/**
	 * 消息推送任务 类型
	 */
	public static final String MESSAGE_PUSH_TASK_TYPE = "taskType";
	
	/**
	 * 常量字符串 - 业务方订单号 - partnerOrderNo
	 */
	public static final String PARTNER_ORDERNO = "partnerOrderNo";
	/**
	 * 常量字符串 - 交易金额 - orderMoney
	 */
	public static final String ORDER_MONEY = "orderMoney";
	/**
	 * 常量字符串 - 蜗牛通行证id - aid
	 */
	public static final String ORDER_ACCOUNT_ID = "aid";
	/**
	 * 常量字符串 - 服务器id - serverId
	 */
	public static final String SERVER_ID = "serverId";
	/**
	 * 常量字符串 - 游戏id - gameId
	 */
	public static final String GAME_ID = "gameId";
	/**
	 * 蜗牛游戏一卡通
	 */
	public static final String WN_GAME_CARD = "1";
	/**
	 * 蜗牛移动充值卡
	 */
	public static final String WN_MOBILE_CARD = "WM";
	/**
	 * 蜗牛移动全能卡
	 */
	public static final String WN_QMOBILE_CARD = "SM";
	
	/**
	 * android 手机充值卡
	 */
	public static final String A_MOBILE_CARD = "AM";
	/**
	 * android 蜗牛移动卡
	 */
	public static final String A_WNMOBILE_CARD = "AQ";
	/**
	 * ios 手机充值卡
	 */
	public static final String I_MOBILE_CARD = "IM";
	/**
	 * ios 蜗牛移动卡
	 */
	public static final String I_WNMOBILE_CARD = "IQ";

	/**
	 * 收银台支付类型,PC、WAP、ios、android
	 */
	public static final String PAYMENT_PC = "PC";
	public static final String PAYMENT_WAP = "WAP";
	public static final String PAYMENT_IOS = "ios";
	public static final String PAYMENT_ANDROID = "android";
	/**
	 * APP类型 Android:A iOS:I
	 */
	public static final String PAYMENT_TYPE_IOS = "I";
	public static final String PAYMENT_TYPE_ANDROID = "A";
	
	
	/**
	 * 支付类型,直接支付:directPay;预支付prePay
	 */
	public static final String PAYMENT_TYPE_DIRECT_PAY = "directPay";
	public static final String PAYMENT_TYPE_PRE_PAY = "prePay";
	
	/**
	 * 超时时间,毫秒
	 */
	public static final String PAYMENT_CALL_TIMEOUT = "timeout";
	
	/**
	 * PC快钱快捷银行编码映射，解决某些银行编码，快钱不识别的问题
	 * @author fuzl@snail.com
	 * @date 2015-11-05
	 */
	public Map<String, String> kqBankCodeMap;
	
	public Map<String, String> getKqBankCodeMap() {
		return kqBankCodeMap;
	}

	public void setKqBankCodeMap(Map<String, String> kqBankCodeMap) {
		this.kqBankCodeMap = kqBankCodeMap;
	}

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
