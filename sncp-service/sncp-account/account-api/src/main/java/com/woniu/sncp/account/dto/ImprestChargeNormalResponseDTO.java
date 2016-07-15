/**
 * <p>Copyright (c) Snail Game 2016</p>
 */
package com.woniu.sncp.account.dto;

import com.alibaba.fastjson.JSON;

/**
 * @Title: ImprestChargeNormalResponseDTO
 * @Description:
 * @Author zhujing
 * @Date 2016/7/15
 * @Version V1.0
 */
public class ImprestChargeNormalResponseDTO extends OcpAccountBanlanceNormalResponseDTO {

    private static final long serialVersionUID = 4670124053929437182L;

    private String orderId;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        final String TAB = ",";
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("\"").append("orderId").append("\"").append(":").append("\"").append(this.orderId).append("\"").append(TAB);

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

