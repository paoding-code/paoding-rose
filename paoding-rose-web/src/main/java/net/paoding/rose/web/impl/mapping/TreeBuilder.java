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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.paoding.rose.util.PrinteHelper;
import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.impl.module.ControllerRef;
import net.paoding.rose.web.impl.module.MethodRef;
import net.paoding.rose.web.impl.module.Module;
import net.paoding.rose.web.impl.thread.ActionEngine;
import net.paoding.rose.web.impl.thread.ControllerEngine;
import net.paoding.rose.web.impl.thread.Engine;
import net.paoding.rose.web.impl.thread.LinkedEngine;
import net.paoding.rose.web.impl.thread.ModuleEngine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class TreeBuilder {

    protected static final Log logger = LogFactory.getLog(TreeBuilder.class);

    /*
     * 构造一个树，树的结点是地址-资源映射，每个结点都能回答是否匹配一个字符串，每个匹配的节点都知道如何执行对该资源的操作.
     * 构造树的过程：
     *   识别组件==>求得他的资源定义==>判断是否已经创建了==>未创建的创建一个树结点==>已创建的找出这个结点
     *   ==>在这个资源增加相应的操作以及逻辑==>若是新建的结点把它加到树中，同时满足遍历、匹配顺序要求
     */
    public void create(MappingNode tree, List<Module> modules) {
        addRoot(tree, modules);
        check(tree, tree, "");
    }

    private void addRoot(MappingNode rootNode, List<Module> modules) {
        for (Module module : modules) {
            addModule(rootNode, module);
        }
    }

    private void addModule(final MappingNode rootNode, Module module) {
        List<Mapping> terms = MappingFactory.parse(module.getMappingPath());
        LinkedEngine rootEngine = rootNode.getMiddleEngines().getEngines(ReqMethod.GET)[0];

        MappingNode parent = rootNode;
        for (Mapping mapping : terms) {
            if (mapping.getDefinition().length() == 0) {
                continue;
            }
            MappingNode temp = parent.getChild(mapping.getDefinition());
            if (temp == null) {
                temp = new MappingNode(mapping);
                parent.linkAsChild(temp);
            }
            parent = temp;
        }
        LinkedEngine moduleEngine = new LinkedEngine(rootEngine, new ModuleEngine(module), parent);
        parent.getMiddleEngines().addEngine(ReqMethod.ALL, moduleEngine);

        // controllers
        List<ControllerRef> controllers = module.getControllers();
        for (ControllerRef controller : controllers) {
            addController(module, parent, moduleEngine, controller);
        }
    }

    private void addController(Module module, MappingNode moduleNode, LinkedEngine moduleEngine,
            ControllerRef controller) {
        //
        Engine engine = new ControllerEngine(module, controller);

        Set<String> mappingPaths = new HashSet<String>(Arrays.asList(controller.getMappingPaths()));
        for (String mappingPath : mappingPaths) {
            List<Mapping> mappings = MappingFactory.parse(mappingPath);
            //
            MappingNode target = moduleNode;
            for (Mapping mapping : mappings) {
                if (mapping.getDefinition().length() == 0) {
                    continue;
                }
                MappingNode temp = target.getChild(mapping.getDefinition());
                if (temp == null) {
                    temp = new MappingNode(mapping);
                    target.linkAsChild(temp);
                }
                target = temp;
            }

            LinkedEngine controllerEngine = new LinkedEngine(moduleEngine, engine, target);
            target.getMiddleEngines().addEngine(ReqMethod.ALL, controllerEngine);
            //

            // actions
            MethodRef[] actions = controller.getActions();
            for (MethodRef action : actions) {
                addAction(module, controller, action, target, controllerEngine);
            }
        }
    }

    private void addAction(Module module, ControllerRef controller, MethodRef action,
            MappingNode controllerNode, LinkedEngine controllerEngine) {
        Map<String, Set<ReqMethod>> mappingPaths = action.getMappings();
        if (mappingPaths.size() == 0) {
            return;
        }

        Engine actionEngine = new ActionEngine(module, controller.getControllerClass(),//
                controller.getControllerObject(), action.getMethod());

        for (String mappingPath : mappingPaths.keySet()) {
            List<Mapping> mappings = MappingFactory.parse(mappingPath);
            MappingNode target = controllerNode;
            for (Mapping mapping : mappings) {
                if (mapping.getDefinition().length() == 0) {
                    continue;
                }
                MappingNode temp = target.getChild(mapping.getDefinition());
                if (temp == null) {
                    temp = new MappingNode(mapping);
                    target.linkAsChild(temp);
                }
                target = temp;
            }
            //
            for (ReqMethod method : mappingPaths.get(mappingPath)) {
                LinkedEngine linkedActionEngine = new LinkedEngine(//
                        controllerEngine, actionEngine, target);
                target.getLeafEngines().addEngine(method, linkedActionEngine);
            }
        }
    }

    /**
     * 检查整个树的状况，尽可能报告可能存在的问题
     * 
     * @param tree
     * @param parent
     * @param prefix
     */
    private void check(MappingNode tree, MappingNode parent, String prefix) {
        MappingNode child = parent.getLeftMostChild();
        MappingNode sibling = null;
        while (child != null) {
            if (sibling != null) {
                if (child.compareTo(sibling) == 0) {
                    logger.error("mapping conflicts: '" + child.getMapping().getDefinition()
                            + "' conflicts with '" + sibling.getMapping().getDefinition()
                            + "' in '" + prefix + "'; here is the mapping tree, "
                            + "you can find the conflict in it:\n" + PrinteHelper.list(tree));
                    throw new IllegalArgumentException("mapping conflicts: '"
                            + child.getMapping().getDefinition() + "' conflicts with '"
                            + sibling.getMapping().getDefinition() + "' in '" + prefix + "'");
                }
            }
            check(tree, child, prefix + child.getMapping().getDefinition());
            sibling = child;
            child = child.getSibling();
        }

    }
}
