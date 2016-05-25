package cbss.core.repository.test;

import java.util.Calendar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cbss.core.repository.Main;
import cbss.core.repository.redis.RedisClusterConnection;
import cbss.core.repository.redis.RedisService;
import cbss.core.repository.zookeeper.ZooKeeperFactory;
import cbss.core.repository.zookeeper.ZookeeperConfValue;

@RunWith(SpringJUnit4ClassRunner.class)
@ComponentScan
@SpringApplicationConfiguration(classes = Main.class)
public class ZookeeperFactoryTest {

	@Autowired
	private ZooKeeperFactory zooKeeperFactory;

	@Autowired
	private RedisClusterConnection redisClusterConnection;

	@Autowired
	private RedisService redisService;

	@Test
	public void testRedisService() throws InterruptedException {

		redisService.set("a", "a", 3);
		System.out.println(redisService.get("a"));

		redisService.set("a", "b", 3);
//		Thread.sleep(2000);
		System.out.println(redisService.get("a"));

		redisService.set("a", "c", 3);
//		Thread.sleep(3000);
		System.out.println(redisService.get("a"));

		redisService.set("a", "c1", 3);
//		Thread.sleep(4000);
		System.out.println(redisService.get("a"));
	}

	@Test
	public void testRedis() {
		try {
			org.springframework.data.redis.connection.RedisClusterConnection connection = redisClusterConnection.connectionFactory().getClusterConnection();
			long time = Calendar.getInstance().getTimeInMillis();
			connection.set("TEST1234".getBytes(), String.valueOf(time).getBytes());

			byte[] value = connection.get("TEST1234".getBytes());
			if (String.valueOf(time).equals(new String(value))) {
				System.out.println("true");
			} else {
				System.out.println("false");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test() {
		try {
			zooKeeperFactory.getData("/a", new ZookeeperConfValue() {
				@Override
				public void value(String value) {
					System.out.println("value:" + value);
				}
			});
			Thread.sleep(Long.MAX_VALUE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetDate() {
		try {
			System.out.println(zooKeeperFactory.getData("/a"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
