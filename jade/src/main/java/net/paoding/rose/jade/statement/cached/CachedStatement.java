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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.paoding.rose.jade.annotation.Cache;
import net.paoding.rose.jade.annotation.CacheDelete;
import net.paoding.rose.jade.annotation.SQLType;
import net.paoding.rose.jade.statement.Statement;
import net.paoding.rose.jade.statement.StatementMetaData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapperImpl;

/**
 * {@link CachedStatement} 封装了支持Cache的逻辑
 * 
 * @author qieqie.wang
 * 
 */
public class CachedStatement implements Statement {

    /**
     * 实际底层Statemenet，比如就是数据库操作的实际执行语句
     */
    private final Statement realStatement;

    /**
     * 注解在DAO方法上的 {@link Cache}，如果没有则为null
     * <p>
     * 仅在该语句为查询语句时侯此有效，即如果某个更新语句的DAO方法上也注解了 {@link Cache} 在这里也仍保留null
     */
    private final Cache cacheAnnotation;

    /**
     * 注解在该语句上的 {@link CacheDelete}，如果没有则为null
     * <p>
     * 
     * 可使用在各种语句上，甚至事查询语句上(很悲剧的：我也不知道为何有这种用法，执行一次查询还会附带删除神马Cache的啊？真的有吗？)
     */
    private final CacheDelete cacheDeleteAnnotation;

    /**
     * cache服务接口
     */
    private final CacheProvider cacheProvider;

    /**
     * 
     * @param cacheProvider
     * @param realStatement
     */
    public CachedStatement(CacheProvider cacheProvider, Statement realStatement) {
        this.realStatement = realStatement;
        this.cacheProvider = cacheProvider;
        StatementMetaData metaData = realStatement.getMetaData();
        SQLType sqlType = metaData.getSQLType();
        cacheDeleteAnnotation = metaData.getMethod().getAnnotation(CacheDelete.class);
        Cache cacheAnnotation = metaData.getMethod().getAnnotation(Cache.class);
        if (sqlType == SQLType.READ) {
            this.cacheAnnotation = cacheAnnotation;
        } else {
            this.cacheAnnotation = null;
            if (cacheAnnotation != null) {
                Log logger = LogFactory.getLog(CachedStatement.class);
                logger.warn("@" + Cache.class.getName() + " is invalid for a " //
                        + sqlType + " SQL:" + metaData.getSQL());
            }
        }
    }

    @Override
    public StatementMetaData getMetaData() {
        return realStatement.getMetaData();
    }

    @Override
    public Object execute(Map<String, Object> parameters) {
        Object value = null;
        if (cacheAnnotation == null) {
            value = realStatement.execute(parameters);
        } else {
            CacheInterface cache = cacheProvider.getCacheByPool(//
                    getMetaData(), cacheAnnotation.pool());
            String cacheKey = buildKey(cacheAnnotation.key(), parameters);
            value = cache.get(cacheKey);
            if (value == null) {
                value = realStatement.execute(parameters);
                cache.set(cacheKey, value, cacheAnnotation.expiry());
            }
        }
        if (cacheDeleteAnnotation != null) {
            CacheInterface cache = cacheProvider.getCacheByPool(//
                    getMetaData(), cacheDeleteAnnotation.pool());
            for (String key : cacheDeleteAnnotation.key()) {
                String cacheKey = buildKey(key, parameters);
                cache.delete(cacheKey);
            }
        }
        return value;
    }

    // 参数的模板
    private static final Pattern PATTERN = Pattern.compile("\\:([a-zA-Z0-9_\\.]*)");

    /**
     * 查找模板 KEY 中所有的 :name, :name.property 参数替换成实际值。
     * 
     * @param key - 作为模板的 KEY
     * @param parameters - 传入的参数
     * 
     * @return 最终的缓存 KEY
     * @author 廖涵 in355hz@gmail.com
     */
    private static String buildKey(String key, Map<String, Object> parameters) {
        // 匹配符合  :name 格式的参数
        Matcher matcher = PATTERN.matcher(key);
        if (matcher.find()) {

            StringBuilder builder = new StringBuilder();

            int index = 0;

            do {
                // 提取参数名称
                final String name = matcher.group(1).trim();

                Object value = null;

                // 解析  a.b.c 类型的名称 
                int find = name.indexOf('.');
                if (find >= 0) {

                    // 用  BeanWrapper 获取属性值
                    Object bean = parameters.get(name.substring(0, find));
                    if (bean != null) {
                        value = new BeanWrapperImpl(bean)
                                .getPropertyValue(name.substring(find + 1));
                    }

                } else {

                    // 获取参数值
                    value = parameters.get(name);
                }

                // 拼装参数值
                builder.append(key.substring(index, matcher.start()));
                builder.append(value);

                index = matcher.end();

            } while (matcher.find());

            // 拼装最后一段
            builder.append(key.substring(index));

            return builder.toString();
        }

        return key;
    }
}
