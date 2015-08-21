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

import net.paoding.rose.web.ControllerInterceptorAdapter;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.InvocationChain;
import net.paoding.rose.web.portal.PortalUtils;

/**
 * 
 * @author qieqie.wang@gmail.com
 * 
 */
public class WindowCancelableSupportInterceptor extends ControllerInterceptorAdapter {

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected Object round(Invocation inv, InvocationChain chain) throws Exception {
        WindowImpl win = (WindowImpl) PortalUtils.getWindow(inv);
        if (win == null || win.mayInterruptIfRunning()) {
            return super.round(inv, chain);
        }
        //
        if (Thread.currentThread().isInterrupted()) {
            Thread.interrupted(); // clear the interruption
            win.setInterrupted(true);
            return "@interrupted " + win.getPath();
        }
        try {
            return super.round(inv, chain);
        } finally {
            if (win.isCancelled()) {
                Thread.interrupted(); // clear the interruption
                return "@interrupted " + win.getPath();
            }
        }
    }

}
