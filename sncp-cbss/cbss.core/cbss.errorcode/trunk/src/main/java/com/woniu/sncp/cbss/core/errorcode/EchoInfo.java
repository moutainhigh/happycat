package com.woniu.sncp.cbss.core.errorcode;

import java.util.Calendar;

public class EchoInfo<T> {

	public static final Integer OK = 0;
	public static final Integer ERROR = -1;

	public static final Integer SIGNATURE_TYPE_DEFAULT = 0;

	public static final Integer SERVER_STATUS_DEAD = 0;
	public static final Integer SERVER_STATUS_ALIVE = 1;
	public static final Integer SERVER_STATUS_STARTING = 2;
	public static final Integer SERVER_STATUS_STOPPING = 3;
	public static final Integer SERVER_STATUS_STOPPED = 4;
	public static final Integer SERVER_STATUS_WARNING = 5;
	public static final Integer SERVER_STATUS_FUTURE_STOPED = 6;
	public static final Integer SERVER_STATUS_FUTURE_MAINTAIN = 7;

	private Integer msgcode;
	private String message;
	private String errorInfo;
	private String url;
	private T data;
	
	private String domaneName;
	private String futureTime;

	private Long appRspTime = Calendar.getInstance().getTimeInMillis();
	private Integer serverState = SERVER_STATUS_ALIVE;
	private String uuid;
	private Integer nextSignType = SIGNATURE_TYPE_DEFAULT;

	public String getDomaneName() {
		return domaneName;
	}

	public void setDomaneName(String domaneName) {
		this.domaneName = domaneName;
	}

	public String getFutureTime() {
		return futureTime;
	}

	public void setFutureTime(String futureTime) {
		this.futureTime = futureTime;
	}

	public Integer getNextSignType() {
		return nextSignType;
	}

	public EchoInfo<T> setNextSignType(Integer nextSignType) {
		this.nextSignType = nextSignType;
		return this;
	}

	public Integer getServerState() {
		return serverState;
	}

	public EchoInfo<T> setServerState(Integer serverState) {
		this.serverState = serverState;
		return this;
	}

	public EchoInfo<T> setUuid(String uuid) {
		this.uuid = uuid;
		return this;
	}

	public String getUuid() {
		return uuid;
	}

	public Long getAppRspTime() {
		return appRspTime;
	}

	public EchoInfo<T> setAppRspTime(Long appRspTime) {
		this.appRspTime = appRspTime;
		return this;
	}

	public Integer getMsgcode() {
		return msgcode;
	}

	public EchoInfo<T> setMsgcode(Integer msgcode) {
		this.msgcode = msgcode;
		return this;
	}

	public String getMessage() {
		return message;
	}

	public EchoInfo<T> setMessage(String message) {
		this.message = message;
		return this;
	}

	public String getErrorInfo() {
		return errorInfo;
	}

	public EchoInfo<T> setErrorInfo(String errorInfo) {
		this.errorInfo = errorInfo;
		return this;
	}

	public String getUrl() {
		return url;
	}

	public EchoInfo<T> setUrl(String url) {
		this.url = url;
		return this;
	}

	public T getData() {
		return data;
	}

	public EchoInfo<T> setData(T data) {
		this.data = data;
		return this;
	}
}
