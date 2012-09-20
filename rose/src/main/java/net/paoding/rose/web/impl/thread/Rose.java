/*
* Copyright 2007-2009 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package net.paoding.rose.web.impl.thread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.web.Dispatcher;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.InvocationUtils;
import net.paoding.rose.web.RequestPath;
import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.impl.mapping.EngineGroup;
import net.paoding.rose.web.impl.mapping.MappingNode;
import net.paoding.rose.web.impl.mapping.MatchResult;
import net.paoding.rose.web.impl.module.Module;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class Rose implements EngineChain {

    protected static final Log logger = LogFactory.getLog(Rose.class);

    private final List<Module> modules;

    private final MappingNode mappingTree;

    private final RequestPath path;

    private final HttpServletRequest originalHttpRequest;

    private final HttpServletResponse originalHttpResponse;

    private boolean started;

    private List<LinkedEngine> engines = new ArrayList<LinkedEngine>(6);

    private List<MatchResult> matchResults;

    private InvocationBean inv;

    private int curIndexOfChain;

    private final LinkedList<AfterCompletion> afterCompletions = new LinkedList<AfterCompletion>();

    public Rose(List<Module> modules, MappingNode mappingTree, HttpServletRequest httpRequest,
            HttpServletResponse httpResponse, RequestPath requestPath) {
        this.mappingTree = mappingTree;
        this.modules = modules;
        this.originalHttpRequest = httpRequest;
        this.originalHttpResponse = httpResponse;
        this.path = requestPath;
    }

    public MappingNode getMappingTree() {
        return mappingTree;
    }

    public InvocationBean getInvocation() {
        return inv;
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<LinkedEngine> getEngines() {
        return engines;
    }

    /**
     * 启动rose逻辑，对请求进行匹配判断，如果匹配未能成功返回false； <br>
     * 对匹配成功的启动相关的架构处理逻辑直至整个请求的完成
     * 
     * @return
     * @throws Throwable
     */
    public boolean start() throws Throwable {
        if (this.started) {
            throw new IllegalStateException("don't start again");
        }
        this.started = true;
        return innerStart();
    }

    public List<MatchResult> getMatchResults() {
        return matchResults;
    }

    /**
     * @throws IndexOutOfBoundsException
     */
    @Override
    public Object doNext() throws Throwable {
        return engines.get(--curIndexOfChain).execute(this);
    }

    private boolean innerStart() throws Throwable {
        final boolean debugEnabled = logger.isDebugEnabled();
        final List<MatchResult> matchResults = mappingTree.match(this.path);
        if (matchResults == null) {
            // not rose uri
            if (debugEnabled) {
                logger.debug("not rose uri: '" + this.path.getUri() + "'");
            }
            return false;
        }

        final MatchResult lastMatched = matchResults.get(matchResults.size() - 1);
        final EngineGroup leafEngineGroup = lastMatched.getMappingNode().getLeafEngines();
        if (leafEngineGroup.size() == 0) {
            // not rose uri
            if (debugEnabled) {
                logger.debug("not rose uri, not exits leaf engines for it: '" + this.path.getUri()
                        + "'");
            }
            return false;

        }
        final LinkedEngine leafEngine = select(leafEngineGroup.getEngines(path.getMethod()));
        if (leafEngine == null) {
            // 405 Method Not Allowed
            /* 
             * The method specified in the Request-Line is not allowed for the
             * resource identified by the Request-URI. The response MUST include an
             * Allow header containing a list of valid methods for the requested
             * resource.
             */
            StringBuilder allow = new StringBuilder();
            final String gap = ", ";

            for (ReqMethod method : leafEngineGroup.getAllowedMethods()) {
                allow.append(method.toString()).append(gap);
            }
            if (allow.length() > 0) {
                allow.setLength(allow.length() - gap.length());
            }
            originalHttpResponse.addHeader("Allow", allow.toString());
            originalHttpResponse.sendError(405, this.path.getUri());

            // true: don't forward to next filter or servlet
            return true;

        }

        if (debugEnabled) {
            ActionEngine actionEngine = (ActionEngine) leafEngine.getTarget();
            logger.debug("mapped '" + path.getUri() + "' to "
                    + actionEngine.getControllerClass().getName() + "#"
                    + actionEngine.getMethod().getName());
        }

        // bind engines
        LinkedEngine tempEngine = leafEngine;
        MappingNode moduleNode = null;
        MappingNode controllerNode = null;
        while (tempEngine != null) {
            engines.add(tempEngine);
            Class<? extends Engine> target = tempEngine.getTarget().getClass();
            if (target == ModuleEngine.class) {
                moduleNode = tempEngine.getNode();
            } else if (target == ControllerEngine.class) {
                controllerNode = tempEngine.getNode();
            }
            tempEngine = tempEngine.getParent();
        }
        // set module/controller/action path to RequsetPath object
        StringBuilder sb = new StringBuilder(path.getUri().length());
        MatchResult moduleMatchResult = null;
        MatchResult controllerMatchResult = null;
        for (int i = 0; i < matchResults.size(); i++) {
            MatchResult matchResult = matchResults.get(i);
            sb.append(matchResult.getValue());
            if (matchResult.getMappingNode() == moduleNode) {
                moduleMatchResult = matchResult;
                path.setModulePath(sb.toString()); // modulePath
                Assert.notNull(engines.get(2));
                sb.setLength(0);
            }
            if (matchResult.getMappingNode() == controllerNode) {
                controllerMatchResult = matchResult;
                path.setControllerPath(sb.toString()); // controllerPath
                Assert.notNull(engines.get(1));
                sb.setLength(0);
            }
        }
        path.setActionPath(sb.toString()); // actionPath
        Assert.notNull(moduleMatchResult);
        Assert.notNull(controllerMatchResult);
        this.matchResults = matchResults;

        this.curIndexOfChain = engines.size();

        Map<String, String> uriParameters = null;
        for (int i = matchResults.size() - 1; i >= 0; i--) {
            MatchResult matchResult = matchResults.get(i);
            String name = matchResult.getParameterName();
            if (name != null) {
                if (uriParameters == null) {
                    uriParameters = new HashMap<String, String>(matchResults.size() << 1);
                }
                uriParameters.put(name, matchResult.getValue());
            }
        }

        HttpServletRequest httpRequest = originalHttpRequest;
        if (uriParameters != null && uriParameters.size() > 0) {
            httpRequest = new ParameteredUriRequest(originalHttpRequest, uriParameters);
        }

        // originalThreadRequest可能为null，特别是在portal框架下
        HttpServletRequest originalThreadRequest = InvocationUtils.getCurrentThreadRequest();
        //
        Invocation preInvocation = null;
        if (path.getDispatcher() != Dispatcher.REQUEST) {
            preInvocation = InvocationUtils.getInvocation(originalHttpRequest);
        }
        // invocation 对象 代表一次Rose调用
        InvocationBean inv = new InvocationBean(httpRequest, originalHttpResponse, path);
        inv.setRose(this);
        inv.setPreInvocation(preInvocation);
        //
        InvocationUtils.bindRequestToCurrentThread(httpRequest);
        InvocationUtils.bindInvocationToRequest(inv, httpRequest);

        // invoke the engine chain
        this.inv = inv;
        Throwable error = null;
        try {
            Object instuction = ((EngineChain) this).doNext();
            if (":continue".equals(instuction)) {
                return false;
            }
        } catch (Throwable local) {
            error = local;
            throw local;
        } finally {
            for (AfterCompletion task : afterCompletions) {
                try {
                    task.afterCompletion(inv, error);
                } catch (Throwable e) {
                    logger.error("", e);
                }
            }
            if (originalThreadRequest != null) {
                InvocationUtils.bindRequestToCurrentThread(originalThreadRequest);
            } else {
                InvocationUtils.unindRequestFromCurrentThread();
            }
            // 更新绑定的invocation，只对于那些forward后request.setAttibute影响了前者的有效。(include的不用处理了，已经做了snapshot了)
            if (preInvocation != null) {
                InvocationUtils.bindInvocationToRequest(preInvocation, httpRequest);
            }
        }

        return true;
    }

    private LinkedEngine select(LinkedEngine[] engines) {
        LinkedEngine selectedEngine = null;
        int score = 0;

        for (LinkedEngine engine : engines) {
            int candidate = engine.isAccepted(this.originalHttpRequest);
            if (logger.isDebugEnabled()) {
                logger.debug("Score of " + engine.getClass().getName() + ":" + candidate);
            }
            if (candidate > score) {
                selectedEngine = engine;
                score = candidate;
            }
        }
        if (logger.isDebugEnabled()) {

            if (selectedEngine == null) {
                logger.debug("No engine selected.");
            } else {
                String msg;
                if (selectedEngine.getTarget() instanceof ActionEngine) {
                    ActionEngine actionEngine = (ActionEngine) selectedEngine.getTarget();
                    msg = actionEngine.getController().getClass().getName() + "#"
                            + actionEngine.getMethod().getName();
                } else {
                    msg = selectedEngine.toString();
                }
                logger.debug("Engine selected:" + msg);
            }
        }
        return selectedEngine;
    }

    @Override
    public void addAfterCompletion(AfterCompletion task) {
        afterCompletions.addFirst(task);
    }
}
