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
package net.paoding.rose.util;

import net.paoding.rose.web.Invocation;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapperImpl;

/**
 * 用于识别识别${xxx}串或识别${xxx?}串，并进行替换的工具类
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class PlaceHolderUtils {

    // 以下这些final都是约定好的，不可随意改变。写成final只是为了不到处写字符串而已。
    
    public static final String DOLLAR = "$";
    
    public static final String PLACEHOLDER_PREFIX = "${";

    public static final char PLACEHOLDER_INNER_PREFIX = '{';

    public static final String PLACEHOLDER_INNER_PREFIX_STRING = "" + PLACEHOLDER_INNER_PREFIX;

    public static final char PLACEHOLDER_SUFFIX_CHAR = '}';

    public static final String PLACEHOLDER_SUFFIX = "" + PLACEHOLDER_SUFFIX_CHAR;

    private static final Log logger = LogFactory.getLog(PlaceHolderUtils.class);

    public static String resolve(String text, Invocation inv) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }
        int startIndex = text.indexOf(PLACEHOLDER_PREFIX);
        if (startIndex == -1) {
            return text;
        }
        StringBuilder buf = new StringBuilder(text);
        while (startIndex != -1) {
            int endIndex = buf
                    .indexOf(PLACEHOLDER_SUFFIX, startIndex + PLACEHOLDER_PREFIX.length());
            if (endIndex != -1) {
                String placeholder = null;
                String defaultValue = null;
                for (int i = startIndex + PLACEHOLDER_PREFIX.length(); i < endIndex; i++) {
                    if (buf.charAt(i) == '?') {
                        placeholder = buf.substring(startIndex + PLACEHOLDER_PREFIX.length(), i);
                        defaultValue = buf.substring(i + 1, endIndex);
                        break;
                    }
                }
                if (placeholder == null) {
                    placeholder = buf.substring(startIndex + PLACEHOLDER_PREFIX.length(), endIndex);
                }
                int nextIndex = endIndex + PLACEHOLDER_SUFFIX.length();
                try {
                    int dot = placeholder.indexOf('.');
                    String attributeName = dot == -1 ? placeholder : placeholder.substring(0, dot);
                    String propertyPath = dot == -1 ? "" : placeholder.substring(dot + 1);
                    Object propVal = inv.getModel().get(attributeName);
                    if (propVal != null) {
                        if (propertyPath.length() > 0) {
                            propVal = new BeanWrapperImpl(propVal).getPropertyValue(propertyPath);
                        }
                    } else {
                        if ("flash".equals(attributeName)) {
                            propVal = inv.getFlash().get(propertyPath);
                        } else {
                            propVal = inv.getParameter(placeholder);
                        }
                    }
                    //
                    if (propVal == null) {
                        propVal = defaultValue;
                    }
                    if (propVal == null) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Could not resolve placeholder '" + placeholder + "' in ["
                                    + text + "].");
                        }
                    } else {
                        String toString = propVal.toString();
                        buf.replace(startIndex, endIndex + PLACEHOLDER_SUFFIX.length(), toString);
                        nextIndex = startIndex + toString.length();
                    }
                } catch (Throwable ex) {
                    logger.warn("Could not resolve placeholder '" + placeholder + "' in [" + text
                            + "] : " + ex);
                }
                startIndex = buf.indexOf(PLACEHOLDER_PREFIX, nextIndex);
            } else {
                startIndex = -1;
            }
        }

        return buf.toString();
    }

}
