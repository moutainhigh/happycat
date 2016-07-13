package com.woniu.sncp.cbss.api.manager.init.health;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.math.BigDecimal;

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
import com.woniu.sncp.cbss.core.util.PidUtil;

@Component
@ConfigurationProperties(value = "cbss.api.application.gc.cpu.conf")
public class CpuHealth implements HealthIndicator {

	private String metricUrl = "http://monitor.yunwei.woniu.com:81/api/push_monitor/";
	private String metricType = "sncp.mirco.precent.cpu";
	private String tags = "";

	private int maxcycle = 3;
	private int timeout = 30000;

	private int cpuPrecentMax = 90;
	private int cpuGcTimes = 1;

	@Autowired
	private AlertService alertService;

	private long cpuPrecent() {
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		int pid = Integer.valueOf(runtimeMXBean.getName().split("@")[0]).intValue();
		return BigDecimal.valueOf(PidUtil.getProcCpu(pid)).longValue();
	}

	private boolean gc(long useMemPrecent) {
		if (useMemPrecent >= cpuPrecentMax) {
			System.gc();
			return true;
		}
		return false;
	}

	private int cpuGC(int time, long useMemPrecent) {
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
		long cpuPrecent = cpuPrecent();
		int index = cpuGC(cpuGcTimes, cpuPrecent);
		if (index >= cpuGcTimes) {
			tags = (index) + "," + cpuPrecent + "%," + cpuPrecentMax + "%";
			Metric metrics = new Metric(metricType, String.valueOf(cpuPrecent), !StringUtils.isEmpty(tags) ? tags + "," + ipport : ipport);
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
			build.withDetail("UseCpuPrecent", cpuPrecent + ",OK");
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
