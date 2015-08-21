/*
 * Copyright 2007-2010 the original author or authors.
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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import net.paoding.rose.RoseFilter;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.RequestPath;
import net.paoding.rose.web.instruction.InstructionExecutor;
import net.paoding.rose.web.instruction.InstructionExecutorImpl;
import net.paoding.rose.web.var.FlashImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link RootEngine}从{@link RoseFilter}接收web请求，并按照Rose规则进行处理.
 * <p>
 * {@link RootEngine}会判断该web请求是否是本{@link RootEngine}
 * 应该处理的，如果是进行后续的委派和处理，如果不是则{@link #match(InvocationBean)}返回false给上层.
 * <p>
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class RootEngine implements Engine {

    // ------------------------------------------------------------

    protected final Log logger = LogFactory.getLog(getClass());

    /** 由它最终负责执行模块返回给Rose的指令，进行页面渲染等 */
    protected InstructionExecutor instructionExecutor = new InstructionExecutorImpl();

    // ------------------------------------------------------------

    /**
     * 构造能够将请求正确转发到所给modules的 {@link RootEngine}对象.
     * <p>
     * 此构造子将调用 {@link #initMappings(List)}进行初始化，需要时，子类可以覆盖提供新的实现
     * 
     * @param modules 模块的集合，非空，对排序无要求； 如果集合的元素为null，该空元素将被忽略
     * @throws Exception
     * @throws NullPointerException 如果所传入的模块集合为null时
     */
    public RootEngine(InstructionExecutor instructionExecutor) {
        if (instructionExecutor != null) {
            this.instructionExecutor = instructionExecutor;
        }
    }

    // ------------------------------------------------------------

    @Override
    public int isAccepted(HttpServletRequest rose) {
        return 1;
    }

    /**
     * {@link RootEngine} 接口.调用此方法判断并处理请求.如果本引擎能够找到该请求相应的控制器方法处理，则启动整个调用过程，
     * 并最终渲染页面到客户端;
     * 
     * @param request
     * @param response
     * @return
     * @throws ServletException
     */

    @Override
    public Object execute(Rose rose) throws Throwable {

        InvocationBean inv = rose.getInvocation();
        ServletRequest request = inv.getRequest();
        //
        if (request.getCharacterEncoding() == null) {
            request.setCharacterEncoding("UTF-8");
            if (logger.isDebugEnabled()) {
                logger.debug("set request.characterEncoding by default:"
                        + request.getCharacterEncoding());
            }
        }

        //
        final RequestPath requestPath = inv.getRequestPath();

        // save before include
        if (requestPath.isIncludeRequest()) {
            saveAttributesBeforeInclude(inv);
            // 恢复include请求前的各种请求属性(包括Model对象)
            rose.addAfterCompletion(new AfterCompletion() {

                @Override
                public void afterCompletion(Invocation inv, Throwable ex) throws Exception {
                    restoreRequestAttributesAfterInclude(inv);
                }
            });
        }

        // 调用之前设置内置属性
        inv.addModel("invocation", inv);
        inv.addModel("ctxpath", requestPath.getCtxpath());

        // instruction是控制器action方法的返回结果或其对应的Instruction对象(也可能是拦截器、错误处理器返回的)
        Object instruction = rose.doNext();

        if (Thread.currentThread().isInterrupted()) {
            logger.info("stop to render: thread is interrupted");
        } else {
            // 写flash消息到Cookie (被include的请求不会有功能)
            if (!requestPath.isIncludeRequest()) {
                FlashImpl flash = (FlashImpl) inv.getFlash(false);
                if (flash != null) {
                    flash.writeNewMessages();
                }
            }

            // 渲染页面
            instructionExecutor.render(inv, instruction);
        }
        return instruction;
    }

    @Override
    public void destroy() {
    }

    /**
     * Keep a snapshot of the request attributes in case of an include, to
     * be able to restore the original attributes after the include.
     * 
     * @param inv
     */
    private void saveAttributesBeforeInclude(final Invocation inv) {
        ServletRequest request = inv.getRequest();
        logger.debug("Taking snapshot of request attributes before include");
        Map<String, Object> attributesSnapshot = new HashMap<String, Object>();
        Enumeration<?> attrNames = request.getAttributeNames();
        while (attrNames.hasMoreElements()) {
            String attrName = (String) attrNames.nextElement();
            attributesSnapshot.put(attrName, request.getAttribute(attrName));
        }
        inv.setAttribute("$$paoding-rose.attributesBeforeInclude", attributesSnapshot);
    }

    /**
     * Restore the request attributes after an include.
     * 
     * @param request current HTTP request
     * @param attributesSnapshot the snapshot of the request attributes
     *        before the include
     */
    private void restoreRequestAttributesAfterInclude(Invocation inv) {
        logger.debug("Restoring snapshot of request attributes after include");
        HttpServletRequest request = inv.getRequest();

        @SuppressWarnings("unchecked")
        Map<String, Object> attributesSnapshot = (Map<String, Object>) inv
                .getAttribute("$$paoding-rose.attributesBeforeInclude");

        // Need to copy into separate Collection here, to avoid side effects
        // on the Enumeration when removing attributes.
        Set<String> attrsToCheck = new HashSet<String>();
        Enumeration<?> attrNames = request.getAttributeNames();
        while (attrNames.hasMoreElements()) {
            String attrName = (String) attrNames.nextElement();
            attrsToCheck.add(attrName);
        }

        // Iterate over the attributes to check, restoring the original value
        // or removing the attribute, respectively, if appropriate.
        for (String attrName : attrsToCheck) {
            Object attrValue = attributesSnapshot.get(attrName);
            if (attrValue != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Restoring original value of attribute [" + attrName
                            + "] after include");
                }
                request.setAttribute(attrName, attrValue);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Removing attribute [" + attrName + "] after include");
                }
                request.removeAttribute(attrName);
            }
        }

    }

    @Override
    public String toString() {
        return "root";
    }
}
