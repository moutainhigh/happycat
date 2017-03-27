package com.woniu.sncp.pay.core.service.payment.platform.alipay;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.net.NetServiceException;
import com.woniu.sncp.pay.common.utils.PaymentConstant;
import com.woniu.sncp.pojo.game.Game;
import com.woniu.sncp.pojo.passport.Passport;
import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.web.IpUtils;

/**
 * 风险检测服务接口
 * 
 * @author luzz
 *
 */
@Service("alipaySecurityRiskDetect")
public class AlipaySecurityRiskDetect {
	
	final static String DETECT_URL = "https://mapi.alipay.com/gateway.do";
	
	//终端类型
	public final static String TERMINAL_TYPE_WEB = "WEB";
	public final static String TERMINAL_TYPE_WAP = "WAP";
	public final static String TERMINAL_TYPE_APP = "APP";
	
	//币种
	public final static String CURRENCY_RMB = "RMB";
	public final static String CURRENCY_USD = "USD";
	
	//场景编码
	public final static String SCENE_CODE_PAYMENT = "PAYMENT";//订单支付
	public final static String SCENE_CODE_PROMOTION_ANTIFRAUD = "PROMOTION_ANTIFRAUD";//营销活动防作弊
	public final static String SCENE_CODE_MOBILE_CHANGE = "MOBILE_CHANGE";//手机绑定/换绑
	public final static String SCENE_CODE_WITHDRAW = "WITHDRAW";//提现
	public final static String SCENE_CODE_LOGIN = "LOGIN";//登录
	
	//银行卡类型
	public final static String BANKCARD_TYPE_DC = "DC";//借记卡
	public final static String BANKCARD_TYPE_CC = "CC";//信用卡
	public final static String BANKCARD_TYPE_PC = "PC";//预付费卡
	public final static String BANKCARD_TYPE_SCC = "SCC";//准贷记卡
	public final static String BANKCARD_TYPE_DCC = "DCC";//存贷合一卡
	public final static String BANKCARD_TYPE_VC = "VC";//消费卡
	public final static String BANKCARD_TYPE_PB = "PB";//存折
	
	/**
	 * 获取终端详情
	 * 
	 * @param osName
	 * @param osVersion
	 * @return
	 */
	public String getTerminalInfo(String osName,String osVersion){
		return osName + "^" + osVersion;
	}
	
	/**
	 * 获取JS SDK生成的 tokenID
	 * 
	 * @param partnerCode
	 * @param sceneCode
	 * @param sessionId
	 * @return
	 */
	public String getTokenId(String partnerCode,String sceneCode,String sessionId){
		return partnerCode + "_" + sceneCode + "_" + sessionId;
	}
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public String detect(String requestUrl,List<BasicNameValuePair> valuePair,String encoding){
		logger.info(valuePair.toString());
		
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = null;
		String response = null;
		try{
			httpclient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,encoding);
			httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
			httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);
			
			httppost = new HttpPost(requestUrl);
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(valuePair, encoding);
			httppost.setEntity(entity);
			
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			response = httpclient.execute(httppost,responseHandler);
			response = URLDecoder.decode(response, encoding);
		} catch (ClientProtocolException e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException("支付宝风险检测服务接口出错，请与客服联系", e);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			throw new NetServiceException("支付宝风险检测服务接口出错，请与客服联系", e);
		}  finally {
			logger.info("支付宝风险检测服务接口url:{} 返回:{}",requestUrl,response);
			abortConnection(httppost, httpclient);
		}
		
		return response;
	}
	
	public void riskDetect(Map<String, Object> inParams,String terminalType, String charsetCode){
		try{
			PaymentOrder paymentOrder = (PaymentOrder) inParams.get(PaymentConstant.PAYMENT_ORDER);
			Platform platform = (Platform) inParams.get(PaymentConstant.PAYMENT_PLATFORM);
			Game game = (Game)inParams.get("game");
			Passport passport = (Passport) inParams.get("passport");
	//		String priKey = AlipayHelper.readText(PRIVATE_KEY_FILE);
			
			Map<String,Object> signMap = new TreeMap<String,Object>();
			signMap.put("service", "alipay.security.risk.detect");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			signMap.put("partner", platform.getMerchantNo());//合作者身份ID
			signMap.put("_input_charset", charsetCode);//参数编码字符集
			signMap.put("timestamp", sdf.format(new Date()));//接口请求时间
			signMap.put("terminal_type", terminalType);//终端类型
			signMap.put("notify_url", platform.getBehindUrl(paymentOrder.getMerchantId()));//通知返回url
			signMap.put("order_no", paymentOrder.getOrderNo());//商户订单唯一标识号
			signMap.put("order_credate_time", sdf.format(paymentOrder.getCreateDate()));
			signMap.put("order_category", "虚拟^游戏^"+(game!=null?game.getName():""));
			signMap.put("order_item_name", StringUtils.trim((String) inParams.get("productName")));
			signMap.put("order_item_city", "苏州");//订单商品所在城市
			signMap.put("order_amount", ObjectUtils.toString(paymentOrder.getMoney()));//订单总金额
			signMap.put("item_unit_price", ObjectUtils.toString(paymentOrder.getMoney()));//订单产品单价
			signMap.put("item_quantity", String.valueOf(paymentOrder.getAmount()));//订单产品数量
			signMap.put("currency", this.CURRENCY_RMB);//币种
			signMap.put("scene_code", this.SCENE_CODE_PAYMENT);//场景编码
			signMap.put("buyer_account_no", String.valueOf(passport.getId()));//买家账户编号
			sdf.applyPattern("yyyy-MM-dd");
			signMap.put("buyer_reg_date", sdf.format(passport.getCreateDate()));//买家注册时间
			signMap.put("env_client_ip", IpUtils.longToIp(paymentOrder.getClientIp()));//客户端ip
			
			List<BasicNameValuePair> valuePair = new ArrayList<BasicNameValuePair>();
			Iterator<String> iter = signMap.keySet().iterator();
			StringBuilder sb = new StringBuilder();
			while (iter.hasNext()) {
				String name = (String) iter.next();
				if(!"sign_type".equalsIgnoreCase(name)){
					sb.append("&").append(name).append("=").append(signMap.get(name));
				}
				valuePair.add(new BasicNameValuePair(name, String.valueOf(signMap.get(name))));
			}
			
	//		LinkedHashMap<String, Object> linkedHashMap = AlipayHelper.sortMap(signMap);
	//		String source = super.linkedHashMapToStringWithKey(linkedHashMap, true);
			
			String source = sb.toString().substring(1);
			logger.info(source);
			// 2.加密
	//		Map<String, Object> encryptParams = new HashMap<String, Object>();
	//		encryptParams.put("source", source);
	//		encryptParams.put("priKey", priKey);
	//		String sign = this.encode(encryptParams);
			
			String sign = MD5Encrypt.encrypt(source+platform.getPayKey(), "gbk");
			logger.info(sign.toLowerCase());
			valuePair.add(new BasicNameValuePair("sign", sign.toLowerCase()));
			valuePair.add(new BasicNameValuePair("sign_type", "MD5"));//签名方式
	//		valuePair.add(new BasicNameValuePair("sign_type", "RSA"));//签名方式
			
			//logger.info("验签结果:"+RSASignature.verify(source, sign, WN_PUBLIC_KEY_FILE, _charset_encode));
			detect(DETECT_URL, valuePair, "gbk");
		} catch (Exception e){
			logger.error("风险检测服务接口异常,"+e);
		}
	}
	
	/**
	 * 释放HttpClient连接
	 * 
	 * @param hrb
	 *            请求对象
	 * @param httpclient
	 * 			  client对象
	 */
	private static void abortConnection(final HttpRequestBase hrb, final HttpClient httpclient){
		if (hrb != null) {
			hrb.abort();
		}
		if (httpclient != null) {
			httpclient.getConnectionManager().shutdown();
		}
	}
	
}
