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

import java.lang.reflect.Method;

import net.paoding.rose.RoseConstants;
import net.paoding.rose.web.ControllerInterceptorAdapter;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.portal.Pipe;
import net.paoding.rose.web.portal.PortalUtils;
import net.paoding.rose.web.portal.Window;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class PipeInterceptor extends ControllerInterceptorAdapter {

    public PipeInterceptor() {
        // 优先级越高，after越后执行
        setPriority(10000);
    }

    @Override
    protected boolean isForAction(Method actionMethod, Class<?> controllerClazz) {
        for (Class<?> paramType : actionMethod.getParameterTypes()) {
            if (paramType == Pipe.class) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object after(Invocation inv, Object instruction) {

        // codes for fix this exception: "Cannot forward after response has been committed"
        // @see RoseFilter#supportsRosepipe
        // @see PortalImpl#addWindow
        Pipe pipe = PortalUtils.getPipe(inv);
        if (pipe != null && pipe.getInvocation() == inv) {
            boolean debugEnabled = logger.isDebugEnabled();
            if (debugEnabled) {
                logger.debug(pipe + " is going to wait pipe windows' ins.");
            }
            final long begin = System.currentTimeMillis();
            final long deadline;
            if (pipe.getTimeout() > 0) {
                deadline = begin + pipe.getTimeout();
            } else {
                deadline = -1;
            }

            try {
                for (Window window : pipe.getWindows()) {
                    if (window.get(RoseConstants.PIPE_WINDOW_IN) != Boolean.TRUE) {
                        synchronized (window) {
                            while (window.get(RoseConstants.PIPE_WINDOW_IN) != Boolean.TRUE) {
                                long now = System.currentTimeMillis();
                                if (deadline <= 0) {
                                    if (debugEnabled) {
                                        logger.debug("waitting for window '" + window.getName()
                                                + "''s in; timetou=never");
                                    }
                                    window.wait();
                                } else if (deadline > now) {
                                    if (debugEnabled) {
                                        logger.debug("waitting for window '" + window.getName()
                                                + "''s in; timetou=" + (deadline - now));
                                    }
                                    window.wait(deadline - now);
                                } else {
                                    if (logger.isInfoEnabled()) {
                                        logger.info("break waiting for this window's in '"
                                                + window.getName()
                                                + "@"
                                                + window.getContainer().getInvocation()
                                                        .getRequestPath() + "'");
                                    }
                                    break;
                                }

                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                logger.error("window-in waiting is interruptted.", e);
            }
            //
            if (logger.isDebugEnabled()) {
                logger.debug(pipe + ".window-in is done; cost="
                        + (System.currentTimeMillis() - begin));
            }
        }

        return instruction;
    }

    // 针对vm，通过afterCompletion我们可以做到自动写pipe
    // 对于jsp，则这个代码不会生效(isStarted会返回true)
    @Override
    public void afterCompletion(Invocation inv, Throwable ex) throws Exception {
        if (ex != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("close the pipe and returen because of exception previous.");
            }
            return;
        }
        //
        PipeImpl pipe = (PipeImpl) PortalUtils.getPipe(inv);
        if (pipe == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("there's no pipe windows.");
            }
            return;
        }

        if (inv != inv.getHeadInvocation()) {
            return;
        }
        if (!pipe.isStarted()) {
            if (logger.isDebugEnabled()) {
                logger.debug("writing " + pipe + "...");
            }

            pipe.write(inv.getResponse().getWriter());

            if (logger.isDebugEnabled()) {
                logger.debug("writing " + pipe + "... done");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug(pipe + " has been started yet.");
            }
        }

    }

}
