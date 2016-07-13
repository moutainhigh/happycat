/**
 * <p>Copyright (c) Snail Game 2016</p>
 */
package com.woniu.sncp.account.dto;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;

/**
 * @Title: EasyImprestNormalResponseDTO
 * @Description:
 * @Author zhujing
 * @Date 2016/7/13
 * @Version V1.0
 */
public class EasyImprestChargeNormalResponseDTO extends OcpAccountDTO {

    private static final long serialVersionUID = 864291249578881993L;

    private Map<String, Object> APPENDIX = new HashMap<String, Object>();

    private String appId;

    private String areaId;

    private String payTypeId;

    private String amt;

    private String orderNo;

    private String userId;

    public Map<String, Object> getAPPENDIX() {
        return APPENDIX;
    }

    public String getAppId() {
        return appId;
    }

    public String getAreaId() {
        return areaId;
    }

    public String getPayTypeId() {
        return payTypeId;
    }

    public String getAmt() {
        return amt;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public String getUserId() {
        return userId;
    }

    public void setAPPENDIX(Map<String, Object> APPENDIX) {
        this.APPENDIX = APPENDIX;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public void setPayTypeId(String payTypeId) {
        this.payTypeId = payTypeId;
    }

    public void setAmt(String amt) {
        this.amt = amt;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        Object APPENDIX_JSON = null;
        if( !APPENDIX.isEmpty() ) {
            APPENDIX_JSON = JSON.toJSON(APPENDIX);
        }

        final String TAB = ",";
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("\"").append("APPENDIX").append("\"").append(":").append("\"").append(APPENDIX_JSON).append("\"").append(TAB);
        sb.append("\"").append("appId").append("\"").append(":").append("\"").append(this.appId).append("\"").append(TAB);
        sb.append("\"").append("areaId").append("\"").append(":").append("\"").append(this.areaId).append("\"").append(TAB);
        sb.append("\"").append("payTypeId").append("\"").append(":").append("\"").append(this.payTypeId).append("\"").append(TAB);
        sb.append("\"").append("amt").append("\"").append(":").append("\"").append(this.amt).append("\"").append(TAB);
        sb.append("\"").append("orderNo").append("\"").append(":").append("\"").append(this.orderNo).append("\"").append(TAB);
        sb.append("\"").append("userId").append("\"").append(":").append("\"").append(this.userId).append("\"").append(TAB);

        String superToString = super.toString();
        if( superToString != null && superToString.startsWith("{") && superToString.endsWith("}") ){
            sb.append( superToString.subSequence(1,superToString.length()) );
            return sb.toString();
        }else{
            String result = sb.substring(0, sb.length() - 1);
            result += "}";
            return result;
        }
    }
}
