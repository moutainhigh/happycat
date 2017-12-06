package com.woniu.sncp.pay.core.service.payment.platform.oversea.openbucks.dto;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * 
 * 
 * <code>
 * &lt;?xml version="1.0" encoding="UTF-8" ?&gt;
 * &lt;pwgc xmlns="http://openbucks.com/xsd/pwgc/2.0"&gt;
 *  &lt;response&gt;
 * &lt;requestID&gt;0&lt;/requestID&gt;
*     &lt;error&gt;
*        &lt;errorCode&gt;0&lt;/errorCode&gt;
*        &lt;errorDescription&gt;OK&lt;/errorDescription&gt;
*     &lt;/error&gt;
*     &lt;payment&gt;
*        &lt;transaction&gt;
*           &lt;transactionID&gt;117830&lt;/transactionID&gt;
*           &lt;pwgcTrackingID&gt;18eb8626-bf63-4312-890b-43bfc05f525e&lt;/pwgcTrackingID&gt;
*           &lt;timestampUTC&gt;2012-10-19 00:13:20&lt;/timestampUTC&gt;
*           &lt;pwgcHash&gt;7f222f3cbefc71e129f1056fed2365b67f15791b3be8d2902b5f170a2e0b8be0&lt;/pwgcHash&gt;
*        &lt;/transaction&gt;
*        &lt;amount&gt;
*           &lt;currencyCode&gt;USD&lt;/currencyCode&gt;
*           &lt;amountValue&gt;0.10&lt;/amountValue&gt;
*        &lt;/amount&gt;
*        &lt;merchantData&gt;
*           &lt;merchantToken&gt;2012-10-18 17:13:0050809b0c8b73c&lt;/merchantToken&gt;
*           &lt;merchantHash&gt;032b5fb9a1aba6d71c92b3cf271522b569a26a43cda191bac2269262270b70b4&lt;/merchantHash&gt;
*           &lt;publicKey&gt;f831c668-9dbf-4a26-8920-02dfdb5408bd&lt;/publicKey&gt;
*           &lt;merchantTrackingID&gt;2012-10-18 17:13:0050809b0c8c327&lt;/merchantTrackingID&gt;
*           &lt;productID&gt;Los Angeles Credits&lt;/productID&gt;
*           &lt;itemDescription&gt;Credits at 1 cents each&lt;/itemDescription&gt;
*           &lt;ratingModel&gt;ESRB&lt;/ratingModel&gt;
*           &lt;productRating&gt;E&lt;/productRating&gt;
*        &lt;/merchantData&gt;
*     &lt;/payment&gt;
*  &lt;/response&gt;
&lt;/pwgc&gt;
</code>
 * 
 * @author caowl
 *
 */
@XmlRootElement(name="pwgc", namespace="http://openbucks.com/xsd/pwgc/2.0")
public class TransactionPostback {
	
	private Payload response;
	
	public Payload getResponse() {
		return response;
	}

	public void setResponse(Payload response) {
		this.response = response;
	}
	
}
