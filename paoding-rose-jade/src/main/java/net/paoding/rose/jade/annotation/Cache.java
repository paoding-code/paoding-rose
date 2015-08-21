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
package net.paoding.rose.jade.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用：{#link Cache} 标注需要缓存的 DAO 接口方法。默认的 expiry 为 0 表示没有过期限制。
 * 
 * @author han.liao
 */
@Target( { ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {

    /**
     * 标注 DAO 方法的缓存 Pool
     * 
     * @return 缓存 Pool
     */
    String pool() default "default";

    /**
     * 标注 DAO 方法的缓存 Key.
     * 
     * @return 缓存 Key
     */
    String key();

    /**
     * 标注 DAO 缓存的过期时间。
     * 
     * @return 缓存过期时间
     */
    int expiry() default 0;
    
    
    Class<?> cl() default Object.class;
}
