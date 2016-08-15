package com.woniu.sncp.cbss.core.authorize;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.woniu.sncp.cbss.core.authorize.exception.AccessAuthorizeException;
import com.woniu.sncp.cbss.core.authorize.exception.AccessLimitException;
import com.woniu.sncp.cbss.core.errorcode.EchoInfo;
import com.woniu.sncp.cbss.core.model.access.AccessSecurityInfo;
import com.woniu.sncp.cbss.core.model.access.Limit;
import com.woniu.sncp.cbss.core.model.access.LogicRule;
import com.woniu.sncp.cbss.core.model.access.ParamLogic;
import com.woniu.sncp.cbss.core.model.access.SecurityResource;
import com.woniu.sncp.cbss.core.model.constant.NameFactory;
import com.woniu.sncp.cbss.core.model.request.access.RequestAccess;
import com.woniu.sncp.cbss.core.repository.redis.RedisService;
import com.woniu.sncp.cbss.core.signature.md5.MD5Encrypt;
import com.woniu.sncp.cbss.core.trace.aspect.listener.Trace;
import com.woniu.sncp.cbss.core.trace.logformat.LogFormat;
import com.woniu.sncp.cbss.core.util.CommonMethod;
import com.woniu.sncp.cbss.core.util.DateUtils;
import com.woniu.sncp.cbss.core.vaildation.IPRangeValidator;

@Component
public class AccessAuthorize {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AccessAuthorizeData accessAuthorizeData;
	@Autowired
	private LogFormat logFormat;
	@Autowired
	private RedisService redisService;
	@Autowired
	private Trace trace;

	/**
	 * 是否验证IP信息
	 * 
	 * @param accessType
	 * @return true为不验证
	 */
	public boolean isNotCheckIp(Long accessType) {
		return AccessSecurityInfo.S_TYPE_SNAIL_HOST_GAME.equals(accessType.toString()) || AccessSecurityInfo.S_TYPE_SNAIL_HOST_INTERFACE_NOIP_NOVAIL.equals(accessType.toString());
	}

	/**
	 * 是否验证请求方法
	 * 
	 * @param accessType
	 * @return true为不验证
	 */
	public boolean isNotCheckAccessMethod(Long accessType) {
		return AccessSecurityInfo.S_TYPE_SNAIL_COMMON.equals(accessType.toString());
	}

	/**
	 * 是否验证校验串
	 * 
	 * @param accessType
	 * @return true为不验证
	 */
	public boolean isNotCheckVailParam(Long accessType) {
		return AccessSecurityInfo.S_TYPE_SNAIL_HOST_INTERFACE.equals(accessType.toString()) || AccessSecurityInfo.S_TYPE_SNAIL_HOST_INTERFACE_NOIP_NOVAIL.equals(accessType.toString());
	}

	/**
	 * @param accessId
	 * @param accessPasswd
	 * @param accessType
	 * @param inputBuffer
	 * @param checkIp
	 * @param sVerifyStr
	 * @param isCheckIp
	 * @param uri
	 * @return true is pass ; other will be Exception
	 */
	public boolean authorize(RequestAccess requestAccess)
			throws AccessAuthorizeException {
		long time = 0, time1 = 0, time2 = 0, time3 = 0, time4 = 0, time5 = 0;
		if (logger.isTraceEnabled())
			time = System.currentTimeMillis();

		try {
			String body = requestAccess.getBody();
			String accessVerfy = requestAccess.getAccessVerify();
			if (logger.isTraceEnabled())
				time1 = System.currentTimeMillis();

			AccessSecurityInfo accessSecurityInfo = accessAuthorizeData.getAccessSecurityInfo(requestAccess.getRequestDatas().getAccessId(), requestAccess.getRequestDatas().getAccessType());
			if (null == accessSecurityInfo) {
				throw new AccessAuthorizeException(trace.traceException("access forbidden security info is null", requestAccess.getTraceState(), "[" + accessSecurityInfo + "]["
						+ requestAccess.getRequestDatas().getAccessId() + requestAccess.getRequestDatas().getAccessType() + "]"));
			}
			if (logger.isTraceEnabled())
				time2 = System.currentTimeMillis();

			if (!accessSecurityInfo.getFullName().startsWith(NameFactory.default_constant.ISDEBUG.getValue())) {
				requestAccess.setTraceState("");
			}

			if (!"1".equals(accessSecurityInfo.getState())) {
				throw new AccessAuthorizeException(trace.traceException("access forbidden", requestAccess.getTraceState(), "[need:1][actual:" + accessSecurityInfo.getState() + "]"));
			}

			if (!requestAccess.getRequestDatas().getAccessPasswd().equals(accessSecurityInfo.getPasswd())) {
				throw new AccessAuthorizeException(trace.traceException("access forbidden passwd is wrong", requestAccess.getTraceState(), ""));
			}

			String accessKey = accessSecurityInfo.getSeed();
			if (StringUtils.isBlank(accessKey)) {
				throw new AccessAuthorizeException(trace.traceException("access forbidden accesskey is null", requestAccess.getTraceState(), ""));
			}

			String writeIp = accessSecurityInfo.getLimitIp();

			if (!isNotCheckIp(requestAccess.getRequestDatas().getAccessType())) {
				String remoteIp = requestAccess.getRemoteIp();
				if (!IPRangeValidator.isValid(writeIp, requestAccess.getRemoteIp())) {
					throw new AccessAuthorizeException(trace.traceException("access forbidden IP verify error", requestAccess.getTraceState(), "[WIP:" + writeIp + "][RIP:" + remoteIp + "]"));
				}
			}

			if (logger.isTraceEnabled())
				time3 = System.currentTimeMillis();

			if (accessSecurityInfo.getStart() != null || accessSecurityInfo.getEnd() != null) {
				if (accessSecurityInfo.getStart() != null && Calendar.getInstance().getTime().before(accessSecurityInfo.getStart())) {
					throw new AccessAuthorizeException(trace.traceException("access forbidden start-time no coming on", requestAccess.getTraceState(), ""));
				}
				if (accessSecurityInfo.getEnd() != null && Calendar.getInstance().getTime().after(accessSecurityInfo.getEnd())) {
					throw new AccessAuthorizeException(trace.traceException("access forbidden more than end-time ", requestAccess.getTraceState(), ""));
				}
			}
			String bodySign = null;
			if (requestAccess.getAccessVerifyType() <= EchoInfo.SIGNATURE_TYPE_DEFAULT) {
				bodySign = body + accessSecurityInfo.getId().getId() + accessSecurityInfo.getId().getType() + accessSecurityInfo.getPasswd() + accessKey;
			}

			String charset = null;
			if (requestAccess.getRequestDatas().getOther() != null && requestAccess.getRequestDatas().getOther().getOtherFirst() != null) {
				charset = ObjectUtils.toString((requestAccess.getRequestDatas().getOther().getOtherFirst().get(NameFactory.request_otherinfo.encryptcharset.getValue())));
			}

			charset = StringUtils.isBlank(charset) ? NameFactory.default_charset.utf8.getValue() : charset;

			if (logger.isTraceEnabled())
				time4 = System.currentTimeMillis();

			String appsign = MD5Encrypt.encrypt(bodySign, charset, true);
			if (logger.isInfoEnabled()) {
				String url = requestAccess.getRequestURI();
				logger.info(url + ",beforemd5:" + bodySign + ",md5:" + appsign + ",accessVerfy:" + accessVerfy);
			}
			if (!accessVerfy.equalsIgnoreCase(appsign)) {
				throw new AccessAuthorizeException(trace.traceException("access forbidden sign verify error", requestAccess.getTraceState(), "[" + body + "][" + accessVerfy + "][" + appsign));
			}
			requestAccess.getRequestDatas().setAccessSecurityInfo(accessSecurityInfo);
			if (logger.isTraceEnabled())
				time5 = System.currentTimeMillis();
		} finally {
			if (logger.isTraceEnabled()) {
				Map<String, Object> authorize = new HashMap<String, Object>();
				String key = requestAccess.getRequestDatas().getAccessId() + "-" + requestAccess.getRequestDatas().getAccessType() + "-authorize";
				authorize.put(key, time5 - time);
				authorize.put(key + "1", time2 - time1);
				authorize.put(key + "2", time3 - time2);
				authorize.put(key + "3", time4 - time3);
				authorize.put(key + "4", time5 - time4);
				trace.trace(authorize);
			}
		}
		return true;
	}

	/**
	 * @param requestAccess
	 * @return false is pass; other will be Exception
	 */
	@SuppressWarnings("unchecked")
	public boolean limit(RequestAccess requestAccess)
			throws AccessLimitException {
		long time = 0, time1 = 0, time2 = 0, time3 = 0, time4 = 0, time5 = 0;
		if (logger.isTraceEnabled())
			time = System.currentTimeMillis();
		try {
			SecurityResource securityResource = accessAuthorizeData.getSecurityResource(requestAccess.getRequestDatas().getAccessId(), requestAccess.getRequestURI(),
					accessAuthorizeData.getMethod(requestAccess.getRequestURI()));

			if (logger.isTraceEnabled())
				time1 = System.currentTimeMillis();

			if (securityResource == null) {
				throw new AccessLimitException(trace.traceException("access fobidden resource is null", requestAccess.getTraceState(), ""));
			}

			if (!"1".equals(securityResource.getState())) {
				throw new AccessLimitException(trace.traceException("access fobidden resource state not equal 1", requestAccess.getTraceState(), ""));
			}
			AccessSecurityInfo info = requestAccess.getRequestDatas().getAccessSecurityInfo();
			String ip = requestAccess.getRemoteIp();
			String clientIp = ip;
			boolean isLimit = false;

			if (logger.isTraceEnabled())
				time2 = System.currentTimeMillis();

			if (securityResource != null && "1".equals(securityResource.getState())) {
				isLimit = hasLimitIp(info, ip, clientIp, securityResource, requestAccess.getRequestURI().toString());
			} else if (securityResource != null && "0".equals(securityResource.getState())) {
				return true;
			}

			if (logger.isTraceEnabled())
				time3 = System.currentTimeMillis();

			if (isLimit) {
				throw new AccessAuthorizeException(trace.traceException("access fobidden ip resource ", requestAccess.getTraceState(), ""));
			}
			Integer limitCode = null;
			if (!isLimit) {

				JSONObject paramdatas = JSONObject.parseObject(requestAccess.getBody());
				paramdatas.putAll(requestAccess.getRequestParamData());

				limitCode = checkPassportRequestTime(requestAccess.getSessionId(), securityResource, requestAccess.getRequestDatas().getAccessType().toString(), paramdatas,
						requestAccess.getRemoteIp(), requestAccess.getRequestURI().toString());

			}

			if (logger.isTraceEnabled())
				time4 = System.currentTimeMillis();

			if (null != limitCode) {
				throw new AccessAuthorizeException(trace.traceException("access fobidden resource " + limitCode, requestAccess.getTraceState(), ""));
			}
			requestAccess.getRequestDatas().setSecurityResource(securityResource);

			if (logger.isTraceEnabled())
				time5 = System.currentTimeMillis();

		} finally {

			if (logger.isTraceEnabled()) {
				Map<String, Object> authorize = new HashMap<String, Object>();
				String key = requestAccess.getRequestDatas().getAccessId() + "-" + requestAccess.getRequestDatas().getAccessType() + "-" + requestAccess.getRequestURI() + "-limit";
				authorize.put(key, time5 - time);
				authorize.put(key + "1", time2 - time1);
				authorize.put(key + "2", time3 - time2);
				authorize.put(key + "3", time4 - time3);
				authorize.put(key + "4", time5 - time4);
				trace.trace(authorize);
			}
		}
		return false;
	}

	/**
	 * 是否限制访问IP单位时间内的次数
	 * 
	 * @param ip
	 * @return true为限制
	 */
	private boolean hasLimitIpInfo(String ip, String limitInfo, String uri, String method, String domainName, long accessId, String accessType)
			throws Exception {
		String limit = limitInfo;
		Map<String, Object> limitData = null;

		long starttime = Calendar.getInstance().getTimeInMillis();
		int iplc = 0;
		int iptc = 0;
		int iprt = 0;
		boolean ipme = true;
		boolean isMillms = true;
		boolean is = true;
		String output = "";
		try {
			limitData = JSONObject.parseObject(limit);

			if (limitData.containsKey("ACPTHTTP") || limitData.containsKey("ACPTSOAP")) {
				return false;
			}

			is = basicLimit(limitData, domainName, ip);
			if (is) {
				return is;
			}

			isMillms = limitData.containsKey("ISMS") ? Boolean.parseBoolean(String.valueOf(limitData.get("ISMS"))) : true;
			if (!limitData.containsKey("IPLC") || !limitData.containsKey("IPTC")) {
				return false;
			}
			iplc = Integer.parseInt(String.valueOf(limitData.get("IPLC")));
			iptc = Integer.parseInt(String.valueOf(limitData.get("IPTC")));
			iprt = Integer.parseInt(String.valueOf(limitData.get("IPRT")));
			if (limitData.containsKey("IPME")) {
				ipme = Boolean.valueOf(String.valueOf(limitData.get("IPME")));
			}
			if (ipme) {
				is = redisService.limitRule(ip + "-" + accessId + "-" + accessType, iplc, iptc, iprt, null, null, isMillms);
			} else {
				is = redisService.limitRule(ip + "-" + accessId + "-" + accessType, iplc, iptc, iprt, uri, method, isMillms);
			}

			if (limitData.containsKey("RCTS")) {
				if (is) {
					if (isMillms) {
						iprt = iprt / 1000;
					}
					redisService.removeLimitInfoByMemcahed(accessId, accessType, uri, method);
					redisService.setLimitInfoByMemcahed(accessId, accessType, uri, method, limitInfo, iprt);
				} else {
					redisService.setLimitInfoByMemcahed(accessId, accessType, uri, method, limitInfo, Integer.parseInt(String.valueOf(limitData.get("RCTS"))));
				}
			}
		} catch (Exception e) {
			if (logger.isDebugEnabled())
				logger.debug("hasLimitIpInfoByParamData:" + " ip:" + ip, e);
			output = "[CHECK-MEM-IP-" + iplc + "-" + iptc + "-" + iprt + "-" + isMillms + "][" + ip + "][" + uri + "][" + method + "][" + is + "][" + e.getMessage() + "]";
			return false;
		} finally {
			if (StringUtils.isEmpty(output)) {
				output = "[CHECK-MEM-IP-" + iplc + "-" + iptc + "-" + iprt + "-" + isMillms + "][" + ip + "][" + uri + "][" + method + "][" + is + "]";
			}
			if (logger.isDebugEnabled()) {
				logger.debug(output);
			}
			logFormat.format("CHECK-MEM-IP", this.getClass().getName(), method, ip, String.valueOf(starttime), String.valueOf(Calendar.getInstance().getTimeInMillis() - starttime), uri, output,
					String.valueOf(limit), true);

		}

		return is;
	}

	private boolean basicLimit(Map<String, Object> limitData, String domainName, String value) {

		if (limitData.containsKey("ACST"))// 访问允许的开始时间
		{
			if (Calendar.getInstance().getTime().before(DateUtils.parseDate(String.valueOf(limitData.get("ACST")), DateUtils.TIMESTAMP_DF))) {
				return true;
			}
		}

		if (limitData.containsKey("ACET"))// 访问允许的开始时间
		{
			if (Calendar.getInstance().getTime().after(DateUtils.parseDate(String.valueOf(limitData.get("ACET")), DateUtils.TIMESTAMP_DF))) {
				return true;
			}
		}

		if (limitData.containsKey("IPNP")) {
			if (IPRangeValidator.isValid(String.valueOf(limitData.get("IPNP")), value)) {
				return true;
			}
		}
		if (limitData.containsKey("IPPS")) {
			if (IPRangeValidator.isValid(String.valueOf(limitData.get("IPPS")), value)) {
				return false;
			}
		}

		if (limitData.containsKey("HOST")) {
			if (StringUtils.isEmpty(domainName)) {
				return true;
			}
			if (!domainName.contains(String.valueOf(limitData.get("HOST"))) && !String.valueOf(limitData.get("HOST")).contains(domainName)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 是否限制访问IP单位时间内的次数
	 * 
	 * @param ip
	 * @return true为限制
	 */
	private boolean hasLimitIp(AccessSecurityInfo info, String serverip, String clientip, SecurityResource securityResource, String domainName) {
		if (securityResource == null) {
			if (isNotCheckAccessMethod(info.getId().getType())) {
				return false;
			} else {
				return true;
			}
		}
		try {

			String infoIp = getRandomLimitInfo(info.getId().getId(), info.getId().getType(), securityResource.getNote(), securityResource.getId().getUrl(), securityResource.getId().getMethodName());

			boolean rtn = hasLimitIpInfo(serverip, infoIp, securityResource.getId().getUrl(), securityResource.getId().getMethodName(), domainName, info.getId().getId(), info.getId().getType());
			if (rtn) {
				return rtn;
			}

			if (!StringUtils.isBlank(clientip) && !clientip.equals(serverip)) {
				infoIp = getRandomLimitInfo(info.getId().getId(), info.getId().getType(), securityResource.getNote(), securityResource.getId().getUrl(), securityResource.getId().getMethodName());
				rtn = hasLimitIpInfo(clientip, infoIp, securityResource.getId().getUrl(), securityResource.getId().getMethodName(), domainName, info.getId().getId(), info.getId().getType());
			} else {
				if (logger.isDebugEnabled())
					logger.debug("checkIpLimitInMillisData securityResource:" + " clientip same as serverip or clientip is null:" + clientip + "/" + securityResource.getId().getId() + "/"
							+ securityResource.getId().getUrl() + "/" + securityResource.getId().getMethodName() + "/" + infoIp + " isLimitIP: " + rtn);
			}
			return rtn;
		} catch (Exception e) {
			if (logger.isDebugEnabled())
				logger.debug("checkIpLimitInMillisData securityResource:" + " clientip:" + clientip + " serverip:" + serverip + "/" + securityResource.getId().getId() + "/"
						+ securityResource.getId().getUrl() + "/" + securityResource.getId().getMethodName() + " isLimitIP: false", e);
			return false;
		}
	}

	private String getRandomLimitInfo(long accessId, String accessType, String securityResourceNote, String uri, String method) {

		if (!StringUtils.isBlank(uri) && !StringUtils.isBlank(method)) {
			String limitInfo = redisService.getLimitInfoByMemcahed(accessId, accessType, uri, method);
			if (!StringUtils.isBlank(limitInfo)) {
				return limitInfo;
			}
		}

		if (StringUtils.isBlank(securityResourceNote)) {
			return securityResourceNote;
		}

		String[] limitIpInfos = StringUtils.split(securityResourceNote, "\\|\\|");
		String infoLimit = null;
		if (limitIpInfos.length > 1) {
			List<String> limitIpTimeSE = new ArrayList<String>(0);
			List<String> limitIpTimeCOMM = new ArrayList<String>(0);
			List<String> limitIpTimeSEServer = new ArrayList<String>(0);
			List<String> limitIpTimeCOMMServer = new ArrayList<String>(0);
			for (String limitIpInfo : limitIpInfos) {
				Map<String, Object> limit = JSONObject.parseObject(limitIpInfo);
				if (!limit.containsKey("SERIPTS") && !limit.containsKey("SERIPTE")) {
					if (limit.containsKey("IPTS") && limit.containsKey("IPTE")) {
						Calendar time = Calendar.getInstance();
						time.set(time.get(Calendar.YEAR), time.get(Calendar.MONTH), time.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
						time.add(Calendar.SECOND, Integer.parseInt(String.valueOf(limit.get("IPTS"))));

						if (Calendar.getInstance().before(time)) {
							continue;
						}

						time.set(time.get(Calendar.YEAR), time.get(Calendar.MONTH), time.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
						time.add(Calendar.SECOND, Integer.parseInt(String.valueOf(limit.get("IPTE"))));

						if (Calendar.getInstance().after(time)) {
							continue;
						}

						limitIpTimeSE.add(limitIpInfo);
					} else {
						limitIpTimeCOMM.add(limitIpInfo);
					}
				} else {

					if (limit.containsKey("SERIPTS") && limit.containsKey("SERIPTE")) {
						Calendar time = Calendar.getInstance();
						time.set(time.get(Calendar.YEAR), time.get(Calendar.MONTH), time.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
						time.add(Calendar.SECOND, Integer.parseInt(String.valueOf(limit.get("SERIPTS"))));

						if (Calendar.getInstance().before(time)) {
							continue;
						}

						time.set(time.get(Calendar.YEAR), time.get(Calendar.MONTH), time.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
						time.add(Calendar.SECOND, Integer.parseInt(String.valueOf(limit.get("SERIPTE"))));

						if (Calendar.getInstance().after(time)) {
							continue;
						}

						limitIpTimeSEServer.add(limitIpInfo);
					} else {
						limitIpTimeCOMMServer.add(limitIpInfo);
					}

				}
			}
			if (!limitIpTimeSEServer.isEmpty()) {
				infoLimit = limitIpTimeSEServer.get(new Random().nextInt(limitIpTimeSEServer.size()));
			} else {
				if (!limitIpTimeCOMMServer.isEmpty()) {
					infoLimit = limitIpTimeCOMMServer.get(new Random().nextInt(limitIpTimeCOMMServer.size()));
				} else {
					infoLimit = limitIpInfos[new Random().nextInt(limitIpInfos.length)];
				}
			}
		} else if (limitIpInfos.length == 1) {
			infoLimit = limitIpInfos[0];
		} else {
			infoLimit = securityResourceNote;
		}
		return infoLimit;
	}

	private String[] resetArray(String[] data, int size) {
		return resetArray(data, size, "");
	}

	private String[] resetArray(String[] data, int size, String defaultv) {
		if (data == null) {
			return new String[size];
		}
		if (data.length != size) {
			String[] tmp = new String[size];
			for (int i = 0; i < size; i++) {
				if (i < data.length - 1) {
					tmp[i] = data[i];
				} else {
					tmp[i] = defaultv;
				}
			}
			return tmp;
		}
		return data;
	}

	private Integer checkPassportRequestTime(String soapId, SecurityResource securityResource, String accessType, Map<?, ?> paramsMap, String soapIp, String soapDomainName) {
		try {
			if (securityResource == null) {
				return null;
			}

			if (StringUtils.isEmpty(securityResource.getNote())) {
				return null;
			}

			if (paramsMap == null) {
				return null;
			}

			String limitinfo = getRandomLimitInfo(securityResource.getId().getId(), accessType, securityResource.getNote(), securityResource.getId().getUrl(), securityResource.getId().getMethodName());
			JSONObject datas = JSONObject.parseObject(limitinfo);
			if (!datas.containsKey("ACPTHTTP")) {
				return null;
			}
			String paramValue = "";

			String[] paramIdexs = datas.getString("ACPTHTTP").split(",");
			int size = paramIdexs.length;

			String[] codetypes = datas.containsKey("ACPTEDTY") ? datas.getString("ACPTEDTY").split(",") : null;
			String[] seconds = datas.containsKey("ACPTSEC") ? datas.getString("ACPTSEC").split(",") : null;
			String[] cachs = datas.containsKey("ACPTCACH") ? datas.getString("ACPTCACH").split(",") : null;
			String[] writeValue = datas.containsKey("ACPTVE") ? datas.getString("ACPTVE").split(",") : null;
			String[] isignore = datas.containsKey("ACPTIGN") ? datas.getString("ACPTIGN").split(",") : null;
			codetypes = resetArray(codetypes, size);
			seconds = resetArray(seconds, size, "1000");
			cachs = resetArray(cachs, size);
			writeValue = resetArray(writeValue, size);
			isignore = resetArray(isignore, size);

			String[] arrays = datas.containsKey("ACPTL") ? datas.getString("ACPTL").split(",") : null;
			Map<String, Object> paramnamevalue = null;
			if (arrays != null) {
				if (seconds == null || seconds.length < 1) {
					arrays = null;
				}
				if (cachs == null || cachs.length < 1) {
					arrays = null;
				}
				if (arrays != null) {
					paramnamevalue = new HashMap<String, Object>();
					for (String array : arrays) {
						String[] paramnames = StringUtils.substringsBetween(array, "[", "]");
						for (String paramname : paramnames) {
							if (!paramnamevalue.containsKey(paramname)) {
								paramnamevalue.put(paramname, "");
							}
						}
					}
				}
			}

			String acptRule = datas.containsKey("ACPTRU") ? datas.getString("ACPTRU") : "";
			List<LogicRule> mixParamRule = null;
			if (!StringUtils.isBlank(acptRule)) {
				mixParamRule = JSONArray.parseArray(acptRule, LogicRule.class);
				for (LogicRule logicRule : mixParamRule) {
					List<ParamLogic> contiation = logicRule.getIF();
					int contiationSize = contiation.size();
					for (int z = 0; z < contiationSize; z++) {
						ParamLogic paramLogic = contiation.get(z);
						String[] paramnames = paramLogic.pnames();
						for (String paramname : paramnames) {
							if (!paramnamevalue.containsKey(paramname)) {
								paramnamevalue.put(paramname, "");
							}
						}
					}
				}
			}

			for (int i = 0; i < size; i++) {
				String paramIdex = paramIdexs[i];

				if (paramIdex.equals("-1000")) {
					paramValue = soapIp;
				} else if (paramIdex.equals("-1001")) {
					paramValue = soapId;
				} else if (paramIdex.equals("-1002")) {
					paramValue = String.valueOf(securityResource.getId().getId());
				} else if (paramIdex.equals("-1003")) {
					paramValue = accessType;
				} else if (paramIdex.equals("-1004")) {
					paramValue = securityResource.getId().getUrl();
				} else if (paramIdex.equals("-1005")) {
					paramValue = securityResource.getId().getMethodName();
				} else if (paramIdex.equals("-1006")) {
					paramValue = soapDomainName;
				} else if (paramIdex.equals("-1007")) {

					String paramValue1 = "";
					Object object = paramsMap.get("dataInfo");
					if (object instanceof String[]) {
						String[] objs = (String[]) object;
						paramValue1 = objs[0];
					} else {
						paramValue1 = String.valueOf(object);
					}
					JSONObject dataInfos = JSONObject.parseObject(paramValue1);
					String i1007 = "-1007" + i;
					String S1007 = datas.containsKey(i1007) ? datas.getString(i1007) : "";
					if (!StringUtils.isEmpty(S1007)) {
						paramValue = String.valueOf(dataInfos.get(S1007));
					} else {
						paramValue = paramValue1;
					}

					paramIdex = i1007;

				} else if (paramIdex.equals("-1008")) {
					paramValue = String.valueOf(Calendar.getInstance().getTimeInMillis());
				} else if (StringUtils.contains(paramIdex, '.')) {
					String[] paramIdexstmp = StringUtils.split(paramIdex, '.');
					Object object = paramsMap.get(paramIdexstmp[0]);
					if (object instanceof List) {
						Object obj = ((List<?>) object).get(Integer.parseInt(paramIdexstmp[1]));
						if (obj instanceof Map) {
							paramValue = String.valueOf(((Map<?, ?>) obj).get(paramIdexstmp[2]));
						}
					} else if (object instanceof Map) {
						paramValue = String.valueOf(((Map<?, ?>) object).get(paramIdexstmp[1]));
					}
				} else {
					Object object = paramsMap.get(paramIdex);
					if (object instanceof String[]) {
						String[] objs = (String[]) object;
						paramValue = objs[0];
					} else if (object instanceof JSONArray) {
						((JSONArray) object).get(0);
					} else {
						paramValue = String.valueOf(object);
					}

				}

				if (paramnamevalue != null && paramnamevalue.containsKey(paramIdex) && StringUtils.isBlank(String.valueOf(paramnamevalue.get(String.valueOf(paramIdex))))) {
					paramnamevalue.put(paramIdex, paramValue);
				}

				Integer is = checkRequestTime(writeValue[i], codetypes[i], seconds[i], cachs[i], paramValue, securityResource.getId().getId(), accessType, securityResource.getId().getUrl(),
						securityResource.getId().getMethodName(), limitinfo, isignore[i]);
				if (is != null) {
					return is;
				}
			}

			if (arrays != null) {
				for (String array : arrays) {
					String[] paramnames = StringUtils.substringsBetween(array, "[", "]");

					ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
					List<String> replacementListArray = new ArrayList<String>();
					int i = 0;
					for (String paramname : paramnames) {
						engine.put("s" + i, paramnamevalue.get(paramname));
						replacementListArray.add("s" + i);
						i++;
					}
					String rule = StringUtils.replaceEachRepeatedly(array, paramnames, replacementListArray.toArray(new String[] {}));
					rule = StringUtils.replaceChars(rule, "[", "");
					rule = StringUtils.replaceChars(rule, "]", "");
					if (!Boolean.parseBoolean(String.valueOf(engine.eval(rule)))) {
						return 2016;
					} else {
						Integer is = checkRequestTime(null, null, seconds[seconds.length - 1], cachs[cachs.length - 1], MD5Encrypt.encrypt(array), securityResource.getId().getId(), accessType,
								securityResource.getId().getUrl(), securityResource.getId().getMethodName(), limitinfo, isignore[isignore.length - 1]);
						if (is != null) {
							return is;
						}
					}
				}
			}

			if (mixParamRule != null && !mixParamRule.isEmpty()) {
				for (LogicRule logicRule : mixParamRule) {

					List<ParamLogic> contiation = logicRule.getIF();
					int contiationSize = contiation.size();
					for (int z = 0; z < contiationSize; z++) {
						ParamLogic paramLogic = contiation.get(z);
						String[] paramnames = paramLogic.pnames();

						ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
						List<String> replacementListArray = new ArrayList<String>();
						int i = 0;
						for (String paramname : paramnames) {
							engine.put("s" + i, paramnamevalue.get(paramname));
							replacementListArray.add("s" + i);
							i++;
						}
						String rule = StringUtils.replaceEachRepeatedly(paramLogic.getExpr(), paramnames, replacementListArray.toArray(new String[] {}));
						rule = StringUtils.replaceChars(rule, "[", "");
						rule = StringUtils.replaceChars(rule, "]", "");

						if (Boolean.parseBoolean(String.valueOf(engine.eval(rule)))) {
							Limit limitSrc = logicRule.getT().get(z);
							Object value = paramnamevalue.get(limitSrc.getPnam());
							Integer is = checkRequestTime("", "", seconds[seconds.length - 1], cachs[cachs.length - 1], MD5Encrypt.encrypt(rule) + String.valueOf(value), securityResource.getId()
									.getId().longValue(), accessType, securityResource.getId().getUrl(), securityResource.getId().getMethodName(), limitSrc.convertTo(), isignore[isignore.length - 2]);
							if (is != null) {
								return is;
							}
						}
					}
				}
			}

		} catch (Exception e) {
			logger.error("", e);
		}
		return null;

	}

	private boolean validateParamvalue(String paramvalue, String rule) {
		if (rule.startsWith("$$$(") && rule.endsWith(")$$$")) {
			try {
				ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
				engine.put("s", paramvalue);
				return Boolean.parseBoolean(String.valueOf(engine.eval(rule.substring(4, rule.length() - 4))));
			} catch (ScriptException e) {
			}
		} else {
			Pattern userPattern = Pattern.compile(rule);
			Matcher userMatcher = userPattern.matcher(paramvalue);
			if (userMatcher.matches() == false) {
				return false;
			}
		}
		return true;
	}

	private Integer checkRequestTime(String writeValue, String codetype, String second, String cach, String paramValue, long accessId, String accessType, String uri, String methodName,
			String limitInfo, String isignore)
			throws Exception {

		if (!StringUtils.isBlank(writeValue) && (!validateParamvalue(paramValue, writeValue)) && (StringUtils.isBlank(isignore) || !"yes".equalsIgnoreCase(isignore))) {
			return 2015;
		}

		if (StringUtils.isBlank(second) || "0".equals(second) || StringUtils.isBlank(cach)) {
			return null;
		}

		try {
			if ("E".equals(codetype)) {
				paramValue = CommonMethod.decodeString(paramValue);
			} else if ("H".equals(codetype)) {
				paramValue = CommonMethod.HexToStr(paramValue);
			}
		} catch (Exception e) {
			logger.error("", e);
			return 2014;
		}
		if (StringUtils.isEmpty(paramValue)) {
			return 2014;
		}

		if ((!StringUtils.isEmpty(cach) && "M1".equals(cach)) && redisService.isAlive()) {
			if (hasLimitValueInfo(paramValue, limitInfo, uri, methodName, uri, accessId, accessType)) {
				return 54061;
			}
		} else {
			if (redisService.isAlive()) {
				String key = paramValue.toUpperCase() + "-" + accessId + "-" + accessType + "-" + uri + "-" + methodName;
				if (redisService.get(key) == null) {
					redisService.set(key, System.currentTimeMillis(), Integer.parseInt(second) / 1000);
				} else {
					return 2013;
				}
			}
		}
		return null;

	}

	/**
	 * 是否限制访问IP单位时间内的次数
	 * 
	 * @param paramValue
	 * @return true为限制
	 */
	private boolean hasLimitValueInfo(String paramValue, String limitInfo, String uri, String method, String domainName, long accessId, String accessType)
			throws Exception {

		long starttime = Calendar.getInstance().getTimeInMillis();

		String limit = limitInfo;
		Map<String, Object> limitData = null;

		int iplc = 0;
		int iptc = 0;
		int iprt = 0;
		boolean ipme = true;
		boolean isMillms = true;
		boolean is = true;
		String output = "";

		try {
			limitData = JSONObject.parseObject(limit);

			is = basicLimit(limitData, domainName, paramValue);
			if (is) {
				return is;
			}

			isMillms = limitData.containsKey("ISMS") ? Boolean.parseBoolean(String.valueOf(limitData.get("ISMS"))) : true;
			if (!limitData.containsKey("IPLC") || !limitData.containsKey("IPTC")) {
				return false;
			}
			iplc = Integer.parseInt(String.valueOf(limitData.get("IPLC")));
			iptc = Integer.parseInt(String.valueOf(limitData.get("IPTC")));
			iprt = Integer.parseInt(String.valueOf(limitData.get("IPRT")));
			if (limitData.containsKey("IPME")) {
				ipme = Boolean.valueOf(String.valueOf(limitData.get("IPME")));
			}
			paramValue = paramValue + "-" + accessId + "-" + accessType;
			if (ipme) {
				is = redisService.limitRule(paramValue, iplc, iptc, iprt, null, null, isMillms);
			} else {
				is = redisService.limitRule(paramValue, iplc, iptc, iprt, uri, method, isMillms);
			}

			if (limitData.containsKey("RCTS")) {
				if (is) {
					if (isMillms) {
						iprt = iprt / 1000;
					}
					redisService.removeLimitInfoByMemcahed(accessId, accessType, uri, method);
					redisService.setLimitInfoByMemcahed(accessId, accessType, uri, method, limitInfo, iprt);
				} else {
					redisService.setLimitInfoByMemcahed(accessId, accessType, uri, method, limitInfo, Integer.parseInt(String.valueOf(limitData.get("RCTS"))));
				}
			}
			return is;
		} catch (Exception e) {
			if (logger.isDebugEnabled())
				logger.debug("hasLimitIpInfoByParamData:" + " paramValue:" + paramValue, e);
			output = "[CHECK-MEM-VALUE-" + iplc + "-" + iptc + "-" + iprt + "-" + isMillms + "][" + paramValue + "][" + uri + "][" + method + "][" + is + "][" + e.getMessage() + "]";
			return false;
		} finally {
			if (StringUtils.isEmpty(output)) {
				output = "[CHECK-MEM-VALUE-" + iplc + "-" + iptc + "-" + iprt + "-" + isMillms + "][" + paramValue + "][" + uri + "][" + method + "][" + is + "]";
			}
			if (logger.isDebugEnabled()) {
				logger.debug(output);
			}
			logFormat.format("CHECK-MEM-VALUE", this.getClass().getName(), method, paramValue, String.valueOf(starttime), String.valueOf(Calendar.getInstance().getTimeInMillis() - starttime), uri,
					output, String.valueOf(limit), true);

		}
	}

	/**
	 * 是否验证IP信息
	 * 
	 * @param accessType
	 * @return true为不验证
	 */
	public static boolean isNotCheckIp(String accessType) {
		return AccessSecurityInfo.S_TYPE_SNAIL_HOST_GAME.equals(accessType) || AccessSecurityInfo.S_TYPE_SNAIL_HOST_INTERFACE_NOIP_NOVAIL.equals(accessType);
	}

	/**
	 * 是否验证请求方法
	 * 
	 * @param accessType
	 * @return true为不验证
	 */
	public static boolean isNotCheckAccessMethod(String accessType) {
		return AccessSecurityInfo.S_TYPE_SNAIL_COMMON.equals(accessType);
	}

	/**
	 * 是否验证校验串
	 * 
	 * @param accessType
	 * @return true为不验证
	 */
	public static boolean isNotCheckVailParam(String accessType) {
		return AccessSecurityInfo.S_TYPE_SNAIL_HOST_INTERFACE.equals(accessType) || AccessSecurityInfo.S_TYPE_SNAIL_HOST_INTERFACE_NOIP_NOVAIL.equals(accessType);
	}
}
