package com.woniu.sncp.pay.core.transfer.platform.alipay;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.common.utils.encrypt.Dsa;
import com.woniu.sncp.pay.common.utils.encrypt.EncryptFactory;
import com.woniu.sncp.pay.common.utils.encrypt.EncryptStringUtils;
import com.woniu.sncp.pay.common.utils.http.PayCheckUtils;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.tools.AlipayHelper;
import com.woniu.sncp.pay.core.transfer.model.TransferModel;
import com.woniu.sncp.pay.core.transfer.platform.AbstractTransfer;
import com.woniu.sncp.pojo.payment.TransferOrder;

/**
 * 付款到支付宝账户无密接口 不支持批量
 * 
 * 
 * @author luzz
 *
 */
@Service("alipayTransfer")
public class AlipayTransfer extends AbstractTransfer{
	
	@Override
	public boolean transferRequest(Platform platform,TransferModel transferModel, Map<String, Object> extParams) {
		try{
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("service", "batch_trans_notify_no_pwd");
			params.put("partner", platform.getMerchantNo());
			params.put("_input_charset", _charset_encode);
			
			params.put("notify_url", platform.getBehindUrl(transferModel.getMerchantId()));
			params.put("account_name", transferModel.getAccountInfo());
			String detailData = transferModel.getReceiveOrderNo()+"^"+transferModel.getAccount()
					+"^"+transferModel.getAccountInfo()+"^"+transferModel.getMoney()
					+"^"+transferModel.getReason();
			params.put("detail_data", detailData);
			params.put("batch_no", transferModel.getOrderNo());
			params.put("batch_num", "1");
			params.put("batch_fee", transferModel.getMoney());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			params.put("pay_date", sdf.format(new Date()));
			params.put("email", transferModel.getAccountInfo());
			
			params.put("sign", requestParamsSign(platform,params));
			params.put("sign_type", "DSA");
			
			String postResponse = PayCheckUtils.postRequst(platform.getTransferUrl(), params, 3000, _charset_encode, "AlipayTransfer");
			Document doc = null;
			try {
				doc = DocumentHelper.parseText(postResponse);
			} catch (DocumentException e) {
				throw new ValidationException("支付宝转账请求返回内容解析异常");
			}
			Node isSuccessNode = doc.selectSingleNode("//alipay/is_success");
			String isSuccess = isSuccessNode.getText();
			
			return "T".equalsIgnoreCase(isSuccess);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return false;
		}
	}
	
	@Override
	public String requestParamsSign(Platform platform,Map<String, Object> inParams)
			throws ValidationException {
		
		LinkedHashMap<String, Object> linkedHashMap = AlipayHelper.sortMap(inParams);
		String source = EncryptStringUtils.linkedHashMapToStringWithKey(linkedHashMap, true);
		String priKey = AlipayHelper.readText(platform.getPrivateUrl());
		
		String sign = null;
		try {
			sign = EncryptFactory.getInstance(Dsa.NAME).sign(source, priKey, _charset_encode);
		} catch (Exception e) {
			throw new ValidationException("支付宝加密失败", e);
		}
		if (logger.isInfoEnabled()) {
			logger.info("支付宝加密源：" + source);
			logger.info("支付宝加密结果：" + sign);
		}
		return sign;
	}

	@Override
	public Map<String, Object> backendParamsValidate(
			HttpServletRequest request, Platform platform)
			throws ValidationException, DataAccessException{
		//验证notify_url
		String notifyId = StringUtils.trim(request.getParameter("notify_id"));
		String partner = platform.getMerchantNo();
		String alipayNotifyURL = platform.getPayCheckUrl() + "?service=notify_verify&partner=" + partner + "&notify_id=" + notifyId;
		String responseTxt = AlipayHelper.checkURL(alipayNotifyURL);
		if (!"true".equals(responseTxt)) {
			logger.error("支付宝后台通知url验证异常,返回responseTxt=" + responseTxt+",notifyUrl:"+alipayNotifyURL);
			throw new ValidationException("支付宝后台通知url验证异常,返回responseTxt=" + responseTxt);
		}
		
		//订单验证
		String batchNo = StringUtils.trim(request.getParameter("batch_no"));
		TransferOrder queryOrder = transferOrderService.queryOrder(batchNo);
		Assert.notNull(queryOrder, "转账订单查询为空,orderNo:" + batchNo);
		
		//获取数据
		String sign = StringUtils.trim(request.getParameter("sign"));
		Map alipay = request.getParameterMap();
		Properties params = new Properties();
		for (Iterator<Entry<String, Object>> keyValuePairs = alipay.entrySet().iterator(); keyValuePairs.hasNext();) {
			Map.Entry<String, Object> entry = (Map.Entry<String, Object>) keyValuePairs.next();
			String key = entry.getKey();
			String value = request.getParameter(key);
			if (!"sign".equalsIgnoreCase(key) && !"sign_type".equalsIgnoreCase(key)) {
				try {
					params.put(key, URLDecoder.decode(value, _charset_encode));
				} catch (UnsupportedEncodingException e) {
					logger.error(e.getMessage(),e);
				}
			}
		}
		
		//验证签名
		String pubKey = AlipayHelper.readText(platform.getPublicUrl());
		boolean result = AlipayHelper.dsaCheck(params, pubKey, _charset_encode, sign);
		if (!result) {
			// 加密校验失败
			if (logger.isInfoEnabled()) {
				logger.info("==============支付宝后台回调参数加密处理失败=================");
				logger.info("我方加密参数：" + JsonUtils.toJson(params));
				logger.info("==============支付宝后台回调参数加密处理结束=================\n");
			}
			throw new ValidationException("支付平台回调参数加密校验失败");
		}
		
		Map<String, Object> returned = new HashMap<String, Object>();
		//判断状态  组装数据
		//success_details 流水号^收款方账号^收款账号姓名^付款金额^成功标识(S)^成功原因(null)^支付宝内部流水号^完成时间
		//fail_details 流水号^收款方账号^收款账号姓名^付款金额^失败标识(F)^失败原因^支付宝内部流水号^完成时间。
		String successDetails = StringUtils.trim(request.getParameter("success_details"));
		if(StringUtils.isBlank(successDetails)){
			String failDetails = StringUtils.trim(request.getParameter("fail_details"));
			String[] failDtlArr = failDetails.split("^");
			returned.put(PaymentConstant.OPPOSITE_MONEY,failDtlArr[3]);
			returned.put(PaymentConstant.TRANSFER_STATE,TransferOrder.TRANSFER_STATE_FAILED);
			returned.put(PaymentConstant.TRANSFER_STATE_MESSAGE, failDtlArr[5]);
			returned.put(PaymentConstant.OPPOSITE_ORDERNO,failDtlArr[6]);
			returned.put(PaymentConstant.TRANSFER_ACCOUNT, failDtlArr[1]);
		} else {
			String[] successDtlArr = successDetails.split("^");
			returned.put(PaymentConstant.OPPOSITE_MONEY,successDtlArr[3]);
			returned.put(PaymentConstant.TRANSFER_STATE,"S".equalsIgnoreCase(successDtlArr[4])?TransferOrder.TRANSFER_STATE_COMPLETED:TransferOrder.TRANSFER_STATE_NOT_COMPLETED);
			returned.put(PaymentConstant.TRANSFER_STATE_MESSAGE, successDtlArr[5]);
			returned.put(PaymentConstant.OPPOSITE_ORDERNO,successDtlArr[6]);
			returned.put(PaymentConstant.TRANSFER_ACCOUNT, successDtlArr[1]);
		}
	
		return returned;
	}

	@Override
	public void backendResponse(Map<String, Object> params,
			HttpServletResponse response, boolean isSccess) {
		if (isSccess)
			super.responseAndWrite(response, "success");
		else
			super.responseAndWrite(response, "fail");
	}

	@Override
	public boolean transferQuery(Platform platform,
			TransferOrder order, Map<String, Object> extParams) {
		
		TransferModel transferModel = new TransferModel();
		transferModel.setMerchantId(order.getMerchantId());
		transferModel.setOrderNo(order.getOrderNo());
		transferModel.setReceiveOrderNo(order.getReceiveOrderNo());
		transferModel.setAccount(order.getReceiveAccount());
		transferModel.setAccountInfo(order.getReceiveAccountInfo());
		transferModel.setReason(order.getReason());
		transferModel.setMoney(String.valueOf(order.getMoney()));
		
		boolean transferRequest = this.transferRequest(platform, transferModel, null);
		return transferRequest;
	}
	
}