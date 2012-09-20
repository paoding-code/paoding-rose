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

import net.paoding.rose.jade.statement.StatementMetaData;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 框架内部使用的 {@link DataAccessFactory}实现，适配到 {@link DataSourceFactory}
 * ，由后者提供最终的数据源
 * 
 * @see DataSourceFactory
 * 
 * @author qieqie
 * 
 */
public class DataAccessFactoryAdapter implements DataAccessFactory {

    protected final DataSourceFactory dataSourceFactory;

    public DataAccessFactoryAdapter(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    public DataSourceFactory getDataSourceFactory() {
        return dataSourceFactory;
    }

    @Override
    public DataAccess getDataAccess(StatementMetaData metaData, Map<String, Object> runtime) {
        DataSourceHolder holder = dataSourceFactory.getHolder(metaData, runtime);
        while (holder != null && holder.isFactory()) {
            holder = holder.getFactory().getHolder(metaData, runtime);
        }
        if (holder == null || holder.getDataSource() == null) {
            throw new NullPointerException("cannot found a dataSource for: " + metaData);
        }
        JdbcTemplate jdbcTemplate = new JdbcTemplate(holder.getDataSource());
        return new DataAccessImpl(jdbcTemplate);
    }
}
