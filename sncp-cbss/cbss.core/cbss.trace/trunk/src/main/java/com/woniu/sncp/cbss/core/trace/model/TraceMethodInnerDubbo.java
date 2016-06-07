package com.woniu.sncp.cbss.core.trace.model;

import org.aspectj.lang.JoinPoint;

public class TraceMethodInnerDubbo extends TraceMethodInner {

	public TraceMethodInnerDubbo(JoinPoint joinPoint) {
		super(joinPoint);
	}
	// DUBBO类型远程调用,记录开始时间
	private String dubboStart;
	// DUBBO类型远程调用,结束时间
	private String dubboEnd;
	// DUBBO类型远程调用,耗时时间
	private long dubboSkip;
	// 方法响应内容，注意屏蔽敏感数据
	private Object dubboRespData;
	// 其他信息，是一个Map
	private String ot;
	public String getDubboStart() {
		return dubboStart;
	}
	public void setDubboStart(String dubboStart) {
		this.dubboStart = dubboStart;
	}
	public String getDubboEnd() {
		return dubboEnd;
	}
	public void setDubboEnd(String dubboEnd) {
		this.dubboEnd = dubboEnd;
	}
	public long getDubboSkip() {
		return dubboSkip;
	}
	public void setDubboSkip(long dubboSkip) {
		this.dubboSkip = dubboSkip;
	}
	public Object getDubboRespData() {
		return dubboRespData;
	}
	public void setDubboRespData(Object dubboRespData) {
		this.dubboRespData = dubboRespData;
	}
	public String getOt() {
		return ot;
	}
	public void setOt(String ot) {
		this.ot = ot;
	}
	
	
}
