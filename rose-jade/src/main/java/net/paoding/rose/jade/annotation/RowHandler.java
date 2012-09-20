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

@Target( { ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RowHandler {

    /**
     * 指定自己设置的 rowMapper 类；rowMapper类应该做到无状态。
     * 
     * @return
     */
    Class<? extends RowMapper> rowMapper() default ByDefault.class;

    /**
     * 这是一个检查开关,默认为true；
     * <p>
     * true代表如果不是所有列都被映射给一个 Bean 的属性，抛出异常。
     * 
     * @return
     */
    boolean checkColumns() default true;

    /**
     * 这是一个检查开关，默认为false; true代表如果不是每一个bean 属性都设置了SQL查询结果的值，抛出异常。
     * 
     * @return
     */
    boolean checkProperties() default false;

    class ByDefault implements RowMapper {

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            return null;
        }

    }
}
