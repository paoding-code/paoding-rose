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
package net.paoding.rose.web.portal.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponseWrapper;

import net.paoding.rose.web.portal.Window;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
class WindowResponse extends HttpServletResponseWrapper {

    private WindowImpl window;

    private PrintWriter writer;

    private ServletOutputStream out;

    private Locale locale;

    private String charset;

    public WindowResponse(WindowImpl window) {
        super(window.getContainer().getResponse());
        this.window = window;
    }

    public Window getWindow() {
        return window;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (out == null) {
            this.out = new ServletOutputStream() {

                ByteArrayOutputStream baos = new ByteArrayOutputStream(getBufferSize());

                @Override
                public void write(int b) throws IOException {
                    baos.write(b);
                }

                @Override
                public void flush() throws IOException {
                    byte[] bytes = baos.toByteArray();
                    baos.reset();
                    window.appendContent(new String(bytes, getCharacterEncoding()));
                }

            };
        }
        return out;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (this.writer == null) {
            this.writer = new PrintWriter(new Writer() {

                @Override
                public void close() throws IOException {

                }

                @Override
                public void flush() throws IOException {
                }

                @Override
                public void write(char[] cbuf, int offset, int len) throws IOException {
                    WindowResponse.this.window.appendContent(cbuf, offset, len);
                }
            });
        }
        return writer;
    }

    @Override
    public void flushBuffer() throws IOException {
    }

    @Override
    public void setHeader(String name, String value) {
        synchronized (window.getContainer()) {
            super.setHeader(name, value);
        }
    }

    @Override
    public void addHeader(String name, String value) {
        synchronized (window.getContainer()) {
            super.addHeader(name, value);
        }
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public Locale getLocale() {
        return locale != null ? locale : super.getLocale();
    }

    @Override
    public void sendError(int sc) throws IOException {
        window.setStatus(sc);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        window.setStatus(sc, msg);
    }

    @Override
    public void setStatus(int sc) {
        window.setStatus(sc);
    }

    @Override
    public void setStatus(int sc, String sm) {
        window.setStatus(sc, sm);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        throw new UnsupportedOperationException("don't call sendRedirect in window request:"
                + location);
    }

    @Override
    public void setBufferSize(int size) {

    }

    @Override
    public void resetBuffer() {
    }

    @Override
    public void setCharacterEncoding(String charset) {
        this.charset = charset;
    }

    @Override
    public String getCharacterEncoding() {
        return charset != null ? charset : super.getCharacterEncoding();
    }

    @Override
    public void setContentType(String type) {
    }

    @Override
    public void setContentLength(int len) {
    }

}
