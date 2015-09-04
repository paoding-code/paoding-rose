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
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class StatementRuntimeImpl implements StatementRuntime {

    private final StatementMetaData metaData;

    private final Map<String, Object> parameters;

    private String sql;

    private Object[] args;

    private Map<String, Object> attributes;

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
    public Map<String, Object> getAttributes() {
        if (attributes == null) {
            return Collections.emptyMap();
        }
        return this.attributes;
    }

    @Override
    public void setAttribute(String name, Object value) {
        if (attributes == null) {
            attributes = new HashMap<String, Object>();
        }
        this.attributes.put(name, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttribute(String name) {
        return (T) (attributes == null ? null : attributes.get(name));
    }

    // 不要删除，以便兼容1.x
    @Override
    public Map<String, Object> getProperties() {
        return getAttributes();
    }

    // 不要删除，以便兼容1.x
    @Override
    public void setProperty(String name, Object value) {
        setAttribute(name, value);
    }

    // 不要删除，以便兼容1.x
    @Override
    public <T> T getProperty(String name) {
        return getAttribute(name);
    }

}
