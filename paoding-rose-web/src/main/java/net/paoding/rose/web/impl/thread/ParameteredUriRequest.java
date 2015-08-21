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
package net.paoding.rose.web.impl.thread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class ParameteredUriRequest extends HttpServletRequestWrapper {

    private final Map<String, String> parameters;

    public ParameteredUriRequest(HttpServletRequest request, Map<String, String> parameters) {
        super(request);
        this.parameters = parameters;
    }

    // 优先获取queryString或forward之后的请求的参数，只有获取不到时，才从URI里获取
    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if (value == null) {
            value = parameters.get(name);
        }
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map getParameterMap() {
        Map<String, String[]> map = new HashMap<String, String[]>(super.getParameterMap());
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (!map.containsKey(entry.getKey())) {
                map.put(entry.getKey(), new String[] { parameters.get(entry.getKey()) });
            }
        }
        return Collections.unmodifiableMap(map);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] value = super.getParameterValues(name);
        if (value == null || value.length == 0) {
            String _value = parameters.get(name);
            if (_value != null) {
                value = new String[] { _value };
            }
        }
        // javadoc: 
        // Returns an array of String objects containing all of the values the given request parameter has,
        // or null if the parameter does not exist.
        return value == null || value.length == 0 ? null : value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Enumeration getParameterNames() {
        final Enumeration<String> requestParamNames = super.getParameterNames();

        return new Enumeration<String>() {

            final Iterator<String> matchResultParamNames = new ArrayList<String>(parameters
                    .keySet()).iterator();

            @Override
            public boolean hasMoreElements() {
                return matchResultParamNames.hasNext() || requestParamNames.hasMoreElements();
            }

            @Override
            public String nextElement() {
                if (matchResultParamNames.hasNext()) {
                    return matchResultParamNames.next();
                }
                if (requestParamNames.hasMoreElements()) {
                    return requestParamNames.nextElement();
                }
                throw new NoSuchElementException();
            }

        };
    }
}
