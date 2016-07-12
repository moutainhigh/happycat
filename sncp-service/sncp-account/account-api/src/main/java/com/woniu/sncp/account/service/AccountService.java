package com.woniu.sncp.account.service;

/**
 * @Title: AccountService
 * @Description:
 * @Author zhujing
 * @Date 2016/7/7
 * @Version V1.0
 */
public interface AccountService {

    public Object queryBalance(Long userId, Integer spId, String appId, String areaId, String sessionId,
                                                  String payTypeId, String eventTimestamp, Object appendix, String businessCode);

}
