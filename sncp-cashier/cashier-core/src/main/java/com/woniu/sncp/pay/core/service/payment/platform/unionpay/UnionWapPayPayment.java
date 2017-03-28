package com.woniu.sncp.pay.core.service.payment.platform.unionpay;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.common.utils.http.PayCheckUtils;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.unionpay.util.PkipairUtil;
import com.woniu.sncp.pay.core.service.payment.platform.unionpay.util.UnionUtil;
import com.woniu.sncp.pojo.payment.PaymentOrder;

@Service("unionWapPayPayment")
public class UnionWapPayPayment extends AbstractPayment {
	
	protected final String _charset_encode = "UTF-8";

	@Override
	public Map<String, Object> orderedParams(Map<String, Object> inParams)
			throws ValidationException {
		Map<String, Object> params = new HashMap<String, Object>();
		// 1.拼装参数
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		/***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
		params.put("version", "5.0.0");   //版本号 全渠道默认值
		params.put("encoding", _charset_encode);   //字符集编码 可以使用UTF-8,GBK两种方式
		params.put("signMethod", "01");            //签名方法 目前只支持01：RSA方式证书加密
		params.put("txnType", "01");               //交易类型 01：消费，02：预授权
		params.put("txnSubType", "01");            //交易子类型 01：自助消费，03：分期付款
		params.put("bizType", "000201");           //业务类型 B2C网关支付，手机wap支付
		params.put("channelType", "07");           //渠道类型 07：PC,08：移动端
		
		/***商户接入参数***/
		params.put("merId", platform.getMerchantNo());    //商户号码，请改成自己申请的商户号或者open上注册得来的777商户号测试
		params.put("accessType", "0");                    //接入类型，商户接入填0 ，不需修改（0：直连商户， 1： 收单机构 2：平台商户）
		params.put("orderId",convert2UnionOrderNo(paymentOrder.getOrderNo()));             //商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则		
		params.put("txnTime", new SimpleDateFormat("yyyyMMddHHmmss").format(paymentOrder.getCreateDate()));   //订单发送时间，取系统时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
		params.put("currencyCode", "156");         //交易币种（境内商户一般是156 人民币）		
		BigDecimal money = new BigDecimal(paymentOrder.getMoney().toString());
		params.put("txnAmt", money.multiply(new BigDecimal(100)).intValue());    //交易金额，单位分，不要带小数点
		params.put("frontUrl", platform.getFrontUrl(paymentOrder.getMerchantId()));
		//注意:1.需设置为外网能访问，否则收不到通知    
		//    2.http https均可  
		//    3.收单后台通知后需要10秒内返回http200或302状态码 
		//    4.如果银联通知服务器发送通知后10秒内未收到返回状态码或者应答码非http200或302，那么银联会间隔一段时间再次发送。总共发送5次，每次的间隔时间为0,1,2,4分钟。
		//    5.后台通知地址如果上送了带有？的参数，例如：http://abc/web?a=b&c=d 在后台通知处理程序验证签名之前需要编写逻辑将这些字段去掉再验签，否则将会验签失败
		params.put("backUrl", platform.getBehindUrl(paymentOrder.getMerchantId()));
		
		/**请求参数设置完毕，以下对请求参数进行签名并生成html表单，将表单写入浏览器跳转打开银联页面------------->**/
		//Map<String, String> submitFromData = this.signData(params); 
		//报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。
		String path = platform.getPrivateUrl();
		String payPwd = platform.getPrivatePassword();
		Map<String, Object> returnMap = null;
		try {
			returnMap = PkipairUtil.signMsg(params, _charset_encode, path, payPwd);
		} catch (Exception e) {
			logger.error("银联wap加密异常：" + e.getMessage());
			throw new ValidationException("银联wap加密异常");
		}
		//设置payurl
		returnMap.put("payUrl", platform.getPayUrl()); // 提交给对方的支付地址
		
		//多证书使用方法：如果有多个商户号接入银联,每个商户号对应不同的证书可以使用此方法:传入私钥证书和密码(并且在acp_sdk.properties中 配置 acpsdk.singleMode=false) 
		//Map<String, String> submitFromData = DemoBase.signData(data,"D:\\certs\\PM_700000000000001_acp.pfx", "000000");
		return returnMap;
	}

	@Override
	public Map<String, Object> validateBackParams(HttpServletRequest request,
			Platform platform) throws ValidationException, DataAccessException,
			PaymentRedirectException {
		//验证签名
		Map<String, Object> reqParam = getAllRequestParam(request);
		Map<String, Object> valideData = null;
		if (null != reqParam && !reqParam.isEmpty()) {
			Iterator<Entry<String, Object>> it = reqParam.entrySet().iterator();
			valideData = new HashMap<String, Object>(reqParam.size());
			while (it.hasNext()) {
				Entry<String, Object> e = it.next();
				String key = (String) e.getKey();
				String value = (String) e.getValue();
				valideData.put(key, value);
			}
		}
		
		//重要！验证签名前不要修改reqParam中的键值对的内容，否则会验签不过
		String path = platform.getPublicUrl();
		if (!PkipairUtil.validate(valideData, _charset_encode, path)) {
			logger.info("快钱wap验证签名失败");
			throw new ValidationException("快钱wap验证签名失败");
		}
		
		String respCode = String.valueOf(valideData.get("respCode"));
		String orderId = String.valueOf(valideData.get("orderId"));
		String oppositeOrderNo = String.valueOf(valideData.get("queryId"));
		String txnAmt = String.valueOf(valideData.get("txnAmt"));
		
		// 订单查询
		String snailOrderNo = convert2SnailOrderNo(orderId);
		PaymentOrder paymentOrder = paymentOrderService.queryOrder(snailOrderNo);
		Assert.notNull(paymentOrder, "订单查询为空,orderNo:" + snailOrderNo);
		
		Map<String, Object> returned = new HashMap<String, Object>();
		if ("00".equals(respCode)) { // 支付成功
			logger.info("银联wap返回支付成功");
			
			//到对方查询，确认充值是否成功
			Map<String, Object> inParams = new HashMap<String, Object>();
			inParams.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
			inParams.put(PaymentConstant.PAYMENT_PLATFORM, platform);
			Map<String, Object> queryMap = this.checkOrderIsPayed(inParams);
			String payState = String.valueOf(queryMap.get(PaymentConstant.PAYMENT_STATE));
			if(PaymentConstant.PAYMENT_STATE_PAYED.equals(payState)){
				returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
			}else{
				//订单查询未支付
				logger.info("对方回调返回支付成功，但是查询返回未支付");
				returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
			}
			
		} else {
			logger.info("银联wap返回未支付,支付码:" + respCode);
			returned.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
		}
		
		returned.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		returned.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo);
		returned.put(PaymentConstant.OPPOSITE_MONEY, String.valueOf(txnAmt));
		//不验证imprestMode,直接取订单中imprestMode
		returned.put(PaymentConstant.PAYMENT_MODE, paymentOrder.getImprestMode());
		return returned;
	}

	@Override
	public Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams) {
		Map<String, Object> outParams = new HashMap<String, Object>();
		PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
		Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
		
		Map<String, Object> data = new HashMap<String, Object>();
		
		/***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
		data.put("version", "5.0.0");                 //版本号
		data.put("encoding", _charset_encode);               //字符集编码 可以使用UTF-8,GBK两种方式
		data.put("signMethod", "01");                          //签名方法 目前只支持01-RSA方式证书加密
		data.put("txnType", "00");                             //交易类型 00-默认
		data.put("txnSubType", "00");                          //交易子类型  默认00
		data.put("bizType", "000201");                         //业务类型 B2C网关支付，手机wap支付
		
		/***商户接入参数***/
		data.put("merId", platform.getMerchantNo());                  //商户号码，请改成自己申请的商户号或者open上注册得来的777商户号测试
		data.put("accessType", "0");                           //接入类型，商户接入固定填0，不需修改
		
		/***要调通交易以下字段必须修改***/
		data.put("orderId", convert2UnionOrderNo(paymentOrder.getOrderNo()));                 //****商户订单号，每次发交易测试需修改为被查询的交易的订单号
		data.put("txnTime", new SimpleDateFormat("yyyyMMddHHmmss").format(paymentOrder.getCreateDate()));                 //****订单发送时间，每次发交易测试需修改为被查询的交易的订单发送时间

		/**请求参数设置完毕，以下对请求参数进行签名并发送http post请求，接收同步应答报文------------->**/
		
		//Map<String, String> submitFromData = DemoBase.signData(data);//报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。

		String path = platform.getPrivateUrl();
		String pwd = platform.getPrivatePassword();
		Map<String, Object> returnMap = null;
		try {
			returnMap = PkipairUtil.signMsg(data, _charset_encode, path, pwd);
		} catch (Exception e) {
			logger.error("银联wap加密异常：" + e.getMessage());
			throw new ValidationException("银联wap加密异常");
		}
		
		//Map<String, Object> submitFromData = UnionEncryptUtil.sign(path, data, _charset_encode);
		
		//多证书使用方法：如果有多个商户号接入银联,每个商户号对应不同的证书可以使用此方法:传入私钥证书和密码(并且在acp_sdk.properties中 配置 acpsdk.singleMode=false) 
		//Map<String, String> submitFromData = DemoBase.signData(data,"D:\\certs\\PM_700000000000001_acp.pfx", "000000");
		String response = PayCheckUtils.postRequst(platform.getPayCheckUrl(), returnMap, 30000, _charset_encode, "银联wap订单查询接口");
		logger.info("银联wap订单查询返回：" + response);
		if(StringUtils.isBlank(response)){
			logger.info("银联wap订单查询返回为空");
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return outParams;
		}
		
		Map<String, Object> resData = new HashMap<String, Object>();
		// 将返回结果转换为map
		resData = UnionUtil.convertResultStringToMap(response);
		//验证签名
		String validtePath = platform.getPublicUrl();
		if(!PkipairUtil.validate(resData, _charset_encode, validtePath)){
			logger.info("银联wap签名验证失败");
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			return outParams;
		}
		
		String respCode = String.valueOf(resData.get("respCode"));
		if("00".equals(respCode)){
			//查询成功
			String origRespCode = String.valueOf(resData.get("origRespCode"));
			if("00".equals(origRespCode)){
				//交易成功
				String oppositeOrderNo = String.valueOf(resData.get("queryId"));
				String txnAmt = String.valueOf(resData.get("txnAmt"));
				outParams.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo); // 对方订单号
				outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_PAYED);
				outParams.put(PaymentConstant.OPPOSITE_MONEY, txnAmt); // 总金额，对方传回的单位已经是分
			}else{
				logger.info("银联wap订单交易状态:" + origRespCode);
				outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
			}
		}else{
			logger.info("银联wap订单查询状态错误，状态码：" + respCode);
			outParams.put(PaymentConstant.PAYMENT_STATE, PaymentConstant.PAYMENT_STATE_NOPAYED);
		}
		
		return outParams;
	}

	@Override
	public String encode(Map<String, Object> inParams)
			throws ValidationException {
		return null;
	}

	@Override
	public void paymentReturn(Map<String, Object> inParams,
			HttpServletResponse response, boolean isImprestedSuccess) {
		if(isImprestedSuccess){
			super.responseAndWrite(response, "ok");
		}
	}

	@Override
	public String getOrderNoFromRequest(HttpServletRequest request) {
		return convert2SnailOrderNo(request.getParameter("orderId"));
	}
	
	/**
	 * 获取请求参数中所有的信息
	 * 
	 * @param request
	 * @return
	 */
	public static Map<String, Object> getAllRequestParam(final HttpServletRequest request) {
		Map<String, Object> res = new HashMap<String, Object>();
		Enumeration<?> temp = request.getParameterNames();
		if (null != temp) {
			while (temp.hasMoreElements()) {
				String en = (String) temp.nextElement();
				String value = request.getParameter(en);
				res.put(en, value);
				//在报文上送时，如果字段的值为空，则不上送<下面的处理为在获取所有参数数据时，判断若值为空，则删除这个字段>
				//System.out.println("ServletUtil类247行  temp数据的键=="+en+"     值==="+value);
				if (null == res.get(en) || "".equals(res.get(en))) {
					res.remove(en);
				}
			}
		}
		return res;
	}

	/**
	 * @param snailOrderNo
	 *            eg:20140218-198-007-0000350921
	 * @return <pre>
	 * </pre>
	 */
	private static String convert2UnionOrderNo(String snailOrderNo) {
		return snailOrderNo.replaceAll("-", "");
	}

	/**
	 * @param unionOrderNo
	 *            eg:2016031420070070000002528 to 20160314-2007-007-0000002528
	 * @return <pre>
	 * </pre>
	 */
	private static String convert2SnailOrderNo(String unionOrderNo) {
		StringBuffer stringbuffer = new StringBuffer(unionOrderNo);
		int[] indexofInsert = new int[] { 8, 13, 17 };
		for (int i = 0; i < indexofInsert.length; i++) {
			stringbuffer.insert(indexofInsert[i], "-");
		}
		return stringbuffer.toString();
	}
}
