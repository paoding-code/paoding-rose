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

import javax.servlet.http.HttpSession;

import net.paoding.rose.web.paramresolver.ResolverFactoryImpl.HttpSessionResolver;

/**
 * 将Create标注在控制器方法参数上，表示该参数必须或不必须创建，比如对 {@link HttpSession}参数，可通过配置
 * {@link Create} 为false，表示如果原先没有 HttpSession 时，保留为null，不用创建。
 * 
 * @see HttpSessionResolver
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
@Target( { ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Create {

    /**
     * 
     * @return
     */
    boolean value() default true;

}
