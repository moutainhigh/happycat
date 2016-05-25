package cbss.core.trace.model;

import org.aspectj.lang.JoinPoint;

public class TraceMethodBefore extends TraceCommon {
	public TraceMethodBefore(JoinPoint joinPoint) {
		super(joinPoint);
	}
	// 方法调用发起时间
	private String methodStart;
	// 方法调用的入参数据,注意屏蔽敏感数据
	private Object methodInData;
	// 其他信息，是一个Map
	private String ot;
	public String getMethodStart() {
		return methodStart;
	}
	public void setMethodStart(String methodStart) {
		this.methodStart = methodStart;
	}
	public Object getMethodInData() {
		return methodInData;
	}
	public void setMethodInData(Object methodInParam) {
		this.methodInData = methodInParam;
	}
	public String getOt() {
		return ot;
	}
	public void setOt(String ot) {
		this.ot = ot;
	}
	
	
}
