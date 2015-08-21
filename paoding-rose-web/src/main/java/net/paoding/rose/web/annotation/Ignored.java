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

import net.paoding.rose.web.ControllerErrorHandler;
import net.paoding.rose.web.ControllerInterceptor;

/**
 * 将{@link Ignored}标注在控制器或者它方法、拦截器( {@link ControllerInterceptor}
 * )、控制器异常处理类( {@link ControllerErrorHandler}、上，表示这个类不要被Rose识别。
 * <p>
 * 对拦截器而言就是不加入对控制器的拦截；对控制器而言就是不暴露该控制器或某个具体的方法；对异常处理器而言就是"就像没有它一样"。
 * <p>
 * 但是如果有其他类继承被标注为Ignored的类时，这个被Ignored的类的annotation仍然可能影响子类，具体请参考：
 * {@link Class#getAnnotation(Class)}的实现
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
@Target( { ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Ignored {

}
