package com.woniu.sncp.alarm.dto;

/**
 * 告警对象
 * @author luzz
 *
 */
public class AlarmMessageTo {
	
	public AlarmMessageTo(String src,String content){
		this.src = src;
		this.content = content;
	}

	/**
	 * 业务类型
	 */
	private String src;
	
	/**
	 * 告警内容
	 */
	private String content;
	
	public String getSrc() {
		return src;
	}
	public void setSrc(String src) {
		this.src = src;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
}