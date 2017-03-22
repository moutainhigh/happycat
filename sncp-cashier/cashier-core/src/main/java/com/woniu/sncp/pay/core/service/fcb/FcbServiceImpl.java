package com.woniu.sncp.pay.core.service.fcb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.woniu.pay.pojo.GamePropsCurrency;
import com.woniu.sncp.jdbc.datasource.DataSourceConstants;
import com.woniu.sncp.jdbc.datasource.DataSourceHolder;
import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.lang.DateUtil;
import com.woniu.sncp.pay.common.errorcode.ErrorCode;
import com.woniu.sncp.pay.common.utils.Assert;
import com.woniu.sncp.pay.common.utils.http.HttpClient;
import com.woniu.sncp.pay.common.utils.http.HttpUtils;
import com.woniu.sncp.pay.core.service.MemcachedService;
import com.woniu.sncp.pay.core.service.ocp.OcpAccountService;
import com.woniu.sncp.pay.dao.BaseSessionDAO;

@Service
public class FcbServiceImpl implements FcbService {
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final static String FCB_AUTHENTICATION_PREFIX = "FCB_AUTHENTICATION_";
	//验证码有效时间，单位分钟
	private final static int EXPIRE_INTERVAL = 10;
	
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
	
	/**
	 * 翡翠身份认证地址
	 */
	@Value("${fcb.validation.token}")
	private String validateUrl;
	
	@Resource
	private BaseSessionDAO sessionDao;
	@Resource
	private OcpAccountService ocpAccountService;
	
	@Autowired
	private MemcachedService memcachedService;

	@Override
	public GamePropsCurrency queryById(long id) {
		Assert.notNull(id);
		String querySql = "SELECT * FROM SN_GAME.GAME_PROPS_CURRENCY C WHERE C.S_STATE = '1' AND C.N_ID = ?";
		
		Map<String,Object> paramMap = new HashMap<String,Object>();
		paramMap.put("id", id);
		
		DataSourceHolder.setDataSourceType(DataSourceConstants.DS_CENTER_READ);
		List<GamePropsCurrency> result = sessionDao.getMyJdbcTemplate().query(querySql, new Object[]{id},new RowMapper<GamePropsCurrency>() {
            @Override
            public GamePropsCurrency mapRow(ResultSet rs, int rowNum) throws SQLException {
            	GamePropsCurrency ret = new GamePropsCurrency();
            	ret.setId(rs.getLong("N_ID"));
            	ret.setCurrency(rs.getString("S_CURRENCY"));
            	ret.setGameId(rs.getLong("N_GAME_ID"));
            	ret.setType(rs.getString("S_TYPE"));
            	ret.setName(rs.getString("S_NAME"));
            	ret.setUnitName(rs.getString("S_UNIT_NAME"));
            	ret.setPointPercent(rs.getFloat("N_POINT_PERCENT"));
            	ret.setNote(rs.getString("S_NOTE"));
            	ret.setLimit(rs.getString("S_LIMIT"));
            	ret.setRate(rs.getString("S_RATE"));
            	ret.setUrl(rs.getString("S_URL"));
                return ret;
            }
        });
		
		return result.isEmpty()?null:result.get(0);
	}

	@Override
	public Map<String, Object> queryAmount(Map<String, Object> params) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String clientIp = ObjectUtils.toString(params.get("clientIp"));
		params.remove("clientIp");
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
    		
    		//获得余额
    		ArrayList<Map<String, Object>> objs = (ArrayList<Map<String, Object>>)blanceMap.get("balanceInfo");
    		resultMap = ErrorCode.getErrorCode(1);
    		resultMap.put("data", objs);
    		
    	} catch (Exception e) {
    		logger.error("OCP 查询余额异常：" + e.getMessage());
    		resultMap = ErrorCode.getErrorCode(60000);
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

	@Override
	public Map<String, Object> addFcbAmount(Long aid, String orderId,
			Float money) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", aid);
		params.put("spId", 7);
		params.put("appId", "74");
		params.put("areaId", "-1");
		params.put("orderId", orderId);
		params.put("payTypeId", "74");
		params.put("amt", money);
		params.put("price", money);
		params.put("depositTime", DateUtil.getCurDateTimeStr());
		params.put("eventTimestamp", DateUtil.getCurDateTimeStr());
		Map<String, Object> ret = ocpAccountService.addAmount(params);
		return ret;
	}

	@Override
	public Map<String, Object> deductFcbAmount(Long aid, String orderNo,
			Float money,String clientIp) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", aid);
		params.put("clientIp", clientIp);
		params.put("orderNo", orderNo);
		params.put("spId", 7);
    	params.put("appId", "74");
    	params.put("areaId", "-1");
		//组装payInfo
		List<Map<String,Object>> listPayInfo = new ArrayList<Map<String,Object>>();
		if(money > 0){
			Map<String,Object> payInfo = new HashMap<String,Object>();
			payInfo.put("payTypeId", "74");//翡翠币
			payInfo.put("amount", money);
			listPayInfo.add(payInfo);
		}
		params.put("payInfo", listPayInfo);
		//组装listItemInfo
		List<Map<String,Object>> listItemInfo = new ArrayList<Map<String,Object>>();
		Map<String,Object> itemInfo = new HashMap<String,Object>();
		itemInfo.put("counterid", "0");
		itemInfo.put("itemid", "-1");
		itemInfo.put("appid", "-1");
		itemInfo.put("areaid", "-1");
		itemInfo.put("num", "1");
		itemInfo.put("paytypeid", "-1");
		itemInfo.put("amt", "0");
		listItemInfo.add(itemInfo);
		params.put("listItemInfo", listItemInfo);
		Map<String, Object> retMap = ocpAccountService.deductAmount(params);
		return retMap;
	}

	@Override
	public Map<String, Object> validateToken(String payToken) {
		if(StringUtils.isBlank(payToken)) {
			logger.info("翡翠payToken为空：" + payToken);
			return null;
		}
		
		Map<String, Object> retMap = new HashMap<String, Object>();
		Map<String,String> nameValuePair = new HashMap<String,String>();
		nameValuePair.put("payToken", payToken);
		
		try {
			logger.info("身份认证地址:" + validateUrl + ",参数:" + nameValuePair);
			String response = HttpUtils.post(validateUrl, nameValuePair);
			logger.info("身份认证地址["+validateUrl+"],返回值:"+response);
			
			retMap = JsonUtils.jsonToMap(response);
		} catch (Exception e) {
			logger.error("翡翠币身份验证异常：" + e.getMessage());
			return null;
		}
		return retMap;
	}
	
	public boolean registFcbIdentity(String fcbAccount,String fcbPhone){
		FcbIdentity fcbIdentity = new FcbIdentity();
		fcbIdentity.setAccount(fcbAccount);
		fcbIdentity.setPhone(fcbPhone);
		return memcachedService.set(getFcbAuthkey(fcbAccount), EXPIRE_INTERVAL*60, fcbIdentity);
	}
	
	public FcbIdentity hasFcbIdentity(String fcbAccount){
		FcbIdentity fcbIdentity = (FcbIdentity)memcachedService.get(getFcbAuthkey(fcbAccount));
		return fcbIdentity;
//		if(fcbIdentity!= null && StringUtils.isNotBlank(fcbIdentity.getAccount())
//				&& StringUtils.isNotBlank(fcbIdentity.getPhone())){
//			return true;
//		}
//		return false;
	}
	
	private String getFcbAuthkey(String fcbaccount){
		return FCB_AUTHENTICATION_PREFIX + fcbaccount;
	}

}
