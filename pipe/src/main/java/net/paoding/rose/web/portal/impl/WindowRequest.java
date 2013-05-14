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
package net.paoding.rose.web.portal.impl;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import net.paoding.rose.web.portal.Window;
import net.paoding.rose.web.portal.util.Enumerator;

/**
 * 封装窗口请求，使每个窗口都有自己的独立属性空间，同时又能共享共同的portal请求对象的属性
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
class WindowRequest extends HttpServletRequestWrapper {

    private static final String HEAER_IF_MODIFIED_SINCE = "If-Modified-Since";

    private static final String HEAER_IF_NONE_MATHC = "If-None-Match";

    private final Window window;

    /**
     * 那些属性是这个窗口所不要的，在此标志
     */
    private Set<String> deleteAttributes;

    /** 锁 */
    private Object mutex = this;

    public WindowRequest(Window window, HttpServletRequest request) {
        super(request);
        this.window = window;
    }

    // ------------------------------------------------- ServletRequest Methods

    /**
     * 返回这个窗口的私有属性或portal主控请求对象的共同属性
     * 
     * @param name Name of the attribute to retrieve
     */
    public Object getAttribute(String name) {
        Object value = null;
        synchronized (mutex) {
            if (deleteAttributes != null && deleteAttributes.contains(name)) {
                return null;
            }
            value = window.get(name);
            if (value == null) {
                value = super.getAttribute(name);
            }
        }
        return value;
    }

    @Override
    public String getHeader(String name) {
        if (isDisabledHeader(name)) {
            return null;
        }
        return super.getHeader(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enumeration getHeaders(String name) {
        if (isDisabledHeader(name)) {
            return null;
        }
        return super.getHeaders(name);
    }

    /**
     * 判断指定header是否被屏蔽掉了。 为了window能够正确执行，可能会屏蔽掉一些header，
     * 例如，通过屏蔽If-Modified-Since和If-None-Match来解决 Window返回304的问题。
     * 
     * @param headerName
     * @return
     */
    private boolean isDisabledHeader(String headerName) {
        return HEAER_IF_MODIFIED_SINCE.equals(headerName) || HEAER_IF_NONE_MATHC.equals(headerName);
    }

    /**
     * 返回这个窗口的私有属性名加portal主控请求对象共同属性的属性名
     */
    @SuppressWarnings("unchecked")
    public Enumeration getAttributeNames() {
        HashSet<String> keys;
        synchronized (mutex) {
            keys = new HashSet<String>(window.getAttributes().keySet());
            Enumeration<String> names = super.getAttributeNames();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                if (deleteAttributes == null || !deleteAttributes.contains(name)) {
                    keys.add(name);
                }
            }
        }
        return new Enumerator(keys);
    }

    /**
     * 实际删除私有属性，如果是窗口共有的portal属性，只是在本窗口中做删除标志，其他窗口还能正常获取
     * 
     * @param name Name of the attribute to remove
     */
    public void removeAttribute(String name) {
        if (name == null) {
            throw new NullPointerException("don't set a NULL named attribute");
        }
        synchronized (mutex) {
            window.remove(name);
            if (deleteAttributes == null) {
                deleteAttributes = new HashSet<String>(4);
                deleteAttributes.add(name);
            }
        }
    }
    

    /**
     * 
     * 设置窗口私有属性
     * 
     * @param name Name of the attribute to set
     * @param value Value of the attribute to set
     */
    public void setAttribute(String name, Object value) {
        if (name == null) {
            throw new NullPointerException("don't set a NULL named attribute");
        }
        if (value == null) {
            removeAttribute(name);
            return;
        }
        synchronized (mutex) {
            window.set(name, value);
            if (deleteAttributes != null) {
                deleteAttributes.remove(name);
            }
        }
    }

    public Map<String, Object> getPrivateAttributes() {
        return window.getAttributes();
    }

    @Override
    public HttpSession getSession() {
        return getSession(true);
    }

    @Override
    public HttpSession getSession(boolean create) {
        HttpSession session = super.getSession(false);
        if (session != null) {
            return session;
        }
        if (create) {
            if (window.getContainer().getInvocation().getResponse().isCommitted()) {
                session = new SessionAfterCommitted(new IllegalStateException(
                        "Cannot create a session after the response has been committed"));
            } else {
                try {
                    session = super.getSession(true);
                } catch (IllegalStateException e) {
                    session = new SessionAfterCommitted(e);
                }
            }
        }
        return session;
    }

}
