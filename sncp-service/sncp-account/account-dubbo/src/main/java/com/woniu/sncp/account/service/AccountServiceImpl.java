/**
 * <p>Copyright (c) Snail Game 2016</p>
 */
package com.woniu.sncp.account.service;

import com.alibaba.fastjson.JSON;
import com.woniu.sncp.account.dto.OcpAccountBanlanceErrorResponseDTO;
import com.woniu.sncp.account.dto.OcpAccountBanlanceNormalResponseDTO;
import com.woniu.sncp.account.dto.OcpAccountDTO;
import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @Title: AccountServiceImpl
 * @Description:
 * @Author zhujing
 * @Date 2016/7/7
 * @Version V1.0
 */
public class AccountServiceImpl implements AccountService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HttpHeaders httpHeaders;

    @Value("${ocp.account.query.url}")
    private String OCP_ACCOUNT_QUERY_URL;


    @Override
    public Object queryBalance(Long userId, Integer spId,
                                  String appId, String areaId,
                                  String sessionId, String payTypeId,
                                  String eventTimestamp, Object appendix,
                                  String businessCode) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userId", userId);
        map.put("spId", spId);
        map.put("appId", appId);
        map.put("areaId", areaId);
        if( sessionId != null && sessionId != "" ){
            map.put("sessionId", sessionId);
        }
        map.put("payTypeId", payTypeId);
        map.put("eventTimestamp", eventTimestamp);
        if( appendix != null && appendix != "" ){
            map.put("appendix", appendix);
        }

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<String, String>();
        requestMap.add("params", JSON.toJSON(map).toString());

        httpHeaders.set("H_CBC", businessCode);
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<MultiValueMap<String, String>>(requestMap, httpHeaders);
        ResponseEntity<?> responseEntity = null;
        try {
            logger.info("Http Request - [" + OCP_ACCOUNT_QUERY_URL+ "], " + request);
            responseEntity = restTemplate.postForEntity(OCP_ACCOUNT_QUERY_URL, request, Object.class);
            logger.info("Http Response - [" + OCP_ACCOUNT_QUERY_URL + "], " + responseEntity );
        } catch (RestClientException e) {
            logger.error("Error: " + e.getMessage());
        }
        Object object = responseEntity.getBody();

        OcpAccountDTO ocpAccountDTO = this.convertResponse(object);

        return ocpAccountDTO;
    }

    public OcpAccountDTO convertResponse(Object obj) {
        OcpAccountBanlanceNormalResponseDTO normalResponseDTO = null;
        OcpAccountBanlanceErrorResponseDTO errorResponseDTO = null;
        if( obj != null ){
            if( obj instanceof Map ){
                Map<String, Object> returnMap = (Map<String, Object>)obj;
                if( returnMap.get("STATE") != null && returnMap.get("STATE").toString().equals("0") ){
                    normalResponseDTO = new DozerBeanMapper().map(returnMap, OcpAccountBanlanceNormalResponseDTO.class);
                    return normalResponseDTO;
                }else{
                    errorResponseDTO = new DozerBeanMapper().map(returnMap, OcpAccountBanlanceErrorResponseDTO.class);
                    return errorResponseDTO;
                }
            }
        }
        return null;
    }

}
