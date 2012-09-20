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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

import net.paoding.rose.jade.annotation.SQL;
import net.paoding.rose.jade.annotation.SQLParam;
import net.paoding.rose.jade.annotation.SQLType;
import net.paoding.rose.jade.annotation.ShardBy;

/**
 * {@link StatementMetaData} 封装、缓存了一个DAO方法的相关信息
 * <p>
 * 
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class StatementMetaData {

    /**
     * 所属的DAO类的classMetaData
     */
    private final DAOMetaData daoMetaData;

    /**
     * 所在的DAO方法
     */
    private final Method method;

    /**
     * DAO方法上的原始SQL语句
     */
    private final String sql;

    /**
     * 方法返回参数的范型类型（不支持多级）－从method中获取并缓存
     */
    private final Class<?>[] genericReturnTypes;

    /**
     * {@link SQLParam} 注解数组－从method中获取并缓存
     * <p>
     * 此数组的长度为方法的参数个数，如果对应位置的方法参数没有注解 {@link SQLParam},该位置的元素值为null
     */
    private final SQLParam[] sqlParams;

    /**
     * <code>@{@link ShardBy}</code>标注在哪个参数上？(从0开始，负数代表无)－从method中获取并缓存
     */
    private final int shardByIndex;

    private final int parameterCount;

    // --------------------------------------------

    public StatementMetaData(DAOMetaData daoMetaData, Method method) {
        this.daoMetaData = daoMetaData;
        this.method = method;
        this.sql = method.getAnnotation(SQL.class).value();

        this.genericReturnTypes = GenericUtils.getActualClass(method.getGenericReturnType());

        Annotation[][] annotations = method.getParameterAnnotations();
        this.parameterCount = annotations.length;
        this.sqlParams = new SQLParam[annotations.length];
        int shardByIndex = -1;
        for (int index = 0; index < annotations.length; index++) {
            for (Annotation annotation : annotations[index]) {
                if (annotation instanceof ShardBy) {
                    if (shardByIndex >= 0) {
                        throw new IllegalArgumentException("duplicated @" + ShardBy.class.getName());
                    }
                    shardByIndex = index;
                } else if (annotation instanceof SQLParam) {
                    this.sqlParams[index] = (SQLParam) annotation;
                }
            }
        }
        this.shardByIndex = shardByIndex;
    }

    public DAOMetaData getDAOMetaData() {
        return daoMetaData;
    }

    public Method getMethod() {
        return method;
    }

    public String getSQL() {
        return sql;
    }

    public int getParameterCount() {
        return parameterCount;
    }

    public SQLParam getSQLParamAt(int argIndex) {
        return sqlParams[argIndex];
    }

    public int getShardByIndex() {
        return shardByIndex;
    }

    public Class<?>[] getGenericReturnTypes() {
        return genericReturnTypes;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return method.getAnnotation(annotationClass);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StatementMetaData) {
            StatementMetaData modifier = (StatementMetaData) obj;
            return daoMetaData.equals(modifier.daoMetaData) && method.equals(modifier.method);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return daoMetaData.hashCode() ^ method.hashCode();
    }

    @Override
    public String toString() {
        return daoMetaData.getDAOClass().getName() + '#' + method.getName();
    }

    private static Pattern[] SELECT_PATTERNS = new Pattern[] {
            //
            Pattern.compile("^\\s*SELECT\\s+", Pattern.CASE_INSENSITIVE), //
            Pattern.compile("^\\s*SHOW\\s+", Pattern.CASE_INSENSITIVE), //
            Pattern.compile("^\\s*DESC\\s+", Pattern.CASE_INSENSITIVE), //
            Pattern.compile("^\\s*DESCRIBE\\s+", Pattern.CASE_INSENSITIVE), //
    };

    private SQLType sqlType;

    public SQLType getSQLType() {
        if (sqlType == null) {
            SQL sql = method.getAnnotation(SQL.class);
            SQLType sqlType = sql.type();
            if (sqlType == SQLType.AUTO_DETECT) {
                for (int i = 0; i < SELECT_PATTERNS.length; i++) {
                    // 用正则表达式匹配  SELECT 语句
                    if (SELECT_PATTERNS[i].matcher(getSQL()).find()) {
                        sqlType = SQLType.READ;
                        break;
                    }
                }
                if (sqlType == SQLType.AUTO_DETECT) {
                    sqlType = SQLType.WRITE;
                }
            }
            this.sqlType = sqlType;
        }
        return sqlType;
    }
}
