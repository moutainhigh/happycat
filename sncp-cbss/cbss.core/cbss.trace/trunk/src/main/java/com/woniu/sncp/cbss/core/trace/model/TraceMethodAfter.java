package com.woniu.sncp.cbss.core.trace.model;

import java.util.Date;

import org.aspectj.lang.JoinPoint;

public class TraceMethodAfter extends TraceCommon {

	public TraceMethodAfter(JoinPoint joinPoint) {
		super(joinPoint);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// 方法处理结束时间
	private Date methodExecEndDate;
	// 方法处理耗时时间
	private long methodExecSkipTime;
	// 方法响应码
	private String methodOutData;
	// 其他信息，是一个Map
	private String ot;

	public Date getMethodExecEndDate() {
		return methodExecEndDate;
	}

	public void setMethodExecEndDate(Date methodExecEndDate) {
		this.methodExecEndDate = methodExecEndDate;
	}

	public long getMethodExecSkipTime() {
		return methodExecSkipTime;
	}

	public void setMethodExecSkipTime(long methodExecSkipTime) {
		this.methodExecSkipTime = methodExecSkipTime;
	}

	public Object getMethodOutData() {
		return methodOutData;
	}

	public void setMethodOutData(String methodOutData) {
		this.methodOutData = methodOutData;
	}

	public String getOt() {
		return ot;
	}

	public void setOt(String ot) {
		this.ot = ot;
	}

}
