package com.woniu.sncp.cbss.core.util;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class CommonMethod {
	/**
	 * 判断字符串是否是数字
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		System.out.println(decodeString("544553545F32303132303431345F2D393534333538383231"));
		System.out.println(enCodeString("congchang_mao_11111"));
		System.out.println(enCodeString("WNTEST2016"));
	}

	/**
	 * 判断字符串是否是16进制数字
	 * 
	 * @param str
	 * @return
	 */
	public static boolean is16Numeric(String str) {
		Pattern pattern = Pattern.compile("[0-9A-Fa-f]*");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}

	/**
	 * 将16进制转化为GB2312编码字符串
	 * 
	 * @param str
	 * @return
	 */
	public static String HexToStr(String HexStr) {
		String result = "";
		if (HexStr.length() % 2 != 0 || !is16Numeric(HexStr)) {
			return result;
		}
		byte b[] = new byte[HexStr.length() / 2];
		for (int i = 0; i < HexStr.length() / 2; i++) {
			b[i] = (byte) Integer.parseInt(HexStr.substring(i * 2, i * 2 + 2), 16);
		}
		try {
			result = new String(b, "GBK");
		} catch (UnsupportedEncodingException e) {

			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 将字符串以GB2312编码转成16进制
	 * 
	 * @param str
	 * @return
	 */
	public static String StrToHex(String HexStr) {
		StringBuffer stringBuffer = new StringBuffer();
		try {
			byte[] b = HexStr.getBytes("GBK");
			for (int i = 0; i < b.length; i++) {
				String stmp = Integer.toHexString(b[i] & 0xFF);
				if (stmp.length() == 1) {
					stringBuffer.append("0");
					stringBuffer.append(stmp);
				} else {
					stringBuffer.append(stmp);
				}
			}
		} catch (UnsupportedEncodingException e) {

			e.printStackTrace();
		}
		return stringBuffer.toString().toUpperCase();
	}

	public static String enCodeString(String src) throws UnsupportedEncodingException { // 加码函数，将系统用到的控制符变成转义符号
		return enCode(src.getBytes("GBK"));
	}

	public static String decodeString(String src) throws UnsupportedEncodingException {
		return new String(deCode(src), "GBK");
	}

	public static String enCode(byte[] bsrc) { // 加码函数，将系统用到的控制符变成转义符号
		String dest = "", str;
		byte bb;
		int num;
		if (bsrc == null) {
			return "";
		}
		for (int ii = 0; ii < bsrc.length; ii++) {
			bb = bsrc[ii];
			if (bb >= 0) {
				num = bb;
			} else {
				num = (bb & 0x7F) + (1 << 7);
			}
			str = Integer.toHexString(num);
			if (str.length() < 2) {
				str = "0" + str;
			}
			dest += str.toUpperCase();
		}
		return dest;
	}

	public static byte[] deCode(String src) { // 还原

		if (src.length() < 2 || src.length() % 2 != 0 || !is16Numeric(src)) {
			return new byte[0];
		}
		byte[] dest = new byte[src.length() / 2];
		byte rb;
		String str;
		Arrays.fill(dest, (byte) 0);
		int index = 0;
		for (int ii = 0; ii < src.length() - 1; ii++) {
			str = "#" + src.substring(ii, ii + 2);
			rb = (byte) Integer.decode(str).intValue();
			dest[index++] = rb;
			ii++;
		}
		return dest;
	}

	// 转换充值卡卡号。
	public static String translate(String cardNo) throws NumberFormatException {
		String substr = cardNo.substring(5, 9);
		String s = "abcdefghmj";
		if (StringUtils.isNumeric(substr)) {
			String month = substr.substring(2, 4);
			return "20" + substr.substring(0, 2) + "_" + Integer.parseInt(month);
		} else {
			String convertedSubstr = "";
			for (char c : substr.toCharArray()) {
				convertedSubstr += s.indexOf(c);
			}
			String month = convertedSubstr.substring(2, 4);
			return "20" + convertedSubstr.substring(0, 2) + "_" + Integer.parseInt(month);
		}
	}

}
