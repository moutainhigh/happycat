package com.woniu.sncp.cbss.core.trace.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

import com.woniu.sncp.cbss.core.trace.aspect.listener.ServletContainerApplicationListener;
import com.woniu.sncp.cbss.core.util.DateUtils;
import com.woniu.sncp.cbss.core.util.IpUtils;

public abstract class TraceCommon implements Serializable {

	private final static long systemstarting = System.currentTimeMillis();
	private final static Date sysdateStartDate = Calendar.getInstance().getTime();

	/**
	 * 主进程优先处理如下数据
	 * 
	 * <pre>
	 * 	日志点的记录时间
		日志点所在的方法名
		日志点所在的类路径
		服务机器IP
		 服务app端口
		 服务进程号
		 服务进程运行时间
		 服务app记录日志时的内存使用情况（最大、最小、剩余）
	 * </pre>
	 * 
	 * @param joinPoint
	 */
	public TraceCommon(JoinPoint joinPoint) {
		init(joinPoint);
	}

	// 日志点的记录时间
	private String logTime;
	// 日志点所在的方法名
	private String classPathMethodName;
	// 服务机器IP
	private String machineIp;
	// 服务app端口
	private String appPort;
	// 服务app记录日志时的内存使用情况（最大、最小、剩余）
	private String appInfo;
	// 唯一数据：如调用接口的URL，客户端请求中获取
	private String recordUUID;

	public void init(JoinPoint joinPoint) {
		this.logTime = DateUtils.format(new Date(), DateUtils.TIMESTAMP_DF);
		Signature signature = joinPoint.getSignature();
		this.classPathMethodName = signature.getDeclaringTypeName().replaceAll("\\.", "#") + "#" + signature.getName();
		this.appInfo = DateUtils.format(sysdateStartDate, DateUtils.TIMESTAMP_DF) + initThreadInfo() + initMemInfo() + initGetAppPort() + initMachineIp() + initAppPidLiveTime();
	}

	private String initThreadInfo() {
		StringBuffer bf = new StringBuffer("[");
		bf.append(Thread.currentThread().getName() + "," + Thread.currentThread().getId() + "," + Thread.currentThread().getPriority());
		bf.append("]");
		return bf.toString();
	}

	private String initMachineIp() {
		StringBuffer bf = new StringBuffer("[");
		bf.append(IpUtils.getLoaclAddr());
		bf.append("]");
		return bf.toString();
	}

	private String initAppPidLiveTime() {
		StringBuffer bf = new StringBuffer("[");
		bf.append(System.currentTimeMillis() - systemstarting);
		bf.append("]");
		return bf.toString();
	}

	private String initGetAppPort() {
		StringBuffer bf = new StringBuffer("[");
		bf.append(ServletContainerApplicationListener.port);
		bf.append("]");
		return bf.toString();
	}

	public String initMemInfo() {
		StringBuffer bf = new StringBuffer("[");
		bf.append(Runtime.getRuntime().maxMemory() + "-" + Runtime.getRuntime().freeMemory() + "-" + Runtime.getRuntime().totalMemory());
		bf.append("]");
		return bf.toString();
	}

	public String getClassPathMethodName() {
		return classPathMethodName;
	}

	public void setClassPathMethodName(String classPathMethodName) {
		this.classPathMethodName = classPathMethodName;
	}

	public String getRecordUUID() {
		return recordUUID;
	}

	public void setRecordUUID(String recordUUID) {
		this.recordUUID = recordUUID;
	}

	public String getLogTime() {
		return logTime;
	}

	public void setLogTime(String logTime) {
		this.logTime = logTime;
	}

	public String getMachineIp() {
		return machineIp;
	}

	public void setMachineIp(String machineIp) {
		this.machineIp = machineIp;
	}

	public String getAppPort() {
		return appPort;
	}

	public void setAppPort(String appPort) {
		this.appPort = appPort;
	}

	public String getAppInfo() {
		return appInfo;
	}

	public void setAppInfo(String appInfo) {
		this.appInfo = appInfo;
	}

}
