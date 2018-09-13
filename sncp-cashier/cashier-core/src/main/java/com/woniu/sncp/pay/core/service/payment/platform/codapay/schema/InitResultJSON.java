package com.woniu.sncp.pay.core.service.payment.platform.codapay.schema;

import java.io.Serializable;

public class InitResultJSON implements Serializable {
	private static final long serialVersionUID = 201203024L;

	private long txnId = 0;

	public InitResultJSON() {
	}

	public InitResultJSON(long txnId) {
		this.txnId = txnId;
	}

	public long getTxnId() {
		return txnId;
	}
	public void setTxnId(long txnId) {
		this.txnId = txnId;
	}
}
