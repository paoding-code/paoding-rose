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

import net.paoding.rose.scanning.LoadScope;
import net.paoding.rose.scanning.context.core.RoseResources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.Resource;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author han.liao [in355hz@gmail.com]
 * 
 */
public class RoseAppContext extends AbstractXmlApplicationContext {

    private Log logger = LogFactory.getLog(getClass());

    private String[] scopeValues;

    public RoseAppContext() {
        this("", true);
    }

    public RoseAppContext(String scope, boolean refresh) {
        this(new LoadScope(scope, "applicationContext"), refresh);
    }

    public RoseAppContext(LoadScope scope, boolean refresh) {
        this.scopeValues = scope.getScope("applicationContext");
        logger.info("create a RoseAppContext, with scope='" + scope + "'");
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
    protected final Resource[] getConfigResources() {
        try {
            return getConfigResourcesThrowsIOException();
        } catch (IOException e) {
            throw new ApplicationContextException("getConfigResources", e);
        }
    }

    protected Resource[] getConfigResourcesThrowsIOException() throws IOException {
        return RoseResources.findContextResources(this.scopeValues).toArray(new Resource[0]);
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
    }

    public RoseAppContext getApplicationContext() {
        return this;
    }

}
