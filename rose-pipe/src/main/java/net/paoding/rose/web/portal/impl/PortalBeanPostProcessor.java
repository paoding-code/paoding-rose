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
package net.paoding.rose.web.portal.impl;

import java.util.Arrays;
import java.util.List;

import net.paoding.rose.web.portal.WindowListener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.WebApplicationContext;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class PortalBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    public static final String PORTAL_EXECUTOR_CORE_POOL_SIZE = "portalExecutorCorePoolSize";

    public static final String PORTAL_EXECUTOR_MAX_POOL_SIZE = "portalExecutorMaxPoolSize";

    public static final String PORTAL_EXECUTOR_KEEP_ALIVE_SECONDS = "portalExecutorKeepAliveSeconds";

    public static final String PORTAL_LISTENERS = "portalListeners";

    private static Log logger = LogFactory.getLog(PortalBeanPostProcessor.class);

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {

        if (applicationContext instanceof WebApplicationContext) {
            WebApplicationContext webApplicationContext = (WebApplicationContext) applicationContext;
            if (ThreadPoolTaskExecutor.class == bean.getClass()) {
                ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) bean;
                String paramCorePoolSize = webApplicationContext.getServletContext()
                        .getInitParameter(PORTAL_EXECUTOR_CORE_POOL_SIZE);
                if (StringUtils.isNotBlank(paramCorePoolSize)) {
                    if (logger.isInfoEnabled()) {
                        logger.info("found param " + PORTAL_EXECUTOR_CORE_POOL_SIZE + "="
                                + paramCorePoolSize);
                    }
                    executor.setCorePoolSize(Integer.parseInt(paramCorePoolSize));
                } else {
                    throw new IllegalArgumentException(
                            "please add '<context-param><param-name>portalExecutorCorePoolSize</param-name><param-value>a number here</param-value></context-param>' in your web.xml");
                }
                String paramMaxPoolSize = webApplicationContext.getServletContext()
                        .getInitParameter(PORTAL_EXECUTOR_MAX_POOL_SIZE);
                if (StringUtils.isNotBlank(paramMaxPoolSize)) {
                    if (logger.isInfoEnabled()) {
                        logger.info("found param " + PORTAL_EXECUTOR_MAX_POOL_SIZE + "="
                                + paramMaxPoolSize);
                    }
                    executor.setMaxPoolSize(Integer.parseInt(paramMaxPoolSize));
                }
                String paramKeepAliveSeconds = webApplicationContext.getServletContext()
                        .getInitParameter(PORTAL_EXECUTOR_KEEP_ALIVE_SECONDS);
                if (StringUtils.isNotBlank(paramKeepAliveSeconds)) {
                    if (logger.isInfoEnabled()) {
                        logger.info("found param " + PORTAL_EXECUTOR_KEEP_ALIVE_SECONDS + "="
                                + paramKeepAliveSeconds);
                    }
                    executor.setKeepAliveSeconds(Integer.parseInt(paramKeepAliveSeconds));
                }
            } else if (List.class.isInstance(bean) && "portalListenerList".equals(beanName)) {
                String paramListeners = webApplicationContext.getServletContext().getInitParameter(
                        PORTAL_LISTENERS);
                @SuppressWarnings("unchecked")
                List<WindowListener> list = (List<WindowListener>) bean;
                if (StringUtils.isNotBlank(paramListeners)) {
                    String[] splits = paramListeners.split(",| ");
                    if (logger.isInfoEnabled()) {
                        logger.info("found portalListener config: " + Arrays.toString(splits));
                    }
                    for (String className : splits) {
                        className = className.trim();
                        if (className.length() > 0) {
                            try {
                                Class<?> clazz = Class.forName(className);
                                WindowListener l = (WindowListener) BeanUtils
                                        .instantiateClass(clazz);
                                list.add(l);
                                if (logger.isInfoEnabled()) {
                                    logger.info("add portalListener: " + l);
                                }
                            } catch (Exception e) {
                                logger.error("", e);
                            }
                        }
                    }
                }
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        return bean;
    }

}
