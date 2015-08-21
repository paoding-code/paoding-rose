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
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

import net.paoding.rose.web.ControllerInterceptor;

/**
 * {@link Intercepted}用于配合{@link ControllerInterceptor}
 * 使用，指示Rose是否对所要分派的控制器(controller)及其方法(action)进行某些拦截器。
 * <p>
 * {@link Intercepted}已经标注为@Inherited，即：如果子类@Intercepted的话，将使用父类的，以此类推。
 * 
 * 控制器默认不用标注{@link Intercepted}都将被拦截。 使用{@link Intercepted}
 * 主要是用来改变默认的行为,使只有某些拦截器才能拦截该控制器<br>
 * 开发者可以使用2种设置方式，一种是通过配置allow，表示只有在allow内的拦截器才能拦截到；另一种是通过配置deny，
 * 表示除了deny中的拦截器都要拦截到
 * (<=allow范围内的)。如果没有配置allow以及deny表示所有拦截器都能拦截到。如果allow和deny同时配置
 * ，则只有同时满足deny和allow才能拦截到，
 * <p>
 * allow和deny里面的字符串表示的是相关拦截器在applicationContext中的id或name。
 * <p>
 * 对拦截器本身是和controller在相同的package中的一些规定： <br>
 * Rose会自动把它们放到module中来，它们的id则为它们的类名(首字母改小写)或者去掉最后"Interceptor"后留下的名字(
 * 同样首字母也要变为小写)。 当然，如果这些{@link ControllerInterceptor}配置了{@link Component}，
 * 则按照Comonent的规定定义它们的id
 * 
 * 开发者可以将{@link Intercepted} 配置在Controller级别上，也可以配置在某个具体的方法级别上。
 * Rose优先找方法声明的Intercepted标注，然后再是类的标注。
 * <p>
 * 
 * <strong>扩展</strong>
 * <p>
 * 可以在每个module目录下的rose.properties写intercepted.allow和intercepted.
 * deny属性配置该module的拦截器可见范围，使得该module下的控制器只能在这些可见的拦截下进行更进一步的选择。<br>
 * rose.properties的这个特性不作用子module
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
@Inherited
//!!
@Target( { ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Intercepted {

    /**
     * 明确表示只允许所列的拦截器才能拦截所在的控制器或方法
     * <p>
     * 如果没有配置allow表示允许所有拦截器拦截(在deny的允许下)
     * 
     * @return
     */
    String[] allow() default { "*" };

    /**
     * 明确表示所列的拦截器不要拦截所在的控制器或方法
     * 
     * @return
     */
    String[] deny() default {};
}
