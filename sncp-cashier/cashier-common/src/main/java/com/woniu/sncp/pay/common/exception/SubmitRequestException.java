package com.woniu.sncp.pay.common.exception;

import com.woniu.sncp.ocp.utils.ProxoolUtil;
import com.woniu.sncp.web.IpUtils;

/**
 * 提交请求支付、直充异常
 */
public class SubmitRequestException extends ValidationException {

	private static final long serialVersionUID = 1L;

	public SubmitRequestException() {
		super();
	}

	public SubmitRequestException(String exceptionInfo) {
		super(exceptionInfo);
	}

	public SubmitRequestException(Throwable t) {
		super(t);
	}

	public SubmitRequestException(String platomformId, String platomformName, String apiinfo, String timeout, String url, Object param,
			String exception, String orderNo) {
		super(String.format("订单号:%s,平台ID:%s,平台名称:%s,请求:%s,参数：%s,设置的超时时间%s,%s对方返回非正常信息:%s.所在机器服务器:[%s:%s@%s]", orderNo, platomformId, platomformName,
				url, param, timeout, apiinfo, exception, IpUtils.getLoaclAddr(), ProxoolUtil.getTomcatPort(), ProxoolUtil.getPid()));
	}

	public SubmitRequestException(String platomformId, String platomformName, String apiinfo, String timeout, String url, Object param,
			String responseInfo, String orderNo, String errorcode, long handletimeStarttime) {
		super(String.format("订单号:%s,平台ID:%s,平台名称:%s,请求:%s,参数：%s,设置的超时时间%s,本次请求处理时间%s,响应原串%s,%s对方返回非正常信息:%s.所在机器服务器:[%s:%s@%s]", orderNo,
				platomformId, platomformName, url, param, timeout, System.currentTimeMillis() - handletimeStarttime, responseInfo, apiinfo,
				errorcode, IpUtils.getLoaclAddr(), ProxoolUtil.getTomcatPort(), ProxoolUtil.getPid()));
	}

}
