/*
 * Copyright 2007-2012 the original author or authors.
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

import java.util.concurrent.ExecutorService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.impl.thread.InvocationBean;
import net.paoding.rose.web.portal.Pipe;
import net.paoding.rose.web.portal.Portal;
import net.paoding.rose.web.portal.PortalFactory;
import net.paoding.rose.web.portal.WindowListener;
import net.paoding.rose.web.portal.PortalSetting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;

/**
 * {@link PortalFactory} 的实现。
 * <p>
 * 
 * 创建 {@link PortalFactoryImpl}实例后，应该通过
 * {@link #setExecutorService(ExecutorService)} 或
 * {@link #setExecutorServiceBySpring(ThreadPoolTaskExecutor)}
 * 设置执行器，用于执行Portal下的每个“窗口请求”。
 * <p>
 * 
 * 可选设置 {@link WindowListener} 来获知portal的创建以及窗口的创建、执行等状态信息。
 * 
 * @see PortalImpl
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class PortalFactoryImpl implements PortalFactory, InitializingBean {

    protected Log logger = LogFactory.getLog(getClass());

    private ExecutorService executorService;

    private WindowListener windowListener;

    public void setExecutorService(ExecutorService executor) {
        if (logger.isInfoEnabled()) {
            logger.info("using executorService: " + executor);
        }
        this.executorService = executor;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setWindowListener(WindowListener portalListener) {
        this.windowListener = portalListener;
    }

    public WindowListener getWindowListener() {
        return windowListener;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(windowListener);
        Assert.notNull(executorService);
    }

    @Override
    public Portal createPortal(Invocation inv) {
        PortalImpl portal = (PortalImpl) inv
                .getAttribute("$$paoding-rose-portal.portal");
        if (portal != null) {
            return portal;
        }
        portal = new PortalImpl(inv, executorService, windowListener);
        //
        long timeout = 0;
        PortalSetting portalSetting = inv.getMethod().getAnnotation(PortalSetting.class);
        if (portalSetting != null) {
            if (portalSetting.timeout() >= 0) {
                long annotationTimeout = portalSetting.timeUnit().toMillis(portalSetting.timeout());
                // < 0的情况，是PortalSetting的默认设置，即如果PortalSetting没有设置有效的timeout，则使用defaultTimeout策略
                // == 0的情况表示并且要求表示不需要设置超时时间，并且也不使用defaultTimeout策略
                if (annotationTimeout >= 0) {
                    timeout = annotationTimeout;
                }
            }
        }
        if (timeout > 0) {
            portal.setTimeout(timeout);
        }

        // 换request对象
        HttpServletRequest innerRequest = inv.getRequest();
        HttpServletRequestWrapper requestWrapper = null;
        while (innerRequest instanceof HttpServletRequestWrapper) {
            requestWrapper = (HttpServletRequestWrapper) innerRequest;
            innerRequest = (HttpServletRequest) ((HttpServletRequestWrapper) innerRequest)
                    .getRequest();
        }
        final PortalRequest portalRequest = new PortalRequest(portal, innerRequest);
        if (requestWrapper == null) {
            inv.setRequest(portalRequest);
        } else {
            requestWrapper.setRequest(portalRequest);
        }

        // 换response对象
        HttpServletResponse innerResponse = inv.getResponse();
        HttpServletResponseWrapper responseWrapper = null;
        while (innerResponse instanceof HttpServletResponseWrapper) {
            responseWrapper = (HttpServletResponseWrapper) innerResponse;
            innerResponse = (HttpServletResponse) ((HttpServletResponseWrapper) innerResponse)
                    .getResponse();
        }
        final PortalResponse portalResponse = new PortalResponse(portal, innerResponse);
        if (responseWrapper == null) {
            ((InvocationBean) inv).setResponse(portalResponse);
        } else {
            responseWrapper.setResponse(portalResponse);
        }

        //
        inv.setAttribute("$$paoding-rose-portal.portal", portal);

        return portal;
    }

    @Override
    public Pipe createPipe(Invocation inv, boolean create) {
        PipeImpl pipe = (PipeImpl) inv.getHeadInvocation().getAttribute(
                "$$paoding-rose-portal.pipe");
        if (pipe == null) {
            if (create) {
                pipe = new PipeImpl(inv, executorService, windowListener);
                inv.getHeadInvocation().setAttribute("$$paoding-rose-portal.pipe", pipe);
            }
        } else if (pipe.getInvocation() != inv) {
            // 因为PortalWaitInterceptor的waitForPipeWindows无法良好处理
            // 尚不支持portal/pipe转发出去的地址还使用pipe
            // 否则，waitForPipeWindows的getWindows方法可能会有java.util.ConcurrentModificationException异常
            throw new UnsupportedOperationException(//
                    "Pipe is only allowed in one place for a request, "
                            + "don't forward to path that using pipe. ");
        }
        return pipe;
    }

}
