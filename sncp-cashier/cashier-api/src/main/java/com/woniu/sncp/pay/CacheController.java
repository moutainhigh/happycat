package com.woniu.sncp.pay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.woniu.common.memcache.MemcacheCluster;

/**
 * 缓存管理
 * 
 * @author luzz
 * 
 */
@Controller
public class CacheController {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@RequestMapping("/cache/get/{key}")
	public @ResponseBody List<Map<String, String>> getCache(@PathVariable("key") String key) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String[] keys = key.split(":");
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		for (String k : keys) {
			if (StringUtils.isBlank(k))
				continue;
			List<String> temp = MemcacheCluster.getInstance().getList(k);
			for (String string : temp) {
				if (StringUtils.isNotBlank(string)) {
					Map<String, String> item = null;
					item = new HashMap<String, String>();
					item.put("orderNo", key);
					item.put("time", sdf.format(new Date(Long.valueOf(string.split("@")[0]))));
					item.put("message", string.split("@")[1]);

					result.add(item);
				}
			}

		}

		return result;
	}

}
