/**
 * <p>Copyright (c) Snail Game 2016</p>
 */
package com.woniu.sncp.account.dto;

import java.io.Serializable;

/**
 * @Title: OcpAccountDTO
 * @Description:
 * @Author zhujing
 * @Date 2016/7/11
 * @Version V1.0
 */
public class OcpAccountDTO implements Serializable{

    private static final long serialVersionUID = -8167391824528181205L;

    private String STATE;

    public String getSTATE() {
        return STATE;
    }

    public void setSTATE(String STATE) {
        this.STATE = STATE;
    }

    @Override
    public String toString() {
        final String TAB = ",";
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("\"").append("STATE").append("\"").append(":").append("\"").append(this.STATE).append("\"").append(TAB);

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
