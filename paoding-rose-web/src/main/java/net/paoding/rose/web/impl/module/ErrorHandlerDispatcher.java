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
package net.paoding.rose.web.impl.module;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.paoding.rose.web.ControllerErrorHandler;
import net.paoding.rose.web.Invocation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author zhiliang.wang
 * 
 */
public class ErrorHandlerDispatcher implements ControllerErrorHandler {

    private Log logger = LogFactory.getLog(getClass());

    private static final int INVOCATION_INDEX = 0;

    private static final int THROWABLE_INDEX = 1;

    private ControllerErrorHandler errorHandler;

    private List<ErrorHandlerDelegate> delegates = new ArrayList<ErrorHandlerDelegate>(8);

    public ErrorHandlerDispatcher(ControllerErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        Method[] methods = this.errorHandler.getClass().getMethods();
        for (final Method method : methods) {
            if (Modifier.isAbstract(method.getModifiers())
                    || Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (method.getName().equals("onError")) {
                final Class<?>[] parameterClasses = method.getParameterTypes();
                if (parameterClasses.length == 2
                        && parameterClasses[INVOCATION_INDEX] == Invocation.class
                        && Throwable.class.isAssignableFrom(parameterClasses[THROWABLE_INDEX])) {
                    delegates.add(new ErrorHandlerDelegate() {

                        @Override
                        public Method getMethod() {
                            return method;
                        }

                        @Override
                        public Object onError(Invocation inv, Throwable ex) throws Throwable {
                            Object[] args = new Object[] { inv, ex };
                            try {
                                return method
                                        .invoke(ErrorHandlerDispatcher.this.errorHandler, args);
                            } catch (Throwable e) {
                                logger.error("error happened when handling error " + ex.getClass()
                                        + " at " + ErrorHandlerDispatcher.this.toString());
                                throw e;
                            }
                        }
                    });
                }
            }
        }
        Collections.sort(delegates, new Comparator<ErrorHandlerDelegate>() {

            @Override
            public int compare(ErrorHandlerDelegate o1, ErrorHandlerDelegate o2) {
                if (o1.getMethod().getParameterTypes()[THROWABLE_INDEX].isAssignableFrom(o2
                        .getMethod().getParameterTypes()[THROWABLE_INDEX])) {
                    return 1;
                } else if (o2.getMethod().getParameterTypes()[THROWABLE_INDEX].isAssignableFrom(o1
                        .getMethod().getParameterTypes()[THROWABLE_INDEX])) {
                    return -1;
                } else {
                    return o1.getMethod().getParameterTypes()[THROWABLE_INDEX].getName().compareTo(
                            o2.getMethod().getParameterTypes()[THROWABLE_INDEX].getName());
                }
            }
        });
    }

    @Override
    public Object onError(Invocation inv, Throwable ex) throws Throwable {
        for (ErrorHandlerDelegate delegate : delegates) {
            if (delegate.getMethod().getParameterTypes()[THROWABLE_INDEX].isAssignableFrom(ex
                    .getClass())) {
                return delegate.onError(inv, ex);
            }
        }
        throw new Error(
                "not found errorHandlerMethod for exceptionClass" + ex.getClass().getName(), ex);
    }

    @Override
    public String toString() {
        return errorHandler.toString() + delegates.toString();
    }

    static abstract class ErrorHandlerDelegate {

        abstract Method getMethod();

        abstract Object onError(Invocation inv, Throwable ex) throws Throwable;

        @Override
        public String toString() {
            return getMethod().getParameterTypes()[THROWABLE_INDEX].getSimpleName();
        }
    }

}
