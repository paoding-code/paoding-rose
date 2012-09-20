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

import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import net.paoding.rose.web.portal.util.Enumerator;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
class SessionAfterCommitted implements HttpSession {

    IllegalStateException exception;

    public SessionAfterCommitted(IllegalStateException exception) {
        this.exception = exception;
    }

    @Override
    public Object getAttribute(String arg0) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enumeration getAttributeNames() {
        return new Enumerator(Collections.emptyList());
    }

    @Override
    public long getCreationTime() {
        return -1;
    }

    @Override
    public String getId() {
        return "-1";
    }

    @Override
    public long getLastAccessedTime() {
        return -1;
    }

    @Override
    public int getMaxInactiveInterval() {
        return -1;
    }

    @Override
    public ServletContext getServletContext() {
        throw new IllegalStateException(exception);
    }

    @SuppressWarnings("deprecation")
    @Override
    public javax.servlet.http.HttpSessionContext getSessionContext() {
        throw new IllegalStateException(exception);
    }

    @Override
    public Object getValue(String arg0) {
        return null;
    }

    @Override
    public String[] getValueNames() {
        return new String[0];
    }

    @Override
    public void invalidate() {
    }

    @Override
    public boolean isNew() {
        return false;
    }

    @Override
    public void putValue(String arg0, Object arg1) {
        throw new IllegalStateException(exception);
    }

    @Override
    public void removeAttribute(String arg0) {
        throw new IllegalStateException(exception);
    }

    @Override
    public void removeValue(String arg0) {
        throw new IllegalStateException(exception);
    }

    @Override
    public void setAttribute(String arg0, Object arg1) {
        throw new IllegalStateException(exception);
    }

    @Override
    public void setMaxInactiveInterval(int arg0) {
        throw new IllegalStateException(exception);
    }

}
