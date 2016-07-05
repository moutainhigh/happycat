package com.woniu.sncp.cbss.core.call.http.alert;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.woniu.sncp.cbss.core.call.http.Http;
import com.woniu.sncp.cbss.core.util.ThreadPool;

@Component
public class AlertService {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Autowired
	private ThreadPool threadPool;
	@Autowired
	private Http http;

	public String metric(String metricUrl, Metric metrics, int maxcycle, int timeout)
			throws InterruptedException, ExecutionException {
		ArrayList<Metric> al = new ArrayList<Metric>();
		al.add(metrics);
		return metric(metricUrl, al, maxcycle, timeout);
	}

	public String metric(String metricUrl, List<Metric> metrics, int maxcycle, int timeout)
			throws InterruptedException, ExecutionException {

		if (maxcycle <= 0) {
			throw new IllegalArgumentException("maxcycle not allow <=0 ");
		}

		Future<String> alterresult = threadPool.executeTask(new Callable<String>() {
			@Override
			public String call()
					throws Exception {
				List datas = new ArrayList();
				for (Metric metric1 : metrics) {
					Map<String, Object> data = new HashMap<String, Object>();
					data.put("metric", metric1.getMetricType());
					data.put("value", metric1.getMetricValue());
					data.put("tags", metric1.getTags());
					datas.add(data);
				}
				String rtn1 = "";
				Map<String, Object> headers = new HashMap<String, Object>();
				headers.put("Content-Type", "application/x-www-form-urlencoded");
				try {
					Map<String, Object> rtn = http.post(metricUrl, headers, JSONObject.toJSONString(datas), timeout, "utf-8");
					String code = String.valueOf(JSONObject.parseObject(String.valueOf(rtn.get("HTTPRSPINFO"))).get("code"));
					if (1 != Integer.parseInt(code)) {
						return "code:" + code;
					}
					rtn1 = code;
				} catch (Exception e) {
					logger.error("monitor-submitAlertMsg1", e);
					for (int i = 0; i < maxcycle; i++) {
						try {
							Map<String, Object> rtn = http.post(metricUrl, headers, JSONObject.toJSONString(datas), timeout, "utf-8");
							String code = String.valueOf(JSONObject.parseObject(String.valueOf(rtn.get("HTTPRSPINFO"))).get("code"));
							if (1 != Integer.parseInt(code)) {
								return "code:" + code;
							}
							rtn1 = code;
						} catch (Exception e1) {
							logger.error("monitor-submitAlertMsg2", e1);
							if (i + 1 == maxcycle) {
								return "code:" + e1.getMessage();
							}
						}
					}
				}
				return rtn1;
			}
		});
		return alterresult.get();

	}

	public String heart(String serviceType, String url, String token, int maxcycle, int timeout)
			throws Exception {
		if (maxcycle <= 0) {
			throw new IllegalArgumentException("maxcycle not allow <=0 ");
		}

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("src", serviceType);
		data.put("token", token);
		String rtn1 = "";
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		try {
			Map<String, Object> rtn = http.post(url, headers, data, timeout, "utf-8");
			if (1 != Integer.parseInt(String.valueOf(rtn.get("code")))) {
				return "code:" + rtn.get("code");
			}
			rtn1 = String.valueOf(rtn.get("code"));
		} catch (Exception e) {
			logger.error("monitor-submitAlertMsg", e);
			for (int i = 0; i < maxcycle; i++) {
				try {
					Map<String, Object> rtn = http.post(url, headers, data, timeout, "utf-8");
					String code = String.valueOf(JSONObject.parseObject(String.valueOf(rtn.get("HTTPRSPINFO"))).get("code"));
					if (1 != Integer.parseInt(code)) {
						return "code:" + code;
					}
					rtn1 = String.valueOf(code);
				} catch (Exception e1) {
					logger.error("monitor-submitAlertMsg", e1);
					if (i + 1 == maxcycle) {
						return e1.getMessage();
					}
				}
			}
		} finally {
			logger.error("[" + url + "][" + serviceType + "][" + data + "][" + timeout + "][" + rtn1 + "]");
		}
		return rtn1;
	}

	public String submitAlertMsg(final String url, final String serviceType, final String content, final int maxcycle, final int timeout, final Map<?, ?> extend)
			throws InterruptedException, ExecutionException {
		if (maxcycle <= 0) {
			throw new IllegalArgumentException("maxcycle not allow <=0 ");
		}

		Future<String> alterresult = threadPool.executeTask(new Callable<String>() {
			@Override
			public String call()
					throws Exception {
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("src", serviceType);
				data.put("ctime", Calendar.getInstance().getTime().getTime() / 1000);
				data.put("content", content);
				data.put("extend", JSONObject.toJSONString(extend).toString());
				String rtn1 = "";
				Map<String, Object> headers = new HashMap<String, Object>();
				headers.put("Content-Type", "application/x-www-form-urlencoded");
				try {
					Map<String, Object> rtn = http.post(url, headers, data, timeout, "utf-8");
					if (1 != Integer.parseInt(String.valueOf(rtn.get("code")))) {
						return "code:" + rtn.get("code");
					}
					rtn1 = String.valueOf(rtn.get("code"));
				} catch (Exception e) {
					logger.error("monitor-submitAlertMsg", e);
					for (int i = 0; i < maxcycle; i++) {
						try {
							Map<String, Object> rtn = http.post(url, headers, data, timeout, "utf-8");
							String code = String.valueOf(JSONObject.parseObject(String.valueOf(rtn.get("HTTPRSPINFO"))).get("code"));
							if (1 != Integer.parseInt(code)) {
								return "code:" + code;
							}
							rtn1 = String.valueOf(code);
						} catch (Exception e1) {
							logger.error("monitor-submitAlertMsg", e1);
							if (i + 1 == maxcycle) {
								return e1.getMessage();
							}
						}
					}
				}
				return rtn1;
			}
		});
		return alterresult.get();
	}
}
