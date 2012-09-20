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
package net.paoding.rose.web;

import net.paoding.rose.web.advancedinterceptor.Ordered;
import net.paoding.rose.web.annotation.Intercepted;

/**
 * 实现{@link ControllerInterceptor}用于拦截整个MVC流程。(通常则是实现
 * {@link ControllerInterceptorAdapter})。
 * <p>
 * 如果你要实现的拦截器是模块特有的，可以直接在控制器所在package中实现它，并以Interceptor作为命名的结尾，
 * Rose会自动把它load到module中，使得控制器能够被该拦截器拦截。<br>
 * 同时因为某种原因，想暂时禁止掉这个module中的某个拦截器又不想删除它或者去除implements
 * ControllerInterceptor, 此时把类标注成@Ignored即可
 * <p>
 * 如果拦截器的实现是公有的(特别是已经打包成jar的拦截器)或者其他package的拦截器，则需要先把它配置在/WEB-INF/
 * *.xml或者某个jar包下applicationContext*.xml中，这样则能够拦截到所有模块的Controller。<br>
 * 如果不想让拦截器拦截到某些控制器，配置控制器@Intercepted的allow和deny属性， 或者通过使拦截器实现{
 * {@link #getAnnotationClasses()}
 * 明确要求只有标注了指定的该annotation的控制器或方法才可以被该拦截器拦截到
 * <p>
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 * @see Intercepted
 * @see Ordered
 * @see ControllerInterceptorAdapter
 */
public interface ControllerInterceptor {

    /**
     * 
     * @param inv
     * @param chain
     * @return
     * @throws Exception
     */
    Object roundInvocation(Invocation inv, InvocationChain chain) throws Exception;

}
