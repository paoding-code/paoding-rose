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

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import net.paoding.rose.web.portal.Portal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
class PortalResponse extends HttpServletResponseWrapper {

    private static final Log logger = LogFactory.getLog(PortalResponse.class);

    private Portal portal;

    public PortalResponse(Portal portal, HttpServletResponse response) {
        super(response);
        this.portal = portal;
    }

    public Portal getPortal() {
        return portal;
    }

    @Override
    public void setResponse(ServletResponse response) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("set response: %s", response));
        }
        super.setResponse(response);
    }

    // header设置可能因并发而冲突，故作同步化
    @Override
    public void setHeader(String name, String value) {
        synchronized (portal) {
            super.setHeader(name, value);
        }
    }

    @Override
    public void addHeader(String name, String value) {
        synchronized (portal) {
            super.addHeader(name, value);
        }
    }

}
