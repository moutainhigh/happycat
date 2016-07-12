package com.woniu.sncp.cbss.api.manager.init.health;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.woniu.sncp.cbss.core.call.http.alert.AlertService;
import com.woniu.sncp.cbss.core.call.http.alert.Metric;
import com.woniu.sncp.cbss.core.trace.aspect.listener.ServletContainerApplicationListener;
import com.woniu.sncp.cbss.core.util.IpUtils;

@Component
@ConfigurationProperties(value = "cbss.api.application.gc.conf")
public class RuntimeHealth implements HealthIndicator {

	private String metricUrl = "http://monitor.yunwei.woniu.com:81/api/push_monitor/";
	private String metricType = "sncp.micro.precent.memory";
	private String tags = "";

	private int maxcycle = 3;
	private int timeout = 30000;

	private int memoryPrecentMax = 90;
	private int memoryGcTimes = 3;

	@Autowired
	private AlertService alertService;

	private long memoryPrecent() {
		MemoryMXBean memoryMXBean = (MemoryMXBean) ManagementFactory.getMemoryMXBean();
		MemoryUsage memoryUsage = memoryMXBean.getHeapMemoryUsage();

		BigDecimal used = new BigDecimal(memoryUsage.getUsed());
		if (memoryUsage.getMax() > 0) {
			BigDecimal max = new BigDecimal(memoryUsage.getMax());
			long useMemPrecent = ((used).divide(max, 2, BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal(100)).longValue();
			return useMemPrecent;
		} else {
			return 0;
		}
	}
	
	@SuppressWarnings("unused")
	private long threadPrecent() {
		ThreadMXBean threadMXBean = (ThreadMXBean) ManagementFactory.getThreadMXBean();
		BigDecimal peak = new BigDecimal(threadMXBean.getPeakThreadCount());
		BigDecimal count = new BigDecimal(threadMXBean.getThreadCount());
		long threadPrecent = ((peak).divide(count, 2, BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal(100)).longValue();
		return threadPrecent;
	}

	private boolean gc(long useMemPrecent) {
		if (useMemPrecent >= memoryPrecentMax) {
			System.gc();
			return true;
		}
		return false;
	}

	private int memoryGC(int time, long useMemPrecent) {
		for (int i = 0; i < time; i++) {
			if (!gc(useMemPrecent)) {
				return i;
			} else {
				continue;
			}
		}
		return time;
	}

	@Override
	public Health health() {

		Builder build = null;
		String ipport = "localip=" + IpUtils.getLoaclAddr().replace(",", "#") + ",appport=" + ServletContainerApplicationListener.port;
		long useMemPrecent = memoryPrecent();
		int index = memoryGC(memoryGcTimes, useMemPrecent);
		if (index >= memoryGcTimes) {
			tags = (index) + "," + useMemPrecent + "%," + memoryPrecentMax + "%";
			Metric metrics = new Metric(metricType, String.valueOf(new Date().getTime()), !StringUtils.isEmpty(tags) ? tags + "," + ipport : ipport);
			try {
				String rtn = alertService.metric(metricUrl, metrics, getMaxcycle(), getTimeout());
				if (rtn.startsWith("code:")) {
					build = Health.down();
					build.withDetail("monitor metric errors:", String.format("[%s][%s][%s][%s] error :[%s]", metricUrl, metrics.getMetricType(), metrics.getMetricValue(), metrics.getTags(), rtn));
				} else {
					build = Health.up();
					build.withDetail("monitor metric success:", String.format("[%s][%s][%s][%s] success :[%s]", metricUrl, metrics.getMetricType(), metrics.getMetricValue(), metrics.getTags(), rtn));
				}
			} catch (Exception e) {
				build = Health.down();
				build.withDetail("monitor metric errors:",
						String.format("[%s][%s][%s][%s] error :[%s]", metricUrl, metrics.getMetricType(), metrics.getMetricValue(), metrics.getTags(), e.getMessage()));
			}
			return build.build();
		} else {
			build = Health.up();
			build.withDetail("UseMemoryPrecent", useMemPrecent + ",OK");
			return build.build();
		}
	}

	public String getMetricUrl() {
		return metricUrl;
	}

	public void setMetricUrl(String metricUrl) {
		this.metricUrl = metricUrl;
	}

	public String getMetricType() {
		return metricType;
	}

	public void setMetricType(String metricType) {
		this.metricType = metricType;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public int getMaxcycle() {
		return maxcycle;
	}

	public void setMaxcycle(int maxcycle) {
		this.maxcycle = maxcycle;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

}
