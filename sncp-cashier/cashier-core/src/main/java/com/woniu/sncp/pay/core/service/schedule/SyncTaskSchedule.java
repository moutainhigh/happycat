package com.woniu.sncp.pay.core.service.schedule;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.woniu.pay.common.utils.PaymentConstant;
import com.woniu.sncp.jdbc.datasource.DataSourceConstants;
import com.woniu.sncp.jdbc.datasource.DataSourceHolder;
import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.pay.common.errorcode.ErrorCode;
import com.woniu.sncp.pay.common.utils.ExchargeConstant;
import com.woniu.sncp.pay.common.utils.ExchargeUtils;
import com.woniu.sncp.pay.common.utils.http.HttpUtils;
import com.woniu.sncp.pay.dao.PassportBaseSessionDAO;
import com.woniu.sncp.pay.repository.pay.MessageQueue;
import com.woniu.sncp.pay.repository.pay.MessageQueueRepository;
import com.woniu.sncp.pay.repository.pay.PassportAsyncTask;
import com.woniu.sncp.pay.repository.pay.PassportAsyncTaskRepository;
import com.woniu.sncp.pojo.payment.PaymentOrder;

/**
 * 
 * <p>descrption: 消息推送异步队列</p>
 * 
 * @author fuzl
 * @date   2016年7月22日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
@Service("syncTaskSchedule")
public class SyncTaskSchedule implements Schedule{

	protected static final Logger LOGGER = LoggerFactory.getLogger(SyncTaskSchedule.class);
	
	public static final String SEHEDULE_STATE_CREATE = "0";
	
	//推送状态,已推送
	public static final String SEHEDULE_STATE_COMPLETE = "1";
	
	@Resource
	private PassportBaseSessionDAO sessionDao;
	
	@Autowired
	private MessageQueueRepository messageQueueRepository;
	
	@Autowired
	PassportAsyncTaskRepository passportAsyncTaskRepository;
	
//	@Resource
//	private BaseHibernateDAO<PassportAsyncTask, Long> ppAsyncTaskDao;
	
//	@Resource
//	private BaseHibernateDAO<PassportQueue, Long> ppQueueTaskDao;
	
	/**
	 * 创建消息推送任务队列
	 */
	@Override
	public PassportAsyncTask createSchedule(Map<String, Object> params) {
		PaymentOrder paymentOrder = (PaymentOrder) params.get(PaymentConstant.PAYMENT_ORDER);
		String taskObj = ObjectUtils.toString(params.get(PaymentConstant.MESSAGE_PUSH_TASK_OBJ));
		String taskType = ObjectUtils.toString(params.get(PaymentConstant.MESSAGE_PUSH_TASK_TYPE));
		
		final String taskSeqSql = "INSERT INTO SN_PASSPORT.PP_ASYNC_TASK_SQ(N_ID) VALUES(NULL)";
		Long sequence = this.getSequence(taskSeqSql);
		
		Date now = Calendar.getInstance().getTime();
		
		PassportAsyncTask task = new PassportAsyncTask();
		task.setId(sequence);
		task.setCount(1);
		task.setCreateDate(now);
		task.setModifyDate(now);//增加修改时间
		task.setOperationId(paymentOrder.getId());
		task.setState(PassportAsyncTask.STATE_NO_OPERATE);
		task.setTaskObj(taskObj);
		task.setTaskType(taskType);
		
//		return ppAsyncTaskDao.save(task);

		return passportAsyncTaskRepository.save(task);
	}
	
	/**
	 * 更新调度
	 */
	@Override
	public void updateSchedule(PassportAsyncTask task,String state) {
		// TODO Auto-generated method stub
		Date now = Calendar.getInstance().getTime();
//		task.setState(state);
//		task.setModifyDate(now);//增加修改时间
//		ppAsyncTaskDao.update(task);
		passportAsyncTaskRepository.updatePpAsyncTask(now, state);
	}

	/**
	 * 执行调度
	 * @throws Exception 
	 */
	@Override
	public String executeSchedule(PassportAsyncTask task) throws Exception {
		try {
			String taskObj = task.getTaskObj();
			JSONObject jsonObj = JSONObject.parseObject(taskObj);
			/**
			 * "sync":"0",
				"rechargeUrl":"",
				"paramsAssem":"1",
				"busiData":{"aa":"1","cc":"2"},
				"head":{"accessId":"","accessType":""},
				"resultType":"json",
				"successFlag":"success",
				"notifyUrl":{}
			 */
			String sync = ObjectUtils.toString(jsonObj.get("sync"));
			String busiData = ObjectUtils.toString(jsonObj.get("busiData"));
			
			if("0".equals(sync)){
				//异步响应
				JSONObject jsonBusiData = JSONObject.parseObject(busiData);
				String signStr = jsonBusiData.getString("sign");
				
				//过滤sign,signType
				Map<String,String> params= ExchargeUtils.paraFilter(jsonBusiData);
				if(task.getTaskType().equals("106")){
					String rechargeUrl = ObjectUtils.toString(jsonObj.get("rechargeUrl"));
					//请求直充方进行充值
					return requestRecharge(params,signStr,rechargeUrl);
				}
//				if(task.getTaskType().equals("107")){
//					String recoverCardUrl = ObjectUtils.toString(jsonObj.get("recoverCardUrl"));
//					//请求扣卡方进行恢复卡
//					return requestRecoverCard(params,signStr,recoverCardUrl);
//				}
				
			}
		} catch (Exception e) {
			LOGGER.error("请求直充方进行充值出错，"+e.getMessage(),e);
			throw new Exception("请求直充方进行充值出错，");
		}
		return null;
	}
	
	
	/**
	 * 请求直充方进行充值
	 * @param params
	 * @throws Exception 
	 */
	public String requestRecharge(Map<String,String> params,String signStr,String rechargeUrl) throws IllegalArgumentException {
		Map<String,String> nameValuePair = new HashMap<String,String>();
		String result = "";
		Integer reqCnt = 0;
		do {
			try {
				StringBuffer sourceStr = new StringBuffer();
				sourceStr.append(ExchargeUtils.createLinkString(params));
				//request请求参数
				List<String> keys = new ArrayList<String>(params.keySet());
				for (int i = 0; i < keys.size(); i++) {
					String key = keys.get(i);
					String value = params.get(key);
					nameValuePair.put(key, value);
				}
				if (LOGGER.isInfoEnabled())
					LOGGER.info("请求直充方进行充值：url[" + rechargeUrl+"]" +sourceStr.toString());
		
				nameValuePair.put("sign", signStr);
//				nameValuePair.putAll(headData);
				String response = HttpUtils.post(rechargeUrl, nameValuePair);
				if(null!=response){
					//将实体对象转换为JSON Object转换  
				    JSONObject responseJSONObject = JSONObject.parseObject(response);
				    
				    if("success".equals(responseJSONObject.get("status"))){
				    	if (LOGGER.isInfoEnabled()){
				    		LOGGER.info("请求直充方进行充值响应结果：" + responseJSONObject);
				    	}
				    	if(null!=responseJSONObject.get("data")){
				    		String data = ObjectUtils.toString(responseJSONObject.get("data"));
				    		JSONObject dataJSONObject = JSONObject.parseObject(data);
				    		if(null!=data && null!=dataJSONObject.get("paymentParams")){
				    			String paymentParams = ObjectUtils.toString(dataJSONObject.get("paymentParams"));
				    			if (LOGGER.isInfoEnabled()){
						    		LOGGER.info("请求直充方进行充值响应paymentParams：" + paymentParams);
						    	}
						    	JSONObject paymentParamsJSONObject = JSONObject.parseObject(paymentParams);
						    	if(null!=paymentParams && null!=paymentParamsJSONObject.get("msgcode")){
						    		String msgcode = ObjectUtils.toString(paymentParamsJSONObject.get("msgcode"));
						    		if(!"1".equals(msgcode)){
						    			dataJSONObject.put(ErrorCode.TIP_CODE, "-1");
						    			result=ObjectUtils.toString(dataJSONObject);
						    			//失败
						    			//break;
						    			return result;
						    		}
						    	}
						    }
					    	if (LOGGER.isInfoEnabled()){
					    		LOGGER.info("请求直充方进行充值成功：" + data);
					    	}
					    	result = data;
				    	}
				    }else{
				    	LOGGER.error("请求直充方进行充值失败，response:"+responseJSONObject);
				    	result = JsonUtils.toJson(responseJSONObject.get("data"));
				    	//throw new IllegalArgumentException("请求直充方进行充值失败，response:"+responseJSONObject);
				    }
				}
				reqCnt = 3;
			} catch (SocketTimeoutException e){
				reqCnt++;
				LOGGER.error("请求直充方进行充值超时重发,["+reqCnt+"],"+e.getMessage(),e);
			} catch (ClientProtocolException e) {
				reqCnt++;
				LOGGER.error("请求直充方进行充值出错，"+e.getMessage(),e);
			} catch (IOException e) {
				reqCnt++;
				LOGGER.error("请求直充方进行充值出错，"+e.getMessage(),e);
			} catch (Exception e) {
				reqCnt++;
				LOGGER.error("请求直充方进行充值出错，"+e.getMessage(),e);
			} finally{
				if(reqCnt > 2){
					break;
				}
			}
		} while (reqCnt<3);
		
		if(StringUtils.isBlank(result)){
			JSONObject resultObj = new JSONObject();
			resultObj.put(ErrorCode.TIP_CODE, "-2");//其他失败
			result = JsonUtils.toJson(resultObj);
		}
		return result;
	}
	
	/**
	 * 请求新计费进行退卡
	 * @param params
	 * @throws Exception 
	 */
	public String requestRecoverCard(Map<String,String> params,String signStr,String rechargeUrl) throws IllegalArgumentException {
		Map<String,String> nameValuePair = new HashMap<String,String>();
		String result = "";
		try {
			
			StringBuffer sourceStr = new StringBuffer();
			sourceStr.append(ExchargeUtils.createLinkString(params));
			//request请求参数
			List<String> keys = new ArrayList<String>(params.keySet());
			for (int i = 0; i < keys.size(); i++) {
				String key = keys.get(i);
				String value = params.get(key);
				nameValuePair.put(key, value);
			}
			if (LOGGER.isInfoEnabled())
				LOGGER.info("请求直充方进行充值：" + sourceStr.toString());
	
			nameValuePair.put("sign", signStr);
//			nameValuePair.putAll(headData);
			String response = HttpUtils.post(rechargeUrl, nameValuePair);
			if(null!=response){
				//将实体对象转换为JSON Object转换  
			    JSONObject responseJSONObject = JSONObject.parseObject(response);
			    if("success".equals(responseJSONObject.get("status"))){
			    	if (LOGGER.isInfoEnabled()){
			    		LOGGER.info("请求直充方进行充值成功：" + responseJSONObject.get("data"));
			    	}
			    	result = ObjectUtils.toString(responseJSONObject.get("data"));
			    }else{
			    	LOGGER.error("请求直充方进行充值失败，response:"+responseJSONObject);
			    	result = JsonUtils.toJson(responseJSONObject.get("data"));
			    	throw new IllegalArgumentException("请求直充方进行充值失败，response:"+responseJSONObject);
			    }
			}
		} catch (SocketTimeoutException e){
			LOGGER.error("请求直充方进行充值超时重发,"+e.getMessage(),e);
			requestRecharge(params,signStr,rechargeUrl);
		} catch (ClientProtocolException e) {
			LOGGER.error("请求直充方进行充值出错，"+e.getMessage(),e);
			return ObjectUtils.toString(ErrorCode.getErrorCode("54210"));
		} catch (IOException e) {
			LOGGER.error("请求直充方进行充值出错，"+e.getMessage(),e);
			return ObjectUtils.toString(ErrorCode.getErrorCode("54210"));
		} catch (Exception e) {
			LOGGER.error("请求直充方进行充值出错，"+e.getMessage(),e);
			return ObjectUtils.toString(ErrorCode.getErrorCode("54210"));
		}
		return result;
	}
	
	/**
	 * 通过id查询调度任务
	 */
	@Override
	public PassportAsyncTask querySchedule(Long newScheduleId) {
//		return ppAsyncTaskDao.findByProperty("id", newScheduleId);
		return passportAsyncTaskRepository.getOne(newScheduleId);
	}

	/**
	 * 通过任务类型和业务id查询调度任务
	 */
	@Override
	public PassportAsyncTask querySchedule(Long operationId, String taskType) {
		String[] properties = new String[]{"operationId","taskType"};
		Object[] values = new Object[]{operationId,taskType};
		return passportAsyncTaskRepository.findByOperationIdAndTaskType(properties, values);
	}

	@Override
	public MessageQueue queryMessageQueue(Long relationId, Long taskType) {
		String[] properties = new String[]{"relationId","taskType"};
		Object[] values = new Object[]{relationId,taskType};
		return messageQueueRepository.findByRelationIdAndTaskType(properties, values);
	}

	@Override
	public MessageQueue createMessageQueue(Map<String, Object> params) {
		PaymentOrder paymentOrder = (PaymentOrder) params.get(ExchargeConstant.PAYMENT_ORDER);
		String taskObj = ObjectUtils.toString(params.get(ExchargeConstant.RECHARGE_TASK_OBJ));
		String taskType = ObjectUtils.toString(params.get(ExchargeConstant.RECHARGE_TASK_TYPE));
		String subdbId = ObjectUtils.toString(params.get(ExchargeConstant.RECHARGE_TASK_SUBDBID));
		
		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER);
		long sequence = sessionDao.findForLong("select sn_queue.que_message_queue_sq.nextval from dual");
		Date now = Calendar.getInstance().getTime();
		
		MessageQueue pQuetask = new MessageQueue();
		pQuetask.setId(sequence);
		pQuetask.setBusinessData(taskObj);
		pQuetask.setCreateDate(now);
		pQuetask.setPieceId((paymentOrder.getId()%10));// mod(n_relation_id,10)
		pQuetask.setRelationId(paymentOrder.getId());
		pQuetask.setSubdbId(Long.parseLong(subdbId));
		pQuetask.setTaskType(Long.parseLong(taskType));
		
		return messageQueueRepository.save(pQuetask);
	}

	
	/**
	 * 通过sql返回当前自增序列值
	 * @param getSeqSql
	 * @return
	 */
	public Long getSequence(final String getSeqSql){
		KeyHolder keyHolder = new GeneratedKeyHolder();
		sessionDao.getPassportJdbcTemplate().update(new PreparedStatementCreator() {  
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
