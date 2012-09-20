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
package net.paoding.rose.jade.dataaccess.datasource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import net.paoding.rose.jade.dataaccess.DataSourceFactory;
import net.paoding.rose.jade.dataaccess.DataSourceHolder;
import net.paoding.rose.jade.statement.StatementMetaData;

import org.apache.commons.lang.StringUtils;

/**
 * package层级式的数据源工厂。
 * <p>
 * 
 * 本工厂实现可以注册多个不同的数据源，使用者需要为每个数据源设定一个以点号分隔的名字，形同一个package名称或class全名称。
 * 当框架要求本工厂为某个DAO方法提供数据源时，本工厂会从class全名开始逐级寻找是否有相应数据源被注册，如果有则返回。
 * 
 * @author qieqie.wang@gmail.com
 * 
 */
public class HierarchicalDataSourceFactory implements DataSourceFactory {

    /**
     * 注册的数据源，key为注册的名字
     */
    private ConcurrentHashMap<String, DataSourceHolder> dataSources = new ConcurrentHashMap<String, DataSourceHolder>();

    /**
     * 默认数据源，逐级没有找到相应的数据源时候返回默认数据源
     */
    private DataSourceHolder defaultDataSource;

    public HierarchicalDataSourceFactory() {
    }

    public HierarchicalDataSourceFactory(DataSource defaultDataSource) {
        this.defaultDataSource = new DataSourceHolder(defaultDataSource);
    }

    /**
     * 注册一个数据源，名称以点号为分隔分成多级。如果名称星号，表示默认数据源。
     * 
     * @param name
     * @param dataSource
     */
    public void registerDataSource(String name, DataSource dataSource) {
        if (dataSource == null) {
            throw new NullPointerException("dataSource");
        }
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("blank name");
        }
        if (name.equals("*")) {
            defaultDataSource = new DataSourceHolder(dataSource);
        } else {
            dataSources.putIfAbsent(name, new DataSourceHolder(dataSource));
        }
    }

    /**
     * 注册一个数据源工厂，名称以点号为分隔分成多级。如果名称星号，表示默认数据源工厂。
     * 
     * @param name
     * @param dataSource
     */

    public void registerDataSource(String name, DataSourceFactory dataSource) {
        if (dataSource == null) {
            throw new NullPointerException("dataSource");
        }
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("blank name");
        }
        if (name.equals("*")) {
            defaultDataSource = new DataSourceHolder(dataSource);
        } else {
            dataSources.putIfAbsent(name, new DataSourceHolder(dataSource));
        }
    }

    /**
     * 根据DAO的类全名、以点号为分隔逐级从注册的数据源中寻找存在的数据源，如果有则立即返回之
     */
    @Override
    public DataSourceHolder getHolder(StatementMetaData metaData, Map<String, Object> runtime) {
        String daoName = metaData.getDAOMetaData().getDAOClass().getName();
        String name = daoName;
        DataSourceHolder dataSource = dataSources.get(name);
        if (dataSource != null) {
            return dataSource;
        }
        while (true) {
            int index = name.lastIndexOf('.');
            if (index == -1) {
                dataSources.putIfAbsent(daoName, defaultDataSource);
                return defaultDataSource;
            }
            name = name.substring(0, index);
            dataSource = dataSources.get(name);
            if (dataSource != null) {
                dataSources.putIfAbsent(daoName, dataSource);
                return dataSource;
            }
        }
    }
}
