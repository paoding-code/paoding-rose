/*
 * Copyright 2009-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License i distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.paoding.rose.jade.statement.cached;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 提供 ConcurrentHashMap 缓存池的 {@link CacheInterface} 实现。
 * 
 * @author han.liao
 */
public class MockCache implements CacheInterface {

    private static Log logger = LogFactory.getLog(MockCache.class);

    private ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<String, Object>();

    private String poolName; // 缓存名称

    private int maxSize = 100; // 默认值

    public MockCache(String poolName) {
        this.poolName = poolName;
    }

    public MockCache(String poolName, int maxSize) {
        this.poolName = poolName;
        this.maxSize = maxSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public Object get(String key) {

        Object value = map.get(key);

        if (logger.isDebugEnabled()) {
            logger.debug("Get cache \'" + key + "\' from pool \'" + poolName + "\': " + value);
        }

        return value;
    }

    @Override
    public boolean set(String key, Object value, int expiry) {

        if (logger.isDebugEnabled()) {
            logger.debug("Set cache \'" + key + "\' to pool \'" + poolName + "\': " + value);
        }

        if (map.size() >= maxSize) {
            map.remove(map.keys().nextElement());
        }

        map.put(key, value);
        return true;
    }

    @Override
    public boolean delete(String key) {

        if (logger.isDebugEnabled()) {
            logger.debug("Remove cache \'" + key + "\' from pool \'" + poolName + "\'.");
        }

        map.remove(key);
        return true;
    }
}
