package com.woniu.sncp.cbss.core.call.http.alert;

public class Metric {
	private String metricType;
	private String metricValue;
	private String tags;
	
	public Metric(String metricType, String metricValue, String tags)
	{
		setMetricType(metricType);
		setMetricValue(metricValue);
		setTags(tags);
	}
	
	public String getMetricType() {
		return metricType;
	}
	public void setMetricType(String metricType) {
		this.metricType = metricType;
	}
	public String getMetricValue() {
		return metricValue;
	}
	public void setMetricValue(String metricValue) {
		this.metricValue = metricValue;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
	
	

}
