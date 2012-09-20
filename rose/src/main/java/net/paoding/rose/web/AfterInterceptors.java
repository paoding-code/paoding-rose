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

/**
 * 如果控制器action方法的参数对象实现了 {@link AfterInterceptors}接口，Rose将在所在拦截器拦截之后，调用
 * {@link #doAfterInterceptors(Invocation, Object)}方法
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface AfterInterceptors {

    /**
     * 
     * @param inv
     * @param instruction
     * @return null表示保留原来所返回的指示
     * @throws Exception
     */
    public Object doAfterInterceptors(Invocation inv, Object instruction) throws Exception;

}
