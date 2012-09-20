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
package net.paoding.rose.controllers;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
import net.paoding.rose.RoseVersion;

import org.springframework.core.SpringVersion;

class Utils {

    public static String wrap(String msg) {
        String roseVersion = RoseVersion.getVersion();
        String springVersion = SpringVersion.getVersion();
        return "@<html><head><title>Paoding Rose " + roseVersion + "@Spring-" + springVersion
                + "</title></head><body>" + msg + "<div>" + roseVersion + "@Spring-"
                + springVersion + "</div></body></html>";
    }
}
