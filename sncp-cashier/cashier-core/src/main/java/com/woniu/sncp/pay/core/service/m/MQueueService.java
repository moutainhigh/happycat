package com.woniu.sncp.pay.core.service.m;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.common.utils.ExchargeConstant;
import com.woniu.sncp.pay.dao.QueueBaseSessionDAO;
import com.woniu.sncp.pay.repository.pay.MessageQueue;
import com.woniu.sncp.pay.repository.pay.MessageQueueLog;
import com.woniu.sncp.pojo.payment.PaymentOrder;

/**
 * 
 * <p>descrption: 队列+队列日志服务</p>
 * 
 * @author fuzl
 * @date   2016年12月13日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
@Service("mQueueService")
public class MQueueService{
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource
	private QueueBaseSessionDAO queueSessionDao;
	
	/**
	 * 查询队列任务
	 * @param relationId
	 * @param taskType
	 * @return
	 */
	public MessageQueue queryMessageQueue(Long relationId, Long taskType) {
		MessageQueue queue = null;
		Integer result = 0;
		try {
			String sql = "select count(0) from SN_PAY.QUE_MESSAGE_QUEUE where N_RELATION_ID=? and N_TYPE=?";
			Object[] values = new Object[]{relationId,taskType};
			result = queueSessionDao.getJdbcTemplate().queryForObject(sql,values,Integer.class);
			
			if(result>0){
				String _sql = "select N_ID id,N_TYPE taskType,N_SUBIDC_ID subdbId,N_RELATION_ID relationId,D_CREATE createDate,S_BUSINESS_DATA businessData,N_PIECE_ID pieceId,S_REMARK remark,N_MERCHANT_ID merchantId from SN_PAY.QUE_MESSAGE_QUEUE where N_RELATION_ID=? and N_TYPE=?";  
				queue = queueSessionDao.getJdbcTemplate().queryForObject(_sql,values, new BeanPropertyRowMapper<MessageQueue>(MessageQueue.class));
			}
		} catch (Exception e) {
			logger.error(this.getClass().getSimpleName(), e);
		}
		return queue;
	}
	
	/**
	 * 创建队列
	 * @param params
	 * @return
	 */
	public Long createQueue(Map<String, Object> params) {
		PaymentOrder paymentOrder = (PaymentOrder) params.get(ExchargeConstant.PAYMENT_ORDER);
		String taskObj = ObjectUtils.toString(params.get(ExchargeConstant.RECHARGE_TASK_OBJ));
		String taskType = ObjectUtils.toString(params.get(ExchargeConstant.RECHARGE_TASK_TYPE));
		String timeout = ObjectUtils.toString(params.get(PaymentConstant.PAYMENT_CALL_TIMEOUT));
		String merchantId = ObjectUtils.toString(params.get(PaymentConstant.MERCHANT_ID));
		
		final String queSeqSql = "INSERT INTO SN_PAY.QUE_MSG_QUEUE_SQ(N_ID) VALUES(NULL)";
		Long sequence = getSequence(queSeqSql);
		Date now = Calendar.getInstance().getTime();
		
		MessageQueue pQuetask = new MessageQueue();
		pQuetask.setId(sequence);
		pQuetask.setBusinessData(taskObj);
		pQuetask.setCreateDate(now);
		pQuetask.setPieceId((paymentOrder.getId()%10));// mod(n_relation_id,10)
		pQuetask.setRelationId(paymentOrder.getId());
		pQuetask.setSubdbId(Long.parseLong(timeout));//  设置超时时间,毫秒
		pQuetask.setTaskType(Long.parseLong(taskType));
		pQuetask.setMerchantId(Long.parseLong(merchantId));
		
		String saveQueue = "INSERT INTO SN_PAY.QUE_MESSAGE_QUEUE(N_ID,N_TYPE,N_SUBIDC_ID,N_RELATION_ID,D_CREATE,S_BUSINESS_DATA,N_PIECE_ID,S_REMARK,N_MERCHANT_ID) values(?,?,?,?,?,?,?,?,?)";
		return Long.valueOf(queueSessionDao.getJdbcTemplate().update(saveQueue,pQuetask.getId(),pQuetask.getTaskType(),
				pQuetask.getSubdbId(),pQuetask.getRelationId(),pQuetask.getCreateDate(),pQuetask.getBusinessData(),pQuetask.getPieceId(),pQuetask.getRemark(),pQuetask.getMerchantId()));
	}
	
	
	/**
	 * 查询队列日志
	 * @param relatedId
	 * @param taskType
	 * @return
	 */
	public MessageQueueLog queryQueueLog(Long relatedId, Long taskType) {
		MessageQueueLog queueLog = null;
		Integer result = 0;
		try {
			String sql = "select count(0) from SN_PAY.QUE_MESSAGE_LOG where N_RELATION_ID=? and N_TYPE=?";
			Object[] values = new Object[]{relatedId,taskType};
			result = queueSessionDao.getJdbcTemplate().queryForObject(sql,values,Integer.class);
			
			if(result>0){
				String _sql = "select N_ID id,N_AID accountId,N_TYPE type,N_RELATION_ID relatedId,S_BUSINESS_DATA taskObj,D_CREATE createDate,N_IP clientIP,N_MERCHANT_ID merchantId from SN_PAY.QUE_MESSAGE_LOG where N_RELATION_ID=? and N_TYPE=?";  
				queueLog = queueSessionDao.getJdbcTemplate().queryForObject(_sql,values, new BeanPropertyRowMapper<MessageQueueLog>(MessageQueueLog.class));
			}
		} catch (Exception e) {
			logger.error(this.getClass().getSimpleName(), e);
		}
		return queueLog;
	}

	/**
	 * 保存队列日志
	 * @param params
	 * @return
	 */
	public Long createQueueLog(Map<String, Object> params) {
		PaymentOrder paymentOrder = (PaymentOrder) params.get(ExchargeConstant.PAYMENT_ORDER);
		String taskObj = ObjectUtils.toString(params.get(ExchargeConstant.RECHARGE_TASK_OBJ));
		String taskType = ObjectUtils.toString(params.get(ExchargeConstant.RECHARGE_TASK_TYPE));
		String merchantId = ObjectUtils.toString(params.get(PaymentConstant.MERCHANT_ID));
		
		final String queSeqSql = "INSERT INTO SN_PAY.QUE_MSG_QUEUE_LOG_SQ(N_ID) VALUES(NULL)";
		Long sequence = getSequence(queSeqSql);
		Date now = Calendar.getInstance().getTime();
		
		MessageQueueLog queueLog = new MessageQueueLog();
		queueLog.setId(sequence);
		queueLog.setAccountId(paymentOrder.getAid());
		queueLog.setClientIP(paymentOrder.getClientIp());
		queueLog.setCreateDate(now);
		queueLog.setRelatedId(paymentOrder.getId());
		queueLog.setTaskObj(taskObj);
		queueLog.setType(Long.parseLong(taskType));
		queueLog.setMerchantId(Long.parseLong(merchantId));
		String saveQueueLog = "INSERT INTO SN_PAY.QUE_MESSAGE_LOG(N_ID,N_AID,N_TYPE,N_RELATION_ID,S_BUSINESS_DATA,D_CREATE,N_IP,N_MERCHANT_ID) values(?,?,?,?,?,?,?,?)";
		return Long.valueOf(queueSessionDao.getJdbcTemplate().update(saveQueueLog,queueLog.getId(),queueLog.getAccountId(),
				queueLog.getType(),queueLog.getRelatedId(),queueLog.getTaskObj(),queueLog.getCreateDate(),queueLog.getClientIP(),queueLog.getMerchantId()));
	}
	
	/**
	 * 通过sql返回当前自增序列值
	 * @param getSeqSql
	 * @return
	 */
	public Long getSequence(final String getSeqSql){
		KeyHolder keyHolder = new GeneratedKeyHolder();
		queueSessionDao.getJdbcTemplate().update(new PreparedStatementCreator() {  
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
	 * 保存队列任务和队列任务日志
	 * @param params
	 * @return
	 */
	@Transactional(propagation=Propagation.REQUIRED,value="transactionManager",rollbackFor = RuntimeException.class)
	public Long createQueueAndLog(Map<String, Object> params) {
		Long result = 0L;
		Long queueRet = 0L;
		Long queueLogRet = 0L;
		try {
			if(null == params){
				throw new ValidationException("参数不能为空");
			}
			
			//保存队列
			PaymentOrder paymentOrder = (PaymentOrder) params.get(ExchargeConstant.PAYMENT_ORDER);
			String taskObj = ObjectUtils.toString(params.get(ExchargeConstant.RECHARGE_TASK_OBJ));
			String taskType = ObjectUtils.toString(params.get(ExchargeConstant.RECHARGE_TASK_TYPE));
			String timeout = ObjectUtils.toString(params.get(PaymentConstant.PAYMENT_CALL_TIMEOUT));
			String merchantId = ObjectUtils.toString(params.get(PaymentConstant.MERCHANT_ID));
			
			final String queSeqSql = "INSERT INTO SN_PAY.QUE_MSG_QUEUE_SQ(N_ID) VALUES(NULL)";
			Long sequence = getSequence(queSeqSql);
			Date now = Calendar.getInstance().getTime();
			
			MessageQueue pQuetask = new MessageQueue();
			pQuetask.setId(sequence);
			pQuetask.setBusinessData(taskObj);
			pQuetask.setCreateDate(now);
			pQuetask.setPieceId((paymentOrder.getId()%10));// mod(n_relation_id,10)
			pQuetask.setRelationId(paymentOrder.getId());
			pQuetask.setSubdbId(Long.parseLong(timeout));//  设置超时时间,毫秒
			pQuetask.setTaskType(Long.parseLong(taskType));
			pQuetask.setMerchantId(Long.parseLong(merchantId));
			
			String saveQueue = "INSERT INTO SN_PAY.QUE_MESSAGE_QUEUE(N_ID,N_TYPE,N_SUBIDC_ID,N_RELATION_ID,D_CREATE,S_BUSINESS_DATA,N_PIECE_ID,S_REMARK,N_MERCHANT_ID) values(?,?,?,?,?,?,?,?,?)";
			queueRet = Long.valueOf(queueSessionDao.getJdbcTemplate().update(saveQueue,pQuetask.getId(),pQuetask.getTaskType(),
					pQuetask.getSubdbId(),pQuetask.getRelationId(),pQuetask.getCreateDate(),pQuetask.getBusinessData(),pQuetask.getPieceId(),pQuetask.getRemark(),pQuetask.getMerchantId()));
		
			if(queueRet>0){
				//保存队列日志
				final String queLogSeqSql = "INSERT INTO SN_PAY.QUE_MSG_QUEUE_LOG_SQ(N_ID) VALUES(NULL)";
				Long _sequence = getSequence(queLogSeqSql);
				
				MessageQueueLog queueLog = new MessageQueueLog();
				queueLog.setId(_sequence);
				queueLog.setAccountId(paymentOrder.getAid());
				queueLog.setClientIP(paymentOrder.getClientIp());
				queueLog.setCreateDate(now);
				queueLog.setRelatedId(paymentOrder.getId());
				queueLog.setTaskObj(taskObj);
				queueLog.setType(Long.parseLong(taskType));
				queueLog.setMerchantId(Long.parseLong(merchantId));

				String saveQueueLog = "INSERT INTO SN_PAY.QUE_MESSAGE_LOG(N_ID,N_AID,N_TYPE,N_RELATION_ID,S_BUSINESS_DATA,D_CREATE,N_IP,N_MERCHANT_ID) values(?,?,?,?,?,?,?,?)";
				queueLogRet =  Long.valueOf(queueSessionDao.getJdbcTemplate().update(saveQueueLog,queueLog.getId(),queueLog.getAccountId(),
						queueLog.getType(),queueLog.getRelatedId(),queueLog.getTaskObj(),queueLog.getCreateDate(),queueLog.getClientIP(),queueLog.getMerchantId()));
			}else{
				throw new ValidationException("队列任务存储失败");
			}
		} catch (ValidationException e) {
			logger.error(this.getClass().getSimpleName(),e);
			throw new RuntimeException(e.getMessage());
		} catch (Exception e) {
			logger.error(this.getClass().getSimpleName(),e);
			throw new RuntimeException(e.getMessage());
		}
		
		if(queueRet >0 && queueLogRet >0){
			result = 1L;
		}
		return result;
	}
}
