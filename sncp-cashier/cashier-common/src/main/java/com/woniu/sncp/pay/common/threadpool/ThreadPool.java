package com.woniu.sncp.pay.common.threadpool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * 异步线程池,通过spring注入初始化参数
 * 
 * @author Yang Hao
 * 
 */
@Service("threadPool")
public class ThreadPool {

	private static final Logger logger = LoggerFactory.getLogger(ThreadPool.class);

	private static ThreadPool threadPool;
	
	private ThreadPoolExecutor threadPoolExecutor;

	@Value("${cashier.thread.core.poolsize}")
	int corePoolSize;
	@Value("${cashier.thread.maximum.poolsize}")
	int maximumPoolSize;
	@Value("${cashier.thread.keepalive.time}")
	long keepAliveTime;
	@Value("${cashier.thread.blockingqueue.num}")
	int blockingQueueNum;

	private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
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
			logger.error("error", e);;
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

	public static ThreadPool getInstance(){
		if(threadPool == null){
			threadPool =  new ThreadPool();
		}
		
		return threadPool;
	}

}
