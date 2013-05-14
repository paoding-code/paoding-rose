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
package net.paoding.rose.scanning.context;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletContext;

import net.paoding.rose.scanning.LoadScope;
import net.paoding.rose.scanning.context.core.RoseResources;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author han.liao [in355hz@gmail.com]
 * 
 */
public class RoseWebAppContext extends XmlWebApplicationContext {

    /** Default config location for the root context */
    public static final String DEFAULT_CONFIG_LOCATION = "/WEB-INF/applicationContext*.xml";

    private LoadScope scope;

    public RoseWebAppContext(ServletContext servletContext, boolean refresh) {
        this(servletContext, "", refresh);
    }

    public RoseWebAppContext(ServletContext servletContext, String scope, boolean refresh) {
        this(servletContext, new LoadScope(scope, "controllers"), refresh);
    }

    public RoseWebAppContext(ServletContext servletContext, LoadScope scope, boolean refresh) {
        this.scope = scope;
        this.setServletContext(servletContext);
        if (refresh) {
            refresh();
        }
    }

    /**
     * 返回对应类型的唯一 Bean, 包括可能的祖先 {@link ApplicationContext} 中对应类型的 Bean.
     * 
     * @param beanType - Bean 的类型
     * 
     * @throws BeansException
     */
    public <T> T getBean(Class<T> beanType) throws BeansException {
        return beanType.cast(BeanFactoryUtils.beanOfTypeIncludingAncestors(this, beanType));
    }

    @Override
    protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException,
            IOException {
        Resource[] configResources = getConfigResourcesThrows();
        if (configResources != null) {
            reader.loadBeanDefinitions(configResources);
        }
        String[] configLocations = getConfigLocations();
        if (configLocations != null) {
            for (int i = 0; i < configLocations.length; i++) {
                reader.loadBeanDefinitions(configLocations[i]);
            }
        }
    }

    protected Resource[] getConfigResourcesThrows() throws IOException {
        return RoseResources.findContextResources(scope).toArray(new Resource[0]);
    }

    /**
     * The default location for the root context is
     * "/WEB-INF/applicationContext*.xml".
     */
    protected String[] getDefaultConfigLocations() {
        return new String[] { RoseWebAppContext.DEFAULT_CONFIG_LOCATION };
    }

    @Override
    protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        try {
            prepareBeanFactoryByRose(beanFactory);
        } catch (IOException e) {
            throw new ApplicationContextException("", e);
        }
        super.prepareBeanFactory(beanFactory);
    }

    /**
     * Rose对BeanFactory的特殊处理，必要时可以覆盖这个方法去掉Rose的特有的处理
     * 
     * @throws IOException
     */
    protected void prepareBeanFactoryByRose(ConfigurableListableBeanFactory beanFactory)
            throws IOException {
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        AnnotationConfigUtils.registerAnnotationConfigProcessors(registry);

        String[] messageBaseNames = getMessageBaseNames();
        if (messageBaseNames != null && messageBaseNames.length > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("[roseWebApp/messages] starting registerMessageSourceIfNecessary");
            }
            registerMessageSourceIfNecessary(registry, messageBaseNames);
            if (logger.isDebugEnabled()) {
                logger.debug("[roseWebApp/messages] finished registerMessageSourceIfNecessary");
            }
        }
    }

    protected String[] getMessageBaseNames() throws IOException {
        //
        if (logger.isInfoEnabled()) {
            logger.info("[roseWebApp/messages] start  ...");
        }
        String[] messageBasenames = RoseResources.findMessageBasenames(scope);

        if (logger.isInfoEnabled()) {
            logger.info("[roseWebApp/messages] exits ");
            logger.info("[roseWebApp/messages] add default messages base name: "
                    + "'/WEB-INF/messages'");
        }

        messageBasenames = Arrays.copyOf(messageBasenames, messageBasenames.length + 1);
        messageBasenames[messageBasenames.length - 1] = "/WEB-INF/messages";

        return messageBasenames;

    }

    /** 如果配置文件没有自定义的messageSource定义，则由Rose根据最佳实践进行预设 */
    public static void registerMessageSourceIfNecessary(BeanDefinitionRegistry registry,
            String[] messageBaseNames) {
        if (!registry.containsBeanDefinition(MESSAGE_SOURCE_BEAN_NAME)) {
            GenericBeanDefinition messageSource = new GenericBeanDefinition();
            messageSource.setBeanClass(ReloadableResourceBundleMessageSource.class);
            MutablePropertyValues propertyValues = new MutablePropertyValues();
            propertyValues.addPropertyValue("useCodeAsDefaultMessage", true);
            propertyValues.addPropertyValue("defaultEncoding", "UTF-8"); // properties文件也将使用UTF-8编辑，而非默认的ISO-9959-1
            propertyValues.addPropertyValue("cacheSeconds", 60); // 暂时hardcode! seconds
            propertyValues.addPropertyValue("basenames", messageBaseNames);

            messageSource.setPropertyValues(propertyValues);
            registry.registerBeanDefinition(MESSAGE_SOURCE_BEAN_NAME, messageSource);
        }
    }

}
