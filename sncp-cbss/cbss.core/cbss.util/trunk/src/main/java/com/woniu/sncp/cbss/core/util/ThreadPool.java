package com.woniu.sncp.cbss.core.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;//import org.apache.log4j.Logger;;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 异步线程池,通过spring注入初始化参数
 * 
 * @author Yang Hao
 * 
 */
@Component
@ConfigurationProperties(value="cbss.api.threadpool.conf")
public class ThreadPool {

	private static final Logger logger = LoggerFactory.getLogger(ThreadPool.class);

	private ThreadPoolExecutor threadPoolExecutor;
	
	private int corePoolSize, maximumPoolSize;
	private long keepAliveTime;
	private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
	private int blockingQueueNum;

	/**
	 * 初始化 - 获取threadPoolExecutor
	 */
	@PostConstruct
	public void init() {
		threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, timeUnit,
				new ArrayBlockingQueue<Runnable>(blockingQueueNum), new ThreadPoolExecutor.DiscardOldestPolicy());
	}

	/**
	 * 添加并执行任务
	 * 
	 * @param task
	 */
	public Future<?> executeTask(Runnable task) {
		try {
			return threadPoolExecutor.submit(task);
		} catch (Exception e) {
			logger.error("",e);
		}
		return null;
	}
	
	/**
	 * 添加并执行任务
	 * 
	 * @param task
	 */
	public Future<String> executeTask(Callable task) {
		try {
			return threadPoolExecutor.submit(task);
		} catch (Exception e) {
			logger.error("", e);
		}
		return null;
	}

	public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
		this.threadPoolExecutor = threadPoolExecutor;
	}

	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public void setMaximumPoolSize(int maximumPoolSize) {
		this.maximumPoolSize = maximumPoolSize;
	}

	public void setKeepAliveTime(long keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
	}

	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

	public void setBlockingQueueNum(int blockingQueueNum) {
		this.blockingQueueNum = blockingQueueNum;
	}

}
