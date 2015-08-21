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
package net.paoding.rose.jade.dataaccess;

import java.util.Map;

import net.paoding.rose.jade.statement.StatementMetaData;

/**
 * 这是框架的内部接口，{@link DataAccess}的工厂类。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 */
public interface DataAccessFactory {

    /**
     * 运行时为框架提供一个 {@link DataAccess} 实例
     */
    DataAccess getDataAccess(StatementMetaData metaData, Map<String, Object> runtime);
}
