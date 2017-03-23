package com.woniu.sncp.pay.core.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 
 * <p>descrption: 用来清空requestBody</p>
 * 
 * @author fuzl
 * @date   2016年10月26日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public class RequestClearFilter extends OncePerRequestFilter {
	
	private Logger logger = LoggerFactory.getLogger(RequestClearFilter.class);
	
	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		long starttime = System.currentTimeMillis();
		logger.info(this.getClass().getSimpleName()+"\t"+request.getRequestURI()+"\t"+"接口开始时间:{}", starttime);
		try{
			filterChain.doFilter(request, response);
		}finally{
			long endtime = System.currentTimeMillis();
			logger.info(this.getClass().getSimpleName()+"\t"+request.getRequestURI()+"\t"+"接口结束时间:{}", endtime);
			logger.info(this.getClass().getSimpleName()+"\t"+request.getRequestURI()+"\t"+"接口耗时:{}", endtime-starttime);
			AuthenticationCommonFilter.getRequestBody().set("");
		}
	}
	

}
