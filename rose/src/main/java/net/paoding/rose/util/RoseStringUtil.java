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
package net.paoding.rose.util;

import org.apache.commons.lang.StringUtils;

public class RoseStringUtil {

    public static String relativePathToModulePath(String relativePath) {
        if (relativePath == null) {
            throw new NullPointerException();
        }
        if (relativePath.length() == 0) {
            return "";
        }
        return StringUtils.removeEnd("/" + relativePath, "/");
    }

    public static String mappingPath(String mappingPath) {
        if (mappingPath.length() != 0) {
            mappingPath = StringUtils.removeEnd(mappingPath, "/");
            while (mappingPath.indexOf("//") != -1) {
                mappingPath = mappingPath.replace("//", "/");
            }
        }
        return mappingPath;
    }

    public static boolean startsWith(CharSequence input, String prefix) {
        if (input.length() < prefix.length()) {
            return false;
        }
        if (input.getClass() == String.class) {
            return ((String) input).startsWith(prefix);
        }
        int len = prefix.length();
        for (int i = 0; i < len; i++) {
            char pi = input.charAt(i);
            char ci = prefix.charAt(i);
            if (pi != ci) {
                return false;
            }
        }
        return true;
    }

    /**
     * 支持'*'前后的匹配
     * 
     * @param array
     * @param value
     * @return
     */
    public static boolean matches(String[] patterns, String value) {
        for (int i = 0; i < patterns.length; i++) {
            String pattern = patterns[i];
            if (pattern.equals(value)) {
                return true;
            }
            if (pattern.endsWith("*")) {
                if (value.startsWith(pattern.substring(0, pattern.length() - 1))) {
                    return true;
                }
            }
            if (pattern.startsWith("*")) {
                if (value.endsWith(pattern.substring(1))) {
                    return true;
                }
            }
        }
        return false;
    }
}
