/**
 * <p>Copyright (c) Snail Game 2016</p>
 */
package com.woniu.sncp.account.dto;

import java.io.Serializable;

/**
 * @Title: PayInfo
 * @Description:
 * @Author zhujing
 * @Date 2016/7/7
 * @Version V1.0
 */
public class PayInfo implements Serializable {

    private static final long serialVersionUID = 8846652075393367641L;

    private String payTypeId; // 货币类型
    private String payTypeName; //对应货币
    private String amount; // 金额
    private String endTime;  //2015.06.08 add zhujing 货币到期时间
    private String totalAmt; //2015.06.16 add zhujing 货币充值总金额



    public PayInfo(String payTypeId, String amount) {
        this.payTypeId = payTypeId;
        this.amount = amount;
    }

    public PayInfo(String payTypeId, String payTypeName, String amount, String endTime, String totalAmt) {
        this.payTypeId = payTypeId;
        this.payTypeName = payTypeName;
        this.amount = amount;
        this.endTime = endTime;
        this.totalAmt = totalAmt;
    }

    public PayInfo() {

    }

    public String getPayTypeId() {
        return payTypeId;
    }

    public void setPayTypeId(String payTypeId) {
        this.payTypeId = payTypeId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getPayTypeName() {
        return payTypeName;
    }

    public void setPayTypeName(String payTypeName) {
        this.payTypeName = payTypeName;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getTotalAmt() {
        return totalAmt;
    }

    public void setTotalAmt(String totalAmt) {
        this.totalAmt = totalAmt;
    }

    @Override
    public String toString() {
        payTypeId = payTypeId == "" ? "" : payTypeId;
        payTypeName = payTypeName == "" ? "" : payTypeName;
        amount = amount == "" ? "" : amount;
        endTime = endTime == "" ? "" : endTime;
        totalAmt = totalAmt == "" ? "" : totalAmt;

        final String TAB = ",";
        StringBuffer retValue = new StringBuffer();
        retValue.append("{");
        retValue.append("\"").append("payTypeId").append("\"").append(":").append("\"").append(this.payTypeId).append("\"").append(TAB);
        retValue.append("\"").append("payTypeName").append("\"").append(":").append("\"").append(this.payTypeName).append("\"").append(TAB);
        retValue.append("\"").append("amount").append("\"").append(":").append("\"").append(this.amount).append("\"").append(TAB);
        retValue.append("\"").append("endTime").append("\"").append(":").append("\"").append(this.endTime).append("\"").append(TAB);
        retValue.append("\"").append("totalAmt").append("\"").append(":").append("\"").append(this.totalAmt).append("\"").append(TAB);

        String superToString = super.toString();
        if(superToString != null && superToString.startsWith("{") && superToString.endsWith("}")) {
            retValue.append(superToString.subSequence(1, superToString.length()));
            return retValue.toString();
        } else {
            String result = retValue.substring(0, retValue.length() - 1);
            result += "}";
            return result;
        }
    }

}
