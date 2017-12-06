package com.woniu.sncp.pay.core.service.payment.platform.oversea.openbucks.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author caowl
 *
 */
@XmlRootElement(name="pwgcApi", namespace="http://openbucks.com/xsd/api/1.0")
@XmlAccessorType(XmlAccessType.FIELD)
public class PwgcApiRequest {
	
	private Request request;

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Request {
		
		private Authentication authentication;
		
		@XmlElement(name="getTransactionPostback")
		private GetTransactionPostback transactionPostback;
		
		public Authentication getAuthentication() {
			return authentication;
		}
		public void setAuthentication(Authentication authentication) {
			this.authentication = authentication;
		}
		
		public GetTransactionPostback getTransactionPostback() {
			return transactionPostback;
		}
		public void setTransactionPostback(GetTransactionPostback transactionPostback) {
			this.transactionPostback = transactionPostback;
		}
	}
	


	public static class GetTransactionPostback {
		private String version;
		private Params params;
		
		public GetTransactionPostback() {
		}

		public GetTransactionPostback(String version, String trackingID) {
			super();
			this.version = version;
			this.params = new Params(trackingID);
		}
		
		public String getVersion() {
			return version;
		}
		public void setVersion(String version) {
			this.version = version;
		}
		public Params getParams() {
			return params;
		}
		public void setParams(Params params) {
			this.params = params;
		}
	}
	
}
