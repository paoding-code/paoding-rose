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
package net.paoding.rose.web.instruction;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.RequestPath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class RedirectInstruction extends AbstractInstruction {

    protected static Log logger = LogFactory.getLog(RedirectInstruction.class);

    @Override
    public void doRender(Invocation inv) throws IOException {
        String location = resolvePlaceHolder(location(), inv);
        if (sc == null || sc == 302) {
            inv.getResponse().sendRedirect(location);
        } else {
            Assert.isTrue(sc == HttpServletResponse.SC_MOVED_PERMANENTLY);
            inv.getResponse().setStatus(sc);
            inv.getResponse().setHeader("Location", location);
        }
    }

    // ----------------------------------------

    private String location;

    private Integer sc;

    /**
     * 设置301永久跳转
     * 
     * @return
     */
    public RedirectInstruction permanently() {
        this.sc = HttpServletResponse.SC_MOVED_PERMANENTLY;
        return this;
    }

    public RedirectInstruction module(final String module) {
        this.preInstruction = new Instruction() {

            @Override
            public void render(Invocation inv) throws IOException, ServletException, Exception {
                String ctxpath = inv.getRequestPath().getCtxpath();
                if (module.length() == 0) {
                    if (ctxpath.length() == 0) {
                        location("/");
                    } else {
                        location(ctxpath);
                    }
                } else if (module.charAt(0) != '/') {
                    location(ctxpath + "/" + module);
                } else {
                    location(ctxpath + module);
                }
            }
        };
        return this;
    }

    public RedirectInstruction controller(final String controller) {
        this.preInstruction = new Instruction() {

            @Override
            public void render(Invocation inv) throws IOException, ServletException, Exception {
                String controllerPath = controller;
                if (controller.length() > 0 && controller.charAt(0) != '/') {
                    controllerPath = "/" + controller;
                }
                RequestPath requestPath = inv.getRequestPath();
                location(requestPath.getCtxpath() + requestPath.getModulePath() + controllerPath);
            }
        };
        return this;
    }

    public RedirectInstruction action(final String action) {
        this.preInstruction = new Instruction() {

            @Override
            public void render(Invocation inv) throws IOException, ServletException, Exception {
                String actionPath = action;
                if (action.length() > 0 && action.charAt(0) != '/') {
                    actionPath = "/" + action;
                }
                RequestPath requestPath = inv.getRequestPath();
                location(requestPath.getCtxpath() + requestPath.getModulePath()
                        + requestPath.getControllerPath() + actionPath);
            }
        };
        return this;
    }

    public String location() {
        return location;
    }

    public RedirectInstruction location(String location) {
        this.location = location;
        return this;
    }
}
