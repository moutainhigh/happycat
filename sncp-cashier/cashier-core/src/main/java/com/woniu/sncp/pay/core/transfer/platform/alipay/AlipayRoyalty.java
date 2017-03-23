package com.woniu.sncp.pay.core.transfer.platform.alipay;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.core.transfer.model.TransferModel;
import com.woniu.sncp.pay.core.transfer.platform.AbstractTransfer;
import com.woniu.sncp.pojo.payment.TransferOrder;

/**
 * 支付宝分润
 * 
 * @author luzz
 *
 */
@Service("alipayRoyalty")
public class AlipayRoyalty extends AbstractTransfer{

	@Override
	public boolean transferRequest(Platform platform,
			TransferModel transferModel, Map<String, Object> extParams) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("service", "distribute_royalty");
		params.put("partner", platform.getMerchantNo());
		params.put("_input_charset", _charset_encode);
		
		params.put("out_bill_no", transferModel.getReceiveOrderNo());
		params.put("royalty_type", "10");
		
		
		return false;
	}

	@Override
	public String requestParamsSign(Platform platform,
			Map<String, Object> inParams) throws ValidationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> backendParamsValidate(
			HttpServletRequest request, Platform platform)
			throws ValidationException, DataAccessException,
			PaymentRedirectException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void backendResponse(Map<String, Object> params,
			HttpServletResponse response, boolean isSccess) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean transferQuery(Platform platform,
			TransferOrder transferOrder, Map<String, Object> extParams) {
		// TODO Auto-generated method stub
		return false;
	}

}
