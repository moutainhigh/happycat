package org.cashier.core;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.woniu.sncp.pay.common.utils.date.DateUtils;

/**
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2017年3月28日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
public class TestCashier {

	public static void main(String[] args) throws ParseException{
		SimpleDateFormat df = new SimpleDateFormat(DateUtils.DATE_FORMAT_DATETIME_COMPACT);
		String d = "20170329140505";
		Date __timeoutExpress = DateUtils.parseDate(d);
		System.out.println(__timeoutExpress);
		System.out.println(df.parse(d));
	}
}
