package com.woniu.sncp.pay.core.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSONObject;
import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.ocp.utils.ProxoolUtil;
import com.woniu.sncp.pay.common.exception.OrderIsRefundException;
import com.woniu.sncp.pay.common.exception.OrderIsSuccessException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.threadpool.ThreadPool;
import com.woniu.sncp.pay.common.utils.date.DateUtils;
import com.woniu.sncp.pay.common.utils.encrypt.EncryptFactory;
import com.woniu.sncp.pay.common.utils.http.HttpUtils;
import com.woniu.sncp.pay.common.utils.http.IpUtils;
import com.woniu.sncp.pay.core.service.m.MQueueService;
import com.woniu.sncp.pay.core.service.monitor.MonitorMessageService;
import com.woniu.sncp.pay.core.service.monitor.MonitorMessageTask;
import com.woniu.sncp.pay.core.service.payment.conf.PaymentProperties;
import com.woniu.sncp.pay.core.service.schedule.Schedule;
import com.woniu.sncp.pay.core.service.schedule.SyncTaskSchedule;
import com.woniu.sncp.pay.dao.BaseSessionDAO;
import com.woniu.sncp.pay.repository.pay.MessageQueue;
import com.woniu.sncp.pay.repository.pay.MessageQueueLog;
import com.woniu.sncp.pay.repository.pay.PassportAsyncTask;
import com.woniu.sncp.pay.repository.pay.PaymentMerchant;
import com.woniu.sncp.pay.repository.pay.PaymentOrderRepository;
import com.woniu.sncp.pojo.payment.PaymentOrder;


@Service("paymentOrderService")
public class PaymentOrderService{
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource
	private BaseSessionDAO sessionDao;
	
	@Resource
	private PaymentOrderRepository paymentOrderRepository;
	
	@Resource
	private PaymentConstant paymentConstant;
	
	@Resource
	private MonitorMessageService monitorMessageService;
	
	@Resource
	private Schedule syncTaskSchedule;
	
	@Resource
	private MQueueService mQueueService;
	
	@Value("${message.push.url}")
	private String messagePushUrl;
	
	@Value("${message.push.task.type}")
	private String messagePushTaskType;
	
	@Resource
	private ThreadPool threadPool;
	
	public void createOrder(PaymentOrder paymentOrder, long issuerId)
			throws DataAccessException {
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
		long sequence = sessionDao.findForLong("select sn_imprest.pay_order_sq.nextval from dual");

		Date now = Calendar.getInstance().getTime();
		paymentOrder.setCreateDate(now); // 时间
		paymentOrder.setCompleteDate(now);
		paymentOrder.setId(sequence);
		
//		paymentOrderDao.save(paymentOrder);
		paymentOrderRepository.save(paymentOrder);

		if (logger.isInfoEnabled())
			logger.info("支付生成订单成功：" + paymentOrder.getOrderNo());
	}
	
	/**
	 * 生成订单号并创建订单
	 * @param paymentOrder
	 * @param issuerId
	 * @throws DataAccessException
	 */
	public void createOrderAndGenOrderNo(PaymentOrder paymentOrder, long issuerId,String timeoutExpress)
			throws DataAccessException {
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
//		long sequence = sessionDao.findForLong("select sn_imprest.pay_order_sq.nextval from dual");
		final String orderSeqSql = "INSERT INTO SN_PAY.PAY_ORDER_SQ(N_ID) VALUES(NULL)";
		Long sequence = mQueueService.getSequence(orderSeqSql);
		

		Date now = Calendar.getInstance().getTime();
		paymentOrder.setCreateDate(now); // 时间
		paymentOrder.setCompleteDate(now);
		String today = DateFormatUtils.format(now, "yyyyMMdd");
		String orderNo = today + "-" + paymentOrder.getPlatformId() + "-"
				+ StringUtils.leftPad(String.valueOf(issuerId), 3, '0') + "-"
				+ StringUtils.leftPad(String.valueOf(sequence), 10, '0');
		
		if(paymentOrder.getPlatformId() == 4001 || paymentOrder.getPlatformId() == 4002 
				|| paymentOrder.getPlatformId() == 4003 ){
			//4001.兔兔币支付，4002.翡翠币web，4003.翡翠币wap
			orderNo = today + "-" + paymentOrder.getMerchantId()+ '-' + paymentOrder.getPlatformId() + "-"
					+ StringUtils.leftPad(String.valueOf(issuerId), 3, '0') + "-"
					+ StringUtils.leftPad(String.valueOf(sequence), 10, '0');
			paymentOrder.setOrderNo(orderNo);
		}else if(paymentOrder.getPlatformId() == 1012){
			//中国银行信用卡分期支付平台id
			orderNo = today + StringUtils.leftPad(String.valueOf(sequence), 10, '0');
			paymentOrder.setOrderNo(ObjectUtils.toString(orderNo));
		}else{
			paymentOrder.setOrderNo(orderNo);
		}
		paymentOrder.setCreateDate(now); // 时间
		paymentOrder.setCompleteDate(now);
		paymentOrder.setId(sequence);
		
		if (StringUtils.isBlank(paymentOrder.getPayPlatformOrderId())){
			paymentOrder.setPayPlatformOrderId(orderNo);
		}
		
		Date __timeoutExpress = null;
		if(StringUtils.isNotBlank(timeoutExpress)){
			//设置最晚付款时间
			String _timeoutExpress = DateUtils.format(
					org.apache.commons.lang.time.DateUtils.addMinutes(
							paymentOrder.getCreateDate(), Integer.parseInt(timeoutExpress)), DateUtils.DATE_FORMAT_DATETIME_COMPACT);
			__timeoutExpress = DateUtils.parseDate(_timeoutExpress,DateUtils.DATE_FORMAT_DATETIME_COMPACT);
			paymentOrder.setTimeoutExpress(__timeoutExpress);
		}else{
			// 默认最晚付款时间24小时
			String _timeoutExpress = DateUtils.format(
					org.apache.commons.lang.time.DateUtils.addMinutes(now, PaymentConstant.DEFAULT_TIMEOUTEXPRESS), DateUtils.DATE_FORMAT_DATETIME_COMPACT);
			__timeoutExpress = DateUtils.parseDate(_timeoutExpress,DateUtils.DATE_FORMAT_DATETIME_COMPACT);
			paymentOrder.setTimeoutExpress(__timeoutExpress);
		}
		
		paymentOrderRepository.save(paymentOrder);

		if (logger.isInfoEnabled())
			logger.info("支付生成订单成功：" + paymentOrder.getOrderNo());
	}

	public void checkOrderIsProcessed(PaymentOrder paymentOrder)
			throws OrderIsSuccessException, ValidationException, OrderIsRefundException {
		
		if(paymentOrder.getPartnerBackendUrl().equals(messagePushUrl)){
			//TODO 校验队列执行状态
			PassportAsyncTask dbtask = syncTaskSchedule.querySchedule(paymentOrder.getId(), messagePushTaskType);
			if(null == dbtask){
				//TODO 创建消息队列，推送游戏
				Boolean cSyncResult = this.createSyncTask(paymentOrder,paymentOrder.getPayPlatformOrderId(),messagePushUrl,messagePushTaskType);
				if(!cSyncResult){
					//推送找不到或者创建失败
					throw new ValidationException("订单消息推送创建失败或已推送["+paymentOrder.getId()+"],tasktype:"+messagePushTaskType);
				}else{
					// 1.已充值
					// 2.未充值 + 支付失败
					if (PaymentOrder.IMPREST_STATE_COMPLETED.equals(paymentOrder.getImprestState())) {
						String msg = "订单已成功支付，勿重复处理:" + paymentOrder.getOrderNo();
						throw new OrderIsSuccessException(msg);
					}
					// 4.已退款
					if (PaymentConstant.PAYMENT_STATE_QUERY_ERR.equals(paymentOrder.getImprestState())) {
						String msg = "订单已退款，勿重复处理:" + paymentOrder.getOrderNo();
						throw new OrderIsRefundException(msg);
					}
				}
			}
		}else{
			// 1.已充值
			// 2.未充值 + 支付失败
			if (PaymentOrder.IMPREST_STATE_COMPLETED.equals(paymentOrder.getImprestState())) {
				String msg = "订单已成功支付，勿重复处理:" + paymentOrder.getOrderNo();
				throw new OrderIsSuccessException(msg);
			}
			// 4.已退款
			if (PaymentConstant.PAYMENT_STATE_QUERY_ERR.equals(paymentOrder.getImprestState())) {
				String msg = "订单已退款，勿重复处理:" + paymentOrder.getOrderNo();
				throw new OrderIsRefundException(msg);
			}
		}
	}
	
	public boolean orderIsPayed(PaymentOrder paymentOrder){
		if (PaymentOrder.PAYMENT_STATE_PAYED.equals(paymentOrder.getPaymentState())
				&& PaymentOrder.IMPREST_STATE_COMPLETED.equals(paymentOrder.getImprestState())) {
			return true;
		}
		
		return false;
	}

	public boolean checkOrderMoney(PaymentOrder paymentOrder, int paymentMoney) {
		BigDecimal orderMoney = new BigDecimal(paymentOrder.getMoney().toString());
		int iOrderMoney = orderMoney.multiply(new BigDecimal(100)).intValue();
		// 订单金额校验
		if (iOrderMoney != paymentMoney) {
			if (logger.isInfoEnabled())
				logger.info("充值订单金额不匹配:我方:" + iOrderMoney + ",对方:" + paymentMoney);
			return false;
		}
		return true;
	}

	public PaymentOrder queryOrder(String orderNo) throws DataAccessException {
		// 切换中心库
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);//网厅需要实时读，不接受延时，所以改到中心库
//		return paymentOrderDao.findByProperty("orderNo", orderNo);
		return paymentOrderRepository.findByOrderNo(orderNo);
	}
	
	public PaymentOrder queryOrderByPartnerOrderNo(String pOrderNo) throws DataAccessException {
		// 切换中心库
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);//网厅需要实时读，不接受延时，所以改到中心库
//		return paymentOrderDao.findByProperty("partnerOrderNo", pOrderNo);
		return paymentOrderRepository.findByPartnerOrderNo(pOrderNo);
	}
	
	public PaymentOrder queyrOrderByOppositeOrderNo(String oppositeOrderNo)
			throws DataAccessException {
		// 切换中心库
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
//		return paymentOrderDao.findByProperty("payPlatformOrderId", oppositeOrderNo);
		return paymentOrderRepository.findByPayPlatformOrderId(oppositeOrderNo);
	}

	public void updateOrder(PaymentOrder paymentOrder, String payedState,
			String imprestState) throws DataAccessException,
			IllegalArgumentException {
		logger.info("更改支付订单" + paymentOrder.getOrderNo() + "为：payedState:" + payedState + ",imprestState:"
				+ imprestState);
		if (StringUtils.isBlank(payedState) && StringUtils.isBlank(imprestState))
			throw new IllegalArgumentException("更新订单状态参数错误，payedState和imprestState不能都为空");

		if (StringUtils.isNotBlank(payedState))
			paymentOrder.setPaymentState(payedState);

		if (StringUtils.isNotBlank(imprestState))
			paymentOrder.setImprestState(imprestState);

//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
//		paymentOrderDao.update(paymentOrder);
		paymentOrderRepository.updateSS(paymentOrder.getId(), payedState,imprestState);
	}
	
	public void updateOrder(PaymentOrder paymentOrder, String yuePayState) throws DataAccessException,
			IllegalArgumentException {
		logger.info("更改支付订单" + paymentOrder.getOrderNo() + "为：yuePayState:" + yuePayState );
		if (StringUtils.isBlank(yuePayState))
			throw new IllegalArgumentException("更新订单状态参数错误，yuePayState不能为空");

		paymentOrder.setPaymentState(yuePayState);
//		paymentOrderDao.update(paymentOrder);
		paymentOrderRepository.updateS(paymentOrder.getId(), yuePayState);
	}
	
	public String callback(PaymentOrder paymentOrder,PaymentMerchant payemntMerchnt){
		
		Map<String,String> nameValuePair = new HashMap<String,String>();
		TreeMap<String,String> treeMap = new TreeMap<String,String>();
		
		try {
			treeMap.put("orderno", paymentOrder.getOrderNo());
			treeMap.put("aid", ObjectUtils.toString(paymentOrder.getAid()));
			treeMap.put("platformid", ObjectUtils.toString(paymentOrder.getPlatformId()));
			treeMap.put("imprestmode", ObjectUtils.toString(paymentOrder.getImprestMode()));
			if(paymentOrder.getYueMoney()!=null &&  paymentOrder.getYueMoney() > 0 ){
				treeMap.put("money", ObjectUtils.toString(paymentOrder.getMoney() + paymentOrder.getYueMoney()));
			}else{
				treeMap.put("money", ObjectUtils.toString(paymentOrder.getMoney()));
			}
			treeMap.put("paystate", PaymentOrder.PAYMENT_STATE_PAYED);
			
			if(!StringUtils.isBlank(paymentOrder.getPartnerOrderNo())){
				treeMap.put("partnerorderno", paymentOrder.getPartnerOrderNo());
				nameValuePair.put("partnerorderno", paymentOrder.getPartnerOrderNo());
			}
			
			String keyType = payemntMerchnt.getKeyType();
			String key = payemntMerchnt.getMerchantKey();
			if(StringUtils.isBlank(key)){
				logger.error("回调密钥为空,订单号:" + paymentOrder.getOrderNo()+",平台id:"+paymentOrder.getPlatformId());
				throw new IllegalArgumentException("平台Id["+paymentOrder.getPlatformId()+"]回调密钥为空");
			}
			
			nameValuePair.put("orderno", paymentOrder.getOrderNo());
			nameValuePair.put("aid", ObjectUtils.toString(paymentOrder.getAid()));
			nameValuePair.put("platformid", ObjectUtils.toString(paymentOrder.getPlatformId()));
			nameValuePair.put("imprestmode", ObjectUtils.toString(paymentOrder.getImprestMode()));
			if(paymentOrder.getYueMoney()!=null &&  paymentOrder.getYueMoney() > 0){
				nameValuePair.put("money", ObjectUtils.toString(paymentOrder.getMoney()+paymentOrder.getYueMoney()));
			}else{
				nameValuePair.put("money", ObjectUtils.toString(paymentOrder.getMoney()));
			}
			nameValuePair.put("paystate", PaymentOrder.PAYMENT_STATE_PAYED);
			nameValuePair.put("sign", ObjectUtils.toString(signStr(treeMap,key,keyType)));
			
			String backendUrl = paymentOrder.getPartnerBackendUrl();
			if(StringUtils.isBlank(backendUrl)){
				throw new IllegalArgumentException("["+ObjectUtils.toString(paymentOrder.getPlatformId())+"]回调地址为空，请检查配置文件[callback.properties]");
			}
			long starttime = System.currentTimeMillis();
			logger.info("订单号:" + paymentOrder.getOrderNo() + " 回调地址:"+backendUrl + " 参数:" + nameValuePair + " 开始时间:" + starttime);
			String response = HttpUtils.post(backendUrl, nameValuePair);
			long endtime = System.currentTimeMillis();
			logger.info("该回调地址["+backendUrl+"],订单号："+paymentOrder.getOrderNo()+",返回值:"+response + ",结束时间:" + endtime +",执行耗时:"+ (endtime-starttime));
			return response;
		} catch (ClientProtocolException e) {
			logger.error("回调商户出错，"+e.getMessage(),e);
		} catch (IOException e) {
			logger.error("回调商户出错，"+e.getMessage(),e);
		} catch (Exception e){
			logger.error("回调商户出错，"+e.getMessage(),e);
		}
		
		//modified by fuzl@snail.com 增加内网ip和当前应用域名信息
		String localIp = IpUtils.getLoaclAddr();// 对方服务器IP
		String serverName = "";
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		if(null!=request){
			serverName = request.getServerName();//当前应用域名
		}else{
			serverName = "cashier.woniu.com"; 
		}
		
		String alertMsg = "所在应用:["+serverName+"],服务器:["+localIp +":"+ ProxoolUtil.getTomcatPort() + "@" + ProxoolUtil.getPid() +"],\n支付订单号：" + paymentOrder.getOrderNo() + ",回调商户地址：" + paymentOrder.getPartnerBackendUrl()+ "失败";
		logger.info(alertMsg);
		threadPool.executeTask(new MonitorMessageTask(alertMsg));
//		monitorMessageService.sendMsg(alertMsg);
		return "failed";
	}
	
	/**
	 * 回调蜗牛话费流量直充中心
	 * @param paymentOrder
	 * @param payemntMerchnt
	 * @return
	 */
	public String callbackByDirectImprest(PaymentOrder paymentOrder,PaymentMerchant payemntMerchnt,Platform platform){
		
		Map<String,String> nameValuePair = new HashMap<String,String>();
		TreeMap<String,String> treeMap = new TreeMap<String,String>();
		
		try {
			treeMap.put("orderno", paymentOrder.getOrderNo());
			treeMap.put("aid", ObjectUtils.toString(paymentOrder.getAid()));
			treeMap.put("platformid", ObjectUtils.toString(paymentOrder.getPlatformId()));
			treeMap.put("imprestmode", ObjectUtils.toString(paymentOrder.getImprestMode()));
			if(paymentOrder.getYueMoney()!=null &&  paymentOrder.getYueMoney() > 0 ){
				treeMap.put("money", ObjectUtils.toString(paymentOrder.getMoney() + paymentOrder.getYueMoney()));
			}else{
				treeMap.put("money", ObjectUtils.toString(paymentOrder.getMoney()));
			}
			treeMap.put("paystate", PaymentOrder.PAYMENT_STATE_PAYED);
			
			if(!StringUtils.isBlank(paymentOrder.getPartnerOrderNo())){
				treeMap.put("partnerorderno", paymentOrder.getPartnerOrderNo());
				nameValuePair.put("partnerorderno", paymentOrder.getPartnerOrderNo());
			}
			
			String keyType = payemntMerchnt.getKeyType();//业务申请渠道的加密方式
			
			String key = platform.getBackendKey();//获取商户的回调密钥
			if(StringUtils.isBlank(key)){
				logger.error("回调密钥为空,订单号:" + paymentOrder.getOrderNo()+",平台id:"+paymentOrder.getPlatformId());
				throw new IllegalArgumentException("平台Id["+paymentOrder.getPlatformId()+"]回调密钥为空");
			}
			
			nameValuePair.put("orderno", paymentOrder.getOrderNo());
			nameValuePair.put("aid", ObjectUtils.toString(paymentOrder.getAid()));
			nameValuePair.put("platformid", ObjectUtils.toString(paymentOrder.getPlatformId()));
			nameValuePair.put("imprestmode", ObjectUtils.toString(paymentOrder.getImprestMode()));
			if(paymentOrder.getYueMoney()!=null &&  paymentOrder.getYueMoney() > 0){
				nameValuePair.put("money", ObjectUtils.toString(paymentOrder.getMoney()+paymentOrder.getYueMoney()));
			}else{
				nameValuePair.put("money", ObjectUtils.toString(paymentOrder.getMoney()));
			}
			nameValuePair.put("paystate", PaymentOrder.PAYMENT_STATE_PAYED);
			nameValuePair.put("sign", ObjectUtils.toString(signStr(treeMap,key,keyType)));
			
			String backendUrl = paymentOrder.getPartnerBackendUrl();
			if(StringUtils.isBlank(backendUrl)){
				throw new IllegalArgumentException("["+ObjectUtils.toString(paymentOrder.getPlatformId())+"]回调地址为空，请检查配置文件[callback.properties]");
			}
			long starttime = System.currentTimeMillis();
			logger.info("订单号:" + paymentOrder.getOrderNo() + " 回调地址:"+backendUrl + " 参数:" + nameValuePair+ " 访问开始时间:"+starttime);
			String response = HttpUtils.post(backendUrl, nameValuePair);
			long endtime = System.currentTimeMillis();
			logger.info("该回调地址["+backendUrl+"],订单号："+paymentOrder.getOrderNo()+",返回值:"+response+",访问结束时间:"+endtime+",执行耗时:"+ (endtime-starttime));
			return response;
		} catch (ClientProtocolException e) {
			logger.error("回调商户出错，"+e.getMessage(),e);
		} catch (IOException e) {
			logger.error("回调商户出错，"+e.getMessage(),e);
		} catch (Exception e){
			logger.error("回调商户出错，"+e.getMessage(),e);
		}
		
		//modified by fuzl@snail.com 增加内网ip和当前应用域名信息
		String localIp = IpUtils.getLoaclAddr();// 对方服务器IP
		String serverName = "";
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		if(null!=request){
			serverName = request.getServerName();//当前应用域名
		}else{
			serverName = "cashier.woniu.com"; 
		}
		
		String alertMsg = "所在应用:["+serverName+"],服务器:["+localIp +":"+ ProxoolUtil.getTomcatPort() + "@" + ProxoolUtil.getPid() +"],\n支付订单号：" + paymentOrder.getOrderNo() + ",回调商户地址：" + paymentOrder.getPartnerBackendUrl()+ "失败";
		logger.info(alertMsg);
		monitorMessageService.sendMsg(alertMsg);
		return "failed";
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
	 * 生成第三方订单号
	 * 
	 * @param paymentOrder
	 * @param issuerId
	 * @throws DataAccessException
	 */
	public String genThirdPartyNo(String second,String third) throws DataAccessException {
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
		long sequence = sessionDao.findForLong("select sn_imprest.imp_thirdparty_order_sq.nextval from dual");

		Date now = Calendar.getInstance().getTime();
		String today = DateFormatUtils.format(now, "yyyyMMdd");
		String orderNo = today  + "-" + second  + "-"
				+ StringUtils.leftPad(String.valueOf(third), 3, '0') + "-"
				+ StringUtils.leftPad(String.valueOf(sequence), 10, '0');
		
		return orderNo;
	}
	
	
	public String getOrderMode(String mode,String bankCd){
		String orderMode = StringUtils.isNotBlank(mode)?mode:"1";
		if(StringUtils.isNotBlank(bankCd)){
			orderMode = paymentConstant.getWebBankMap().get(bankCd);
			if(StringUtils.isBlank(orderMode)){
				orderMode = StringUtils.isNotBlank(mode)?mode:"1";
			}
		}
		
		return orderMode;
	}
	
	public String getOrderMode(String platformId){
		String paymentMode = PaymentProperties.getValue("PAYMENT_MODE_"+platformId);
		return (StringUtils.isBlank(paymentMode)) ? "1" :  paymentMode;
	}
	
	/**
	 * 获取PC快钱快捷银行编码
	 * @param bankCd
	 * @return
	 */
	public String getKqBankCode(String bankCd){
		String bankCode = "";
		if(StringUtils.isNotBlank(bankCd)){
			bankCode = paymentConstant.getKqBankCodeMap().get(bankCd);
		}
		return bankCode;
	}

	/**
	 * 创建推送消息队列
	 * @param paymentOrder     收银台订单
	 * @param oppositeOrderNo  第三方支付平台交易号
	 * @param messagePushUrl   消息推送接口标识
	 * @param messagePushTaskType消息推送类型
	 * @return
	 */
	public synchronized Boolean createSyncTask(PaymentOrder paymentOrder,String oppositeOrderNo,
			String messagePushUrl,String messagePushTaskType) {
		
		Boolean flag = false;
		try {
			if (StringUtils.isBlank(messagePushUrl) || StringUtils.isBlank(messagePushTaskType))
				throw new IllegalArgumentException("创建消息推送参数错误，messagePushUrl和messagePushTaskType不能为空");
			
			// TODO 验证推送队列是否存在?
			Map<String, Object> inParams = null;
			PassportAsyncTask dbTask = syncTaskSchedule.querySchedule(paymentOrder.getId(),messagePushTaskType);//tasktype "139" 手游PC版本支付消息
			if(null == dbTask){
				inParams = new HashMap<String, Object>();
				JSONObject extend = new JSONObject();
				if(StringUtils.isNotBlank(paymentOrder.getExtend())){
					extend = JSONObject.parseObject(paymentOrder.getExtend());
				}else{
					logger.info("订单扩展未配置,orderNo:"+paymentOrder.getOrderNo()+",ext:"+paymentOrder.getExtend());
					return false;
				}
				
				//推送消息详情
				JSONObject messagePushObj = new JSONObject();
				messagePushObj.put(PaymentConstant.PARTNER_ORDERNO, paymentOrder.getPartnerOrderNo());//业务订单号
				messagePushObj.put(PaymentConstant.ORDER_MONEY, paymentOrder.getMoney());//金额
				messagePushObj.put(PaymentConstant.ORDER_ACCOUNT_ID, paymentOrder.getAid());//蜗牛通行证id
				messagePushObj.put(PaymentConstant.OPPOSITE_ORDERNO, oppositeOrderNo);//第三方支付平台订单号
				messagePushObj.put(PaymentConstant.SERVER_ID, extend.get(PaymentConstant.SERVER_ID));//服务器id
				messagePushObj.put(PaymentConstant.GAME_ID, paymentOrder.getGameId());//游戏id
				
				inParams.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
				inParams.put(PaymentConstant.MESSAGE_PUSH_TASK_OBJ, messagePushObj.toString());
				inParams.put(PaymentConstant.MESSAGE_PUSH_TASK_TYPE, messagePushTaskType);
				
				PassportAsyncTask task = syncTaskSchedule.createSchedule(inParams);
				logger.info("创建消息推送队列id:"+task.getId()+",tasktype:"+messagePushTaskType);
				if(null != task.getId() && task.getId() > 0){
					return true;
				}else{
					//创建失败
					return false;
				}
			}else{
				if(dbTask.getState().equals(SyncTaskSchedule.SEHEDULE_STATE_COMPLETE)){
					//已推送
					return false;
				}
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("创建消息推送失败:"+e);
		}
		return flag;
	}
	
	/**
	 * 创建收银台回调业务方队列任务
	 * @param paymentOrder        收银台订单
	 * @param oppositeOrderNo     第三方支付平台交易号
	 * @param callbackTaskType    收银台回调业务方队列任务类型
	 * @return
	 */
	public synchronized String createCallbackSyncTask(PaymentOrder paymentOrder,PaymentMerchant payemntMerchnt,String oppositeOrderNo,String callbackTaskType) {
		try {
			if (StringUtils.isBlank(callbackTaskType))
				throw new IllegalArgumentException("创建收银台回调业务方队列任务参数错误，callbackTaskType不能为空");
			
			// TODO 验证推送队列是否存在?
			Map<String, Object> inParams = null;
			//sn_queue.que_message_queue
			MessageQueue dbTask = mQueueService.queryMessageQueue(paymentOrder.getId(),Long.parseLong(callbackTaskType));//tasktype "174" 收银台回调业务方队列任务类型
			//sn_queue.que_message_log
			MessageQueueLog queueLog = mQueueService.queryQueueLog(paymentOrder.getId(),Long.parseLong(callbackTaskType));//tasktype "174" 收银台回调业务方队列任务类型
			
			if(null == dbTask  && null == queueLog){
				inParams = new HashMap<String, Object>();
				TreeMap<String,String> treeMap = new TreeMap<String,String>();
				treeMap.put("orderno", paymentOrder.getOrderNo());
				treeMap.put("aid", ObjectUtils.toString(paymentOrder.getAid()));
				treeMap.put("platformid", ObjectUtils.toString(paymentOrder.getPlatformId()));
				treeMap.put("imprestmode", ObjectUtils.toString(paymentOrder.getImprestMode()));
				if(paymentOrder.getYueMoney()!=null &&  paymentOrder.getYueMoney() > 0 ){
					treeMap.put("money", ObjectUtils.toString(paymentOrder.getMoney() + paymentOrder.getYueMoney()));
				}else{
					treeMap.put("money", ObjectUtils.toString(paymentOrder.getMoney()));
				}
				treeMap.put("paystate", PaymentOrder.PAYMENT_STATE_PAYED);
				
				if(!StringUtils.isBlank(paymentOrder.getPartnerOrderNo())){
					treeMap.put("partnerorderno", paymentOrder.getPartnerOrderNo());
				}
				String keyType = payemntMerchnt.getKeyType();
				String key = payemntMerchnt.getMerchantKey();
				if(StringUtils.isBlank(key)){
					logger.error("回调密钥为空,订单号:" + paymentOrder.getOrderNo()+",平台id:"+paymentOrder.getPlatformId());
					throw new IllegalArgumentException("平台Id["+paymentOrder.getPlatformId()+"]回调密钥为空");
				}
				
				//推送消息详情
				JSONObject callbackObj = new JSONObject();
				callbackObj.put("orderno", paymentOrder.getOrderNo());//收银台订单号
				callbackObj.put("aid", ObjectUtils.toString(paymentOrder.getAid()));//账号id
				callbackObj.put("platformid", ObjectUtils.toString(paymentOrder.getPlatformId()));//收银台的支付平台id
				callbackObj.put("imprestmode", ObjectUtils.toString(paymentOrder.getImprestMode()));//订单支付模式
				if(paymentOrder.getYueMoney()!=null &&  paymentOrder.getYueMoney() > 0 ){
					callbackObj.put("money", ObjectUtils.toString(paymentOrder.getMoney() + paymentOrder.getYueMoney()));
				}else{
					callbackObj.put("money", ObjectUtils.toString(paymentOrder.getMoney()));//订单支付金额
				}
				callbackObj.put("paystate", PaymentOrder.PAYMENT_STATE_PAYED);
				if(!StringUtils.isBlank(paymentOrder.getPartnerOrderNo())){
					callbackObj.put("partnerorderno", paymentOrder.getPartnerOrderNo());//业务方订单号
				}
				callbackObj.put("sign", ObjectUtils.toString(signStr(treeMap,key,keyType)));
				
				String backendUrl = paymentOrder.getPartnerBackendUrl();
				if(StringUtils.isBlank(backendUrl)){
					throw new IllegalArgumentException("["+ObjectUtils.toString(paymentOrder.getPlatformId())+"]回调地址为空，请检查配置文件[callback.properties]");
				}
				callbackObj.put("backendUrl", backendUrl);//回调业务方地址
				
				String timeout = "30000";//默认为30s
				if(StringUtils.isNotBlank(paymentOrder.getExtend())){
					JSONObject extend = JSONObject.parseObject(paymentOrder.getExtend());
					if(extend.containsKey("timeout") && StringUtils.isNotBlank(extend.getString("timeout"))){
						timeout = extend.getString("timeout");
					}
				}
				
				inParams.put(PaymentConstant.PAYMENT_ORDER, paymentOrder);
				inParams.put(PaymentConstant.MESSAGE_PUSH_TASK_OBJ, callbackObj.toString());
				inParams.put(PaymentConstant.MESSAGE_PUSH_TASK_TYPE, callbackTaskType);
				inParams.put(PaymentConstant.PAYMENT_CALL_TIMEOUT, timeout);//超时时间
				inParams.put(PaymentConstant.MERCHANT_ID, payemntMerchnt.getId());
				
				
				logger.info("创建收银台回调业务方推送详情:"+inParams.toString());
				
				Long queueResult = mQueueService.createQueueAndLog(inParams);
				
				logger.info("创建收银台回调业务方推送队列id:"+queueResult+",tasktype:"+callbackTaskType);
				if(null != queueResult && queueResult > 0){
					return "success";
				}else{
					//创建失败
					return "fail";
				}
			}else{
				logger.info("创建收银台回调业务方推送队列失败已存在该订单:{}的处理队列",paymentOrder.getId());
				return "fail";
			}
		} catch (Exception e) {
			logger.error("创建收银台回调业务方推送失败:",e);
		}
		return "fail";
	}
	
	/**
	public static void main(String[] args) throws ClientProtocolException, IOException {
		Map<String,String> nameValuePair = new HashMap<String,String>();
		
		nameValuePair.put("orderno", "20130829-141-028-0000010105");
		nameValuePair.put("aid", "1502527677");
		nameValuePair.put("platformid", "187");
		nameValuePair.put("imprestmode", "D");
		nameValuePair.put("money", "0.01");
		nameValuePair.put("paystate", "1");
		nameValuePair.put("sign", "2658F5F4AE76B28850F9F18068A03A78");
		
		PaymentOrderService pos = new PaymentOrderService();
		System.out.println(pos.httpPost("http://mobile.woniu.com/web/eshop/pay/return", nameValuePair));
	}
	**/
}
