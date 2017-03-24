package com.woniu.sncp.pay.core.service.ocp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.snail.ocp.client.exception.APIException;
import com.snail.ocp.client.pojo.Header;
import com.snail.ocp.sdk.http.account.pojo.AccountBalanceRequest;
import com.snail.ocp.sdk.http.account.pojo.AccountBalanceResponse;
import com.snail.ocp.sdk.http.account.pojo.PayInfoType;
import com.snail.ocp.sdk.http.account.service.AccountInterface;
import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.lang.ArrayUtil;
import com.woniu.sncp.lang.DateUtil;
import com.woniu.sncp.pay.common.errorcode.ErrorCode;
import com.woniu.sncp.pay.common.utils.date.DateUtils;
import com.woniu.sncp.pay.common.utils.http.HttpClient;

@Service("ocpAccountService")
public class OcpAccountServiceImpl implements OcpAccountService {
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private Header accountHeader;
	
	@Autowired
	private AccountInterface httpAccountService;
	
	@Value("${core.account.app.id}")
	private String appId;
	
	@Value("${core.account.app.pwd}")
	private String appPwd;
	
	@Value("${core.account.version}")
	private String version;
	
	@Value("${core.account.cbc}")
	private String cbc;
	
	@Value("${core.account.server}")
	private String server;
	
	@Value("${core.account.connect.timeout}")
	private String connctTime;
	
	@Value("${bussiness.app.url}")
	private String url;
	
	@Value("${bussiness.app.accessId}")
	private String accessId;
	
	@Value("${bussiness.app.accessPassword}")
	private String accessPassword;
	
	@Value("${bussiness.app.accessType}")
	private String accessType;
	
	@Value("${bussiness.app.key}")
	private String key;
	
	@Override
	public Map<String, Object> queryAmount(Map<String, Object> params) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		AccountBalanceRequest accountBalanceRequest = new AccountBalanceRequest();
		accountBalanceRequest.setHeader(accountHeader);
		accountBalanceRequest.setSpId(Integer.valueOf(ObjectUtils.toString(params.get("spId"))));
		accountBalanceRequest.setUserId(Long.valueOf(ObjectUtils.toString(params.get("userId"))));
		accountBalanceRequest.setEventTimestamp(DateUtil.getCurDate());
		accountBalanceRequest.setAppId(ObjectUtils.toString(params.get("appId")));
		accountBalanceRequest.setAreaId(ObjectUtils.toString(params.get("areaId")));
		accountBalanceRequest.setPayTypeId(ObjectUtils.toString(params.get("payTypeId")));
		AccountBalanceResponse accountBalanceResponse = null;
		try {
			accountBalanceResponse = httpAccountService.query(accountBalanceRequest);
		} catch (APIException e) {
			logger.error("OCP 查询余额异常：" + e.getMessage());
			resultMap = ErrorCode.getErrorCode(60000);
			return resultMap;
		}
		String state = accountBalanceResponse.getState();
		if(!"0".equals(state)){
			logger.info("OCP 查询余额返回状态错误,STATE:"+state+",CODE:"+accountBalanceResponse.getResultCode()+",DESC:" + accountBalanceResponse.getFailReason());
			resultMap = ErrorCode.getErrorCode(60001);
			return resultMap;
		}
		resultMap = ErrorCode.getErrorCode(1);
		List<PayInfoType> list = accountBalanceResponse.getBalanceInfo();
		resultMap.put("data", list);
		return resultMap;
	}
	
	public Map<String, Object> queryAmount2(Map<String, Object> params) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String clientIp = ObjectUtils.toString(params.get("clientIp"));
		String merchantId = ObjectUtils.toString(params.get("merchantId"));
		params.remove("clientIp");
		params.remove("merchantId");
		String requsetUrl = server + "account/query";
    	String responseBody = null;
    	try {
    		responseBody = HttpClient.post(requsetUrl, this.getHeaders(ObjectUtils.toString(params.get("userId")), clientIp), params, Integer.valueOf(connctTime), "UTF-8");
    		Map<String, Object> blanceMap = new HashMap<String, Object>();
    		blanceMap = JsonUtils.jsonToMap(responseBody);
    		if(!"0".equals(String.valueOf(blanceMap.get("STATE")))){
    			logger.info("OCP 返回状态为错误,STATE:"+blanceMap.get("STATE")+",CODE:"+blanceMap.get("CODE")+",DESC:" + blanceMap.get("DESC"));
    			resultMap = ErrorCode.getErrorCode(60001);
    			return resultMap;
			}
    		
    		//查询代金卷
    		String resp = null;
    		try {
				resp = queryDjj("36","");//写死gameId=36，type=5;type为空查询所有币种余额,前台进行过滤
			} catch (Exception e) {
				logger.error("查询代金卷异常:" + e.getMessage());
				resultMap = ErrorCode.getErrorCode(60005);
    			return resultMap;
			}
    		
    		List<Map<String, Object>> dList = null;
    		if(StringUtils.isNotBlank(resp)){
    			Map<String, Object> respMap = JsonUtils.jsonToMap(resp);
    			if("1".equals(ObjectUtils.toString(respMap.get("msgcode")))){
        			dList = (List<Map<String, Object>>)respMap.get("data");
        		}
    		}
    		
    		//获得余额
    		ArrayList<Map<String, Object>> objs = (ArrayList<Map<String, Object>>)blanceMap.get("balanceInfo");
    		List<Map<String, Object>> retList = new ArrayList<Map<String,Object>>();
    		if(objs!=null && objs.size() >0){
    			for(int i =0;i<objs.size();i++){
    				Map<String, Object> temp = new HashMap<String, Object>();
    				Map<String, Object> adjj = objs.get(i);
    				String payTypeId = (String)adjj.get("payTypeId");
    				if("o".equals(payTypeId)){
    					temp = adjj;
    					for(int j=0;j<dList.size();j++){
    						Map<String, Object> djj = dList.get(j);
    						String currencyId = (String)djj.get("currencyId");
    						String payments = (String)djj.get("payments");
    						if(StringUtils.isBlank(payments)) continue;
    						String[] paymentArray = payments.split(","); 
    						String sms = (String)djj.get("sms");
    						if(payTypeId.equals(currencyId)){
    							if(ArrayUtil.contains(paymentArray, merchantId)){
    								temp.put("sms", sms);
    								break;
    							}
    						}
        				}
    				}else{
    					String amount = (String)adjj.get("amount");
    					if(Float.parseFloat(amount) <= 0) 
    						continue;
    					String endTime = (String)adjj.get("endTime");
    					if(StringUtils.isBlank(endTime)){
    						boolean configPaymentCurrencyId = false;
    						for(int j=0;j<dList.size();j++){
        						Map<String, Object> djj = dList.get(j);
        						String currencyId = (String)djj.get("currencyId");
        						String payments = (String)djj.get("payments");
        						if(StringUtils.isBlank(payments)) continue;
        						String[] paymentArray = payments.split(","); 
        						if(payTypeId.equals(currencyId)){
        							if(ArrayUtil.contains(paymentArray, merchantId)){
        								configPaymentCurrencyId = true;
        								break;
        							}
        						}
            				}
    						
    						// 已配置的币种，需要显示;未配置的过滤
    						if(!configPaymentCurrencyId){
    							continue;
    						}
    					}else{
    						if(DateUtils.parse(endTime).before(Calendar.getInstance(TimeZone.getDefault()))) 
    							continue;
    					}
    					if(dList!=null && dList.size() >0){
        					for(int j=0;j<dList.size();j++){
        						Map<String, Object> djj = dList.get(j);
        						String currencyId = (String)djj.get("currencyId");
        						String payments = (String)djj.get("payments");
        						if(StringUtils.isBlank(payments)) continue;
        						String[] paymentArray = payments.split(","); 
        						String sms = (String)djj.get("sms");
        						if(payTypeId.equals(currencyId)){
        							if(ArrayUtil.contains(paymentArray, merchantId)){
        								temp = adjj;
        								temp.put("sms", sms);
        								break;
        							}
        						}
            				}
        				}
    				}
    				if(!temp.isEmpty()){
    					retList.add(temp);
    				}
    			}
    		}
    		
    		resultMap = ErrorCode.getErrorCode(1);
    		resultMap.put("data", retList);
    		
    	} catch (Exception e) {
    		logger.error("OCP 查询余额异常：" + e.getMessage());
    		resultMap = ErrorCode.getErrorCode(60000);
			return resultMap;
		}
		return resultMap;
	}
	
	@Override
	public Map<String, Object> chargeAmount(Map<String, Object> inParams) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String userId = ObjectUtils.toString(inParams.get("userId"));
		String clientIp = ObjectUtils.toString(inParams.get("clientIp"));
		String orderNo = ObjectUtils.toString(inParams.get("orderNo"));
//		String payInfo = ObjectUtils.toString(inParams.get("payInfo"));
//		String listItemInfo = ObjectUtils.toString(inParams.get("listItemInfo"));
		
		Map<String, Object> params = new HashMap<String, Object>();
    	params.put("userId", userId);
    	params.put("spId", 7);
    	params.put("appId", "36");
    	params.put("areaId", "-1");
    	params.put("orderId", orderNo);
    	params.put("payInfo", inParams.get("payInfo"));
    	params.put("itemNum", "1");
    	params.put("itemInfo", inParams.get("listItemInfo"));
    	params.put("eventTimestamp", DateUtil.getCurDateTimeStr());
    	
    	String requsetUrl = server + "account/charge";
    	String responseBody = null;
    	try {
    		responseBody = HttpClient.post(requsetUrl, this.getHeaders(String.valueOf(userId), clientIp), params, Integer.valueOf(connctTime), "UTF-8");
    		Map<String, Object> blanceMap = new HashMap<String, Object>();
    		blanceMap = JsonUtils.jsonToMap(responseBody);
    		if(!"0".equals(String.valueOf(blanceMap.get("STATE")))){
    			logger.info("OCP 扣款返回状态为错误,STATE:"+blanceMap.get("STATE")+",CODE:"+blanceMap.get("CODE")+",DESC:" + blanceMap.get("DESC"));
    			resultMap = ErrorCode.getErrorCode(60004);
    			resultMap.put("message", blanceMap.get("DESC"));
    			return resultMap;
			}
    		//扣款成功
    		resultMap = ErrorCode.getErrorCode(1);
    		
    	} catch (Exception e) {
    		logger.error("OCP 扣除余额异常：" + e.getMessage());
    		resultMap = ErrorCode.getErrorCode(60003);
			return resultMap;
		}
		return resultMap;
	}
	
	public Map<String,Object> getHeaders(String uid,String uip){
    	Map<String, Object> headers = new HashMap<String, Object>();
    	headers.put("H_APPID", appId);
    	headers.put("H_PWD", appPwd);
    	headers.put("H_UID", uid);
    	headers.put("H_UIP", uip);
    	headers.put("H_CBC", cbc);
    	headers.put("Content-Type", "application/x-www-form-urlencoded");
    	headers.put("H_V", version);
		return headers;
    }
	
	public String queryDjj(String gameId,String type) throws Exception{
		if(StringUtils.isBlank(gameId))
			return null;
		String resp = null;
		Map<String, Object> dataInfoMap = new HashMap<String, Object>();
		dataInfoMap.put("type", type);
		dataInfoMap.put("gameId", gameId);
		String dataInfo = JsonUtils.toJson(dataInfoMap);
		Map<String, Object> securityInfoMap = new HashMap<String, Object>();
		securityInfoMap.put("accessId", accessId);
		securityInfoMap.put("accessPasswd", accessPassword);
		securityInfoMap.put("accessType", accessType);
		securityInfoMap.put("returnType", "json");
		String source = accessId+accessPassword+accessType+dataInfo+"json"+key;
		String md5 = MD5Encrypt.encrypt(source, "utf-8");
		securityInfoMap.put("verifyStr", md5);
		String securityInfo = JsonUtils.toJson(securityInfoMap);
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("securityInfo", securityInfo);
		params.put("dataInfo", dataInfo);
		try {
			resp = HttpClient.post(url, null, params, Integer.valueOf(connctTime), "utf-8", false);
		} catch (Exception e) {
			logger.error("查询代金卷异常:" + e.getMessage());
			throw new Exception(e.getMessage());
		}
		
		return resp;
	}
	
	public boolean isSmsCheck(String djj){
		boolean isSms = false;
		if(StringUtils.isBlank(djj)){
			return false;
		}
		//查询代金卷
		String resp = null;
		try {
			resp = queryDjj("36","");//写死gameId=36，type=5
		} catch (Exception e) {
			return false;
		}
		
		List<Map<String, Object>> dList = null;
		if(StringUtils.isNotBlank(resp)){
			Map<String, Object> respMap = JsonUtils.jsonToMap(resp);
			if("1".equals(ObjectUtils.toString(respMap.get("msgcode")))){
    			dList = (List<Map<String, Object>>)respMap.get("data");
    		}
		}
		
		String[] djjArr =  djj.split("#");
		if(djjArr !=null && ArrayUtil.isNotEmpty(djjArr)){
			for(int i=0;i<djjArr.length;i++){
				String[] currAndAmount = djjArr[i].split(",");
				String curr = currAndAmount[0];
				if(dList!=null && dList.size() >0){
					for(int j=0;j<dList.size();j++){
						Map<String, Object> qdjj = dList.get(j);
						String currencyId = (String)qdjj.get("currencyId");
						if(curr.equals(currencyId)){
							String sms = (String)qdjj.get("sms");
							if("1".equals(sms)){
								isSms  =true;
								break;
							}
						}
					}
				}
			}
			
		}
		return isSms;
	}
	
	public boolean isSmsCheck(String djj,String merchantid){
		boolean isSms = false;
		if(StringUtils.isBlank(djj)){
			return false;
		}
		//查询代金卷
		String resp = null;
		try {
			resp = queryDjj("36","");//写死gameId=36，type=5
		} catch (Exception e) {
			return false;
		}
		
		List<Map<String, Object>> dList = null;
		if(StringUtils.isNotBlank(resp)){
			Map<String, Object> respMap = JsonUtils.jsonToMap(resp);
			if("1".equals(ObjectUtils.toString(respMap.get("msgcode")))){
    			dList = (List<Map<String, Object>>)respMap.get("data");
    		}
		}
		
		String[] djjArr =  djj.split("#");
		if(djjArr !=null && ArrayUtil.isNotEmpty(djjArr)){
			for(int i=0;i<djjArr.length;i++){
				String[] currAndAmount = djjArr[i].split(",");
				String curr = currAndAmount[0];
				if(dList!=null && dList.size() >0){
					for(int j=0;j<dList.size();j++){
						Map<String, Object> qdjj = dList.get(j);
						String currencyId = (String)qdjj.get("currencyId");
						String payments = (String)qdjj.get("payments");
						if(StringUtils.isBlank(payments)) continue;
						String[] paymentArray = payments.split(","); 
						String sms = (String)qdjj.get("sms");
						if(curr.equals(currencyId)){
							if(ArrayUtil.contains(paymentArray, merchantid)){
								
								if("1".equals(sms)){
									isSms  =true;
									break;
								}
							}
						}
//						if(curr.equals(currencyId)){
//							String sms = (String)qdjj.get("sms");
//							if("1".equals(sms)){
//								isSms  =true;
//								break;
//							}
//						}
					}
				}
			}
			
		}
		return isSms;
	}
	
	@Override
	public Map<String, Object> addAmount(Map<String, Object> params) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String clientIp = ObjectUtils.toString(params.get("clientIp"));
		params.remove("clientIp");
		String requsetUrl = server + "account/imprest";
    	String responseBody = null;
    	try {
    		responseBody = HttpClient.post(requsetUrl, this.getHeaders(ObjectUtils.toString(params.get("userId")), clientIp), params, Integer.valueOf(connctTime), "UTF-8");
    		Map<String, Object> blanceMap = new HashMap<String, Object>();
    		blanceMap = JsonUtils.jsonToMap(responseBody);
    		if(!"0".equals(String.valueOf(blanceMap.get("STATE")))){
    			logger.info("OCP 返回状态为错误,STATE:"+blanceMap.get("STATE")+",CODE:"+blanceMap.get("CODE")+",DESC:" + blanceMap.get("DESC"));
    			resultMap = ErrorCode.getErrorCode(60007);
    			return resultMap;
			}
    		
    		//获得余额
    		ArrayList<Map<String, Object>> objs = (ArrayList<Map<String, Object>>)blanceMap.get("balanceInfo");
    		resultMap = ErrorCode.getErrorCode(1);
    		resultMap.put("data", objs);
    		
    	} catch (Exception e) {
    		logger.error("OCP 增加余额异常：" + e.getMessage());
    		resultMap = ErrorCode.getErrorCode(60006);
			return resultMap;
		}
		return resultMap;
	}

	@Override
	public Map<String, Object> deductAmount(Map<String, Object> inParams) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String userId = ObjectUtils.toString(inParams.get("userId"));
		String appId = ObjectUtils.toString(inParams.get("appId"));
		String spId = ObjectUtils.toString(inParams.get("spId"));
		String areaId = ObjectUtils.toString(inParams.get("areaId"));
		String clientIp = ObjectUtils.toString(inParams.get("clientIp"));
		String orderNo = ObjectUtils.toString(inParams.get("orderNo"));
		
		Map<String, Object> params = new HashMap<String, Object>();
    	params.put("userId", userId);
    	params.put("spId", spId);
    	params.put("appId", appId);
    	params.put("areaId", areaId);
    	params.put("orderId", orderNo);
    	params.put("payInfo", inParams.get("payInfo"));
    	params.put("itemNum", "1");
    	params.put("itemInfo", inParams.get("listItemInfo"));
    	params.put("eventTimestamp", DateUtil.getCurDateTimeStr());
    	
    	String requsetUrl = server + "account/charge";
    	String responseBody = null;
    	try {
    		responseBody = HttpClient.post(requsetUrl, this.getHeaders(String.valueOf(userId), clientIp), params, Integer.valueOf(connctTime), "UTF-8");
    		Map<String, Object> blanceMap = new HashMap<String, Object>();
    		blanceMap = JsonUtils.jsonToMap(responseBody);
    		if(!"0".equals(String.valueOf(blanceMap.get("STATE")))){
    			logger.info("OCP 扣款返回状态为错误,STATE:"+blanceMap.get("STATE")+",CODE:"+blanceMap.get("CODE")+",DESC:" + blanceMap.get("DESC"));
    			resultMap = ErrorCode.getErrorCode(60004);
    			resultMap.put("message", blanceMap.get("DESC"));
    			return resultMap;
			}
    		//扣款成功
    		resultMap = ErrorCode.getErrorCode(1);
    		
    	} catch (Exception e) {
    		logger.error("OCP 扣除余额异常：" + e.getMessage());
    		resultMap = ErrorCode.getErrorCode(60003);
			return resultMap;
		}
		return resultMap;
	}
	
}
