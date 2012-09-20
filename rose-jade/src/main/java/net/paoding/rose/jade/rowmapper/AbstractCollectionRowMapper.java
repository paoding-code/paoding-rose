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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import net.paoding.rose.jade.statement.StatementMetaData;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * 将SQL结果集的一行映射为某种集合
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 */
public abstract class AbstractCollectionRowMapper implements RowMapper {

    private Class<?> elementType;

    public AbstractCollectionRowMapper(StatementMetaData modifier) {
        Class<?>[] genericTypes = modifier.getGenericReturnTypes();
        if (genericTypes.length < 1) {
            throw new IllegalArgumentException("Collection generic");
        }
        elementType = genericTypes[0];
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        int columnSize = rs.getMetaData().getColumnCount();
        Collection collection = createCollection(columnSize);
        // columnIndex从1开始
        for (int columnIndex = 1; columnIndex <= columnSize; columnIndex++) {
            collection.add(JdbcUtils.getResultSetValue(rs, columnIndex, elementType));
        }
        return collection;
    }

    /**
     * 由子类覆盖此方法，提供一个空的具体集合实现类
     * 
     * @param columnSize
     * @return
     */
    @SuppressWarnings("unchecked")
    protected abstract Collection createCollection(int columnSize);

}
