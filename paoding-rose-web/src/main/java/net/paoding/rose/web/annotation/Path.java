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

/**
 * 标注 {@link Path}设置对控制器的自定义映射规则。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
@Target( { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Path {

    /**
     * 设定哪些路径应由所在控制器处理，可以设置多个。
     * <p>
     * 可以在设置中使用不限制个数的${xx}，并结合 {@link Param}标注使用 。 路径是否以'/'开头不做区别。
     * <p>
     * 
     * 特别的，如果不想让一个控制器生效，除了使用@Ignored外(推荐)，还可以使用@Path({}),即使用零长度数组来实现.
     * 
     * @return
     */
    String[] value();

}
