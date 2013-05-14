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

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class WindowListeners implements WindowListener {

    private List<WindowListener> listeners = new ArrayList<WindowListener>();

    public void setListeners(List<WindowListener> listeners) {
        List<WindowListener> copied = new ArrayList<WindowListener>(listeners);
        for (WindowListener portalListener : copied) {
            if (portalListener == null) {
                throw new NullPointerException("PortalListener");
            }
        }
        this.listeners = copied;
    }

    public void addListener(WindowListener l) {
        if (l == null) {
            throw new NullPointerException("PortalListener");
        }
        this.listeners.add(l);
    }

    @Override
    public void onWindowAdded(Window window) {
        for (WindowListener l : listeners) {
            l.onWindowAdded(window);
        }
    }

    @Override
    public void onWindowCanceled(Window window) {
        for (WindowListener l : listeners) {
            l.onWindowCanceled(window);
        }
    }

    @Override
    public void onWindowDone(Window window) {
        for (WindowListener l : listeners) {
            l.onWindowDone(window);
        }
    }

    @Override
    public void onWindowError(Window window) {
        for (WindowListener l : listeners) {
            l.onWindowError(window);
        }
    }

    @Override
    public void onWindowStarted(Window window) {
        for (WindowListener l : listeners) {
            l.onWindowStarted(window);
        }
    }

    @Override
    public void onWindowTimeout(Window window) {
        for (WindowListener l : listeners) {
            l.onWindowTimeout(window);
        }
    }

}
