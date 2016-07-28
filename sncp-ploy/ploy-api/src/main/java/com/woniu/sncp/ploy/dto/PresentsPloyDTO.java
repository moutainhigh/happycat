package com.woniu.sncp.ploy.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * 活动信息
 * @author chenyx
 *
 */
public class PresentsPloyDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	//活动ID
	private Long id;

	//活动名称
	private String ployName;

	//活动类型
	private String type;
	
	private String ployDesc;

	private String limitGame;

	private String limitOperator;

	private String limitAgent;

	private String limitContent;

	private String isLimit;

	private Date start;

	private Date end;

	private String state;

	private Date create;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPloyName() {
		return ployName;
	}

	public void setPloyName(String ployName) {
		this.ployName = ployName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPloyDesc() {
		return ployDesc;
	}

	public void setPloyDesc(String ployDesc) {
		this.ployDesc = ployDesc;
	}

	public String getLimitGame() {
		return limitGame;
	}

	public void setLimitGame(String limitGame) {
		this.limitGame = limitGame;
	}

	public String getLimitOperator() {
		return limitOperator;
	}

	public void setLimitOperator(String limitOperator) {
		this.limitOperator = limitOperator;
	}

	public String getLimitAgent() {
		return limitAgent;
	}

	public void setLimitAgent(String limitAgent) {
		this.limitAgent = limitAgent;
	}

	public String getLimitContent() {
		return limitContent;
	}

	public void setLimitContent(String limitContent) {
		this.limitContent = limitContent;
	}

	public String getIsLimit() {
		return isLimit;
	}

	public void setIsLimit(String isLimit) {
		this.isLimit = isLimit;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Date getCreate() {
		return create;
	}

	public void setCreate(Date create) {
		this.create = create;
	}

	@Override
	public String toString() {
		return "PresentsPloy [id=" + id + ", ployName=" + ployName + ", type=" + type + ", ployDesc=" + ployDesc
				+ ", limitGame=" + limitGame + ", limitOperator=" + limitOperator + ", limitAgent=" + limitAgent
				+ ", limitContent=" + limitContent + ", isLimit=" + isLimit + ", start=" + start + ", end=" + end
				+ ", state=" + state + ", create=" + create + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PresentsPloyDTO other = (PresentsPloyDTO) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
