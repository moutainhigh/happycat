package com.woniu.sncp.pay.core.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.persistence.Column;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.http.client.ClientProtocolException;
import org.bouncycastle.crypto.RuntimeCryptoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
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
import com.woniu.sncp.pay.core.service.dataroute.PayConfigToute;
import com.woniu.sncp.pay.core.service.m.MQueueService;
import com.woniu.sncp.pay.core.service.monitor.MonitorMessageService;
import com.woniu.sncp.pay.core.service.monitor.MonitorMessageTask;
import com.woniu.sncp.pay.core.service.payment.conf.PaymentProperties;
import com.woniu.sncp.pay.core.service.schedule.Schedule;
import com.woniu.sncp.pay.core.service.schedule.SyncTaskSchedule;
import com.woniu.sncp.pay.dao.BaseSessionDAO;
import com.woniu.sncp.pay.dao.PaymentOrderDao;
import com.woniu.sncp.pay.repository.pay.MessageQueue;
import com.woniu.sncp.pay.repository.pay.MessageQueueLog;
import com.woniu.sncp.pay.repository.pay.PassportAsyncTask;
import com.woniu.sncp.pay.repository.pay.PaymentMerchant;
import com.woniu.sncp.pay.repository.pay.PaymentOrderDiscountRecordRepository;
import com.woniu.sncp.pay.repository.pay.PaymentOrderRepository;
import com.woniu.sncp.pojo.payment.PaymentOrder;
import com.woniu.sncp.pojo.payment.PaymentOrderDiscountRecord;


@Service("paymentOrderService")
public class PaymentOrderService{
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource
	private BaseSessionDAO sessionDao;
 
 	@Resource 
	PaymentOrderDao paymentOrderDao;
	
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
	
	@Resource
	private PayConfigToute payConfigToute;
	
	/**
	 * 生成订单号并创建订单
	 * @param paymentOrder
	 * @param issuerId
	 * @throws DataAccessException
	 */
	@Transactional(propagation=Propagation.REQUIRED,value="txManager",rollbackFor=RuntimeException.class)
	public void createOrderAndGenOrderNo(PaymentOrder paymentOrder, long issuerId,String timeoutExpress,PaymentOrderDiscountRecord discountRecord)
		throws DataAccessException,RuntimeException{
//		long sequence = sessionDao.findForLong("select sn_imprest.pay_order_sq.nextval from dual");
//		final String orderSeqSql = "INSERT INTO SN_PAY.PAY_ORDER_SQ(N_ID,N_MERCHANT_ID,S_PAYPARTNER_OTHER_ORDER_NO,S_ORDER_NO) VALUES(NULL,"+paymentOrder.getMerchantId()+",'"+paymentOrder.getPaypartnerOtherOrderNo()+"','"+paymentOrder.getOrderNo()+"')";
		try {
			final String orderSeqSql = "INSERT INTO SN_PAY.PAY_ORDER_SQ(N_ID,N_MERCHANT_ID,S_PAYPARTNER_OTHER_ORDER_NO) VALUES(NULL,"+paymentOrder.getMerchantId()+",'"+paymentOrder.getPaypartnerOtherOrderNo()+"')";
			Long sequence = this.createSequence(orderSeqSql);
			
			if(null == sequence){
				throw new ValidationException("订单序列创建失败["+sequence+"],payPartnerOtherOrderNo:"+paymentOrder.getPaypartnerOtherOrderNo());
			}

			Date now = Calendar.getInstance().getTime();
			paymentOrder.setCreate(now); // 时间
			paymentOrder.setPayEnd(now);
			String today = DateFormatUtils.format(now, "yyyyMMdd");
			String orderNo = today + "-" + paymentOrder.getPayPlatformId() + "-"
					+ StringUtils.leftPad(String.valueOf(issuerId), 3, '0') + "-"
					+ StringUtils.leftPad(String.valueOf(sequence), 10, '0');
			
			if(paymentOrder.getPayPlatformId() == 4001 || paymentOrder.getPayPlatformId() == 4002 
					|| paymentOrder.getPayPlatformId() == 4003 || paymentOrder.getPayPlatformId() == 4011
					|| paymentOrder.getPayPlatformId() == 4012 || paymentOrder.getPayPlatformId() == 4013
					|| paymentOrder.getPayPlatformId() == 4014){
				//4001.兔兔币支付，4002.翡翠币web，4003.翡翠币wap
				//4011.PC兔兔币,4012.wap兔兔币,4013.android兔兔币,4014.ios兔兔币
				orderNo = today + "-" + paymentOrder.getMerchantId()+ '-' + paymentOrder.getPayPlatformId() + "-"
						+ StringUtils.leftPad(String.valueOf(issuerId), 3, '0') + "-"
						+ StringUtils.leftPad(String.valueOf(sequence), 20, '0');
				paymentOrder.setOrderNo(orderNo);
			}else if(paymentOrder.getPayPlatformId() == 1012){
				//中国银行信用卡分期支付平台id
				orderNo = today + StringUtils.leftPad(String.valueOf(sequence), 20, '0');
				paymentOrder.setOrderNo(ObjectUtils.toString(orderNo));
			}else{
				paymentOrder.setOrderNo(orderNo);
			}
			paymentOrder.setCreate(now); // 时间
			paymentOrder.setPayEnd(now);
			paymentOrder.setOrderId(sequence);
			
			if (StringUtils.isBlank(paymentOrder.getOtherOrderNo())){
				paymentOrder.setOtherOrderNo(orderNo);
			}
			
			Date __timeoutExpress = null;
			if(StringUtils.isNotBlank(timeoutExpress)){
				//设置最晚付款时间，yyyyMMddHHmmss
//				String _timeoutExpress = DateUtils.format(
//						org.apache.commons.lang.time.DateUtils.addMinutes(
//								paymentOrder.getCreate(), Integer.parseInt(timeoutExpress)), DateUtils.DATE_FORMAT_DATETIME_COMPACT);
				__timeoutExpress = DateUtils.parseDate(timeoutExpress,DateUtils.DATE_FORMAT_DATETIME_COMPACT);
				paymentOrder.setTimeoutExpress(__timeoutExpress);
			}else{
				// 默认最晚付款时间24小时
				String _timeoutExpress = DateUtils.format(
						org.apache.commons.lang.time.DateUtils.addMinutes(now, PaymentConstant.DEFAULT_TIMEOUTEXPRESS), DateUtils.DATE_FORMAT_DATETIME_COMPACT);
				__timeoutExpress = DateUtils.parseDate(_timeoutExpress,DateUtils.DATE_FORMAT_DATETIME_COMPACT);
				paymentOrder.setTimeoutExpress(__timeoutExpress);
			}
			
			StringBuffer insertOrderSql = new StringBuffer();
			insertOrderSql.setLength(0);
			insertOrderSql.append("insert into SN_PAY.PAY_ORDER");
			
			insertOrderSql.append(payConfigToute.getSuffixBySeq(sequence));//添加表后缀
			
			insertOrderSql.append(" (N_ORDER_ID,S_ORDER_NO,N_PAY_PLATFORM_ID,S_OTHER_ORDER_NO,N_CARDTYPE_ID,N_AID,N_AMOUNT,S_CURRENCY,N_MONEY,N_GAME_ID,N_GAREA_ID,N_IMPREST_PLOY_ID,N_GIFT_GAREA_ID,D_CREATE,N_IP,N_PAY_IP,S_PAY_STATE,D_PAY_END,S_STATE,S_MONEY_CURRENCY,S_IMPREST_MODE,S_PAYPARTNER_FRONT_CALL,S_PAYPARTNER_BACKEND_CALL,S_PAYPARTNER_OTHER_ORDER_NO,N_GSERVER_ID,S_INFO,N_VALUE_AMOUNT,N_MERCHANT_ID,S_YUE_CURRENCY,N_YUE_MONEY,S_YUE_PAY_STATE,S_MERCHANT_NO,S_MERCHANT_NAME,S_PRODUCTNAME,S_BODY,S_GOODS_DETAIL,S_TERMINAL_TYPE,D_TIMEOUT_EXPRESS,N_LOGIN_AID) ");
			insertOrderSql.append(" values(:orderId,:orderNo,:payPlatformId,:otherOrderNo,:cardTypeId,:aid,:amount,:currency,:money,:gameId,:gareaId,:imprestPloyId,:giftGareaId,:create,:ip,:payIp,:payState,:payEnd,:state,:moneyCurrency,:imprestMode,:paypartnerFrontCall,:paypartnerBackendCall,:paypartnerOtherOrderNo,:gserverId,:info,:valueAmount,:merchantId,:yueCurrency,:yueMoney,:yuePayState,:merchantNo,:merchantName,:productname,:body,:goodsDetail,:terminalType,:timeoutExpress,:loginAid);");
			SqlParameterSource paramSource = new BeanPropertySqlParameterSource(paymentOrder);
			int result = sessionDao.update(insertOrderSql.toString(), paramSource);
			if(discountRecord!=null) {
				discountRecord.setOrderNo(paymentOrder.getOrderNo());
				discountRecord.setCreateDate(now);
				discountRecord.setMerchantId(paymentOrder.getMerchantId());
				
 
				
				discountRecord.setPartnerOrderNo(paymentOrder.getPaypartnerOtherOrderNo());	
				StringBuilder sql=new StringBuilder("insert into SN_PAY.PAY_ORDER_DISCOUNT_RECORD(N_DISCOUNT_ID,N_MERCHANT_ID,N_PAYMENT_ID,S_ORDER_NO,S_PAYPARTNER_OTHER_ORDER_NO,N_MONEY,D_CREATE)");
				sql.append("values(:discountId,:merchantId,:paymentId,:orderNo,:partnerOrderNo,:money,:createDate)");
 				sessionDao.update(sql.toString() , new BeanPropertySqlParameterSource(discountRecord));
 				if (logger.isInfoEnabled())
					logger.info("订单：" + paymentOrder.getOrderNo()+"价格改变:"+discountRecord.getMoney());
			}
			if (logger.isInfoEnabled() && result > 0)
				logger.info("支付生成订单成功：" + paymentOrder.getOrderNo());
		} catch (ValidationException e) {
			logger.error(this.getClass().getSimpleName(),e);
//			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			throw new IllegalArgumentException("支付生成订单失败,{}",e);
		} catch (Exception e) {
			logger.error(this.getClass().getSimpleName(),e);
//			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			throw new IllegalArgumentException("支付生成订单异常,{}",e);
		}
	}
	 
	public void checkOrderIsProcessed(PaymentOrder paymentOrder)
			throws OrderIsSuccessException, ValidationException, OrderIsRefundException {
		
		if(paymentOrder.getPaypartnerBackendCall().equals(messagePushUrl)){
			//TODO 校验队列执行状态
			PassportAsyncTask dbtask = syncTaskSchedule.querySchedule(paymentOrder.getOrderId(), messagePushTaskType);
			if(null == dbtask){
				//TODO 创建消息队列，推送游戏
				Boolean cSyncResult = this.createSyncTask(paymentOrder,paymentOrder.getOtherOrderNo(),messagePushUrl,messagePushTaskType);
				if(!cSyncResult){
					//推送找不到或者创建失败
					throw new ValidationException("订单消息推送创建失败或已推送["+paymentOrder.getOrderId()+"],tasktype:"+messagePushTaskType);
				}else{
					// 1.已充值
					// 2.未充值 + 支付失败
					if (PaymentOrder.IMPREST_STATE_COMPLETED.equals(paymentOrder.getState())) {
						String msg = "订单已成功支付，勿重复处理:" + paymentOrder.getOrderNo();
						throw new OrderIsSuccessException(msg);
					}
					// 4.已退款
					if (PaymentConstant.PAYMENT_STATE_QUERY_ERR.equals(paymentOrder.getState())) {
						String msg = "订单已退款，勿重复处理:" + paymentOrder.getOrderNo();
						throw new OrderIsRefundException(msg);
					}
				}
			}
		}else{
			// 1.已充值
			// 2.未充值 + 支付失败
			if (PaymentOrder.IMPREST_STATE_COMPLETED.equals(paymentOrder.getState())) {
				String msg = "订单已成功支付，勿重复处理:" + paymentOrder.getOrderNo();
				throw new OrderIsSuccessException(msg);
			}
			// 4.已退款
			if (PaymentConstant.PAYMENT_STATE_QUERY_ERR.equals(paymentOrder.getState())) {
				String msg = "订单已退款，勿重复处理:" + paymentOrder.getOrderNo();
				throw new OrderIsRefundException(msg);
			}
		}
	}
	
	public boolean orderIsPayed(PaymentOrder paymentOrder){
		if (PaymentOrder.PAYMENT_STATE_PAYED.equals(paymentOrder.getPayState())
				&& PaymentOrder.IMPREST_STATE_COMPLETED.equals(paymentOrder.getState())) {
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

	@SuppressWarnings("unchecked")
	public PaymentOrder queryOrder(String orderNo) throws DataAccessException {
		//1.取出我方单号的seq，最后-
		String seq = orderNo.substring(orderNo.lastIndexOf("-")+1);
		Long id = Long.parseLong(seq);
		if(null!=id && id>0){
			StringBuffer selectOrdersql= new StringBuffer();
			selectOrdersql.setLength(0);
			selectOrdersql.append("select * from SN_PAY.PAY_ORDER");
			
			selectOrdersql.append(payConfigToute.getSuffixBySeq(id));//添加表后缀
			
			selectOrdersql.append(" where S_ORDER_NO = '"+orderNo+"';");
//			Map<String,Object> paramMap = new HashMap<String,Object>();
//			paramMap.put("orderNo", orderNo);
			List<PaymentOrder> result = (List<PaymentOrder>) paymentOrderDao.queryListEntity(selectOrdersql.toString(), null, PaymentOrder.class);
			if(result!=null){
				return result.isEmpty()?null:result.get(0);
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public PaymentOrder queryOrderByPartnerOrderNo(String pOrderNo,Long merchantId) throws DataAccessException {
		//1.确认业务单号的seq
		String selectOrderSeqsql = "select N_ID from SN_PAY.PAY_ORDER_SQ where N_MERCHANT_ID = "+merchantId+" and S_PAYPARTNER_OTHER_ORDER_NO = '"+pOrderNo+"';";
		List<Long> idList = sessionDao.queryForList(selectOrderSeqsql, null, Long.class);
		if(idList.size()>0 && null != idList.get(0)){
			Long id = idList.get(0);
			StringBuffer selectOrdersql= new StringBuffer();
			selectOrdersql.setLength(0);
			selectOrdersql.append("select * from SN_PAY.PAY_ORDER");
			
			selectOrdersql.append(payConfigToute.getSuffixBySeq(id));//添加表后缀
			
			selectOrdersql.append(" where S_PAYPARTNER_OTHER_ORDER_NO = '"+pOrderNo+"';");
//			selectOrdersql.append(" where S_PAYPARTNER_OTHER_ORDER_NO = :paypartnerOtherOrderNo ;");
//			
//			Map<String,Object> paramMap = new HashMap<String,Object>();
//			paramMap.put("paypartnerOtherOrderNo", pOrderNo);
			List<PaymentOrder> result = (List<PaymentOrder>) paymentOrderDao.queryListEntity(selectOrdersql.toString(), null, PaymentOrder.class);
			if(result!=null){
				return result.isEmpty()?null:result.get(0);
			}
		}
		
		return null;
	}
	
//	@SuppressWarnings("unchecked")
//	public PaymentOrder queryOrderByPartnerOrderNo(String pOrderNo) throws DataAccessException {
//		//1.确认业务单号的seq
//		String selectOrderSeqsql = "select N_ID from SN_PAY.PAY_ORDER_SQ where S_PAYPARTNER_OTHER_ORDER_NO='"+pOrderNo+"'";
//		List<Long> idList = sessionDao.queryForList(selectOrderSeqsql, null, Long.class);
//		if(idList.size()>0 && null != idList.get(0)){
//			Long id = idList.get(0);
//			StringBuffer selectOrdersql= new StringBuffer();
//			selectOrdersql.setLength(0);
//			selectOrdersql.append("select * from SN_PAY.PAY_ORDER");
//			//判断数据在哪个表
//			if(id<=60 && id>40){
//				selectOrdersql.append("_T1 ");//添加表后缀
//			}
//			if(id>60){
//				selectOrdersql.append("_T2 ");//添加表后缀
//			}
//			selectOrdersql.append("where S_PAYPARTNER_OTHER_ORDER_NO = :paypartnerOtherOrderNo");
//			
//			Map<String,Object> paramMap = new HashMap<String,Object>();
//			paramMap.put("paypartnerOtherOrderNo", pOrderNo);
//			List<PaymentOrder> result = (List<PaymentOrder>) paymentOrderDao.queryListEntity(selectOrdersql.toString(), paramMap, PaymentOrder.class);
//			return result.isEmpty()?null:result.get(0);
//		}
//		
//		return null;
//	}
	
//	public PaymentOrder queyrOrderByOppositeOrderNo(String oppositeOrderNo)
//			throws DataAccessException {
//		return paymentOrderRepository.findByOtherOrderNo(oppositeOrderNo);
//	}

	public void updateOrder(PaymentOrder paymentOrder, String payedState,
			String imprestState) throws DataAccessException,
			IllegalArgumentException {
		logger.info("更改支付订单" + paymentOrder.getOrderNo() + "为：payedState:" + payedState + ",imprestState:"
				+ imprestState);
		if (StringUtils.isBlank(payedState) && StringUtils.isBlank(imprestState))
			throw new IllegalArgumentException("更新订单状态参数错误，payedState和imprestState不能都为空");

		if (StringUtils.isNotBlank(payedState))
			paymentOrder.setPayState(payedState);

		if (StringUtils.isNotBlank(imprestState))
			paymentOrder.setState(imprestState);

		StringBuffer updateOrderSql = new StringBuffer();
		updateOrderSql.setLength(0);
		updateOrderSql.append("update SN_PAY.PAY_ORDER");
		
		updateOrderSql.append(payConfigToute.getSuffixBySeq(paymentOrder.getOrderId()));//添加表后缀
		
		updateOrderSql.append(" set S_ORDER_NO = :orderNo,S_OTHER_ORDER_NO = :otherOrderNo,N_PAY_IP = :payIp,D_PAY_END = :payEnd,N_PAY_PLATFORM_ID = :payPlatformId,N_AID = :aid,N_GAME_ID = :gameId,S_CURRENCY = :currency,S_MERCHANT_NO = :merchantNo,S_MERCHANT_NAME = :merchantName,S_PAY_STATE = :payState,S_STATE = :state,N_LOGIN_AID = :loginAid,S_MONEY_CURRENCY = :moneyCurrency,N_MONEY = :money");
		updateOrderSql.append(" where N_ORDER_ID = :orderId;");
		SqlParameterSource paramSource = new BeanPropertySqlParameterSource(paymentOrder);
		int result = sessionDao.update(updateOrderSql.toString(), paramSource);
		
		if (logger.isInfoEnabled() && result > 0)
			logger.info("更改支付订单成功：" + paymentOrder.getOrderNo());
	}
	public void updateOrderMoney(PaymentOrder paymentOrder,float money) throws DataAccessException,
			IllegalArgumentException {
		logger.info("更改支付订单" + paymentOrder.getOrderNo() + "为：money:" + money);
		paymentOrder.setMoney(money);
		StringBuffer updateOrderSql = new StringBuffer();
		updateOrderSql.setLength(0);
		
		updateOrderSql.append("update SN_PAY.PAY_ORDER");
		
		updateOrderSql.append(payConfigToute.getSuffixBySeq(paymentOrder.getOrderId()));//添加表后缀
		
		updateOrderSql.append(" set S_ORDER_NO = :orderNo,S_OTHER_ORDER_NO = :otherOrderNo,N_PAY_IP = :payIp,D_PAY_END = :payEnd,N_PAY_PLATFORM_ID = :payPlatformId,N_AID = :aid,N_GAME_ID = :gameId,S_CURRENCY = :currency,S_MERCHANT_NO = :merchantNo,S_MERCHANT_NAME = :merchantName,S_PAY_STATE = :payState,S_STATE = :state,N_LOGIN_AID = :loginAid,S_MONEY_CURRENCY = :moneyCurrency,N_MONEY = :money");
		updateOrderSql.append(" where N_ORDER_ID = :orderId;");
		SqlParameterSource paramSource = new BeanPropertySqlParameterSource(paymentOrder);
		int result = sessionDao.update(updateOrderSql.toString(), paramSource);
		
		if (logger.isInfoEnabled() && result > 0)
			logger.info("更改支付订单成功：" + paymentOrder.getOrderNo());
	}
	
	
	public String callback(PaymentOrder paymentOrder,PaymentMerchant payemntMerchnt){
		
		Map<String,String> nameValuePair = new HashMap<String,String>();
		TreeMap<String,String> treeMap = new TreeMap<String,String>();
		
		try {
			treeMap.put("orderno", paymentOrder.getOrderNo());
			treeMap.put("aid", ObjectUtils.toString(paymentOrder.getAid()));
			treeMap.put("platformid", ObjectUtils.toString(paymentOrder.getPayPlatformId()));
			treeMap.put("imprestmode", ObjectUtils.toString(paymentOrder.getImprestMode()));
			if(paymentOrder.getYueMoney()!=null &&  paymentOrder.getYueMoney() > 0 ){
				treeMap.put("money", ObjectUtils.toString(paymentOrder.getMoney() + paymentOrder.getYueMoney()));
			}else{
				treeMap.put("money", ObjectUtils.toString(paymentOrder.getMoney()));
			}
			treeMap.put("paystate", PaymentOrder.PAYMENT_STATE_PAYED);
			
			if(!StringUtils.isBlank(paymentOrder.getPaypartnerOtherOrderNo())){
				treeMap.put("partnerorderno", paymentOrder.getPaypartnerOtherOrderNo());
				nameValuePair.put("partnerorderno", paymentOrder.getPaypartnerOtherOrderNo());
			}
			
			String keyType = payemntMerchnt.getKeyType();
			String key = payemntMerchnt.getMerchantKey();
			if(StringUtils.isBlank(key)){
				logger.error("回调密钥为空,订单号:" + paymentOrder.getOrderNo()+",平台id:"+paymentOrder.getPayPlatformId());
				throw new IllegalArgumentException("平台Id["+paymentOrder.getPayPlatformId()+"]回调密钥为空");
			}
			
			nameValuePair.put("orderno", paymentOrder.getOrderNo());
			nameValuePair.put("aid", ObjectUtils.toString(paymentOrder.getAid()));
			nameValuePair.put("platformid", ObjectUtils.toString(paymentOrder.getPayPlatformId()));
			nameValuePair.put("imprestmode", ObjectUtils.toString(paymentOrder.getImprestMode()));
			if(paymentOrder.getYueMoney()!=null &&  paymentOrder.getYueMoney() > 0){
				nameValuePair.put("money", ObjectUtils.toString(paymentOrder.getMoney()+paymentOrder.getYueMoney()));
			}else{
				nameValuePair.put("money", ObjectUtils.toString(paymentOrder.getMoney()));
			}
			nameValuePair.put("paystate", PaymentOrder.PAYMENT_STATE_PAYED);
			nameValuePair.put("sign", ObjectUtils.toString(signStr(treeMap,key,keyType)));
			
			String backendUrl = paymentOrder.getPaypartnerBackendCall();
			if(StringUtils.isBlank(backendUrl)){
				throw new IllegalArgumentException("["+ObjectUtils.toString(paymentOrder.getPayPlatformId())+"]回调地址为空，请检查配置文件[callback.properties]");
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
		
		String alertMsg = "所在应用:["+serverName+"],服务器:["+localIp +":"+ ProxoolUtil.getTomcatPort() + "@" + ProxoolUtil.getPid() +"],\n支付订单号：" + paymentOrder.getOrderNo() + ",回调商户地址：" + paymentOrder.getPaypartnerBackendCall()+ "失败";
		logger.info(alertMsg);
		threadPool.executeTask(new MonitorMessageTask(alertMsg));
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
			treeMap.put("platformid", ObjectUtils.toString(paymentOrder.getPayPlatformId()));
			treeMap.put("imprestmode", ObjectUtils.toString(paymentOrder.getImprestMode()));
			if(paymentOrder.getYueMoney()!=null &&  paymentOrder.getYueMoney() > 0 ){
				treeMap.put("money", ObjectUtils.toString(paymentOrder.getMoney() + paymentOrder.getYueMoney()));
			}else{
				treeMap.put("money", ObjectUtils.toString(paymentOrder.getMoney()));
			}
			treeMap.put("paystate", PaymentOrder.PAYMENT_STATE_PAYED);
			
			if(!StringUtils.isBlank(paymentOrder.getPaypartnerOtherOrderNo())){
				treeMap.put("partnerorderno", paymentOrder.getPaypartnerOtherOrderNo());
				nameValuePair.put("partnerorderno", paymentOrder.getPaypartnerOtherOrderNo());
			}
			
			String keyType = payemntMerchnt.getKeyType();//业务申请渠道的加密方式
			
			String key = platform.getBackendKey();//获取商户的回调密钥
			if(StringUtils.isBlank(key)){
				logger.error("回调密钥为空,订单号:" + paymentOrder.getOrderNo()+",平台id:"+paymentOrder.getPayPlatformId());
				throw new IllegalArgumentException("平台Id["+paymentOrder.getPayPlatformId()+"]回调密钥为空");
			}
			
			nameValuePair.put("orderno", paymentOrder.getOrderNo());
			nameValuePair.put("aid", ObjectUtils.toString(paymentOrder.getAid()));
			nameValuePair.put("platformid", ObjectUtils.toString(paymentOrder.getPayPlatformId()));
			nameValuePair.put("imprestmode", ObjectUtils.toString(paymentOrder.getImprestMode()));
			if(paymentOrder.getYueMoney()!=null &&  paymentOrder.getYueMoney() > 0){
				nameValuePair.put("money", ObjectUtils.toString(paymentOrder.getMoney()+paymentOrder.getYueMoney()));
			}else{
				nameValuePair.put("money", ObjectUtils.toString(paymentOrder.getMoney()));
			}
			nameValuePair.put("paystate", PaymentOrder.PAYMENT_STATE_PAYED);
			nameValuePair.put("sign", ObjectUtils.toString(signStr(treeMap,key,keyType)));
			
			String backendUrl = paymentOrder.getPaypartnerBackendCall();
			if(StringUtils.isBlank(backendUrl)){
				throw new IllegalArgumentException("["+ObjectUtils.toString(paymentOrder.getPayPlatformId())+"]回调地址为空，请检查配置文件[callback.properties]");
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
		
		String alertMsg = "所在应用:["+serverName+"],服务器:["+localIp +":"+ ProxoolUtil.getTomcatPort() + "@" + ProxoolUtil.getPid() +"],\n支付订单号：" + paymentOrder.getOrderNo() + ",回调商户地址：" + paymentOrder.getPaypartnerBackendCall()+ "失败";
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
			PassportAsyncTask dbTask = syncTaskSchedule.querySchedule(paymentOrder.getOrderId(),messagePushTaskType);//tasktype "139" 手游PC版本支付消息
			if(null == dbTask){
				inParams = new HashMap<String, Object>();
				JSONObject extend = new JSONObject();
				if(StringUtils.isNotBlank(paymentOrder.getInfo())){
					extend = JSONObject.parseObject(paymentOrder.getInfo());
				}else{
					logger.info("订单扩展未配置,orderNo:"+paymentOrder.getOrderNo()+",ext:"+paymentOrder.getInfo());
					return false;
				}
				
				//推送消息详情
				JSONObject messagePushObj = new JSONObject();
				messagePushObj.put(PaymentConstant.PARTNER_ORDERNO, paymentOrder.getPaypartnerOtherOrderNo());//业务订单号
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
	public String createCallbackSyncTask(PaymentOrder paymentOrder,PaymentMerchant payemntMerchnt,String oppositeCurrency,String oppositeOrderNo,String callbackTaskType) {
		try {
			if (StringUtils.isBlank(callbackTaskType))
				throw new IllegalArgumentException("创建收银台回调业务方队列任务参数错误，callbackTaskType不能为空");
			
			// TODO 验证推送队列是否存在?
			Map<String, Object> inParams = null;
			//sn_queue.que_message_queue
			MessageQueue dbTask = mQueueService.queryMessageQueue(paymentOrder.getOrderId(),Long.parseLong(callbackTaskType));//tasktype "174" 收银台回调业务方队列任务类型
			//sn_queue.que_message_log
			MessageQueueLog queueLog = mQueueService.queryQueueLog(paymentOrder.getOrderId(),Long.parseLong(callbackTaskType));//tasktype "174" 收银台回调业务方队列任务类型
			
			if(null == dbTask  && null == queueLog){
				inParams = new HashMap<String, Object>();
				TreeMap<String,String> treeMap = new TreeMap<String,String>();
				treeMap.put("orderno", paymentOrder.getOrderNo());
				treeMap.put("aid", ObjectUtils.toString(paymentOrder.getAid()));
				treeMap.put("platformid", ObjectUtils.toString(paymentOrder.getPayPlatformId()));
				treeMap.put("imprestmode", ObjectUtils.toString(paymentOrder.getImprestMode()));
				if(paymentOrder.getYueMoney()!=null &&  paymentOrder.getYueMoney() > 0 ){
					treeMap.put("money", ObjectUtils.toString(paymentOrder.getMoney() + paymentOrder.getYueMoney()));
				}else{
					treeMap.put("money", ObjectUtils.toString(paymentOrder.getMoney()));
				}
				if(StringUtils.isNotBlank(oppositeCurrency)){
					treeMap.put("currency", oppositeCurrency);
				}
				treeMap.put("paystate", PaymentOrder.PAYMENT_STATE_PAYED);
				treeMap.put("merchantid", ObjectUtils.toString(paymentOrder.getMerchantId()));
				
				if(!StringUtils.isBlank(paymentOrder.getPaypartnerOtherOrderNo())){
					treeMap.put("partnerorderno", paymentOrder.getPaypartnerOtherOrderNo());
				}
				String keyType = payemntMerchnt.getKeyType();
				String key = payemntMerchnt.getMerchantKey();
				if(StringUtils.isBlank(key)){
					logger.error("回调密钥为空,订单号:" + paymentOrder.getOrderNo()+",平台id:"+paymentOrder.getPayPlatformId());
					throw new IllegalArgumentException("平台Id["+paymentOrder.getPayPlatformId()+"]回调密钥为空");
				}
				
				//推送消息详情
				JSONObject callbackObj = new JSONObject();
				callbackObj.put("orderno", paymentOrder.getOrderNo());//收银台订单号
				callbackObj.put("aid", ObjectUtils.toString(paymentOrder.getAid()));//账号id
				callbackObj.put("platformid", ObjectUtils.toString(paymentOrder.getPayPlatformId()));//收银台的支付平台id
				callbackObj.put("imprestmode", ObjectUtils.toString(paymentOrder.getImprestMode()));//订单支付模式
				if(paymentOrder.getYueMoney()!=null &&  paymentOrder.getYueMoney() > 0 ){
					callbackObj.put("money", ObjectUtils.toString(paymentOrder.getMoney() + paymentOrder.getYueMoney()));
				}else{
					callbackObj.put("money", ObjectUtils.toString(paymentOrder.getMoney()));//订单支付金额
				}
				if(StringUtils.isNotBlank(oppositeCurrency)){
					callbackObj.put("currency", oppositeCurrency);
				}
				callbackObj.put("paystate", PaymentOrder.PAYMENT_STATE_PAYED);
				callbackObj.put("merchantid", ObjectUtils.toString(paymentOrder.getMerchantId()));
				if(!StringUtils.isBlank(paymentOrder.getPaypartnerOtherOrderNo())){
					callbackObj.put("partnerorderno", paymentOrder.getPaypartnerOtherOrderNo());//业务方订单号
				}
				callbackObj.put("sign", ObjectUtils.toString(signStr(treeMap,key,keyType)));
				
				String backendUrl = paymentOrder.getPaypartnerBackendCall();
				if(StringUtils.isBlank(backendUrl)){
					throw new IllegalArgumentException("["+ObjectUtils.toString(paymentOrder.getPayPlatformId())+"]回调地址为空，请检查配置文件[callback.properties]");
				}
				callbackObj.put("backendUrl", backendUrl);//回调业务方地址
				
				String timeout = "30000";//默认为30s
				if(StringUtils.isNotBlank(paymentOrder.getInfo())){
					JSONObject extend = JSONObject.parseObject(paymentOrder.getInfo());
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
				logger.info("创建收银台回调业务方推送队列失败已存在该订单:{}的处理队列",paymentOrder.getOrderId());
				return "fail";
			}
		} catch (Exception e) {
			logger.error("创建收银台回调业务方推送失败:",e);
		}
		return "fail";
	}
	
	
	/**
	 * 通过sql返回当前自增序列值
	 * @param getSeqSql
	 * @return
	 */
	public Long createSequence(final String getSeqSql){
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
}
