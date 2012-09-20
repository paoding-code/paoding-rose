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
package net.paoding.rose.web.impl.mapping;

/**
 * 控制器的action path参数映射结果,从这个结果中可以知道一个地址映射给哪个资源、资源的参数值是什么
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface MatchResult {

    /**
     * 
     * @return
     */
    public MappingNode getMappingNode();

    /**
     * 返回匹配结果字符串
     * 
     * @return
     */
    public String getValue();

    /**
     * 返回从结果字符串中得到的资源参数名(如果该资源使用了使用了参数化的映射地址)
     * 
     * @param name
     * @return
     */
    public String getParameterName();

}
