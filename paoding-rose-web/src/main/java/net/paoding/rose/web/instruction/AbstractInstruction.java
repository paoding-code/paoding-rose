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

import net.paoding.rose.util.PlaceHolderUtils;
import net.paoding.rose.web.Invocation;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public abstract class AbstractInstruction implements Instruction {

    protected Instruction preInstruction;

    protected String resolvePlaceHolder(String text, Invocation inv) {
        return PlaceHolderUtils.resolve(text, inv);
    }

    @Override
    public final void render(Invocation inv) throws IOException, ServletException, Exception {
        preRender(inv);
        if (preInstruction != null) {
            preInstruction.render(inv);
        }
        doRender(inv);
    }

    protected void preRender(Invocation inv) throws IOException, ServletException, Exception {
    }

    protected abstract void doRender(Invocation inv) throws IOException, ServletException,
            Exception;

}
