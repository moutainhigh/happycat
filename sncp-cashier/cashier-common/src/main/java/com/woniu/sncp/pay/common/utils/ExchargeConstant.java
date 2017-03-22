package com.woniu.sncp.pay.common.utils;


/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2015年12月18日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public class ExchargeConstant {
	
	/**
	 * 常量字符串 - 充值模式
	 */
	public static final String IMPREST_MODE = "1";
	
	/**
	 * 常量字符串 - 我方订单号 - orderNo
	 */
	public static final String ORDER_NO = "orderNo";
	
	/**
	 * 常量字符串 - 对方支付平台给我方分配的ID - merchant_id
	 */
	public static final String MERCHANT_ID = "merchant_id";
	
	/**
	 * 常量字符串 - 支付平台POJO - paymentPlatform
	 */
	public static final String PAYMENT_PLATFORM = "paymentPlatform";
	
	/**
	 * 常量字符串 - 支付订单POJO - imprestOrder
	 */
	public static final String PAYMENT_ORDER = "paymentOrder";
	
	/**
	 * 常量字符串 - 扣卡状态 - deducttState
	 */
	public static final String DEDUCT_STATE = "deductState";
	/**
	 * 扣卡结果 - 未扣卡 - 0
	 */
	public static final String DEDUCT_STATE_CREATE = "0";
	/**
	 * 扣卡结果(后台返回和订单校验使用) - 扣卡成功 - 1
	 */
	public static final String DEDUCT_STATE_DEDUCTED = "1";
	/**
	 * 扣卡结果(后台返回和订单校验使用) - 扣卡失败 - 2
	 */
	public static final String DEDUCT_STATE_FAILED = "2";
	/**
	 * 扣卡结果(后台返回和订单校验使用) - 未扣卡 - 3
	 */
	public static final String DEDUCT_STATE_NODEDUCTED = "3";
	/**
	 * 调度任务详情
	 */
	public static final String RECHARGE_TASK_OBJ = "taskObj";
	
	/**
	 * 调度任务 类型
	 */
	public static final String RECHARGE_TASK_TYPE = "taskType";
	/**
	 * 调度任务 1 立即执行，还是0 延时执行
	 */
	public static final String RECHARGE_TASK_SUBDBID = "subdbId";
	
	
	
	/**
	 * 扣卡返回订单号
	 */
	public static final String DEDUCT_OTHER_ORDERNO = "deductOrderNo";
	
	
	/**
	 * 常量字符串 - 充值状态 - imprestState
	 */
	public static final String IMPREST_STATE = "imprestState";
	/**
	 * 充值结果 - 未充值 - 0
	 */
	public static final String IMPREST_STATE_CREATE = "0";
	/**
	 * 充值结果(后台返回和订单校验使用) - 充值成功 - 1
	 */
	public static final String IMPREST_STATE_IMPRESTED = "1";
	/**
	 * 充值结果(后台返回和订单校验使用) - 充值失败 - 2
	 */
	public static final String IMPREST_STATE_FAILED = "2";
	/**
	 * 充值结果(后台返回和订单校验使用) - 未充值 - 3
	 */
	public static final String IMPREST_STATE_NOIMPRESTED = "3";
	
	/**
	 * 充值中心订单号
	 */
	public static final String OPPOSITE_ORDERNO = "oppositeOrderno";
	/**
	 * 常量字符串 - 对方支付服务器IP - payIp
	 */
	public static final String PAY_IP = "payIp";
	
	
	
	
	
	
	
	
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
	 */
	public static final String PAYMENT_STATE_QUERY_ERR = "4";
	
	
	
	
}
