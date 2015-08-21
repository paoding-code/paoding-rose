/*
* Copyright 2007-2009 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package net.paoding.rose.web.impl.module;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class ModuleAppContext extends XmlWebApplicationContext {

    private static final Log logger = LogFactory.getLog(ModuleAppContext.class);

    //

    public static ModuleAppContext createModuleContext(WebApplicationContext parent, //
            List<URL> contextResources, String[] messageBasenames, String uniqueId, String namespace)
            throws IOException {

        long startTime = System.currentTimeMillis();

        String loadingMsg = "[moduleContext.create] Loading Spring '" + namespace
                + "' WebApplicationContext";
        logger.info(loadingMsg);
        Assert.notNull(parent);
        ServletContext servletContext = parent.getServletContext();
        Assert.notNull(servletContext);
        ModuleAppContext wac = new ModuleAppContext();
        wac.setParent(parent);
        wac.setServletContext(servletContext);
        wac.setContextResources(toResources(contextResources));
        wac.setId(uniqueId);
        wac.setNamespace(namespace);
        wac.setMessageBaseNames(messageBasenames);
        wac.refresh();

        // 日志打印
        if (logger.isDebugEnabled()) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            logger.debug("[moduleContext.create] Using context class [" + wac.getClass().getName()
                    + "] for " + namespace + " WebApplicationContext");
            logger.info("[moduleContext.create] " + namespace
                    + " WebApplicationContext: initialization completed in " + elapsedTime + " ms");
        }
        return wac;
    }

    public static List<Resource> toResources(List<URL> contextResources) {
        List<Resource> resources = new ArrayList<Resource>();
        for (URL url : contextResources) {
            resources.add(new UrlResource(url));
        }
        return resources;
    }

    //

    private List<Resource> contextResources = Collections.emptyList();

    private String[] messageBaseNames = new String[0];

    public ModuleAppContext() {
    }

    public void setContextResources(List<Resource> contextResources) {
        this.contextResources = contextResources;
    }

    @Override
    protected String[] getDefaultConfigLocations() {
        return null;
    }

    public void setMessageBaseNames(String[] messageBaseNames) {
        if (messageBaseNames == null) {
            messageBaseNames = new String[0];
        }
        this.messageBaseNames = messageBaseNames;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException,
            IOException {
        super.loadBeanDefinitions(reader);
        for (Resource resource : contextResources) {
            reader.loadBeanDefinitions(resource);
        }
    }

    @Override
    protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        prepareBeanFactoryByRose(beanFactory);
        super.prepareBeanFactory(beanFactory);
    }

    /** Rose对BeanFactory的特殊处理，必要时可以覆盖这个方法去掉Rose的特有的处理 */
    protected void prepareBeanFactoryByRose(ConfigurableListableBeanFactory beanFactory) {
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        AnnotationConfigUtils.registerAnnotationConfigProcessors(registry);
        if (messageBaseNames != null && messageBaseNames.length > 0) {
            registerMessageSourceIfNecessary(registry, messageBaseNames);
        }
    }

    /** 如果配置文件没有自定义的messageSource定义，则由Rose根据最佳实践进行预设 */
    public static void registerMessageSourceIfNecessary(BeanDefinitionRegistry registry,
            String[] messageBaseNames) {
        if (!ArrayUtils.contains(registry.getBeanDefinitionNames(), MESSAGE_SOURCE_BEAN_NAME)) {
            logger.debug("registerMessageSource  " + ArrayUtils.toString(messageBaseNames));
            GenericBeanDefinition messageSource = new GenericBeanDefinition();
            messageSource.setBeanClass(ReloadableResourceBundleMessageSource.class);
            MutablePropertyValues propertyValues = new MutablePropertyValues();
            propertyValues.addPropertyValue("useCodeAsDefaultMessage", true);
            propertyValues.addPropertyValue("defaultEncoding", "UTF-8"); // properties文件也将使用UTF-8编辑，而非默认的ISO-9959-1
            propertyValues.addPropertyValue("cacheSeconds", 60); // 暂时hardcode! 60
            propertyValues.addPropertyValue("basenames", messageBaseNames);

            messageSource.setPropertyValues(propertyValues);
            registry.registerBeanDefinition(MESSAGE_SOURCE_BEAN_NAME, messageSource);
        }
    }

}
