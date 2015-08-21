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
package net.paoding.rose.web.paramresolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.paoding.rose.web.annotation.FlashParam;
import net.paoding.rose.web.annotation.Param;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.ClassUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class ParameterNameDiscovererImpl {

    public String[] getParameterNames(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        String[] names = new String[parameterTypes.length];
        Map<String, Integer> counts = new HashMap<String, Integer>();
        for (int i = 0; i < names.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            for (Annotation annotation : annotations) {
                String name = null;
                if (annotation instanceof Param) {
                    name = ((Param) annotation).value();
                } else if (annotation instanceof FlashParam) {
                    name = ((FlashParam) annotation).value();
                }
                if (name != null) {
                    if (StringUtils.isNotEmpty(name)) {
                        names[i] = name;
                        if ((parameterTypes[i] == BindingResult.class || parameterTypes[i] == Errors.class)
                                && !name.endsWith("BindingResult")) {
                            names[i] = name + "BindingResult";
                        }
                    }
                    break;
                }
            }
            if (names[i] != null) {
                continue;
            }
            if (parameterTypes[i] == BindingResult.class || parameterTypes[i] == Errors.class) {
                if (i > 0 && names[i - 1] != null) {
                    names[i] = names[i - 1] + "BindingResult";
                    continue;
                }
            }
            String rawName = getParameterRawName(parameterTypes[i]);
            if (rawName == null) {
                continue;
            }
            names[i] = rawName;
            Integer count = counts.get(rawName);
            if (count == null) {
                counts.put(rawName, 1);
            } else {
                counts.put(rawName, count + 1);
                if (count == 1) {
                    for (int j = 0; j < i; j++) {
						if (names[j] != null && names[j].equals(rawName)) {
							names[j] = rawName + "1";
							break;
						}
                    }
                }
                if (names[i] == rawName) {
                    names[i] = names[i] + (count + 1);
                }
            }
        }
        Set<String> uniques = new HashSet<String>();
        for (String name : names) {
            if (name == null) {
                continue;
            }
            if (uniques.contains(name)) {
                // action方法不能有相同名字的@Param参数
                throw new IllegalArgumentException("params with same name: '" + name + "'");
            }
            uniques.add(name);
        }
        return names;
    }

    protected String getParameterRawName(Class<?> clz) {
        if (ClassUtils.isPrimitiveOrWrapper(clz) //
                || clz == String.class // 
                || Map.class.isAssignableFrom(clz) //
                || Collection.class.isAssignableFrom(clz) //
                || clz.isArray() //
                || clz == MultipartFile.class) {
            return null;
        }
        if (clz == MultipartFile.class) {
            return null;
        }
        return ClassUtils.getShortNameAsProperty(clz);
    }

    public String[] getParameterNames(@SuppressWarnings("unchecked") Constructor ctor) {
        throw new UnsupportedOperationException();
    }
}
