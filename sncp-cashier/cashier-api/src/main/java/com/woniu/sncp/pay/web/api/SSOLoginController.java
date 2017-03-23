package com.woniu.sncp.pay.web.api;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.SingletonMap;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.woniu.sncp.web.response.ResultResponse;

@Controller
public class SSOLoginController extends ApiBaseController{
	
	private final static String ORDER_ERROR_PAGE = "/payment/error";
	
	@RequestMapping(value={"/sso/isSSOLogin","/m/sso/isSSOLogin"})
	@ResponseBody
	public String isSSOLogin(HttpServletRequest request) throws UnsupportedEncodingException {
		Assertion assertion = org.jasig.cas.client.util.AssertionHolder
				.getAssertion();
		if (assertion != null) {
			AttributePrincipal ap = assertion.getPrincipal();
			java.util.Map<String, Object> map = ap.getAttributes();
			request.getSession().setAttribute("loginUser",
					String.valueOf(map.get("SSOPrincipal")));
		}
		return null;
	}
    
    @RequestMapping(value={"/isLogin","/m/isLogin"})
	@ResponseBody
	public  ResultResponse isLogin(HttpServletRequest request) throws UnsupportedEncodingException {
    	String loginUser = (String) request.getSession().getAttribute(
				"loginUser");
		return new ResultResponse(ResultResponse.SUCCESS,"查询成功",new SingletonMap("loginUser",loginUser));
	}
    
    @RequestMapping(value={"/isLogin/json","/m/isLogin/json"})
	public void isLogin(@RequestParam(value="callback",required=false) String callback,HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
    	String loginUser = (String) request.getSession().getAttribute("loginUser");
    	writeJsonp(callback, response, new ResultResponse(ResultResponse.SUCCESS,"查询成功",new SingletonMap("loginUser",loginUser)));
		return;
	}
    
    
    @RequestMapping(value={"/sso/loginSuccess"})
	public String success(HttpServletRequest request) {
		Assertion assertion = org.jasig.cas.client.util.AssertionHolder
				.getAssertion();
		if (assertion != null) {
			AttributePrincipal ap = assertion.getPrincipal();
			java.util.Map<String, Object> map = ap.getAttributes();
			request.getSession().setAttribute("loginUser",
					String.valueOf(map.get("SSOPrincipal")));
			return "/payment/login_success";
		}
		request.setAttribute("msg", "登录失败");
		return ORDER_ERROR_PAGE;
	}
    
    @RequestMapping(value={"/m/sso/loginSuccess"})
	public String successMobile(HttpServletRequest request) {
		Assertion assertion = org.jasig.cas.client.util.AssertionHolder
				.getAssertion();
		if (assertion != null) {
			AttributePrincipal ap = assertion.getPrincipal();
			java.util.Map<String, Object> map = ap.getAttributes();
			request.getSession().setAttribute("loginUser",
					String.valueOf(map.get("SSOPrincipal")));
			return "/payment/login_success_mobile";
		}
		request.setAttribute("msg", "登录失败");
		return ORDER_ERROR_PAGE;
	}
    
    @RequestMapping(value={"/sso/loginUser","/m/sso/loginUser"})
	@ResponseBody
	public String getLoginUser(HttpServletRequest request) {
		String loginUser = (String) request.getSession().getAttribute(
				"loginUser");
		return loginUser;
	}

}
