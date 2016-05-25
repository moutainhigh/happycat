package cbss.core.errorcode;

import java.util.Calendar;

public class EchoInfo<T> {

	public static final Integer OK = 0;
	public static final Integer ERROR = -1000;

	private Integer msgcode;
	private String message;
	private String errorInfo;
	private String url;
	private T data;

	private Long appRspTime = Calendar.getInstance().getTimeInMillis();

	public Long getAppRspTime() {
		return appRspTime;
	}

	public void setAppRspTime(Long appRspTime) {
		this.appRspTime = appRspTime;
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
