package com.woniu.sncp.cbss.core.authorize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.woniu.sncp.cbss.core.model.access.AccessSecurityInfo;
import com.woniu.sncp.cbss.core.model.access.AccessSecurityInfoKey;
import com.woniu.sncp.cbss.core.model.access.SecurityResource;
import com.woniu.sncp.cbss.core.model.constant.NameFactory;
import com.woniu.sncp.cbss.core.repository.redis.RedisService;
import com.woniu.sncp.cbss.core.repository.zookeeper.ZooKeeperFactory;
import com.woniu.sncp.cbss.core.repository.zookeeper.ZookeeperConfValue;
import com.woniu.sncp.cbss.core.trace.aspect.listener.Trace;

@Component
public class AccessAuthorizeData {

	private static final Logger logger = LoggerFactory.getLogger(AccessAuthorizeData.class);

	public static List<AccessSecurityInfo> accessSecurityInfos = Collections.synchronizedList(new ArrayList<AccessSecurityInfo>());
	public static List<SecurityResource> securityResources = Collections.synchronizedList(new ArrayList<SecurityResource>());
	@Autowired
	private Trace trace;

	@Autowired
	private ZooKeeperFactory zooKeeperFactory;

	@Autowired
	private RedisService redisService;

	@Autowired
	private AccessUrlConfigurationProperties accessUrlConfigurationProperties;

	/**
	 * @param accessSecurityInfo
	 * @param securityResources
	 * @return isEmp
	 */
	private boolean deleteAccessSecurityInfoFromsecurityResources(AccessSecurityInfo accessSecurityInfo, List<SecurityResource> securityResources) {
		boolean ishave = false;
		for (SecurityResource securityResource : securityResources) {
			if (accessSecurityInfo.getId().getId().compareTo(securityResource.getId().getId()) == 0) {
				ishave = true;
			}
		}
		return ishave;
	}

	private List<AccessSecurityInfo> deleteAccessSecurityInfoFromsecurityResources(List<AccessSecurityInfo> accessSecurityInfos, List<SecurityResource> securityResources) {
		List<AccessSecurityInfo> wDelete = new ArrayList<AccessSecurityInfo>();
		for (AccessSecurityInfo accessSecurityInfo : accessSecurityInfos) {
			boolean ishave = false;
			for (SecurityResource securityResource : securityResources) {
				if (accessSecurityInfo.getId().getId().compareTo(securityResource.getId().getId()) == 0) {
					ishave = true;
				}
			}

			if (!ishave) {
				wDelete.add(accessSecurityInfo);
			}
		}
		return wDelete;
	}

	@PostConstruct
	public void fromZookeeperData()
			throws Exception {
		String securityInfos = redisService.get(NameFactory.zookeeper_constant.accessSecurityInfoPath2.getValue());
		List<AccessSecurityInfo> infos = JSONArray.parseArray(securityInfos, AccessSecurityInfo.class);
		accessSecurityInfos.addAll(infos);
		List<String> urls = accessUrlConfigurationProperties.getUrls();
		try {
			if (urls != null) {
				for (String url : urls) {
					String resourcesInfos = redisService.get(NameFactory.zookeeper_constant.accessSecurityResourcesPath2.getValue() + url);
					if (StringUtils.isNotBlank(resourcesInfos)) {
						List<SecurityResource> data = JSONArray.parseArray(resourcesInfos, SecurityResource.class);
						securityResources.addAll(data);
					}
				}

				List<AccessSecurityInfo> wDelete = deleteAccessSecurityInfoFromsecurityResources(accessSecurityInfos, securityResources);
				if (!wDelete.isEmpty()) {
					accessSecurityInfos.removeAll(wDelete);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		zooKeeperFactory.getData(NameFactory.zookeeper_constant.accessSecurityInfoPathAdd.getValue(), new ZookeeperConfValue() {
			@Override
			public void value(String newValue) {
				logger.trace("[AccessSecurityInfo-zookeeper-alter][{0}]", newValue);
				AccessSecurityInfo accessSecurityInfoNew = accessSecurityInfo(newValue);
				boolean isHave = false;
				int index = 0;
				for (AccessSecurityInfo accessSecurityInfo : accessSecurityInfos) {
					index++;
					try {
						if (accessSecurityInfo.getId().getId().compareTo(accessSecurityInfoNew.getId().getId()) == 0
								&& accessSecurityInfo.getId().getType().equals(accessSecurityInfoNew.getId().getType())) {
							isHave = true;
							break;
						}
					} catch (Exception e) {
						logger.error("", e);
					}
				}
				if (isHave) {
					// 更新
					accessSecurityInfos.set(index - 1, accessSecurityInfoNew);
				} else {
					// 新增
					accessSecurityInfos.add(accessSecurityInfoNew);
				}
			}
		});

		zooKeeperFactory.getData(NameFactory.zookeeper_constant.accessSecurityResourcesPathAdd.getValue(), new ZookeeperConfValue() {
			@Override
			public void value(String newValue) {
				logger.trace("[AccessSecurityResource-zookeeper-alter][{0}]", newValue);
				SecurityResource securityResourceNew = accessSecurityResource(newValue);

				if (urls == null) {
					return;
				}

				if (!urls.contains(securityResourceNew.getId().getUrl())) {
					return;
				}

				boolean isHave = false;
				int index = 0;
				for (SecurityResource securityResource : securityResources) {
					index++;
					try {
						if (securityResource.getId().getId().compareTo(securityResourceNew.getId().getId()) == 0 && securityResource.getId().getUrl().equals(securityResourceNew.getId().getUrl())
								&& securityResourceNew.getId().getMethodName().equals(securityResource.getId().getMethodName())) {
							isHave = true;
							break;
						}
					} catch (Exception e) {
						logger.error("", e);
					}
				}
				if (isHave) {
					// 更新
					securityResources.set(index - 1, securityResourceNew);
				} else {
					// 新增
					securityResources.add(securityResourceNew);

					List<AccessSecurityInfo> wDelete = deleteAccessSecurityInfoFromsecurityResources(accessSecurityInfos, securityResources);
					if (!wDelete.isEmpty()) {
						accessSecurityInfos.removeAll(wDelete);
					}
				}
			}
		});
	}

	public AccessSecurityInfo accessSecurityInfo(String securityInfo) {
		return JSONObject.parseObject(securityInfo, AccessSecurityInfo.class);
	}

	public SecurityResource accessSecurityResource(String securityResource) {
		return JSONObject.parseObject(securityResource, SecurityResource.class);
	}

	public AccessSecurityInfo getAccessSecurityInfo(Long accessId, Long accessType) {
		long time = 0, time1 = 0, time2 = 0, time3 = 0, time4 = 0, time5 = 0;
		long time7 = 0;
		long[] time6 = null;
		if (logger.isTraceEnabled())
			time = System.currentTimeMillis();
		AccessSecurityInfo accessSecurityInfo = null;
		try {
			int size = accessSecurityInfos.size();
			if (accessSecurityInfos == null || accessSecurityInfos.size() == 0) {
				return accessSecurityInfo;
			}

			if (logger.isTraceEnabled())
				time1 = System.currentTimeMillis();
			if (logger.isTraceEnabled())
				time6 = new long[accessSecurityInfos.size()];

			if (logger.isTraceEnabled()) {

				for (int i = 0; i < size; i++) {

					if (logger.isTraceEnabled())
						time7 = System.currentTimeMillis();

					AccessSecurityInfo info = accessSecurityInfos.get(i);
					try {
						if (info != null) {
							AccessSecurityInfoKey idinfo = info.getId();
							if (idinfo != null) {
								if (idinfo.getType().equals(accessType.toString()) && idinfo.getId().compareTo(accessId) == 0) {
									accessSecurityInfo = info;
									break;
								}
							}
						}
					} catch (Exception e) {
						return accessSecurityInfo;
					} finally {

						if (logger.isTraceEnabled())
							time6[i] = System.currentTimeMillis() - time7;
					}

				}
			} else {
				int i = 0;
				for (AccessSecurityInfo info : accessSecurityInfos) {

					if (logger.isTraceEnabled())
						time7 = System.currentTimeMillis();

					try {
						if (info != null && info.getId() != null) {
							if (info.getId().getType().equals(accessType.toString()) && info.getId().getId().compareTo(accessId) == 0) {
								accessSecurityInfo = info;
								break;
							}
						}
					} catch (Exception e) {
						return accessSecurityInfo;
					} finally {

						if (logger.isTraceEnabled())
							time6[i] = System.currentTimeMillis() - time7;
					}
				}
			}
			if (logger.isTraceEnabled())
				time2 = System.currentTimeMillis();

		} finally {
			if (logger.isTraceEnabled()) {
				Map<String, Object> authorize = new HashMap<String, Object>();
				String key = accessId + "-" + accessType + "-getAccessSecurityInfo";
				authorize.put(key, time5 - time);
				authorize.put(key + "1", time2 - time1);
				authorize.put(key + "2", time3 - time2);
				authorize.put(key + "3", time4 - time3);
				authorize.put(key + "4", time5 - time4);
				authorize.put(key + "5", time6);
				trace.trace(authorize);
			}
		}
		return accessSecurityInfo;
	}

	public SecurityResource getSecurityResource(Long accessId, String uri, String methodName) {
		for (SecurityResource securityResource : securityResources) {
			if (securityResource.getId().getId().compareTo(accessId) == 0) {
				if (securityResource.getId().getMethodName().equals(methodName) && securityResource.getId().getUrl().equals(uri)) {
					return securityResource;
				}
			}
		}

		for (SecurityResource securityResource : securityResources) {
			if (securityResource.getId().getId().compareTo(accessId) == 0) {
				if (securityResource.getId().getMethodName().equals(NameFactory.default_constant.INFPARAM_HTTPPARAM_ALLMETHOD.getValue())
						&& securityResource.getId().getUrl().equals(NameFactory.default_constant.INFPARAM_HTTPPARAM_ALLMETHOD.getValue())) {
					return securityResource;
				}
			}
		}
		return null;
	}

	public String getMethod(String servletPath) {
		if (StringUtils.isBlank(servletPath)) {
			return NameFactory.default_constant.INFPARAM_HTTPPARAM_ALLMETHOD.getValue();
		}
		try {
			int start = servletPath.lastIndexOf("/");
			if (start < 0) {
				start = servletPath.lastIndexOf(".");
			}
			return servletPath.substring(start + 1);
		} catch (Exception e) {
			return NameFactory.default_constant.INFPARAM_HTTPPARAM_ALLMETHOD.getValue();
		}
	}
}
