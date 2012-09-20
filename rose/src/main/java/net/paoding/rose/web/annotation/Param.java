/*
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.paoding.rose.web.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.paoding.rose.web.var.Model;

/**
 * 将{@link Param}标注在控制器方法的参数上，可以获得在{@link Path}
 * 中的占位符的参数，或者request的请求参数。
 * <p>
 * 如果{@link Param}标注的是bean，则表示这个bean放到model中应该使用配置的名称。
 * <p>
 * 可以使用{@link Param}标注Map, List, Set,
 * String/int/Integer[]等参数，并从配置的名称中获取request参数绑定进来
 * <p>
 * Map的规则是请求参数以名为"param_value:map_key"的形式出现，并和该参数的值作为map的一个映射。<br>
 * 数组的规则是请求参数以名为param_value的所有request参数。如果只有一个参数，则将该参数按照逗号做切割分成数组<br>
 * Map,List,Set均只支持String类型的；数组可以支持String类型以及一般的int/Integer等类型的。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
@Target( { ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Param {

    /**
     * 声明参数的名字，框架将把这个名字参数的值设置到所在的参数上；
     * <p>
     * 如果该参数是一个bean，则将该bean绑定到 {@link Model}中的这个名字中
     * 
     * @return
     */
    String value();

}
