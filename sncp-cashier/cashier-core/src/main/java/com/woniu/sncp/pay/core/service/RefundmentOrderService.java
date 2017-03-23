package com.woniu.sncp.pay.core.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;




import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.PaymentMerchant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.dao.BaseHibernateDAO;
import com.woniu.sncp.dao.BaseSessionDAO;
import com.woniu.sncp.jdbc.datasource.DataSourceConstants;
import com.woniu.sncp.jdbc.datasource.DataSourceHolder;
import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.pojo.refund.PayRefundBatch;
import com.woniu.sncp.pojo.refund.PayRefundBatchDetail;

//@Service("refundmentOrderService")
public class RefundmentOrderService{
	
//	final Logger logger = LoggerFactory.getLogger(this.getClass());
//	
//	@Autowired
//	private BaseSessionDAO sessionDao;
//	
//	@Resource
//	private BaseHibernateDAO<PaymentOrder, Long> paymentOrderDao;
//	
//	@Resource
//	private BaseHibernateDAO<PayRefundBatch, Long> refundBatchDao;
//	
//	@Resource
//	private BaseHibernateDAO<PayRefundBatchDetail, Long> refundBatchDetailDao;
//	
//	@Resource
//	private PaymentConstant paymentConstant;
//	
//	@Resource
//	private PaymentMerchantService paymentMerchantService;
//	
//	public void createOrder(PaymentOrder paymentOrder, long issuerId)
//			throws DataAccessException {
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
//		long sequence = sessionDao.findForLong("select sn_imprest.imp_order_sq.nextval from dual");
//
//		Date now = Calendar.getInstance().getTime();
//		paymentOrder.setCreateDate(now); // 时间
//		paymentOrder.setCompleteDate(now);
//		paymentOrder.setId(sequence);
//		
//		paymentOrderDao.save(paymentOrder);
//
//		if (logger.isInfoEnabled())
//			logger.info("支付生成订单成功：" + paymentOrder.getOrderNo());
//	}
//	
//	/**
//	 * 生成订单号并创建订单
//	 * @param paymentOrder
//	 * @param issuerId
//	 * @throws DataAccessException
//	 */
//	public void createOrderAndGenOrderNo(PaymentOrder paymentOrder, long issuerId)
//			throws DataAccessException {
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
//		long sequence = sessionDao.findForLong("select sn_imprest.imp_order_sq.nextval from dual");
//
//		Date now = Calendar.getInstance().getTime();
//		paymentOrder.setCreateDate(now); // 时间
//		paymentOrder.setCompleteDate(now);
//		String today = DateFormatUtils.format(now, "yyyyMMdd");
//		String orderNo = today + "-" + paymentOrder.getPlatformId() + "-"
//				+ StringUtils.leftPad(String.valueOf(issuerId), 3, '0') + "-"
//				+ StringUtils.leftPad(String.valueOf(sequence), 10, '0');
//		
//		if(paymentOrder.getPlatformId() == 4001){
//			//兔兔币支付
//			orderNo = today + "-" + paymentOrder.getMerchantId()+ '-' + paymentOrder.getPlatformId() + "-"
//					+ StringUtils.leftPad(String.valueOf(issuerId), 3, '0') + "-"
//					+ StringUtils.leftPad(String.valueOf(sequence), 10, '0');
//			paymentOrder.setOrderNo(orderNo);
//		}else if(paymentOrder.getPlatformId() == 1012){
//			//中国银行信用卡分期支付平台id
//			orderNo = today + StringUtils.leftPad(String.valueOf(sequence), 10, '0');
//			paymentOrder.setOrderNo(ObjectUtils.toString(orderNo));
//		}else{
//			paymentOrder.setOrderNo(orderNo);
//		}
//		paymentOrder.setCreateDate(now); // 时间
//		paymentOrder.setCompleteDate(now);
//		paymentOrder.setId(sequence);
//		
//		if (StringUtils.isBlank(paymentOrder.getPayPlatformOrderId())){
//			paymentOrder.setPayPlatformOrderId(orderNo);
//		}
//		
//		paymentOrderDao.save(paymentOrder);
//
//		if (logger.isInfoEnabled())
//			logger.info("支付生成订单成功：" + paymentOrder.getOrderNo());
//	}
//
//	public void checkOrderIsProcessed(PaymentOrder paymentOrder)
//			throws OrderIsSuccessException, ValidationException {
//		// 1.已充值
//		// 2.未充值 + 支付失败
//		if (PaymentOrder.IMPREST_STATE_COMPLETED.equals(paymentOrder.getImprestState())) {
//			String msg = "订单已成功支付，勿重复处理:" + paymentOrder.getOrderNo();
//			throw new OrderIsSuccessException(msg);
//		}
//	}
//	
//	public boolean orderIsPayed(PaymentOrder paymentOrder){
//		if (PaymentOrder.PAYMENT_STATE_PAYED.equals(paymentOrder.getPaymentState())
//				&& PaymentOrder.IMPREST_STATE_COMPLETED.equals(paymentOrder.getImprestState())) {
//			return true;
//		}
//		
//		return false;
//	}
//
//	public boolean checkOrderMoney(PaymentOrder paymentOrder, int paymentMoney) {
//		BigDecimal orderMoney = new BigDecimal(paymentOrder.getMoney().toString());
//		int iOrderMoney = orderMoney.multiply(new BigDecimal(100)).intValue();
//		// 订单金额校验
//		if (iOrderMoney != paymentMoney) {
//			if (logger.isInfoEnabled())
//				logger.info("*_*退款金额与原支付订单金额不匹配:我方:" + iOrderMoney + ",对方:" + paymentMoney);
//			return false;
//		}
//		return true;
//	}
//	
//	public boolean checkRefundDetailMoney(PayRefundBatchDetail payRefundBatchDetail, int paymentMoney) {
//		BigDecimal orderMoney = new BigDecimal(payRefundBatchDetail.getMoney().toString());
//		int iOrderMoney = orderMoney.multiply(new BigDecimal(100)).intValue();
//		// 订单金额校验
//		if (iOrderMoney != paymentMoney) {
//			if (logger.isInfoEnabled())
//				logger.info("*_*退款回调金额与请求退款金额不匹配:我方:" + iOrderMoney + ",对方:" + paymentMoney);
//			return false;
//		}
//		return true;
//	}
//
//	public PaymentOrder queryOrder(String orderNo) throws DataAccessException {
//		// 切换中心库
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
//		return paymentOrderDao.findByProperty("orderNo", orderNo);
//	}
//	
//	public PaymentOrder queryOrderByPartnerOrderNo(String pOrderNo) throws DataAccessException {
//		// 切换中心库
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
//		return paymentOrderDao.findByProperty("partnerOrderNo", pOrderNo);
//	}
//	
//	/**
//	 * 通过
//	 * @param merchantid  商户id
//	 * @param pOrderNo    业务方订单号
//	 * @return
//	 * @throws DataAccessException
//	 */
//	public PaymentOrder queryOrderByMidPartnerOrderNo(Long merchantid,String pOrderNo) throws DataAccessException {
//		// 切换中心库
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
//		String[] properties = new String[]{"merchantId","partnerOrderNo"};
//		Object[] values = new Object[]{merchantid,pOrderNo};
//		return paymentOrderDao.findByProperty(properties, values);
//	}
//
//	public PaymentOrder queyrOrderByOppositeOrderNo(String oppositeOrderNo)
//			throws DataAccessException {
//		// 切换中心库
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
//		return paymentOrderDao.findByProperty("payPlatformOrderId", oppositeOrderNo);
//	}
//
//	public void updateOrder(PaymentOrder paymentOrder, String payedState,
//			String imprestState) throws DataAccessException,
//			IllegalArgumentException {
//		logger.info("更改支付订单" + paymentOrder.getOrderNo() + "为：payedState:" + payedState + ",imprestState:"
//				+ imprestState);
//		if (StringUtils.isBlank(payedState) && StringUtils.isBlank(imprestState))
//			throw new IllegalArgumentException("更新订单状态参数错误，payedState和imprestState不能都为空");
//
//		if (StringUtils.isNotBlank(payedState))
//			paymentOrder.setPaymentState(payedState);
//
//		if (StringUtils.isNotBlank(imprestState))
//			paymentOrder.setImprestState(imprestState);
//
//		paymentOrderDao.update(paymentOrder);
//	}
//	
////	public String callback(PaymentOrder paymentOrder,PaymentMerchant payemntMerchnt){
////		
////		Map<String,String> nameValuePair = new HashMap<String,String>();
////		TreeMap<String,String> treeMap = new TreeMap<String,String>();
////		
////		try {
////			treeMap.put("orderno", paymentOrder.getOrderNo());
////			treeMap.put("aid", ObjectUtils.toString(paymentOrder.getAid()));
////			treeMap.put("platformid", ObjectUtils.toString(paymentOrder.getPlatformId()));
////			treeMap.put("imprestmode", ObjectUtils.toString(paymentOrder.getImprestMode()));
////			treeMap.put("money", ObjectUtils.toString(paymentOrder.getMoney()));
////			treeMap.put("paystate", PaymentOrder.PAYMENT_STATE_PAYED);
////			
////			if(!StringUtils.isBlank(paymentOrder.getPartnerOrderNo())){
////				treeMap.put("partnerorderno", paymentOrder.getPartnerOrderNo());
////				nameValuePair.put("partnerorderno", paymentOrder.getPartnerOrderNo());
////			}
////			
////			String keyType = payemntMerchnt.getKeyType();
////			String key = payemntMerchnt.getKey();
////			if(StringUtils.isBlank(key)){
////				logger.error("回调密钥为空,订单号:" + paymentOrder.getOrderNo()+",平台id:"+paymentOrder.getPlatformId());
////				throw new IllegalArgumentException("平台Id["+paymentOrder.getPlatformId()+"]回调密钥为空");
////			}
////			
////			nameValuePair.put("orderno", paymentOrder.getOrderNo());
////			nameValuePair.put("aid", ObjectUtils.toString(paymentOrder.getAid()));
////			nameValuePair.put("platformid", ObjectUtils.toString(paymentOrder.getPlatformId()));
////			nameValuePair.put("imprestmode", ObjectUtils.toString(paymentOrder.getImprestMode()));
////			nameValuePair.put("money", ObjectUtils.toString(paymentOrder.getMoney()));
////			nameValuePair.put("paystate", PaymentOrder.PAYMENT_STATE_PAYED);
////			nameValuePair.put("sign", ObjectUtils.toString(signStr(treeMap,key,keyType)));
////			
////			String backendUrl = paymentOrder.getPartnerBackendUrl();
////			if(StringUtils.isBlank(backendUrl)){
////				throw new IllegalArgumentException("["+ObjectUtils.toString(paymentOrder.getPlatformId())+"]回调地址为空，请检查配置文件[callback.properties]");
////			}
////			logger.info("订单号:" + paymentOrder.getOrderNo() + " 回调地址:"+backendUrl + " 参数:" + nameValuePair);
////			String response = HttpUtils.post(backendUrl, nameValuePair);
////			logger.info("该回调地址["+backendUrl+"],订单号："+paymentOrder.getOrderNo()+",返回值:"+response);
////			return response;
////		} catch (ClientProtocolException e) {
////			logger.error("回调商户出错，"+e.getMessage(),e);
////		} catch (IOException e) {
////			logger.error("回调商户出错，"+e.getMessage(),e);
////		} catch (Exception e){
////			logger.error("回调商户出错，"+e.getMessage(),e);
////		}
////		
////		return "failed";
////	}
//	
//	private String signStr(TreeMap<String,String> treeMap,String key,String keyType) throws Exception{
//		StringBuffer sb = new StringBuffer();
//
//		Iterator<String> iter = treeMap.keySet().iterator();
//		while (iter.hasNext()) {
//			String name = (String) iter.next();
//			sb.append(name).append(treeMap.get(name));
//		}
//		sb.append(key);
//		logger.info("原串:"+sb.toString());
//		return EncryptFactory.getInstance(keyType).sign(sb.toString(), "", "");
//	}
//	
//	
//	
//	/**
//	 * 生成第三方订单号
//	 * 
//	 * @param paymentOrder
//	 * @param issuerId
//	 * @throws DataAccessException
//	 */
//	public String genThirdPartyNo(String second,String third) throws DataAccessException {
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
//		long sequence = sessionDao.findForLong("select sn_imprest.imp_thirdparty_order_sq.nextval from dual");
//
//		Date now = Calendar.getInstance().getTime();
//		String today = DateFormatUtils.format(now, "yyyyMMdd");
//		String orderNo = today  + "-" + second  + "-"
//				+ StringUtils.leftPad(String.valueOf(third), 3, '0') + "-"
//				+ StringUtils.leftPad(String.valueOf(sequence), 10, '0');
//		
//		return orderNo;
//	}
//	
//	
//	public String getOrderMode(String mode,String bankCd){
//		String orderMode = StringUtils.isNotBlank(mode)?mode:"1";
//		if(StringUtils.isNotBlank(bankCd)){
//			orderMode = paymentConstant.getWebBankMap().get(bankCd);
//		}
//		
//		return orderMode;
//	}
//	
//	public String getOrderMode(String platformId){
//		String paymentMode = PaymentProperties.getValue("PAYMENT_MODE_"+platformId);
//		return (StringUtils.isBlank(paymentMode)) ? "1" :  paymentMode;
//	}
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	/**
//	 * 校验退款批次单处理状态
//	 * @param payRefundBatch
//	 * @throws RefundBatchIsSuccessException
//	 * @throws ValidationException
//	 */
//	public void checkRefundBatchIsHandled(PayRefundBatch payRefundBatch)
//			throws RefundBatchIsSuccessException, ValidationException {
//		// 2.退款成功,4.退款失败
//		if ( (PayRefundBatch.REFUNDMENT_STATE_SUCCESS.equals(payRefundBatch.getRefundState()))||
//				(PayRefundBatch.REFUNDMENT_STATE_FAIL.equals(payRefundBatch.getRefundState())) ) {
//			String msg = "退款批次单已处理，勿重复处理:" + payRefundBatch.getBatchNo();
//			throw new RefundBatchIsSuccessException(msg);
//		}
//	}
//	
//	/**
//	 * 校验退款批次处理中
//	 * @param payRefundBatch
//	 * @throws RefundBatchIsSuccessException
//	 * @throws ValidationException
//	 */
//	public void checkRefundBatchIsProcessed(PayRefundBatch payRefundBatch)
//			throws RefundBatchIsSuccessException, ValidationException {
//		// 3.处理中
//		if ( PayRefundBatch.REFUNDMENT_STATE_PROCESS.equals(payRefundBatch.getRefundState())  ) {
//			String msg = "退款批次单处理中，勿重复处理:" + payRefundBatch.getBatchNo();
//			throw new RefundBatchIsSuccessException(msg);
//		}
//	}
//	
//	public boolean checkRefundBatchDetailIsProcessed(PayRefundBatchDetail payRefundBatchDetail)
//			throws RefundBatchIsSuccessException, ValidationException {
//		// 1.已退款,退款失败
//		if ( PayRefundBatch.REFUNDMENT_STATE_SUCCESS.equals(payRefundBatchDetail.getRefundState())||
//						PayRefundBatch.REFUNDMENT_STATE_FAIL.equals(payRefundBatchDetail.getRefundState())  ) {
//			//String msg = "退款批次单明细处理中或已处理，勿重复处理:" + payRefundBatchDetail.getBatchNo();
//			return true;
//		}
//		return false;
//	}
//	
//	/**
//	 * 退款状态
//	 * @param payRefundBatch
//	 * @return
//	 */
//	public boolean refundBatchIsRefunded(PayRefundBatch payRefundBatch){
//		if (RefundmentConstant.PAYMENT_STATE_REFUNDED.equals(payRefundBatch.getRefundState())) {
//			return true;
//		}
//		return false;
//	}
//	
//	/**
//	 * 根据商户批次号查询批次信息
//	 * @param partnerBatchNo
//	 * @return
//	 */
//	public PayRefundBatch queryRefundBatchByPartnerBatchNo(String partnerBatchNo) {
//		// 切换中心库
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
//		return refundBatchDao.findByProperty("partnerBatchNo", partnerBatchNo);
//	}
//	
//	/**
//	 * 根据商户批次号查询批次信息
//	 * @param partnerBatchNo
//	 * @return
//	 */
//	public PayRefundBatch queryRefundBatchByMidPartnerBatchNo(Long merchantid,String partnerBatchNo) {
//		// 切换中心库
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
//		String[] properties = new String[]{"partnerId","partnerBatchNo"};
//		Object[] values = new Object[]{merchantid,partnerBatchNo};
//		return refundBatchDao.findByProperty(properties, values);
//	}
//	
//	/**
//	 * 根据批次号查询批次信息
//	 * @param batchNo
//	 * @return
//	 * @throws DataAccessException
//	 */
//	public PayRefundBatch queryRefundBatch(String batchNo) throws DataAccessException {
//		// 切换中心库
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
//		return refundBatchDao.findByProperty("batchNo", batchNo);
//	}
//	
//	/**
//	 * 退款批次创建
//	 * @param payRefundBatch
//	 * @param l
//	 */
//	public void createRefundBatchAndGenRefundBatchNo(
//			PayRefundBatch payRefundBatch, long l) {
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
//		long sequence = sessionDao.findForLong("select sn_imprest.pay_refund_batch_sq.nextval from dual");
//
//		Date now = Calendar.getInstance().getTime();
//		payRefundBatch.setRefundDate(now);
//		
//		String today = DateFormatUtils.format(now, "yyyyMMddHHmmss");
//		String batchNo = today + payRefundBatch.getPlatformId() + 
//				StringUtils.leftPad(String.valueOf(sequence), 10, '0');
//		payRefundBatch.setId(sequence);//设置id
//		payRefundBatch.setBatchNo(batchNo);//设置批次号
//		payRefundBatch.setRefundState(RefundmentConstant.PAYMENT_STATE_REFUND_INIT);//未退款
//		refundBatchDao.save(payRefundBatch);
//
//		if (logger.isInfoEnabled())
//			logger.info("创建退款批次单成功：" + payRefundBatch.getBatchNo());
//	}
//	
//	/**
//	 * 创建退款订单明细
//	 * @param payRefundBatchDetail
//	 */
//	public void createRefundBatchDetail(
//			PayRefundBatchDetail payRefundBatchDetail) {
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
//		long sequence = sessionDao.findForLong("select sn_imprest.pay_refund_batch_dtl_sq.nextval from dual");
//		Date now = Calendar.getInstance().getTime();
//		payRefundBatchDetail.setHandDate(now);
//		payRefundBatchDetail.setId(sequence);//设置id
//		payRefundBatchDetail.setRefundState(RefundmentConstant.PAYMENT_STATE_REFUND_INIT);//未退款
//		
//		refundBatchDetailDao.save(payRefundBatchDetail);
//		
//		if (logger.isInfoEnabled())
//			logger.info("创建退款批次明细单成功：" + payRefundBatchDetail.getOrderNo());
//	}
//	
//	
//	/**
//	 * 通过partnerBatchNo
//	 * 获取退款批次明细列表
//	 * @param partnerBatchNo
//	 * @return
//	 */
//	public List<PayRefundBatchDetail> getRefundBatchDetailByPBatchNo(String partnerBatchNo){
//		return refundBatchDetailDao.findAllByProperty("partnerBatchNo", partnerBatchNo);
//	}
//	
//	/**
//	 * 请求业务方校验订单
//	 * @param params
//	 */
//	public void checkOrder(String pBatchNo,String verifyurl,String orderno,long partnerId,String amount,Platform platform,PaymentOrder paymentOrder) {
//		Map<String,String> nameValuePair = new HashMap<String,String>();
//		try {
//			
//			StringBuffer sourceStr = new StringBuffer();
//			if(StringUtils.isNotBlank(RefundmentConstant.TRADE_ACTION_REFUND)){
//				sourceStr.append("action"+RefundmentConstant.TRADE_ACTION_REFUND);
//			}
//			if(StringUtils.isNotBlank(amount)){
//				sourceStr.append("amount"+amount);
//			}
//			if(StringUtils.isNotBlank(orderno)){
//				sourceStr.append("orderno"+orderno);
//			}
//			if(StringUtils.isNotBlank(pBatchNo)){
//				sourceStr.append("partnerbatchno"+pBatchNo);
//			}
//			if(partnerId>0){
//				sourceStr.append("partnerid"+partnerId);
//			}
//			
//			PaymentMerchant payemntMerchnt = paymentMerchantService.queryPayemntMerchnt(partnerId);
//			
//			//request请求参数
//			nameValuePair.put("partnerbatchno", pBatchNo);
//			nameValuePair.put("partnerid", ObjectUtils.toString(partnerId));
//			nameValuePair.put("orderno", orderno);
//			//nameValuePair.put("productid", productid);//商品id
//			//nameValuePair.put("account", ObjectUtils.toString(paymentOrder.getAid()));
//			nameValuePair.put("amount", amount);
//			nameValuePair.put("action", RefundmentConstant.TRADE_ACTION_REFUND);
//			
//			String extend = platform.getExtend();
//			String verifyKey = "";//格式V,m,s
////			if(StringUtils.isNotEmpty(extend)){
////				JSONObject extJson = JSONObject.parseObject(extend);
////				verifyKey = extJson.getString("verifyKey");
////			}
//			verifyKey = payemntMerchnt.getKey();
//			if(StringUtils.isBlank(verifyKey)){
//				logger.error("业务方校验密钥为空,批次单号:" + pBatchNo+",平台id:"+partnerId);
//				throw new IllegalArgumentException("平台Id["+partnerId+"]回调密钥为空");
//			}
//			
//			if (logger.isInfoEnabled())
//				logger.info("业务方订单校验：" + sourceStr.toString());
//			
//			String signStr = MD5Encrypt.encrypt(sourceStr.toString() + verifyKey);
//			nameValuePair.put("sign", signStr);
//			
//			//String checkUrl = platform.getPayCheckUrl();//需要增加退款校验地址
//			String response = HttpUtils.post(verifyurl, nameValuePair);
//			
//			if(null!=response){
//				//将实体对象转换为JSON Object转换  
//			    JSONObject responseJSONObject = JSONObject.parseObject(response);
//			    if("200".equals(responseJSONObject.get("status"))){
//			    	if (logger.isInfoEnabled())
//						logger.info("订单校验成功：" + responseJSONObject.get("data"));
//			    }else{
//			    	logger.error("回调商户订单校验失败，response:"+responseJSONObject);
//			    	throw new IllegalArgumentException("回调商户订单校验失败，response:"+responseJSONObject);
//			    }
//			}
//		} catch (ClientProtocolException e) {
//			logger.error("回调商户出错，"+e.getMessage(),e);
//			throw new IllegalArgumentException(e);
//		} catch (IOException e) {
//			logger.error("回调商户出错，"+e.getMessage(),e);
//			throw new IllegalArgumentException(e);
//		}
//	}
//	
//	/**
//	 * 退款回调业务商户
//	 * @param paymentOrder
//	 * @param payemntMerchnt
//	 * @return
//	 */
//	public String callback(PaymentOrder paymentOrder,PaymentMerchant payemntMerchnt){
//		
//		Map<String,String> nameValuePair = new HashMap<String,String>();
//		TreeMap<String,String> treeMap = new TreeMap<String,String>();
//		
//		try {
//			treeMap.put("orderno", paymentOrder.getOrderNo());
//			treeMap.put("aid", ObjectUtils.toString(paymentOrder.getAid()));
//			treeMap.put("platformid", ObjectUtils.toString(paymentOrder.getPlatformId()));
//			treeMap.put("imprestmode", ObjectUtils.toString(paymentOrder.getImprestMode()));
//			treeMap.put("money", ObjectUtils.toString(paymentOrder.getMoney()));
//			treeMap.put("paystate", PaymentOrder.PAYMENT_STATE_PAYED);
//			
//			if(!StringUtils.isBlank(paymentOrder.getPartnerOrderNo())){
//				treeMap.put("partnerorderno", paymentOrder.getPartnerOrderNo());
//				nameValuePair.put("partnerorderno", paymentOrder.getPartnerOrderNo());
//			}
//			
//			String keyType = payemntMerchnt.getKeyType();
//			String key = payemntMerchnt.getKey();
//			if(StringUtils.isBlank(key)){
//				logger.error("回调密钥为空,订单号:" + paymentOrder.getOrderNo()+",平台id:"+paymentOrder.getPlatformId());
//				throw new IllegalArgumentException("平台Id["+paymentOrder.getPlatformId()+"]回调密钥为空");
//			}
//			
//			nameValuePair.put("orderno", paymentOrder.getOrderNo());
//			nameValuePair.put("aid", ObjectUtils.toString(paymentOrder.getAid()));
//			nameValuePair.put("platformid", ObjectUtils.toString(paymentOrder.getPlatformId()));
//			nameValuePair.put("imprestmode", ObjectUtils.toString(paymentOrder.getImprestMode()));
//			nameValuePair.put("money", ObjectUtils.toString(paymentOrder.getMoney()));
//			nameValuePair.put("paystate", PaymentOrder.PAYMENT_STATE_PAYED);
//			nameValuePair.put("sign", ObjectUtils.toString(signStr(treeMap,key,keyType)));
//			
//			String backendUrl = paymentOrder.getPartnerBackendUrl();
//			if(StringUtils.isBlank(backendUrl)){
//				throw new IllegalArgumentException("["+ObjectUtils.toString(paymentOrder.getPlatformId())+"]回调地址为空，请检查配置文件[callback.properties]");
//			}
//			logger.info("订单号:" + paymentOrder.getOrderNo() + " 回调地址:"+backendUrl + " 参数:" + nameValuePair);
//			String response = HttpUtils.post(backendUrl, nameValuePair);
//			logger.info("该回调地址["+backendUrl+"],订单号："+paymentOrder.getOrderNo()+",返回值:"+response);
//			return response;
//		} catch (ClientProtocolException e) {
//			logger.error("回调商户出错，"+e.getMessage(),e);
//		} catch (IOException e) {
//			logger.error("回调商户出错，"+e.getMessage(),e);
//		} catch (Exception e){
//			logger.error("回调商户出错，"+e.getMessage(),e);
//		}
//		
//		return "failed";
//	}
//	
//	
//	public static void main(String[] args) throws ClientProtocolException, IOException {
////		Map<String,String> nameValuePair = new HashMap<String,String>();
////		
////		nameValuePair.put("orderno", "20130829-141-028-0000010105");
////		nameValuePair.put("aid", "1502527677");
////		nameValuePair.put("platformid", "187");
////		nameValuePair.put("imprestmode", "D");
////		nameValuePair.put("money", "0.01");
////		nameValuePair.put("paystate", "1");
////		nameValuePair.put("sign", "2658F5F4AE76B28850F9F18068A03A78");
////		
////		PaymentOrderService pos = new PaymentOrderService();
////		System.out.println(pos.httpPost("http://mobile.woniu.com/web/eshop/pay/return", nameValuePair));
////		Date now = Calendar.getInstance().getTime();
//////		System.out.println(now);
//////		System.out.println(DateFormatUtils.format(now, "yyyyMMddHHmmss"));
////		String today = DateFormatUtils.format(now, "yyyyMMddHHmmss");
////		String sequence = "010";
////		String batchNo = today + 419 + 
////				StringUtils.leftPad(String.valueOf(sequence), 10, '0');
////		System.out.println(batchNo);
//		RefundmentOrderService oService = new RefundmentOrderService();
//		String verifyurl = "";
//		String orderno = "20130829-141-028-0000010105";
//		long productid = 10004;
//		String amount = "0.01";
//		Platform platform = new Platform();
//		platform.setPayCheckUrl("http://localhost:85/api/refundment/test");
//		platform.setMerchantNo("20541222");
//		
//		PaymentOrder paymentOrder = new PaymentOrder();
//		paymentOrder.setAid(2000L);
//		oService.checkOrder("",verifyurl,orderno,productid,amount,platform,paymentOrder);
//	}
//
//	
//	/**
//	 * 退款回调
//	 * @param refundBatch
//	 * @param payemntMerchnt
//	 * @return
//	 */
//	public String refundCallback(PayRefundBatch refundBatch,
//			PaymentMerchant payemntMerchnt,Map<String,Object> inParams) {
//		Map<String,String> nameValuePair = new HashMap<String,String>();
//		TreeMap<String,String> treeMap = new TreeMap<String,String>();
//		
//		try {
//			treeMap.put("partnerbatchno", refundBatch.getPartnerBatchNo());
//			treeMap.put("notifytype", "trade_status_sync");
//			treeMap.put("platformid", ObjectUtils.toString(refundBatch.getPlatformId()));
//			treeMap.put("resultdetails", ObjectUtils.toString(inParams.get(RefundmentConstant.REFUND_RESULT_DETAILS)));
//			treeMap.put("successnum", ObjectUtils.toString(inParams.get(RefundmentConstant.REFUND_SUCCESS_NUM)));
//			
//			String keyType = payemntMerchnt.getKeyType();
//			String key = payemntMerchnt.getKey();
//			if(StringUtils.isBlank(key)){
//				logger.error("回调密钥为空,批次单号:" + refundBatch.getBatchNo()+",平台id:"+refundBatch.getPlatformId());
//				throw new IllegalArgumentException("平台Id["+refundBatch.getPlatformId()+"]回调密钥为空");
//			}
//			
//			nameValuePair.put("partnerbatchno",refundBatch.getPartnerBatchNo());
//			nameValuePair.put("notifytype", "trade_status_sync");
//			nameValuePair.put("resultdetails", ObjectUtils.toString(inParams.get(RefundmentConstant.REFUND_RESULT_DETAILS)));
//			nameValuePair.put("successnum", ObjectUtils.toString(inParams.get(RefundmentConstant.REFUND_SUCCESS_NUM)));
//			nameValuePair.put("platformid", ObjectUtils.toString(refundBatch.getPlatformId()));
//			nameValuePair.put("sign", ObjectUtils.toString(signStr(treeMap,key,keyType)));
//			
//			String backendUrl = refundBatch.getPartnerBackendUrl();
//			if(StringUtils.isBlank(backendUrl)){
//				throw new IllegalArgumentException("["+ObjectUtils.toString(refundBatch.getPlatformId())+"]回调地址为空，请检查配置文件[callback.properties]");
//			}
//			String response = HttpUtils.post(backendUrl, nameValuePair);
//			logger.info("批次单号:" + refundBatch.getBatchNo() + " 回调地址:"+backendUrl + " 参数:" + nameValuePair);
//			logger.info("该回调地址["+backendUrl+"],批次单号:" + refundBatch.getBatchNo()+",返回值:"+response);
//			return response;
//		} catch (ClientProtocolException e) {
//			logger.error("回调商户出错，"+e.getMessage(),e);
//		} catch (IOException e) {
//			logger.error("回调商户出错，"+e.getMessage(),e);
//		} catch (Exception e){
//			logger.error("回调商户出错，"+e.getMessage(),e);
//		}
//		
//		return "failed";
//	}
//	
//	/**
//	 * 更新退款批次明细状态
//	 * @param payRefundBatchDetail
//	 * @param refundState
//	 * @throws DataAccessException
//	 * @throws IllegalArgumentException
//	 */
//	public void updateRefundBatchDetail(PayRefundBatchDetail payRefundBatchDetail, String refundState) throws DataAccessException,
//			IllegalArgumentException {
//		logger.info("更改退款明细单" + payRefundBatchDetail.getOrderNo() + "为：refundState:" + refundState);
//		if (StringUtils.isBlank(refundState))
//			throw new IllegalArgumentException("更新退款明细单状态参数错误，refundState不能都为空");
//
//		if (StringUtils.isNotBlank(refundState))
//			payRefundBatchDetail.setRefundState(refundState);
//
//		refundBatchDetailDao.update(payRefundBatchDetail);
//	}
//	/**
//	 * 通过批次号和业务方订单号 查询退款批次明细
//	 * @param batchNo
//	 * @param pOrderNo
//	 * @return
//	 */
//	public PayRefundBatchDetail queryRefundBatchDetail(String batchNo,
//			String pOrderNo) {
//		String[] properties = new String[]{"batchNo","partnerOrderNo"};
//		Object[] values = new Object[]{batchNo,pOrderNo};
//		return refundBatchDetailDao.findByProperty(properties, values);
//	}
//	
//	/**
//	 * 通过计费分配商户id和业务方订单号 查询退款批次明细
//	 * @param merchantId
//	 * @param pOrderNo
//	 * @return
//	 */
//	public PayRefundBatchDetail queryRefundBatchDetailByMidPno(Long merchantId,String pOrderNo) {
//		String[] properties = new String[]{"partnerId","partnerOrderNo"};
//		Object[] values = new Object[]{merchantId,pOrderNo};
//		return refundBatchDetailDao.findByProperty(properties, values);
//	}
//	
//	/**
//	 * 更新批次单状态
//	 * @param refundBatch
//	 * @param paymentStateRefunded
//	 */
//	public void updateRefundBatch(PayRefundBatch refundBatch,
//			String paymentStateRefunded) {
//		refundBatch.setRefundState(paymentStateRefunded);
//		refundBatchDao.update(refundBatch);
//	}
	
	
}
