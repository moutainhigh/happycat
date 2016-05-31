package cbss.core.trace.model;

import org.aspectj.lang.JoinPoint;

public class TraceMethodException extends TraceCommon {

	public TraceMethodException(JoinPoint joinPoint) {
		super(joinPoint);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// 方法调用发起时间
	private Object methodStartTime;
	// 方法处理结束时间
	private Object methodExecEndTime;
	// 方法处理耗时时间
	private long methodExecSkipTime;
	// 方法调用的入参数据,注意屏蔽敏感数据
	private Object methodInData;
	// 方法响应码
	private Object methodOutData;
	// 方法响应码
	private Object methodOutException;
	// 其他信息，是一个Map
	private String ot;


	public Object getMethodInData() {
		return methodInData;
	}

	public void setMethodInData(String methodInData) {
		this.methodInData = methodInData;
	}

	public Object getMethodOutException() {
		return methodOutException;
	}

	public void setMethodOutException(Object methodOutException) {
		this.methodOutException = methodOutException;
	}

	public Object getMethodStartTime() {
		return methodStartTime;
	}

	public void setMethodStartTime(Object methodStartTime) {
		this.methodStartTime = methodStartTime;
	}

	public Object getMethodExecEndTime() {
		return methodExecEndTime;
	}

	public void setMethodExecEndTime(Object methodExecEndTime) {
		this.methodExecEndTime = methodExecEndTime;
	}

	public void setMethodInData(Object methodInData) {
		this.methodInData = methodInData;
	}

	public Object getMethodExecSkipTime() {
		return methodExecSkipTime;
	}

	public void setMethodExecSkipTime(long methodExecSkipTime) {
		this.methodExecSkipTime = methodExecSkipTime;
	}

	public Object getMethodOutData() {
		return methodOutData;
	}

	public void setMethodOutData(Object methodOutData) {
		this.methodOutData = methodOutData;
	}

	public String getOt() {
		return ot;
	}

	public void setOt(String ot) {
		this.ot = ot;
	}

}
