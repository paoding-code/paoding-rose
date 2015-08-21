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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author qieqie
 * 
 */
public class StatementRuntimeImpl implements StatementRuntime {

    private final StatementMetaData metaData;

    private final Map<String, Object> parameters;

    private String sql;

    private Object[] args;

    private Map<String, Object> properties;

    public StatementRuntimeImpl(StatementMetaData metaData, Map<String, Object> parameters) {
        this.metaData = metaData;
        this.parameters = parameters;
        this.sql = metaData.getSQL();
    }

    @Override
    public StatementMetaData getMetaData() {
        return metaData;
    }

    @Override
    public Map<String, Object> getParameters() {
        return this.parameters;
    }

    @Override
    public void setSQL(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSQL() {
        return sql;
    }

    @Override
    public Object[] getArgs() {
        return args;
    }

    @Override
    public void setArgs(Object[] args) {
        this.args = args;
    }

    @Override
    public Map<String, Object> getProperties() {
        if (properties == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(this.properties);
    }

    @Override
    public void setProperty(String name, Object value) {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        this.properties.put(name, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProperty(String name) {
        return (T) (properties == null ? null : properties.get(name));
    }

}
