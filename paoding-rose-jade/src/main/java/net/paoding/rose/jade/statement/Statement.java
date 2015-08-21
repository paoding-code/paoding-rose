/*
 * Copyright 2009-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License i distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.paoding.rose.jade.statement;

import java.util.Map;

/**
 * {@link Statement} 表示一个符合规范的DAO方法，代表一个对数据库进行检索或更新的语句。
 * <p>
 * 
 * 这是一个内部接口
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public interface Statement {

    /**
     * 所属的DAO方法及其相关的信息
     * 
     * @return
     */
    public StatementMetaData getMetaData();

    /**
     * 按照给定的方法参数值，执行一次数据库检索或更新
     * <p>
     * 您可以通过parameters.get(":1")、parameters.get(":2")获得方法中的第1、第2个参数(从1开始)<br>
     * 如果DAO方法中的参数设置了<code>@SQLParam(name)</code>
     * 注解，您还可以从parameters.get(name)取得该参数。
     * 
     * @return
     */
    public Object execute(Map<String, Object> parameters);
}
