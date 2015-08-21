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
package net.paoding.rose.jade.context.spring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.dataaccess.DataSourceFactory;
import net.paoding.rose.jade.dataaccess.DataSourceHolder;
import net.paoding.rose.jade.statement.StatementMetaData;

import org.apache.commons.lang.IllegalClassException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 
 * @author qieqie.wang
 */
public class SpringDataSourceFactory implements DataSourceFactory, ApplicationContextAware {

    private Log logger = LogFactory.getLog(getClass());

    private ListableBeanFactory applicationContext;

    private ConcurrentHashMap<Class<?>, DataSourceHolder> cachedDataSources = new ConcurrentHashMap<Class<?>, DataSourceHolder>();

    public SpringDataSourceFactory() {
    }

    public SpringDataSourceFactory(ListableBeanFactory applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public DataSourceHolder getHolder(StatementMetaData metaData,
            Map<String, Object> runtimeProperties) {
        Class<?> daoClass = metaData.getDAOMetaData().getDAOClass();
        DataSourceHolder holder = cachedDataSources.get(daoClass);
        if (holder != null) {
            return holder;
        }

        holder = getDataSourceByDirectory(daoClass, daoClass.getName());
        if (holder != null) {
            cachedDataSources.put(daoClass, holder);
            return holder;
        }
        String catalog = daoClass.getAnnotation(DAO.class).catalog();
        if (catalog.length() > 0) {
            holder = getDataSourceByDirectory(daoClass, catalog + "." + daoClass.getSimpleName());
        }
        if (holder != null) {
            cachedDataSources.put(daoClass, holder);
            return holder;
        }
        holder = getDataSourceByKey(daoClass, "jade.dataSource");
        if (holder != null) {
            cachedDataSources.put(daoClass, holder);
            return holder;
        }
        holder = getDataSourceByKey(daoClass, "dataSource");
        if (holder != null) {
            cachedDataSources.put(daoClass, holder);
            return holder;
        }
        return null;
    }

    private DataSourceHolder getDataSourceByDirectory(Class<?> daoClass, String catalog) {
        String tempCatalog = catalog;
        DataSourceHolder dataSource;
        while (tempCatalog != null && tempCatalog.length() > 0) {
            dataSource = getDataSourceByKey(daoClass, "jade.dataSource." + tempCatalog);
            if (dataSource != null) {
                return dataSource;
            }
            int index = tempCatalog.lastIndexOf('.');
            if (index == -1) {
                tempCatalog = null;
            } else {
                tempCatalog = tempCatalog.substring(0, index);
            }
        }
        return null;
    }

    private DataSourceHolder getDataSourceByKey(Class<?> daoClass, String key) {
        if (applicationContext.containsBean(key)) {
            Object dataSource = applicationContext.getBean(key);
            if (!(dataSource instanceof DataSource) && !(dataSource instanceof DataSourceFactory)) {
                throw new IllegalClassException("expects DataSource or DataSourceFactory, but a "
                        + dataSource.getClass().getName());
            }
            if (logger.isDebugEnabled()) {
                logger.debug("found dataSource: " + key + " for DAO " + daoClass.getName());
            }
            return new DataSourceHolder(dataSource);
        }
        return null;
    }
}
