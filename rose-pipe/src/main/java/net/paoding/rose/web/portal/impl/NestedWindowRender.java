/*
 * Copyright 2007-2012 the original author or authors.
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
package net.paoding.rose.web.portal.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import net.paoding.rose.web.portal.Window;
import net.paoding.rose.web.portal.WindowRender;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author qieqie
 * 
 */
public final class NestedWindowRender implements WindowRender {

    private static Log logger = LogFactory.getLog(NestedWindowRender.class);

    private static WindowRender simpleRender = new SimpleWindowRender();

    private WindowRender innerRender;

    public NestedWindowRender(WindowRender innerRender) {
        setInnerRender(innerRender);
    }

    public NestedWindowRender() {
    }

    public void setInnerRender(WindowRender actualRender) {
        this.innerRender = actualRender;
    }

    public WindowRender getInnerRender() {
        return innerRender;
    }

    @Override
    public void render(Writer out, Window w) throws IOException {
        WindowRender render = this.innerRender;
        if (render == null) {
            render = simpleRender;
        }
        if (w instanceof WindowForView) {
            w = ((WindowForView) w).getInner();
        }
        WindowImpl window = (WindowImpl) w;
        if (window.getContentLength() >= 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("rendering window: " + window.getPath() + "; contentLength="
                        + window.getContentLength());
            }
            render.render(out, window);
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("rendering a unsuccess window: " + window.getPath() + "; contentLength="
                    + window.getContentLength() + "; sc=" + window.getStatusCode());
        }
        if (window.getThrowable() != null) {
            writeExceptionAsContent(out, window);
        } else if (window.getStatusCode() < 200 || window.getStatusCode() >= 300) {
            out.write(window.getPath());
            out.write("<br>sc=");
            out.write(String.valueOf(window.getStatusCode()));
            if (window.getStatusMessage() != null) {
                out.write(" ");
                out.write(window.getStatusMessage());
            }
        }
    }

    private void writeExceptionAsContent(Writer out, WindowImpl window) throws IOException {
        out.write(window.getPath());
        out.write("<br>");
        out.write(String.valueOf(window.getStatusCode()));
        if (window.getStatusMessage() != null) {
            out.write(" ");
            out.write(window.getStatusMessage());
        }
        out.write("<br>");
        out.write("<pre>");
        Throwable ex = window.getThrowable();
        PrintWriter printWriter = new PrintWriter(out);
        ex.printStackTrace(printWriter);
        out.write("</pre>");
    }

}
