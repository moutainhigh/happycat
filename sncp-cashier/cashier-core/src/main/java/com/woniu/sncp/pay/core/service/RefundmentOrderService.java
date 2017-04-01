package com.woniu.sncp.pay.core.service;


import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.woniu.pay.common.utils.RefundmentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.pay.common.exception.OrderIsSuccessException;
import com.woniu.sncp.pay.common.exception.RefundBatchIsSuccessException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.encrypt.EncryptFactory;
import com.woniu.sncp.pay.common.utils.encrypt.MD5Encrypt;
import com.woniu.sncp.pay.common.utils.http.HttpUtils;
import com.woniu.sncp.pay.core.service.dataroute.PayConfigToute;
import com.woniu.sncp.pay.dao.BaseSessionDAO;
import com.woniu.sncp.pay.dao.PaymentOrderDao;
import com.woniu.sncp.pay.repository.pay.PaymentMerchant;
import com.woniu.sncp.pay.repository.pay.PaymentOrderRepository;
import com.woniu.sncp.pay.repository.pay.RefundBatchDetailRepository;
import com.woniu.sncp.pay.repository.pay.RefundBatchRepository;
import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.pojo.refund.PayRefundBatch;
import com.woniu.sncp.pojo.refund.PayRefundBatchDetail;


@Service("refundmentOrderService")
public class RefundmentOrderService{
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	RefundBatchRepository refundBatchRepository;
	
	@Autowired
	RefundBatchDetailRepository refundBatchDetailRepository;
	
	@Autowired
	PaymentOrderRepository paymentOrderRepository;
	
	@Autowired
	private BaseSessionDAO sessionDao;
	
	@Autowired
	private PaymentMerchantService paymentMerchantService;
	
	@Resource
	private PayConfigToute payConfigToute;
	
	@Resource 
	PaymentOrderDao paymentOrderDao;
	
	public void checkOrderIsProcessed(PaymentOrder paymentOrder)
			throws OrderIsSuccessException, ValidationException {
		// 1.已充值
		// 2.未充值 + 支付失败
		if (PaymentOrder.IMPREST_STATE_COMPLETED.equals(paymentOrder.getState())) {
			String msg = "订单已成功支付，勿重复处理:" + paymentOrder.getOrderNo();
			throw new OrderIsSuccessException(msg);
		}
	}

	public boolean checkOrderMoney(PaymentOrder paymentOrder, int paymentMoney) {
		BigDecimal orderMoney = new BigDecimal(paymentOrder.getMoney().toString());
		int iOrderMoney = orderMoney.multiply(new BigDecimal(100)).intValue();
		// 订单金额校验
		if (iOrderMoney != paymentMoney) {
			if (logger.isInfoEnabled())
				logger.info("*_*退款金额与原支付订单金额不匹配:我方:" + iOrderMoney + ",对方:" + paymentMoney);
			return false;
		}
		return true;
	}
	
	public boolean checkRefundDetailMoney(PayRefundBatchDetail payRefundBatchDetail, int paymentMoney) {
		BigDecimal orderMoney = new BigDecimal(payRefundBatchDetail.getMoney().toString());
		int iOrderMoney = orderMoney.multiply(new BigDecimal(100)).intValue();
		// 订单金额校验
		if (iOrderMoney != paymentMoney) {
			if (logger.isInfoEnabled())
				logger.info("*_*退款回调金额与请求退款金额不匹配:我方:" + iOrderMoney + ",对方:" + paymentMoney);
			return false;
		}
		return true;
	}

	/**
	 * 校验退款批次单处理状态
	 * @param payRefundBatch
	 * @throws RefundBatchIsSuccessException
	 * @throws ValidationException
	 */
	public void checkRefundBatchIsHandled(PayRefundBatch payRefundBatch)
			throws RefundBatchIsSuccessException, ValidationException {
		// 2.退款成功,4.退款失败
		if ( (PayRefundBatch.REFUNDMENT_STATE_SUCCESS.equals(payRefundBatch.getRefundState()))||
				(PayRefundBatch.REFUNDMENT_STATE_FAIL.equals(payRefundBatch.getRefundState())) ) {
			String msg = "退款批次单已处理，勿重复处理:" + payRefundBatch.getBatchNo();
			throw new RefundBatchIsSuccessException(msg);
		}
	}
	
	/**
	 * 校验退款批次处理中
	 * @param payRefundBatch
	 * @throws RefundBatchIsSuccessException
	 * @throws ValidationException
	 */
	public void checkRefundBatchIsProcessed(PayRefundBatch payRefundBatch)
			throws RefundBatchIsSuccessException, ValidationException {
		// 3.处理中
		if ( PayRefundBatch.REFUNDMENT_STATE_PROCESS.equals(payRefundBatch.getRefundState())  ) {
			String msg = "退款批次单处理中，勿重复处理:" + payRefundBatch.getBatchNo();
			throw new RefundBatchIsSuccessException(msg);
		}
	}
	
	public boolean checkRefundBatchDetailIsProcessed(PayRefundBatchDetail payRefundBatchDetail)
			throws RefundBatchIsSuccessException, ValidationException {
		// 1.已退款,退款失败
		if ( PayRefundBatch.REFUNDMENT_STATE_SUCCESS.equals(payRefundBatchDetail.getRefundState())||
						PayRefundBatch.REFUNDMENT_STATE_FAIL.equals(payRefundBatchDetail.getRefundState())  ) {
			//String msg = "退款批次单明细处理中或已处理，勿重复处理:" + payRefundBatchDetail.getBatchNo();
			return true;
		}
		return false;
	}
	
	/**
	 * 退款状态
	 * @param payRefundBatch
	 * @return
	 */
	public boolean refundBatchIsRefunded(PayRefundBatch payRefundBatch){
		if (RefundmentConstant.PAYMENT_STATE_REFUNDED.equals(payRefundBatch.getRefundState())) {
			return true;
		}
		return false;
	}

	/**
	 * 根据批次号查询批次信息
	 * @param batchNo
	 * @return
	 * @throws DataAccessException
	 */
	public PayRefundBatch queryRefundBatch(String batchNo) throws DataAccessException {
		return refundBatchRepository.findByBatchNo(batchNo);
	}
	/**
	 * 根据商户批次号查询批次信息
	 * @param partnerBatchNo
	 * @return
	 */
	public PayRefundBatch queryRefundBatchByPartnerBatchNo(String partnerBatchNo) {
		return refundBatchRepository.findByPartnerBatchNo(partnerBatchNo);
	}
	
	/**
	 * 退款批次创建
	 * @param payRefundBatch
	 * @param l
	 */
	public void createRefundBatchAndGenRefundBatchNo(
			PayRefundBatch payRefundBatch, long l) {

		final String orderSeqSql = "INSERT INTO SN_PAY.PAY_REFUND_BATCH_SQ(N_ID) VALUES(NULL)";
		Long sequence = getSequence(orderSeqSql);
		
		
		Date now = Calendar.getInstance().getTime();
		payRefundBatch.setRefundDate(now);
		
		String today = DateFormatUtils.format(now, "yyyyMMddHHmmss");
		String batchNo = today + payRefundBatch.getPlatformId() + 
				StringUtils.leftPad(String.valueOf(sequence), 10, '0');
		payRefundBatch.setId(sequence);//设置id
		payRefundBatch.setBatchNo(batchNo);//设置批次号
		payRefundBatch.setRefundState(RefundmentConstant.PAYMENT_STATE_REFUND_INIT);//未退款

		refundBatchRepository.save(payRefundBatch);

		if (logger.isInfoEnabled())
			logger.info("创建退款批次单成功：" + payRefundBatch.getBatchNo());
	}
	
	/**
	 * 创建退款订单明细
	 * @param payRefundBatchDetail
	 */
	public void createRefundBatchDetail(
			PayRefundBatchDetail payRefundBatchDetail) {
		final String orderSeqSql = "INSERT INTO SN_PAY.PAY_REFUND_BATCH_DTL_SQ(N_ID) VALUES(NULL)";
		Long sequence = getSequence(orderSeqSql);
		
		Date now = Calendar.getInstance().getTime();
		payRefundBatchDetail.setHandDate(now);
		payRefundBatchDetail.setId(sequence);//设置id
		payRefundBatchDetail.setRefundState(RefundmentConstant.PAYMENT_STATE_REFUND_INIT);//未退款
		
		refundBatchDetailRepository.save(payRefundBatchDetail);
		
		if (logger.isInfoEnabled())
			logger.info("创建退款批次明细单成功：" + payRefundBatchDetail.getOrderNo());
	}
	
	
	/**
	 * 通过partnerBatchNo
	 * 获取退款批次明细列表
	 * @param partnerBatchNo
	 * @return
	 */
	public List<PayRefundBatchDetail> getRefundBatchDetailByPBatchNo(String partnerBatchNo){
		return refundBatchDetailRepository.findAll(new Specification<PayRefundBatchDetail>() {  
			@Override
			public Predicate toPredicate(Root<PayRefundBatchDetail> root,  
		            CriteriaQuery<?> query, CriteriaBuilder cb) {  
		        List<Predicate> predicate = new ArrayList<Predicate>();  
		        if(StringUtils.isNotBlank(partnerBatchNo)){  
		        	predicate.add(cb.equal(root.get("partnerBatchNo").as(String.class), partnerBatchNo));  
		        }
		        Predicate[] p = new Predicate[predicate.size()];
		        return cb.and(predicate.toArray(p));  
		    }
		});
	}
	
	/**
	 * 根据商户批次号查询批次信息
	 * @param partnerBatchNo
	 * @return
	 */
	public PayRefundBatch queryRefundBatchByMidPartnerBatchNo(Long merchantid,String partnerBatchNo) {
		return refundBatchRepository.findByPartnerIdAndPartnerBatchNo(merchantid,partnerBatchNo);
	}
	
	private String signStr(TreeMap<String,String> treeMap,String key,String keyType) throws Exception{
		StringBuffer sb = new StringBuffer();

		Iterator<String> iter = treeMap.keySet().iterator();
		while (iter.hasNext()) {
			String name = (String) iter.next();
			sb.append(name).append(treeMap.get(name));
		}
		sb.append(key);
		logger.info("原串:"+sb.toString());
		return EncryptFactory.getInstance(keyType).sign(sb.toString(), "", "");
	}
	
	/**
	 * 退款回调
	 * @param refundBatch
	 * @param payemntMerchnt
	 * @return
	 */
	public String refundCallback(PayRefundBatch refundBatch,
			PaymentMerchant payemntMerchnt,Map<String,Object> inParams) {
		Map<String,String> nameValuePair = new HashMap<String,String>();
		TreeMap<String,String> treeMap = new TreeMap<String,String>();
		
		try {
			treeMap.put("partnerbatchno", refundBatch.getPartnerBatchNo());
			treeMap.put("notifytype", "trade_status_sync");
			treeMap.put("platformid", ObjectUtils.toString(refundBatch.getPlatformId()));
			treeMap.put("resultdetails", ObjectUtils.toString(inParams.get(RefundmentConstant.REFUND_RESULT_DETAILS)));
			treeMap.put("successnum", ObjectUtils.toString(inParams.get(RefundmentConstant.REFUND_SUCCESS_NUM)));
			
			String keyType = payemntMerchnt.getKeyType();
			String key = payemntMerchnt.getMerchantKey();
			if(StringUtils.isBlank(key)){
				logger.error("回调密钥为空,批次单号:" + refundBatch.getBatchNo()+",平台id:"+refundBatch.getPlatformId());
				throw new IllegalArgumentException("平台Id["+refundBatch.getPlatformId()+"]回调密钥为空");
			}
			
			nameValuePair.put("partnerbatchno",refundBatch.getPartnerBatchNo());
			nameValuePair.put("notifytype", "trade_status_sync");
			nameValuePair.put("resultdetails", ObjectUtils.toString(inParams.get(RefundmentConstant.REFUND_RESULT_DETAILS)));
			nameValuePair.put("successnum", ObjectUtils.toString(inParams.get(RefundmentConstant.REFUND_SUCCESS_NUM)));
			nameValuePair.put("platformid", ObjectUtils.toString(refundBatch.getPlatformId()));
			nameValuePair.put("sign", ObjectUtils.toString(signStr(treeMap,key,keyType)));
			
			String backendUrl = refundBatch.getPartnerBackendUrl();
			if(StringUtils.isBlank(backendUrl)){
				throw new IllegalArgumentException("["+ObjectUtils.toString(refundBatch.getPlatformId())+"]回调地址为空，请检查配置文件[callback.properties]");
			}
			String response = HttpUtils.post(backendUrl, nameValuePair);
			logger.info("批次单号:" + refundBatch.getBatchNo() + " 回调地址:"+backendUrl + " 参数:" + nameValuePair);
			logger.info("该回调地址["+backendUrl+"],批次单号:" + refundBatch.getBatchNo()+",返回值:"+response);
			return response;
		} catch (ClientProtocolException e) {
			logger.error("回调商户出错，"+e.getMessage(),e);
		} catch (IOException e) {
			logger.error("回调商户出错，"+e.getMessage(),e);
		} catch (Exception e){
			logger.error("回调商户出错，"+e.getMessage(),e);
		}
		
		return "failed";
	}
	
	/**
	 * 更新退款批次明细状态
	 * @param payRefundBatchDetail
	 * @param refundState
	 * @throws DataAccessException
	 * @throws IllegalArgumentException
	 */
	public void updateRefundBatchDetail(PayRefundBatchDetail payRefundBatchDetail, String refundState) throws DataAccessException,
			IllegalArgumentException {
		logger.info("更改退款明细单" + payRefundBatchDetail.getOrderNo() + "为：refundState:" + refundState);
		if (StringUtils.isBlank(refundState))
			throw new IllegalArgumentException("更新退款明细单状态参数错误，refundState不能都为空");

		refundBatchDetailRepository.updateS(payRefundBatchDetail.getId(), refundState);
	}
	/**
	 * 通过批次号和业务方订单号 查询退款批次明细
	 * @param batchNo
	 * @param pOrderNo
	 * @return
	 */
	public PayRefundBatchDetail queryRefundBatchDetail(String batchNo,
			String pOrderNo) {
		return refundBatchDetailRepository.findByBatchNoAndPartnerBatchNo(batchNo,pOrderNo);
	}
	
	/**
	 * 通过计费分配商户id和业务方订单号 查询退款批次明细
	 * @param merchantId
	 * @param pOrderNo
	 * @return
	 */
	public PayRefundBatchDetail queryRefundBatchDetailByMidPno(Long merchantId,String pOrderNo) {
		return refundBatchDetailRepository.findByPartnerIdAndPartnerOrderNo(merchantId,pOrderNo);
	}
	
	/**
	 * 更新批次单状态
	 * @param refundBatch
	 * @param paymentStateRefunded
	 */
	public void updateRefundBatch(PayRefundBatch refundBatch,
			String paymentStateRefunded) {
		refundBatchRepository.updateS(refundBatch.getId(),paymentStateRefunded);
	}
	
	/**
	 * 通过sql返回当前自增序列值
	 * @param getSeqSql
	 * @return
	 */
	public Long getSequence(final String getSeqSql){
		KeyHolder keyHolder = new GeneratedKeyHolder();
		sessionDao.getMyJdbcTemplate().update(new PreparedStatementCreator() {  
            public PreparedStatement createPreparedStatement(  
                    Connection connection) throws SQLException {  
                PreparedStatement ps = connection.prepareStatement(getSeqSql,PreparedStatement.RETURN_GENERATED_KEYS);  
                return ps;  
            }  
        }, keyHolder); 
		Long sequence = keyHolder.getKey().longValue();
		return sequence;
	}
	
	
	/**
	 * 请求业务方校验订单
	 * @param params
	 */
	public void checkOrder(String pBatchNo,String verifyurl,String orderno,long partnerId,String amount,Platform platform,PaymentOrder paymentOrder) {
		Map<String,String> nameValuePair = new HashMap<String,String>();
		try {
			
			StringBuffer sourceStr = new StringBuffer();
			if(StringUtils.isNotBlank(RefundmentConstant.TRADE_ACTION_REFUND)){
				sourceStr.append("action"+RefundmentConstant.TRADE_ACTION_REFUND);
			}
			if(StringUtils.isNotBlank(amount)){
				sourceStr.append("amount"+amount);
			}
			if(StringUtils.isNotBlank(orderno)){
				sourceStr.append("orderno"+orderno);
			}
			if(StringUtils.isNotBlank(pBatchNo)){
				sourceStr.append("partnerbatchno"+pBatchNo);
			}
			if(partnerId>0){
				sourceStr.append("partnerid"+partnerId);
			}
			
			PaymentMerchant payemntMerchnt = paymentMerchantService.queryPayemntMerchnt(partnerId);
			
			//request请求参数
			nameValuePair.put("partnerbatchno", pBatchNo);
			nameValuePair.put("partnerid", ObjectUtils.toString(partnerId));
			nameValuePair.put("orderno", orderno);
			//nameValuePair.put("productid", productid);//商品id
			//nameValuePair.put("account", ObjectUtils.toString(paymentOrder.getAid()));
			nameValuePair.put("amount", amount);
			nameValuePair.put("action", RefundmentConstant.TRADE_ACTION_REFUND);
			
			String extend = platform.getExtend();
			String verifyKey = "";//格式V,m,s
//			if(StringUtils.isNotEmpty(extend)){
//				JSONObject extJson = JSONObject.parseObject(extend);
//				verifyKey = extJson.getString("verifyKey");
//			}
			verifyKey = payemntMerchnt.getMerchantKey();
			if(StringUtils.isBlank(verifyKey)){
				logger.error("业务方校验密钥为空,批次单号:" + pBatchNo+",平台id:"+partnerId);
				throw new IllegalArgumentException("平台Id["+partnerId+"]回调密钥为空");
			}
			
			if (logger.isInfoEnabled())
				logger.info("业务方订单校验：" + sourceStr.toString());
			
			String signStr = MD5Encrypt.encrypt(sourceStr.toString() + verifyKey);
			nameValuePair.put("sign", signStr);
			
			//String checkUrl = platform.getPayCheckUrl();//需要增加退款校验地址
			String response = HttpUtils.post(verifyurl, nameValuePair);
			
			if(null!=response){
				//将实体对象转换为JSON Object转换  
			    JSONObject responseJSONObject = JSONObject.parseObject(response);
			    if("200".equals(responseJSONObject.get("status"))){
			    	if (logger.isInfoEnabled())
						logger.info("订单校验成功：" + responseJSONObject.get("data"));
			    }else{
			    	logger.error("回调商户订单校验失败，response:"+responseJSONObject);
			    	throw new IllegalArgumentException("回调商户订单校验失败，response:"+responseJSONObject);
			    }
			}
		} catch (ClientProtocolException e) {
			logger.error("回调商户出错，"+e.getMessage(),e);
			throw new IllegalArgumentException(e);
		} catch (IOException e) {
			logger.error("回调商户出错，"+e.getMessage(),e);
			throw new IllegalArgumentException(e);
		}
	}
}
