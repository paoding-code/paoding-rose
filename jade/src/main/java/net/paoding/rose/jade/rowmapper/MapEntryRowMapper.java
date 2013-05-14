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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * 在将SQL结果集的一行映射为某种对象后，再提取出一个列作为key，形成key-value映射对。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 * 
 */
public class MapEntryRowMapper implements RowMapper {

    private static Log logger = LogFactory.getLog(MapEntryRowMapper.class);

    private final RowMapper mapper;

    private String keyColumn;

    private int keyColumnIndex = 1;

    private Class<?> keyType;

    private StatementMetaData modifier;

    public MapEntryRowMapper(StatementMetaData modifier, RowMapper mapper) {
        this.modifier = modifier;
        Class<?>[] genericTypes = modifier.getGenericReturnTypes();
        if (genericTypes.length < 2) {
            throw new IllegalArgumentException("please set map generic parameters in method: "
                    + modifier.getMethod());
        }
        KeyColumnOfMap mapKey = modifier.getAnnotation(KeyColumnOfMap.class);
        this.keyColumn = (mapKey != null) ? mapKey.value() : null;
        this.keyType = genericTypes[0];
        this.mapper = mapper;
    }

    @Override
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        if (rowNum == 0) {
            if (StringUtils.isNotEmpty(keyColumn)) {
                keyColumnIndex = rs.findColumn(keyColumn);
                if (keyColumnIndex <= 0) {
                    throw new IllegalArgumentException(String.format(
                            "wrong key name %s for method: %s ", keyColumn, modifier.getMethod()));
                }
                keyColumn = null;
            }

            if (logger.isDebugEnabled()) {
                logger.debug(String.format("keyIndex=%s; for method: %s ", keyColumnIndex, modifier
                        .getMethod()));
            }
        }

        // 从  JDBC ResultSet 获取 Key
        Object key = JdbcUtils.getResultSetValue(rs, keyColumnIndex, keyType);
        if (key != null && !keyType.isInstance(key)) {
            ResultSetMetaData rsmd = rs.getMetaData();
            throw new TypeMismatchDataAccessException( // NL
                    "Type mismatch affecting row number " + rowNum + " and column type '"
                            + rsmd.getColumnTypeName(keyColumnIndex) + "' expected type is '"
                            + keyType + "'");
        }

        return new MapEntryImpl<Object, Object>(key, mapper.mapRow(rs, rowNum));
    }
}
