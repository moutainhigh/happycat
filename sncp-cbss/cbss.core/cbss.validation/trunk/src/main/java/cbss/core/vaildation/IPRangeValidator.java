package cbss.core.vaildation;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

/**
 * IP范围校验
 * 
 * @author yang.hao
 * @since 2011-11-14 下午1:38:38
 */
public class IPRangeValidator {

	/**
	 * IP列表校验，看IP范围是否包含此IP
	 * 
	 * @param ipRange
	 *            格式类似192.168.252.15-255,192.168.11.1
	 * @param ip
	 * @return
	 */
	public static boolean isValid(String ipRange, String ip) {
		String[] split = StringUtils.split(ipRange, ",");
		for (String singleIpRange : split) {
			if (singleValid(singleIpRange, ip)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isValid(List<String> ipRange, String ip) {
		for (String singleIpRange : ipRange) {
			if (singleValid(singleIpRange, ip)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 单个IP范围校验
	 * 
	 * @param ipRange
	 *            格式类似192.168.252.15-255 或者 192.168.11.1
	 * @param ip
	 * @return
	 */
	private static boolean singleValid(String ipRange, String ip) {
		if (StringUtils.isBlank(ipRange) || StringUtils.isBlank(ip)) {
			return false;
		}
		// 两者相同
		if (ip.equals(ipRange)) {
			return true;
		}
		if (ipRange.indexOf('/') > 0) {
			IpAddressMatcher ipAddressMatcher = new IpAddressMatcher(ipRange);
			boolean result = ipAddressMatcher.matches(ip);
			return result;
		}
		// 前缀判断
		String ipRangePrefix = StringUtils.substringBeforeLast(ipRange, ".");
		String ipPrefix = StringUtils.substringBeforeLast(ip, ".");
		if (!StringUtils.equals(ipRangePrefix, ipPrefix)) {
			return false;
		}
		// 后缀范围判断
		String ipRangeSuffix = StringUtils.substringAfterLast(ipRange, ".");
		String ipSuffix = StringUtils.substringAfterLast(ip, ".");
		String[] split = StringUtils.split(ipRangeSuffix, "-");
		if (split.length == 2 && StringUtils.isNotBlank(split[0]) && StringUtils.isNotBlank(split[1])) {
			int _ip = NumberUtils.toInt(ipSuffix);
			int floor = NumberUtils.toInt(split[0]);
			int ceiling = NumberUtils.toInt(split[1]);
			if (_ip >= floor && _ip <= ceiling) {
				return true;
			}
		}
		return false;
	}
}
