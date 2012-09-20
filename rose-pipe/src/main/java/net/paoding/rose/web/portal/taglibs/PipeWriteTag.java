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
package net.paoding.rose.web.portal.taglibs;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.InvocationUtils;
import net.paoding.rose.web.portal.PortalUtils;
import net.paoding.rose.web.portal.impl.PipeImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */

public class PipeWriteTag extends TagSupport {

    private static final long serialVersionUID = -6302408795675569266L;

    private static Log logger = LogFactory.getLog(PipeWriteTag.class);

    @Override
    public int doStartTag() throws JspException {
        Invocation inv = InvocationUtils.getCurrentThreadInvocation();
        if (inv == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("it is not in a rose request: '"
                        + ((HttpServletRequest) pageContext.getRequest()).getRequestURI() + "'");
            }
            return SKIP_BODY;
        }
        PipeImpl pipe = (PipeImpl) PortalUtils.getPipe(inv);
        if (pipe == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("there is not pipe for this jsp: '"
                        + ((HttpServletRequest) pageContext.getRequest()).getRequestURI() + "'");
            }
            return SKIP_BODY;
        }
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("writing " + pipe + "...");
            }
            pipe.write(pageContext.getOut());

            if (logger.isDebugEnabled()) {
                logger.debug("writing " + pipe + "... done");
            }
        } catch (IOException e) {
            throw new JspException(e);
        }
        return SKIP_BODY;
    }
}
