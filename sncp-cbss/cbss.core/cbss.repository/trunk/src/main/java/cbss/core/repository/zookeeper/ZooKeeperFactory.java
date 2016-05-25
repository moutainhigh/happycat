package cbss.core.repository.zookeeper;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 *
 */
@Component
public class ZooKeeperFactory {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ZookeeperConfigurationProperties configurationProperties;

	private CuratorFramework client;
	private ExecutorService pool;

	@PostConstruct
	private CuratorFramework init() {

		pool = Executors.newFixedThreadPool(configurationProperties.getThreadPoolSize());

		RetryPolicy retryPolicy = new ExponentialBackoffRetry(configurationProperties.getBaseSleepTimeMS(), configurationProperties.getMaxRetrie());
		client = CuratorFrameworkFactory.builder().connectString(configurationProperties.getNodes()).retryPolicy(retryPolicy).namespace(configurationProperties.getNameSpace()).build();
		client.start();
		return client;
	}

	public boolean isAlive() {
		return client.getState().compareTo(CuratorFrameworkState.STARTED) == 0;
	}

	public String info() {
		return client.getZookeeperClient().getCurrentConnectionString();
	}

	public String getData(String path)
			throws Exception {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(client.getData().watched().forPath(path));
		String data = IOUtils.toString(byteArrayInputStream);
		return data;
	}

	/**
	 * 获取节点数据
	 * 
	 * @param path
	 *            mainpath + this.param
	 * @throws Exception
	 */
	public void getData(String path, ZookeeperConfValue v)
			throws Exception {
		NodeCache nodeCache = new NodeCache(client, path, false);
		nodeCache.start(true);
		nodeCache.getListenable().addListener(new NodeCacheListener() {
			@Override
			public void nodeChanged()
					throws Exception {
				String newValue = new String(nodeCache.getCurrentData().getData(), "utf-8");
				v.value(newValue);
				logger.info(path + ",nodeChanged:" + newValue);
			}
		}, pool);
	}

	public List<String> getChild(String path)
			throws Exception {
		return client.getChildren().forPath(path);
	}

	/**
	 * 监听子节点的变化情况
	 * 
	 * @param path
	 * @param v
	 * @throws Exception
	 */
	public void getChildData(String path, ZookeeperConfValue v)
			throws Exception {

		final PathChildrenCache childrenCache = new PathChildrenCache(client, path, true);
		childrenCache.start(StartMode.POST_INITIALIZED_EVENT);
		childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
			@Override
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event)
					throws Exception {
			}
		}, pool);
	}
}
