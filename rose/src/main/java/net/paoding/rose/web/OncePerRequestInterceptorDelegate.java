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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO 目前是每个Module中都有一个指定interceptor的实例（或Delegate），
 * 这个可能造成一些隐患，需要修改掉。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class OncePerRequestInterceptorDelegate extends InterceptorDelegate {

    private static Log logger = LogFactory.getLog(OncePerRequestInterceptorDelegate.class);

    private final String filteredKey;

    public OncePerRequestInterceptorDelegate(ControllerInterceptor interceptor) {
        super(interceptor);
        String realInterceptorName = getMostInnerInterceptor(interceptor).getClass().getName();
        this.filteredKey = "$$paoding-rose.interceptor.oncePerRequest." + "." + realInterceptorName;
    }

    @Override
    public Object roundInvocation(Invocation inv, InvocationChain chain) throws Exception {
        Invocation tempInv = inv;
        boolean tobeIntercepted; // true代表该执行拦截器，false代表不执行该拦截器而把流程交给下一个拦截器
        
        int preCount = 0;
        while (true) {
            tobeIntercepted = (tempInv.getAttribute(filteredKey) == null);
            if (!tobeIntercepted) {
                if (logger.isDebugEnabled()) {
                	logger.debug(preCount + "-th preInvocation found filteredKey:" + filteredKey);
                }
            	break;
            }
            tempInv = tempInv.getPreInvocation();
            preCount ++;
            if (logger.isDebugEnabled()) {
            	logger.debug(preCount + "-th preInvocation:" + tempInv);
            }
            if (tempInv == null) {
                break;
            }
        }
        if (tobeIntercepted) {
            inv.setAttribute(filteredKey, Boolean.TRUE);
            if (logger.isDebugEnabled()) {
                logger.debug("do oncePerRequest interceptor.roundInvocation: " + getName());
            }
            return super.roundInvocation(inv, chain);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("skip oncePerRequest interceptor.roundInvocation: " + getName());
            }
            return chain.doNext();
        }
    }

    @Override
    public final void afterCompletion(Invocation inv, Throwable ex) throws Exception {
        if (inv.getAttribute(filteredKey) != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("do oncePerRequest interceptor.afterCompletion: " + getName());
            }
            super.afterCompletion(inv, ex);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("skip oncePerRequest interceptor.afterCompletion: " + getName());
            }
        }
    }

    @Override
    public String toString() {
        return "oncePerRequest." + this.interceptor.toString();
    }

}
