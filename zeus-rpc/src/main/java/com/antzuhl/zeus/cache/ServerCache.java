package com.antzuhl.zeus.cache;

import com.antzuhl.zeus.registry.ServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Funcation: zookeeper服务节点缓存
 *
 * @author antzuhl
 * */
@Component
public class ServerCache {

    @Autowired
    private LoadingCache<String, String> cache;

    @Autowired
    private ServiceRegistry serviceRegistry;

    private AtomicLong index = new AtomicLong();

    public void addCache(String key) {
        cache.put(key, key);
    }

    /**
     * 更新所有缓存/先删除 再新增
     *
     * @param currentChilds
     */
    public void updateCache(List<String> currentChilds) {
        cache.invalidateAll();
        for (String currentChild : currentChilds) {
            String key = currentChild.split("-")[1];
            addCache(key);
        }
    }


    /**
     * 获取所有的服务列表
     *
     * @return
     */
    public List<String> getAll() {

        List<String> list = new ArrayList<>();

        if (cache.size() == 0) {
            List<String> allNode = serviceRegistry.getAllNode();
            for (String node : allNode) {
                String key = node.split("-")[1];
                addCache(key);
            }
        }
        for (Map.Entry<String, String> entry : cache.asMap().entrySet()) {
            list.add(entry.getKey());
        }
        return list;

    }

    /**
     * 选取服务器
     *
     * @return
     */
    public String selectServer() {
        List<String> all = getAll();
        if (all.size() == 0) {
            throw new RuntimeException("路由列表为空");
        }
        Long position = index.incrementAndGet() % all.size();
        if (position < 0) {
            position = 0L;
        }

        return all.get(position.intValue());
    }
}
