package com.woniu.sncp.pay.core.transfer.platform;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.http.PayCheckUtils;
import com.woniu.sncp.pay.core.service.TransferOrderService;
import com.woniu.sncp.pay.core.transfer.model.TransferModel;


public abstract class AbstractTransfer implements Transfer{
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource
	protected TransferOrderService transferOrderService;
	
	@Override
	public boolean validateRequestSource(Platform platform,TransferModel transferModel,
			Map<String, Object> extParams) {
		if(StringUtils.isEmpty(platform.getTransferValidateUrl())){
			throw new ValidationException("请求验证地址不可以为空");
		}
		
		Map<String, Object> params = new HashMap<String, Object>();
		
		params.put("orderno", transferModel.getReceiveOrderNo());
		params.put("money", transferModel.getMoney());
		params.put("account", transferModel.getAccount());
		params.put("merchantid", transferModel.getMerchantId());
		params.put("platformid", transferModel.getPlatformId());
		
		String postResponse = PayCheckUtils.postRequst(platform.getTransferValidateUrl(), params, 3000, _charset_encode, "validateRequestSource");
		return new Boolean(postResponse);
	}
	
	/**
	 * 写出信息到response
	 * 
	 * @param response
	 * @param message
	 */
	public void responseAndWrite(HttpServletResponse response, String message) {
		PrintWriter writer = null;
		try {
			if (logger.isInfoEnabled())
				logger.info("返回给支付平台的信息：" + message);
			
			writer = response.getWriter();
			writer.print(message);
			writer.flush();
		} catch (IOException ex) {
			logger.error("返回给对方跳转的地址失败", ex);
		} finally {
			try {
				writer.close();
				writer = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return;
	}
	
	
}
