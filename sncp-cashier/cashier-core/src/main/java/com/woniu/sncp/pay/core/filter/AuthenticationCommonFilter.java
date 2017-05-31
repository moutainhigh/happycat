package com.woniu.sncp.pay.core.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.exception.ApiAuthenticationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.common.utils.encrypt.DesUtil;
import com.woniu.sncp.pay.common.utils.encrypt.EncryptFactory;
import com.woniu.sncp.pay.common.utils.http.RequestUtil;
import com.woniu.sncp.pay.core.service.PaymentMerchantService;
import com.woniu.sncp.pay.core.service.PlatformService;
import com.woniu.sncp.pay.repository.pay.PaymentMerchant;
import com.woniu.sncp.web.IpUtils;

/**
 * 
 * <p>descrption: 通用过滤器</p>
 * 
 * @author fuzl
 * @date   2016年10月26日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
@Service("authenticationCommonFilter")
public class AuthenticationCommonFilter extends OncePerRequestFilter {
	
	private static ThreadLocal<String> requestBody = new ThreadLocal<String>();
	
	public static ThreadLocal<String> getRequestBody() {
		return requestBody;
	}
	

	@Autowired
    @Qualifier(value="paymentMerchantService")
	private PaymentMerchantService paymentMerchantService;
	
	@Autowired
    @Qualifier(value="platformService")
	private PlatformService platformService;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			
			requestBody.set("");//AbstractPayment use
			
			request.setCharacterEncoding("utf-8");
			
//			Map<String, Object> requestParams = request.getParameterMap();
			Enumeration<String> requestParams = request.getParameterNames();
			Map<String, String> treeMap = putToTreeMap(request, requestParams);

			logRequestParams("(" + request.getMethod() + ")"
					+ request.getRequestURL().toString(), request, treeMap);

			String merchantId = StringUtils.trim(request.getParameter("merchantid"));
			String platformId = StringUtils.trim(request.getParameter("platformid"));
			String sign = request.getParameter("sign");
			String sign1 = request.getParameter("sign1");

			//获取商户信息
			PaymentMerchant payemntMerchnt = paymentMerchantService.queryPayemntMerchnt(Long.valueOf(merchantId));
			
			if(payemntMerchnt == null){
				throw new ApiAuthenticationException("非法支付申请号");
			}
			
			//验证商户状态
			if(!PaymentMerchant.STATUS_VALID.equals(payemntMerchnt.getStatus())){
				throw new ApiAuthenticationException("支付申请号未审批或已失效");
			}
			
			//验证平台id是否可使用
			if(StringUtils.isNotBlank(platformId)){
				Assert.assertNotSame("非法支付平台", 0L, Long.valueOf(platformId));
				Platform platform = platformService.queryPlatform(Long.valueOf(merchantId), Long.valueOf(platformId));
				platformService.validatePaymentPlatform(platform);
			}
			
			//获取签名类型
			StringBuffer sbSource = genSignStr(request, treeMap);
			String signgType = payemntMerchnt.getKeyType();
			String signKey = payemntMerchnt.getMerchantKey();
			
			//DES解密特殊处理
			if("DES".equals(signgType)){
				if("get".equalsIgnoreCase(request.getMethod())){
					String data = request.getParameter("d");
					String[] signKeys = signKey.split(",");
					logger.info("data^_^"+data);
					logger.info("signKeys^_^"+signKeys[0]+","+signKeys[1]);
					DesUtil util = new DesUtil(signKeys[0], signKeys[1]);//DesUtil.getInstance(signKeys[0], signKeys[1]);
					byte[] decrypt = util.decrypt(data);
					String result = new String(decrypt,"utf-8");
					
					RequestUtil.filter(result);//解析参数
					net.sf.json.JSONObject jsonData = RequestUtil.parseResponseparameters(result);
					
					request.setAttribute("orderno", jsonData.get("orderno"));
					request.setAttribute("money", jsonData.get("money"));
					request.setAttribute("backendurl", jsonData.get("backendurl"));
					request.setAttribute("merchantid", jsonData.get("merchantid"));
					request.setAttribute("gameid", jsonData.get("gameid"));
					request.setAttribute("account", jsonData.get("account"));
					request.setAttribute("aid", jsonData.containsKey("aid")?jsonData.get("aid"):"");
					request.setAttribute("clientip", jsonData.get("clientip"));
					request.setAttribute("imprestmode", jsonData.get("imprestmode"));
					request.setAttribute("productname", jsonData.get("productname"));//
					request.setAttribute("fontendurl", jsonData.get("fontendurl"));
					request.setAttribute("security", jsonData.get("security"));
					request.setAttribute("ext", jsonData.get("ext"));
				}
			}else{
			
				//验证签名
				boolean isSuccess = EncryptFactory.getInstance(signgType).verify(sign, sbSource.toString(), signKey, "");
				if(!isSuccess){
					//如果传输sign1,校验sign1
					if(StringUtils.isNotBlank(sign1)){
						boolean _isSuccess = EncryptFactory.getInstance(signgType).verify(sign1, sbSource.toString(), signKey, "");
						if(!_isSuccess){
							logger.error("接口签名校验不通过,返回结果:"+isSuccess);
							throw new ApiAuthenticationException("签名校验不通过");
						}
					}else{
						logger.error("接口签名校验不通过,返回结果:"+isSuccess);
						throw new ApiAuthenticationException("签名校验不通过");
					}
				}
			}
		} catch (Exception e) {
			logger.error("接口认证异常," + e.getMessage(), e);
//			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			response.sendRedirect("/error");
			return;
		}
		try{
			filterChain.doFilter(request, response);
		}finally{
			requestBody.set("");
		}
//		//request处理
//		ServletRequest requestWrapper = null;
//        if(request instanceof HttpServletRequest) {  
//            requestWrapper = new BodyReaderHttpServletRequestWrapper((HttpServletRequest) request);  
//        }
//        if(null == requestWrapper){
//        	filterChain.doFilter(request, response);  
//        } else {
//        	filterChain.doFilter(requestWrapper, response);  
//        }
	}
	
	private Map<String, String> putToTreeMap(HttpServletRequest request,
			Enumeration<String> requestParams) throws UnsupportedEncodingException {
		Map<String, String> treeMap = new TreeMap<String, String>();
		while(requestParams.hasMoreElements()){
			String key = requestParams.nextElement();
			String value = request.getParameter(key);
			treeMap.put(key, value);
		}
//		for (Iterator<Entry<String, Object>> keyValuePairs = requestParams
//				.entrySet().iterator(); keyValuePairs.hasNext();) {
//			Map.Entry<String, Object> entry = keyValuePairs.next();
//			String key = entry.getKey();
//			String value = request.getParameter(key);
////			if( ("get".equalsIgnoreCase(request.getMethod())) 
////					&& (request.getRequestURI().equals("/wap/api/phonecard/dp/jsonp") || request.getRequestURI().equals("/wap/api/wncard/dp/jsonp") || request.getRequestURI().equals("/wap/api/security/ttb/pay/json")) 
////					&& key.equals("productname")){
////				value = new String(value.getBytes("iso8859-1"),"UTF-8");
////			}
//			treeMap.put(key, value);
//		}
		return treeMap;
	}
	
	private StringBuffer genSignStr(HttpServletRequest request, Map<String, String> treeMap) {
		StringBuffer sb = new StringBuffer();
		treeMap.remove("sign");
		treeMap.remove("ticket");
		treeMap.remove("platformid");
		treeMap.remove("bankcd");
		treeMap.remove("referer");
		treeMap.remove("cardtype");
		treeMap.remove("stagePlan");
		treeMap.remove("stageNum");
		treeMap.remove("ttbDjjMoney");
		treeMap.remove("cardNo");
		treeMap.remove("cardPwd");
		treeMap.remove("captchaValue");
		treeMap.remove("accountid");
		treeMap.remove("smscode");
		treeMap.remove("tgt");
		treeMap.remove("yueMoney");
		treeMap.remove("yueCurrency");
		treeMap.remove("fcbsmscode");
		treeMap.remove("bankCardType");//签名过滤掉bankCardType
		treeMap.remove("callback");
		treeMap.remove("type");
		treeMap.remove("_");

		Iterator<String> iter = treeMap.keySet().iterator();
		while (iter.hasNext()) {
			String name = iter.next();
			if(StringUtils.isBlank(treeMap.get(name))) continue;
			sb.append(name).append(treeMap.get(name));
		}
		
		return sb;
	}

	protected void logRequestParams(String method, HttpServletRequest request,Map<String, String> treeMap) {
		try {
			StringBuffer sb = new StringBuffer();
			Iterator<String> iter = treeMap.keySet().iterator();
			sb.append("\n++++++[" + method + "]参数 开始++++++\n");
			sb.append("requestIp=" + IpUtils.getRemoteAddr(request));
			sb.append("\n");
			while (iter.hasNext()) {
				String name = iter.next();
				sb.append(name + "=" + treeMap.get(name));
				sb.append("\n");
			}
			sb.append("++++++[" + method + "]参数 结束++++++");
			logger.info(sb.toString());
		} catch (Exception e) {
			logger.error("获取请求[" + method + "]参数异常", e);
		}
	}
	

}
