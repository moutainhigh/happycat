package com.woniu.sncp.pay.core.service.sms;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.woniu.sncp.crypto.MD5Encrypt;
import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.pay.common.utils.http.HttpClient;

@Service
public class SmsServiceImpl implements SmsService {
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String _char_set = "utf-8";
	
	@Value("${sms.sendurl}")
	private String sendurl;
	
	@Value("${sms.checkurl}")
	private String checkurl;
	
	@Value("${sms.appId}")
	private String appId;
	
	@Value("${sms.type}")
	private String smstype;
	
	@Value("${sms.key}")
	private String key;

	@Override
	public Map<String, Object> sendSmsValidateCode(String mobileNo, String type) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		TreeMap<String, Object> params = new TreeMap<String, Object>();
        params.put("mobileNo", mobileNo);
        params.put("appId", this.appId);
        String sendType = StringUtils.isBlank(type) ? this.smstype : type;
        params.put("type", sendType);
        String source = this.getMd5Source(params, this.key);
        String sign = MD5Encrypt.encrypt(source,_char_set);
        params.put("sign", sign);
        
        String resp = null;
        try {
        	resp = HttpClient.post(this.sendurl, null, params, 5000, _char_set, false);
		} catch (Exception e) {
			logger.error("发送短信验证码失败：" + e.getMessage());
			retMap.put("code", "1");
			retMap.put("message", "短信发送失败");
			return retMap;
		}
        //code=0发送成功
        retMap = JsonUtils.jsonToMap(resp);
        return retMap;
	}
	
	@Override
	public Map<String, Object> validateSmsCode(String mobileNo, String type, String smsCode) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		TreeMap<String, Object> params = new TreeMap<String, Object>();
        params.put("mobileNo", mobileNo);
        params.put("content", smsCode);
        params.put("appId", this.appId);
        String sendType = StringUtils.isBlank(type) ? this.smstype : type;
        params.put("type", sendType);
        String source = this.getMd5Source(params, this.key);
        String sign = MD5Encrypt.encrypt(source,_char_set);
        params.put("sign", sign);
        
        String resp = null;
        try {
        	resp = HttpClient.post(this.checkurl, null, params, 5000, _char_set, false);
		} catch (Exception e) {
			logger.error("短信验证码验证失败：" + e.getMessage());
			retMap.put("code", "21");
			retMap.put("message", "短信验证码验证异常");
			return retMap;
		}
        
        //code=20验证成功
        retMap = JsonUtils.jsonToMap(resp);
        return retMap;
	}
	
	private String getMd5Source(Map<String, Object> params,String key){
		if(params == null || params.isEmpty()) 
			return null;
		Iterator<String> iter = params.keySet().iterator();
        StringBuffer sb = new StringBuffer();
        while (iter.hasNext()) {
            String mk = iter.next();
            sb.append(mk);
            sb.append(params.get(mk));
        }
        sb.append(key);
        return sb.toString();
	}

}
