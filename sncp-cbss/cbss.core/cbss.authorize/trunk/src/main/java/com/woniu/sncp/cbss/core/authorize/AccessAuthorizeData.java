package com.woniu.sncp.cbss.core.authorize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.woniu.sncp.cbss.core.model.access.AccessSecurityInfo;
import com.woniu.sncp.cbss.core.model.access.SecurityResource;
import com.woniu.sncp.cbss.core.model.constant.NameFactory;
import com.woniu.sncp.cbss.core.repository.redis.RedisService;
import com.woniu.sncp.cbss.core.repository.zookeeper.ZooKeeperFactory;
import com.woniu.sncp.cbss.core.repository.zookeeper.ZookeeperConfValue;
import com.woniu.sncp.cbss.core.trace.aspect.ParamsAndReturningLog;

@Component
public class AccessAuthorizeData {

	private static final Logger logger = LoggerFactory.getLogger(AccessAuthorize.class);

	public static List<AccessSecurityInfo> accessSecurityInfos = Collections.synchronizedList(new ArrayList<AccessSecurityInfo>());
	public static List<SecurityResource> securityResources = Collections.synchronizedList(new ArrayList<SecurityResource>());

	@Autowired
	private ZooKeeperFactory zooKeeperFactory;

	@Autowired
	private RedisService redisService;

	@Autowired
	private AccessUrlConfigurationProperties accessUrlConfigurationProperties;

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
					if(StringUtils.isNotBlank(resourcesInfos)){
						List<SecurityResource> data = JSONArray.parseArray(resourcesInfos, SecurityResource.class);
						securityResources.addAll(data);
					}
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

	@ParamsAndReturningLog
	public AccessSecurityInfo getAccessSecurityInfo(Long accessId, Long accessType) {
		AccessSecurityInfo accessSecurityInfo = null;
		if (accessSecurityInfos == null || accessSecurityInfos.size() == 0) {
			return accessSecurityInfo;
		}
		for (AccessSecurityInfo info : accessSecurityInfos) {
			try {
				if (info != null && info.getId() != null) {
					if (info.getId().getType().equals(accessType.toString()) && info.getId().getId().compareTo(accessId) == 0) {
						accessSecurityInfo = info;
						break;
					}
				}
			} catch (Exception e) {
				return accessSecurityInfo;
			}
		}
		return accessSecurityInfo;
	}

	@ParamsAndReturningLog
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
			return servletPath.substring(start + 1);
		} catch (Exception e) {
			return NameFactory.default_constant.INFPARAM_HTTPPARAM_ALLMETHOD.getValue();
		}

	}
}
