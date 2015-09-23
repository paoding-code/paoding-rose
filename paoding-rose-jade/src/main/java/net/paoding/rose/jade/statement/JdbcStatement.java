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
package net.paoding.rose.jade.statement;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import net.paoding.rose.jade.annotation.AfterInvocation;
import net.paoding.rose.jade.annotation.ReturnGeneratedKeys;
import net.paoding.rose.jade.annotation.SQLType;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class JdbcStatement implements Statement {

    private static final Log sqlLogger = LogFactory.getLog("jade_sql.log");

    private final StatementMetaData metaData;

    private final Interpreter[] interpreters;

    private final Querier querier;

    private final AfterInvocationCallback afterInvocationCallback;

    private final boolean batchUpdate;

    private final SQLType sqlType;

    private final String logPrefix;

    private static final AfterInvocationCallback nullAfterInvocationCallback = new AfterInvocationCallback() {

        @Override
        public Object execute(StatementRuntime runtime, Object returnValue) {
            return returnValue;
        }
    };

    public JdbcStatement(StatementMetaData statementMetaData, SQLType sqlType,
                         Interpreter[] interpreters, Querier querier) {
        this.metaData = statementMetaData;
        AfterInvocation afterInvocationAnnotation = metaData.getMethod()
            .getAnnotation(AfterInvocation.class);
        if (afterInvocationAnnotation != null) {
            try {
                this.afterInvocationCallback = afterInvocationAnnotation.value().newInstance();
            } catch (InstantiationException e) {
                throw new IllegalArgumentException(e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            this.afterInvocationCallback = nullAfterInvocationCallback;
        }
        this.interpreters = (interpreters == null) ? new Interpreter[0] : interpreters;
        this.querier = querier;
        this.sqlType = sqlType;
        if (sqlType == SQLType.WRITE) {
            Method method = statementMetaData.getMethod();
            Class<?>[] types = method.getParameterTypes();
            Class<?> returnType = statementMetaData.getReturnType();
            if (returnType.isPrimitive()) {
                returnType = ClassUtils.primitiveToWrapper(returnType);
            }
            if (types.length > 0 && List.class.isAssignableFrom(types[0])) {
                this.batchUpdate = true;
                if (metaData.getMethod().getAnnotation(ReturnGeneratedKeys.class) != null) {
                    // 批量处理的直接不支持@ReturnGeneratedKeys注解
                    throw new InvalidDataAccessApiUsageException(
                        "batch update method cannot return generated keys: " + method);
                }
                if (returnType != void.class && returnType != int[].class //
                    && returnType != Integer.class && returnType != Boolean.class) {
                    throw new InvalidDataAccessApiUsageException(
                        "error return type, only support type of {void,boolean,int,int[]}: "
                                                                 + method);
                }
            } else {
                this.batchUpdate = false;
                if (metaData.getMethod().getAnnotation(ReturnGeneratedKeys.class) != null) {
                    metaData.getReturnGeneratedKeys().checkMethodReturnType(metaData.getReturnType(),
                        metaData);
                } else if (returnType != void.class && returnType != Boolean.class
                           && returnType != Integer.class) {
                    throw new InvalidDataAccessApiUsageException(
                        "error return type, only support type of {void,boolean,int}:" + method);
                }
            }
        } else {
            this.batchUpdate = false;
        }
        this.logPrefix = "\n @method:" + this.metaData;
    }

    @Override
    public StatementMetaData getMetaData() {
        return metaData;
    }

    @Override
    public Object execute(Map<String, Object> parameters) {
        Object result;
        if (batchUpdate) {
            //
            Iterable<?> iterable = (Iterable<?>) parameters.get(":1");
            Iterator<?> iterator = (Iterator<?>) iterable.iterator();
            List<StatementRuntime> runtimes = new LinkedList<StatementRuntime>();
            int index = 0;
            while (iterator.hasNext()) {
                Object arg = iterator.next();
                HashMap<String, Object> clone = new HashMap<String, Object>(parameters);
                // 更新执行参数
                clone.put(":1", arg);
                if (metaData.getSQLParamAt(0) != null) {
                    clone.put(metaData.getSQLParamAt(0).value(), arg);
                }
                StatementRuntime runtime = new StatementRuntimeImpl(metaData, clone);
                for (Interpreter interpreter : interpreters) {
                    interpreter.interpret(runtime);
                }
                if (index == 0) {
                    log(parameters, runtime);
                }
                runtimes.add(runtime);
                index++;
            }
            result = querier.execute(sqlType, runtimes.toArray(new StatementRuntime[0]));
            result = afterInvocationCallback.execute(runtimes.get(0), result);
        } else {
            StatementRuntime runtime = new StatementRuntimeImpl(metaData, parameters);
            for (Interpreter interpreter : interpreters) {
                interpreter.interpret(runtime);
            }
            log(parameters, runtime);
            result = querier.execute(sqlType, runtime);
            result = afterInvocationCallback.execute(runtime, result);
        }
        return result;

    }

    private void log(Map<String, Object> parameters, StatementRuntime runtime) {
        if (sqlLogger.isInfoEnabled()) {
            String sql = runtime.getSQL();
            String argsAsString = Arrays.toString(runtime.getArgs());
            StringBuilder sb = new StringBuilder(1024);
            sb.append(logPrefix);
            sb.append("\n @sql:\t").append(sql);//
            sb.append("\n @args:\t");
            ArrayList<String> keys = new ArrayList<String>(parameters.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                sb.append(key).append("='").append(parameters.get(key)).append("'  ");
            }
            sb.append("\n sql:\t").append(runtime.getSQL());//
            sb.append("\n args:\t").append(argsAsString);
            sqlLogger.info(sb.toString());
        }
    }

}
