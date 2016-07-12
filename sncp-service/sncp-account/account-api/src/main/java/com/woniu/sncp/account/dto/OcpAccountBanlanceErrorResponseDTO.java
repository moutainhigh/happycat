/**
 * <p>Copyright (c) Snail Game 2016</p>
 */
package com.woniu.sncp.account.dto;

/**
 * @Title: OcpAccountBanlanceErrorResponseDTO
 * @Description:
 * @Author zhujing
 * @Date 2016/7/11
 * @Version V1.0
 */
public class OcpAccountBanlanceErrorResponseDTO extends OcpAccountDTO {

    private static final long serialVersionUID = -7089599417068375644L;

    private String CODE;

    private String DESC;

    public String getCODE() {
        return CODE;
    }

    public String getDESC() {
        return DESC;
    }

    public void setCODE(String CODE) {
        this.CODE = CODE;
    }

    public void setDESC(String DESC) {
        this.DESC = DESC;
    }

    @Override
    public String toString() {
        final String TAB = ",";
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("\"").append("CODE").append("\"").append(":").append("\"").append(this.CODE).append("\"").append(TAB);
        sb.append("\"").append("DESC").append("\"").append(":").append("\"").append(this.DESC).append("\"").append(TAB);


        String superToString = super.toString();
        if( superToString != null && superToString.startsWith("{") && superToString.endsWith("}") ){
            sb.append( superToString.subSequence(1, superToString.length()) );
            return sb.toString();
        }else{
            String result = sb.substring(0, sb.length() - 1);
            result += "}";
            return result;
        }
    }
}
