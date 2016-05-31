package cbss.core.trace.model;


public class TraceMethodInnerHttp {
	// http类型远程调用， 记录完成的URL
	private String httpUrl;
	// http类型远程调用，记录发起调用的时间
	private String httpReqTime;
	// http类型远程调用，设置超时时间
	private long httpTimeOut;
	// http类型远程调用，参数数据信息，注意屏蔽敏感数据
	private String httpReqData;
	// http类型远程调用,响应内容，注意屏蔽敏感数据
	private String httRespData;
	// 其他信息，是一个Map
	private String ot;
	public String getHttpUrl() {
		return httpUrl;
	}
	public void setHttpUrl(String httpUrl) {
		this.httpUrl = httpUrl;
	}
	public String getHttpReqTime() {
		return httpReqTime;
	}
	public void setHttpReqTime(String httpReqTime) {
		this.httpReqTime = httpReqTime;
	}
	public long getHttpTimeOut() {
		return httpTimeOut;
	}
	public void setHttpTimeOut(long httpTimeOut) {
		this.httpTimeOut = httpTimeOut;
	}
	public Object getHttpReqData() {
		return httpReqData;
	}
	public void setHttpReqData(String httpReqData) {
		this.httpReqData = httpReqData;
	}
	public String getHttRespData() {
		return httRespData;
	}
	public void setHttRespData(String httRespData) {
		this.httRespData = httRespData;
	}
	public String getOt() {
		return ot;
	}
	public void setOt(String ot) {
		this.ot = ot;
	}
	
	
}
