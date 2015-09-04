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
package net.paoding.rose.jade.dataaccess;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import net.paoding.rose.jade.statement.StatementMetaData;

/**
 * 框架内部使用的 {@link DataAccessFactory}实现，适配到 {@link DataSourceFactory}
 * ，由后者提供最终的数据源
 * 
 * @see DataSourceFactory
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class DataAccessFactoryAdapter implements DataAccessFactory {

    private final DataSourceFactory dataSourceFactory;
    private final ConcurrentHashMap<DataSource, DataAccess> dataAccessCache ;

    public DataAccessFactoryAdapter(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
        this.dataAccessCache = new ConcurrentHashMap<DataSource, DataAccess>();
    }

    public DataSourceFactory getDataSourceFactory() {
        return dataSourceFactory;
    }

    @Override
    public DataAccess getDataAccess(StatementMetaData metaData, Map<String, Object> attributes) {
        DataSourceHolder holder = dataSourceFactory.getHolder(metaData, attributes);
        while (holder != null && holder.isFactory()) {
            holder = holder.getFactory().getHolder(metaData, attributes);
        }
        if (holder == null || holder.getDataSource() == null) {
            throw new NullPointerException("cannot found a dataSource for: " + metaData);
        }
        DataSource dataSource = holder.getDataSource();
        DataAccess dataAccess = dataAccessCache.get(dataSource);
        if (dataAccess == null) {
            dataAccessCache.putIfAbsent(dataSource, new DataAccessImpl(dataSource));
            dataAccess = dataAccessCache.get(dataSource);
        }
        return dataAccess;
    }
}
