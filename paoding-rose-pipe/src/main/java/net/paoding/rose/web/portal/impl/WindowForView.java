/*
 * Copyright 2007-2011 the original author or authors.
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
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.Future;

import net.paoding.rose.web.portal.Portal;
import net.paoding.rose.web.portal.Window;

/**
 * 
 * @author qieqie.wang@gmail.com
 * 
 */
public class WindowForView implements Window {

    private WindowImpl inner;

    public WindowForView(WindowImpl window) {
        this.inner = window;
    }

    @Override
    public String toString() {
        StringWriter sw = new StringWriter(getContentLength() >= 0 ? getContentLength() : 16);
        PrintWriter out = new PrintWriter(sw);
        try {
            render(out);
        } catch (IOException e) {
            e.printStackTrace(out);
        }
        out.flush();
        return sw.getBuffer().toString();
    }

    @Override
    public void render(Writer out) throws IOException {
        this.inner.render(out);
    }

    @Override
    public void clearContent() {
        inner.clearContent();
    }

    @Override
    public Object get(String key) {
        return inner.get(key);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return inner.getAttributes();
    }

    @Override
    public GenericWindowContainer getContainer() {
        return inner.getContainer();
    }

    @Override
    public String getContent() {
        return inner.getContent();
    }

    @Override
    public int getContentLength() {
        return inner.getContentLength();
    }

    @Override
    public Future<?> getFuture() {
        return inner.getFuture();
    }

    @Override
    public String getName() {
        return inner.getName();
    }

    @Override
    public String getPath() {
        return inner.getPath();
    }

    @Override
    public Portal getPortal() {
        return inner.getPortal();
    }

    @Override
    public int getStatusCode() {
        return inner.getStatusCode();
    }

    @Override
    public String getStatusMessage() {
        return inner.getStatusMessage();
    }

    @Override
    public Throwable getThrowable() {
        return inner.getThrowable();
    }

    @Override
    public Object getTitle() {
        return inner.getTitle();
    }

    @Override
    public boolean isCancelled() {
        return inner.isCancelled();
    }

    @Override
    public boolean isDone() {
        return inner.isDone();
    }

    @Override
    public boolean isSuccess() {
        return inner.isSuccess();
    }

    @Override
    public boolean mayInterruptIfRunning() {
        return inner.mayInterruptIfRunning();
    }

    @Override
    public void remove(String key) {
        inner.remove(key);
    }

    @Override
    public void set(String key, Object value) {
        inner.set(key, value);
    }

    @Override
    public void setMayInterruptIfRunning(boolean mayInterruptIfRunning) {
        inner.setMayInterruptIfRunning(mayInterruptIfRunning);
    }

    @Override
    public void setThrowable(Throwable throwable) {
        inner.setThrowable(throwable);
    }

    @Override
    public void setTitle(Object title) {
        inner.setTitle(title);
    }

    public WindowImpl getInner() {
        return this.inner;
    }

}
