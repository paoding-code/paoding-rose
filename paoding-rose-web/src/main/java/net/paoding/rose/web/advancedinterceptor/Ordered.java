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
package net.paoding.rose.web.advancedinterceptor;

/**
 * 如果拦截器实现了此接口，代表这个拦截器关注其在所有拦截器中的顺序。
 * 
 * @author Administrator
 * 
 */
public interface Ordered {

    /**
     * 返回一个数字，值大的具有最高优先拦截权
     * 
     * @return
     */
    int getPriority();
}
