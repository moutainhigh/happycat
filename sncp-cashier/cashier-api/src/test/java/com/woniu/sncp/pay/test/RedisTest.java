package com.woniu.sncp.pay.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import redis.clients.jedis.JedisCluster;


/**
 * Created by lujun.chen on 2017/3/3.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(com.woniu.sncp.pay.CashierAppRun.class)
public class RedisTest {

    @Autowired
    private JedisCluster jedisCluster;

    @Test
    public void get(){
    	jedisCluster.set("youqian-spread-sync-to-mysql-date", ""+System.currentTimeMillis());
       System.out.println("=============="+jedisCluster.get("youqian-spread-sync-to-mysql-date"));
    }

}
