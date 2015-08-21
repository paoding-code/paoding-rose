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
 * 
 * @author qieqie
 * 
 */
public interface StatementRuntime {

    StatementMetaData getMetaData();

    String getSQL();

    // 一个Statement完全可以被解出多个runtime，他们的sql不同，同时一个对应的args可能有几个（即批量更新）
    Object[] getArgs();

    Map<String, Object> getParameters();

    void setSQL(String sql);

    void setArgs(Object[] args);

    Map<String, Object> getProperties();

    void setProperty(String name, Object value);

    <T> T getProperty(String name);
}
