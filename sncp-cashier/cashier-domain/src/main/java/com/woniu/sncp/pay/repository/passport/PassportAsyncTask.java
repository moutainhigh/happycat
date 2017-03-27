package com.woniu.sncp.pay.repository.passport;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.woniu.sncp.pojo.IdEntity;
import com.woniu.sncp.pojo.SingleKeyPojo;

@Entity
@Table(name = "PP_ASYNC_TASK", schema = "SN_PASSPORT")
public class PassportAsyncTask implements SingleKeyPojo{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 状态 - 未操作 - 0
	 */
	public static String STATE_NO_OPERATE = "0";
	
	/**
	 * 状态 - 操作成功 - 1
	 */
	public static String STATE_SUCCESS = "1";
	
	/**
	 * 状态 - 操作失败 - 2
	 */
	public static String STATE_FAILED = "2";
	
	/**
	 * 状态 - 正在恢复 - 3
	 */
	public static String STATE_RECOVERING = "3";

	@Id
	@Column(name = "N_ID")
	protected Long id;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * 任务类型
	 */
	@Column(name = "S_TASK_TYPE")
	private String taskType;
	
	/**
	 *  - N_OPERATION_ID
	 */
	@Column(name = "N_OPERATION_ID")
	private Long operationId;
	
	/**
	 * 任务详情 
	 */
	@Column(name = "S_TASK_OBJ")
	private String taskObj;
	
	/**
	 * 任务状态
	 */
	@Column(name = "S_STATE")
	private String state;
	
	/**
	 * 任务执行次数 - N_AMOUNT
	 */
	@Column(name = "N_AMOUNT")
	private Integer count;
	
	/**
	 * 创建时间 - D_CREATE
	 */
	@Column(name = "D_CREATE")
	private Date createDate;
	
	/**
	 * 最后执行时间 - D_MODIFY
	 */
	@Column(name = "D_MODIFY")
	private Date modifyDate;
	
	/**
	 * 账号 - S_ACCOUNT
	 */
	@Column(name = "S_ACCOUNT")
	private String account;

	public String getTaskType() {
		return taskType;
	}

	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}

	public Long getOperationId() {
		return operationId;
	}

	public void setOperationId(Long operationId) {
		this.operationId = operationId;
	}

	public String getTaskObj() {
		return taskObj;
	}

	public void setTaskObj(String taskObj) {
		this.taskObj = taskObj;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}
	
}
