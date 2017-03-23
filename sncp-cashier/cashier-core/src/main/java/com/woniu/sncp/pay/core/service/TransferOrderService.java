package com.woniu.sncp.pay.core.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.snail.ocp.tools.IpUtils;
import com.woniu.pay.pojo.PaymentMerchant;
import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.pay.common.utils.encrypt.EncryptFactory;
import com.woniu.sncp.pay.common.utils.http.HttpUtils;
import com.woniu.sncp.pay.core.transfer.constants.TransferConstants;
import com.woniu.sncp.pay.core.transfer.model.TransferModel;
import com.woniu.sncp.pojo.payment.TransferOrder;

@Service("transferOrderService")
public class TransferOrderService {
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
//	@Resource
//	private BaseSessionDAO sessionDao;
//	@Resource
//	private BaseHibernateDAO<TransferOrder, Long> transferOrderDao;
	
	public long getSequence(){
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
//		long sequence = sessionDao.findForLong("select sn_imprest.PAY_TRANSFER_ORDER_SQ.nextval from dual");
//		return sequence;
		return 0;
	}
	
	public void createOrder(TransferOrder transferOrder)
			throws DataAccessException {

//		Date now = Calendar.getInstance().getTime();
//		transferOrder.setCreateDate(now); // 时间
//		transferOrder.setId(getSequence());
//		
//		String today = DateFormatUtils.format(now, "yyyyMMdd");
//		String orderNo = today + "-" 
//				+ StringUtils.leftPad(String.valueOf(transferOrder.getMerchantId()), 5, '0') + "-"
//				+ transferOrder.getPlatformId() + "-"
//				+ StringUtils.leftPad(String.valueOf(transferOrder.getId()), 10, '0');
//		
//		transferOrder.setOrderNo(orderNo);
//		
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
//		transferOrderDao.save(transferOrder);

		if (logger.isInfoEnabled())
			logger.info("支付生成订单成功：" + transferOrder.getOrderNo());
	}
	
	public boolean isNotified(TransferOrder transferOrder){
		boolean isNotify = false;
		if (TransferOrder.TRANSFER_STATE_NOTIFIED.equals(transferOrder.getNotifyState())) {
			logger.info("订单已成功通知，订单号:" + transferOrder.getOrderNo());
			isNotify = true; 
		}
		return isNotify;
	}
	
	public boolean isTransfer(TransferOrder transferOrder){
		boolean isTransfer = false;
		if (TransferOrder.TRANSFER_STATE_COMPLETED.equals(transferOrder.getState())) {
			logger.info("订单已转账成功，订单号:" + transferOrder.getOrderNo());
			isTransfer = true; 
		}
		
		return isTransfer;
	}
	
	public boolean isTransferFailed(TransferOrder transferOrder){
		boolean isFailed = false;
		if (TransferOrder.TRANSFER_STATE_FAILED.equals(transferOrder.getState())) {
			logger.info("订单已转账失败，订单号:" + transferOrder.getOrderNo());
			isFailed = true; 
		}
		
		return isFailed;
	}
	
	/**
	 * 实际金额比较 不需要乘100
	 * @param transferOrder
	 * @param paymentMoney
	 * @return
	 */
	public boolean checkOrderMoney(TransferOrder transferOrder, BigDecimal paymentMoney) {
		// 订单金额校验
		if (transferOrder.getMoney().equals(paymentMoney)) {
			logger.error("订单金额不匹配:我方:" + transferOrder.getMoney() + ",对方:" + paymentMoney);
			return false;
		}
		return true;
	}
	
	public TransferOrder queryOrder(String orderNo) throws DataAccessException {
		// 切换中心库
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
//		return transferOrderDao.findByProperty("orderNo", orderNo);
		return null;
	}
	
	public TransferOrder queryOrderByReceiveOrderNo(String receiveOrderNo) throws DataAccessException {
		// 切换中心库
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
//		return transferOrderDao.findByProperty("receiveOrderNo", receiveOrderNo);
		return null;
	}
	
	public void updateOrder(TransferOrder transferOrder, String transferState,
			String notifyState,String message) throws DataAccessException,
			IllegalArgumentException {
		logger.info("更改转账订单" + transferOrder.getOrderNo() + "为：transferState:" + transferState + ",notifyState:"
				+ notifyState);
//		if (StringUtils.isBlank(transferState) && StringUtils.isBlank(notifyState))
//			throw new IllegalArgumentException("更新订单状态参数错误，payedState和imprestState不能都为空");
//
//		if (StringUtils.isNotBlank(transferState))
//			transferOrder.setState(transferState);
//
//		if (StringUtils.isNotBlank(notifyState))
//			transferOrder.setNotifyState(notifyState);
//		
//		if (StringUtils.isNotBlank(message))
//			transferOrder.setMessage(message);
//
//		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
//		transferOrderDao.update(transferOrder);
	}
	
	public TransferOrder convert(TransferModel transferModel,Platform platform){
		TransferOrder order = new TransferOrder();
		order.setMerchantId(transferModel.getMerchantId());
		order.setPlatformId(transferModel.getPlatformId());
		
		order.setPaymentAccount(platform.getMerchantNo());
		order.setPaymentAccountName(platform.getManageUser());
		
		order.setMoney(new BigDecimal(transferModel.getMoney()));
		
		order.setReceiveAccount(transferModel.getAccount());
		order.setReceiveAccountInfo(transferModel.getAccountInfo());
		order.setReceiveBackendUrl(transferModel.getBackendUrl());
		order.setReason(transferModel.getReason());
		
		order.setClientIp(IpUtils.ipToLong(transferModel.getClientIp()));
		order.setState(TransferOrder.TRANSFER_STATE_NOT_COMPLETED);
		order.setNotifyState(TransferOrder.TRANSFER_STATE_NOT_NOTIFIED);
		
		return order;
	}
	
	public String callback(TransferOrder transferOrder,PaymentMerchant payemntMerchnt){
		
		Map<String,String> nameValuePair = new HashMap<String,String>();
		TreeMap<String,String> treeMap = new TreeMap<String,String>();
		
		try {
			treeMap.put("orderno", transferOrder.getOrderNo());//转账订单号
			treeMap.put("platformid", ObjectUtils.toString(transferOrder.getPlatformId()));//转账平台ID
			treeMap.put("partnerorderno", transferOrder.getReceiveOrderNo());//转账流水号
			treeMap.put("money", ObjectUtils.toString(transferOrder.getMoney()));//转账金额
			treeMap.put("state", transferOrder.getState());//转账状态
			treeMap.put("receiveacct", transferOrder.getReceiveAccount());//转账帐号
			
			String keyType = payemntMerchnt.getKeyType();
			String key = payemntMerchnt.getKey();
			if(StringUtils.isBlank(key)){
				logger.error("回调密钥为空,订单号:" + transferOrder.getOrderNo()+",平台id:"+transferOrder.getPlatformId());
				throw new IllegalArgumentException("平台Id["+transferOrder.getPlatformId()+"]回调密钥为空");
			}
			
			nameValuePair.put("orderno", transferOrder.getOrderNo());
			nameValuePair.put("platformid", ObjectUtils.toString(transferOrder.getPlatformId()));
			nameValuePair.put("partnerorderno", transferOrder.getReceiveOrderNo());
			nameValuePair.put("money", ObjectUtils.toString(transferOrder.getMoney()));
			nameValuePair.put("state", transferOrder.getState());
			nameValuePair.put("receiveacct", transferOrder.getReceiveAccount());
			nameValuePair.put("sign", ObjectUtils.toString(signStr(treeMap,key,keyType)));
			
			String backendUrl = transferOrder.getReceiveBackendUrl();
			if(StringUtils.isBlank(backendUrl)){
				throw new IllegalArgumentException("["+ObjectUtils.toString(transferOrder.getPlatformId())+"]回调地址为空，请检查配置文件[callback.properties]");
			}
			logger.info("订单号:" + transferOrder.getOrderNo() + " 回调地址:"+backendUrl + " 参数:" + nameValuePair);
			String response = HttpUtils.post(backendUrl, nameValuePair);
			logger.info("该回调地址["+backendUrl+"],订单号："+transferOrder.getOrderNo()+",返回值:"+response);
			return response;
		} catch (ClientProtocolException e) {
			logger.error("回调商户出错，"+e.getMessage(),e);
		} catch (IOException e) {
			logger.error("回调商户出错，"+e.getMessage(),e);
		} catch (Exception e){
			logger.error("回调商户出错，"+e.getMessage(),e);
		}
		
		return TransferConstants.CALLBACK_FAILED;
	}
	
	public boolean updateNotifyState(TransferOrder transferOrder,String callbackResponse){
		boolean result = false;
		//更新为已回调
		if( TransferConstants.CALLBACK_SUCCESS.equals(callbackResponse)){
			transferOrder.setReceiveDate(new Date());
			transferOrder.setNotifyState(TransferOrder.TRANSFER_STATE_NOTIFIED);
		} else {
			if(!this.isNotified(transferOrder)){
				transferOrder.setNotifyState(TransferOrder.TRANSFER_STATE_NOTIFY_FAILED);
			} 
		}
		this.updateOrder(transferOrder, null, null,null);
		result = true;
		
		return result;
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
}
