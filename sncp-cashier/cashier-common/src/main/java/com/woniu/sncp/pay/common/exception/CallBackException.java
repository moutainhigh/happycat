package com.woniu.sncp.pay.common.exception;

import com.woniu.sncp.ocp.utils.ProxoolUtil;
import com.woniu.sncp.web.IpUtils;

/**
 * 回调处理异常
 */
public class CallBackException extends ValidationException {

	private static final long serialVersionUID = 1L;

	public CallBackException() {
		super();
	}

	public CallBackException(String platomformId, String platomformName, String apiinfo, Object param, String orderNo) {
		super(String.format("订单号:%s,平台ID:%s,平台名称:%s,参数：%s,%s对方出现异常信息.所在机器服务器:[%s:%s@%s]", orderNo, platomformId, platomformName, param, apiinfo,
				IpUtils.getLoaclAddr(), ProxoolUtil.getTomcatPort(), ProxoolUtil.getPid()));
	}

	public CallBackException(String platomformId, String platomformName, String apiinfo, Object param, String state, String orderNo) {
		super(String.format("订单号:%s,平台ID:%s,平台名称:%s,参数：%s,%s对方出现处理失败返回状态%s.所在机器服务器:[%s:%s@%s]", orderNo, platomformId, platomformName, param, apiinfo,
				state, IpUtils.getLoaclAddr(), ProxoolUtil.getTomcatPort(), ProxoolUtil.getPid()));
	}

	public CallBackException(String platomformId, String platomformName, String apiinfo, Object param, String state, String platformValue,
			String woniuValue, String orderNo) {
		super(String.format("订单号:%s,平台ID:%s,平台名称:%s,参数：%s,%s对方出现返回非正常状态信息%s,对方值[%s]-蜗牛值[%s].所在机器服务器:[%s:%s@%s]", orderNo, platomformId, platomformName,
				param, apiinfo, state, platformValue, woniuValue, IpUtils.getLoaclAddr(), ProxoolUtil.getTomcatPort(), ProxoolUtil.getPid()));
	}

	public CallBackException(Throwable t) {
		super(t);
	}

}
