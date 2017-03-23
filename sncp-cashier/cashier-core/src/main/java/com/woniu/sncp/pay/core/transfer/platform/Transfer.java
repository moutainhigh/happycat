package com.woniu.sncp.pay.core.transfer.platform;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.dao.DataAccessException;

import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.core.transfer.model.TransferModel;
import com.woniu.sncp.pojo.payment.TransferOrder;

/**
 * 转账
 * 
 * @author luzz
 *
 */
public interface Transfer {

	String _charset_encode = "utf-8";
	

	/**
	 * 转账请求
	 * 
	 * @param requestUrl
	 * @param params
	 * @return
	 */
	boolean transferRequest(Platform platform,TransferModel transferModel,Map<String,Object> extParams);
	
	/**
	 * 验证转账请求来源
	 * 
	 * @param paymentPlatform
	 * @param params
	 * @return
	 */
	boolean validateRequestSource(Platform platform,TransferModel transferModel,Map<String,Object> extParams);
	
	/**
	 * 转账请求参数组装
	 * 
	 * @param inParams
	 * @return
	 * @throws ValidationException
	 */
	String requestParamsSign(Platform platform,Map<String, Object> inParams)
			throws ValidationException;

	
	/**
	 * 回调参数验证
	 * 
	 * @param request
	 * @param paymentPlatform
	 * @return
	 * @throws ValidationException
	 * @throws DataAccessException
	 * @throws PaymentRedirectException
	 */
	Map<String, Object> backendParamsValidate(HttpServletRequest request,
			Platform platform) throws ValidationException,
			DataAccessException, PaymentRedirectException;
	
	
	/**
	 * 回调响应
	 * 
	 * @param params
	 * @param response
	 * @param isSccess
	 */
	void backendResponse(Map<String,Object> params, HttpServletResponse response, boolean isSccess);
	
	/**
	 * 查询接口
	 * 
	 * @param platform
	 * @param transferModel
	 * @param extParams
	 * @return true 请求成功
	 */
	boolean transferQuery(Platform platform,TransferOrder transferOrder,Map<String,Object> extParams);
}
