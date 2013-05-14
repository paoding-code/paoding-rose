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

/**
 * {@link CacheInterface} 抽象DAO方法所使用的缓存接口
 * 
 * @author han.liao [in355hz@gmail.com]
 */
public interface CacheInterface {

    /**
     * 从缓存取出给定key对应的对象，如果没有则返回null
     * 
     */
    Object get(String key);

    /**
     * 将某个对象和给定的key绑定起来存储在缓存中
     * 
     * @param expiryInSecond - 缓存过期时间，单位为秒
     * 
     */
    boolean set(String key, Object value, int expiryInSecond);

    /**
     * 从 Cache 缓存池删除对象。
     * 
     * @param key - 缓存关键字
     */
    boolean delete(String key);
}
