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
package net.paoding.rose.web.impl.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.impl.thread.Engine;
import net.paoding.rose.web.impl.thread.LinkedEngine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class EngineGroupImpl implements EngineGroup {

    private static final Log logger = LogFactory.getLog(EngineGroup.class);

    /** ARRAY_SIZE 代表用于存放 Engine 的数组的大小 */
    private static final int ARRAY_SIZE = ReqMethod.ALL.parse().size();

    private static final LinkedEngine[] emptyEngines = new LinkedEngine[0];

    /**
     * 该资源支持的操作逻辑，如果不支持某种操作对应位置的元素为长度为0的engines数组
     * <p>
     * 处理指定http method的逻辑存放于该本数组的指定的、唯一位置，即 {@link ReqMethod#ordinal()}
     * 
     */
    private final LinkedEngine[][] engines;

    private int engineCount;

    private transient String toStringCache;

    private transient List<ReqMethod> allowedMethodsCache;

    //-----------------------------------

    /**
     * @param simpleName 资源相对于上级的资源的名称
     */
    public EngineGroupImpl() {
        LinkedEngine[][] engines = new LinkedEngine[ARRAY_SIZE][];
        Arrays.fill(engines, emptyEngines);
        this.engines = engines;
    }

    /**
     * 添加一个 {@link Engine} ；如果所给的 method 是 {@link ReqMethod#ALL}，则优先级最低。
     * 
     * @param method
     * @param engine
     */
    public void addEngine(ReqMethod method, LinkedEngine engine) {
        for (ReqMethod md : method.parse()) {
            LinkedEngine[] methodEngines = engines[md.ordinal()];
            if (methodEngines.length == 0) {
                methodEngines = new LinkedEngine[] { engine };
            } else {
                methodEngines = Arrays.copyOf(methodEngines, methodEngines.length + 1);
                methodEngines[methodEngines.length - 1] = engine;
            }
            engines[md.ordinal()] = methodEngines;
            engineCount++;
        }
        clearCache();
    }

    @Override
    public int size() {
        return engineCount;
    }

    /**
     * 返回处理这个资源的处理逻辑，如果该资源不支持该操作方法返回长度为0的数组。
     * 
     * @param method 除 {@link ReqMethod#ALL} 外的其他 {@link ReqMethod}
     *        实例；可以为null
     * @return
     */
    @Override
    public LinkedEngine[] getEngines(ReqMethod method) {
        if (method == null) {
            return emptyEngines;
        }
        if (method == ReqMethod.ALL) {
            throw new IllegalArgumentException("method");
        }
        return engines[method.ordinal()];
    }

    /**
     * 本资源是否支持此操作?
     * 
     * @param method
     * @return
     */
    public boolean isMethodAllowed(ReqMethod method) {
        return method != null && engines[method.ordinal()].length > 0;
    }

    public List<ReqMethod> getAllowedMethods() {
        if (allowedMethodsCache == null) {
            List<ReqMethod> allowedMethods = new ArrayList<ReqMethod>();
            for (ReqMethod method : ReqMethod.ALL.parse()) {
                Engine[] methodEngines = this.engines[method.ordinal()];
                if (methodEngines.length > 0) {
                    allowedMethods.add(method);
                }
            }
            allowedMethodsCache = Collections.unmodifiableList(allowedMethods);
        }
        return allowedMethodsCache;
    }

    private boolean destroyed = false;

    /**
     * 销毁该资源
     */
    public void destroy() {
        if (!destroyed) {
            return;
        }
        destroyed = true;
        for (Engine[] methodEngines : engines) {
            for (Engine engine : methodEngines) {
                try {
                    engine.destroy();
                } catch (Throwable e) {
                    logger.error("", e);
                }
            }
        }
    }

    @Override
    public String toString() {
        if (this.toStringCache == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            int oriLen = sb.length();
            for (ReqMethod method : getAllowedMethods()) {
                sb.append(method.toString()).append(", ");
            }
            if (sb.length() > oriLen) {
                sb.setLength(sb.length() - 2);
            }
            sb.append("]");

            this.toStringCache = sb.toString();
        }
        return this.toStringCache;
    }

    private void clearCache() {
        allowedMethodsCache = null;
        toStringCache = null;
    }

}
