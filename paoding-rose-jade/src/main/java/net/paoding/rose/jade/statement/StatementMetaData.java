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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import net.paoding.rose.jade.annotation.ReturnGeneratedKeys;
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
@SuppressWarnings({ "rawtypes" })
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
     * DAO方法上的原始SQL语句，如果没有执行SQL语句，则根据方法签名生成相应的串辅助debug
     */
    private final String sql;

    /**
     * SQL类型（查询类型或者更新类型）：默认由方法名和SQL语句判断，除非强制指定。
     * @see SQLType
     */
    private final SQLType sqlType;

    /**
     * DAO方法上的ReturnGeneratedKeys注解
     */
    private DynamicReturnGeneratedKeys returnGeneratedKeys;

    /**
     * 
     */
    private AfterInvocationCallback afterInvocationCallback;

    /**
     * DAO方法的“最低返回类型”。
     * <P>
     * 大部分情况returnType和method.getReturnType是相同的，但对于一些声明为泛型的返回类型，
     * Jade会尽量提取出实际的类型作为returnType
     * <P>
     * 比如：
     * 
     * <pre>
     * //@DAO、@SQL注解从略
     * public interface BaseDAO&lt;E&gt; {
     * 
     *     public E getById(Long id);
     * 
     * }
     * public interface UserDAO extends BaseDAO[User] {
     * 
     * }
     * </pre>
     * 
     * 此时，UserDAO#getById方法的returnType是User，而非Object;
     */
    private final Class returnType;

    /**
     * 方法返回参数的范型类型（不支持多级）－从method中获取并缓存
     * 
     * <pre>
     * 示例：
     * （1） List<E>中的E
     * （2） Map<K, V>中的K、V
     */
    private final Class[] parameterTypesOfReturnType;

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

    private final ShardBy shardBy;

    private final int parameterCount;

    /**
     * 框架或插件设置的属性
     */
    private Map<String, Object> attributes;

    private static final DynamicReturnGeneratedKeys nullDynamicReturnGeneratedKeys = new DynamicReturnGeneratedKeys() {

        @Override
        public boolean shouldReturnGerneratedKeys(StatementRuntime runtime) {
            return false;
        }
    };

    // --------------------------------------------

    public StatementMetaData(DAOMetaData daoMetaData, Method method) {
        this.daoMetaData = daoMetaData;
        this.method = method;
        SQL sqlAnnotation = method.getAnnotation(SQL.class);
        if (sqlAnnotation == null) {
            sqlAnnotation = new SQL() {

                @Override
                public Class<? extends Annotation> annotationType() {
                    return SQL.class;
                }

                @Override
                public String value() {
                    String toString = StatementMetaData.this.method.toString();
                    int paramStart = toString.indexOf("(");
                    int methodNameStart = toString.lastIndexOf('.', paramStart) + 1;
                    return toString.substring(methodNameStart) + "@" //
                           + StatementMetaData.this.method.getDeclaringClass().getName();
                }

                @Override
                public SQLType type() {
                    return SQLType.AUTO_DETECT;
                }
            };
        }
        this.sql = sqlAnnotation.value();
        this.sqlType = resolveSQLType(sqlAnnotation);
        ReturnGeneratedKeys generatedKeysAnnotation = method
            .getAnnotation(ReturnGeneratedKeys.class);
        if (generatedKeysAnnotation != null) {
            try {
                this.returnGeneratedKeys = generatedKeysAnnotation.value().newInstance();
            } catch (InstantiationException e) {
                throw new IllegalArgumentException(e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            this.returnGeneratedKeys = nullDynamicReturnGeneratedKeys;
        }

        this.returnType = GenericUtils.resolveTypeVariable(daoMetaData.getDAOClass(),
            method.getGenericReturnType());
        this.parameterTypesOfReturnType = GenericUtils
            .resolveTypeParameters(daoMetaData.getDAOClass(), method.getGenericReturnType());

        Annotation[][] annotations = method.getParameterAnnotations();
        this.parameterCount = annotations.length;
        this.sqlParams = new SQLParam[annotations.length];
        int shardByIndex = -1;
        ShardBy shardBy = null;
        for (int index = 0; index < annotations.length; index++) {
            for (Annotation annotation : annotations[index])

            {
                if (annotation instanceof ShardBy) {
                    if (shardByIndex >= 0) {
                        throw new IllegalArgumentException(
                            "duplicated @" + ShardBy.class.getName());
                    }
                    shardByIndex = index;
                    shardBy = (ShardBy) annotation;
                } else if (annotation instanceof SQLParam) {
                    this.sqlParams[index] = (SQLParam) annotation;
                }
            }

        }
        this.shardByIndex = shardByIndex;
        this.shardBy = shardBy;
    }

    public DAOMetaData getDAOMetaData() {
        return daoMetaData;
    }

    public Method getMethod() {
        return method;
    }

    public DynamicReturnGeneratedKeys getReturnGeneratedKeys() {
        return returnGeneratedKeys;
    }

    public void setReturnGeneratedKeys(DynamicReturnGeneratedKeys returnGeneratedKeys) {
        this.returnGeneratedKeys = returnGeneratedKeys;
    }

    public AfterInvocationCallback getAfterInvocationCallback() {
        return afterInvocationCallback;
    }

    public void setAfterInvocationCallback(AfterInvocationCallback afterInvocationCallback) {
        this.afterInvocationCallback = afterInvocationCallback;
    }

    public Class<?> getReturnType() {
        return returnType;
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

    public ShardBy getShardBy() {
        return shardBy;
    }

    public Class<?>[] getGenericReturnTypes() {
        return parameterTypesOfReturnType;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return method.getAnnotation(annotationClass);
    }

    public SQLType getSQLType() {
        return sqlType;
    }

    /**
     * 设置挂在DAO方法上的属性
     * 
     * @param name
     * @param value
     */
    public void setAttribute(String name, Object value) {
        if (attributes == null) {
            synchronized (this) {
                if (attributes == null) {
                    attributes = new ConcurrentHashMap<String, Object>(4);
                }
            }
        }
        this.attributes.put(name, value);
    }

    /**
     * 
     * @param name
     * @return 获取由 {@link #setAttribute(String, Object)} 的属性
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T) (attributes == null ? null : attributes.get(name));
    }

    protected SQLType resolveSQLType(SQL sql) {
        SQLType sqlType = sql.type();
        if (sqlType == SQLType.AUTO_DETECT) {
            for (int i = 0; i < SELECT_PATTERNS.length; i++) {
                // 用正则表达式匹配 SELECT 语句
                if (SELECT_PATTERNS[i].matcher(getSQL()).find() //
                    || SELECT_PATTERNS[i].matcher(getMethod().getName()).find()) {
                    sqlType = SQLType.READ;
                    break;
                }
            }
            if (sqlType == SQLType.AUTO_DETECT) {
                sqlType = SQLType.WRITE;
            }
        }
        return sqlType;
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
                                                               Pattern.compile("^\\s*SELECT.*",
                                                                   Pattern.CASE_INSENSITIVE), //
                                                               Pattern.compile("^\\s*GET.*",
                                                                   Pattern.CASE_INSENSITIVE), //
                                                               Pattern.compile("^\\s*FIND.*",
                                                                   Pattern.CASE_INSENSITIVE), //
                                                               Pattern.compile("^\\s*READ.*",
                                                                   Pattern.CASE_INSENSITIVE), //
                                                               Pattern.compile("^\\s*QUERY.*",
                                                                   Pattern.CASE_INSENSITIVE), //
                                                               Pattern.compile("^\\s*COUNT.*",
                                                                   Pattern.CASE_INSENSITIVE), //
                                                               Pattern.compile("^\\s*SHOW.*",
                                                                   Pattern.CASE_INSENSITIVE), //
                                                               Pattern.compile("^\\s*DESC.*",
                                                                   Pattern.CASE_INSENSITIVE), //
                                                               Pattern.compile("^\\s*DESCRIBE.*",
                                                                   Pattern.CASE_INSENSITIVE), //
    };

}
