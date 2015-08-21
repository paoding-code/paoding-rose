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

import net.paoding.rose.web.Invocation;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author Li Weibo[weibo.leo@gmail.com]
 */
public class HttpErrorInstruction extends AbstractInstruction {

    @Override
    public void doRender(Invocation inv) throws Exception {
        String message = resolvePlaceHolder(this.message, inv);
        message = StringEscapeUtils.escapeHtml(message);	//输出到页面之前对HTML转义，防止XSS注入
        if (StringUtils.isEmpty(message)) {
            inv.getResponse().sendError(code);
        } else {
            inv.getResponse().sendError(code, message);
        }
    }

    // --------------------------------------------

    private int code;

    private String message;

    public HttpErrorInstruction() {
    }

    public HttpErrorInstruction(int code) {
        this.code = code;
    }

    public HttpErrorInstruction(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
