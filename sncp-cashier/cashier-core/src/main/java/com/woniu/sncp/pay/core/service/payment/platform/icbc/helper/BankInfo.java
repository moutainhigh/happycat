package com.woniu.sncp.pay.core.service.payment.platform.icbc.helper;

import javax.xml.bind.annotation.XmlElement;

public class BankInfo {
	
	private String tranBatchNo;
	private String notifyDate;
	//1-“交易成功，已清算” 2-“交易失败” 3-“交易可疑”
	private String tranStat;
	private String comment;
	
	@XmlElement(name="TranBatchNo")
	public String getTranBatchNo() {
		return tranBatchNo;
	}
	public void setTranBatchNo(String tranBatchNo) {
		this.tranBatchNo = tranBatchNo;
	}
	public String getNotifyDate() {
		return notifyDate;
	}
	public void setNotifyDate(String notifyDate) {
		this.notifyDate = notifyDate;
	}
	public String getTranStat() {
		return tranStat;
	}
	public void setTranStat(String tranStat) {
		this.tranStat = tranStat;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	
}
