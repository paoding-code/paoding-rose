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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import net.paoding.rose.jade.annotation.KeyColumnOfMap;
import net.paoding.rose.jade.statement.StatementMetaData;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.TypeMismatchDataAccessException;
import org.springframework.jdbc.IncorrectResultSetColumnCountException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * 在将SQL结果集某一行的两列，一列作为key，一列作为value，形成一个key-value映射对。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 * 
 */
public class MapEntryColumnRowMapper implements RowMapper {

    private static Log logger = LogFactory.getLog(MapEntryColumnRowMapper.class);

    private String keyColumn;

    private int keyColumnIndex = 1;

    private int valueColumnIndex = 2;

    private Class<?> keyType, valueType;

    private StatementMetaData modifier;

    public MapEntryColumnRowMapper(StatementMetaData modifier, Class<?> requiredType) {
        this.modifier = modifier;
        Class<?>[] genericTypes = modifier.getGenericReturnTypes();
        if (genericTypes.length < 2) {
            throw new IllegalArgumentException("please set map generic parameters in method: "
                    + modifier.getMethod());
        }

        // 获取 Key 类型与列
        KeyColumnOfMap mapKey = modifier.getAnnotation(KeyColumnOfMap.class);
        // 设置 Key 类型与列
        this.keyColumn = (mapKey != null) ? mapKey.value() : null;
        this.keyType = genericTypes[0];
        this.valueType = genericTypes[1];
    }

    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {

        // 验证列的数目
        if (rowNum == 0) {
            ResultSetMetaData rsmd = rs.getMetaData();
            int nrOfColumns = rsmd.getColumnCount();
            if (nrOfColumns != 2) {
                throw new IncorrectResultSetColumnCountException(2, nrOfColumns);
            }

            if (StringUtils.isNotEmpty(keyColumn)) {
                keyColumnIndex = rs.findColumn(keyColumn);
                if (keyColumnIndex == 1) {
                    valueColumnIndex = 2;
                } else if (keyColumnIndex == 2) {
                    valueColumnIndex = 1;
                } else {
                    throw new IllegalArgumentException(String.format(
                            "wrong key name %s for method: %s ", keyColumn, modifier.getMethod()));
                }
                keyColumn = null;
            }
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("keyIndex=%s; valueIndex=%s; for method: %s ",
                        keyColumnIndex, valueColumnIndex, modifier.getMethod()));
            }
        }

        // 从  JDBC ResultSet 获取  Key
        Object key = JdbcUtils.getResultSetValue(rs, keyColumnIndex, keyType);
        if (key != null && !keyType.isInstance(key)) {
            ResultSetMetaData rsmd = rs.getMetaData();
            throw new TypeMismatchDataAccessException( // NL
                    "Type mismatch affecting row number " + rowNum + " and column type '"
                            + rsmd.getColumnTypeName(keyColumnIndex) + "' expected type is '"
                            + keyType + "'");
        }

        // 从  JDBC ResultSet 获取  Value
        Object value = JdbcUtils.getResultSetValue(rs, valueColumnIndex, valueType);
        if (value != null && !valueType.isInstance(value)) {
            ResultSetMetaData rsmd = rs.getMetaData();
            throw new TypeMismatchDataAccessException( // NL
                    "Type mismatch affecting row number " + rowNum + " and column type '"
                            + rsmd.getColumnTypeName(valueColumnIndex) + "' expected type is '"
                            + valueType + "'");
        }

        // key有可能为null，不过我们还是做进去
        return new MapEntryImpl<Object, Object>(key, value);
    }
}
