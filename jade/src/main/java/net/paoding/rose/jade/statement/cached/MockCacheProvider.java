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

import net.paoding.rose.jade.statement.StatementMetaData;

/**
 * 提供 ConcurrentHashMap 缓存池的 {@link CacheProvider} 实现。
 * 
 * @author han.liao
 */
public class MockCacheProvider implements CacheProvider {

    private ConcurrentHashMap<String, MockCache> caches = new ConcurrentHashMap<String, MockCache>();

    @Override
    public CacheInterface getCacheByPool(StatementMetaData metaData, String poolName) {
        MockCache cache = caches.get(poolName);
        if (cache == null) {
            cache = new MockCache(poolName);

            MockCache cacheExist = caches.putIfAbsent(poolName, cache);
            if (cacheExist != null) {
                cache = cacheExist;
            }
        }

        return cache;
    }
}
