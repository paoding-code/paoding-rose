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
package net.paoding.rose.web.impl.view;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import net.paoding.rose.util.SpringUtils;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.impl.view.velocity.RoseVelocityConfigurer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.web.servlet.view.velocity.VelocityConfig;
import org.springframework.web.servlet.view.velocity.VelocityLayoutViewResolver;
import org.springframework.web.servlet.view.velocity.VelocityViewResolver;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class ViewDispatcherImpl implements ViewDispatcher, ApplicationContextAware {

    private static Log logger = LogFactory.getLog(ViewDispatcherImpl.class);

    private ViewResolver jspViewResolver;

    private ViewResolver internalResourceViewResolver = new InternalResourceViewResolver();

    private ConfigurableWebApplicationContext applicationContext;

    private Map<String, VelocityViewResolver> velocityViewResolvers = new HashMap<String, VelocityViewResolver>();

    public ViewDispatcherImpl() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableWebApplicationContext) applicationContext;
        SpringUtils.autowire(internalResourceViewResolver, applicationContext);
    }

    public ConfigurableWebApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public View resolveViewName(Invocation inv, String viewPath, Locale locale) throws Exception {
        ViewResolver viewResolver = getSpringViewResolver(inv, viewPath);
        if (logger.isDebugEnabled()) {
            logger.debug("found viewResolver '" + viewResolver + "' for viewPath '" + viewPath
                    + "'");
        }
        return viewResolver.resolveViewName(viewPath, locale);
    }

    protected ViewResolver getSpringViewResolver(Invocation inv, String viewPath)
            throws IOException {
        if (viewPath.endsWith(".vm")) {
            if (logger.isDebugEnabled()) {
                logger.debug("to get velocity view resolver.");
            }
            return getVelocityViewResolver(inv, viewPath);
        }
        if (viewPath.endsWith(".xml")) {
            if (logger.isDebugEnabled()) {
                logger.debug("to get velocity view resolver.");
            }
            return new DyContentTypeViewResolver(getVelocityViewResolver(inv, viewPath), "text/xml");
        }
        if (viewPath.endsWith(".jsp")) {
            if (logger.isDebugEnabled()) {
                logger.debug("to get jsp view resolver.");
            }
            return getJspViewResolver();
        }

        int lastIndex = viewPath.lastIndexOf('.');
        if (lastIndex >= 0) {
            if (viewPath.endsWith(".js") || viewPath.endsWith(".css") || viewPath.endsWith(".ico")
                    || viewPath.endsWith(".html") || viewPath.endsWith(".htm")
                    || viewPath.endsWith(".txt") || viewPath.endsWith(".jpg")
                    || viewPath.endsWith(".gif")) {
                if (logger.isDebugEnabled()) {
                    logger.debug("to get internal resource resolver.");
                }
                return internalResourceViewResolver;
            }
            String beanName = viewPath.substring(lastIndex + 1) + "ViewResolver";
            ViewResolver resolver = (ViewResolver) SpringUtils.getBean(getApplicationContext(),
                    beanName);
            return resolver != null ? resolver : internalResourceViewResolver;
        } else {
            return internalResourceViewResolver;
        }
    }

    private String getDirectory(String viewPath) {
        int index = viewPath.lastIndexOf('/');
        return index == -1 ? "" : viewPath.substring(0, index);
    }

    protected ViewResolver getJspViewResolver() throws IOException {
        if (this.jspViewResolver != null) {
            return this.jspViewResolver;
        }
        String beanName = "jspViewResolver";
        this.jspViewResolver = (ViewResolver) SpringUtils
                .getBean(getApplicationContext(), beanName);
        if (jspViewResolver == null) {
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(InternalResourceViewResolver.class);
            MutablePropertyValues propertyValues = new MutablePropertyValues();
            propertyValues.addPropertyValue(new PropertyValue("viewClass", JstlView.class));
            beanDefinition.setPropertyValues(propertyValues);
            ((BeanDefinitionRegistry) getApplicationContext().getBeanFactory())
                    .registerBeanDefinition(beanName, beanDefinition);
            logger.info("registered bean definition named " + beanName + ": "
                    + InternalResourceViewResolver.class.getName());
            jspViewResolver = (ViewResolver) SpringUtils.getBean(getApplicationContext(), beanName);
        }
        return this.jspViewResolver;
    }

    protected ViewResolver getVelocityViewResolver(Invocation inv, String viewPath)
            throws IOException {
        //
        VelocityViewResolver viewResolver = velocityViewResolvers.get(viewPath);
        if (viewResolver != null) {
            return viewResolver;
        }
        //
        String viewDirectory = getDirectory(viewPath);
        viewResolver = velocityViewResolvers.get(viewDirectory);
        if (viewResolver != null) {
            velocityViewResolvers.put(viewPath, viewResolver);
            return viewResolver;
        }
        StringBuilder sb = new StringBuilder();
        boolean beUpperCase = false;
        for (int i = 0; i < viewDirectory.length(); i++) {
            if (viewDirectory.charAt(i) != '/') {
                if (beUpperCase) {
                    sb.append(Character.toUpperCase(viewDirectory.charAt(i)));
                    beUpperCase = false;
                } else {
                    sb.append(viewDirectory.charAt(i));
                }
            } else {
                beUpperCase = true;
            }
        }
        String beanName = sb.toString() + "VelocityViewResolver";
        viewResolver = (VelocityViewResolver) SpringUtils
                .getBean(getApplicationContext(), beanName);
        if (viewResolver == null) {
            String temp = viewDirectory;
            String layoutUrl = null;
            while (temp.length() > 0) {
                String _layoutUrl = temp + "/layout.vm";
                if (logger.isDebugEnabled()) {
                    logger.debug("is default layout file exist? " + _layoutUrl);
                }
                File layout = new File(inv.getServletContext().getRealPath(_layoutUrl));
                if (layout.exists()) {
                    layoutUrl = _layoutUrl;
                    if (logger.isDebugEnabled()) {
                        logger.debug("found default layout file " + _layoutUrl);
                    }
                    break;
                } else {
                    int i = temp.lastIndexOf('/');
                    if (i <= 0) {
                        break;
                    }
                    temp = temp.substring(0, i);
                }
            }
            //
            if (layoutUrl == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("not found layout file for " + viewDirectory);
                }
                beanName = "velocityViewResolver";
                viewResolver = (VelocityViewResolver) SpringUtils.getBean(getApplicationContext(),
                        beanName);
            }
            if (viewResolver == null) {
                viewResolver = createVelocityViewResolver(inv, beanName, layoutUrl);
            }
        }
        velocityViewResolvers.put(viewDirectory, viewResolver);
        velocityViewResolvers.put(viewPath, viewResolver);
        return viewResolver;
    }

    private VelocityViewResolver createVelocityViewResolver(Invocation inv, String beanName,
            String layoutUrl) throws MalformedURLException, IOException {
        if (SpringUtils.getBean(getApplicationContext(), VelocityConfig.class) == null) {
            URL propertiesLocation = inv.getServletContext().getResource(
                    "/WEB-INF/velocity.properties");
            Properties velocityProperties = new Properties();
            if (propertiesLocation != null) {
                InputStream is = propertiesLocation.openStream();
                velocityProperties.load(is);
                is.close();
            }
            if (StringUtils.isBlank(velocityProperties.getProperty("input.encoding"))) {
                velocityProperties.setProperty("input.encoding", "UTF-8");
            }
            if (StringUtils.isBlank(velocityProperties.getProperty("output.encoding"))) {
                velocityProperties.setProperty("output.encoding", "UTF-8");
            }
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(RoseVelocityConfigurer.class);
            MutablePropertyValues propertyValues = new MutablePropertyValues();
            propertyValues.addPropertyValue(new PropertyValue("velocityProperties",
                    velocityProperties));
            propertyValues.addPropertyValue(new PropertyValue("resourceLoaderPath", "/"));
            beanDefinition.setPropertyValues(propertyValues);
            ((BeanDefinitionRegistry) getApplicationContext().getBeanFactory())
                    .registerBeanDefinition("velocityConfigurer", beanDefinition);
            logger.info("registered bean definition named" + " velocityConfigurer: "
                    + RoseVelocityConfigurer.class.getName());
        }
        //
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        MutablePropertyValues propertyValues = new MutablePropertyValues();
        if (layoutUrl == null) {
            beanDefinition.setBeanClass(VelocityViewResolver.class);
        } else {
            beanDefinition.setBeanClass(VelocityLayoutViewResolver.class);
            propertyValues.addPropertyValue(new PropertyValue("layoutUrl", layoutUrl));
        }
        propertyValues
                .addPropertyValue(new PropertyValue("contentType", "text/html;charset=UTF-8"));
        String toolboxConfigLocation = "/WEB-INF/velocity-toolbox.xml";
        propertyValues.addPropertyValue(new PropertyValue("cache", Boolean.TRUE));
        URL toolbox = inv.getServletContext().getResource(toolboxConfigLocation);
        if (toolbox == null) {
            toolboxConfigLocation = "/WEB-INF/toolbox.xml";
            toolbox = inv.getServletContext().getResource(toolboxConfigLocation);
        }
        if (toolbox != null) {
            propertyValues.addPropertyValue(new PropertyValue("toolboxConfigLocation",
                    toolboxConfigLocation));
        }
        beanDefinition.setPropertyValues(propertyValues);
        ((BeanDefinitionRegistry) getApplicationContext().getBeanFactory()).registerBeanDefinition(
                beanName, beanDefinition);
        logger.info("registered bean definition named " + beanName + ": "
                + VelocityViewResolver.class.getName());
        return (VelocityViewResolver) SpringUtils.getBean(getApplicationContext(), beanName);
    }

}
