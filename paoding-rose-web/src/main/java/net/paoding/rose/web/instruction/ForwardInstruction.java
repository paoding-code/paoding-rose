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

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.RequestPath;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class ForwardInstruction extends AbstractInstruction {

    @Override
    public void doRender(Invocation inv) throws ServletException, IOException {
        String path = resolvePlaceHolder(this.path, inv);
        inv.getRequest().getRequestDispatcher(path).forward(inv.getRequest(), inv.getResponse());
    }

    // -----------------------------

    private String path;

    public ForwardInstruction module(final String module) {
        this.preInstruction = new Instruction() {

            @Override
            public void render(Invocation inv) throws IOException, ServletException, Exception {
                String modulePath = module;
                if (module.length() > 0 && module.charAt(0) != '/') {
                    modulePath = "/" + module;
                }
                path(modulePath);
            }
        };
        return this;
    }

    public ForwardInstruction controller(final String controller) {
        this.preInstruction = new Instruction() {

            @Override
            public void render(Invocation inv) throws IOException, ServletException, Exception {
                String controllerPath = controller;
                if (controller.length() > 0 && controller.charAt(0) != '/') {
                    controllerPath = "/" + controller;
                }
                path(inv.getRequestPath().getModulePath() + controllerPath);
            }
        };
        return this;
    }

    public ForwardInstruction action(final String action) {
        this.preInstruction = new Instruction() {

            @Override
            public void render(Invocation inv) throws IOException, ServletException, Exception {
                String actionPath = action;
                if (action.length() > 0 && action.charAt(0) != '/') {
                    actionPath = "/" + action;
                }
                RequestPath requestPath = inv.getRequestPath();
                path(requestPath.getModulePath() + requestPath.getControllerPath() + actionPath);
            }
        };
        return this;
    }

    public ForwardInstruction path(String path) {
        this.path = path;
        return this;
    }

    public String path() {
        return path;
    }

}
