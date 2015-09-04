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
package net.paoding.rose.jade.rowmapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import net.paoding.rose.jade.annotation.RowHandler;
import net.paoding.rose.jade.annotation.StatementAware;
import net.paoding.rose.jade.statement.StatementMetaData;

/**
 * 支持DAO方法返回类型：
 * <p>
 * <ul>
 * <li>int、long等primitive：期望返回单列、1行</li>
 * <li>Integer、Long等包装类型的：期望返回单列、0行或1行</li>
 * <li>String、BigDecimal：期望返回单列，0行或1行</li>
 * <li>java.util.Date及其子类：期望返回单列，0行或1行</li>
 * <li>byte[]：期望返回单行、单列；列类型可转化为byte[]类型(比如blob类型)</li>
 * <li>Blob、Clob：期望返回单，0行或1行</li>
 * <li><code>数组(int[]、String[]等)：期望返回单列，多行；</li>
 * <li>数组(User[]等)：期望返回多列，多行；</li>
 * <li>集合(List&lt;Integer&gt;、Set&lt;String&gt等): 期望返回单列，多行；</li>
 * <li>集合(List&lt;User&gt;、Set&lt;User&gt等): 期望返回单列，多行；</li>
 * <li>映射(Map&lt;String, Date&gt): 期望返回2列，多行</li>
 * <li>映射(Map&lt;String, User&gt): 期望返回多列，多行</li>
 * <li>映射(Map&lt;String, String[]&gt): 期望返回多列，多行</li>
 * <ul>
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class DefaultRowMapperFactory implements RowMapperFactory {

    private static Log logger = LogFactory.getLog(RowMapperFactory.class);

    private Map<String, RowMapper> rowMappers = new HashMap<String, RowMapper>();

    @Override
    public RowMapper getRowMapper(StatementMetaData smd) {
        RowHandler rowHandler = smd.getAnnotation(RowHandler.class);

        // 根据类型创建  RowMapper
        RowMapper rowMapper = null;

        if (rowHandler != null) {
            if (rowHandler.rowMapper() != RowHandler.NotSettingRowMapper.class) {
                try {
                    rowMapper = rowHandler.rowMapper().newInstance();
                } catch (Exception ex) {
                    throw new BeanInstantiationException(rowHandler.rowMapper(), ex.getMessage(),
                        ex);
                }
            } else
                if (rowHandler.rowMapperFactory() != RowHandler.NotSettingRowMapperFactory.class) {
                try {
                    RowMapperFactory rowMapperFactory = rowHandler.rowMapperFactory().newInstance();
                    rowMapper = rowMapperFactory.getRowMapper(smd);
                } catch (Exception ex) {
                    throw new BeanInstantiationException(rowHandler.rowMapper(), ex.getMessage(),
                        ex);
                }
            }
        }
        // 
        if (rowMapper == null) {
            //

            Class<?> returnClassType = smd.getMethod().getReturnType();
            Class<?> rowType = getRowType(smd);

            // BUGFIX: SingleColumnRowMapper 处理  Primitive Type 抛异常
            if (rowType.isPrimitive()) {
                rowType = ClassUtils.primitiveToWrapper(rowType);
            }

            // 返回单列的查询的(或者返回只有2列的Map类型查询的)
            if (TypeUtils.isColumnType(rowType)) {
                if (Map.class.isAssignableFrom(returnClassType)) {
                    rowMapper = new MapEntryColumnRowMapper(smd, rowType);
                } else {
                    rowMapper = new SingleColumnRowMapper(rowType);
                }
            }
            // 返回多列的，用Bean对象、集合、映射、数组来表示每一行的
            else {
                if (rowType == Map.class) {
                    rowMapper = new ColumnMapRowMapper();
                } else if (rowType.isArray()) {
                    rowMapper = new ArrayRowMapper(rowType);
                } else if ((rowType == List.class) || (rowType == Collection.class)) {
                    rowMapper = new ListRowMapper(smd);
                } else if (rowType == Set.class) {
                    rowMapper = new SetRowMapper(smd);
                } else {
                    boolean checkColumns = (rowHandler == null) ? true : rowHandler.checkColumns();
                    boolean checkProperties = (rowHandler == null) ? false
                        : rowHandler.checkProperties();
                    String key = rowType.getName() + "[checkColumns=" + checkColumns
                                 + "&checkProperties=" + checkProperties + "]";
                    rowMapper = rowMappers.get(key);
                    if (rowMapper == null) {
                        rowMapper = new BeanPropertyRowMapper(rowType, checkColumns,
                            checkProperties); // jade's BeanPropertyRowMapper here
                        rowMappers.put(key, rowMapper);
                    }
                }
                // 如果DAO方法最终返回的是Map，rowMapper要返回Map.Entry对象
                if (Map.class.isAssignableFrom(returnClassType)) {
                    rowMapper = new MapEntryRowMapper(smd, rowMapper);
                }
            }
        }
        
        //

        if (rowMapper instanceof StatementAware) {
            ((StatementAware) rowMapper).setStatementMetaData(smd);
        }

        if (logger.isInfoEnabled()) {
            logger.info("using rowMapper " + rowMapper + " for " + smd);
        }

        return rowMapper;
    }

    // 获得返回的集合元素类型
    private static Class<?> getRowType(StatementMetaData statementMetaData) {
        Class<?> returnClassType = statementMetaData.getReturnType();
        if (Collection.class.isAssignableFrom(returnClassType)//
            || Iterable.class == returnClassType // 
            || Iterator.class == returnClassType) {
            return getRowTypeFromCollectionType(statementMetaData, returnClassType);
        } //
        else if (Map.class.isAssignableFrom(returnClassType)) {
            return getRowTypeFromMapType(statementMetaData, returnClassType);
        }
        //
        else if (returnClassType.isArray() && returnClassType != byte[].class) {
            // 数组类型, 支持多重数组
            return returnClassType.getComponentType();
        }

        // 此时代表整个DAO方法只关心结果集第一行
        return returnClassType;
    }

    private static Class<?> getRowTypeFromMapType(StatementMetaData modifier,
                                                  Class<?> returnClassType) {
        Class<?> rowType;
        // 获取  Map<K, V> 值元素类型
        Class<?>[] genericTypes = modifier.getGenericReturnTypes();
        if (genericTypes.length != 2) {
            throw new IllegalArgumentException(
                "the returned generic type '" + returnClassType.getName()
                                               + "' should has two actual type parameters.");
        }
        rowType = genericTypes[1]; // 取  V 类型
        return rowType;
    }

    private static Class<?> getRowTypeFromCollectionType(StatementMetaData modifier,
                                                         Class<?> returnClassType) {
        Class<?> rowType;
        // 仅支持  List/ArrayList/LinkedList, CollectionIterable/Iterator, Set/HashSet
        if ((returnClassType != List.class) && (returnClassType != ArrayList.class)
            && (returnClassType != LinkedList.class)//
            && (returnClassType != Collection.class) && (returnClassType != Iterable.class)
            && (returnClassType != Iterator.class) && (returnClassType != Set.class)
            && (returnClassType != HashSet.class)) {
            throw new IllegalArgumentException("error collection type " + returnClassType
                .getName() + "; only support List/ArrayList/LinkedList, CollectionIterable/Iterator, Set/HashSet");
        }
        // 获取集合元素类型
        Class<?>[] genericTypes = modifier.getGenericReturnTypes();
        if (genericTypes.length != 1) {
            throw new IllegalArgumentException(
                "the returned generic type '" + returnClassType.getName()
                                               + "' should has a actual type parameter.");
        }
        rowType = genericTypes[0];
        return rowType;
    }

}
