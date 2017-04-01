package com.woniu.sncp.pay.core.service.refundment;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.common.utils.RefundmentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.pay.common.errorcode.ErrorCode;
import com.woniu.sncp.pay.common.exception.RefundBatchIsSuccessException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.core.service.PaymentMerchantService;
import com.woniu.sncp.pay.core.service.PaymentOrderService;
import com.woniu.sncp.pay.core.service.PaymentService;
import com.woniu.sncp.pay.core.service.PlatformService;
import com.woniu.sncp.pay.core.service.RefundmentOrderService;
import com.woniu.sncp.pay.core.service.RefundmentService;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.refundment.process.RefundmentProcess;
import com.woniu.sncp.pay.repository.pay.PaymentMerchant;
import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.pojo.refund.PayRefundBatch;
import com.woniu.sncp.pojo.refund.PayRefundBatchDetail;


/**
 * 退款接口流程
 * @author fuzl
 *
 */
@Service("refundmentFacade")
public class RefundmentFacade {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Resource
	private PaymentService paymentService;
	
	@Resource
	private RefundmentOrderService refundmentOrderService;
	
	@Resource
	private PaymentOrderService paymentOrderService;
	
	@Resource
	private PlatformService platformService;
	
	@Resource
	protected RefundmentService refundmentService;
	
	@Resource
	private PaymentMerchantService paymentMerchantService;
	
	/**
	 * 订单校验
	 * @param refundmentProcess
	 * @param pBatchNo
	 * @return
	 */
	public Map<String, Object> checkRefundBatch(HttpServletRequest request,RefundmentProcess refundmentProcess,String pBatchNo){
		Map<String, Object> retMap = refundmentProcess.doBatchCheck(request,pBatchNo);
		return retMap;
	}
	
	/**
	 * 订单退款
	 * @param refundmentProcess
	 * @param inParams
	 */
	public void processRefundmentBack(RefundmentProcess refundmentProcess, Map<String, Object> inParams){
		refundmentProcess.doRefund(inParams);
	}
	
	/**
	 * 
	 * @param pOrderNo
	 * @param merchantId
	 * @param paymentId
	 * @param money
	 * @param productName
	 * @param account
	 * @param gameId
	 * @param imprestMode
	 * @param clientIp
	 * @param extendParams
	 * @return
	 */
	public Map<String, Object> createRefund(String isbatch, String batchno,
			String batchnum, String datadetails, long merchantId, long refundmentId,
			String refunddate, String clientIp, String backendurl, String sign){
		
		return null;
	}
	
	
	/**
	 * 订单退款
	 * @param isbatch
	 * @param batchno
	 * @param batchnum
	 * @param datadetails
	 * @param long1
	 * @param long2
	 * @param refunddate
	 * @param clientIp
	 * @param backendurl
	 * @param sign
	 * @return
	 */
	public Map<String, Object> refundOrder(String isbatch, String pBatchNo,
			String batchNum, String dataDetails, long merchantId, long paymentId,
			String refunddate, String clientIp, String backendurl,String account, String sign,String verifyurl,String ext,HttpServletRequest request) {
		Map<String, Object> outParams = null;
		Map<String, Object> resultMap = null;
		// 支付平台信息
		Platform platform = null;
		// 支付订单信息
		PaymentOrder paymentOrder = null;
		try {
			
			// 是否调用远端服务
			Boolean callRemoteFlag = false;
			// 是否有渠道回调,默认有回调true
			Boolean isCallBack = true; 
			
			
			// 1.退款批次生成
			if(!StringUtils.isBlank(dataDetails)){
				// a.校验
    			//退款交易结果集
    			//业务方付款交易号^退款总金额^退款理由
        		JSONArray details = JSONArray.parseArray(dataDetails);
        		if( details.size()>0 && null != details.get(0)){
        			JSONObject jsonDetail = JSONObject.parseObject(details.get(0).toString());
	    			if(StringUtils.isBlank(jsonDetail.getString("money")) || "0".equals(StringUtils.trim(jsonDetail.getString("money")))){
	    	    		logger.error("退款订单金额不可以为空或零,account:"+account);
	    	    		throw new IllegalArgumentException("订单退款失败,退款订单详情不可以为空或零");
	    	    	}
	    			
	    			/* 判断订单以及金额 */
	    			paymentOrder = paymentOrderService.queryOrderByPartnerOrderNo(ObjectUtils.toString(jsonDetail.get("orderno")), merchantId);
	    			
	    			// 2.查询支付平台信息
	    			platform = platformService.queryPlatform(Long.valueOf(merchantId), paymentOrder.getPayPlatformId(),paymentOrder.getMerchantNo());
	    			platformService.validatePaymentPlatform(platform);
	    			
	    			// 3.根据平台扩展判断是否需要调用远程服务
	    			String extend = platform.getExtend();
	    			
	    			String imprestMode = "";//格式V,m,s
	    			
	    			if(StringUtils.isNotEmpty(extend)){
	    				JSONObject extJson = JSONObject.parseObject(extend);
	    				// callRemote 0 不要远程服务，1 需要远程服务
	    				if(extJson.containsKey("callRemote") && StringUtils.isNotEmpty(extJson.getString("callRemote"))){
	    					if(extJson.getString("callRemote").equals("1")){
	    						callRemoteFlag = true;
	    					};
	    				}
	    				// isCallBack 0 没有回调，1  有回调;默认是有回调
	    				if(extJson.containsKey("isCallBack") && StringUtils.isNotEmpty(extJson.getString("isCallBack"))){
	    					if(extJson.getString("isCallBack").equals("0")){
	    						isCallBack = false;
	    					};
	    				}
	    				if(extJson.containsKey("imprestMode")){
	    					imprestMode = extJson.getString("imprestMode");
	    				}
	    			}
	    			
	    			if(null!=paymentOrder && StringUtils.isNotEmpty(imprestMode)){
	    				
	    				/*判断申请收银台的商户号是否一致*/
	    				if(!paymentOrder.getMerchantId().equals(merchantId)){
	    					// 不一致，直接返回
	    					logger.info("支付平台号:"+paymentOrder.getPayPlatformId()+",退款平台号:"+paymentOrder.getPayPlatformId());
	    					throw new IllegalArgumentException("该订单不支持退款,收银台申请业务商户号不匹配,orderNo:"+paymentOrder.getOrderNo()+",porderno:"+paymentOrder.getPaypartnerOtherOrderNo());
	    				}
//	    				if(!paymentOrder.getPlatformId().equals(paymentId)){
//	    					// 不一致，直接返回
//	    					logger.info("支付平台:"+paymentOrder.getPlatformId()+",退款平台:"+paymentId);
//	    					throw new IllegalArgumentException("该订单不支持退款,收银台支付平台不匹配,orderNo:"+paymentOrder.getOrderNo()+",porderno:"+paymentOrder.getPartnerOrderNo());
//	    				}
	    				
	    				/*增加退款批次明细校验，fuzl@mysnail.com 20161207*/
	    	    		PayRefundBatchDetail payRefundBatchDetail = refundmentOrderService.queryRefundBatchDetailByMidPno(merchantId, ObjectUtils.toString(jsonDetail.get("orderno")));
	    	    		if(null!=payRefundBatchDetail){
	    	    			logger.info("退款批次对应明细已经存在,orderNo:"+payRefundBatchDetail.getOrderNo());
	    	    			if(payRefundBatchDetail.getRefundState().equals(RefundmentConstant.PAYMENT_STATE_REFUNDED) || 
	    	    					payRefundBatchDetail.getRefundState().equals(RefundmentConstant.PAYMENT_STATE_REFUND_FAILED)){
	    	    				logger.info("退款批次对应明细已处理完成,orderNo:"+payRefundBatchDetail.getOrderNo());
	    	    				// 如果同一商户下的支付订单不同的批次来申请,返回异常
	    	    				throw new IllegalArgumentException("同一支付订单只允许申请退款一次,orderNo:"+payRefundBatchDetail.getOrderNo()+",oldPBatchNo:"+payRefundBatchDetail.getPartnerBatchNo());
	    	    			}
	    	    		}
	    				
	    				if(imprestMode.contains(paymentOrder.getImprestMode())){
	    					// 必须配置的支持退款的充值模式才可进行退款
	    					jsonDetail.remove("orderno");
	//            				jsonDetail.put("orderno", paymentOrder.getOrderNo());//业务方订单号转为计费侧订单号
	    					//BigDecimal比较退款金额和订单金额
	        				if(Float.parseFloat(StringUtils.trim(jsonDetail.getString("money"))) > paymentOrder.getMoney()){
	            	    		logger.error("退款订单金额同原订单金额不符,account:"+account+",porderno:"+paymentOrder.getPaypartnerOtherOrderNo());
	            	    		throw new IllegalArgumentException("订单退款失败,退款金额同原订单金额不符,orderNo:"+paymentOrder.getOrderNo()+",porderno:"+paymentOrder.getPaypartnerOtherOrderNo());
	            	    	}
	        				if(!paymentOrder.getPayState().equals(PaymentConstant.PAYMENT_STATE_PAYED)){
	        					throw new IllegalArgumentException("订单退款失败,订单未支付,orderNo:"+paymentOrder.getOrderNo()+",porderno:"+paymentOrder.getPaypartnerOtherOrderNo());
	        				}
	    				}else{
	    					throw new IllegalArgumentException("该订单不支持退款,orderNo:"+paymentOrder.getOrderNo()+",porderno:"+paymentOrder.getPaypartnerOtherOrderNo());
	    				}
	    			}else{
	    				logger.error("退款交易号找不到订单或订单不支持退款,porderno:"+ObjectUtils.toString(jsonDetail.get("orderno")));
	            		throw new IllegalArgumentException("订单退款失败,退款交易号找不到订单或订单不支持退款,porderno:"+ObjectUtils.toString(jsonDetail.get("orderno")));
	    			}
        		}
        		// 批量处理取消
//        		for(Object detail :details){
//        		}
        		
        	
				// b.生成批次信息
				PayRefundBatch payRefundBatch = refundmentOrderService.queryRefundBatchByMidPartnerBatchNo(merchantId,pBatchNo);
				if(null==payRefundBatch){
					payRefundBatch = new PayRefundBatch();
					payRefundBatch.setBatchNum(Integer.parseInt(batchNum));
					payRefundBatch.setDetails(dataDetails);
					payRefundBatch.setMerchantNo(platform.getMerchantNo());//退款时使用的第三方支付商户号
					payRefundBatch.setPartnerBatchNo(pBatchNo);//商户批次号
					payRefundBatch.setPartnerId(merchantId);//计费分配的商户号
					payRefundBatch.setPlatformId(paymentOrder.getPayPlatformId());//渠道id
					payRefundBatch.setPartnerBackendUrl(backendurl);//异步回调业务方的地址
					payRefundBatch.setPartnerVerifyUrl(verifyurl);//业务方实现订单校验地址
					
					/*创建退款批次 */
					refundmentOrderService.createRefundBatchAndGenRefundBatchNo(payRefundBatch, 7L);
				}else{
					if( (PayRefundBatch.REFUNDMENT_STATE_SUCCESS.equals(payRefundBatch.getRefundState())) || 
							(PayRefundBatch.REFUNDMENT_STATE_FAIL.equals(payRefundBatch.getRefundState()))){
						throw new RefundBatchIsSuccessException("该批次订单退款已处理，请重新核对");
					}
//					if( PayRefundBatch.REFUNDMENT_STATE_PROCESS.equals(payRefundBatch.getRefundState())){
//						throw new RefundBatchIsProcessException("该批次订单退款处理中，请重新核对");
//					}
				}
				
				
				// c.处理退款交易明细
				if( details.size()>0 && null != details.get(0)){
					JSONObject jsonDetail = JSONObject.parseObject(details.get(0).toString());
    	    		// d.生成批次--订单信息
//    	    		PaymentOrder paymentOrder = refundmentOrderService.queryOrderByMidPartnerOrderNo(merchantId,ObjectUtils.toString(jsonDetail.get("orderno")));
    	    		if(null!=paymentOrder){
	    				//e.业务方校验订单
        	    		String orderno = ObjectUtils.toString(jsonDetail.get("orderno"));
        	    		String amount = ObjectUtils.toString(jsonDetail.get("money"));
        	    		
        	    		refundmentOrderService.checkOrder(pBatchNo,verifyurl,orderno,merchantId,amount,platform,paymentOrder);
    	    			/*创建退款批次明细*/
        	    		PayRefundBatchDetail payRefundBatchDetail = refundmentOrderService.queryRefundBatchDetailByMidPno(merchantId, orderno);
        	    		if(null!=payRefundBatchDetail){
        	    			logger.info("退款批次对应明细已经存在,orderNo:"+payRefundBatchDetail.getOrderNo());
        	    			if(!payRefundBatchDetail.getPartnerBatchNo().equals(payRefundBatch.getPartnerBatchNo())){
        	    				// 如果同一商户下的支付订单不同的批次来申请,返回异常
        	    				throw new IllegalArgumentException("同一商户下的同一支付订单只允许申请一次,newPBatchNo:"+payRefundBatch.getPartnerBatchNo()+",oldPBatchNo:"+payRefundBatchDetail.getPartnerBatchNo());
        	    			}
        	    		}else{
            	    		payRefundBatchDetail = new PayRefundBatchDetail();
        	    			payRefundBatchDetail.setBatchNo(payRefundBatch.getBatchNo());
        	    			payRefundBatchDetail.setMoney(Float.parseFloat(StringUtils.trim(jsonDetail.getString("money"))));
        	    			payRefundBatchDetail.setOrderNo(paymentOrder.getOrderNo());//业务方订单号转为计费侧订单号
        	    			payRefundBatchDetail.setPayPlatformOrderNo(paymentOrder.getOtherOrderNo());//第三方支付平台交易号
        	    			JSONObject extNote = new JSONObject();
        	    			extNote.put("reason", StringUtils.trim(jsonDetail.getString("refundnote")));
        	    			payRefundBatchDetail.setRefundNote(extNote.toJSONString());
        	    			payRefundBatchDetail.setPartnerBatchNo(pBatchNo);//商户批次号
        	    			payRefundBatchDetail.setPartnerOrderNo(paymentOrder.getPaypartnerOtherOrderNo());//商户订单号
        	    			payRefundBatchDetail.setPayPlatformBatchNo(payRefundBatch.getBatchNo());// 默认我方退款批次号
        	    			payRefundBatchDetail.setPartnerId(merchantId);//计费分配的商户号
        	    			refundmentOrderService.createRefundBatchDetail(payRefundBatchDetail);
    	    			}
    	    		}else{
    	    			logger.error("退款订单不存在,"+ObjectUtils.toString(jsonDetail.get("orderno")));
    	    			throw new IllegalArgumentException("退款订单不存在,porderno:"+ObjectUtils.toString(jsonDetail.get("orderno")));
    	    		}
				}
	    		// 批量处理取消
//            	for(Object detail :details){
//        		}
        		
        		//5.封装退款报文
    			// a.结果信息map
    			// b.提交给对方平台地址 - refundUrl 和 支付编码 - acceptCharset
    			// c.提交给对方平台信息 - refundmentParams
    			Map<String, Object> inParams = new HashMap<String, Object>();
    			inParams.put(RefundmentConstant.REFUNDMENT_BATCH, payRefundBatch);
    			inParams.put(RefundmentConstant.PAYMENT_PLATFORM, platform);
    			inParams.put(RefundmentConstant.CLIENT_IP, clientIp);
    			inParams.put(RefundmentConstant.HTTP_REQUEST, request);
    			
    			
    			
    			Map<String, Object> refundmentOutParams = null;
    			Map<String, Object> refundResult = null;
    			// 4.获取实际退款平台对象
    			AbstractPayment actualPayment = (AbstractPayment) paymentService.findPaymentById(paymentOrder.getPayPlatformId());
    			Assert.notNull(actualPayment,"抽象退款平台配置不正确,查询平台为空,paymentId:" + paymentOrder.getPayPlatformId());
    			if(callRemoteFlag){
    				//5.请求远端服务,提交第三方平台处理退款
    				refundResult = actualPayment.callRefund(inParams);
    			}else{
        			refundmentOutParams = actualPayment.refundedParams(inParams);
        			logger.info("refundmentOutParams:"+refundmentOutParams);
        			//5.提交第三方平台处理退款
        			refundResult = actualPayment.executeRefund(refundmentOutParams);
    			}
    			
    			
    			if(null != refundResult.get(RefundmentConstant.REFUNDMENT_STATE)){
    				// 请求退款,只有退款初始化、退款中 状态,增加已退款状态
    				// 6.1更新退款批次单状态
    				PayRefundBatch refundBatch = refundmentOrderService.queryRefundBatchByMidPartnerBatchNo(merchantId,pBatchNo);
    				refundmentOrderService.updateRefundBatch(refundBatch, ObjectUtils.toString(refundResult.get(RefundmentConstant.REFUNDMENT_STATE)));
    				for(Object detail :details){
    					//6.2 更新退款批次明细单状态
    					JSONObject jsonDetail = JSONObject.parseObject(detail.toString());
    					String orderno = ObjectUtils.toString(jsonDetail.get("orderno"));
    					PayRefundBatchDetail payRefundBatchDetail = refundmentOrderService.queryRefundBatchDetailByMidPno(merchantId, orderno);
    					refundmentOrderService.updateRefundBatchDetail(payRefundBatchDetail, ObjectUtils.toString(refundResult.get(RefundmentConstant.REFUNDMENT_STATE)));
    					
    					if(!isCallBack && ObjectUtils.toString(refundResult.get(RefundmentConstant.REFUNDMENT_STATE)).equals(RefundmentConstant.PAYMENT_STATE_REFUNDED)){
        					
    						//a.回调商户,如果失败应用通过验证接口补单
    						PaymentMerchant payemntMerchnt = paymentMerchantService.queryPayemntMerchnt(merchantId);
    						if(StringUtils.isEmpty(ObjectUtils.toString(inParams.get(RefundmentConstant.REFUND_SUCCESS_NUM)))){
    							refundResult.put(RefundmentConstant.REFUND_SUCCESS_NUM, refundBatch.getBatchNum());//默认是单笔退款
    						}
    						// 回调商户明细retDetails
							JSONArray retDetails = new JSONArray();
							JSONObject _jsonDetail = JSONObject.parseObject(detail.toString());
							_jsonDetail.put("statuscode", "SUCCESS");
							retDetails.add(_jsonDetail);
							refundResult.put(RefundmentConstant.REFUND_RESULT_DETAILS,retDetails);
							
    						String returned = refundmentOrderService.refundCallback(refundBatch,payemntMerchnt,refundResult);
    						logger.info("退款回调业务商户订单返回:"+returned);
    						
    						//没有回调且是退款成功标识,增加修改订单表状态
    						PaymentOrder refundOrder = paymentOrderService.queryOrderByPartnerOrderNo(orderno, merchantId);
        					paymentOrderService.updateOrder(refundOrder,refundOrder.getPayState(), PaymentConstant.PAYMENT_STATE_QUERY_ERR);//已退款
        					
    					}
    				}
    			}
    			
    			outParams = ErrorCode.getErrorCode(1);
    			outParams.put(ErrorCode.TIP_INFO, refundResult.get(ErrorCode.TIP_INFO));// 操作结果
    			outParams.put(ErrorCode.ERROR_INFO, refundResult.get(ErrorCode.ERROR_INFO));
    			outParams.put("platformid", platform.getPlatformId());//交易平台id
    			outParams.put("partnerbatchno", payRefundBatch.getPartnerBatchNo());//业务商户方批次号
    			outParams.put("batchno", payRefundBatch.getBatchNo());//计费侧批次号
    			outParams.put(RefundmentConstant.REFUNDMENT_STATE, refundResult.get(RefundmentConstant.REFUNDMENT_STATE));
    			
        	}else{
        		throw new IllegalArgumentException("退款订单详情不可以为空或零");
        	}
		} catch (DataAccessException e) {
			logger.error("数据库操作异常", e);
			refundmentService.monitorExcetpionToAlter(e);
			resultMap = ErrorCode.getErrorCode(53208);
			resultMap.put("platformid", paymentOrder.getPayPlatformId());//交易平台id
			resultMap.put("partnerbatchno", pBatchNo);//业务商户方批次号
			resultMap.put("batchno", "");//计费侧批次号
			resultMap.put(RefundmentConstant.REFUNDMENT_STATE, RefundmentConstant.PAYMENT_STATE_REFUND_FAILED);
			resultMap.put(ErrorCode.ERROR_INFO, e.getMessage());
			return resultMap;
		} catch (RefundBatchIsSuccessException e) {
			logger.error("批次单已处理", e);
			resultMap = ErrorCode.getErrorCode(53211);
			resultMap.put("platformid", paymentOrder.getPayPlatformId());//交易平台id
			resultMap.put("partnerbatchno", pBatchNo);//业务商户方批次号
			resultMap.put("batchno", "");//计费侧批次号
			resultMap.put(RefundmentConstant.REFUNDMENT_STATE, RefundmentConstant.PAYMENT_STATE_REFUND_FAILED);
			resultMap.put(ErrorCode.ERROR_INFO, e.getMessage());
			return resultMap;
		} catch(ValidationException e){
			logger.error("validation异常", e);
			refundmentService.monitorExcetpionToAlter(e);
			resultMap = ErrorCode.getErrorCode(53205);
			resultMap.put("platformid", paymentOrder.getPayPlatformId());//交易平台id
			resultMap.put("partnerbatchno", pBatchNo);//业务商户方批次号
			resultMap.put("batchno", "");//计费侧批次号
			resultMap.put(RefundmentConstant.REFUNDMENT_STATE, RefundmentConstant.PAYMENT_STATE_REFUND_FAILED);
			resultMap.put(ErrorCode.ERROR_INFO, e.getMessage());
			return resultMap;
		} catch (IllegalArgumentException e) {
			logger.error("illegalArgument异常", e);
			refundmentService.monitorExcetpionToAlter(e);
			resultMap = ErrorCode.getErrorCode(53209);
			resultMap.put("platformid", paymentOrder.getPayPlatformId());//交易平台id
			resultMap.put("partnerbatchno", pBatchNo);//业务商户方批次号
			resultMap.put("batchno", "");//计费侧批次号
			resultMap.put(RefundmentConstant.REFUNDMENT_STATE, RefundmentConstant.PAYMENT_STATE_REFUND_FAILED);
			resultMap.put(ErrorCode.ERROR_INFO, e.getMessage());
			return resultMap;
		} catch (Exception e) {
			logger.error("未知异常", e);
			refundmentService.monitorExcetpionToAlter(e);
			resultMap = ErrorCode.getErrorCode(53210);
			resultMap.put("platformid", paymentOrder.getPayPlatformId());//交易平台id
			resultMap.put("partnerbatchno", pBatchNo);//业务商户方批次号
			resultMap.put("batchno", "");//计费侧批次号
			resultMap.put(RefundmentConstant.REFUNDMENT_STATE, RefundmentConstant.PAYMENT_STATE_REFUND_FAILED);
			resultMap.put(ErrorCode.ERROR_INFO, e.getMessage());
			return resultMap;
		}
		return outParams;
	}
	
	/**
	 * 校验商户平台权限
	 * @param partnerId
	 * @param refundmentId
	 */
	public void validateRefundmentPlatform(String merchantId, String refundmentId){
		Platform platform = platformService.queryPlatform(Long.valueOf(merchantId), Long.valueOf(refundmentId));
		platformService.validatePaymentPlatform(platform);
	}
}
