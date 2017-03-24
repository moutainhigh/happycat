package com.woniu.sncp.pay.web.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.woniu.kaptcha.util.CaptchaValidation;
import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.web.IpUtils;

public class ApiBaseController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	/**
	 * 打印request参数
	 * 
	 * @param method
	 * @param request
	 */
	protected void logRequestParams(String method, HttpServletRequest request) {
		Enumeration<String> requestParams = request.getParameterNames();
		Map<String, String> treeMap = putToTreeMap(request, requestParams);
		
		try {
			StringBuffer sb = new StringBuffer();
			Iterator<String> iter = treeMap.keySet().iterator();
			sb.append("\n++++++[" + method + "]参数 开始++++++\n");
			sb.append("requestIp=" + IpUtils.getRemoteAddr(request));
			sb.append("\n");
			while (iter.hasNext()) {
				String name = (String) iter.next();
				sb.append(name + "=" + treeMap.get(name));
				sb.append("\n");
			}
			sb.append("++++++[" + method + "]参数 结束++++++");
			logger.info(sb.toString());
		} catch (Exception e1) {
			logger.error("获取请求[" + method + "]参数异常", e1);
		}
	}
	
	/**
	 * Map中参数排序
	 * 
	 * @param request
	 * @param requestParams
	 * @return
	 */
	public Map<String, String> putToTreeMap(HttpServletRequest request,
			Enumeration<String> requestParams) {
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
//			
//			treeMap.put(key, value);
//		}
		return treeMap;
	}
    
	/**
	 * 带callback参数时，返回jsonp格式，否则返回json格式
	 * 
	 * @param callback
	 * @param response
	 * @param resultMap
	 */
	protected void writeJsonp(String callback, HttpServletResponse response, Object result) {

		PrintWriter out;
		try {
			out = response.getWriter();
			if (StringUtils.isEmpty(callback)) {
				out.print(JsonUtils.toJson(result));
			} else {
				response.setContentType("text/javascript; charset=UTF-8");
				out.print(callback + "(" + JsonUtils.toJson(result) + ")");
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	/**
	 * 获取request
	 * 
	 * @return
	 */
	protected HttpServletRequest getRequest() {
		return ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
	}

	/**
	 * 获取当前request的session
	 * 
	 * @return
	 */
	protected HttpSession getSession() {
		return this.getRequest().getSession();
	}
	
	/**
	 * 验证码验证
	 * 
	 * @param captchValue
	 *            输入的验证码
	 * @return
	 */
	@SuppressWarnings("static-access")
	protected Boolean isCaptchaPass(String captchValue) {
		logger.info("验证码验证开始");
		if (StringUtils.isEmpty(captchValue)) {
			logger.error("captchValue不能为空");
			return false;
		}
		String expectValue = (String) getSession().getAttribute(com.woniu.kaptcha.Constants.KAPTCHA_SESSION_KEY);
		Boolean result = new CaptchaValidation().isCaptchaPass(captchValue.trim(), expectValue);
		if (result) {
			getSession().removeAttribute(com.woniu.kaptcha.Constants.KAPTCHA_SESSION_KEY);
		}
		logger.info("预期值：" + expectValue + " 输入值：" + captchValue);
		return result;
	}
	
	
	/**
	 * 注册翡翠币身份
	 * @param request
	 * @param fcbAccount
	 * @param fcbPhone
	 */
	public static void registFcbIdentity(HttpServletRequest request,String fcbAccount,String fcbPhone){
		request.getSession().setAttribute("fcbAccount", StringUtils.trim(fcbAccount));
		request.getSession().setAttribute("fcbPhone", StringUtils.trim(fcbPhone));
	}
	
	/**
	 * 判断是否有翡翠身份
	 * @param request
	 * @return
	 */
	public static boolean hasFcbIdentity(HttpServletRequest request){
		String fcbAccount = ObjectUtils.toString(request.getSession().getAttribute("fcbAccount"));
		String fcbPhone = ObjectUtils.toString(request.getSession().getAttribute("fcbPhone"));
		if(StringUtils.isNotBlank(fcbAccount) && StringUtils.isNotBlank(fcbPhone)){
			return true;
		}
		return false;
	}
	
	public static String getFcbAccount(HttpServletRequest request){
		return ObjectUtils.toString(request.getSession().getAttribute("fcbAccount"));
	}
	
	public static String getFcbPhone(HttpServletRequest request){
		return ObjectUtils.toString(request.getSession().getAttribute("fcbPhone"));
	}
	
	protected Boolean isCaptchaGamePass(String captchValue, String accountid) {
		logger.info("验证码验证开始");
		if (StringUtils.isEmpty(captchValue)) {
			logger.error("captchValue不能为空");
			return false;
		}
		
		return CaptchaValidation.isGameCaptchaPass(captchValue, accountid);
	}
	
	public static Long getLoginId(){
		Assertion assertion = org.jasig.cas.client.util.AssertionHolder
				.getAssertion();
		if (assertion != null) {
			AttributePrincipal ap = assertion.getPrincipal();
			java.util.Map<String, Object> map = ap.getAttributes();
			return Long.valueOf(String.valueOf(map.get("naid")));
		}
		return null;
	}
	
	public  String shortMobile(String mobile) {
		if(org.apache.commons.lang.StringUtils.isNotEmpty(mobile) && mobile.length() == 11) {
			return mobile.substring(0, 3) + "******" + mobile.substring(9);
		}
		return mobile;
	}
}
