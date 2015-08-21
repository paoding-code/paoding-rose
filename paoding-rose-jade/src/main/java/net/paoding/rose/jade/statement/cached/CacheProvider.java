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

import net.paoding.rose.jade.statement.StatementMetaData;

/**
 * 定义 CacheProvider 接口从缓存池名称获取实例。
 * 
 * @author han.liao
 */
public interface CacheProvider {

    /**
     * 从缓存池的名称获取实例。
     * 
     * @param poolName - 缓存池的名称
     * 
     * @return 缓存池实例
     */
    CacheInterface getCacheByPool(StatementMetaData metaData, String poolName);
}
