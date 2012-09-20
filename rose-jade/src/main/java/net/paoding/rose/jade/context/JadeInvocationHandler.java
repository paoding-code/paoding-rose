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
package net.paoding.rose.jade.context;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.annotation.SQLParam;
import net.paoding.rose.jade.annotation.SQLType;
import net.paoding.rose.jade.dataaccess.DataAccessFactory;
import net.paoding.rose.jade.rowmapper.RowMapperFactory;
import net.paoding.rose.jade.statement.DAOMetaData;
import net.paoding.rose.jade.statement.Interpreter;
import net.paoding.rose.jade.statement.InterpreterFactory;
import net.paoding.rose.jade.statement.JdbcStatement;
import net.paoding.rose.jade.statement.Querier;
import net.paoding.rose.jade.statement.SelectQuerier;
import net.paoding.rose.jade.statement.Statement;
import net.paoding.rose.jade.statement.StatementMetaData;
import net.paoding.rose.jade.statement.StatementWrapperProvider;
import net.paoding.rose.jade.statement.UpdateQuerier;
import net.paoding.rose.jade.statement.cached.CacheProvider;
import net.paoding.rose.jade.statement.cached.CachedStatement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowMapper;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class JadeInvocationHandler implements InvocationHandler {

    private static final Log logger = LogFactory.getLog(JadeInvocationHandler.class);

    private final ConcurrentHashMap<Method, Statement> statements = new ConcurrentHashMap<Method, Statement>();

    private final DAOMetaData daoMetaData;

    private final DataAccessFactory dataAccessFactory;

    private final RowMapperFactory rowMapperFactory;

    private final InterpreterFactory interpreterFactory;

    private final CacheProvider cacheProvider;
    
    private final StatementWrapperProvider statementWrapperProvider;
    
    public JadeInvocationHandler(//
            DAOMetaData daoMetaData,//
            InterpreterFactory interpreterFactory, //
            RowMapperFactory rowMapperFactory,//
            DataAccessFactory dataAccessFactory,//
            CacheProvider cacheProvider, //
            StatementWrapperProvider statementWrapperProvider) {
        this.daoMetaData = daoMetaData;
        this.rowMapperFactory = rowMapperFactory;
        this.dataAccessFactory = dataAccessFactory;
        this.interpreterFactory = interpreterFactory;
        this.cacheProvider = cacheProvider;
        this.statementWrapperProvider = statementWrapperProvider;
    }

    private static final String[] INDEX_NAMES = new String[] { ":1", ":2", ":3", ":4", ":5", ":6",
            ":7", ":8", ":9", ":10", ":11", ":12", ":13", ":14", ":15", ":16", ":17", ":18", ":19",
            ":20", ":21", ":22", ":23", ":24", ":25", ":26", ":27", ":28", ":29", ":30", };

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final boolean debugEnabled = logger.isDebugEnabled();
        if (debugEnabled) {
            logger.debug("invoking " + daoMetaData.getDAOClass().getName() + "#" + method.getName());
        }

        // 调用object的方法
        if (method.getDeclaringClass() == Object.class) {
            return invokeObjectMethod(proxy, method, args);
        }
        // 获取当前DAO方法对应的Statement对象
        Statement statement = getStatement(method);
        //
        // 将参数放入  Map
        Map<String, Object> parameters;
        StatementMetaData statemenetMetaData = statement.getMetaData();
        if (args == null || args.length == 0) {
            parameters = new HashMap<String, Object>(4);
        } else {
            parameters = new HashMap<String, Object>(args.length * 2 + 4);
            for (int i = 0; i < args.length; i++) {
                parameters.put(INDEX_NAMES[i], args[i]);
                SQLParam sqlParam = statemenetMetaData.getSQLParamAt(i);
                if (sqlParam != null) {
                    parameters.put(sqlParam.value(), args[i]);
                }
            }
        }
        // logging
        StringBuilder invocationInfo = null;
        if (debugEnabled) {
            invocationInfo = getInvocationInfo(statemenetMetaData, parameters);
            logger.debug("invoking " + invocationInfo.toString());
        }

        // executing
        long begin = System.currentTimeMillis();
        final Object result = statement.execute(parameters);
        long cost = System.currentTimeMillis() - begin;

        // logging
        if (logger.isInfoEnabled()) {
            if (invocationInfo == null) {
                invocationInfo = getInvocationInfo(statemenetMetaData, parameters);
            }
            logger.info("cost " + cost + "ms: " + invocationInfo);
        }
        return result;
    }

    private StringBuilder getInvocationInfo(StatementMetaData metaData,
            Map<String, Object> parameters) {
        StringBuilder sb = new StringBuilder();
        sb.append(metaData).append("\n");
        sb.append("\tsql: ").append(metaData.getSQL()).append("\n");
        sb.append("\tparameters: ");
        ArrayList<String> keys = new ArrayList<String>(parameters.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            sb.append(key).append("='").append(parameters.get(key)).append("'  ");
        }
        return sb;
    }

    private Statement getStatement(Method method) {
        Statement statement = statements.get(method);
        if (statement == null) {
            synchronized (method) {
                statement = statements.get(method);
                if (statement == null) {
                    StatementMetaData smd = new StatementMetaData(daoMetaData, method);
                    SQLType sqlType = smd.getSQLType();
                    Querier querier;
                    if (sqlType == SQLType.READ) {
                        RowMapper rowMapper = rowMapperFactory.getRowMapper(smd);
                        querier = new SelectQuerier(dataAccessFactory, smd, rowMapper);
                    } else {
                        querier = new UpdateQuerier(dataAccessFactory, smd);
                    }
                    Interpreter[] interpreters = interpreterFactory.getInterpreters(smd);
                    statement = new JdbcStatement(smd, sqlType, interpreters, querier);
                    if (cacheProvider != null) {
                        statement = new CachedStatement(cacheProvider, statement);
                    }
                    statements.put(method, wrap(statement));
                }
            }
        }
        return statement;
    }
    
    private Statement wrap(Statement statement) {
        if (statementWrapperProvider != null) {
            return statementWrapperProvider.wrap(statement);
        }
        return statement;
    }

    private Object invokeObjectMethod(Object proxy, Method method, Object[] args)
            throws CloneNotSupportedException {
        String methodName = method.getName();
        if (methodName.equals("toString")) {
            return JadeInvocationHandler.this.toString();
        }
        if (methodName.equals("hashCode")) {
            return daoMetaData.getDAOClass().hashCode() * 13 + this.hashCode();
        }
        if (methodName.equals("equals")) {
            return args[0] == proxy;
        }
        if (methodName.equals("clone")) {
            throw new CloneNotSupportedException("clone is not supported for jade dao.");
        }
        throw new UnsupportedOperationException(daoMetaData.getDAOClass().getName() + "#"
                + method.getName());
    }

    @Override
    public String toString() {
        DAO dao = daoMetaData.getDAOClass().getAnnotation(DAO.class);
        String toString = daoMetaData.getDAOClass().getName()//
                + "[catalog=" + dao.catalog() + "]";
        return toString;
    }

}
