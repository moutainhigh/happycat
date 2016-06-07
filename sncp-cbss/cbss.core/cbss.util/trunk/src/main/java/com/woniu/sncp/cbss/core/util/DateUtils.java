package com.woniu.sncp.cbss.core.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.time.DateFormatUtils;

/**
 * 日期操作工具类
 * 
 * @author Sun Xiaochen
 * @since 2008-2-2
 */
public class DateUtils {
	public static final String DATE_DF = "yyyy-MM-dd";
	public static final String TIMESTAMP_DF = "yyyy-MM-dd HH:mm:ss";
	public static final String TIMESTAMP_DM = "MM-dd HH:mm:ss";
	public static final String TIMESTAMP_MS = "yyyy-MM-dd HH:mm:ss:SSS";

	// 限制实例化
	private DateUtils() {
	}

	public static String format(Date date, String strFomate) {
		SimpleDateFormat df = new SimpleDateFormat(strFomate);
		return df.format(date);
	}

	/**
	 * String -> Calendar，使用默认的日期格式{@link #TIMESTAMP_DF}解析
	 * 
	 * @param strDate
	 *            待解析的字符串
	 * @return Calendar对象
	 */
	public static Calendar parse(String strDate) {
		return parse(strDate, null);
	}

	/**
	 * String -> Calendar
	 * 
	 * @param strDate
	 *            待解析的字符串
	 * @param strFomate
	 *            格式
	 * @return Calendar对象
	 */
	public static Calendar parse(String strDate, String strFomate) {
		Date date = parseDate(strDate, strFomate);
		if (date == null)
			return null;

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

	/**
	 * String -> Date，使用默认的日期格式{@link #TIMESTAMP_DF}解析
	 * 
	 * @param strDate
	 *            ，strDate 待解析的字符串
	 * @return Date对象
	 */
	public static Date parseDate(String strDate) {
		return parseDate(strDate, null);
	}

	/**
	 * String -> Date
	 * 
	 * @param strDate
	 *            待解析的字符串
	 * @param strFomate
	 *            格式
	 * @return Date对象
	 */
	public static Date parseDate(String strDate, String strFomate) {
		if (strFomate == null)
			strFomate = TIMESTAMP_DF;
		SimpleDateFormat df = new SimpleDateFormat(strFomate);

		return df.parse(strDate, new ParsePosition(0));
	}

	/**
	 * @return 今天的日期，格式为{@link #DATE_DF}
	 */
	public static String getToday() {
		return DateFormatUtils.format(new Date(), DATE_DF);
	}

	/**
	 * @return 当前时间(精确到秒)
	 */
	public static String getTodayTime() {
		return DateFormatUtils.format(new Date(), "yyMMddHHmmss");
	}

	/**
	 * @return 当前yyyy-MM-dd HH:mm:ss时间(精确到秒)
	 */
	public static String getTodayLongTime() {
		return DateFormatUtils.format(new Date(), TIMESTAMP_DF);
	}

	/**
	 * @return 昨天的日期，格式为{@link #DATE_DF}
	 */
	public static String getYesterday() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);

		return DateFormatUtils.format(cal.getTime(), DATE_DF);
	}

	/**
	 * 得到和当前日期的间隔天数
	 * 
	 * @param returnDate
	 *            比较的时间
	 * @return 天数
	 */
	public static int daysBetween(Date returnDate) {
		return daysBetween(null, returnDate);
	}

	/**
	 * 得到两个日期之间的间隔天数
	 * 
	 * @param now
	 *            当前时间
	 * @param returnDate
	 *            比较的时间
	 * @return 天数
	 */
	public static int daysBetween(Date now, Date returnDate) {
		if (returnDate == null)
			return 0;

		Calendar cNow = Calendar.getInstance();
		Calendar cReturnDate = Calendar.getInstance();
		if (now != null) {
			cNow.setTime(now);
		}
		cReturnDate.setTime(returnDate);
		setTimeToMidnight(cNow);
		setTimeToMidnight(cReturnDate);
		long nowMs = cNow.getTimeInMillis();
		long returnMs = cReturnDate.getTimeInMillis();
		return millisecondsToDays(nowMs - returnMs);
	}

	/**
	 * 时间差
	 * 
	 * @param startTime
	 *            开始时间
	 * @param endTime
	 *            结束时间
	 * @return 返回时间差数组:(天,时:分:秒)
	 */
	public static Object[] timeDifference(Date startTime, Date endTime) {
		if (startTime == null || endTime == null) {
			return new Object[] { 0, 0, 0, 0 };
		} else {
			Calendar start = Calendar.getInstance();
			Calendar end = Calendar.getInstance();
			start.setTime(startTime);
			end.setTime(endTime);
			long startMs = start.getTimeInMillis();
			long endMs = end.getTimeInMillis();
			long l_differ = endMs - startMs;// 毫秒数
			long s_differ = l_differ / 1000;// 秒
			long d_differ = s_differ / (60 * 60 * 24);// 得到天数
			s_differ = s_differ - d_differ * 60 * 60 * 24;// 天
			long h_differ = s_differ / (60 * 60);// 时
			s_differ = s_differ - h_differ * 60 * 60;
			long m_differ = s_differ / 60;// 分
			s_differ = s_differ - m_differ * 60;
			return new Object[] { d_differ, h_differ, m_differ, s_differ };
		}
	}

	/**
	 * 取得当月份的第一天，小时、分、秒、毫秒都归零
	 * 
	 * @param date
	 *            日期
	 * @return cal
	 */
	public static Calendar getMonthBeginTime(Date date) {
		Calendar cal = Calendar.getInstance();
		if (date != null) {
			cal.setTime(date);
		}
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal;
	}

	/**
	 * 取得当前月的最后一天，小时、分、秒、毫秒都归零
	 * 
	 * @param date
	 *            日期
	 * @return cal
	 */
	public static Calendar getMonthEndTime(Date date) {
		Calendar cal = Calendar.getInstance();
		if (date != null) {
			cal.setTime(date);
		}
		cal.add(Calendar.MONTH, 1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.DATE, -1);

		return cal;
	}

	/**
	 * 取得一天开始的时间，即凌晨
	 * 
	 * @param date
	 *            日期
	 * @return cal
	 */
	public static Calendar getDayBeginTime(Date date) {
		Calendar cal = Calendar.getInstance();
		if (date != null) {
			cal.setTime(date);
		}
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}

	/**
	 * 取得一天结束的时间，即午夜12点
	 * 
	 * @param date
	 *            日期
	 * @return cal
	 */
	public static Calendar getDayEndTime(Date date) {
		Calendar cal = Calendar.getInstance();
		if (date != null) {
			cal.setTime(date);
		}
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 59);

		return cal;
	}

	// 将long换算成天数
	private static int millisecondsToDays(long intervalMs) {
		return (int) (intervalMs / (1000 * 86400));
	}

	// 将时间设到午夜
	private static void setTimeToMidnight(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
	}

	/**
	 * 取得今天的下一天,即明天的时间
	 * 
	 * @param today
	 * @return
	 */
	public static Date getNextDay(Date today) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(today);
		calendar.add(Calendar.DATE, 1);
		Date nextDay = calendar.getTime();
		return nextDay;
	}

	/**
	 * 将时间转化成数据库时间，精确到秒
	 * 
	 * @param strDate
	 *            时间字符串
	 * @param strFomate
	 *            时间格式
	 * @return
	 */
	public static java.sql.Timestamp getTimestamp(String strDate, String strFomate) {
		Calendar parse = parse(strDate, strFomate);
		return new java.sql.Timestamp(parse.getTimeInMillis());
	}

	/**
	 * 取得向前某个月的第一天。
	 * 
	 * @param monthAgo
	 * @return
	 */
	public static Date getFirstDayOfMonthAgo(int monthAgo) {
		Calendar time = Calendar.getInstance();
		time.add(Calendar.MONTH, 0 - monthAgo);
		time.set(Calendar.DATE, 1);
		time.set(Calendar.HOUR_OF_DAY, 0);
		time.set(Calendar.MINUTE, 0);
		time.set(Calendar.SECOND, 0);
		return time.getTime();
	}

	/**
	 * 取得当前的日期。
	 * 
	 * @param monthAgo
	 * @return
	 */
	public static String getTodayShortTime() {
		return DateFormatUtils.format(new Date(), DATE_DF);
	}

	public static void main(String[] args) {
		System.out.println(getTodayShortTime());
	}

	/**
	 * 获取周期的开始和结束时间
	 * 
	 * @param startDay
	 *            开始日期
	 * @param currentDay
	 *            当期日期
	 * @param period
	 *            周期
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> getStartAndEndDay(String startDay, String currentDay, int period)
			throws Exception {
		Map<String, Object> ret = new HashMap<String, Object>();
		Date st = DateUtils.parseDate(startDay);
		Date nt = DateUtils.parseDate(currentDay);
		Calendar xx = DateUtils.parse(currentDay);
		int length = DateUtils.daysBetween(nt, st);
		int size = length % period;
		ret.put("end", DateFormatUtils.format(xx, DateUtils.DATE_DF));
		xx.add(Calendar.DAY_OF_MONTH, -size);
		ret.put("start", DateFormatUtils.format(xx, DateUtils.DATE_DF));
		return ret;
	}

	/**
	 * 计算两日期相隔多少天
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public final static int diffDayCount(Calendar start, Calendar end) {
		int result = 0;
		if (start.equals(end)) {
			return result;
		}

		if (start.get(Calendar.YEAR) == end.get(Calendar.YEAR)) {
			return end.get(Calendar.DAY_OF_YEAR) - start.get(Calendar.DAY_OF_YEAR);
		} else {
			Calendar tmp = Calendar.getInstance();
			result = end.getActualMaximum(Calendar.DAY_OF_YEAR) - start.get(Calendar.DAY_OF_YEAR);
			for (int i = start.get(Calendar.YEAR) + 1; i < end.get(Calendar.YEAR); i++) {
				tmp.set(i, tmp.get(Calendar.MONTH), tmp.get(Calendar.DAY_OF_MONTH));
				result += tmp.getActualMaximum(Calendar.DAY_OF_YEAR);
			}
			result += end.get(Calendar.DAY_OF_YEAR);
		}

		return result;
	}
}
