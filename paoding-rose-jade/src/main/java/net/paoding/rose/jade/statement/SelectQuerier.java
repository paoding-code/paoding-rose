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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;

import net.paoding.rose.jade.annotation.SQLType;
import net.paoding.rose.jade.dataaccess.DataAccess;
import net.paoding.rose.jade.dataaccess.DataAccessFactory;

/**
 * 实现 SELECT 查询。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class SelectQuerier implements Querier {

    private final RowMapper rowMapper;

    private final Class<?> returnType;

    private final DataAccessFactory dataAccessFactory;
    
    private final ResultConverter converter;

    public SelectQuerier(DataAccessFactory dataAccessFactory, StatementMetaData metaData,
            RowMapper rowMapper) {
        this.dataAccessFactory = dataAccessFactory;
        this.returnType = metaData.getReturnType();
        this.rowMapper = rowMapper;
        this.converter = makeResultConveter();
    }

    @Override
    public Object execute(SQLType sqlType, StatementRuntime... runtimes) {
        return execute(sqlType, (StatementRuntime) runtimes[0]);
    }

    public Object execute(SQLType sqlType, StatementRuntime runtime) {
        DataAccess dataAccess = dataAccessFactory.getDataAccess(//
                runtime.getMetaData(), runtime.getAttributes());
        // 执行查询
        List<?> listResult = dataAccess.select(runtime.getSQL(), runtime.getArgs(), rowMapper);
        return converter.convert(runtime, listResult);
    }
    
    
    protected ResultConverter makeResultConveter() {
        ResultConverter converter;
        if (List.class == returnType || Collection.class == returnType || Iterable.class == returnType) {
            converter = new ResultConverter() {

                @Override
                public Object convert(StatementRuntime runtime, List<?> listResult) {
                    return listResult;
                }
                
            };
        }
        else if (ArrayList.class == returnType) {
            converter = new ResultConverter() {

                @Override
                public Object convert(StatementRuntime runtime, List<?> listResult) {
                    return new ArrayList(listResult);
                }
                
            };
        }
        else if (LinkedList.class == returnType) {
            converter = new ResultConverter() {

                @Override
                public Object convert(StatementRuntime runtime, List<?> listResult) {
                    return new LinkedList(listResult);
                }
                
            };
        }
        else if (Set.class == returnType || HashSet.class == returnType) {
            converter = new ResultConverter() {

                @Override
                public Object convert(StatementRuntime runtime, List<?> listResult) {
                    return new HashSet(listResult);
                }
                
            };
        }
        else if (Collection.class.isAssignableFrom(returnType)) {
            converter = new ResultConverter() {

                @Override
                public Object convert(StatementRuntime runtime, List<?> listResult) {
                    Collection listToReturn;
                    try {
                        listToReturn = (Collection) returnType.newInstance();
                    } catch (Exception e) {
                        throw new Error("error to create instance of " + returnType.getName());
                    }
                    listToReturn.addAll(listResult);
                    return listToReturn;     
                }
                
            };
        }
        else if (Iterator.class == returnType) {
            converter = new ResultConverter() {

                @Override
                public Object convert(StatementRuntime runtime, List<?> listResult) {
                    return listResult.iterator();
                }
                
            };
        }
        else if (returnType.isArray() && byte[].class != returnType) {
            if (returnType.getComponentType().isPrimitive()) {
                converter = new ResultConverter() {

                    @Override
                    public Object convert(StatementRuntime runtime, List<?> listResult) {
                        Object array = Array.newInstance(returnType.getComponentType(), listResult.size());
                        int len = listResult.size();
                        for (int i = 0; i < len; i++) {
                            Array.set(array, i, listResult.get(i));
                        }
                        return array;
                    }
                    
                };
            }
            else {
                converter = new ResultConverter() {

                    @Override
                    public Object convert(StatementRuntime runtime, List<?> listResult) {
                        Object array = Array.newInstance(returnType.getComponentType(), listResult.size());
                        return listResult.toArray((Object[]) array);
                    }
                    
                };
            }

        } 
        else if (Map.class == returnType || HashMap.class == returnType) {
            converter = new MapConverter() {
                @Override
                protected Map creatMap(StatementRuntime runtime) {
                    return new HashMap();
                }
            };
        } 
        else if (Hashtable.class == returnType) {
            converter = new MapConverter() {
                @Override
                protected Map creatMap(StatementRuntime runtime) {
                    return new Hashtable();
                }
            };
        }
        else if (Map.class.isAssignableFrom(returnType)) {
            converter = new MapConverter() {
                @Override
                protected Map creatMap(StatementRuntime runtime) {
                    try {
                        return (Map) returnType.newInstance();
                    } catch (Exception e) {
                        throw new Error("error to create instance of " + returnType.getName());
                    }
                }
            };
        }
        // 
        else {
            converter = new ResultConverter() {

                @Override
                public Object convert(StatementRuntime runtime, List<?> listResult) {
                    final int sizeResult = listResult.size();
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
            };
        }
        return converter;
    }

    /**
     * {@link ResultConverter} 负责将SELECT出来的List转化为DAO方法要求的返回结果
     * <p>
     * 
     */
    static interface ResultConverter {

        Object convert(StatementRuntime runtime, List<?> listResult);
    }
    
    static abstract class MapConverter implements ResultConverter {

        @Override
        public Object convert(StatementRuntime runtime, List<?> listResult) {
            Map map = creatMap(runtime);
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
        }

        protected abstract Map creatMap(StatementRuntime runtime) ;
        
    }
}

