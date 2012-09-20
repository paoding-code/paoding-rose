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
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.web.Invocation;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class TextInstruction extends AbstractInstruction {

    protected static Log logger = LogFactory.getLog(TextInstruction.class);

    //-------------------------------------------

    private String text;

    public String text() {
        return text;
    }

    public TextInstruction text(String text) {
        this.text = text;
        return this;
    }

    //-------------------------------------------

    @Override
    public void doRender(Invocation inv) throws Exception {
        if (StringUtils.isEmpty(text)) {
            return;
        }
        
        if (logger.isDebugEnabled()) {
        	logger.debug("trying to render text:" + text);
        }
        
        HttpServletResponse response = inv.getResponse();
        String oldEncoding = response.getCharacterEncoding();
        if (StringUtils.isBlank(oldEncoding) || oldEncoding.startsWith("ISO-")) {
            String encoding = inv.getRequest().getCharacterEncoding();
            Assert.isTrue(encoding != null);
            response.setCharacterEncoding(encoding);
            if (logger.isDebugEnabled()) {
                logger.debug("set response.characterEncoding by default:"
                        + response.getCharacterEncoding());
            }
        }
        
        if (response.getContentType() == null) {
            response.setContentType("text/html");
            if (logger.isDebugEnabled()) {
                logger.debug("set response content-type by default: "
                        + response.getContentType());
            }
        }
        sendResponse(response, text);
    }

    private void sendResponse(HttpServletResponse response, String text) throws IOException {
        if (StringUtils.isNotEmpty(text)) {
            PrintWriter out = response.getWriter();
            if (logger.isDebugEnabled()) {
            	logger.debug("write text to response:" + text);
            }
            out.print(text);
        }
    }

    @Override
    public String toString() {
        return text;
    }
}
