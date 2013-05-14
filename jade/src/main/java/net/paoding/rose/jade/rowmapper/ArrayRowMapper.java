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

import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * 将SQL结果集的一行映射为一个数组
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 */
public class ArrayRowMapper implements RowMapper {

    private Class<?> componentType;

    public ArrayRowMapper(Class<?> returnType) {
        this.componentType = returnType.getComponentType();
    }

    @Override
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        int columnSize = rs.getMetaData().getColumnCount();
        Object array = Array.newInstance(componentType, columnSize);
        for (int i = 0; i < columnSize; i++) {
            Object value = JdbcUtils.getResultSetValue(rs, (i + 1), componentType);
            Array.set(array, i, value);
        }
        return array;
    }
}
