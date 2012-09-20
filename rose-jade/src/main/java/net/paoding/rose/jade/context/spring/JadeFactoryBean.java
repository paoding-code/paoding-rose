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

import java.lang.reflect.Proxy;

import net.paoding.rose.jade.context.JadeInvocationHandler;
import net.paoding.rose.jade.dataaccess.DataAccessFactory;
import net.paoding.rose.jade.rowmapper.RowMapperFactory;
import net.paoding.rose.jade.statement.DAOMetaData;
import net.paoding.rose.jade.statement.InterpreterFactory;
import net.paoding.rose.jade.statement.StatementWrapperProvider;
import net.paoding.rose.jade.statement.cached.CacheProvider;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 * 
 */
public class JadeFactoryBean implements FactoryBean, InitializingBean {

    protected Class<?> objectType;

    protected DataAccessFactory dataAccessFactory;

    protected RowMapperFactory rowMapperFactory;

    protected InterpreterFactory interpreterFactory;

    protected CacheProvider cacheProvider;

    protected Object daoObject;
    
    // 可选的
    private StatementWrapperProvider statementWrapperProvider;

    public JadeFactoryBean() {
    }

    @Override
    public Class<?> getObjectType() {
        return objectType;
    }

    public void setObjectType(Class<?> objectType) {
        this.objectType = objectType;
    }

    /**
     * 
     * @param dataAccessFactory
     */
    public void setDataAccessFactory(DataAccessFactory dataAccessFactory) {
        this.dataAccessFactory = dataAccessFactory;
    }

    public DataAccessFactory getDataAccessFactory() {
        return dataAccessFactory;
    }

    /**
     * 
     * @param rowMapperFactory
     */
    public void setRowMapperFactory(RowMapperFactory rowMapperFactory) {
        this.rowMapperFactory = rowMapperFactory;
    }

    public RowMapperFactory getRowMapperFactory() {
        return rowMapperFactory;
    }

    /**
     * 
     * @param interpreterFactory
     */
    public void setInterpreterFactory(InterpreterFactory interpreterFactory) {
        this.interpreterFactory = interpreterFactory;
    }

    public InterpreterFactory getInterpreterFactory() {
        return interpreterFactory;
    }

    public void setCacheProvider(CacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    public CacheProvider getCacheProvider() {
        return cacheProvider;
    }
    
    public StatementWrapperProvider getStatementWrapperProvider() {
        return statementWrapperProvider;
    }
    
    public void setStatementWrapperProvider(StatementWrapperProvider statementWrapperProvider) {
        this.statementWrapperProvider = statementWrapperProvider;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.isTrue(objectType.isInterface(), "not a interface class: " + objectType.getName());
        Assert.notNull(dataAccessFactory);
        Assert.notNull(rowMapperFactory);
        Assert.notNull(interpreterFactory);
        // cacheProvider可以null，不做assert.notNull判断
    }

    @Override
    public Object getObject() {
        if (daoObject == null) {
            daoObject = createDAO();
            Assert.notNull(daoObject);
        }
        return daoObject;
    }

    protected Object createDAO() {
        try {
            DAOMetaData daoMetaData = new DAOMetaData(objectType);
            JadeInvocationHandler handler = new JadeInvocationHandler(
                    //
                    daoMetaData, interpreterFactory, rowMapperFactory, dataAccessFactory,
                    cacheProvider, statementWrapperProvider);
            return Proxy.newProxyInstance(ClassUtils.getDefaultClassLoader(),
                    new Class[] { objectType }, handler);
        } catch (RuntimeException e) {
            throw new IllegalStateException("failed to create bean for "
                    + this.objectType.getName(), e);
        }
    }

}
