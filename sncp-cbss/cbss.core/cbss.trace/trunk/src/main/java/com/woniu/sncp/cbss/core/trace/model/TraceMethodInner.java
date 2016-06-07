package com.woniu.sncp.cbss.core.trace.model;

import java.io.Serializable;

import org.aspectj.lang.JoinPoint;

public class TraceMethodInner extends TraceCommon implements Serializable {
	public TraceMethodInner(JoinPoint joinPoint) {
		super(joinPoint);
	}
	// 方法接收时间
	private String methodReceiveDate;
	// 其他信息，是一个Map
	private String ot;
	public String getMethodReceiveDate() {
		return methodReceiveDate;
	}
	public void setMethodReceiveDate(String methodReceiveDate) {
		this.methodReceiveDate = methodReceiveDate;
	}
	public String getOt() {
		return ot;
	}
	public void setOt(String ot) {
		this.ot = ot;
	}
	
}
