package com.woniu.sncp.account.service;

import com.woniu.sncp.account.dto.OcpAccountDTO;

import java.util.List;

/**
 * @Title: AccountService
 * @Description:
 * @Author zhujing
 * @Date 2016/7/7
 * @Version V1.0
 */
public interface AccountService {

    /**
     * 余额查询微服务
     * @param userId
     * @param spId
     * @param appId
     * @param areaId
     * @param sessionId
     * @param payTypeId
     * @param eventTimestamp
     * @param appendix
     * @param businessCode
     * @return
     */
    public OcpAccountDTO queryBalanceMicroService(Long userId, Integer spId, String appId, String areaId, String sessionId,
                                                  String payTypeId, String eventTimestamp, Object appendix, String businessCode);

    /**
     * 简易充值微服务
     * @param userId
     * @param appId
     * @param areaId
     * @param payTypeId
     * @param price
     * @param amt
     * @param orderNo
     * @param appendix
     * @param businessCode
     * @return
     */
    public OcpAccountDTO easyImprestMicroService(Long userId, String appId, String areaId, String payTypeId, String price,
                                                 String amt, String orderNo, Object appendix, String businessCode);

    /**
     * 简易扣费微服务
     * @param userId
     * @param appId
     * @param areaId
     * @param payTypeId
     * @param amt
     * @param orderNo
     * @param appendix
     * @param businessCode
     * @return
     */
    public OcpAccountDTO easyChargeMicroService(Long userId, String appId, String areaId, String payTypeId, String amt,
                             String orderNo, Object appendix, String businessCode);

    /**
     * 核心充值微服务
     * @param userId
     * @param appId
     * @param areaId
     * @param sessionId
     * @param orderNo
     * @param payTypeId
     * @param amt
     * @param price
     * @param endTime
     * @param depositTime
     * @param eventTimestamp
     * @param appendix
     * @param businessCode
     * @return
     */
    public OcpAccountDTO imprestMicroService(Long userId, String appId, String areaId, String sessionId, String orderNo,
                                             String payTypeId, String amt, String price, String endTime, String depositTime,
                                             String eventTimestamp, Object appendix, String businessCode);

    /**
     * 直接扣费微服务
     * @param userId
     * @param spId
     * @param appId
     * @param areaId
     * @param sessionId
     * @param orderNo
     * @param payInfo
     * @param itemNum
     * @param itemInfo
     * @param eventTimestamp
     * @param ignoreExpired
     * @param appendix
     * @param businessCode
     * @return
     */
    public OcpAccountDTO chargeMicroService(Long userId, int spId, String appId, String areaId, String sessionId, String orderNo,
                                     Object payInfo, String itemNum, List itemInfo, String eventTimestamp, Boolean ignoreExpired,
                                     Object appendix, String businessCode);
}
