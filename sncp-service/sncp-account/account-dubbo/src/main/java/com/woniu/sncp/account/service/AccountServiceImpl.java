/**
 * <p>Copyright (c) Snail Game 2016</p>
 */
package com.woniu.sncp.account.service;

import com.alibaba.fastjson.JSON;
import com.woniu.sncp.account.dto.*;
import com.woniu.sncp.exception.SystemException;
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
import java.util.List;
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

    @Value("${http.ocp.account.appid}")
    private String H_APPID;

    @Value("${http.ocp.account.pwd}")
    private String H_PWD;

    @Value("${ocp.account.query.url}")
    private String OCP_ACCOUNT_QUERY_URL;

    @Value("${ocp.account.easy.imprest.url}")
    private String OCP_ACCOUNT_EASY_IMPREST_URL;

    @Value("${ocp.account.easy.charge.url}")
    private String OCP_ACCOUNT_EASY_CHARGE_URL;

    @Value("${ocp.account.imprest.url}")
    private String OCP_ACCOUNT_IMPREST_URL;

    @Value("${ocp.account.charge.url}")
    private String OCP_ACCOUNT_CHARGE_URL;


    @Override
    public OcpAccountDTO queryBalanceMicroService(Long userId, Integer spId,
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

        HttpHeaders httpHeaders = this.create(businessCode);
        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<MultiValueMap<String, String>>(requestMap, httpHeaders);
        ResponseEntity<?> responseEntity = null;

        long start = System.currentTimeMillis();
        try {
            logger.info("Http Request - [" + OCP_ACCOUNT_QUERY_URL+ "], " + request);
            responseEntity = restTemplate.postForEntity(OCP_ACCOUNT_QUERY_URL, request, Object.class);
            logger.info("Http Response - [" + OCP_ACCOUNT_QUERY_URL + "], " + responseEntity );
        } catch (RestClientException e) {
            logger.error("Error: " + e.getMessage());
            throw new SystemException("RestClientException  :  " + e.getMessage());
        } finally {
            long end = System.currentTimeMillis();
            logger.info("sncp-account.queryBalance接口耗时情况,开始时间: " + start + ",结束时间: " + end + ",耗时: " + (end-start) + ",账号ID:" + userId);
        }
        Object object = responseEntity.getBody();

        OcpAccountDTO ocpAccountDTO = this.convertResponse(object);

        return ocpAccountDTO;
    }

    @Override
    public OcpAccountDTO easyImprestMicroService(Long userId, String appId, String areaId, String payTypeId,
                              String price, String amt, String orderNo, Object appendix, String businessCode) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userId", userId);
        map.put("appId", appId);
        map.put("areaId", areaId);
        map.put("payTypeId", payTypeId);
        map.put("price", price);
        map.put("amt", amt);
        if( orderNo != null && orderNo != "" ){
            map.put("orderNo", orderNo);
        }
        if( appendix != null && appendix != "" ){
            map.put("appendix", appendix);
        }

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<String, String>();
        requestMap.add("params", JSON.toJSON(map).toString());

        HttpHeaders httpHeaders = this.create(businessCode);
        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<MultiValueMap<String, String>>(requestMap, httpHeaders);
        ResponseEntity<?> responseEntity = null;

        long start = System.currentTimeMillis();
        try {
            logger.info("Http Request - [" + OCP_ACCOUNT_EASY_IMPREST_URL+ "], " + request);
            responseEntity = restTemplate.postForEntity(OCP_ACCOUNT_EASY_IMPREST_URL, request, Object.class);
            logger.info("Http Response - [" + OCP_ACCOUNT_EASY_IMPREST_URL + "], " + responseEntity );
        } catch (RestClientException e) {
            logger.error("Error: " + e.getMessage());
            throw new SystemException("RestClientException  :  " + e.getMessage());
        } finally {
            long end = System.currentTimeMillis();
            logger.info("sncp-account.easyImprest接口耗时情况,开始时间: " + start + ",结束时间: " + end + ",耗时: " + (end-start) + ",账号ID:" + userId);
        }
        Object object = responseEntity.getBody();

        OcpAccountDTO ocpAccountDTO = this.convertEasyImprestChargeResponse(object);

        return ocpAccountDTO;
    }

    @Override
    public OcpAccountDTO easyChargeMicroService(Long userId, String appId, String areaId, String payTypeId,
                             String amt, String orderNo, Object appendix, String businessCode) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userId", userId);
        map.put("appId", appId);
        map.put("areaId", areaId);
        map.put("payTypeId", payTypeId);
        map.put("amt", amt);
        if( orderNo != null && orderNo != "" ){
            map.put("orderNo", orderNo);
        }
        if( appendix != null && appendix != "" ){
            map.put("appendix", appendix);
        }

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<String, String>();
        requestMap.add("params", JSON.toJSON(map).toString());

        HttpHeaders httpHeaders = this.create(businessCode);
        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<MultiValueMap<String, String>>(requestMap, httpHeaders);
        ResponseEntity<?> responseEntity = null;

        long start = System.currentTimeMillis();
        try {
            logger.info("Http Request - [" + OCP_ACCOUNT_EASY_CHARGE_URL+ "], " + request);
            responseEntity = restTemplate.postForEntity(OCP_ACCOUNT_EASY_CHARGE_URL, request, Object.class);
            logger.info("Http Response - [" + OCP_ACCOUNT_EASY_CHARGE_URL + "], " + responseEntity );
        } catch (RestClientException e) {
            logger.error("Error: " + e.getMessage());
            throw new SystemException("RestClientException  :  " + e.getMessage());
        } finally {
            long end = System.currentTimeMillis();
            logger.info("sncp-account.easyCharge接口耗时情况,开始时间: " + start + ",结束时间: " + end + ",耗时: " + (end-start) + ",账号ID:" + userId);
        }
        Object object = responseEntity.getBody();

        OcpAccountDTO ocpAccountDTO = this.convertEasyImprestChargeResponse(object);

        return ocpAccountDTO;
    }

    @Override
    public OcpAccountDTO imprestMicroService(Long userId, String appId, String areaId, String sessionId, String orderNo,
                                      String payTypeId, String amt, String price, String endTime, String depositTime,
                                      String eventTimestamp, Object appendix, String businessCode) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userId", userId);
        map.put("appId", appId);
        map.put("areaId", areaId);
        if( sessionId != null && sessionId != "" ){
            map.put("sessionId", sessionId);
        }
        map.put("orderId", orderNo);
        map.put("payTypeId", payTypeId);
        map.put("amt", amt);
        map.put("price", price);
        if( endTime != null && endTime != "" ){
            map.put("endTime", endTime);
        }
        map.put("depositTime", depositTime);
        map.put("eventTimestamp", eventTimestamp);
        if( appendix != null && appendix != "" ){
            map.put("appendix", appendix);
        }

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<String, String>();
        requestMap.add("params", JSON.toJSON(map).toString());

        HttpHeaders httpHeaders = this.create(businessCode);
        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<MultiValueMap<String, String>>(requestMap, httpHeaders);
        ResponseEntity<?> responseEntity = null;

        long start = System.currentTimeMillis();
        try {
            logger.info("Http Request - [" + OCP_ACCOUNT_IMPREST_URL+ "], " + request);
            responseEntity = restTemplate.postForEntity(OCP_ACCOUNT_IMPREST_URL, request, Object.class);
            logger.info("Http Response - [" + OCP_ACCOUNT_IMPREST_URL + "], " + responseEntity );
        } catch (RestClientException e) {
            logger.error("Error: " + e.getMessage());
            throw new SystemException("RestClientException  :  " + e.getMessage());
        } finally {
            long end = System.currentTimeMillis();
            logger.info("sncp-account.imprest接口耗时情况,开始时间: " + start + ",结束时间: " + end + ",耗时: " + (end-start) + ",账号ID:" + userId);
        }
        Object object = responseEntity.getBody();

        OcpAccountDTO ocpAccountDTO = this.convertImprestChargeResponse(object);


        return ocpAccountDTO;
    }

    @Override
    public OcpAccountDTO chargeMicroService(Long userId, int spId, String appId, String areaId, String sessionId,
                                     String orderNo, Object payInfo, String itemNum, List itemInfo, String eventTimestamp,
                                     Boolean ignoreExpired, Object appendix, String businessCode) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userId", userId);
        map.put("spId", spId);
        map.put("appId", appId);
        map.put("areaId", areaId);
        if( sessionId != null && sessionId != "" ){
            map.put("sessionId", sessionId);
        }
        map.put("orderId", orderNo);
        map.put("payInfo", payInfo);
        map.put("itemNum", itemNum);
        map.put("itemInfo", itemInfo);
        map.put("eventTimestamp", eventTimestamp);
        map.put("ignoreExpired", ignoreExpired);
        if( appendix != null && appendix != "" ){
            map.put("appendix", appendix);
        }

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<String, String>();
        requestMap.add("params", JSON.toJSON(map).toString());

        HttpHeaders httpHeaders = this.create(businessCode);
        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<MultiValueMap<String, String>>(requestMap, httpHeaders);
        ResponseEntity<?> responseEntity = null;

        long start = System.currentTimeMillis();
        try {
            logger.info("Http Request - [" + OCP_ACCOUNT_CHARGE_URL+ "], " + request);
            responseEntity = restTemplate.postForEntity(OCP_ACCOUNT_CHARGE_URL, request, Object.class);
            logger.info("Http Response - [" + OCP_ACCOUNT_CHARGE_URL + "], " + responseEntity );
        } catch (RestClientException e) {
            logger.error("Error: " + e.getMessage());
            throw new SystemException("RestClientException  :  " + e.getMessage());
        } finally {
            long end = System.currentTimeMillis();
            logger.info("sncp-account.charge接口耗时情况,开始时间: " + start + ",结束时间: " + end + ",耗时: " + (end-start) + ",账号ID:" + userId);
        }
        Object object = responseEntity.getBody();

        OcpAccountDTO ocpAccountDTO = this.convertImprestChargeResponse(object);

        return ocpAccountDTO;
    }


    /**
     * create HttpHeaders
     * @param businessCode
     * @return
     */
    private HttpHeaders create(String businessCode){
        HttpHeaders httpHeader = new HttpHeaders();
        httpHeader.set("H_APPID", H_APPID);
        httpHeader.set("H_PWD", H_PWD);
        httpHeader.set("H_CBC", businessCode);
        httpHeader.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return httpHeader;
    }

    /**
     * convert response
     * @param obj
     * @return
     */
    private OcpAccountDTO convertResponse(Object obj) {
        OcpAccountBanlanceNormalResponseDTO normalResponseDTO = null;
        OcpAccountErrorResponseDTO errorResponseDTO = null;
        if( obj != null ){
            if( obj instanceof Map ){
                Map<String, Object> returnMap = (Map<String, Object>)obj;
                if( returnMap.get("STATE") != null && returnMap.get("STATE").toString().equals("0") ){
                    normalResponseDTO = new DozerBeanMapper().map(returnMap, OcpAccountBanlanceNormalResponseDTO.class);
                    return normalResponseDTO;
                }else{
                    errorResponseDTO = new DozerBeanMapper().map(returnMap, OcpAccountErrorResponseDTO.class);
                    return errorResponseDTO;
                }
            }
        }
        return null;
    }

    /**
     * Convert EasyImprest and EasyCharge Response
     * @param obj
     * @return
     */
    private OcpAccountDTO convertEasyImprestChargeResponse(Object obj){
        EasyImprestChargeNormalResponseDTO normalResponseDTO = null;
        OcpAccountErrorResponseDTO errorResponseDTO = null;
        if( obj != null ){
            if( obj instanceof Map ){
                Map<String, Object> returnMap = (Map<String, Object>)obj;
                if( returnMap.get("STATE") != null && returnMap.get("STATE").toString().equals("0") ){
                    normalResponseDTO = new DozerBeanMapper().map(returnMap, EasyImprestChargeNormalResponseDTO.class);
                    return normalResponseDTO;
                }else{
                    errorResponseDTO = new DozerBeanMapper().map(returnMap, OcpAccountErrorResponseDTO.class);
                    return errorResponseDTO;
                }
            }
        }
        return null;
    }

    /**
     *Convert Imprest and Charge Response
     * @param object
     * @return
     */
    private OcpAccountDTO convertImprestChargeResponse(Object object) {
        ImprestChargeNormalResponseDTO normalResponseDTO = null;
        OcpAccountErrorResponseDTO errorResponseDTO = null;
        if( object != null ){
            if( object instanceof Map ){
                Map<String, Object> returnMap = (Map<String, Object>)object;
                if( returnMap.get("STATE") != null && returnMap.get("STATE").toString().equals("0") ){
                    normalResponseDTO = new DozerBeanMapper().map(returnMap, ImprestChargeNormalResponseDTO.class);
                    return normalResponseDTO;
                }else{
                    errorResponseDTO = new DozerBeanMapper().map(returnMap, OcpAccountErrorResponseDTO.class);
                    return errorResponseDTO;
                }
            }
        }
        return null;
    }

}
