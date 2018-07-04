/*
 * Copyright 2009 Rixty, Inc. All Rights Reserved.
 */

package com.woniu.sncp.pay.core.service.payment.platform.oversea.rixty.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.woniu.sncp.pay.core.service.payment.platform.oversea.rixty.utils.NVPCodec.Field;



/**
 * A java binding to Rixty's Name-Value pair protocol. 
 */
public class NVPClient {

    private static final int PROTOCOL_VERSION = 2;

    private final String rixtyUrl;
    private final String apiUser;
    private final String apiPassword;
    private final String apiSignature;
    
    /**
     * Create an NVPClient to issue transactions using the Rixty NVP API.
     * 
     * See the Rixty Administration Guide
     * for information about setting up your API Profile.
     * 
     * @param rixtyUrl The URL to communicate with Rixty's NVP API
     * @param apiUser The User specified in your API Profile.
     * @param apiPassword The Password specified in your API Profile
     * @param apiSignature The Signature specified in your API Profile
     */
    public NVPClient(String rixtyUrl, String apiUser, String apiPassword, String apiSignature) {
        this.rixtyUrl = rixtyUrl;
        this.apiUser = apiUser;
        this.apiPassword = apiPassword;
        this.apiSignature = apiSignature;
    }
    
    
    /**
     * Send purchase details to Rixty for approval by a Rixty account holder.
     * 
     * @param amt Amount for this transaction
     * @param sku Merchant SKU
     * @param description Description for listing on payments page
     * @param extras Extra name-value pair Strings.  Use NVPCodec.encode(field, value) to construct extra parameters.
     * @return Name-value pairs: {@link NVPCodec.Field#CHECKOUTURL}, {@link NVPCodec.Field#TOKEN},
     * @throws NVPException if unsuccessful
     * @see #getRixtyCheckoutDetails(String token)
     * @see NVPCodec#encode(Field field, String value)
     */
    public NVPCodec setRixtyCheckout(double amt, String sku, String description, String... extras) throws NVPException {
        NVPCodec inputs = new NVPCodec();
        inputs.put(Field.METHOD, "SetRixtyCheckout");
        inputs.put(Field.AMT, amt+"");
        inputs.put(Field.SKU, sku);
        inputs.put(Field.DESC, description);
        return callService(inputs, extras);
    }
    
    public NVPCodec setRixtyCheckout(String... params) throws NVPException {
        NVPCodec inputs = new NVPCodec();
        inputs.put(Field.METHOD, "SetRixtyCheckout");
        return callService(inputs, params);
    }
    
    public NVPCodec setSubscriptionCheckout(double amt, String sku, String description, String term, String... extras) throws NVPException {
        NVPCodec inputs = new NVPCodec();
        inputs.put(Field.METHOD, "SetRixtyCheckout");
        inputs.put(Field.AMT, amt+"");
        inputs.put(Field.SKU, sku);
        inputs.put(Field.DESC, description);
        inputs.put(Field.SUBSCRIPTION, term);
        return callService(inputs, extras);
    }
    
    /**
     * After a user approves a transaction (initiated via setRixtyCheckout), the details of the transaction
     * can be retrieved via getRixtyCheckoutDetails.  The transaction is committed via doRixtyCheckoutPayment().
     * 
     * @param token
     * @return Name-Value pairs {@link NVPCodec.Field#TOKEN}, {@link NVPCodec.Field#SKU}, 
     * {@link NVPCodec.Field#PAYERID}, {@link NVPCodec.Field#COUNTRYCODE}, {@link NVPCodec.Field#BUTTONSOURCE},
     * {@link NVPCodec.Field#AMT}, {@link NVPCodec.Field#CURRENCYCODE}, {@link NVPCodec.Field#NOTIFYURL},
     * {@link NVPCodec.Field#CUSTOM}, {@link NVPCodec.Field#INVNUM}
     * @throws NVPException if unsuccessful
     * @see #doRixtyCheckoutPayment(String token, String payerid)
     */
    public NVPCodec getRixtyCheckoutDetails(String token) throws NVPException {
        NVPCodec inputs = new NVPCodec();
        inputs.put(Field.METHOD, "GetRixtyCheckoutDetails");
        inputs.put(Field.TOKEN, token);
        return callService(inputs);
    }
    
    /**
     * Commit a transaction that has been approved by the Rixty account holder.
     * 
     * @param token The token identifying the transaction, returned by setRixtyCheckout()
     * @param payerid The account ID for the Rixty user.  This ID is passed to your RETURNURL, and can also be retrieved
     * via getRixtyCheckoutDetails()
     * @return Name-Value pairs: {@link NVPCodec.Field#TRANSACTIONID}, {@link NVPCodec.Field#TRANSACTIONTYPE},
     * {@link NVPCodec.Field#AMT}, {@link NVPCodec.Field#RETURNURL}, {@link NVPCodec.Field#SETTLEAMT},
     * {@link NVPCodec.Field#FEEAMT}, {@link NVPCodec.Field#ORDERTIME}, {@link NVPCodec.Field#CURRENCYCODE},
     * {@link NVPCodec.Field#PAYMENTTYPE}, {@link NVPCodec.Field#PAYMENTSTATUS}
     * @param extras Extra name-value pair Strings.  Use NVPCodec.encode(field, value) to construct extra parameters.
     * @throws NVPException if unsuccessful
     * @see #getTransactionDetails(String transactionId)
     */
    public NVPCodec doRixtyCheckoutPayment(String token, String payerid, String... extras) throws NVPException {
        NVPCodec inputs = new NVPCodec();
        inputs.put(Field.METHOD, "DoRixtyCheckoutPayment");
        inputs.put(Field.TOKEN, token);
        inputs.put(Field.PAYERID, payerid);
        return callService(inputs, extras);
    }
    
    /**
     * Renew a subscription
     * @param suscriptionId The subscriptionID originally obtained from getTransactionDetails()
     * @return Name-Value pairs: {@link NVPCodec.Field#SUBSCRIPTIONID} 
     * @throws NVPException if unsuccessful
     * @see #getTransactionDetails(String transactionId)
     */
    public NVPCodec renewSubscription(String subscriptionId) throws NVPException {
        NVPCodec inputs = new NVPCodec();
        inputs.put(Field.METHOD, "RenewSubscription");
        inputs.put(Field.SUBSCRIPTIONID, subscriptionId);
        return callService(inputs);
    }
    
    /**
     * Renew a subscription, providing full details
     * @param suscriptionId The subscriptionID originally obtained from getTransactionDetails()
     * @param start The start date for this subscription period
     * @param end The end date for this subscription period
     * @param amt The renewal amount
     * @return Name-Value pairs: {@link NVPCodec.Field#SUBSCRIPTIONID} 
     * @throws NVPException if unsuccessful
     * @see #getTransactionDetails(String transactionId)
     */
    public NVPCodec renewSubscription(String subscriptionId, Date start, Date end, double amt) throws NVPException {
        NVPCodec inputs = new NVPCodec();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        inputs.put(Field.METHOD, "RenewSubscription");
        inputs.put(Field.SUBSCRIPTIONID, subscriptionId);
        inputs.put(Field.PERIODSTART, sdf.format(start));
        inputs.put(Field.PERIODEND, sdf.format(end));
        inputs.put(Field.RENEWAMT, amt+"");
        return callService(inputs);
    }
    
    /**
     * Cancel a subscription
     * @param suscriptionId The subscriptionID originally obtained from getTransactionDetails()
     * @return Name-Value pairs: {@link NVPCodec.Field#SUBSCRIPTIONID} 
     * @throws NVPException if unsuccessful
     * @see #getTransactionDetails(String transactionId)
     */
    public NVPCodec cancelSubscription(String subscriptionId) throws NVPException {
        NVPCodec inputs = new NVPCodec();
        inputs.put(Field.METHOD, "CancelSubscription");
        inputs.put(Field.SUBSCRIPTIONID, subscriptionId);
        return callService(inputs);
    }

    /**
     * Get Subscription Details
     * @param suscriptionId The subscriptionID originally obtained from getTransactionDetails()
     * @return Name-Value pairs: {@link NVPCodec.Field#SUBSCRIPTIONID}
     * {@link NVPCodec.Field#SUBSCRIPTIONSTATUS}, {@link NVPCodec.Field#SUBSCRIPTION} 
     * {@link NVPCodec.Field#PAYERID} 
     * @throws NVPException if unsuccessful
     * @see #getTransactionDetails(String transactionId)
     */
    public NVPCodec getSubscriptionDetails(String subscriptionId) throws NVPException {
        NVPCodec inputs = new NVPCodec();
        inputs.put(Field.METHOD, "GetSubscriptionDetails");
        inputs.put(Field.SUBSCRIPTIONID, subscriptionId);
        return callService(inputs);
    }
    
    /**
     * Get details of a committed transaction.
     * @param transactionId The transactionID returned by doRixtyCheckoutPayment()
     * @return Name-Value pairs: {@link NVPCodec.Field#TRANSACTIONID}, {@link NVPCodec.Field#TRANSACTIONTYPE},
     * {@link NVPCodec.Field#AMT}, {@link NVPCodec.Field#SETTLEAMT}, {@link NVPCodec.Field#FEEAMT}
     * {@link NVPCodec.Field#ORDERTIME}, {@link NVPCodec.Field#CURRENCYCODE}, {@link NVPCodec.Field#PAYMENTTYPE},
     * {@link NVPCodec.Field#PAYMENTSTATUS}
     * @throws NVPException if unsuccessful
     * @see #doRixtyCheckoutPayment(String token, String payerid)
     */
    public NVPCodec getTransactionDetails(String transactionId) throws NVPException {
        NVPCodec inputs = new NVPCodec();
        inputs.put(Field.METHOD, "GetTransactionDetails");
        inputs.put(Field.TRANSACTIONID, transactionId);
        return callService(inputs);
    }
    
    /**
     * Perform a full refund of a committed transaction.
     * @param transactionId The transactionID returned by doRixtyCheckoutPayment()
     * @param note optional note to associate with this refund (may be null)
     * @return Name-Value pairs: {@link NVPCodec.Field#REFUNDTRANSACTIONID}, {@link NVPCodec.Field#FEEREFUNDAMT},
     * {@link NVPCodec.Field#GROSSREFUNDAMT}, {@link NVPCodec.Field#NETREFUNDAMT}
     * @throws NVPException if unsuccessful
     */
    public NVPCodec refundTransactionFull(String transactionId, String note)
    throws NVPException {
        NVPCodec inputs = new NVPCodec();
        inputs.put(Field.METHOD, "RefundTransaction");
        inputs.put(Field.TRANSACTIONID, transactionId);
        inputs.put(Field.REFUNDTYPE, "Full");
        if (note != null) {
            inputs.put(Field.NOTE, note);
        }
        return callService(inputs);
    }
    
    /**
     * Perform a partial refund of a committed transaction.
     * 
     * @param transactionId The transactionID returned by doRixtyCheckoutPayment()
     * @param amount Amount to refund to the user, in dollars.
     * @param note Optional note to associate wtih this refund (may be null)
     * @return Name-Value pairs: {@link NVPCodec.Field#REFUNDTRANSACTIONID}, {@link NVPCodec.Field#FEEREFUNDAMT},
     * {@link NVPCodec.Field#GROSSREFUNDAMT}, {@link NVPCodec.Field#NETREFUNDAMT}
     * @throws NVPException if unsuccessful
     */
    public NVPCodec refundTransactionPartial(String transactionId, double amount, String note)
    throws NVPException {
        NVPCodec inputs = new NVPCodec();
        inputs.put(Field.METHOD, "RefundTransaction");
        inputs.put(Field.TRANSACTIONID, transactionId);
        inputs.put(Field.REFUNDTYPE, "Partial");
        inputs.put(Field.AMT, amount + "");
        if (note != null) {
            inputs.put(Field.NOTE, note);
        }
        return callService(inputs);
    }
    

    private void addSignature(NVPCodec nvp) {
        nvp.put(Field.USER, apiUser);
        nvp.put(Field.PWD, apiPassword);
        nvp.put(Field.SIGNATURE, apiSignature);
    }
    
    protected NVPCodec callService(NVPCodec params, String... extraParams) throws NVPException {
        addSignature(params);
        try {
            URL url = new URL(rixtyUrl);
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            params.put(Field.VERSION, PROTOCOL_VERSION + "");
            out.write(params.toString());
            for (String param : extraParams) {
                out.write("&" + param);
            }
            out.flush();
    
            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = rd.readLine();
            out.close();
            rd.close();
            NVPCodec result = new NVPCodec(line);
            if (! "Success".equalsIgnoreCase(result.get(Field.ACK))) {
                throw new NVPException(result.get(Field.ERRORMESSAGE), result);
            }
            return result;
        } catch (IOException ioe) {
            throw new NVPException(ioe.getMessage(), null);
        }
    }
}
