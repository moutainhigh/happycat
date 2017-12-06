package com.woniu.sncp.pay.core.service.payment.platform.oversea.xsolla.dto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 *  
 *  xsolla消息格式
 * 
 */
public abstract class XsollaResponseBase {
	
	public static Map<String, String> payComment = new HashMap<>(7);
	public static Map<String, String> cancelComment = new HashMap<>(7);
	public static Map<String, String> checkComment = new HashMap<>(7);
	
	static {
		cancelComment.put("0", "Payment indicated in the request was successfully cancelled");
		cancelComment.put("2", "Payment indicated in the request was not found");
		cancelComment.put("7", "Payment indicated in the request cannot be cancelled");
		
		checkComment.put("0", "It is possible to increase balance of the user ID indicated in the request.After successful status check, the system will send balance increaserequest.");
		checkComment.put("7", "Account is disabled or not present");
		
		payComment.put("0", "Ok");
		payComment.put("1", "Temporary error, retry request later");
		payComment.put("2", "Invalid user ID");
		payComment.put("3", "Invalid MD5 signature");
		payComment.put("4", "Invalid request format (invalid amount, some parameters are missing)");
		payComment.put("5", "Another error (to be described in comment)");
		payComment.put("7", "Certain user's payment cannot be processed/denied due to technical reasons");
	}
	
	public static double div(double v1,double v2,int scale){
		if(scale<0){
			throw new IllegalArgumentException(
			"The scale must be a positive integer or zero");
		}
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.divide(b2,scale,BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
	
	private String result;
	private String comment;
	
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
