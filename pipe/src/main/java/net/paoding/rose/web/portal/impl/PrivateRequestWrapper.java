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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

/**
 * 私有请求包装器，但不继承于 {@link HttpServletRequestWrapper}
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class PrivateRequestWrapper implements HttpServletRequest {

    private HttpServletRequest request;

    private Object mutex;

    public PrivateRequestWrapper(HttpServletRequest request) {
        this.request = request;
        this.mutex = request;
    }

    protected HttpServletRequest getRequest() {
        return request;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return getRequest().getRequestDispatcher(path);
    }

    @Override
    public Object getAttribute(String name) {
        return getRequest().getAttribute(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Enumeration getAttributeNames() {
        synchronized (mutex) {
            return getRequest().getAttributeNames();
        }
    }

    @Override
    public void removeAttribute(String name) {
        synchronized (mutex) {
            getRequest().removeAttribute(name);
        }
    }

    @Override
    public void setAttribute(String name, Object value) {
        synchronized (mutex) {
            getRequest().setAttribute(name, value);
        }
    }

    @Override
    public String getContextPath() {
        return getRequest().getContextPath();
    }

    @Override
    public String getQueryString() {
        return getRequest().getQueryString();
    }

    @Override
    public String getRequestURI() {
        return getRequest().getRequestURI();
    }

    @Override
    public String getServletPath() {
        return getRequest().getServletPath();
    }

    @Override
    public StringBuffer getRequestURL() {
        return getRequest().getRequestURL();
    }

    @Override
    public String getAuthType() {
        return getRequest().getAuthType();
    }

    @Override
    public Cookie[] getCookies() {
        synchronized (mutex) {
            return getRequest().getCookies();
        }
    }

    @Override
    public long getDateHeader(String name) {
        synchronized (mutex) {
            return getRequest().getDateHeader(name);
        }
    }

    @Override
    public String getHeader(String name) {
        synchronized (mutex) {
            return getRequest().getHeader(name);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Enumeration getHeaders(String name) {
        synchronized (mutex) {
            return getRequest().getHeaders(name);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Enumeration getHeaderNames() {
        synchronized (mutex) {
            return getRequest().getHeaderNames();
        }
    }

    @Override
    public int getIntHeader(String name) {
        synchronized (mutex) {
            return getRequest().getIntHeader(name);
        }
    }

    @Override
    public String getMethod() {
        return getRequest().getMethod();
    }

    @Override
    public String getPathInfo() {
        return getRequest().getPathInfo();
    }

    @Override
    public String getPathTranslated() {
        return getRequest().getPathTranslated();
    }

    @Override
    public String getRemoteUser() {
        return getRequest().getRemoteUser();
    }

    @Override
    public boolean isUserInRole(String role) {
        return getRequest().isUserInRole(role);
    }

    @Override
    public java.security.Principal getUserPrincipal() {
        return getRequest().getUserPrincipal();
    }

    @Override
    public String getRequestedSessionId() {
        return getRequest().getRequestedSessionId();
    }

    @Override
    public HttpSession getSession(boolean create) {
        return getRequest().getSession(create);
    }

    @Override
    public HttpSession getSession() {
        return getRequest().getSession();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return getRequest().isRequestedSessionIdValid();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return getRequest().isRequestedSessionIdFromCookie();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return getRequest().isRequestedSessionIdFromURL();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return getRequest().isRequestedSessionIdFromUrl();
    }

    @Override
    public String getCharacterEncoding() {
        return getRequest().getCharacterEncoding();
    }

    @Override
    public void setCharacterEncoding(String enc) throws java.io.UnsupportedEncodingException {
        synchronized (mutex) {
            getRequest().setCharacterEncoding(enc);
        }
    }

    @Override
    public int getContentLength() {
        return getRequest().getContentLength();
    }

    @Override
    public String getContentType() {
        return getRequest().getContentType();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        synchronized (mutex) {
            return getRequest().getInputStream();
        }
    }

    @Override
    public String getParameter(String name) {
        synchronized (mutex) {
            return getRequest().getParameter(name);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized Map getParameterMap() {
        // 如果没有同步，tomcat下可能出现java.lang.IllegalStateException: No modifications are allowed to a locked ParameterMap
        // see http://code.google.com/p/paoding-rose/issues/detail?id=9
        return getRequest().getParameterMap();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Enumeration getParameterNames() {
        return getRequest().getParameterNames();
    }

    @Override
    public String[] getParameterValues(String name) {
        return getRequest().getParameterValues(name);
    }

    @Override
    public String getProtocol() {
        return getRequest().getProtocol();
    }

    @Override
    public String getScheme() {
        return getRequest().getScheme();
    }

    @Override
    public String getServerName() {
        return getRequest().getServerName();
    }

    @Override
    public int getServerPort() {
        return getRequest().getServerPort();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        synchronized (mutex) {
            return getRequest().getReader();
        }
    }

    @Override
    public String getRemoteAddr() {
        return getRequest().getRemoteAddr();
    }

    @Override
    public String getRemoteHost() {
        return getRequest().getRemoteHost();
    }

    @Override
    public Locale getLocale() {
        return getRequest().getLocale();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Enumeration getLocales() {
        return getRequest().getLocales();
    }

    @Override
    public boolean isSecure() {
        return getRequest().isSecure();
    }

    @SuppressWarnings("deprecation")
    @Override
    public String getRealPath(String path) {
        return getRequest().getRealPath(path);
    }

    @Override
    public int getRemotePort() {
        return getRequest().getRemotePort();
    }

    @Override
    public String getLocalName() {
        return getRequest().getLocalName();
    }

    @Override
    public String getLocalAddr() {
        return getRequest().getLocalAddr();
    }

    @Override
    public int getLocalPort() {
        return getRequest().getLocalPort();
    }

}
