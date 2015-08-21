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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;

import net.paoding.rose.util.SpringUtils;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.impl.thread.InvocationBean;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

/**
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class InstructionExecutorImpl implements InstructionExecutor {

    private Log logger = LogFactory.getLog(getClass());

    @Override
    public Object render(Invocation inv, Object instruction) throws IOException, ServletException,
            Exception {
        instruction = translatesToInstructionObject((InvocationBean) inv, instruction);
        if (instruction != null && !Thread.currentThread().isInterrupted()) {
            ((Instruction) instruction).render(inv);
        }
        return instruction;
    }

    /**
     * @param inv
     * @param instruction
     * @return
     * @throws StackOverflowError
     */
    private Instruction translatesToInstructionObject(InvocationBean inv, Object instruction)
            throws StackOverflowError {
        int count = 0;
        while (!(instruction instanceof Instruction)) {
            if (count++ > 50) {
                throw new StackOverflowError("Unable to parse the instruction to an"
                        + " Instruction object less than " + count + " times. Is the instruction"
                        + " that returned by your controller" + " action is right?");
            }

            if (Thread.interrupted() || instruction == null) {
                return null;
            } else {
                if (instruction.getClass() != String.class
                        && !ClassUtils.isPrimitiveOrWrapper(instruction.getClass())
                        && instruction.getClass().getComponentType() == null
                        && instruction.getClass().getAnnotation(Component.class) != null) {
                    SpringUtils.autowire(instruction, inv.getApplicationContext());
                }
                instruction = parseInstruction(inv, instruction);
            }
        }
        return (Instruction) instruction;
    }

    protected Object parseInstruction(Invocation inv, Object ins) {
        if (logger.isDebugEnabled()) {
            logger.debug("parset instruction:" + ins.getClass().getName() + ": '" + ins + "'");
        }
        if (ClassUtils.isPrimitiveOrWrapper(ins.getClass())) {
            return Text.text(ins);
        } else if (ins instanceof CharSequence) {
            String str = ins.toString();
            if (str.length() == 0 || str.equals("@")) {
                return null;
            }
            if (str.charAt(0) == '@') {
                return Text.text(str.substring(1)); // 具体content-type等信息由@HttpFeatures决定
            }
            if (str.charAt(0) == '/') {
                return new ViewInstruction(str);
            }
            if (str.startsWith("r:") || str.startsWith("redirect:")) {
                StringInstruction si = new StringInstruction(false, str
                        .substring(str.indexOf(':') + 1));
                if (si.innerInstruction.startsWith("http://")
                        || si.innerInstruction.startsWith("https://")) {
                    return Redirect.location(si.innerInstruction);
                }
                return si;
            }
            if (str.startsWith("pr:") || str.startsWith("perm-redirect:")) {
                StringInstruction si = new StringInstruction(false, str
                        .substring(str.indexOf(':') + 1));
                si.permanentlyWhenRedirect = true;
                if (si.innerInstruction.startsWith("http://")
                        || si.innerInstruction.startsWith("https://")) {
                    return si.permanentlyIfNecessary(Redirect.location(si.innerInstruction));
                }
                return si;
            }
            if (str.startsWith("f:") || str.startsWith("forward:")) {
                return new StringInstruction(true, str.substring(str.indexOf(':') + 1));
            }
            if (str.startsWith("e:") || str.startsWith("error:")) {
                int begin = str.indexOf(':') + 1;
                int codeEnd = str.indexOf(';');
                if (codeEnd == -1) {
                    String text = str.substring(begin);
                    if (text.length() > 0 && NumberUtils.isNumber(text)) {
                        return HttpError.code(Integer.parseInt(text));
                    }
                    return HttpError.code(500, text);
                } else {
                    return HttpError.code(Integer.parseInt(str.substring(begin, codeEnd)), str
                            .substring(codeEnd + 1).trim());
                }
            }
            if (str.startsWith("s:") || str.startsWith("status:")) {
                int begin = str.indexOf(':') + 1;
                int codeEnd = str.indexOf(';', begin);
                if (codeEnd == -1) {
                    inv.getResponse().setStatus(Integer.parseInt(str.substring(begin)));
                    return null;
                } else {
                    // setStatus(int, String)是不推荐的，所以';'后的我们不认为是msg
                    // 这和sendError不一样
                    inv.getResponse().setStatus(Integer.parseInt(str.substring(begin, codeEnd)));
                    for (int i = codeEnd; i < str.length(); i++) {
                        if (str.charAt(i) != ' ') {
                            str = str.substring(i + 1);
                            break;
                        }
                    }
                }
            }
            if (str.equals(":continue")) {
                return null;
            }
            return new StringInstruction(null, str);
        } else if (ins.getClass() == StringInstruction.class) {
            StringInstruction fr = (StringInstruction) ins;
            String str = fr.innerInstruction;
            int queryIndex = str.indexOf('?');
            for (int i = (queryIndex == -1) ? str.length() - 1 : queryIndex - 1; i >= 0; i--) {
                if (str.charAt(i) != ':') {
                    continue;
                }
                if (i > 0 && str.charAt(i - 1) == '\\') { // 转义符号
                    str = str.substring(0, i - 1) + str.substring(i);
                    i--;
                    continue;
                }
                int cmdEnd = i;
                int cmdBeforeBegin = i - 1;
                while (cmdBeforeBegin >= 0 && str.charAt(cmdBeforeBegin) != ':') {
                    cmdBeforeBegin--;
                }
                String prefix = str.subSequence(cmdBeforeBegin + 1, cmdEnd).toString();
                String body = str.subSequence(i + 1, str.length()).toString();
                if ("a".equals(prefix) || "action".equals(prefix)) {
                    if (fr.isReirect()) {
                        return fr.permanentlyIfNecessary(Redirect.action(body));
                    } else {
                        return Forward.action(body);
                    }
                }
                if ("c".equals(prefix) || "controller".equals(prefix)) {
                    if (fr.isReirect()) {
                        return fr.permanentlyIfNecessary(Redirect.controller(body));
                    } else {
                        return Forward.controller(body);
                    }
                }
                if ("m".equals(prefix) || "module".equals(prefix)) {
                    if (fr.isReirect()) {
                        return fr.permanentlyIfNecessary(Redirect.module(body));
                    } else {
                        return Forward.module(body);
                    }
                }
                logger.warn("skip the prefix '" + prefix + ":' of " + str);
                if (fr.isReirect()) {
                    return fr.permanentlyIfNecessary(Redirect.location(str));
                } else if (fr.isForward()) {
                    return Forward.path(str);
                } else {
                    return new ViewInstruction(str);
                }
            }
            if (fr.isReirect()) {
                return fr.permanentlyIfNecessary(Redirect.location(str));
            } else if (fr.isForward()) {
                return Forward.path(str);
            }
            return new ViewInstruction(str);
        } else if (ins instanceof InputStream) {
            return new InputStreamInstruction((InputStream) ins);
        } else if (ins instanceof byte[]) {
            return new InputStreamInstruction(new ByteArrayInputStream((byte[]) ins));
        } else {
            return Text.text(ins.toString());
        }
    }

    private class StringInstruction {

        Boolean forward;

        String innerInstruction;

        boolean permanentlyWhenRedirect = false;

        public StringInstruction(Boolean forward, String innerInstruction) {
            super();
            this.forward = forward;
            this.innerInstruction = innerInstruction;
        }

        public RedirectInstruction permanentlyIfNecessary(RedirectInstruction redirectInstruction) {
            if (permanentlyWhenRedirect) {
                redirectInstruction.permanently();
            }
            return redirectInstruction;
        }

        public boolean isReirect() {
            return forward != null && !forward.booleanValue();
        }

        public boolean isForward() {
            return forward != null && forward.booleanValue();
        }

        @Override
        public String toString() {
            return forward == null ? "" : (forward ? "f:" : "r:") + innerInstruction;
        }
    }

}
