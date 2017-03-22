package com.woniu.sncp.pay.common.utils.http;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;

import com.woniu.sncp.properties.ConfigurableConstants;


/**
 * ip地址转换工具类
 * 
 * @author Wang Yuxing, Sun Xiaochen
 */
public class IpUtils extends ConfigurableConstants {
	private static String IP = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
	
	private static String LONG_IP = "(((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))[.](((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))[.](((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))[.]((((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))([-](((2[0-4]\\d)|(25[0-5]))|(1\\d{2})|([1-9]\\d)|(\\d))){0,1}([/]((3[0-2])|([1-2]\\d)|(\\d))){0,1})";
	
	static {
		init("/opt/security/ip/ip.properties");
	}

	/**
	 * 经过代理以后，由于在客户端和服务之间增加了中间层，因此服务器无法直接拿到客户端的IP，<br>
	 * 服务器端应用也无法直接通过转发请求的地址返回给客户端。 但是在转发请求的HTTP头信息中，<br>
	 * 增加了X－FORWARDED－FOR信息用以跟踪原有的客户端IP地址和原来客户端请求的服务器地址。
	 * 原来如此，我们的项目中正好是有前置apache， 将一些请求转发给后端的weblogic，<br>
	 * 看来就是这样导致的。
	 * 
	 * 
	 * @param request
	 * @return 获取真实的IP
	 */
	public static String getRemoteAddr(HttpServletRequest request) {
		String ip = null;
		if (!StringUtils.isBlank(getValue("open-x-forwarded-for")) && "yes".equalsIgnoreCase(getValue("open-x-forwarded-for"))) {
			ip = request.getHeader("x-forwarded-for");
		} else if (!StringUtils.isBlank(getValue("open-Proxy-Client-IP")) && "yes".equalsIgnoreCase(getValue("open-Proxy-Client-IP"))) {
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("Proxy-Client-IP");
			}
		} else if (!StringUtils.isBlank(getValue("open-WL-Proxy-Client-IP")) && "yes".equalsIgnoreCase(getValue("open-WL-Proxy-Client-IP"))) {
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("WL-Proxy-Client-IP");
			}
		} else if (!StringUtils.isBlank(getValue("open-Other-Attract-IP"))) {
			String otherAttractIp = getValue("open-Other-Attract-IP");
			String[] otherAttractIps = otherAttractIp.split(",");
			for (String attractIp : otherAttractIps) {
				ip = request.getHeader(attractIp);
				if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
					break;
				}
			}
		}

		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		String[] ips = ip.split(",");
		if (ips.length > 1) {
			for (int i = 0; i < ips.length; i++) {
				ip = ips[i].trim();
				if(!"unknown".equalsIgnoreCase(ip)){
					break;
				}
			}
		}
		return ip;
	}

	public static boolean ipMacth(String ip) {
		if (StringUtils.isNotEmpty(ip)) {
			return ip.matches(IP);
		}
		return false;
	}
    
    /**
     * 校验ip格式是否正确,支持127.0.0.1/23和127.0.0.1-255这种格式
     * @param ip
     * @return
     */
    public static boolean ipMacthLongIp(String ip) {
		if (StringUtils.isNotEmpty(ip)) {
			return ip.matches(LONG_IP);
		}
		return false;
	}
    
	public static String getLoaclAddr() {
		Enumeration<NetworkInterface> netInterfaces = null;
		StringBuffer result = new StringBuffer();
		try {
			netInterfaces = NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				NetworkInterface ni = netInterfaces.nextElement();
				if (ni.isLoopback() || ni.isVirtual() || !ni.isUp())
					continue;
				Enumeration<InetAddress> ips = ni.getInetAddresses();
				while (ips.hasMoreElements()) {
					InetAddress ia = ips.nextElement();
					if (ia instanceof Inet6Address)
						continue;
					result.append(ia.getHostAddress()).append(",");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result.toString();
	}

	// 限制实例化
	private IpUtils() {
	}

	/**
	 * 字符串转换为数字
	 * 
	 * @param ip
	 *            ip地址
	 * @return 0 ~ 4294967295的数字，如果ip地址格式错误，返回-1
	 */
	public static long ipToLong(String ip) {
		if (ip == null || "".equals(ip.trim()))
			return -1;

		String[] segment = ip.trim().split("[.]");
		if (segment.length != 4)
			return -1;

		long[] ips = new long[4];
		for (int i = 0; i < segment.length; i++) {
			ips[i] = Integer.parseInt(segment[i]);
			if (ips[i] < 0 || ips[i] > 255)
				return -1;
		}
		return ipToLong(ips);
	}

	/**
	 * ip字符串根据点号分隔的数组转换为数字
	 * 
	 * @param ip
	 *            long类型数组，长度为4
	 * @return 数字格式的ip地址
	 */
	public static long ipToLong(long[] ip) {
		return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
	}

	/**
	 * 数字转换为字符串
	 * 
	 * @param ip
	 *            数字格式的ip
	 * @return 字符串格式的ip地址，例如127.0.0.1。如果超出范围(0 ~ 4294967295)，返回null。
	 */
	public static String longToIp(long ip) {
		if (ip < 0 || ip > 4294967295L)
			return null;

		StringBuffer sb = new StringBuffer("");
		// 直接右移24位
		sb.append(String.valueOf(ip >>> 24));
		sb.append(".");
		// 将高8位置0，然后右移16位
		sb.append(String.valueOf((ip & 0x00FFFFFF) >>> 16));
		sb.append(".");
		sb.append(String.valueOf((ip & 0x0000FFFF) >>> 8));
		sb.append(".");
		sb.append(String.valueOf(ip & 0x000000FF));

		return sb.toString();
	}

	public static String remarkIp(String ip) {
		if (StringUtils.isEmpty(ip)) {
			return "";
		}

		StringBuffer buffer = new StringBuffer();
		int len = ip.toCharArray().length;
		for (int i = 0; i < len; i++) {
			char tmp = ip.charAt(i);
			if (CharUtils.isAsciiNumeric(tmp) || tmp == '.') {
				buffer.append(tmp);
			}
		}
		return buffer.toString();
	}

	public static void main(String args[]) {
		System.out.print(IpUtils.remarkIp("0.0.0.0"));
	}

	public static StringBuffer requestInfo(HttpServletRequest request, HttpServletResponse response) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("req ip:{},url:{},param:{},cookie:{},sessionid:{},session-param:{},encode:{},content-type:{},Locale:{}");

		buffer.append("Servlet").append("\t");
		buffer.append("Servlet init parameters:");
		Enumeration<?> e = request.getSession().getServletContext().getInitParameterNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String value = request.getSession().getServletContext().getInitParameter(key);
			buffer.append(" " + key + " : " + value + " ");
		}
		buffer.append("\t");

		buffer.append("Context init parameters:");
		ServletContext context = request.getSession().getServletContext();
		e = context.getInitParameterNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			Object value = context.getInitParameter(key);
			buffer.append(" " + key + " : " + value + " ");
		}
		buffer.append("\t");

		buffer.append("Context attributes:");
		e = context.getAttributeNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			Object value = context.getAttribute(key);
			buffer.append(" " + key + " : " + value + " ");
		}
		buffer.append("\t");
		buffer.append("Request attributes:");
		e = request.getAttributeNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			Object value = request.getAttribute(key);
			buffer.append(" " + key + " : " + value + " ");
		}
		buffer.append("\t");
		buffer.append("Servlet Name: " + request.getServerName());
		buffer.append("\t");
		buffer.append("Protocol: " + request.getProtocol());
		buffer.append("\t");
		buffer.append("Scheme: " + request.getScheme());
		buffer.append("\t");
		buffer.append("Server Name: " + request.getServerName());
		buffer.append("\t");
		buffer.append("Server Port: " + request.getServerPort());
		buffer.append("\t");
		buffer.append("Server Info: " + context.getServerInfo());
		buffer.append("\t");
		buffer.append("Remote Addr: " + request.getRemoteAddr());
		buffer.append("\t");
		buffer.append("Remote Host: " + request.getRemoteHost());
		buffer.append("\t");
		buffer.append("Character Encoding: " + request.getCharacterEncoding());
		buffer.append("\t");
		buffer.append("Content Length: " + request.getContentLength());
		buffer.append("\t");
		buffer.append("Content Type: " + request.getContentType());
		buffer.append("\t");
		buffer.append("Locale: " + request.getLocale());
		buffer.append("\t");
		buffer.append("Default Response Buffer: " + response.getBufferSize());
		buffer.append("\t");
		buffer.append("Parameter names in this request:");
		e = request.getParameterNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String[] values = request.getParameterValues(key);
			buffer.append(" " + key + " : ");
			for (int i = 0; i < values.length; i++) {
				buffer.append(values[i] + " ");
			}
		}
		buffer.append("\t");
		buffer.append("Headers in this request:");
		e = request.getHeaderNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String value = request.getHeader(key);
			buffer.append(" " + key + ": " + value + " ");
		}
		buffer.append("\t");
		buffer.append("Cookies in this request:");
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				Cookie cookie = cookies[i];
				buffer.append(" " + cookie.getName() + " = " + cookie.getValue());
			}
		}
		buffer.append("\t");
		buffer.append("Request Is Secure: " + request.isSecure());
		buffer.append("\t");
		buffer.append("Auth Type: " + request.getAuthType());
		buffer.append("\t");
		buffer.append("HTTP Method: " + request.getMethod());
		buffer.append("\t");
		buffer.append("Remote User: " + request.getRemoteUser());
		buffer.append("\t");
		buffer.append("Request URI: " + request.getRequestURI());
		buffer.append("\t");
		buffer.append("Context Path: " + request.getContextPath());
		buffer.append("\t");
		buffer.append("Servlet Path: " + request.getServletPath());
		buffer.append("\t");
		buffer.append("Path Info: " + request.getPathInfo());
		buffer.append("\t");
		buffer.append("Path Trans: " + request.getPathTranslated());
		buffer.append("\t");
		buffer.append("Query String: " + (null==request.getQueryString() ? "***" : request.getQueryString()));
		buffer.append("\t");

		HttpSession session = request.getSession();
		buffer.append("Requested Session Id: " + request.getRequestedSessionId());
		buffer.append("\t");
		buffer.append("Current Session Id: " + session.getId());
		buffer.append("\t");
		buffer.append("Session Created Time: " + session.getCreationTime());
		buffer.append("\t");
		buffer.append("Session Last Accessed Time: " + session.getLastAccessedTime());
		buffer.append("\t");
		buffer.append("Session Max Inactive Interval Seconds: " + session.getMaxInactiveInterval());
		buffer.append("\t");
		buffer.append("Session values: ");
		e = session.getAttributeNames();
		while (e.hasMoreElements()) {
			String name = (String) e.nextElement();
			buffer.append(" " + name + " : " + session.getAttribute(name) + " ");
		}
		buffer.append("\t");
		return buffer;
	}
}