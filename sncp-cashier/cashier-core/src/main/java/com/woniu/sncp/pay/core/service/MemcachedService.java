package com.woniu.sncp.pay.core.service;

import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class MemcachedService {
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource(name="xmemcachedClient")
	private MemcachedClient memcachedClient;
	
	public MemcachedClient getClient(){
		return memcachedClient;
	}
	
	public boolean set(final String key, final int exp, final Object value){
		boolean result = false;
		try {
			result = memcachedClient.set(key,exp, value);
		} catch (TimeoutException e) {
			logger.error(e.getMessage(),e);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(),e);
		} catch (MemcachedException e) {
			logger.error(e.getMessage(),e);
		}
		
		return result;
	}
	
	public <T> T get(final String key){
		Object object = null;
		try {
			object = memcachedClient.get(key);
		} catch (TimeoutException e) {
			logger.error(e.getMessage(),e);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(),e);
		} catch (MemcachedException e) {
			logger.error(e.getMessage(),e);
		}
		
		return (T) object;
	}
	
	public boolean delete(final String key){
		boolean result = false;
		try {
			result = memcachedClient.delete(key);
		} catch (TimeoutException e) {
			logger.error(e.getMessage(),e);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(),e);
		} catch (MemcachedException e) {
			logger.error(e.getMessage(),e);
		}
		
		return result;
	}
}
