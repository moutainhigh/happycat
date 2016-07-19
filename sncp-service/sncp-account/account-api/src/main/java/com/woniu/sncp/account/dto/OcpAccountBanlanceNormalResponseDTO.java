/**
 * <p>Copyright (c) Snail Game 2016</p>
 */
package com.woniu.sncp.account.dto;

import com.alibaba.fastjson.JSON;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Title: OcpAccountBanlanceNormalRequestDTO
 * @Description:
 * @Author zhujing
 * @Date 2016/7/11
 * @Version V1.0
 */
public class OcpAccountBanlanceNormalResponseDTO extends OcpAccountDTO {

    private static final long serialVersionUID = 7177152269623732420L;

    private List<PayInfo> balanceInfo;

    private Map<String, Object> APPENDIX = new HashMap<String, Object>();

    private String settleTime;

    public List<PayInfo> getBalanceInfo() {
        return balanceInfo;
    }

    public Map<String, Object> getAPPENDIX() {
        return APPENDIX;
    }

    public String getSettleTime() {
        return settleTime;
    }

    public void setBalanceInfo(List<PayInfo> balanceInfo) {
        this.balanceInfo = balanceInfo;
    }

    public void setAPPENDIX(Map<String, Object> APPENDIX) {
        this.APPENDIX = APPENDIX;
    }

    public void setSettleTime(String settleTime) {
        this.settleTime = settleTime;
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
        sb.append("\"").append("balanceInfo").append("\"").append(":").append("\"").append(this.balanceInfo).append("\"").append(TAB);
        sb.append("\"").append("APPENDIX").append("\"").append(":").append("\"").append(APPENDIX_JSON).append("\"").append(TAB);
        sb.append("\"").append("settleTime").append("\"").append(":").append("\"").append(this.settleTime).append("\"").append(TAB);

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
