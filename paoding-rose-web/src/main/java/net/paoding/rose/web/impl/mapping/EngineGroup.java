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
package net.paoding.rose.web.impl.mapping;

import java.util.List;

import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.impl.thread.Engine;
import net.paoding.rose.web.impl.thread.LinkedEngine;

/**
 * {@link EngineGroup} 代表一个 {@link Engine} 集合，一个 {@link Mapping} 可能含一个
 * {@link EngineGroup}实例，也可能包含多个；
 * <p>
 * 原类名WebResource，2010年5月改为EngineGroup
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface EngineGroup {

    /**
     * 注册该资源对指定请求方法的处理逻辑，可以对某一个具体的请求方法注册多个处理逻辑。
     * <p>
     * 一个请求方法有多个处理逻辑时，最终只有逻辑是真正执行的。不同的请求，根据其URI、参数等不同，真正执行的逻辑可能不一样。
     * 
     * @param method 可以使用 {@link ReqMethod#ALL}
     * @param engine
     */
    public void addEngine(ReqMethod method, LinkedEngine engine);

    /**
     * 
     * @return
     */
    public int size();

    /**
     * 返回某种请求方法的处理逻辑
     * 
     * @param method
     * @return 如果没有注册处理逻辑，返回一个长度为0的数组
     */
    public LinkedEngine[] getEngines(ReqMethod method);

    /**
     * 是否支持此请求方法?
     * 
     * @param method
     * @return
     */
    public boolean isMethodAllowed(ReqMethod method);

    /**
     * 返回本对象支持的请求方法
     * 
     * @return
     */
    public List<ReqMethod> getAllowedMethods();

    /**
     * 销毁旗下的engines
     */
    public void destroy();

    /**
     * 
     * @return
     */
    public String toString();
}
