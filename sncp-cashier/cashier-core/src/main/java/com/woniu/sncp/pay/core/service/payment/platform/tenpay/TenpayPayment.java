package com.woniu.sncp.pay.core.service.payment.platform.tenpay;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.common.utils.encrypt.EncryptStringUtils;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.alipay.tools.AlipayHelper;
import com.woniu.sncp.pojo.payment.PaymentOrder;

/**
 * <pre>
 * 财付通支付平台 - 直接公网测试 - 低于1元的交易可以没有IP和域名限制
 * 
 * 总金额：以分为单位
 * 特殊：在生成订单的时候有2个订单，我方和对方的，因此这里有我方和对方订单转化方法
 * 特殊：md5加密，有大写有小写，故比对时不区分大小写
 * 生成订单和后台校验都是顺序排列，订单校验是按照a-z升序排列
 * </pre>
 * 
 * @author yang.hao
 * @since 2011-5-12 上午10:57:14
 * 
 */
@Service("tenpayPayment")
public class TenpayPayment extends AbstractPayment {

	/**
	 * 财付通支付编码
	 */
	private final String _charset_encode = "UTF-8";
	/**
	 * 财付通支付币种，固定值
	 */
	private static final String _fee_type = "1";

	@Override
	public String encode(Map<String, Object> inParams) throws ValidationException {
		String source = (String) inParams.get("source");
		String encrypted = StringUtils.lowerCase(MD5Encrypt.encrypt(source));
		if (logger.isInfoEnabled()) {
			logger.info("=========财付通支付加密开始=========");
			logger.info("source：" + source);
			logger.info("encrypted：" + encrypted);
			logger.info("=========财付通支付加密结束=========\n");
		}
		return encrypted;
	}
	
	public String encode(String orderNo, String checkKey) {
		return MD5Encrypt.encrypt(orderNo + checkKey);
	}

	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams) throws ValidationException {
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);

		// 1.拼装参数
		LinkedHashMap<String, Object> linkedParams = new LinkedHashMap<String, Object>();
		linkedParams.put("cmdno", 1);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		String date = dateFormat.format(paymentOrder.getCreateDate());
		linkedParams.put("date", date);

		linkedParams.put("bargainor_id", platform.getMerchantNo()); // 商户ID

		// 财付通需要的订单号
		linkedParams.put("transaction_id", our2tenpay(platform.getMerchantNo(), paymentOrder.getOrderNo()));
		linkedParams.put("sp_billno", paymentOrder.getOrderNo()); // 我方格式订单号
		BigDecimal money = new BigDecimal(paymentOrder.getMoney().toString());
		linkedParams.put("total_fee", money.multiply(new BigDecimal(100)).intValue());// 总金额，单位分
		linkedParams.put("fee_type", 1); // 人民币
		linkedParams.put("return_url", platform.getBehindUrl(paymentOrder.getMerchantId())); // 后台返回地址
		linkedParams.put("attach", encode(paymentOrder.getOrderNo(), platform.getBackendKey()));
		linkedParams.put("spbill_create_ip", inParams.get(PaymentConstant.CLIENT_IP)); // 用户IP（非商户服务器IP），为了防止欺诈，支付时财付通会校验此IP
		linkedParams.put("key", platform.getPayKey()); // 加密key

		// 空值不加入
		String source = EncryptStringUtils.linkedHashMapToStringWithKey(linkedParams, true);
		// 2.参数加密
		Map<String, Object> encodeParams = new HashMap<String, Object>();
		encodeParams.put("source", source);
		String encrypted = this.encode(encodeParams);
		linkedParams.put("sign", encrypted); // md5

		// 3.移除加密key
		linkedParams.remove("key");

		// 4.剩余需要传递参数
		linkedParams.put("bank_type", 0);
		linkedParams.put("desc", inParams.get("productName")); // 交易的商品名称
		// 用不到，用户(买方)的财付通帐户(QQ或EMAIL),如无法获取，填空值
		// linkedParams.put("purchaser_id", null);

		linkedParams.put("payUrl", platform.getPayUrl()); // 提交给对方的支付地址
		linkedParams.put("cs", _charset_encode); // 提交给对方的支付编码

		return linkedParams;
	}

	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request, Platform platform)
			throws ValidationException, DataAccessException, PaymentRedirectException {
		// 加密校验，必须保证顺序
		LinkedHashMap<String, Object> encryParams = new LinkedHashMap<String, Object>();

		String payResult = StringUtils.trim(request.getParameter("pay_result"));
		String oppositeOrderNo = StringUtils.trim(request.getParameter("transaction_id"));
		String paymentMoney = StringUtils.trim(request.getParameter("total_fee"));
		String orderNo = StringUtils.trim(request.getParameter("sp_billno"));
		String oppositeEncrypted = StringUtils.trim(request.getParameter("sign"));
		String attach = StringUtils.trim(request.getParameter("attach")); // 附件

		encryParams.put("cmdno", 1);
		encryParams.put("pay_result", payResult);
		encryParams.put("date", StringUtils.trim(request.getParameter("date")));
		// 加密里未用到
		// encryParams.put("bargainor_id",
		// StringUtils.trim(request.getParameter("bargainor_id")));
		encryParams.put("transaction_id", oppositeOrderNo);
		encryParams.put("sp_billno", orderNo);
		encryParams.put("total_fee", StringUtils.trim(request.getParameter("total_fee")));
		encryParams.put("fee_type", StringUtils.trim(request.getParameter("fee_type")));
		encryParams.put("attach", attach);
		encryParams.put("key", platform.getPayKey());

		String source = EncryptStringUtils.linkedHashMapToStringWithKey(encryParams, true);
		Map<String, Object> encodeParams = new HashMap<String, Object>();
		encodeParams.put("source", source);
		String encrypted = this.encode(encodeParams);

		if (!encrypted.equalsIgnoreCase(oppositeEncrypted)) {
			if (logger.isInfoEnabled()) {
				logger.info("==============财付通后台加密处理失败=================");
				logger.info("我方加密串：" + encrypted);
				logger.info("对方加密串：" + oppositeEncrypted);
				logger.info("==============财付通后台加密处理结束=================\n");
			}
			throw new ValidationException("支付平台加密校验失败");
		}

		// 我们提交的参数原样返回校验 (orderNo + key MD5值)
		if (!encode(orderNo, platform.getBackendKey()).equals(attach)) {
			if (logger.isInfoEnabled()) {
				logger.info("==============财付通订单orderNo + key校验失败=================");
				logger.info("我方原文：" + orderNo + platform.getBackendKey());
				logger.info("对方传回扩展信息：" + attach);
				logger.info("==============财付通订单orderNo + key校验结束=================\n");
			}
			throw new ValidationException("财付通订单orderNo + key校验失败");
		}

		// 订单查询
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
		Assert.notNull(paymentOrder, "支付订单查询为空,orderNo:" + orderNo);

		Map<String, Object> returned = new HashMap<String, Object>();
		if ("0".equals(payResult)) { // 支付成功
			logger.info("财付通返回支付成功");
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
		} else { // 未支付 - 只有2个状态 0 和 非0
			logger.info("财付通返回未支付,pay_info:" + request.getParameter("pay_info"));
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
		}

		// 设置充值类型 - 不传则默认1-网银支付
		returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returned.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo);
		returned.put(PaymentConstant.OPPOSITE_MONEY, paymentMoney);
		return returned;
	}

	@Override
	public void paymentReturn(Map<String, Object> inParams, HttpServletResponse response, boolean isImprestedSuccess) {
		HttpServletRequest request = (HttpServletRequest) inParams.get("request");
		Platform platform = platformService.queryPlatform(Long.valueOf(String.valueOf(inParams.get("merchantid"))), Long.valueOf(String.valueOf(inParams.get("paymentId"))));
		StringBuffer show_url = new StringBuffer(platform.getFrontUrl(Long.valueOf(String.valueOf(inParams.get("merchantid")))))
				.append("?sp_billno=").append(request.getParameter("sp_billno")).append("&transaction_id=")
				.append(request.getParameter("transaction_id"));
		StringBuffer strHtml = new StringBuffer("<html><head>\r\n");
		if (isImprestedSuccess)
			strHtml.append("<meta name=\"TENCENT_ONLINE_PAYMENT\" content=\"China TENCENT\">\r\n");

		strHtml.append("<script language=\"javascript\">\r\n" + "window.location.href='").append(show_url)
				.append("';\r\n</script>\r\n").append("</head><body>").append(isImprestedSuccess ? "充值成功" : "充值失败")
				.append("</body></html>");

		super.responseAndWrite(response, strHtml.toString());
	}

	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		// 1.加密，是按照 a-z 升序排列
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		String date = dateFormat.format(paymentOrder.getCreateDate());

		String attach = encode(paymentOrder.getOrderNo(), platform.getBackendKey());
		String bargainor_id = platform.getMerchantNo();
		String cmdno = "2";
		String output_xml = "1";
		String sp_billno = paymentOrder.getOrderNo();
		String transaction_id = our2tenpay(platform.getMerchantNo(), paymentOrder.getOrderNo());

		LinkedHashMap<String, Object> linkedParams = new LinkedHashMap<String, Object>();
		linkedParams.put("attach", attach);
		linkedParams.put("bargainor_id", bargainor_id); // 商户ID
		linkedParams.put("charset", _charset_encode);
		linkedParams.put("cmdno", cmdno);
		linkedParams.put("date", date);
		linkedParams.put("output_xml", output_xml);
		linkedParams.put("sp_billno", sp_billno);
		linkedParams.put("transaction_id", transaction_id);

		// 升序排序
		LinkedHashMap<String, Object> sortMap = AlipayHelper.sortMap(linkedParams);
		sortMap.put("key", platform.getPayKey());

		Map<String, Object> encodeParams = new HashMap<String, Object>();
		encodeParams.put("source", EncryptStringUtils.linkedHashMapToStringWithKey(sortMap, true));
		String encrypted = this.encode(encodeParams);

		// 2.向财付通请求参数
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(platform.getPayCheckUrl());

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("attach", attach));
		params.add(new BasicNameValuePair("bargainor_id", bargainor_id));
		params.add(new BasicNameValuePair("charset", _charset_encode));
		params.add(new BasicNameValuePair("cmdno", cmdno));
		params.add(new BasicNameValuePair("date", date));
		params.add(new BasicNameValuePair("output_xml", output_xml));
		params.add(new BasicNameValuePair("sp_billno", sp_billno));
		params.add(new BasicNameValuePair("transaction_id", transaction_id));
		params.add(new BasicNameValuePair("sign", encrypted));

		UrlEncodedFormEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(params, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		httpPost.setEntity(entity);

		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = null;
		try {
			responseBody = httpclient.execute(httpPost, responseHandler);
		} catch (Exception e) {
			throw new ValidationException("财付通订单校验返回失败", e);
		}
		httpclient.getConnectionManager().shutdown();

		if (logger.isInfoEnabled())
			logger.info("财付通订单校验返回：" + responseBody);

		if (StringUtils.isBlank(responseBody)) {
			throw new ValidationException("财付通订单验证返回responseBody为空");
		}

		// 3.请求返回
		Document document = null;
		try {
			document = DocumentHelper.parseText(responseBody);
		} catch (DocumentException e) {
			throw new ValidationException("财付通订单校验xml转换异常", e);
		}

		String _attach = "";
		String _bargainor_id = "";
		String _cmdno = "";
		String _date = "";
		String _pay_info = "";
		String _pay_result = "";

		String _sp_billno = "";
		String _total_fee = "";
		String _transaction_id = "";
		String _sign = "";
		String _retmsg = "";

		@SuppressWarnings("unchecked")
		List<Node> nodes = document.selectNodes("//root/*");
		for (Node node : nodes) {
			if ("attach".equals(node.getName()))
				_attach = node.getText();
			else if ("bargainor_id".equals(node.getName()))
				_bargainor_id = node.getText();
			else if ("cmdno".equals(node.getName()))
				_cmdno = node.getText();
			else if ("date".equals(node.getName()))
				_date = node.getText();
			else if ("pay_info".equals(node.getName()))
				_pay_info = node.getText();
			else if ("pay_result".equals(node.getName()))
				_pay_result = node.getText();
			else if ("sp_billno".equals(node.getName()))
				_sp_billno = node.getText();
			else if ("total_fee".equals(node.getName()))
				_total_fee = node.getText();
			else if ("transaction_id".equals(node.getName()))
				_transaction_id = node.getText();
			else if ("sign".equals(node.getName()))
				_sign = node.getText();
			else if ("retmsg".equals(node.getName()))
				_retmsg = node.getText();
		}

		Map<String, Object> outParams = new HashMap<String, Object>();

		// 5.pay_result是有记录的时候存在，即retcode=0才会有的
		String payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
		if ("0".equals(_pay_result)) { // 支付成功
			// 4.返回信息校验
			LinkedHashMap<String, Object> returnLinkedParams = new LinkedHashMap<String, Object>();
			returnLinkedParams.put("attach", _attach);
			returnLinkedParams.put("bargainor_id", _bargainor_id); // 商户ID
			returnLinkedParams.put("cmdno", _cmdno);
			returnLinkedParams.put("date", _date);
			returnLinkedParams.put("fee_type", _fee_type);
			returnLinkedParams.put("pay_info", _pay_info); // check 和文档一致？
			returnLinkedParams.put("pay_result", _pay_result);
			returnLinkedParams.put("sp_billno", _sp_billno);
			returnLinkedParams.put("total_fee", _total_fee);
			returnLinkedParams.put("transaction_id", _transaction_id);

			// 升序排序
			LinkedHashMap<String, Object> _sortMap = AlipayHelper.sortMap(returnLinkedParams);
			_sortMap.put("key", platform.getPayKey());

			Map<String, Object> _encodeParams = new HashMap<String, Object>();
			_encodeParams.put("source", EncryptStringUtils.linkedHashMapToStringWithKey(_sortMap, true));
			String returnEncrypted = this.encode(_encodeParams);
			
			if (!_sign.equalsIgnoreCase(returnEncrypted)) {
				if (logger.isInfoEnabled()) {
					logger.info("==============财付通订单校验返回加密处理失败=================");
					logger.info("我方加密串：" + returnEncrypted);
					logger.info("对方加密串：" + _sign);
					logger.info("==============财付通订单校验返回加密处理结束=================\n");
				}
				outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
				return outParams;
			}

			// 我们提交的参数原样返回校验 (orderNo + key MD5值)
			if (!encode(_sp_billno, platform.getBackendKey()).equals(attach)) {
				if (logger.isInfoEnabled()) {
					logger.info("==============财付通订单orderNo + key校验失败=================");
					logger.info("我方原文：" + _sp_billno + platform.getBackendKey());
					logger.info("对方传回扩展信息：" + attach);
					logger.info("==============财付通订单orderNo + key校验结束=================\n");
				}
				outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
				return outParams;
			}

			payState = PaymentConstant.PAYMENT_STATE_PAYED;
		} else {
			payState = PaymentConstant.PAYMENT_STATE_NOPAYED;
			logger.info("财付通返回未成功支付，pay_info:" + _pay_info + ",retmsg:" + _retmsg);
		}

		// 设置充值类型 - 不传则默认1-网银支付
		outParams.put(PaymentConstant.OPPOSITE_ORDERNO, _transaction_id); // 对方订单号
		outParams.put(PaymentConstant.PAYMENT_STATE, payState);
		outParams.put(PaymentConstant.OPPOSITE_MONEY, _total_fee); // 总金额，对方传回的单位已经是分
		return outParams;
	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		String ourOrderNo = request.getParameter("sp_billno");
		String tenpayOrderNo = request.getParameter("transaction_id");

		String translatedOurOrderNo = tenpay2Our(String.valueOf(paymentService.findPaymentIdByPayment(this)),
				tenpayOrderNo);
		if (StringUtils.isNotBlank(ourOrderNo) && !ourOrderNo.equals(translatedOurOrderNo)) {
			logger.error("财付通订单号转换为我方订单号与传递过来的我方订单号不符");
			return null;
		}

		return translatedOurOrderNo;
	}

	/**
	 * 我方订单号转为财付通订单号(网银支付)
	 * 
	 * @param ourOrderNo
	 *            格式：日期-平台ID-运营商ID-sequence 20110512-100-180-0000007275
	 * @return
	 */
	public static String our2tenpay(String merchantId, String ourOrderNo) {
		String date = StringUtils.substringBefore(ourOrderNo, "-");
		return merchantId + date + StringUtils.substring(ourOrderNo, ourOrderNo.length() - 10, ourOrderNo.length());
	}

	/**
	 * 财付通订单号转为我方订单号(网银支付)
	 * 
	 * @param paymentId
	 *            支付平台ID - 财付通 - 147
	 * @param tenpayOrderNo
	 *            格式：我方商户ID(10位) + 日期(8位) + 唯一键(10位，我方订单最后10位)
	 * @return
	 */
	public static String tenpay2Our(String paymentId, String tenpayOrderNo) {
		String date = StringUtils.substring(tenpayOrderNo, 10, 18);
		String sequence = StringUtils.substring(tenpayOrderNo, 18, 28);

		return date + "-" + paymentId + "-" + PaymentConstant.OFFICIAL_ISSUER_ID + "-" + sequence;
	}
}
