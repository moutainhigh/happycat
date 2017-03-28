package com.woniu.sncp.pay.core.service.refundment.process;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.woniu.sncp.cbss.api.imprest.direct.request.DIOrderRefundQueryRequest;
import com.woniu.sncp.cbss.api.imprest.direct.response.DIOrderNoRefundBackCallData;
import com.woniu.sncp.cbss.api.imprest.direct.response.DIOrderNoRefundData;
import com.woniu.sncp.cbss.api.imprest.direct.response.DIOrderNoRefundQueryData;
import com.woniu.sncp.jdbc.datasource.DataSourceConstants;
import com.woniu.sncp.jdbc.datasource.DataSourceHolder;
import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.pay.common.errorcode.ErrorCode;
import com.woniu.sncp.pay.common.exception.OrderIsSuccessException;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.RefundBatchIsSuccessException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.core.service.PaymentMerchantService;
import com.woniu.sncp.pay.core.service.PaymentOrderService;
import com.woniu.sncp.pay.core.service.PlatformService;
import com.woniu.sncp.pay.core.service.RefundmentOrderService;
import com.woniu.sncp.pay.core.service.RefundmentService;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.repository.pay.PaymentMerchant;
import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.pojo.refund.PayRefundBatch;
import com.woniu.sncp.pojo.refund.PayRefundBatchDetail;
import com.woniu.sncp.web.IpUtils;

@Service("standardRefundmentProcess")
public class StandardRefundmentProcess extends AbstractRefundmentProcess{

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Resource
	protected RefundmentService refundmentService;
	@Resource
	protected PlatformService platformService;
	@Resource
	private PaymentOrderService paymentOrderService;
	@Resource
	private RefundmentOrderService refundmentOrderService;
	@Resource
	private PaymentMerchantService paymentMerchantService;
	
	
	/**
	 * 退款批次单校验
	 * @param pBatchNo
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> doBatchCheck(String pBatchNo) {
		if (logger.isInfoEnabled()) {
			logger.info("++++++++++++++++退款批次单校验+++++++++++++++++");
			logger.info("1.退款批次单校验进入：pBatchNo:" + pBatchNo);
		}
		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);

		String refundResult = RefundmentConstant.PAYMENT_STATE_REFUND_PRO;
		Map<String, Object> refundResultMap = null;
		Map<String, Object> resultMap = null;
		try {
			
			// 1.校验退款单是否处理中
			PayRefundBatch refundBatch = refundmentOrderService.queryRefundBatchByPartnerBatchNo(pBatchNo);
			logger.warn("2.退款批次订单验证通过,继续进行退款,batchNo:" + refundBatch.getBatchNo());
			// 验证是否退款中
			refundmentOrderService.checkRefundBatchIsProcessed(refundBatch);
			
			
			// 2.请求查询验证
			refundResultMap = this.validateRefundBatchCheckParams(pBatchNo);
			if (logger.isInfoEnabled())
				logger.info("验证订单返回：" + JsonUtils.toJson(refundResultMap));
			
			// 3.获取支付平台返回的退款结果明细
			List<DIOrderNoRefundQueryData> orderNoRefundQueryDataList = (List<DIOrderNoRefundQueryData>) refundResultMap.get(RefundmentConstant.REFUND_RESULT_DETAILS);
			
			if( orderNoRefundQueryDataList.size() == 0){
				if (logger.isInfoEnabled())
					logger.info("订单查询验证==>对方渠道订单查询失败：pBatchNo:" + pBatchNo);
				return ErrorCode.put(ErrorCode.getErrorCode(53214), "refundResult", RefundmentConstant.PAYMENT_STATE_REFUND_ERR);
			}
			
			PaymentMerchant payemntMerchnt = paymentMerchantService.queryPayemntMerchnt(refundBatch.getPartnerId());
			// 回调商户明细retDetails
			JSONArray details = JSONArray.parseArray(refundBatch.getDetails());
			JSONArray retDetails = new JSONArray();
			if( details.size()>0 && null != details.get(0)){
				JSONObject _jsonDetail = JSONObject.parseObject(details.get(0).toString());
				_jsonDetail.put("statuscode", "SUCCESS");
				retDetails.add(_jsonDetail);
			}
			refundResultMap.put(RefundmentConstant.REFUND_RESULT_DETAILS, retDetails);
			refundResultMap.put(RefundmentConstant.REFUND_SUCCESS_NUM,orderNoRefundQueryDataList.size());
			// 2.回调商户
			String returned = refundmentOrderService.refundCallback(refundBatch,payemntMerchnt,refundResultMap);
			if (logger.isInfoEnabled())
				logger.info("订单查询验证业务商户订单返回：" + JsonUtils.toJson(returned));
			
			// a.更新退款成功,处理时间（无需以商户方返回结果为准&& "success".equals(returned)）
			if(orderNoRefundQueryDataList.size()>=1){
				if( "success".equals(returned) ){
					//更新批次明细
					for(DIOrderNoRefundQueryData detail :orderNoRefundQueryDataList){
						// 返回非正常退款直接响应
						if (!DIOrderNoRefundData.PAYMENT_STATE_REFUNDED.equals(detail.getStatusCode())) {
							return ErrorCode.put(ErrorCode.getErrorCode(53214), "refundResult", refundResult);
						}
						
						if(StringUtils.isBlank(detail.getMoney()) || "0".equals(StringUtils.trim(detail.getMoney()))){
							logger.info("订单查询验证==>退款订单金额不可以为空或零");
							throw new ValidationException("订单查询验证退款订单金额不可以为空或零");
						}
						// 判断订单以及金额
						PaymentOrder refundOrder = paymentOrderService.queryOrder(ObjectUtils.toString(detail.getOrderNo()));
						
						if( !refundmentOrderService.checkOrderMoney(refundOrder,Integer.parseInt(detail.getMoney())) ){
							logger.info("订单查询验证==>退款订单金额与原支付订单金额不匹配");
							throw new ValidationException("订单查询验证退款订单金额与原支付订单金额不匹配");
				    	}
						
						// 通过批次号和业务方订单号查询批次明细
						PayRefundBatchDetail payRefundBatchDetail = refundmentOrderService.queryRefundBatchDetailByMidPno(refundBatch.getPartnerId(), refundOrder.getPartnerOrderNo());
						
						// 校验批次明细是否已处理
						if(refundmentOrderService.checkRefundBatchDetailIsProcessed(payRefundBatchDetail)){
							logger.info("订单查询验证==>退款批次单明细处理中或已处理，勿重复处理==>batchNo:"+payRefundBatchDetail.getBatchNo()+",orderNo:"+payRefundBatchDetail.getOrderNo());
							continue;
						}
						
						payRefundBatchDetail.setHandDate(new Date());
						payRefundBatchDetail.setPayPlatformBatchNo(detail.getPayplatformBatchNo());// 对方退款批次号
						payRefundBatchDetail.setPayPlatformOrderNo(detail.getPayplatformOrderNo());// 对方支付订单号
						if(null!=detail.getStatusCode()){
							if(DIOrderNoRefundData.PAYMENT_STATE_REFUNDED.equals(detail.getStatusCode())){
								// 更改退款批次明细单状态为退款成功
								refundmentOrderService.updateRefundBatchDetail(payRefundBatchDetail, RefundmentConstant.PAYMENT_STATE_REFUNDED);
								
								// 收银台支付订单的充值状态改为  4 已退款,
								paymentOrderService.updateOrder(refundOrder, null, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
							}else if(DIOrderNoRefundData.PAYMENT_STATE_REFUND_FAILED.equals(detail.getStatusCode())){
								// 更改订单状态为退款失败
								refundmentOrderService.updateRefundBatchDetail(payRefundBatchDetail, RefundmentConstant.PAYMENT_STATE_REFUND_FAILED);
							}else{
								throw new IllegalArgumentException("订单查询验证退款平台返回非正常退款");
							}
						}else{
							throw new IllegalArgumentException("退款平台返回非正常退款");
						}
					}
					
					// 更新批次单 状态为3 处理中,
					refundmentOrderService.updateRefundBatch(refundBatch, RefundmentConstant.PAYMENT_STATE_REFUND_PRO);
				}else{
					//回调业务商户失败,加入队列
					logger.error("回调业务商户失败，业务批次单号:"+refundBatch.getPartnerBatchNo()+"批次单号："+refundBatch.getBatchNo());
				}
			}
			
		} catch (DataAccessException e) {
			logger.error("订单校验数据库异常,pBatchNo:" + pBatchNo, e);
			refundmentService.monitorExcetpionToAlter(e);
			resultMap = ErrorCode.getErrorCode(53209);
			resultMap.put(RefundmentConstant.REFUNDMENT_STATE, RefundmentConstant.PAYMENT_STATE_REFUND_FAILED);
			resultMap.put(ErrorCode.ERROR_INFO, e.getMessage());
			return resultMap;
		}  catch (ValidationException e) {
			logger.error("订单校验异常,pBatchNo:" + pBatchNo, e);
			refundmentService.monitorExcetpionToAlter(e);
			resultMap = ErrorCode.getErrorCode(53215);
			resultMap.put(RefundmentConstant.REFUNDMENT_STATE, RefundmentConstant.PAYMENT_STATE_REFUND_FAILED);
			resultMap.put(ErrorCode.ERROR_INFO, e.getMessage());
			return resultMap;
		} catch (IllegalArgumentException e) {
			logger.error("订单校验异常,pBatchNo:" + pBatchNo, e);
			refundmentService.monitorExcetpionToAlter(e);
			resultMap = ErrorCode.getErrorCode(53210);
			resultMap.put(RefundmentConstant.REFUNDMENT_STATE, RefundmentConstant.PAYMENT_STATE_REFUND_FAILED);
			resultMap.put(ErrorCode.ERROR_INFO, e.getMessage());
			return resultMap;
		} catch (RefundBatchIsSuccessException e) {
			logger.error("订单校验:订单退款完成,无需重复处理,pBatchNo:" + pBatchNo, e);
			resultMap = ErrorCode.getErrorCode(1);
			resultMap.put(RefundmentConstant.REFUNDMENT_STATE, RefundmentConstant.PAYMENT_STATE_REFUNDED);
			resultMap.put(ErrorCode.ERROR_INFO, e.getMessage());
			return resultMap;
		} catch (Exception e) {
			logger.error("订单校验未知异常,pBatchNo:" + pBatchNo+",error:"+ErrorCode.ERROR_INFO, e);
			refundmentService.monitorExcetpionToAlter(e);
			resultMap = ErrorCode.getErrorCode(53210);
			resultMap.put(RefundmentConstant.REFUNDMENT_STATE, RefundmentConstant.PAYMENT_STATE_REFUND_FAILED);
			resultMap.put(ErrorCode.ERROR_INFO, e.getMessage());
			return resultMap;
		}
		resultMap = ErrorCode.getErrorCode(1);
		resultMap.put(RefundmentConstant.REFUNDMENT_STATE, RefundmentConstant.PAYMENT_STATE_REFUNDED);
		return resultMap;
	}
	
	/**
	 * 退款批次单校验
	 * @param request
	 * @param pBatchNo
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> doBatchCheck(HttpServletRequest request,String pBatchNo) {
		if (logger.isInfoEnabled()) {
			logger.info("++++++++++++++++退款批次单校验+++++++++++++++++");
			logger.info("1.退款批次单校验进入：pBatchNo:" + pBatchNo);
		}
		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
		
		String refundResult = RefundmentConstant.PAYMENT_STATE_REFUND_PRO;
		Map<String, Object> refundResultMap = null;
		Map<String, Object> resultMap = null;
		try {
			
			// 1.校验 并 执行请求退款
			refundResultMap = this.validateRefundBatchCheckParams(request,pBatchNo);
			if (logger.isInfoEnabled())
				logger.info("2.验证订单返回：" + JsonUtils.toJson(refundResultMap));
			
			// 2.获取支付平台返回的退款结果明细
			List<DIOrderNoRefundQueryData> orderNoRefundQueryDataList = (List<DIOrderNoRefundQueryData>) refundResultMap.get(RefundmentConstant.REFUND_RESULT_DETAILS);
			
			if( orderNoRefundQueryDataList.size() == 0){
				if (logger.isInfoEnabled())
					logger.info("订单查询验证==>查询验证对方订单失败：pBatchNo:" + pBatchNo);
				return ErrorCode.put(ErrorCode.getErrorCode(53214), "refundResult", RefundmentConstant.PAYMENT_STATE_REFUND_ERR);
			}
			
			PayRefundBatch refundBatch = refundmentOrderService.queryRefundBatchByPartnerBatchNo(pBatchNo);
			logger.warn("3.退款批次订单验证通过,继续进行退款,batchNo:" + refundBatch.getBatchNo());
			PaymentMerchant payemntMerchnt = paymentMerchantService.queryPayemntMerchnt(refundBatch.getPartnerId());
			
			// 4.回调商户
			Map<String, Object> inParam = new HashMap<String, Object>();
			// 回调商户明细retDetails
			JSONArray details = JSONArray.parseArray(refundBatch.getDetails());
			JSONArray retDetails = new JSONArray();
			if( details.size()>0 && null != details.get(0)){
				JSONObject _jsonDetail = JSONObject.parseObject(details.get(0).toString());
				_jsonDetail.put("statuscode", "SUCCESS");
				retDetails.add(_jsonDetail);
			}
			inParam.put(RefundmentConstant.REFUND_RESULT_DETAILS, retDetails);
			inParam.put(RefundmentConstant.REFUND_SUCCESS_NUM,orderNoRefundQueryDataList.size());
			String returned = refundmentOrderService.refundCallback(refundBatch,payemntMerchnt,inParam);
			if (logger.isInfoEnabled())
				logger.info("订单查询验证==>业务商户订单返回：" + JsonUtils.toJson(returned));
			
			// 5.验证是否已处理
			refundmentOrderService.checkRefundBatchIsHandled(refundBatch);
			if(orderNoRefundQueryDataList.size()>=1){
				// a.更新退款成功,处理时间（无需以商户方返回结果为准&& "success".equals(returned)）
				if("success".equals(returned)){
					
					//更新批次明细
					for(DIOrderNoRefundQueryData detail :orderNoRefundQueryDataList){
						// 返回非正常退款直接响应
						if (!DIOrderNoRefundData.PAYMENT_STATE_REFUNDED.equals(detail.getStatusCode())) {
							return ErrorCode.put(ErrorCode.getErrorCode(53214), "refundResult", refundResult);
						}
						
						if(StringUtils.isBlank(detail.getMoney()) || "0".equals(StringUtils.trim(detail.getMoney()))){
							logger.info("订单查询验证==>退款订单金额不可以为空或零");
							throw new ValidationException("订单查询验证退款订单金额不可以为空或零");
						}
						// 判断订单以及金额
						PaymentOrder refundOrder = paymentOrderService.queryOrder(ObjectUtils.toString(detail.getOrderNo()));
						
						if( !refundmentOrderService.checkOrderMoney(refundOrder,Integer.parseInt(detail.getMoney())) ){
							logger.info("订单查询验证==>退款订单金额与原支付订单金额不匹配");
							throw new ValidationException("订单查询验证退款订单金额与原支付订单金额不匹配");
				    	}
						
						// 通过批次号和业务方订单号查询批次明细
						PayRefundBatchDetail payRefundBatchDetail = refundmentOrderService.queryRefundBatchDetailByMidPno(refundBatch.getPartnerId(), refundOrder.getPartnerOrderNo());
						
						// 校验批次明细是否已处理
						if(refundmentOrderService.checkRefundBatchDetailIsProcessed(payRefundBatchDetail)){
							logger.info("订单查询验证==>退款批次单明细处理中或已处理，勿重复处理==>batchNo:"+payRefundBatchDetail.getBatchNo()+",orderNo:"+payRefundBatchDetail.getOrderNo());
							continue;
						}
						
						payRefundBatchDetail.setHandDate(new Date());
						payRefundBatchDetail.setPayPlatformBatchNo(detail.getPayplatformBatchNo());// 对方退款批次号
						payRefundBatchDetail.setPayPlatformOrderNo(detail.getPayplatformOrderNo());// 对方支付订单号
						if(null!=detail.getStatusCode()){
							if(DIOrderNoRefundData.PAYMENT_STATE_REFUNDED.equals(detail.getStatusCode())){
								// 更改退款批次明细单状态为退款成功
								refundmentOrderService.updateRefundBatchDetail(payRefundBatchDetail, RefundmentConstant.PAYMENT_STATE_REFUNDED);
								
								// 收银台支付订单的充值状态改为  4 已退款,
								paymentOrderService.updateOrder(refundOrder, null, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
							}else if(DIOrderNoRefundData.PAYMENT_STATE_REFUND_FAILED.equals(detail.getStatusCode())){
								// 更改订单状态为退款失败
								refundmentOrderService.updateRefundBatchDetail(payRefundBatchDetail, RefundmentConstant.PAYMENT_STATE_REFUND_FAILED);
							}else{
								throw new IllegalArgumentException("订单查询验证退款平台返回非正常退款");
							}
						}else{
							throw new IllegalArgumentException("退款平台返回非正常退款");
						}
					}
					
					// 更新批次单 状态为3 处理中,
					refundmentOrderService.updateRefundBatch(refundBatch, RefundmentConstant.PAYMENT_STATE_REFUND_PRO);

				}else{
					// 回调业务失败
					for(DIOrderNoRefundQueryData detail :orderNoRefundQueryDataList){
						// 返回非正常退款直接响应
						if (!DIOrderNoRefundData.PAYMENT_STATE_REFUNDED.equals(detail.getStatusCode())) {
							return ErrorCode.put(ErrorCode.getErrorCode(53214), "refundResult", refundResult);
						}
						
						if(StringUtils.isBlank( detail.getMoney() ) || "0".equals(StringUtils.trim( detail.getMoney() ))){
							logger.info("订单查询验证==>退款订单金额不可以为空或零");
				    		throw new ValidationException("退款订单金额不可以为空或零");
				    	}
						/*判断订单以及金额*/
						PaymentOrder refundOrder = paymentOrderService.queyrOrderByOppositeOrderNo(detail.getPayplatformOrderNo());
						if( !refundmentOrderService.checkOrderMoney(refundOrder,Integer.parseInt(detail.getMoney())) ){
							logger.info("订单查询验证==>退款订单金额与原支付订单金额不匹配");
							throw new ValidationException("退款订单金额与原支付订单金额不匹配");
				    	}
						
						// 通过批次号和业务方订单号查询批次明细
						PayRefundBatchDetail payRefundBatchDetail = refundmentOrderService.queryRefundBatchDetailByMidPno(refundBatch.getPartnerId(), refundOrder.getPartnerOrderNo());
						
						// 校验批次明细是否已处理
						if(refundmentOrderService.checkRefundBatchDetailIsProcessed(payRefundBatchDetail)){
							logger.info("订单查询验证==>退款批次单明细处理中或已处理，勿重复处理==>batchNo:"+payRefundBatchDetail.getBatchNo()+",orderNo:"+payRefundBatchDetail.getOrderNo());
							continue;
						}
						
						payRefundBatchDetail.setHandDate(new Date());
						payRefundBatchDetail.setPayPlatformBatchNo(detail.getPayplatformBatchNo());// 对方退款批次号
						payRefundBatchDetail.setPayPlatformOrderNo(detail.getPayplatformOrderNo());// 对方支付订单号
						if(null!=detail.getStatusCode()){
							if(DIOrderNoRefundData.PAYMENT_STATE_REFUNDED.equals(detail.getStatusCode())){
								//更改退款批次明细单状态为退款成功
								refundmentOrderService.updateRefundBatchDetail(payRefundBatchDetail, RefundmentConstant.PAYMENT_STATE_REFUNDED);
							}else if(DIOrderNoRefundData.PAYMENT_STATE_REFUND_FAILED.equals(detail.getStatusCode())){
								//更改订单状态为退款失败
								refundmentOrderService.updateRefundBatchDetail(payRefundBatchDetail, RefundmentConstant.PAYMENT_STATE_REFUND_FAILED);
							}else{
								throw new IllegalArgumentException("退款平台返回非正常退款");
							}
						}else{
							throw new IllegalArgumentException("退款平台返回非正常退款");
						}
						
					}
					// 更新批次单 状态为3 处理中,
					refundmentOrderService.updateRefundBatch(refundBatch, RefundmentConstant.PAYMENT_STATE_REFUND_PRO);

					//回调业务商户失败,加入队列
					logger.error("*_*回调业务失败，批次单号："+refundBatch.getBatchNo());
				}
			}
			
		} catch (DataAccessException e) {
			logger.error("订单校验数据库异常,pBatchNo:" + pBatchNo, e);
			refundmentService.monitorExcetpionToAlter(e);
			resultMap = ErrorCode.getErrorCode(53209);
			resultMap.put(RefundmentConstant.REFUNDMENT_STATE, RefundmentConstant.PAYMENT_STATE_REFUND_FAILED);
			resultMap.put(ErrorCode.ERROR_INFO, e.getMessage());
			return resultMap;
		} catch (ValidationException e) {
			logger.error("订单校验异常,pBatchNo:" + pBatchNo, e);
			refundmentService.monitorExcetpionToAlter(e);
			resultMap = ErrorCode.getErrorCode(53215);
			resultMap.put(RefundmentConstant.REFUNDMENT_STATE, RefundmentConstant.PAYMENT_STATE_REFUND_FAILED);
			resultMap.put(ErrorCode.ERROR_INFO, e.getMessage());
			return resultMap;
		} catch (IllegalArgumentException e) {
			logger.error("订单校验异常,pBatchNo:" + pBatchNo, e);
			refundmentService.monitorExcetpionToAlter(e);
			resultMap = ErrorCode.getErrorCode(53214);
			resultMap.put(RefundmentConstant.REFUNDMENT_STATE, RefundmentConstant.PAYMENT_STATE_REFUND_FAILED);
			resultMap.put(ErrorCode.ERROR_INFO, e.getMessage());
			return resultMap;
		} catch (RefundBatchIsSuccessException e) {
			logger.error("订单校验:订单退款完成,无需重复处理,pBatchNo:" + pBatchNo, e);
			resultMap = ErrorCode.getErrorCode(1);
			resultMap.put(RefundmentConstant.REFUNDMENT_STATE, RefundmentConstant.PAYMENT_STATE_REFUNDED);
			resultMap.put(ErrorCode.ERROR_INFO, e.getMessage());
			return resultMap;
		} catch (Exception e) {
			logger.error("订单校验未知异常,pBatchNo:" + pBatchNo+",error:"+ErrorCode.ERROR_INFO, e);
			refundmentService.monitorExcetpionToAlter(e);
			resultMap = ErrorCode.getErrorCode(53210);
			resultMap.put(RefundmentConstant.REFUNDMENT_STATE, RefundmentConstant.PAYMENT_STATE_REFUND_FAILED);
			resultMap.put(ErrorCode.ERROR_INFO, e.getMessage());
			return resultMap;
		}
		resultMap = ErrorCode.getErrorCode(1);
		resultMap.put(RefundmentConstant.REFUNDMENT_STATE, RefundmentConstant.PAYMENT_STATE_REFUNDED);
		return resultMap;
	}
	
	/**
	 * 退款批次单校验 - 前期参数校验
	 * 
	 * @param pBatchNo
	 *            由eai传过来的钱数
	 * @return
	 * @throws DataAccessException
	 * @throws ValidationException
	 * @throws RefundBatchIsSuccessException
	 */
	public Map<String, Object> validateRefundBatchCheckParams(String pBatchNo) throws DataAccessException,
			ValidationException, RefundBatchIsSuccessException {
		// 1.退款批次单查询
		PayRefundBatch payRefundBatch = refundmentOrderService.queryRefundBatchByPartnerBatchNo(pBatchNo);
		if(payRefundBatch == null){
			payRefundBatch = refundmentOrderService.queryRefundBatch(pBatchNo);
		}
		Assert.notNull(payRefundBatch, "退款批次单查询为空,pBatchNo:" + pBatchNo);
		// 2.验证是否已退款
		refundmentOrderService.checkRefundBatchIsHandled(payRefundBatch);
		
		// 3.获取退款交易平台
		Platform platform = platformService.queryPlatform(payRefundBatch.getPartnerId(), payRefundBatch.getPlatformId(),payRefundBatch.getMerchantNo());
		platformService.validatePaymentPlatform(platform);
		
		// 4.根据平台扩展判断是否需要调用远程服务
		String extend = platform.getPlatformExt();
		// 是否调用远端服务
		Boolean callRemoteFlag = false;
		if(StringUtils.isNotEmpty(extend)){
			JSONObject extJson = JSONObject.parseObject(extend);
			// callRemote 0 不要远程服务，1 需要远程服务
			if(StringUtils.isNotBlank(extJson.getString("callRemote")) && extJson.getString("callRemote").equals("1")){
				callRemoteFlag = true;
			};
		}
		
		
		AbstractPayment actualPayment = (AbstractPayment) refundmentService.findPaymentById(payRefundBatch.getPlatformId());
		Assert.notNull(actualPayment, "抽象退款平台查询为空，可能是配置不对");

		
		Map<String, Object> isRefundedParams = new HashMap<String, Object>();
		isRefundedParams.put(RefundmentConstant.REFUNDMENT_BATCH, payRefundBatch);
		isRefundedParams.put(RefundmentConstant.PAYMENT_PLATFORM, platform);
		isRefundedParams.put(RefundmentConstant.REFUNDMENT, actualPayment);
		isRefundedParams.put(RefundmentConstant.REFUND_RESULT_DETAILS, payRefundBatch.getDetails());
		
		Map<String, Object> refundResult = null;
		// 5.各平台单独校验 - 校验参数的合法性
		if(callRemoteFlag){
			// a.调用远程服务
			refundResult = actualPayment.callRefundQuery(isRefundedParams);
		}else {
			// b.调用本地服务
			DIOrderRefundQueryRequest orderRefundQueryRequest = new DIOrderRefundQueryRequest();
			refundResult = actualPayment.orderRefundQuery(orderRefundQueryRequest);
		}
		

		isRefundedParams.clear();
		isRefundedParams.put("platformid", platform.getPlatformId());//交易平台id
		isRefundedParams.put("partnerbatchno", payRefundBatch.getPartnerBatchNo());//业务商户方批次号
		isRefundedParams.put("batchno", payRefundBatch.getBatchNo());//计费侧批次号
		isRefundedParams.putAll(refundResult);
		return isRefundedParams;
	}
	
	/**
	 * 退款批次单校验 - 前期参数校验
	 * 
	 * @param pBatchNo
	 *            由eai传过来的钱数
	 * @return
	 * @throws DataAccessException
	 * @throws ValidationException
	 * @throws RefundBatchIsSuccessException
	 */
	public Map<String, Object> validateRefundBatchCheckParams(HttpServletRequest request,String pBatchNo) throws DataAccessException,
	ValidationException, RefundBatchIsSuccessException {
		// 1.退款批次单查询
		PayRefundBatch payRefundBatch = refundmentOrderService.queryRefundBatchByPartnerBatchNo(pBatchNo);
		if(payRefundBatch == null){
			payRefundBatch = refundmentOrderService.queryRefundBatch(pBatchNo);
		}
		Assert.notNull(payRefundBatch, "退款批次单查询为空,pBatchNo:" + pBatchNo);
		// 2.验证是否已退款
		refundmentOrderService.checkRefundBatchIsHandled(payRefundBatch);
		
		// 3.获取退款交易平台
		Platform platform = platformService.queryPlatform(payRefundBatch.getPartnerId(), payRefundBatch.getPlatformId(),payRefundBatch.getMerchantNo());
		platformService.validatePaymentPlatform(platform);
		
		// 4.根据平台扩展判断是否需要调用远程服务
		String extend = platform.getPlatformExt();
		// 是否调用远端服务
		Boolean callRemoteFlag = false;
		if(StringUtils.isNotEmpty(extend)){
			JSONObject extJson = JSONObject.parseObject(extend);
			// callRemote 0 不要远程服务，1 需要远程服务
			if(StringUtils.isNotBlank(extJson.getString("callRemote")) && extJson.getString("callRemote").equals("1")){
				callRemoteFlag = true;
			};
		}
		
		
		AbstractPayment actualPayment = (AbstractPayment) refundmentService.findPaymentById(payRefundBatch.getPlatformId());
		Assert.notNull(actualPayment, "抽象退款平台查询为空，可能是配置不对");
		
		
		Map<String, Object> isRefundedParams = new HashMap<String, Object>();
		isRefundedParams.put(RefundmentConstant.REFUNDMENT_BATCH, payRefundBatch);
		isRefundedParams.put(RefundmentConstant.PAYMENT_PLATFORM, platform);
		isRefundedParams.put(RefundmentConstant.REFUNDMENT, actualPayment);
		isRefundedParams.put(RefundmentConstant.REFUND_RESULT_DETAILS, payRefundBatch.getDetails());
		isRefundedParams.put(RefundmentConstant.HTTP_REQUEST, request);
		
		Map<String, Object> refundResult = null;
		// 5.各平台单独校验 - 校验参数的合法性
		if(callRemoteFlag){
			// a.调用远程服务
			refundResult = actualPayment.callRefundQuery(isRefundedParams);
		}else {
			// b.调用本地服务
			DIOrderRefundQueryRequest orderRefundQueryRequest = new DIOrderRefundQueryRequest();
			refundResult = actualPayment.orderRefundQuery(orderRefundQueryRequest);
		}
		
		
		isRefundedParams.clear();
		isRefundedParams.put("platformid", platform.getPlatformId());//交易平台id
		isRefundedParams.put("partnerbatchno", payRefundBatch.getPartnerBatchNo());//业务商户方批次号
		isRefundedParams.put("batchno", payRefundBatch.getBatchNo());//计费侧批次号
		isRefundedParams.putAll(refundResult);
		return isRefundedParams;
	}
	
	
	
	/**
	 * 处理退款回调
	 */
	@SuppressWarnings("unchecked")
	public synchronized void doRefund(Map<String, Object> inParams){
		HttpServletRequest request = (HttpServletRequest) inParams.get("request");
		HttpServletResponse response = (HttpServletResponse) inParams.get("response");
		AbstractPayment actualPayment = (AbstractPayment) inParams.get("actualPayment");
		
		logger.info("++++++++++++++++++退款回调++++++++++++++++");
		logger.info("退款回调进入：" + actualPayment.getClass().getSimpleName());
		
		String returned = null;
		try {
			// 1.验证参数
			DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
			/* 校验支付平台返回退款结果 */
			
			Map<String, Object> centerInfo = this.validateBackParams(actualPayment,inParams);
			String batchNo = (String) centerInfo.get(RefundmentConstant.BATCHNO);
			
			PayRefundBatch refundBatch = refundmentOrderService.queryRefundBatch(batchNo);
			
			PaymentMerchant payemntMerchnt = paymentMerchantService.queryPayemntMerchnt(Long.parseLong(ObjectUtils.toString(inParams.get("merchantid"))));
			
			// 2.验证是否已申请退款成功
			refundmentOrderService.checkRefundBatchIsHandled(refundBatch);
			
			// 3.获取支付平台返回的退款结果明细
			List<DIOrderNoRefundBackCallData> orderNoRefundBackCallDataList = (List<DIOrderNoRefundBackCallData>) centerInfo.get(RefundmentConstant.REFUND_RESULT_DETAILS);
			
			//a.回调商户,如果失败应用通过验证接口补单
			// 回调商户明细retDetails
			JSONArray details = JSONArray.parseArray(refundBatch.getDetails());
			JSONArray retDetails = new JSONArray();
			if( details.size()>0 && null != details.get(0)){
				JSONObject _jsonDetail = JSONObject.parseObject(details.get(0).toString());
				_jsonDetail.put("statuscode", "SUCCESS");
				retDetails.add(_jsonDetail);
			}
			centerInfo.put(RefundmentConstant.REFUND_RESULT_DETAILS, retDetails);
			returned = refundmentOrderService.refundCallback(refundBatch,payemntMerchnt,centerInfo);
			logger.info("退款回调业务商户订单返回:"+returned);
			
			//b.更新退款成功,处理时间（改为以商户方返回结果为准,需要同时成功,返回支付系统success && "success".equals(returned)）
			if(!refundmentOrderService.refundBatchIsRefunded(refundBatch) && "success".equals(returned)){
				//更新批次明细,直接在validateBackParams时，已经进行状态更新
				//返回给支付系统，处理成功
				// 5.处理结果集
				for(DIOrderNoRefundBackCallData detail :orderNoRefundBackCallDataList){
					if(StringUtils.isBlank(detail.getMoney()) || "0".equals(StringUtils.trim(detail.getMoney()))){
			    		throw new ValidationException("退款订单金额不可以为空或零");
			    	}
					/*判断退款订单金额以及回调金额*/
					PaymentOrder refundOrder = paymentOrderService.queyrOrderByOppositeOrderNo(detail.getPayplatformOrderNo());
					
					// 通过计费分配商户id和业务方订单号查询批次明细
					PayRefundBatchDetail payRefundBatchDetail = refundmentOrderService.queryRefundBatchDetailByMidPno(refundBatch.getPartnerId(), refundOrder.getPartnerOrderNo());
					/*判断退款订单金额以及回调金额*/
					if( !refundmentOrderService.checkRefundDetailMoney(payRefundBatchDetail,Integer.parseInt(detail.getMoney())) ){
			    		throw new ValidationException("退款订单回调金额与请求退款金额不匹配");
			    	}
					
					if( null != detail.getStatusCode() ){
						if(RefundmentConstant.PAYMENT_STATE_REFUNDED.equals( detail.getStatusCode() )){
							payRefundBatchDetail.setPayPlatformBatchNo(detail.getPayplatformBatchNo());
							payRefundBatchDetail.setHandDate(new Date());
							// 更改退款批次明细单状态为退款成功
							refundmentOrderService.updateRefundBatchDetail(payRefundBatchDetail, RefundmentConstant.PAYMENT_STATE_REFUNDED);
							
							// 收银台支付订单的充值状态改为  4 已退款,
							paymentOrderService.updateOrder(refundOrder, null, PaymentConstant.PAYMENT_STATE_QUERY_ERR);
							
							// 更新批次单
							refundmentOrderService.updateRefundBatch(refundBatch, RefundmentConstant.PAYMENT_STATE_REFUNDED);
						}else if(RefundmentConstant.PAYMENT_STATE_REFUND_FAILED.equals( detail.getStatusCode() )){
							// 一笔失败，批次就失败
							// 更改订单状态为退款失败
							payRefundBatchDetail.setPayPlatformBatchNo(detail.getPayplatformBatchNo());
							refundmentOrderService.updateRefundBatchDetail(payRefundBatchDetail, RefundmentConstant.PAYMENT_STATE_REFUND_FAILED);
							
							// 更新批次单
							refundmentOrderService.updateRefundBatch(refundBatch, RefundmentConstant.PAYMENT_STATE_REFUND_FAILED);
							throw new ValidationException("退款平台返回退款失败");
						}else{
							//提示出错
							throw new ValidationException("退款平台返回退款失败,状态码非成功或失败");
						}
					}else{
						//提示出错
						throw new ValidationException("退款平台返回退款失败,没有返回状态码");
					}
					
				}
				actualPayment.refundmentReturn(inParams, response, true);
			}else{
				// 更新批次单  状态为3 处理中,
				refundmentOrderService.updateRefundBatch(refundBatch, RefundmentConstant.PAYMENT_STATE_REFUND_PRO);
				for(DIOrderNoRefundBackCallData detail :orderNoRefundBackCallDataList){
					if(StringUtils.isBlank( detail.getMoney() ) || "0".equals(StringUtils.trim( detail.getMoney() ))){
			    		throw new ValidationException("退款订单金额不可以为空或零");
			    	}
					
					PaymentOrder refundOrder = paymentOrderService.queyrOrderByOppositeOrderNo(detail.getPayplatformOrderNo());
					
					// 通过批次号和业务方订单号查询批次明细
					PayRefundBatchDetail payRefundBatchDetail = refundmentOrderService.queryRefundBatchDetailByMidPno(refundBatch.getPartnerId(), refundOrder.getPartnerOrderNo());
					/*判断退款订单金额以及回调金额*/
					if( !refundmentOrderService.checkRefundDetailMoney(payRefundBatchDetail,Integer.parseInt(detail.getMoney())) ){
			    		throw new ValidationException("退款订单回调金额与请求退款金额不匹配");
			    	}
					
					
					if(null!= detail.getStatusCode() ){
						if(RefundmentConstant.PAYMENT_STATE_REFUNDED.equals( detail.getStatusCode() )){
							//更改退款批次明细单状态为退款处理中
							refundmentOrderService.updateRefundBatchDetail(payRefundBatchDetail, RefundmentConstant.PAYMENT_STATE_REFUND_PRO);
						}else if(RefundmentConstant.PAYMENT_STATE_REFUND_FAILED.equals( detail.getStatusCode() )){
							// 一笔失败，批次就失败
							// 更改订单状态为退款失败
							refundmentOrderService.updateRefundBatchDetail(payRefundBatchDetail, RefundmentConstant.PAYMENT_STATE_REFUND_FAILED);
							throw new ValidationException("退款平台返回退款失败");
						}else{
							// 提示出错
							throw new ValidationException("退款平台返回退款失败,状态码非成功或失败");
						}
					}else{
						// 提示出错
						throw new ValidationException("退款平台返回退款失败,没有返回状态码");
					}
					
					
				}
				//回调业务商户失败,加入队列
				logger.error("*_*回调失败，批次单号："+refundBatch.getBatchNo());
				actualPayment.refundmentReturn(inParams, response, false);
			}
			
		} catch (RefundBatchIsSuccessException e) {
			logger.error("批次单已处理", e);
			actualPayment.refundmentReturn(inParams, response, true);
		} catch (DataAccessException e) {
			logger.error("数据库异常", e);
			refundmentService.monitorExcetpionToAlter(e);
			actualPayment.refundmentReturn(inParams, response, false);
		}  catch (ValidationException e) {
			logger.error("验证异常", e);
			refundmentService.monitorExcetpionToAlter(e);
			actualPayment.refundmentReturn(inParams, response, false);
		} catch (IllegalArgumentException e) {
			logger.error("验证异常", e);
			refundmentService.monitorExcetpionToAlter(e);
			actualPayment.refundmentReturn(inParams, response, false);
		} catch (Exception e) {
			logger.error("未知异常", e);
			refundmentService.monitorExcetpionToAlter(e);
			actualPayment.refundmentReturn(inParams, response, false);
		}
	}
	
	
	
	
	
	
	
	
	
	/**
	 * 是否需要返回信息给第三方退款平台（钩子方法，可覆写）
	 */
	protected boolean isPaymentReturned() {
		return true;
	}
	
	
	
	public Map<String, Object> validateBackParams(AbstractPayment actualPayment,
			Map<String, Object> inParams) throws DataAccessException, ValidationException, OrderIsSuccessException, PaymentRedirectException, RefundBatchIsSuccessException{
	
		HttpServletRequest request = (HttpServletRequest) inParams.get("request");
		Assert.notNull(request, "官方退款缺少参数:request");

		// 1.查询退款平台及判断
		String merchantId = String.valueOf(inParams.get("merchantid"));
		String paymentId = String.valueOf(inParams.get("paymentId"));
		Platform platform = platformService.queryPlatform(Long.valueOf(merchantId), Long.valueOf(paymentId));
		
		
		// 2.验证退款平台和IP校验
		String remoteIp = IpUtils.getRemoteAddr(request);
		platformService.validatePaymentPlatform(platform);
		platformService.validateIp(platform.getPlatformId(), platform.getLimitIp(), remoteIp);
		
		// 3.根据渠道技术信息扩展判断是否需要调用远程服务
		String extend = platform.getPlatformExt();
		// 是否调用远端服务
		Boolean callRemoteFlag = false;
		if(StringUtils.isNotEmpty(extend)){
			JSONObject extJson = JSONObject.parseObject(extend);
			// callRemote 0 不要远程服务，1 需要远程服务
			if(extJson.containsKey("callRemote") && StringUtils.isNotBlank(extJson.getString("callRemote")) && extJson.getString("callRemote").equals("1")){
				callRemoteFlag = true;
			};
		}
		
		Map<String, Object> validateParams = null;
		
		// 4.判断订单是否已处理
		String batchNo = "";//我方退款批次号
		if(callRemoteFlag){
			// a.调用远程服务
			batchNo = actualPayment.callRefundBackGetNo(request, platform);
		}else {
			// b.调用本地服务
			batchNo = actualPayment.getBatchNoFromRequest(request);
		}
		
		// 5.退款批次单查询
		PayRefundBatch refundBatch = refundmentOrderService.queryRefundBatch(batchNo);
		refundmentOrderService.checkRefundBatchIsHandled(refundBatch);
		
		// 6. 获取订单的支付平台信息
		platform = platformService.queryPlatform(Long.valueOf(merchantId), Long.valueOf(paymentId),refundBatch.getMerchantNo());
		// 6.1设置 platform
		inParams.put(RefundmentConstant.PAYMENT_PLATFORM,platform);
		
		// 7.各平台单独校验 - 校验参数的合法性
		if(callRemoteFlag){
			// a.调用远程服务
			validateParams = actualPayment.callRefundBack(request, platform ,refundBatch);
		}else {
			// b.调用本地服务
			validateParams = actualPayment.validateRefundBackParams(request, platform);
		}
		
		// 6.处理结果集,改为根据业务方返回结果判断处理
//		JSONArray details = JSONArray.parseArray(ObjectUtils.toString(validateParams.get(RefundmentConstant.REFUND_RESULT_DETAILS)));
//		boolean statusFlag = true;
//		for(Object detail :details){
//			JSONObject jsonDetail = JSONObject.parseObject(detail.toString());
//			if(StringUtils.isBlank(jsonDetail.getString("money")) || "0".equals(StringUtils.trim(jsonDetail.getString("money")))){
//	    		throw new ValidationException("退款订单金额不可以为空或零");
//	    	}
//			/*判断订单以及金额*/
//			PaymentOrder refundOrder = refundmentOrderService.queryOrderByPartnerOrderNo(ObjectUtils.toString(jsonDetail.get("orderno")));
//			if(Float.parseFloat(StringUtils.trim(jsonDetail.getString("money"))) > refundOrder.getMoney()){
//	    		throw new ValidationException("退款订单金额大于原订单金额");
//	    	}
//			
//			//通过批次号和业务方订单号查询批次明细
//			PayRefundBatchDetail payRefundBatchDetail = refundmentOrderService.queryRefundBatchDetail(refundBatch.getBatchNo(),ObjectUtils.toString(jsonDetail.get("orderno")));
//			
//			if((null!=jsonDetail.getString("statuscode") )&& "SUCCESS".equals(jsonDetail.getString("statuscode"))){
//				//更改退款批次明细单状态为退款成功
//				refundmentOrderService.updateRefundBatchDetail(payRefundBatchDetail, RefundmentConstant.PAYMENT_STATE_REFUNDED);
//			}else{
//				//一笔失败，批次就失败
//				statusFlag = false;
//				//更改订单状态为退款失败
//				refundmentOrderService.updateRefundBatchDetail(payRefundBatchDetail, RefundmentConstant.PAYMENT_STATE_REFUND_FAILED);
//				//throw new ValidationException("退款平台返回未退款");
//			}
//			
//		}

//		validateParams.put(RefundmentConstant.REFUND_STATUS_FLAG, statusFlag);
		validateParams.put(RefundmentConstant.BATCHNO, refundBatch.getBatchNo());
		validateParams.put(RefundmentConstant.PARTNER_BATCHNO, refundBatch.getPartnerBatchNo());
		validateParams.put(RefundmentConstant.PAY_IP, remoteIp); // 第三方平台IP
		validateParams.put(RefundmentConstant.PAYMENT_PLATFORM, platform); 
		return validateParams;
	}
	
	/**
	 * 订单校验 - 前期参数校验
	 * 
	 * @param orderNo
	 *            由eai传过来的钱数
	 * @return
	 * @throws DataAccessException
	 * @throws ValidationException
	 * @throws OrderIsSuccessException
	 */
	public Map<String, Object> validateOrderCheckParams(String orderNo) throws DataAccessException,
			ValidationException, OrderIsSuccessException {
		// 订单查询
		PaymentOrder paymentOrder = paymentOrderService.queryOrderByPartnerOrderNo(orderNo);
		if(paymentOrder == null){
			paymentOrder = paymentOrderService.queryOrder(orderNo);
		}
		Assert.notNull(paymentOrder, "订单查询为空,orderNo:" + orderNo);

//		if (StringUtils.isBlank(paymentOrder.getImprestMode()))
//			throw new ValidationException("充值订单中imprestMode为空，不能进行充值");

		// 判断订单是否已处理
		refundmentOrderService.checkOrderIsProcessed(paymentOrder);
		Platform platform = platformService.queryPlatform(paymentOrder.getMerchantId(), paymentOrder.getPlatformId());
		platformService.validatePaymentPlatform(platform);

		AbstractPayment actualPayment = (AbstractPayment) refundmentService.findPaymentById(paymentOrder.getPlatformId());
		Assert.notNull(actualPayment, "抽象退款平台查询为空，可能是配置不对");

		Map<String, Object> isPayedParams = new HashMap<String, Object>();
		isPayedParams.put(RefundmentConstant.PAYMENT_ORDER, paymentOrder);
		isPayedParams.put(RefundmentConstant.PAYMENT_PLATFORM, platform);
		isPayedParams.put(RefundmentConstant.REFUNDMENT, actualPayment);

		Map<String, Object> outParams = actualPayment.checkOrderIsPayed(isPayedParams);

		// 充值模式 - 订单校验中不验证

		isPayedParams.putAll(outParams);
		return isPayedParams;
	}

}
