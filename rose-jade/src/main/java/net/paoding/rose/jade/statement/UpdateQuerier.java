/*
 * Copyright 2009-2010 the original author or authors.
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
import java.util.Map;

import net.paoding.rose.jade.annotation.ReturnGeneratedKeys;
import net.paoding.rose.jade.annotation.SQLType;
import net.paoding.rose.jade.dataaccess.DataAccess;
import net.paoding.rose.jade.dataaccess.DataAccessFactory;

import org.apache.commons.lang.ClassUtils;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 */
public class UpdateQuerier implements Querier {

    private final DataAccessFactory dataAccessProvider;

    private final Class<?> returnType;

    private final boolean returnGeneratedKeys;

    public UpdateQuerier(DataAccessFactory dataAccessProvider, StatementMetaData metaData) {
        this.dataAccessProvider = dataAccessProvider;
        Method method = metaData.getMethod();
        // 转换基本类型
        Class<?> returnType = method.getReturnType();
        if (returnType.isPrimitive()) {
            returnType = ClassUtils.primitiveToWrapper(returnType);
        }
        this.returnType = returnType;
        if (returnType != void.class && (method
                        .isAnnotationPresent(ReturnGeneratedKeys.class))) {
            returnGeneratedKeys = true;
        } else {
            returnGeneratedKeys = false;
        }
    }

    @Override
    public Object execute(SQLType sqlType, StatementRuntime... runtimes) {
        return runtimes.length > 1 ? executeBatch(runtimes)
                : executeSingle(runtimes[0], returnType);
    }

    private Object executeSingle(StatementRuntime runtime, Class<?> returnType) {
        Number result;
        DataAccess dataAccess = dataAccessProvider.getDataAccess(//
                runtime.getMetaData(), runtime.getProperties());
        if (returnGeneratedKeys) {
            ArrayList<Map<String, Object>> keys = new ArrayList<Map<String, Object>>(1);
            KeyHolder generatedKeyHolder = new GeneratedKeyHolder(keys);
            dataAccess.update(runtime.getSQL(), runtime.getArgs(), generatedKeyHolder);
            if (keys.size() > 0) {
                result = generatedKeyHolder.getKey();
            } else {
                result = null;
            }
        } else {
            result = new Integer(dataAccess.update(runtime.getSQL(), runtime.getArgs(), null));
        }
        //
        if (result == null || returnType == void.class) {
            return null;
        }
        if (returnType == result.getClass()) {
            return result;
        }

        // 将结果转成方法的返回类型
        if (returnType == Integer.class) {
            return result.intValue();
        } else if (returnType == Long.class) {
            return result.longValue();
        } else if (returnType == Boolean.class) {
            return result.intValue() > 0 ? Boolean.TRUE : Boolean.FALSE;
        } else if (returnType == Double.class) {
            return result.doubleValue();
        } else if (returnType == Float.class) {
            return result.floatValue();
        } else if (Number.class.isAssignableFrom(returnType)) {
            return result;
        } else {
            throw new DataRetrievalFailureException(
                    "The generated key is not of a supported numeric type. " + "Unable to cast ["
                            + result.getClass().getName() + "] to [" + Number.class.getName() + "]");
        }
    }

    private Object executeBatch(StatementRuntime... runtimes) {
        int[] updatedArray = new int[runtimes.length];
        for (int i = 0; i < updatedArray.length; i++) {
            StatementRuntime runtime = runtimes[i];
            updatedArray[i] = (Integer) executeSingle(runtime, Integer.class);
        }
        return updatedArray;

        //        Map<String, Object> parameters = runtime.getParameters();
        //        List<?> list = (List<?>) parameters.get(":1");
        //
        //        int[] updatedArray;
        //
        //        if (true) {
        //            List<Map<String, Object>> parametersList = new ArrayList<Map<String, Object>>(
        //                    list.size());
        //            for (Object arg : list) {
        //
        //                HashMap<String, Object> clone = new HashMap<String, Object>(parameters);
        //
        //                // 更新执行参数
        //                clone.put(":1", arg);
        //                if (runtime.getMetaData().getSQLParamAt(0) != null) {
        //                    clone.put(runtime.getMetaData().getSQLParamAt(0).value(), arg);
        //                }
        //                parametersList.add(clone);
        //            }
        //            updatedArray = dataAccess.batchUpdate(runtime.getSQL(), parametersList);
        //        } else {
        //            // 批量执行查询
        //            int index = 0;
        //            updatedArray = new int[list.size()];
        //            for (Object arg : list) {
        //
        //                HashMap<String, Object> clone = new HashMap<String, Object>(parameters);
        //
        //                // 更新执行参数
        //                clone.put(":1", arg);
        //                if (this.metaData.getSQLParamAt(0) != null) {
        //                    clone.put(this.metaData.getSQLParamAt(0).value(), arg);
        //                }
        //                updatedArray[index] = (Integer) executeSignle(dataAccess, clone, int.class);
        //
        //                index++;
        //            }
        //        }
        //        Class<?> batchReturnClazz = metaData.getMethod().getReturnType();
        //        if (batchReturnClazz == int[].class) {
        //            return updatedArray;
        //        }
        //        if (batchReturnClazz == Integer[].class) {
        //            Integer[] ret = new Integer[updatedArray.length];
        //            for (int i = 0; i < ret.length; i++) {
        //                ret[i] = updatedArray[i];
        //            }
        //            return updatedArray;
        //        }
        //        if (batchReturnClazz == void.class) {
        //            return null;
        //        }
        //        if (batchReturnClazz == int.class || batchReturnClazz == Integer.class) {
        //            int updated = 0;
        //            for (int i = 0; i < updatedArray.length; i++) {
        //                updated += updatedArray[i];
        //            }
        //            return updated;
        //        }
        //
        //        return null;
    }

}
