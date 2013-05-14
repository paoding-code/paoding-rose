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

import javax.sql.DataSource;

/**
 * 用于表示一个 {@link DataSource} 或 {@link DataSourceFactory} 类。一个
 * {@link DataSourceHolder} 有且只能表示这两种类型其中之一
 * <p>
 * 
 * @see DataSourceFactory
 * @author qieqie.wang
 * 
 */
public class DataSourceHolder {

    private final DataSource dataSource;

    private final DataSourceFactory dataSourceFactory;

    /**
     * 构造一个holder实例，所提供的参数必须是 {@link DataSource} 或
     * {@link DataSourceFactory}类型
     * 
     * @throws IllegalArgumentException
     * 
     * @param dataSourceOrItsFactory
     */
    public DataSourceHolder(Object dataSourceOrItsFactory) {
        if (dataSourceOrItsFactory instanceof DataSource) {
            this.dataSource = (DataSource) dataSourceOrItsFactory;
            this.dataSourceFactory = null;
            return;
        }
        if (dataSourceOrItsFactory instanceof DataSourceFactory) {
            this.dataSource = null;
            this.dataSourceFactory = (DataSourceFactory) dataSourceOrItsFactory;
            return;
        }
        throw new IllegalArgumentException("" + dataSourceOrItsFactory);
    }

    //------------------

    /**
     * 包含的是一个DataSourceFactory?
     */
    public boolean isFactory() {
        return this.dataSourceFactory != null;
    }

    /**
     * 返回所代表的 {@link DataSource}，如不是返回null
     * 
     * @return
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * 返回所代表的 {@link DataSourceFactory}，如不是返回null
     * 
     * @return
     */
    public DataSourceFactory getFactory() {
        return dataSourceFactory;
    }

}
