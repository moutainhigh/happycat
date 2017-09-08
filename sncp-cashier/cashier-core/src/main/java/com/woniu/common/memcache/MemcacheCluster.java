package com.woniu.common.memcache;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
 
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.woniu.sncp.pay.core.service.MemcachedService;

import net.snail.sms.threadpool.ThreadPool;

 
 public class MemcacheCluster  {
	private final Logger logger = Logger.getLogger(this.getClass());

	// private final static String ORDER_KEY = "IMP_ORDER_KEY";
	private int keyExpiredDay = 7;// 默认7天
 	private MemcachedService memcachedService;

	public void setMemcachedService(MemcachedService memcachedService) {
		this.memcachedService = memcachedService;
	}

	private static MemcacheCluster redisCluster = new MemcacheCluster();
 

	public static MemcacheCluster getInstance() {
		
		return redisCluster;
	}

	public synchronized void setList(final String key, final String value) {

		ThreadPool.getInstance().executeTask(new Runnable() {
			public void run() {

				if (memcachedService == null || StringUtils.isBlank(key)) {
					logger.error("redis 未获取到cluster实例" + ",key:" + key);
					return;
				}
				try {
					Date date = new Date();
					String idxValue = key + "@";
					String json = memcachedService.get(idxValue);
					JSONObject data = null;
					if (StringUtils.isBlank(json)) {
						data = JSON.parseObject(json);
					} else {
						data = new JSONObject();
					}
					data.put(String.valueOf(date.getTime()), value);
					memcachedService.set(idxValue, keyExpiredDay * 86400000, data.toString());

					// jedisCluster.zadd(ORDER_KEY,-date.getTime(),key);
					// jedisCluster.zadd(key, -date.getTime(),idxValue);
					// jedisCluster.set(idxValue,value);
					// memcachedService.set(key, exp, value)
					//// jedisCluster.pexpire(ORDER_KEY, keyExpiredDay*86400000);
					// jedisCluster.pexpire(key, keyExpiredDay*86400000);
					// jedisCluster.pexpire(idxValue, keyExpiredDay*86400000);
				} catch (Exception e) {
					logger.error("redis setList 异常," + e.getMessage() + ",key:" + key + ",value:" + value);
				}
			}
		});

	}

	public synchronized List<String> getList(String key) {
		List<String> list = new ArrayList<String>();
		if (memcachedService == null || StringUtils.isBlank(key)) {
			logger.error("redis 未获取到cluster实例" + ",key:" + key);
			return list;
		}

		try {
			String idxValue = key + "@";

			String json = memcachedService.get(idxValue);
			if (StringUtils.isNotBlank(json)) {
				TreeMap<String, Object> map = JSON.parseObject(json, TreeMap.class);

				for (Map.Entry<String, Object> entry : map.entrySet()) {
					list.add(entry.getKey() + "@" + entry.getValue());
				}
			}

		} catch (Exception e) {
			logger.error("redis getList 异常," + e.getMessage() + ",key:" + key);
		}

		return list;
	}

 

}
