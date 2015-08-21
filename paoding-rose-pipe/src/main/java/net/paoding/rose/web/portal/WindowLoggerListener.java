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
package net.paoding.rose.web.portal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class WindowLoggerListener implements WindowListener {

    private Log logger = LogFactory.getLog(WindowLoggerListener.class);

    @Override
    public void onWindowAdded(Window window) {
        if (logger.isDebugEnabled()) {
            logger.debug("onWindowAdded: [" + window.getName() + "]@" + window.getContainer());
        }
    }

    @Override
    public void onWindowCanceled(Window window) {
        if (logger.isDebugEnabled()) {
            logger.debug("onWindowCanceled: [" + window.getName() + "]@" + window.getContainer());
        }
    }

    @Override
    public void onWindowDone(Window window) {
        if (logger.isDebugEnabled()) {
            logger.debug("onWindowDone: [" + window.getName() + "]@" + window.getContainer());
        }
    }

    @Override
    public void onWindowError(Window window) {
        if (logger.isDebugEnabled()) {
            logger.debug("onWindowError: [" + window.getName() + "]@" + window.getContainer(),
                    window.getThrowable());
        }
    }

    @Override
    public void onWindowStarted(Window window) {
        if (logger.isDebugEnabled()) {
            logger.debug("onWindowStarted: [" + window.getName() + "]@" + window.getContainer());
        }
    }

    @Override
    public void onWindowTimeout(Window window) {
        if (logger.isDebugEnabled()) {
            logger.debug("onWindowTimeout: [" + window.getName() + "]@" + window.getContainer());
        }
    }
}
