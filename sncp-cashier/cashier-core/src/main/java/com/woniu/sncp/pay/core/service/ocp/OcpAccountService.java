package com.woniu.sncp.pay.core.service.ocp;

import java.util.Map;

public interface OcpAccountService {
	
	/**
	 * 查询余额(SDK方式) 
	 * @param params
	 * @return
	 */
	Map<String, Object> queryAmount(Map<String, Object> params);
	
	/**
	 * 查询余额(HTTP POST)
	 * @param params
	 * @return
	 */
	Map<String, Object> queryAmount2(Map<String, Object> params);
	
	/**
	 * 扣费(HTTP POST)
	 * @param params
	 * @return
	 */
	Map<String, Object> chargeAmount(Map<String, Object> params);
	
	/**
	 * 验证代金卷是否需要发送验证码
	 * @param djj 代金卷，参数格式：curr1,10#curr2,20
	 * @return true 需要，false 不需要
	 */
	boolean isSmsCheck(String djj);
	
	/**
	 * 增加余额
	 * @param params
	 * @return
	 */
	Map<String, Object> addAmount(Map<String, Object> params);
	
	/**
	 * 直接扣费
	 * @param params
	 * @return
	 */
	Map<String, Object> deductAmount(Map<String, Object> params);

}
