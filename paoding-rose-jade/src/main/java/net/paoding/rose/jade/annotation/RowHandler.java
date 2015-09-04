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
package net.paoding.rose.jade.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import net.paoding.rose.jade.rowmapper.RowMapperFactory;
import net.paoding.rose.jade.statement.StatementMetaData;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RowHandler {

    /**
     * 指定自己设置的 rowMapper 类；rowMapper类应该做到无状态。<p>
     * 
     * 如果实现的是 {@link StatementAware} ，Jade会调用它的init进行初始化，告知对应的DAO方法
     * @see StatementAware
     * 
     */
    @SuppressWarnings("rawtypes")
    Class<? extends RowMapper>rowMapper() default NotSettingRowMapper.class;

    /**
     * 通过自定义的 {@link RowMapperFactory} 指定自己设置的 rowMapper 类；返回的rowMapper类应该做到无状态。<p>
     * 
     * 如果实现的是 {@link StatementAware} ，Jade会调用它的init进行初始化，告知对应的DAO方法
     * 
     */
    Class<? extends RowMapperFactory>rowMapperFactory() default NotSettingRowMapperFactory.class;

    /**
     * 这是一个检查开关,默认为true；
     * <p>
     * true代表如果不是所有列都被映射给一个 Bean 的属性，抛出异常。
     * 
     */
    boolean checkColumns() default true;

    /**
     * 这是一个检查开关，默认为false; true代表如果不是每一个bean 属性都设置了SQL查询结果的值，抛出异常。
     * 
     */
    boolean checkProperties() default false;

    class NotSettingRowMapper implements RowMapper<Object> {

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            return null;
        }

    }

    class NotSettingRowMapperFactory implements RowMapperFactory {

        @Override
        public RowMapper<?> getRowMapper(StatementMetaData metaData) {
            return null;
        }

    }

}
