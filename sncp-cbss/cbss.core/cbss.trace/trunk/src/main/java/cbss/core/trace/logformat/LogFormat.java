package cbss.core.trace.logformat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import cbss.core.trace.aspect.listener.Trace;
import cbss.core.util.IpUtils;

@Component
@ConfigurationProperties(prefix = "cbss.api.forbiLog.conf", locations = { "classpath:forbiLog.properties" })
public class LogFormat {
	private static Logger logger = LoggerFactory.getLogger(LogFormat.class);
	public List<String> forbiMethodInfos;
	public List<String> forbiAttrInfos;
	public List<String> forbiKeys;
	public List<String> newlinereplces;

	@Autowired
	private Trace trace;

	public List<String> getForbiMethodInfos() {
		return forbiMethodInfos;
	}

	public void setForbiMethodInfos(List<String> forbiMethodInfos) {
		this.forbiMethodInfos = forbiMethodInfos;
	}

	public List<String> getForbiAttrInfos() {
		return forbiAttrInfos;
	}

	public void setForbiAttrInfos(List<String> forbiAttrInfos) {
		this.forbiAttrInfos = forbiAttrInfos;
	}

	public List<String> getForbiKeys() {
		return forbiKeys;
	}

	public void setForbiKeys(List<String> forbiKeys) {
		this.forbiKeys = forbiKeys;
	}

	public List<String> getNewlinereplces() {
		return newlinereplces;
	}

	public void setNewlinereplces(List<String> newlinereplces) {
		this.newlinereplces = newlinereplces;
	}

	public boolean isRecordSecurityLog(String key) {
		if (key == null) {
			return false;
		}
		for (String forbiKey : forbiMethodInfos) {
			if (!forbiKey.isEmpty() && key.indexOf(forbiKey) >= 0) {
				return true;
			}
		}
		for (String forbiKey : forbiAttrInfos) {
			if (!forbiKey.isEmpty() && key.indexOf(forbiKey) >= 0) {
				return true;
			}
		}
		return false;
	}

	public boolean isRecordNoLog(String key) {
		if (key == null) {
			return false;
		}
		for (String forbiKey : forbiKeys) {
			if (!forbiKey.isEmpty() && key.indexOf(forbiKey) >= 0) {
				return true;
			}
		}
		return false;
	}

	public boolean isNewlinereplcesLog(String key) {
		if (key == null) {
			return false;
		}
		for (String newlinereplce : newlinereplces) {
			if (!newlinereplce.isEmpty() && key.indexOf(newlinereplce) >= 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 参数格式如下(前3个为log4j自带的值,需要在log4j的配置文件中修改):
	 * 时间,日志级别,日志方法,自定义消息,监控类名,监控类方法,参数,方法执行开始时间,执行花费时间,请求url,请求ip,异常消息
	 * 
	 * @param output
	 *            是否需要打印日志
	 * @return
	 */
	public String format(String customHead, String fullClassName, String method, String params, String startTime, String timeDifference, String url, String ip, String errorMsg, boolean output) {
		return format(customHead, true, fullClassName, method, params, startTime, timeDifference, url, ip, errorMsg, output);
	}

	/**
	 * 
	 * 参数格式如下(前3个为log4j自带的值,需要在log4j的配置文件中修改):
	 * 时间,日志级别,日志方法,自定义消息,监控类名,监控类方法,参数,方法执行开始时间,执行花费时间,请求url,请求ip,异常消息
	 * 
	 * @param appendHead
	 *            是否需要拼接自定义消息(customHead)
	 * @param output
	 *            是否需要打印日志
	 * @return
	 */
	public String format(String customHead, boolean appendHead, String fullClassName, String method, String params, String startTime, String timeDifference, String url, String ip, String errorMsg,
			boolean output) {

		Map<String, Object> tracelog = new HashMap<String, Object>();

		StringBuffer loggerBuffer = new StringBuffer();
		if (appendHead) {
			tracelog.put("customkey", format(customHead));
			loggerBuffer.append(tracelog.get("customkey"));
			loggerBuffer.append("\t");
		}

		boolean isNewlinereplcesLog = isNewlinereplcesLog(method);
		tracelog.put("fullClassName", (format(fullClassName)));
		tracelog.put("method", format(method));
		tracelog.put("params", isRecordSecurityLog(method) ? "***" : (isNewlinereplcesLog ? formatNewLine(params) : format(params)));
		tracelog.put("startTime", format(startTime));
		tracelog.put("timeDifference", (isNewlinereplcesLog ? formatNewLine(timeDifference) : format(timeDifference)));
		tracelog.put("url", isNewlinereplcesLog ? formatNewLine(url) : format(url));
		tracelog.put("ip", format(ip));
		tracelog.put("errorMsg", isRecordSecurityLog(errorMsg) ? "***" : (isNewlinereplcesLog ? formatNewLine(errorMsg) : format(errorMsg)));
		tracelog.put("pidinfo", format(IpUtils.getLoaclAddr()));

		loggerBuffer.append(tracelog.get("fullClassName"));
		loggerBuffer.append("\t");
		loggerBuffer.append(tracelog.get("method"));
		loggerBuffer.append("\t");
		loggerBuffer.append(tracelog.get("params"));
		loggerBuffer.append("\t");
		loggerBuffer.append(tracelog.get("startTime"));
		loggerBuffer.append("\t");
		loggerBuffer.append(tracelog.get("timeDifference"));
		loggerBuffer.append("\t");
		loggerBuffer.append(tracelog.get("url"));
		loggerBuffer.append("\t");
		loggerBuffer.append(tracelog.get("ip"));
		loggerBuffer.append("\t");
		loggerBuffer.append(tracelog.get("errorMsg"));
		loggerBuffer.append("\t");
		loggerBuffer.append(tracelog.get("pidinfo"));

		if (output) {
			logger.debug(loggerBuffer.toString());
		}

		trace.trace(tracelog);

		return loggerBuffer.toString();
	}

	private static String formatNewLine(String params) {
		return StringUtils.isBlank(params) ? "-" : StringUtils.replace(StringUtils.replace(StringUtils.replace(params, "\t", "|-|"), "\r\n", "-||"), "\n", "||-");
	}

	private static String format(String params) {
		return StringUtils.isBlank(params) ? "-" : params.trim();
	}

}
