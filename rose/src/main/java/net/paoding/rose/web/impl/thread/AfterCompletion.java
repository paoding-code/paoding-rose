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
package net.paoding.rose.web.impl.thread;

import net.paoding.rose.web.Invocation;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface AfterCompletion {

    /**
     * 整个流程(包括页面render流程)结束时调用，不管是否发生过异常。如果发生了异常，则将传送一个非空的Throwable对象到该方法。
     * <p>
     * 只有之前调用before时返回true时才会调用到它的afterRender方法
     * 
     * @param inv
     * @param ex
     * @throws Exception
     */
    void afterCompletion(Invocation inv, Throwable ex) throws Exception;
}
