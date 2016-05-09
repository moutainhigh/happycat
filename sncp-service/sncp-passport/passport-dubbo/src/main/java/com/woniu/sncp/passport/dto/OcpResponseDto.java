package com.woniu.sncp.passport.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ocp帐号系统返回类
 * @author chenyx
 *
 */
@JsonAutoDetect
@JsonIgnoreProperties(value={"APPENDIX","PARAMS"})
public class OcpResponseDto implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@JsonProperty(value="STATE")
	private String respState;
	
	@JsonProperty(value="CODE")
	private Long code;
	
	@JsonProperty(value="DESC")
	private String desc;

	public String getRespState() {
		return respState;
	}

	public void setRespState(String respState) {
		this.respState = respState;
	}

	public Long getCode() {
		return code;
	}

	public void setCode(Long code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	@Override
	public String toString() {
		return "OcpResponseDto [respState=" + respState + ", code=" + code + ", desc=" + desc + "]";
	}
	
}
