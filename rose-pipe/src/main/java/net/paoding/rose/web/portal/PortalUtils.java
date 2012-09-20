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
package net.paoding.rose.web.portal;

import net.paoding.rose.RoseConstants;
import net.paoding.rose.web.Invocation;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class PortalUtils {

    public static Window getWindow(Invocation inv) {
        // get from window request attributes
        // @see WindowTask#run
        return (Window) inv.getRequest().getAttribute(RoseConstants.WINDOW_ATTR);
    }

    public static Portal getPortal(Invocation inv) {
        // get from invocation attributes
        // @see PortalResolver#resolve
        return (Portal) inv.getAttribute("$$paoding-rose-portal.portal");
    }

    public static Pipe getPipe(Invocation inv) {
        // get from head invocation attributes
        return (Pipe) inv.getHeadInvocation().getAttribute("$$paoding-rose-portal.pipe");
    }
}
