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
package net.paoding.rose.web.var;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.paoding.rose.util.PlaceHolderUtils;
import net.paoding.rose.web.Invocation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Conventions;
import org.springframework.util.Assert;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class ModelImpl implements Model {

    private static Log logger = LogFactory.getLog(ModelImpl.class);

    private Map<String, Object> map = new HashMap<String, Object>();

    private Invocation invocation;

    final Object mutex; // Object on which to synchronize

    public ModelImpl(Invocation inv) {
        this.invocation = inv;
        this.mutex = map;
    }

    public Map<String, Object> getAttributes() {
        final Map<String, Object> cloneAndFiltered = new HashMap<String, Object>(map.size() * 2);
        synchronized (mutex) {
            final Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                final Map.Entry<String, Object> entry = iterator.next();
                final String key = entry.getKey();
                if (key != null && !key.startsWith("$$paoding-rose")) {
                    cloneAndFiltered.put(key, entry.getValue());
                }
            }
        }
        return Collections.unmodifiableMap(cloneAndFiltered);
    }

    @Override
    public Model add(String name, Object value) {
        Assert.notNull(name, "Model attribute name must not be null");
        if (value instanceof String) {
            value = PlaceHolderUtils.resolve((String) value, invocation);
        }
        synchronized (mutex) {
            map.put(name, value);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("add attribute to model: " + name + "=" + value);
        }
        return this;
    }

    @Override
    public Model add(Object value) {
        if (value != null) {
            if (value instanceof Collection) {
                Collection<?> collection = (Collection<?>) value;
                if (collection.size() == 0) {
                    return this;
                }
            }
            return add(Conventions.getVariableName(value), value);
        }
        return this;
    }

    @Override
    public boolean contains(String name) {
        synchronized (mutex) {
            return map.containsKey(name);
        }
    }

    @Override
    public Object get(String name) {
        synchronized (mutex) {
            return map.get(name);
        }
    }

    @Override
    public Model merge(Map<String, Object> attributes) {
        if (attributes != null) {
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                final String key = entry.getKey();
                if (!contains(key)) {
                    add(key, entry.getValue());
                }
            }
        }
        return this;
    }

    @Override
    public Model remove(String name) {
        if (name == null) {
            return this;
        }
        synchronized (mutex) {
            map.remove(name);
        }
        return this;
    }

    public Invocation getInvocation() {
        return invocation;
    }

}
