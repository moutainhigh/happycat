package com.woniu.sncp.alarm.dto;

import java.io.Serializable;

/**
 * 告警对象
 * @author luzz
 *
 */
public class AlarmMessageTo implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3756098010967356541L;

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
	
	/**
	 * 
	 * @return 业务类型
	 */
	public String getSrc() {
		return src;
	}
	
	/**
	 * 
	 * @param src 业务类型
	 */
	public void setSrc(String src) {
		this.src = src;
	}
	
	/**
	 * @return 告警内容
	 */
	public String getContent() {
		return content;
	}
	
	/**
	 * 
	 * @param content 告警内容
	 */
	public void setContent(String content) {
		this.content = content;
	}
	
}
