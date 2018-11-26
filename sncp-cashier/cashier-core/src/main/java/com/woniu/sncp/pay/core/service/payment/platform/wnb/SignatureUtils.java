package com.woniu.sncp.pay.core.service.payment.platform.wnb;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.woniu.sncp.pay.common.utils.encrypt.MD5Encrypt;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignatureUtils {

    static Logger logger = LoggerFactory.getLogger(SignatureUtils.class);

    /**
     *
     * @param uri
     *            域名之后问号之前的部分，没有问号取域名之后所有字符
     * @param method
     *            GET/POST
     * @param requestBody
     *            content-Type:application/json 提交的RequestBody内容
     * @param header
     *            HTTP HEAD
     * @param param
     *            GET/POST queryParam
     * @param accessKey
     * @return
     */
    public static String signature(String uri, String method, String requestBody, Map<String, String> header, Map<String, String> param, String accessKey) {
        Map<String, String> map = new HashMap<String, String>();
        if (header != null) {
            //	HTTP头只有这几个参与签名运算
            map.put("accessId", header.get("accessId"));
            map.put("accessType", header.get("accessType"));
            map.put("accessPasswd", header.get("accessPasswd"));
            map.put("second", header.get("second"));
            map.put("signVersion", "1.0");
            map.put("localReqIp", header.get("localReqIp"));// 非必填
            map.put("tgt", header.get("tgt"));// 非必填
            map.put("clientUserIp", header.get("clientUserIp"));// 非必填
            map.put("captchaContent", header.get("captchaContent"));// 非必填

        }

        if (param != null) {
            //参数优先，同名覆盖HTTP头参数/
            map.putAll(param);
        }
        if (accessKey == null || accessKey.isEmpty()) {
            throw new IllegalArgumentException("accessKey is empty");
        }
        if (uri == null || uri.isEmpty()) {
            throw new IllegalArgumentException("uri is empty");
        }
        validateNotEmpty(map, "accessId", "accessType", "accessPasswd", "second", "signVersion");


        StringBuilder builder = new StringBuilder();
        //Content-Type:application/json   json参与运算
        if (StringUtils.equalsIgnoreCase("POST", method) && requestBody != null&&header!=null) {
            for(String key:header.keySet()){
                if(StringUtils.equalsIgnoreCase(key, "Content-Type")&&containsIgnoreCase(header.get(key), "application/json")){
                    builder.append(requestBody);
                    break;
                }

            }
        }

        builder.append(uri);

        List<String> keys = new ArrayList<String>(map.keySet());
        //key按字母排序
        Collections.sort(keys);
        //按字母排序拼接串
        for (String key : keys) {

            if (key.equals("callback") || key.equals("accessVerify"))
                continue;
            String value = map.get(key);
            if (value != null) {
                builder.append(value);
            }

        }

        builder.append(accessKey);

        String accessVerify = MD5Encrypt.encrypt(builder.toString());

        System.out.println ("sort key:" + keys.toString());
        System.out.println ("singature String:" + builder.toString());
        System.out.println ("accessVerify:" + accessVerify);


        return accessVerify;

    }
    //	非空验证
    private static void validateNotEmpty(Map<String, String> map, String... keys) {
        for (String key : keys) {
            String value = map.get(key);
            if (value == null || value.isEmpty()) {
                throw new IllegalArgumentException(key + " is empty");
            }
        }

    }
     static boolean containsIgnoreCase(String str, String searchStr) {
    	return StringUtils.containsIgnoreCase(str, searchStr);
        
    }

    public static void main(String[] args){
        Map<String,String >header=new HashMap<String,String>();
        //{"id":{"id":10011,"type":"9"},"name":"8","fullName":"8","linker":"丁玉军","email":"309279023@qq.com","seed":"wXut34RcMYpQhV","passwd":"uWH3oDNXJN4Vl","limitIp":"222.92.146.66","dateCreate":1471317060000,"state":"1"}
        //{"id":{"id":203,"type":"9"},"name":"IT质检测试","fullName":"管理架构\\管理\\IT中心\\质量控制部","linker":"丁玉军","tel":"13616270889","email":"309279023@qq.com","im":"309279023","seed":"QhZtz8GYJFARFVlWoUG","passwd":"gdHjPvVF0xVpVRsi","limitIp":"222.92.146.66,192.168.94.11,222.92.100.230,117.121.49.205,58.211.28.182,115.182.109.62,112.86.96.80,58.211.28.178,117.121.49.206,127.0.0.1,192.168.95.1-255","dateCreate":1410767941000,"state":"1","note":"{\"logAccessPrivacy\":[\"accessId\",\"accessType\",\"param3\"]}"},
        //	HTTP头只有这几个参与签名运算
        header.put("accessId", "203");
        header.put("accessType", "9");
        header.put("accessPasswd", "gdHjPvVF0xVpVRsi");
        header.put("second", "11111");
        header.put("signVersion", "1.0");
//			header.put("localReqIp", header.get("localReqIp"));// 非必填
//			header.put("tgt", header.get("tgt"));// 非必填
//			header.put("clientUserIp", header.get("clientUserIp"));// 非必填
//			header.put("captchaContent", header.get("captchaContent"));// 非必填
        header.put("Content-Type", "application/json;charset=utf-8");


        String signature=signature("/gameroles/role/info","POST","{\"gameId\":\"73\",\"accountId\":\"WN36314697\"}",header,null,"QhZtz8GYJFARFVlWoUG");
        System.out.println(signature);
    }
}
