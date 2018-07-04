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
public class PwgcApiResponse {
	
	private Response response;

	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Response {
		
		private Error error;
		
		private Authentication authentication;
		
		@XmlElement(name="getTransactionPostback")
		private GetTransactionPostback transactionPostback;
		
		public Error getError() {
			return error;
		}
		public void setError(Error error) {
			this.error = error;
		}
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
	


	public static class Params {
		
		private String trackingID;
		
		public Params() {
		}

		public Params(String trackingID) {
			super();
			this.trackingID = trackingID;
		}

		public String getTrackingID() {
			return trackingID;
		}

		public void setTrackingID(String trackingID) {
			this.trackingID = trackingID;
		}
	}
	
}
