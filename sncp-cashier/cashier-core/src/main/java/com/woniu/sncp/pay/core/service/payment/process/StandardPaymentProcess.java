package com.woniu.sncp.pay.core.service.payment.process;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.pay.common.errorcode.ErrorCode;
import com.woniu.sncp.pay.common.exception.OrderIsRefundException;
import com.woniu.sncp.pay.common.exception.OrderIsSuccessException;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.common.utils.PaymentConstant;
import com.woniu.sncp.pay.core.service.MemcachedService;
import com.woniu.sncp.pay.core.service.PaymentMerchantService;
import com.woniu.sncp.pay.core.service.PaymentOrderService;
import com.woniu.sncp.pay.core.service.PaymentService;
import com.woniu.sncp.pay.core.service.PlatformService;
import com.woniu.sncp.pay.core.service.fcb.FcbService;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.repository.pay.PaymentMerchant;
import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.web.IpUtils;

@Service("standardPaymentProcess")
public class StandardPaymentProcess extends AbstractPaymentProcess{

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Resource
	protected PaymentService paymentService;
	@Resource
	protected PlatformService platformService;
	@Resource
	private PaymentOrderService paymentOrderService;
	@Resource
	private PaymentMerchantService paymentMerchantService;
	@Resource
	private FcbService fcbService;
	
	@Resource
	private MemcachedService memcachedService;
	
	@Value("${message.push.url}")
	private String messagePushUrl;
	
	@Value("${message.push.task.type}")
	private String messagePushTaskType;
	
	@Value("${callback.task.type}")
	private String callbackTaskType;
	
	@Value("${direct.imprest.url}")
	private String directImprestUrl;
	
	public synchronized void doPay(Map<String, Object> inParams){
		HttpServletRequest request = (HttpServletRequest) inParams.get("request");
		HttpServletResponse response = (HttpServletResponse) inParams.get("response");
		AbstractPayment actualPayment = (AbstractPayment) inParams.get("abstractPayment");
		
		logger.info("++++++++++++++++++支付回调++++++++++++++++");
		logger.info("支付回调进入：" + actualPayment.getClass().getSimpleName());
		
		// 是否调用远端服务
		Boolean callPayRemoteFlag = false;
		String returned = null;
		try {
			// 1.验证参数
//			DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
			
			// 2.查询支付平台及判断
			String merchantId = String.valueOf(inParams.get("merchantid"));
			String paymentId = String.valueOf(inParams.get("paymentId"));
			Platform _platform = platformService.queryPlatform(Long.valueOf(merchantId), Long.valueOf(paymentId));
			inParams.put(PaymentConstant.PAYMENT_PLATFORM, _platform);
			// 3.根据平台扩展判断是否需要调用远程服务,add by fuzl@mysnail.com
			String merchantExt = _platform.getExtend();
			
			if(StringUtils.isNotEmpty(merchantExt)){
				JSONObject extJson = JSONObject.parseObject(merchantExt);
				// callPayRemoteFlag 0 不要远程服务，1 需要远程服务
				if(extJson.containsKey("callPayRemote") && StringUtils.isNotEmpty(extJson.getString("callPayRemote"))){
					if(extJson.getString("callPayRemote").equals("1")){
						callPayRemoteFlag = true;
					};
				}
			}
			
			// 3. 每个平台单独处理
			Map<String, Object> centerInfo = this.validateBackParams(actualPayment,inParams);
			
			String orderNo = (String) centerInfo.get(PaymentConstant.ORDER_NO);
			String oppositeOrderNo = (String) centerInfo.get(PaymentConstant.OPPOSITE_ORDERNO);
			String payIp = (String) centerInfo.get(PaymentConstant.PAY_IP);
			
			PaymentOrder paymentOrder = paymentOrderService.queryOrder(orderNo);
			
			PaymentMerchant payemntMerchnt = paymentMerchantService.queryPayemntMerchnt(paymentOrder.getMerchantId());
			
			// 4.验证是否已充值
			paymentOrderService.checkOrderIsProcessed(paymentOrder);
			
			//======= 特殊处理  ===============
			//当余额+支付平台组合支付时
			if(paymentOrder.getYueMoney()!=null &&  paymentOrder.getYueMoney() > 0){
				//充值
				Map<String, Object> retMap = fcbService.addFcbAmount(paymentOrder.getAid(), paymentOrder.getOrderNo(), paymentOrder.getMoney());
				if("1".equals(ObjectUtils.toString(retMap.get("msgcode")))){
					//充值成功
					if(!paymentOrderService.orderIsPayed(paymentOrder)){
						paymentOrder.setPayPlatformOrderId(oppositeOrderNo);
						paymentOrder.setPayPlatformIp(IpUtils.ipToLong(payIp));
						paymentOrder.setCompleteDate(new Date());
						
//						DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
						paymentOrderService.updateOrder(paymentOrder, PaymentOrder.PAYMENT_STATE_PAYED, PaymentOrder.IMPREST_STATE_COMPLETED);
						request.setAttribute("retCode", "1");
						request.setAttribute("retMsg", "操作成功");
						if(callPayRemoteFlag){
							actualPayment.callPayBackReturn(inParams, response, true);
						}else{
							actualPayment.paymentReturn(inParams, response, true);
						}
					} else{
						//已充值
						request.setAttribute("retCode", "1");
						request.setAttribute("retMsg", "操作成功");
						//已充值
						if(callPayRemoteFlag){
							actualPayment.callPayBackReturn(inParams, response, true);
						}else{
							actualPayment.paymentReturn(inParams, response, true);
						}
					}
				}else{
					//充值失败
					request.setAttribute("retCode", "0");
					request.setAttribute("retMsg", "ocp充值失败,msgcode:" + retMap.get("msgcode") + ",message:" + retMap.get("message"));
					logger.error("ocp充值失败,msgcode:" + retMap.get("msgcode") + ",message:" + retMap.get("message"));
					if(callPayRemoteFlag){
						actualPayment.callPayBackReturn(inParams, response, false);
					}else{
						actualPayment.paymentReturn(inParams, response, false);
					}
				}
				
				//扣费
				Map<String, Object> deductMap = fcbService.deductFcbAmount(paymentOrder.getAid(), 
						paymentOrder.getOrderNo(), paymentOrder.getMoney() + paymentOrder.getYueMoney(), IpUtils.longToIp(paymentOrder.getClientIp()));
				
				if("1".equals(ObjectUtils.toString(deductMap.get("msgcode")))){
					//b.回调商户,如果失败应用通过验证接口补单
					returned = paymentOrderService.callback(paymentOrder,payemntMerchnt);
					if("success".equals(returned)){
						paymentOrder.setYuePayState(PaymentOrder.PAYMENT_STATE_PAYED);
//						DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
						paymentOrderService.updateOrder(paymentOrder, PaymentOrder.PAYMENT_STATE_PAYED);
					}else{
						//TODO 不做处理 
						logger.error("回调商户失败，returnd：" + returned);
						request.setAttribute("retCode", "0");
						request.setAttribute("retMsg", "回调商户失败，returnd：" + returned);
					}
				}
				
			}else if( StringUtils.isNotBlank(paymentOrder.getPartnerBackendUrl()) && paymentOrder.getPartnerBackendUrl().endsWith(directImprestUrl)){
				//判断是否需要回调充值中心的话费流量直充接口,非其他业务方
				// 0.增加支付渠道商若返回支付成功,修改支付状态为支付成功,充值状态为未充值
				String paymentState = (String) centerInfo.get(PaymentConstant.PAYMENT_STATE);
				if (PaymentConstant.PAYMENT_STATE_PAYED.equals(paymentState)){
					// 更改订单状态为支付成功
					paymentOrderService.updateOrder(paymentOrder, PaymentOrder.PAYMENT_STATE_PAYED, null);
				}
				// 1. 查询商户号
				Platform platform = platformService.queryPlatform(paymentOrder.getMerchantId(), paymentOrder.getPlatformId());
				// 2.回调商户,如果失败应用通过验证接口补单
				returned = paymentOrderService.callbackByDirectImprest(paymentOrder,payemntMerchnt,platform);
				// 3.更新支付成功
				if(!paymentOrderService.orderIsPayed(paymentOrder) && "success".equals(returned)){
					paymentOrder.setPayPlatformOrderId(oppositeOrderNo);
					paymentOrder.setPayPlatformIp(IpUtils.ipToLong(payIp));
					paymentOrder.setCompleteDate(new Date());
					
//					DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
					paymentOrderService.updateOrder(paymentOrder, null, PaymentOrder.IMPREST_STATE_COMPLETED);
					request.setAttribute("retCode", "1");
					request.setAttribute("retMsg", "操作成功");
					if(callPayRemoteFlag){
						actualPayment.callPayBackReturn(inParams, response, true);
					}else{
						actualPayment.paymentReturn(inParams, response, true);
					}
				} else {
					logger.error("回调失败，订单号："+paymentOrder.getOrderNo());
					request.setAttribute("retCode", "0");
					request.setAttribute("retMsg", "回调失败，订单号："+paymentOrder.getOrderNo());
					if(callPayRemoteFlag){
						actualPayment.callPayBackReturn(inParams, response, false);
					}else{
						actualPayment.paymentReturn(inParams, response, false);
					}
				}
				
			}else{
				//b.回调商户,如果失败应用通过验证接口补单
//				returned = paymentOrderService.callback(paymentOrder,payemntMerchnt);
				
				// modified by fuzl@mysnail.com 回调业务方修改为异步队列来做
				returned = paymentOrderService.createCallbackSyncTask(paymentOrder, payemntMerchnt, oppositeOrderNo, callbackTaskType);
				
				//a.更新支付成功
				if(!paymentOrderService.orderIsPayed(paymentOrder) && "success".equals(returned)){
					paymentOrder.setPayPlatformOrderId(oppositeOrderNo);
					paymentOrder.setPayPlatformIp(IpUtils.ipToLong(payIp));
					paymentOrder.setCompleteDate(new Date());
					
					//TODO 创建消息队列，推送游戏
					//判断是否需要创建消息队列
					if(paymentOrder.getPartnerBackendUrl().endsWith(messagePushUrl)){
						
						//判断memcache是否已经存在此订单
						Object t = memcachedService.get("^_^"+paymentOrder.getOrderNo());
						if(null == t){
							//memcache存放paymentOrder
							memcachedService.set("^_^"+paymentOrder.getOrderNo(), 7000, paymentOrder);
							Boolean cSyncResult = paymentOrderService.createSyncTask(paymentOrder,oppositeOrderNo,messagePushUrl,messagePushTaskType);
							if(cSyncResult){
								//推送任务已创建
//								DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
								paymentOrderService.updateOrder(paymentOrder, PaymentOrder.PAYMENT_STATE_PAYED, PaymentOrder.IMPREST_STATE_COMPLETED);
								request.setAttribute("retCode", "1");
								request.setAttribute("retMsg", "操作成功");
								if(callPayRemoteFlag){
									actualPayment.callPayBackReturn(inParams, response, true);
								}else{
									actualPayment.paymentReturn(inParams, response, true);
								}
							}else{
								logger.error("推送游戏任务创建失败，订单号*_*"+paymentOrder.getOrderNo());
								request.setAttribute("retCode", "0");
								request.setAttribute("retMsg", "推送游戏任务创建失败，订单号*_*"+paymentOrder.getOrderNo());
								if(callPayRemoteFlag){
									actualPayment.callPayBackReturn(inParams, response, false);
								}else{
									actualPayment.paymentReturn(inParams, response, false);
								}
							}
							//删除memcache保存的值
							memcachedService.delete("^_^"+paymentOrder.getOrderNo());
						}else{
							logger.info("推送游戏任务正在创建，订单号^_^"+paymentOrder.getOrderNo());
							request.setAttribute("retCode", "0");
							request.setAttribute("retMsg", "推送游戏任务正在创建，订单号^_^"+paymentOrder.getOrderNo());
							if(callPayRemoteFlag){
								actualPayment.callPayBackReturn(inParams, response, false);
							}else{
								actualPayment.paymentReturn(inParams, response, false);
							}
						}
					}else{
						//不需要走消息推送
//						DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
						paymentOrderService.updateOrder(paymentOrder, PaymentOrder.PAYMENT_STATE_PAYED, PaymentOrder.IMPREST_STATE_COMPLETED);
						request.setAttribute("retCode", "1");
						request.setAttribute("retMsg", "操作成功");
						if(callPayRemoteFlag){
							actualPayment.callPayBackReturn(inParams, response, true);
						}else{
							actualPayment.paymentReturn(inParams, response, true);
						}
					}
				} else {
					logger.error("回调失败，订单号："+paymentOrder.getOrderNo());
					request.setAttribute("retCode", "0");
					request.setAttribute("retMsg", "回调失败，订单号："+paymentOrder.getOrderNo());
					if(callPayRemoteFlag){
						actualPayment.callPayBackReturn(inParams, response, false);
					}else{
						actualPayment.paymentReturn(inParams, response, false);
					}
				}
			}
				
				inParams.put(PaymentConstant.ORDER_NO, orderNo);
				inParams.put(PaymentConstant.PAYMENT_PLATFORM, centerInfo.get(PaymentConstant.PAYMENT_PLATFORM));
		} catch (OrderIsSuccessException e) {
			logger.error("订单已处理", e);
			request.setAttribute("retCode", "1");
			request.setAttribute("retMsg", "订单已处理");
			if(callPayRemoteFlag){
				actualPayment.callPayBackReturn(inParams, response, true);
			}else{
				actualPayment.paymentReturn(inParams, response, true);
			}
		} catch (DataAccessException e) {
			logger.error("数据库异常", e);
			request.setAttribute("retCode", "-1");
			request.setAttribute("retMsg", "数据库异常");
			if(callPayRemoteFlag){
				actualPayment.callPayBackReturn(inParams, response, false);
			}else{
				actualPayment.paymentReturn(inParams, response, false);
			}
		} catch (IllegalArgumentException e) {
			logger.error("验证异常", e);
			request.setAttribute("retCode", "-1");
			request.setAttribute("retMsg", "验证异常");
			if(callPayRemoteFlag){
				actualPayment.callPayBackReturn(inParams, response, false);
			}else{
				actualPayment.paymentReturn(inParams, response, false);
			}
		} catch (Exception e) {
			logger.error("未知异常", e);
			request.setAttribute("retCode", "-1");
			request.setAttribute("retMsg", "未知异常");
			if(callPayRemoteFlag){
				actualPayment.callPayBackReturn(inParams, response, false);
			}else{
				actualPayment.paymentReturn(inParams, response, false);
			}
		}
	}
	
	/**
	 * 是否需要返回信息给第三方支付平台（钩子方法，可覆写）
	 */
	protected boolean isPaymentReturned() {
		return true;
	}
	
	/**
	 * 订单后台校验
	 * 
	 * @param orderNo
	 *            订单
	 *            交易金额
	 * @return 1 - {@link PaymentConstant#PAYMENT_STATE_PAYED}<br />
	 *         2 - {@link PaymentConstant#PAYMENT_STATE_FAILED} <br />
	 *         3 - {@link PaymentConstant#PAYMENT_STATE_NOPAYED} <br />
	 *         4 - {@link PaymentConstant#PAYMENT_STATE_NOPAYED}
	 */
	public Map<String, Object> doOrderCheck(String orderNo) {
		if (logger.isInfoEnabled()) {
			logger.info("++++++++++++++++订单校验+++++++++++++++++");
			logger.info("1.订单校验进入：orderNo:" + orderNo);
		}

//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);

		String payResult = PaymentConstant.PAYMENT_STATE_NOPAYED;
		Map<String, Object> payResultMap = null;
		Map<String, Object> resultMap = null;

		try {
			payResultMap = this.validateOrderCheckParams(orderNo);
			if (logger.isInfoEnabled())
				logger.info("验证订单返回：" + JsonUtils.toJson(payResultMap));

			payResult = String.valueOf(payResultMap.get(PaymentConstant.PAYMENT_STATE));

			if (!PaymentConstant.PAYMENT_STATE_PAYED.equals(payResult)) {
				return ErrorCode.put(ErrorCode.getErrorCode(14128), "payResult", payResult);
			}

			PaymentOrder paymentOrder = (PaymentOrder) payResultMap.get(PaymentConstant.PAYMENT_ORDER);
			
			// 0.增加支付渠道商若返回支付成功,修改支付状态为支付成功,充值状态为未充值
			if (PaymentConstant.PAYMENT_STATE_PAYED.equals(payResult)){
				// 更改订单状态为支付成功,充值状态为未充值
				paymentOrderService.updateOrder(paymentOrder, PaymentOrder.PAYMENT_STATE_PAYED, null);
			}
			
			// 订单金额校验 - 对方金额和我方金额比对
			String oppositeMoney = String.valueOf(payResultMap.get(PaymentConstant.OPPOSITE_MONEY));
			if (!paymentOrderService.checkOrderMoney(paymentOrder, (new BigDecimal(oppositeMoney)).intValue())){
				throw new ValidationException("支付订单金额不匹配:我方:" + paymentOrder.getMoney() * 100 + ",对方:" + oppositeMoney);
			}
				
			logger.warn("2.订单验证通过,继续进行支付,orderNo:" + paymentOrder.getOrderNo());

			String oppositeOrderNo = String.valueOf(payResultMap.get(PaymentConstant.OPPOSITE_ORDERNO));
			// 订单校验的支付，如果是该IP则表示是支付校验充值的
			String payIp = PaymentConstant.PAYMENT_ORDER_CHECK_IP;

			// 2.验证是否已充值
			paymentOrderService.checkOrderIsProcessed(paymentOrder);
			
			//b.回调商户,如果失败应用通过查询接口来更新
			PaymentMerchant payemntMerchnt = paymentMerchantService.queryPayemntMerchnt(paymentOrder.getMerchantId());
			String returned = "";
			if( StringUtils.isNotBlank(paymentOrder.getPartnerBackendUrl()) && paymentOrder.getPartnerBackendUrl().endsWith(directImprestUrl)){
				//判断是否需要回调充值中心的话费流量直充接口,非其他业务方
				// 1. 查询商户号
				Platform platform = platformService.queryPlatform(paymentOrder.getMerchantId(), paymentOrder.getPlatformId());
				// 2. 回调充值中心,如果失败应用通过验证接口补单
				returned = paymentOrderService.callbackByDirectImprest(paymentOrder,payemntMerchnt,platform);
			}else{
				// 1. 回调业务方
				returned = paymentOrderService.callback(paymentOrder,payemntMerchnt);
			}
			
			//a.更新支付成功
			if(!paymentOrderService.orderIsPayed(paymentOrder) && "success".equals(returned)){
				paymentOrder.setPayPlatformOrderId(oppositeOrderNo);
				paymentOrder.setPayPlatformIp(IpUtils.ipToLong(payIp));
				paymentOrder.setCompleteDate(new Date());
				
				if(paymentOrder.getPartnerBackendUrl().endsWith(messagePushUrl)){
					//判断memcache是否已经存在此订单
					Object t = memcachedService.get("^_^"+paymentOrder.getOrderNo());
					if(null == t){
						//memcache存放paymentOrder
						memcachedService.set("^_^"+paymentOrder.getOrderNo(), 7000, paymentOrder);
						//TODO 创建消息队列，推送游戏
						Boolean cSyncResult = paymentOrderService.createSyncTask(paymentOrder,oppositeOrderNo,messagePushUrl,messagePushTaskType);
						if(cSyncResult){
							paymentOrderService.updateOrder(paymentOrder, PaymentOrder.PAYMENT_STATE_PAYED, PaymentOrder.IMPREST_STATE_COMPLETED);
						}else{
							//推送找不到或者创建失败
							throw new ValidationException("订单消息推送创建失败或已推送["+paymentOrder.getId()+"],tasktype："+messagePushTaskType);
						}
						//删除memcache保存的值
						memcachedService.delete("^_^"+paymentOrder.getOrderNo());
					}else{
						logger.info("推送游戏任务正在创建，订单号^_^"+paymentOrder.getOrderNo());
					}
				}else{
					paymentOrderService.updateOrder(paymentOrder, null, PaymentOrder.IMPREST_STATE_COMPLETED);
				}
			} else {
				if("success".equals(returned)){
					logger.info("订单已付款,订单号："+paymentOrder.getOrderNo());
				}else{
					logger.error("回调失败，订单号："+paymentOrder.getOrderNo());
					throw new ValidationException("订单已付款,回调该地址["+paymentOrder.getPartnerBackendUrl()+"]失败返回："+returned);
				}
			}

		} catch (DataAccessException e) {
			logger.error("订单校验数据库异常,orderNo:" + orderNo, e);
			resultMap = ErrorCode.getErrorCode(14102);
			resultMap.put("payResult", PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			resultMap.put(ErrorCode.ERROR_INFO, e.getMessage());
			return resultMap;
		} catch (IllegalArgumentException e) {
			logger.error("订单校验异常,orderNo:" + orderNo, e);
			resultMap = ErrorCode.getErrorCode(14101);
			resultMap.put("payResult", PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			resultMap.put(ErrorCode.ERROR_INFO, e.getMessage());
			return resultMap;
		} catch (OrderIsSuccessException e) {
			logger.error("订单校验:订单支付完成,无需重复处理,orderNo:" + orderNo, e);
			resultMap = ErrorCode.getErrorCode(1);
			resultMap.put("payResult", PaymentConstant.PAYMENT_STATE_PAYED);
			resultMap.put(ErrorCode.ERROR_INFO, e.getMessage());
			return resultMap;
		} catch (Exception e) {
			logger.error("订单校验未知异常,orderNo:" + orderNo, e);
			resultMap = ErrorCode.getErrorCode(14128);
			resultMap.put("payResult", PaymentConstant.PAYMENT_STATE_QUERY_ERR);
			resultMap.put(ErrorCode.ERROR_INFO, e.getMessage());
			return resultMap;
		}
		resultMap = ErrorCode.getErrorCode(1);
		resultMap.put("payResult", PaymentConstant.PAYMENT_STATE_PAYED);
		return resultMap;
	}
	
	public Map<String, Object> validateBackParams(AbstractPayment actualPayment,
			Map<String, Object> inParams) throws DataAccessException, ValidationException, OrderIsSuccessException, PaymentRedirectException, OrderIsRefundException {
	
		HttpServletRequest request = (HttpServletRequest) inParams.get("request");
		Assert.notNull(request, "官方支付缺少参数:request");

		// 1.查询支付平台及判断
		String merchantId = String.valueOf(inParams.get("merchantid"));
		String paymentId = String.valueOf(inParams.get("paymentId"));
		Platform platform = platformService.queryPlatform(Long.valueOf(merchantId), Long.valueOf(paymentId));
		
		// 2.根据平台扩展判断是否需要调用远程服务,add by fuzl@mysnail.com
		String merchantExt = platform.getExtend();
		// 是否调用远端服务
		Boolean callPayRemoteFlag = false;
		if(StringUtils.isNotEmpty(merchantExt)){
			JSONObject extJson = JSONObject.parseObject(merchantExt);
			// callPayRemoteFlag 0 不要远程服务，1 需要远程服务
			if(extJson.containsKey("callPayRemote") && StringUtils.isNotEmpty(extJson.getString("callPayRemote"))){
				if(extJson.getString("callPayRemote").equals("1")){
					callPayRemoteFlag = true;
				};
			}
		}
		
		// 3.验证支付平台和IP校验
		String remoteIp = IpUtils.getRemoteAddr(request);
		platformService.validatePaymentPlatform(platform);
		platformService.validateIp(inParams,platform.getPlatformId(), platform.getLimitIp(), remoteIp);
		
		// 4.各平台单独校验 - 校验参数的合法性
		Map<String, Object> validateParams = null;
		if(callPayRemoteFlag){
			// 4.1   调用远端服务,提交cbss-api处理第三方平台支付请求,
			String orderNo = actualPayment.callPayBackGetOrderNoFromRequest(platform, request);
			PaymentOrder payOrder = paymentOrderService.queryOrder(orderNo);
			if(null == payOrder){
				throw new ValidationException("订单["+orderNo+"],获取我方订单失败.");
			}
			validateParams = actualPayment.callPayBack(request, platform,payOrder);
		}else{
			// 4.2 调用本地服务
			validateParams = actualPayment.validateBackParams(request, platform);
		}
				
		// 5.判断订单是否已处理
		PaymentOrder paymentOrder = (PaymentOrder) validateParams.get(PaymentConstant.PAYMENT_ORDER);
		paymentOrderService.checkOrderIsProcessed(paymentOrder);
		
		// 6.订单金额校验
		String oppositeMoney = (String) validateParams.get(PaymentConstant.OPPOSITE_MONEY);
		if (!paymentOrderService.checkOrderMoney(paymentOrder, (int) NumberUtils.toFloat(oppositeMoney)))
			throw new ValidationException("订单["+paymentOrder.getOrderNo()+"]金额不匹配:我方:" + paymentOrder.getMoney() * 100 + ",对方:" + oppositeMoney);
		
		
		// 7.设置订单状态（支付平台返回值判断） 
		String paymentState = (String) validateParams.get(PaymentConstant.PAYMENT_STATE);
		if (PaymentConstant.PAYMENT_STATE_PAYED.equals(paymentState)){
			// 更改订单状态为支付成功
			//paymentOrderService.updateOrder(paymentOrder, PaymentOrder.PAYMENT_STATE_PAYED, null);
		} else if (PaymentConstant.PAYMENT_STATE_FAILED.equals(paymentState)) {
			// 更改订单状态为支付失败
			paymentOrderService.updateOrder(paymentOrder, PaymentOrder.PAYMENT_STATE_FAILED, null);
			throw new ValidationException("支付平台返回支付失败");
		} else {
			// 其他为未支付，不做操作
			throw new ValidationException("支付平台返回未支付");
		}

		validateParams.put(PaymentConstant.ORDER_NO, paymentOrder.getOrderNo());
		validateParams.put(PaymentConstant.PAY_IP, remoteIp); // 第三方平台IP
		validateParams.put(PaymentConstant.PAYMENT_PLATFORM, platform); 
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
	 * @throws OrderIsRefundException 
	 */
	public Map<String, Object> validateOrderCheckParams(String orderNo) throws DataAccessException,
			ValidationException, OrderIsSuccessException, OrderIsRefundException {
		// 1. 订单查询
		PaymentOrder paymentOrder = paymentOrderService.queryOrderByPartnerOrderNo(orderNo);
		if(paymentOrder == null){
			paymentOrder = paymentOrderService.queryOrder(orderNo);
		}
		Assert.notNull(paymentOrder, "订单查询为空,orderNo:" + orderNo);

//		if (StringUtils.isBlank(paymentOrder.getImprestMode()))
//			throw new ValidationException("充值订单中imprestMode为空，不能进行充值");

		// 2.判断订单是否已处理
		paymentOrderService.checkOrderIsProcessed(paymentOrder);
		Platform platform = platformService.queryPlatform(paymentOrder.getMerchantId(), paymentOrder.getPlatformId(),paymentOrder.getMerchantNo());
		platformService.validatePaymentPlatform(platform);

		AbstractPayment actualPayment = (AbstractPayment) paymentService.findPaymentById(paymentOrder.getPlatformId());
		Assert.notNull(actualPayment, "抽象支付平台查询为空，可能是配置不对");
		
		Map<String, Object> isPayedParams = new HashMap<String, Object>();
		isPayedParams.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		isPayedParams.put(PaymentConstant.PAYMENT_PLATFORM, platform);
		isPayedParams.put(PaymentConstant.PAYMENT, actualPayment);
		
		
		// 3.根据平台扩展判断是否需要调用远程服务,add by fuzl@mysnail.com
		String merchantExt = platform.getExtend();
		// 是否调用远端服务
		Boolean callPayRemoteFlag = false;
		if(StringUtils.isNotEmpty(merchantExt)){
			JSONObject extJson = JSONObject.parseObject(merchantExt);
			// callPayRemoteFlag 0 不要远程服务，1 需要远程服务
			if(extJson.containsKey("callPayRemote") && StringUtils.isNotEmpty(extJson.getString("callPayRemote"))){
				if(extJson.getString("callPayRemote").equals("1")){
					callPayRemoteFlag = true;
				};
			}
		}
		
		Map<String, Object> outParams = null;
		if(callPayRemoteFlag){
			// 调用远端服务
			outParams = actualPayment.callPayQuery(isPayedParams);
		}else{
			// 调用本地服务
			outParams = actualPayment.checkOrderIsPayed(isPayedParams);
		}
		
		// 充值模式 - 订单校验中不验证

		isPayedParams.putAll(outParams);
		return isPayedParams;
	}

	@Override
	public Map<String, Object> cancelOrder(String orderNo,long merchantId,long platformId,Map<String,Object> extParams) {
		Map<String,Object> retMap = new HashMap<String,Object>();
		
		PaymentOrder paymentOrder = null;
		try{
			paymentOrder = paymentOrderService.queryOrderByPartnerOrderNo(orderNo);
			if(paymentOrder == null){
				paymentOrder = paymentOrderService.queryOrder(orderNo);
			}
		
			if(paymentOrder == null){
				logger.error("订单查询为空,orderNo:" + orderNo);
				retMap = ErrorCode.getErrorCode(14105);
				return retMap;
			}
			
			if(paymentOrder.getMerchantId() != merchantId || paymentOrder.getPlatformId() != platformId){
				retMap = ErrorCode.getErrorCode(53101);
				return retMap;
			}
		} catch (Exception e){
			retMap = ErrorCode.getErrorCode(53106);
			return retMap;
		}
		
		if(!PaymentOrder.PAYMENT_STATE_CREATED.equals(paymentOrder.getPaymentState()) && !PaymentConstant.PAYMENT_STATE_NOPAYED.equals(paymentOrder.getPaymentState())){
			retMap = ErrorCode.getErrorCode(53102);
			retMap.put("orderno", orderNo);
			retMap.put("state", paymentOrder.getPaymentState());
			logger.error("订单状态非未支付状态不可取消,orderNo:" + orderNo+",state:"+paymentOrder.getPaymentState());
			return retMap;
		}
		
		AbstractPayment actualPayment = (AbstractPayment) paymentService.findPaymentById(paymentOrder.getPlatformId());
		if(actualPayment == null){
			retMap = ErrorCode.getErrorCode(53103);
			logger.error("XML抽象平台查询为空,orderNo:" + orderNo+",state:"+paymentOrder.getPaymentState());
			return retMap;
		}
		
		Platform platform = null;
		try{
			platform = platformService.queryPlatform(paymentOrder.getMerchantId(), paymentOrder.getPlatformId());
			if(platform == null || StringUtils.isBlank(platform.getType()) || (","+platform.getOperatorType()+",").indexOf(","+Platform.OPERATOR_TYPE_CANCEL+",") == -1){
				retMap = ErrorCode.getErrorCode(53101);
				logger.error("申请号无权限取消订单,orderNo:" + orderNo+",state:"+paymentOrder.getPaymentState());
				return retMap;
			}
		} catch (Exception e){
			retMap = ErrorCode.getErrorCode(53107);
			logger.error("查询平台时数据库出错,orderNo:" + orderNo+",state:"+paymentOrder.getPaymentState());
			return retMap;
		}
		
		Map<String, Object> cancelParams = new HashMap<String, Object>();
		cancelParams.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
		cancelParams.put(PaymentConstant.PAYMENT_PLATFORM, platform);
		
		cancelParams.putAll(extParams);
		
		Map<String, Object> cancelRet = actualPayment.cancelOrder(cancelParams);
		if(cancelRet == null){
			retMap = ErrorCode.getErrorCode(53103);
			return retMap;
		}
		
		String state = String.valueOf(cancelRet.get(PaymentConstant.PAYMENT_STATE));
		if(PaymentConstant.PAYMENT_STATE_CANCEL.equalsIgnoreCase(state)){
			paymentOrder.setPaymentState(PaymentConstant.PAYMENT_STATE_CANCEL);
		} else {
			retMap = ErrorCode.getErrorCode(53104);
			return retMap;
		}
		try{
			paymentOrderService.updateOrder(paymentOrder, paymentOrder.getPaymentState(), null);
			retMap = ErrorCode.getErrorCode(1);
			retMap.put("orderno", orderNo);
			retMap.put("state", paymentOrder.getPaymentState());
		} catch (Exception e){
			retMap = ErrorCode.getErrorCode(53105);
		}
		
		return retMap;
	}

}
