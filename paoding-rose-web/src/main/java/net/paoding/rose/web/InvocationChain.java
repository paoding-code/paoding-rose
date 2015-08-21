/*
 * Copyright 2007-2010 the original author or authors.
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
 * 开发者用于实现拦截器时，将控制流程交给下一个拦截器或最终的控制器方法
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public interface InvocationChain {

    /**
     * 将控制流程交给下一个拦截器，并获取下一个拦截器或最终的控制器方法返回的指示
     * 
     * @return
     * @throws Exception
     */
    Object doNext() throws Exception;
}
