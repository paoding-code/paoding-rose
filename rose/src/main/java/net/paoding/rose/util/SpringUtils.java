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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

/**
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class SpringUtils {

    private static Log logger = LogFactory.getLog(SpringUtils.class);

    public static <T> T getBean(ListableBeanFactory bf, Class<?> beanClass) {
        String[] names = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(bf, beanClass);
        @SuppressWarnings("unchecked")
        T bean = (T) ((names.length == 0) ? null : bf.getBean(names[0]));
        return bean;
    }

    public static String[] getBeanNames(ListableBeanFactory bf, Class<?> beanClass) {
        return BeanFactoryUtils.beanNamesForTypeIncludingAncestors(bf, beanClass);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getBeans(ListableBeanFactory bf, Class<T> beanClass) {
        String[] names = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(bf, beanClass);
        List<T> beans = new ArrayList<T>(names.length);
        for (String name : names) {
            beans.add((T) bf.getBean(name));
        }
        return beans;
    }

    public static <T> T getBean(ListableBeanFactory bf, String beanName) {
        @SuppressWarnings("unchecked")
        T bean = (T) (bf.containsBean(beanName) ? bf.getBean(beanName) : null);
        return bean;
    }

    /**
     * @param clazz
     * @param context
     * @return
     */
    public static <T> T createBean(Class<?> clazz, ApplicationContext context) {
        @SuppressWarnings("unchecked")
        T bean = (T) context.getAutowireCapableBeanFactory().createBean(clazz,
                AutowireCapableBeanFactory.AUTOWIRE_NO, false);
        if (logger.isDebugEnabled()) {
            logger.debug("create spring bean: " + bean.getClass() + "@" + bean.hashCode());
        }
        return bean;
    }

    /**
     * @param bean
     * @param context
     */
    public static <T> T autowire(T bean, ApplicationContext context) {
        context.getAutowireCapableBeanFactory().autowireBeanProperties(bean,
                AutowireCapableBeanFactory.AUTOWIRE_NO, false);
        @SuppressWarnings("unchecked")
        T ret = (T) context.getAutowireCapableBeanFactory().initializeBean(bean,
                bean.getClass().getName());
        return ret;
    }

}
