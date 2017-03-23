package com.woniu.sncp.pay.core.transfer.constants;

public interface TransferConstants {

	final static String TRANSFER_RESULT = "result";
	final static String DB_RESULT = "db_result";
	final static String TRANSFER_RESULT_MSG = "msg";

	final static String TRANSFER_SUCCESS_AND_CALLBACK_SUCCESS = "11";// 转账成功，通知成功，请等待异步通知结果
	final static String TRANSFER_SUCCESS_AND_CALLBACK_FAILED = "12";// 转账成功，通知失败，请重新发起请求
	final static String TRANSFER_SUCCESS_AND_NOT_CALLBACK = "13";// 转账成功，未通知，请重新发起请求
	final static String TRANSFER_FAILED_AND_CALLBACK_SUCCESS = "21";// 转账失败，通知成功，请等待异步通知结果
	final static String TRANSFER_FAILED_AND_CALLBACK_FAILED = "22";// 转账失败，通知失败，请重新发起请求
	final static String TRANSFER_FAILED_AND_NOT_CALLBACK = "23";// 转账失败，未通知，请重新发起请求
	final static String TRANSFER_REQUEST_SUCCESS = "31";// 转账请求发送成功，请等待异步通知
	final static String TRANSFER_REQUEST_FAILED = "32";// 转账请求发送失败，请重新发起请求
	final static String TRANSFER_REQUEST_NOT_SEND = "33";// 转账请求未发送失败，请重新发起请求
	final static String TRANSFER_OTHER_ERROR = "41";// 其他错误，查看错误消息，请重新发起请求
	
	final static String DB_SUCCESS = "1";// 更新数据库成功
	final static String DB_FAILED = "0";// 数据库出错
	
	final static String CALLBACK_SUCCESS = "success";
	final static String CALLBACK_FAILED = "failed";
	
}
