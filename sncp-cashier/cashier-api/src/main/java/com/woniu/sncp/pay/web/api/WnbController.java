package com.woniu.sncp.pay.web.api;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ObjectUtils;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.pay.common.errorcode.ErrorCode;
import com.woniu.sncp.pay.core.HttpClient;
import com.woniu.sncp.pay.core.security.SecuritySSOAuth;
import com.woniu.sncp.pay.core.service.IssuerComparisonService;
import com.woniu.sncp.pay.core.service.payment.platform.wnb.SignatureUtils;
import com.woniu.sncp.web.response.ResultResponse;

import okhttp3.HttpUrl;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Controller
public class WnbController extends ApiBaseController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
Environment environment;
//	cloud.api.url=http://cloud.api.woniu.com
//		cloud.api.accessId= 2010
//		cloud.api.accessType= 8
//		cloud.api.accessKey=grssiAYYUjvRPUV
//		cloud.api.accessPasswd=v2fz2wN8hnPqB3


	@Resource
	IssuerComparisonService issuerComparisonService;
	private String accessId;
	private String accessType;
	private String accessKey;
	private String accessPasswd;
	private String api;
	@PostConstruct
	public void initData() {
    	accessId=	environment.getProperty("cloud.api.accessId");
		accessType=	environment.getProperty("cloud.api.accessType");
		accessKey=	environment.getProperty("cloud.api.accessKey");
		accessPasswd=	environment.getProperty("cloud.api.accessPasswd");
		api=	environment.getProperty("cloud.api.url");
		
	}

	@RequestMapping("/wnb/query/jsonp")
 	public void queryWnbAmountJson(@RequestParam(value="callback",required=false) String callback
			,HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object>  resultMap=	queryWnbAmount(request);
	 
 		String msgcode=ObjectUtils.toString(resultMap.get("msgcode"));

		String message=ObjectUtils.toString(resultMap.get("message"));
		
	 
		writeJsonp(callback, response, new ResultResponse(msgcode,message,(Map)resultMap.get("data")));
	}

	@RequestMapping("/wnb/query")
	@ResponseBody
	public Map<String, Object> queryWnbAmount(HttpServletRequest request) {
		Long aid = SecuritySSOAuth.getLoginId();



		Map<String, Object> retMap = new HashMap<String, Object>();
		if(aid==null){
			retMap.put("msgcode", "0");
			retMap.put("message", "你还未登录!");
	 
			request.setAttribute("retCode", ErrorCode.getErrorCode(56102).get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", ErrorCode.getErrorCode(56102).get(ErrorCode.TIP_INFO));
			return retMap;
		}
		try {
			
			Map<String, Object> initRequest = new HashMap<String, Object>();
			Map<String, String> headers = new HashMap<String, String>();

			headers.put("accessId", accessId);
			headers.put("accessType", accessType);
			headers.put("accessPasswd", accessPasswd);
			headers.put("second", System.currentTimeMillis() + "");
			headers.put("signVersion", "1.0");
			Map<String, String> params = new HashMap<>();
		
			String query=String.format("{\r\n" + 
					"  checkPay(globalRequest: {aid:\"%d\"}) {\r\n" + 
					"    code\r\n" + 
					"    message\r\n" + 
					"    success\r\n" + 
					"    data {\r\n" + 
					"      balance\r\n" + 
					"      email\r\n" + 
					"      existPwd\r\n" + 
					"    }\r\n" + 
					"  }\r\n" + 
					"}\r\n" + 
					"",aid);
		
			params.put("query", query);

			String body = JsonUtils.toJson(params);
			headers.put("content-type", "application/json");
			String url = api + "/global/woniucoin/v1/graphql";
			String accessVerify = SignatureUtils.signature(HttpUrl.get(url).encodedPath(), "POST", body, headers, null, accessKey);

			headers.put("accessVerify", accessVerify);

			initRequest.put("headers", headers);
			initRequest.put("body", body);
			initRequest.put("method", "POST");
			String resp = null;
			 
				resp = HttpClient.fetch(url, initRequest).body().string();
		 
			
			JSONObject json = JSONObject.parseObject(resp);
			JSONObject	checkPay=json.getJSONObject("data").getJSONObject("checkPay");
			if(checkPay.getBoolean("success")) {
				
				retMap.put("data", checkPay.getJSONObject("data"));
			}
			retMap.put("msgcode", "1");
			retMap.put("message", "查询成功 ");
	 		request.setAttribute("retCode", ErrorCode.getErrorCode(56106).get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", ErrorCode.getErrorCode(56106).get(ErrorCode.TIP_INFO));
			return retMap;
		}catch(Exception e) {
			logger.error("",e);
			retMap.put("msgcode", "-1");
			retMap.put("message", "查询失败 ");
	 		request.setAttribute("retCode", ErrorCode.getErrorCode(56106).get(ErrorCode.TIP_CODE));
			request.setAttribute("retMsg", ErrorCode.getErrorCode(56106).get(ErrorCode.TIP_INFO));
			return retMap;
		}
		
	

	}

}
