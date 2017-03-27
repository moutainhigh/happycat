package com.woniu.sncp.pay.cache;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p>descrption: redis 集群配置</p>
 * 
 * @author fuzl
 * @date   2017年3月27日
 * @Copyright 2017 Snail Soft, Inc. All rights reserved.
 */
@Component
@ConfigurationProperties(prefix="spring.redis.cluster")
public class RedisClusterProperties {

	//集群节点
    private List<String> nodes=new ArrayList<>();

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
}
}
