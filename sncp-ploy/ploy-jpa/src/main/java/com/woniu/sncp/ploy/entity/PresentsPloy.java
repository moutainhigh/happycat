package com.woniu.sncp.ploy.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PP_PRESENTS_PLOY", schema = "SN_PASSPORT")
public class PresentsPloy implements Serializable {

	private static final long serialVersionUID = 8309413091878493343L;

	enum State {
		enabled(0), disabled(1);
		private int value = 0;
		private State(int value) {
			this.value = value;
		}
	}

	@Id
	@Column(name = "N_ID")
	private Long id;

	@Column(name = "S_PLOY_NAME")
	private String ployName;

	@Column(name = "S_TYPE")
	private String type;

	@Column(name = "S_PLOY_DESC")
	private String ployDesc;

	@Column(name = "S_LIMIT_GAME")
	private String limitGame;

	@Column(name = "S_LIMIT_OPERATOR")
	private String limitOperator;

	@Column(name = "S_LIMIT_AGENT")
	private String limitAgent;

	@Column(name = "S_LIMIT_CONTENT")
	private String limitContent;

	@Column(name = "S_IS_LIMIT")
	private String isLimit;

	@Column(name = "D_START")
	private Date start;

	@Column(name = "D_END")
	private Date end;

	@Column(name = "S_STATE")
	private String state;

	@Column(name = "D_CREATE")
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

	public boolean isSatisfiedBy(PresentsPloy presentsPloy) {
		return presentsPloy.getId().longValue() == this.id.longValue();
	}

	@Override
	public String toString() {
		return "PresentsPloy [id=" + id + ", ployName=" + ployName + ", type=" + type + ", ployDesc=" + ployDesc
				+ ", limitGame=" + limitGame + ", limitOperator=" + limitOperator + ", limitAgent=" + limitAgent
				+ ", limitContent=" + limitContent + ", isLimit=" + isLimit + ", start=" + start + ", end=" + end
				+ ", state=" + state + ", create=" + create + "]";
	}
}
