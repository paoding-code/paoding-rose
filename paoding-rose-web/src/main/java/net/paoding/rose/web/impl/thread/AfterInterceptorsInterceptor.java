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

import java.lang.reflect.Method;

import net.paoding.rose.web.AfterInterceptors;
import net.paoding.rose.web.ControllerInterceptorAdapter;
import net.paoding.rose.web.Invocation;

/**
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class AfterInterceptorsInterceptor extends ControllerInterceptorAdapter {

    public AfterInterceptorsInterceptor() {
        setPriority(Integer.MAX_VALUE);
    }

    @Override
    protected boolean isForAction(Method actionMethod, Class<?> controllerClazz) {
    	for (Class<?> clazz :actionMethod.getParameterTypes()) {
            if (AfterInterceptors.class.isAssignableFrom(clazz)) {
                return true;
            }
        }
    	return false;
    }

    @Override
    public Object after(Invocation inv, Object instruction) throws Exception {
        for (Object object : inv.getMethodParameters()) {
            if (object instanceof AfterInterceptors) {
                instruction = ((AfterInterceptors) object).doAfterInterceptors(inv, instruction);
            }
        }
        return instruction;
    }
}
