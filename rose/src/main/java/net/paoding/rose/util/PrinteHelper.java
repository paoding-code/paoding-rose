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
package net.paoding.rose.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import net.paoding.rose.RoseFilter;
import net.paoding.rose.web.InterceptorDelegate;
import net.paoding.rose.web.ParamValidator;
import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.impl.mapping.EngineGroup;
import net.paoding.rose.web.impl.mapping.MappingNode;
import net.paoding.rose.web.impl.module.ControllerRef;
import net.paoding.rose.web.impl.module.Module;
import net.paoding.rose.web.impl.thread.ActionEngine;
import net.paoding.rose.web.impl.thread.LinkedEngine;
import net.paoding.rose.web.paramresolver.ParamResolver;

/**
 * 
 * @see RoseFilter
 * 
 */
public class PrinteHelper {

    // 后续可以提取出来放到什么地方，是不是采用模板语言来定义?
    public static String dumpModules(List<Module> modules) {
        final StringBuilder sb = new StringBuilder(4028);
        sb.append("\n--------Modules(Total ").append(modules.size()).append(")--------");
        sb.append("\n");
        for (int i = 0; i < modules.size(); i++) {
            final Module module = modules.get(i);
            sb.append("module ").append(i + 1).append(":");
            sb.append("\n\tmappingPath='").append(module.getMappingPath());
            sb.append("';\n\tpackageRelativePath='").append(module.getRelativePath());
            sb.append("';\n\turl='").append(module.getUrl());
            sb.append("';\n\tcontrollers=[");
            final List<ControllerRef> controllerMappings = module.getControllers();

            for (final ControllerRef controller : controllerMappings) {
                sb.append("'").append(Arrays.toString(controller.getMappingPaths())).append("'=")
                        .append(controller.getControllerClass().getSimpleName()).append(", ");
            }
            if (!controllerMappings.isEmpty()) {
                sb.setLength(sb.length() - 2);
            }
            sb.append("];\n\tparamResolvers=[");
            for (ParamResolver resolver : module.getCustomerResolvers()) {
                sb.append(resolver.getClass().getSimpleName()).append(", ");
            }
            if (module.getCustomerResolvers().size() > 0) {
                sb.setLength(sb.length() - 2);
            }
            sb.append("];\n\tvalidators=[");
            for (ParamValidator validator : module.getValidators()) {
                sb.append(validator.getClass().getSimpleName()).append(", ");
            }
            if (module.getValidators().size() > 0) {
                sb.setLength(sb.length() - 2);
            }
            sb.append("];\n\tinterceptors=[");
            for (InterceptorDelegate interceptor : module.getInterceptors()) {
                sb.append(interceptor.getName()).append("(").append(interceptor.getPriority())
                        .append("), ");
            }
            if (module.getInterceptors().size() > 0) {
                sb.setLength(sb.length() - 2);
            }
            sb.append("];\n\terrorHander=").append(
                    module.getErrorHandler() == null ? "<null>" : module.getErrorHandler());
            sb.append("\n\n");
        }
        sb.append("--------end--------");
        return sb.toString();
    }

    public static String list(MappingNode root) {
        StringBuilder sb = new StringBuilder(2048 >> 2);
        println(root, "", -1, sb);
        return sb.toString();
    }

    private static void println(final MappingNode node, final String prefix, final int deep,
            StringBuilder sb) {
        final String gap = "    ";
        String tab = "";
        for (int i = 0; i < deep; i++) {
            tab += gap;
        }
        String path = tab + prefix + node.getMappingPath();
        if (path.length() == 0) {
            path = "ROOT";
        }
        sb.append(path).append("\n");
        //
        tab += gap;
        EngineGroup leaf = node.getLeafEngines();
        if (leaf.size() > 0) {
            for (ReqMethod method : leaf.getAllowedMethods()) {
                for (LinkedEngine engine : leaf.getEngines(method)) {
                    ActionEngine action = (ActionEngine) engine.getTarget();
                    Method m = action.getMethod();
                    Class<?> cc = action.getControllerClass();
                    sb.append(tab);
                    sb.append(method + "=\"" + cc.getSimpleName() + "#" + m.getName() + "\" ");
                    sb.append(//
                            "package=\"" + m.getDeclaringClass().getPackage().getName() + "\"\n");
                }
            }
        }
        //
        MappingNode child = node.getLeftMostChild();
        while (child != null) {
            println(child, prefix + node.getMappingPath(), deep + 1, sb);
            child = child.getSibling();
        }
    }
}
