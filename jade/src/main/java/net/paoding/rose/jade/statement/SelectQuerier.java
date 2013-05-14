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

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.paoding.rose.jade.annotation.SQLType;
import net.paoding.rose.jade.dataaccess.DataAccess;
import net.paoding.rose.jade.dataaccess.DataAccessFactory;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;

/**
 * 实现 SELECT 查询。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 */
public class SelectQuerier implements Querier {

    private final RowMapper rowMapper;

    private final Class<?> returnType;

    private final DataAccessFactory dataAccessProvider;

    public SelectQuerier(DataAccessFactory dataAccessProvider, StatementMetaData metaData,
            RowMapper rowMapper) {
        this.dataAccessProvider = dataAccessProvider;
        this.returnType = metaData.getMethod().getReturnType();
        this.rowMapper = rowMapper;
    }

    @Override
    public Object execute(SQLType sqlType, StatementRuntime... runtimes) {
        return execute(sqlType, (StatementRuntime) runtimes[0]);
    }

    public Object execute(SQLType sqlType, StatementRuntime runtime) {
        DataAccess dataAccess = dataAccessProvider.getDataAccess(//
                runtime.getMetaData(), runtime.getProperties());
        // 执行查询
        List<?> listResult = dataAccess.select(runtime.getSQL(), runtime.getArgs(), rowMapper);
        final int sizeResult = listResult.size();

        // 将 Result 转成方法的返回类型
        if (returnType.isAssignableFrom(List.class)) {

            // 返回  List 集合
            return listResult;

        } else if (returnType.isArray() && byte[].class != returnType) {
            Object array = Array.newInstance(returnType.getComponentType(), sizeResult);
            if (returnType.getComponentType().isPrimitive()) {
                int len = listResult.size();
                for (int i = 0; i < len; i++) {
                    Array.set(array, i, listResult.get(i));
                }
            } else {
                listResult.toArray((Object[]) array);
            }
            return array;

        } else if (Map.class.isAssignableFrom(returnType)) {
            // 将返回的  KeyValuePair 转换成  Map 对象
            // 因为entry.key可能为null，所以使用HashMap
            Map<Object, Object> map;
            if (returnType.isAssignableFrom(HashMap.class)) {

                map = new HashMap<Object, Object>(listResult.size() * 2);

            } else if (returnType.isAssignableFrom(Hashtable.class)) {

                map = new Hashtable<Object, Object>(listResult.size() * 2);

            } else {

                throw new Error(returnType.toString());
            }
            for (Object obj : listResult) {
                if (obj == null) {
                    continue;
                }

                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) obj;

                if (map.getClass() == Hashtable.class && entry.getKey() == null) {
                    continue;
                }

                map.put(entry.getKey(), entry.getValue());
            }

            return map;

        } else if (returnType.isAssignableFrom(HashSet.class)) {

            // 返回  Set 集合
            return new HashSet<Object>(listResult);

        } else {

            if (sizeResult == 1) {
                // 返回单个  Bean、Boolean等类型对象
                return listResult.get(0);

            } else if (sizeResult == 0) {

                // 基础类型的抛异常，其他的返回null
                if (returnType.isPrimitive()) {
                    String msg = "Incorrect result size: expected 1, actual " + sizeResult + ": "
                            + runtime.getMetaData();
                    throw new EmptyResultDataAccessException(msg, 1);
                } else {
                    return null;
                }

            } else {
                // IncorrectResultSizeDataAccessException
                String msg = "Incorrect result size: expected 0 or 1, actual " + sizeResult + ": "
                        + runtime.getMetaData();
                throw new IncorrectResultSizeDataAccessException(msg, 1, sizeResult);
            }
        }
    }
}
