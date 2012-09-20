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
package net.paoding.rose.web;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import net.paoding.rose.web.annotation.ReqMethod;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.util.WebUtils;

/**
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class RequestPath {

    private static Log logger = LogFactory.getLog(RequestPath.class);

    private ReqMethod method;

    private String uri; // = contextPath + ctxpath + pathInfo

    private String ctxpath; // by servlet container

    private String rosePath; // = modulePath + controllerPath + actionPath

    private String modulePath; //

    private String controllerPathInfo; //

    private String controllerPath;

    private String actionPath;

    private Dispatcher dispatcher;

    public RequestPath(ReqMethod method, String uri, String ctxpath, Dispatcher dispatcher) {
        this.setMethod(method);
        setUri(uri);
        setCtxpath(ctxpath);
        setDispatcher(dispatcher);
        setRosePath(uri.substring(ctxpath.length()));
    }

    public RequestPath(HttpServletRequest request) {
        // method
        setMethod(parseMethod(request));

        // ctxpath
        setCtxpath(request.getContextPath());
        String invocationCtxpath = null; // 对include而言，invocationCtxPath指的是被include的ctxpath
        // dispather, uri, ctxpath
        String uri;
        if (WebUtils.isIncludeRequest(request)) {
            setDispatcher(Dispatcher.INCLUDE);
            uri = (String) request.getAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE);
            invocationCtxpath = ((String) request
                    .getAttribute(WebUtils.INCLUDE_CONTEXT_PATH_ATTRIBUTE));
            setRosePath((String) request.getAttribute(WebUtils.INCLUDE_SERVLET_PATH_ATTRIBUTE));
        } else {
            uri = request.getRequestURI();
            this.setRosePath(request.getServletPath());
            if (request.getAttribute(WebUtils.FORWARD_REQUEST_URI_ATTRIBUTE) == null) {
                this.setDispatcher(Dispatcher.REQUEST);
            } else {
                this.setDispatcher(Dispatcher.FORWARD);
            }
        }
        if (uri.startsWith("http://") || uri.startsWith("https://")) {
            int start = uri.indexOf('/', 9);
            if (start == -1) {
                uri = "";
            } else {
                uri = uri.substring(start);
            }
        }
        if (uri.indexOf('%') != -1) {
            try {
                String encoding = request.getCharacterEncoding();
                if (encoding == null || encoding.length() == 0) {
                    encoding = "UTF-8";
                }
                uri = URLDecoder.decode(uri, encoding);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        this.setUri(uri);
        // 记录到requestPath的ctxpath值在include的情况下是invocationCtxpath

        if (getCtxpath().length() <= 1) {
            setRosePath(getUri());
        } else {
            setRosePath(getUri().substring(
                    (invocationCtxpath == null ? getCtxpath() : invocationCtxpath).length()));
        }
    }

    private ReqMethod parseMethod(HttpServletRequest request) {
        ReqMethod reqMethod = ReqMethod.parse(request.getMethod());
        if (reqMethod != null && reqMethod.equals(ReqMethod.POST)) {
            // 为什么不用getParameter：
            // 1、使_method只能在queryString中，不能在body中
            // 2、getParameter会导致encoding，使用UTF-8? 尽量不做这个假设
            String queryString = request.getQueryString();
            if (queryString != null) {
                boolean methodChanged = false;
                int start = queryString.indexOf("_method=");
                if (start == 0 || (start > 0 && queryString.charAt(start - 1) == '&')) {
                    int end = queryString.indexOf('&', start);
                    String method = queryString.substring(start + "_method=".length(),//
                            end > 0 ? end : queryString.length());
                    ReqMethod _reqMethod = ReqMethod.parse(method);
                    if (_reqMethod != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("override http method from POST to " + _reqMethod);
                        }
                        reqMethod = _reqMethod;
                        methodChanged = true;
                    }
                }
                if (!methodChanged) {
                    int inBodyStart = queryString.indexOf("_method_in_body=1");
                    if (inBodyStart == 0
                            || (inBodyStart > 0 && queryString.charAt(inBodyStart - 1) == '&')) {
                        String method = request.getParameter("_method");
                        ReqMethod _reqMethod = ReqMethod.parse(method);
                        if (_reqMethod != null) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("override http method from POST to " + _reqMethod);
                            }
                            reqMethod = _reqMethod;
                            methodChanged = true;
                        }
                    }
                }
            }
        }
        return reqMethod;
    }

    public boolean isIncludeRequest() {
        return dispatcher == Dispatcher.INCLUDE;
    }

    public boolean isForwardRequest() {
        return dispatcher == Dispatcher.FORWARD;
    }

    public void setDispatcher(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public ReqMethod getMethod() {
        return method;
    }

    public void setMethod(ReqMethod method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getCtxpath() {
        return ctxpath;
    }

    public void setCtxpath(String ctxpath) {
        this.ctxpath = ctxpath;
    }

    public String getRosePath() {
        return rosePath;
    }

    public void setRosePath(String rosePath) {
        // 如果是"/"，也转化为""
        if (rosePath.equals("") || rosePath.equals("/")) {
            this.rosePath = "";
            return;
        }
        // 只判断一次，如果有以request请求以'//'结尾的，rose肯定会映射失败，但是rose不会为了兼容做这个事情
        if (rosePath.charAt(rosePath.length() - 1) == '/') {
            rosePath = rosePath.substring(0, rosePath.length() - 1);
        }
        this.rosePath = rosePath;
    }

    public String getModulePath() {
        return modulePath;
    }

    public void setModulePath(String modulePath) {
        this.modulePath = modulePath;
    }

    public String getControllerPathInfo() {
        if (controllerPathInfo == null) {
            controllerPathInfo = rosePath.substring(modulePath.length());
        }
        return controllerPathInfo;
    }

    public String getControllerPath() {
        return controllerPath;
    }

    public void setControllerPath(String controllerPath) {
        this.controllerPath = controllerPath;
    }

    public String getActionPath() {
        return actionPath;
    }

    public void setActionPath(String actionPath) {
        this.actionPath = actionPath;
    }

    @Override
    public String toString() {
        return "ctxpath=" + ctxpath + "; pathInfo=" + rosePath + "; modulePath=" + modulePath
                + "; controllerPath=" + controllerPath + "; actionPath=" + actionPath;
    }

}
