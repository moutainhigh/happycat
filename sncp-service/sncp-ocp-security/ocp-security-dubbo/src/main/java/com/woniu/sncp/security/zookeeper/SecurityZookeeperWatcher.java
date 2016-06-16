package com.woniu.sncp.security.zookeeper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.woniu.sncp.security.service.OcpSecurityService;

@Component
public class SecurityZookeeperWatcher implements NodeCacheListener, InitializingBean,DisposableBean  {

	@Autowired
	private CuratorFramework client;
	
	@Autowired
	private OcpSecurityService ocpSecurityService;
	
	private NodeCache nodeCache;

	private static final String SECURITY_ZK_PATH = "/com.snail.ocp.security.acl.update/aclCache";

	/**
	 * 初始化连接zookeeper
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(client, "zookeeper clinet is required");
		client.start();
		Stat stat = client.checkExists().forPath(SECURITY_ZK_PATH); 
		if(stat == null) {
			client.create().creatingParentsIfNeeded().forPath(SECURITY_ZK_PATH, "1".getBytes());
		}
		
		/**
		 * 在注册监听器的时候，如果传入此参数，当事件触发时，逻辑由线程池处理
		 */
		ExecutorService pool = Executors.newFixedThreadPool(2);
		/**
		 * 监听数据节点的变化情况
		 */
		nodeCache = new NodeCache(client, SECURITY_ZK_PATH, false);
		nodeCache.start(true);
		nodeCache.getListenable().addListener(this, pool);
	}

	@Override
	public void nodeChanged() throws Exception {
		ocpSecurityService.reload();
	}

	@Override
	public void destroy() throws Exception {
		if(nodeCache != null) {
			nodeCache.close();
		}
		if(client != null) {
			client.close();
		}
	}

}
