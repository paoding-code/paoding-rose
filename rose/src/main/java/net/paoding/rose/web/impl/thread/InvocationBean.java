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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.InvocationUtils;
import net.paoding.rose.web.RequestPath;
import net.paoding.rose.web.impl.mapping.MatchResult;
import net.paoding.rose.web.impl.module.Module;
import net.paoding.rose.web.impl.validation.ParameterBindingResult;
import net.paoding.rose.web.paramresolver.ParamMetaData;
import net.paoding.rose.web.var.Flash;
import net.paoding.rose.web.var.FlashImpl;
import net.paoding.rose.web.var.Model;
import net.paoding.rose.web.var.ModelImpl;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public final class InvocationBean implements Invocation {

    public static final Object[] UN_INITIATED_ARRAY = new Object[0];

    private static final Log logger = LogFactory.getLog(InvocationBean.class);

    private Object[] methodParameters = UN_INITIATED_ARRAY; // 在还没有设置方法参数进来时为UN_INITIATED_ARRAY

    private Map<String, Object> attributes;

    private HttpServletRequest request;

    private HttpServletResponse response;

    private RequestPath requestPath;

    private transient Model model;

    private transient Flash flash;

    private Invocation preInvocation;

    private boolean multiPartRequest;

    private List<BindingResult> bindingResults;

    private List<String> bindingResultNames;

    private Rose rose;

    private Module viewModule;

    private ActionEngine actionEngine;

    private ControllerEngine controllerEngine;

    private ModuleEngine moduleEngine;

    public InvocationBean(HttpServletRequest request, HttpServletResponse response,
            RequestPath requestPath) {
        setRequest(request);
        setResponse(response);
        setRequestPath(requestPath);
    }

    public void setRose(Rose rose) {
        this.rose = rose;
    }

    public Rose getRose() {
        return rose;
    }

    protected boolean isMethodParametersInitiated() {
        return methodParameters != UN_INITIATED_ARRAY;
    }

    @Override
    public WebApplicationContext getApplicationContext() {
        return getModule().getApplicationContext();
    }

    public void setMethodParameters(Object[] methodParameters) {
        this.methodParameters = methodParameters;
    }

    @Override
    public Object getController() {
        ControllerEngine engine = getControllerEngine();
        return engine.getController();
    }

    @Override
    public Class<?> getControllerClass() {
        ControllerEngine engine = getControllerEngine();
        return engine.getControllerClass();
    }

    public Module getModule() {
        return getModuleEngine().getModule();
    }

    public Module getViewModule() {
        return viewModule == null ? getModule() : viewModule;
    }

    public void setViewModule(Module viewModule) {
        this.viewModule = viewModule;
    }

    @Override
    public Method getMethod() {
        return getActionEngine().getMethod();
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return getMethod().isAnnotationPresent(annotationClass)
                || getControllerClass().isAnnotationPresent(annotationClass);
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        T t = getMethod().getAnnotation(annotationClass);
        if (t == null) {
            t = getControllerClass().getAnnotation(annotationClass);
        }
        return t;
    }

    public ModuleEngine getModuleEngine() {
        if (moduleEngine != null) {
            return moduleEngine;
        }
        return moduleEngine = getEngine(ModuleEngine.class);
    }

    public ControllerEngine getControllerEngine() {
        if (controllerEngine != null) {
            return controllerEngine;
        }
        return controllerEngine = getEngine(ControllerEngine.class);
    }

    public ActionEngine getActionEngine() {
        if (actionEngine != null) {
            return actionEngine;
        }
        return actionEngine = getEngine(ActionEngine.class);
    }

    private <T extends Engine> T getEngine(Class<? extends Engine> engineClass) {
        for (LinkedEngine engine : rose.getEngines()) {
            if (engine.getTarget().getClass() == engineClass) {
                @SuppressWarnings("unchecked")
                T t = (T) engine.getTarget();
                return t;
            }
        }
        throw new Error("cannot found " + engineClass.getName());
    }

    @Override
    public String[] getMethodParameterNames() {
        return (String[]) ArrayUtils.clone(getActionEngine().getParameterNames());
    }

    @Override
    public Object[] getMethodParameters() {
        return methodParameters;
    }

    @Override
    public Object getMethodParameter(String name) {
        if (!isMethodParametersInitiated()) {
            throw new IllegalStateException();
        }
        String[] names = getActionEngine().getParameterNames();
        for (int i = 0; i < names.length; i++) {
            if (name != null && name.equals(names[i])) {
                return methodParameters[i];
            }
        }
        return null;
    }

    @Override
    public String getParameter(String name) {
        return request.getParameter(name);
    }

    @Override
    public void changeMethodParameter(int index, Object newValue) {
        if (!isMethodParametersInitiated()) {
            throw new IllegalStateException();
        }
        if (newValue != this.methodParameters[index]) {
            if (logger.isDebugEnabled()) {
                String[] names = getActionEngine().getParameterNames();
                logger.debug("change method parameter " + names[index] + " (index=" + index
                        + ") from '" + this.methodParameters[index] + "' to '" + newValue + "'");
            }
            Object oldValue = this.methodParameters[index];
            this.methodParameters[index] = newValue;
            if (logger.isDebugEnabled()) {
                logger.debug("change method parameter at " + index//
                        + ": " + oldValue + "->" + newValue);
            }
        }
    }

    public void changeMethodParameter(String name, Object newValue) {
        if (!isMethodParametersInitiated()) {
            throw new IllegalStateException();
        }
        if (StringUtils.isEmpty(name)) {
            throw new NullPointerException("parameter name");
        }
        String[] names = getActionEngine().getParameterNames();
        for (int i = 0; i < names.length; i++) {
            if (name.equals(names[i])) {
                changeMethodParameter(i, newValue);
                return;
            }
        }
    }

    @Override
    public void changeMethodParameter(ParamMetaData paramMeta, Object newValue) {
        changeMethodParameter(paramMeta.getIndex(), newValue);
    }

    @Override
    public void addModel(Object value) {
        getModel().add(value);
    }

    @Override
    public void addModel(String name, Object value) {
        getModel().add(name, value);
    }

    @Override
    public Model getModel() {
        if (this.model != null) {
            return this.model;
        }
        synchronized (this) {
            ModelImpl model = (ModelImpl) getRequest().getAttribute("$$paoding-rose.model");
            if (model == null || model.getInvocation() != this) {
                ModelImpl parent = model;
                model = new ModelImpl(this);
                if (parent != null && requestPath.isForwardRequest()) {
                    model.merge(parent.getAttributes());
                }
                getRequest().setAttribute("$$paoding-rose.model", model);
            }
            this.model = model;
        }
        return this.model;
    }

    @Override
    public Object getModel(String name) {
        return getModel().get(name);
    }

    @Override
    public synchronized Object getAttribute(String name) {
        if (attributes == null) {
            return null;
        }
        return attributes.get(name);
    }

    @Override
    public synchronized void removeAttribute(String name) {
        if (attributes != null) {
            attributes.remove(name);
        }
    }

    @Override
    public synchronized Set<String> getAttributeNames() {
        if (attributes == null) {
            return Collections.emptySet();
        }
        return attributes.keySet();
    }

    @Override
    public synchronized Invocation setAttribute(String name, Object value) {
        if (attributes == null) {
            attributes = new HashMap<String, Object>();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("setAttribute(" + name + "=" + value + ")");
        }
        attributes.put(name, value);
        return this;
    }

    @Override
    public void addFlash(String name, String msg) {
        getFlash(true).add(name, msg);
    }

    @Override
    public Flash getFlash() {
        return getFlash(true);
    }

    public Flash getFlash(boolean create) {
        if (this.flash != null) {
            return this.flash;
        }
        Flash flash = (Flash) getRequest().getAttribute("$$paoding-rose.flash");
        if (flash == null && create) {
            flash = new FlashImpl(this);
            getRequest().setAttribute("$$paoding-rose.flash", flash);
        }
        return this.flash = flash;
    }

    @Override
    public RequestPath getRequestPath() {
        return requestPath;
    }

    @Override
    public HttpServletRequest getRequest() {
        return this.request;
    }

    @Override
    public HttpServletResponse getResponse() {
        return response;
    }

    public void setRequest(HttpServletRequest request) {
        if (request == null) {
            throw new NullPointerException("request");
        }
        if (request == this.request) {
            return;
        }
        if (this.request == null) {
            this.request = request;
        } else {
            if (this.request == InvocationUtils.getCurrentThreadRequest()) {
                InvocationUtils.bindRequestToCurrentThread(request);
            }
            this.request = request;
        }
    }

    @Override
    public ServletContext getServletContext() {
        return getModule().getApplicationContext().getServletContext();
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public void setRequestPath(RequestPath requestPath) {
        this.requestPath = requestPath;
    }

    public Invocation getPreInvocation() {
        return preInvocation;
    }

    public void setPreInvocation(Invocation preInvocation) {
        this.preInvocation = preInvocation;
    }

    @Override
    public Invocation getHeadInvocation() {
        Invocation inv = this;
        while (inv.getPreInvocation() != null) {
            inv = inv.getPreInvocation();
        }
        return inv;
    }

    public void setMultiPartRequest(boolean multiPartRequest) {
        this.multiPartRequest = multiPartRequest;
    }

    public boolean isMultiPartRequest() {
        return multiPartRequest;
    }

    @Override
    public List<BindingResult> getBindingResults() {
        fetchBindingResults();
        return this.bindingResults;
    }

    @Override
    public List<String> getBindingResultNames() {
        fetchBindingResults();
        return this.bindingResultNames;
    }

    @Override
    public BindingResult getParameterBindingResult() {
        return (BindingResult) this.getModel().get(
                BindingResult.MODEL_KEY_PREFIX + ParameterBindingResult.OBJECT_NAME);
    }

    @Override
    public BindingResult getBindingResult(String name) {
        Assert.notNull(name);
        if (name instanceof String) {
            if (!((String) name).endsWith("BindingResult")) {
                name = name + "BindingResult";
            }
            BindingResult br = (BindingResult) this.getModel().get(
                    BindingResult.MODEL_KEY_PREFIX + name);
            if (br == null) {
                br = getParameterBindingResult();
            }
            return br;
        }
        return null;
    }

    protected void fetchBindingResults() {
        if (this.bindingResults == null) {
            Map<String, Object> attributes = getModel().getAttributes();
            List<String> bindingResultNames = new ArrayList<String>();
            List<BindingResult> bindingResults = new ArrayList<BindingResult>();
            for (String key : attributes.keySet()) {
                if (key.startsWith(BindingResult.MODEL_KEY_PREFIX)) {
                    Object value = attributes.get(key);
                    if (value instanceof BindingResult) {
                        bindingResults.add((BindingResult) value);
                        bindingResultNames.add(((BindingResult) value).getObjectName());
                    }
                }
            }
            this.bindingResults = Collections.unmodifiableList(bindingResults);
            this.bindingResultNames = Collections.unmodifiableList(bindingResultNames);
        }
    }

    @Override
    public String getResourceId() {
        StringBuilder sb = new StringBuilder(255);
        sb.append(getRequest().getContextPath());
        for (MatchResult matchResult : rose.getMatchResults()) {
            sb.append(matchResult.getMappingNode().getMappingPath());
        }
        return sb.toString();
    }

    @Override
    public void addAfterCompletion(AfterCompletion afterComletion) {
        rose.addAfterCompletion(afterComletion);
    }

    @Override
    public String toString() {
        return requestPath.getUri();
    }

}
