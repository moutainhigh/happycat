package com.woniu.sncp.account.service;

import com.woniu.sncp.account.dto.OcpAccountDTO;

/**
 * @Title: AccountService
 * @Description:
 * @Author zhujing
 * @Date 2016/7/7
 * @Version V1.0
 */
public interface AccountService {

    public OcpAccountDTO queryBalance(Long userId, Integer spId, String appId, String areaId, String sessionId,
                                                  String payTypeId, String eventTimestamp, Object appendix, String businessCode);

    public OcpAccountDTO easyImprest(Long userId, String appId, String areaId, String payTypeId, String price, String amt,
                              String orderNo, Object appendix, String businessCode);
}
