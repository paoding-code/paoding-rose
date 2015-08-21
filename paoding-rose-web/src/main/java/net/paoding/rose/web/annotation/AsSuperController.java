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
 * 标注成为{@link AsSuperController}的类，他的方法可以作为直接继承类(子类)控制器的action被使用。
 * <p>
 * 如果一个类A标注了{@link AsSuperController}，但是他的一个子类B没有标注
 * {@link AsSuperController} ，如果这个子类的子类C是一个控制器，刚才标注了
 * {@link AsSuperController}的类A的方法仍旧不能作为action方法暴露出来。 <br>
 * 但是如果类B标注了{@link AsSuperController}，则类A和B的方法将暴露给类C作为action方法。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
@Target( { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AsSuperController {

}
