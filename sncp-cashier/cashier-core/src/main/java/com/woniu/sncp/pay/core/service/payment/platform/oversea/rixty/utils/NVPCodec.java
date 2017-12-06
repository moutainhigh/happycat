/*
 * Copyright 2009 Rixty, Inc. All Rights Reserved.
 */

package  com.woniu.sncp.pay.core.service.payment.platform.oversea.rixty.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;


/**
 * This is a simple utility for converting between a hashmap of name value pairs
 * and a URL-encoded string suitable for use with the Rixty API.  Common field definitions
 * are defined using the Field enum.
 *
 * To parse a name-value pair string, create an NVPCodec with the String in the constructor, as in:
 * <p><code>
 *   NVPCodec codec = new NVPCodec("TOKEN=123&PAYERID=ABCD");
 * </code></p>
 * Extract values by calling "get", as in:
 * <p><code>
 *    String token = codec.get(Field.TOKEN);
 * </code></p>
 * To generate a name value pair string, create an NVPCodec, and set values on it:
 *<p><code>
 *   NVPCodec codec = new NVPCodec();
 *   codec.put(Field.TOKEN, "123");
 *</code></p>
 * To generate the name value pair string, call toString():
 *<code>String nvpstring = codec.toString();</code>
 */

public class NVPCodec extends HashMap<String, String> {

    private static final long serialVersionUID = 1L;

    public enum Field {
        /** Success or Error status of METHOD */                        ACK,
        /** Dollar amount for a transaction */                          AMT,
        /** URL to direct the browser on Cancel */                      CANCELURL,
        /** HTML target attribute for cancel link */                    CANCELURLTARGET,
        /** URL to direct the browser for payment approval */           CHECKOUTURL,
        /** User Country Code */                                        COUNTRYCODE,
        /** Currency Code for this transaction */                       CURRENCYCODE,
        /** Custom field for your use */                                CUSTOM,
        /** Description of purchase */                                  DESC,
        /** Domain for the USERID field */                              DOMAIN,
        /** Message if ACK is Error */                                  ERRORMESSAGE,
        /** Commission on a transaction */                              FEEAMT, 
        /** Commission refunded */                                      FEEREFUNDAMT,
        /** Amount refunded to payer */                                 GROSSREFUNDAMT,
        /** Banner Image */												HDRIMG,
        /** URL to use for iFrame integration */                        IFRAMEURL,
        /** Invoice Number */                                           INVNUM,
        /** NVP API Method to invoke */                                 METHOD,
        /** Mobile checkout URL */                                      MOBILEURL,
        /** Amount deducted from merchant account */                    NETREFUNDAMT,
        /** Note to associate with a transaction */                     NOTE,
        /** Notification URL associated with merchant */                NOTIFYURL,
        /** Transaction time in ISO 8601 format */                      ORDERTIME,
        /** Rixty PayerID */                                            PAYERID,
        /** Status of Payment */                                        PAYMENTSTATUS,
        /** Currently unused */                                         PAYMENTTYPE,
        /** Process transactions without browser redirect */		    POSTBACKURL,    
        /** Frequency of subscription billing */                        SUBSCRIPTION,
        /** Subscription ID */                                          SUBSCRIPTIONID,
        /** Auto Renew */                                               AUTORENEW,
        /** Price for a subscription renewal **/                        RENEWAMT,
        /** Subscription Status */                                      STATUS,
        /** Subscription Period Start */                                PERIODSTART,
        /** Subscription Period End */                                  PERIODEND,
        /** API Password */                                             PWD, 
        /** Property ID */                                              RECEIVERID,
        /** ID associated with a Refund */                              REFUNDTRANSACTIONID,
        /** Full or Partial Refund */                                   REFUNDTYPE,
        /** URL to direct browser following a successful transaction */ RETURNURL,
        /** HTML target attribute for return link */                    RETURNURLTARGET,
        /** Transaction amount due merchant */                          SETTLEAMT,
        /** API Signature */                                            SIGNATURE,
        /** Merchant SKU for a purchase */                              SKU,
        /** Token to identify a purchase being processed*/              TOKEN,
        /** Identifier for a completed transaction */                   TRANSACTIONID,
        /** Type of transaction */                                      TRANSACTIONTYPE,
        /** API User */                                                 USER,
        /** Merchant's USERID, for support purposes */                  USERID,
        /** For Signature Verification */                               VERIFY_SIGN,
        /** Version of Rixty Protocol */								VERSION;
        
        /** This method can be used for constructing nvp parameters **/
        public String param(String value) {
        	return this.toString() + "=" + value;
        }
        
        public String param(Double value) {
        	return this.toString() + "=" + value;
        }
    }

    public NVPCodec() {
        super();
    }

    public NVPCodec(String s) {
        super();
        this.fromString(s);
    }

    public String put(Field f, String value) {
        return put(f.name(), value);
    }

    public String put(String name, String value) {
        if (name != null && value != null) {
            return super.put(name.toUpperCase(), value);
        } else {
            return null;
        }
    }

    public String get(Field f) {
        return get(f.name());
    }

    public String get(String name) {
        return super.get(name.toUpperCase());
    }
    
    public boolean containsKey(Field f) {
    	return super.containsKey(f.name());
    }

    public void fromString(String nvps) {
        String[] pairs = nvps.split("&");
        for (int i = 0; i < pairs.length; i++) {
            String[] pair = pairs[i].split("=");
            if (pair.length == 2) {
                try {
                    this.put(URLDecoder.decode(pair[0], "UTF-8"), URLDecoder
                            .decode(pair[1], "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                }
            }
        }
    }

    public String toString() {
        StringBuffer nvps = new StringBuffer();
        for (String key : this.keySet()) {
            nvps.append(encode(key, get(key)));
            nvps.append('&');
        }
        if (nvps.length() > 0) {
            nvps.deleteCharAt(nvps.length() - 1); // delete trailing &
        }
        return nvps.toString();
    }
    
    /**
     * Generate a name-value pair for use in with the NVP API
     * @param field NVP Field
     * @param value NVP Value
     * @return URL encoded name=value pair
     */
    public static String encode(Field field, String value) {
        return encode(field.name(), value);
    }

    public static String encode(String key, String val) {
        try {
            return URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(val, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    
}
