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
package net.paoding.rose.web.impl.module;

import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import net.paoding.rose.RoseConstants;
import net.paoding.rose.scanner.ModuleResource;
import net.paoding.rose.util.RoseStringUtil;
import net.paoding.rose.util.SpringUtils;
import net.paoding.rose.web.ControllerErrorHandler;
import net.paoding.rose.web.ControllerInterceptor;
import net.paoding.rose.web.InterceptorDelegate;
import net.paoding.rose.web.OncePerRequestInterceptorDelegate;
import net.paoding.rose.web.ParamValidator;
import net.paoding.rose.web.advancedinterceptor.Ordered;
import net.paoding.rose.web.annotation.Ignored;
import net.paoding.rose.web.annotation.Interceptor;
import net.paoding.rose.web.annotation.NotForSubModules;
import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.paramresolver.ParamResolver;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * 根据输入的module类信息，构造出具体的Module结构出来
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class ModulesBuilderImpl implements ModulesBuilder {

    private Log logger = LogFactory.getLog(getClass());

    public List<Module> build(List<ModuleResource> moduleResources,
            WebApplicationContext rootContext) throws Exception {

        // 重排序，使父级别的模块资源比子模块资源先处理
        moduleResources = new ArrayList<ModuleResource>(moduleResources);
        Collections.sort(moduleResources);

        // 将要返回的模块列表
        List<Module> modules = new ArrayList<Module>(moduleResources.size());
        Map<ModuleResource, Module> modulesAsMap = new HashMap<ModuleResource, Module>();

        // 
        for (ModuleResource moduleResource : moduleResources) {
            final Module parentModule = (moduleResource.getParent() == null) ? null//
                    : modulesAsMap.get(moduleResource.getParent());
            final WebApplicationContext parentContext = (parentModule == null) ? rootContext//
                    : parentModule.getApplicationContext();
            final String namespace = "context@controllers"
                    + moduleResource.getRelativePath().replace('/', '.');

            // 创建该module的spring context对象
            final ServletContext servletContext = parentContext == null ? null //
                    : parentContext.getServletContext();
            final ModuleAppContext moduleContext = ModuleAppContext.createModuleContext(//
                    parentContext,//
                    moduleResource.getContextResources(),//
                    moduleResource.getMessageBasenames(),//
                    /*id*/moduleResource.getModuleUrl().toString(),//
                    namespace//
                    );

            // 扫描找到的类...定义到applicationContext
            registerBeanDefinitions(moduleContext, moduleResource.getModuleClasses());

            // 创建module对象
            final ModuleImpl module = new ModuleImpl(//
                    parentModule, //
                    moduleResource.getModuleUrl(), //
                    moduleResource.getMappingPath(), //
                    moduleResource.getRelativePath(), //
                    moduleContext);
            //
            modulesAsMap.put(moduleResource, module);

            // 设置到servletContext全局属性
            if (servletContext != null) {
                String contextAttrKey = WebApplicationContext.class.getName() + "@"
                        + moduleResource.getModuleUrl();
                servletContext.setAttribute(contextAttrKey, moduleContext);
            }

            // 从Spring应用环境中找出本web模块要使用的ParamValidator，ParamResolver, ControllerInterceptor, ControllerErrorHandler
            List<ParamResolver> customerResolvers = findContextResolvers(moduleContext);

            // resolvers
            module.setCustomerResolvers(customerResolvers);
            if (logger.isDebugEnabled()) {
                logger.debug("module '" + module.getMappingPath() + "': apply resolvers "
                        + customerResolvers);
            }

            // 将拦截器设置到module中
            List<InterceptorDelegate> interceptors = findInterceptors(moduleContext);
            for (Iterator<InterceptorDelegate> iter = interceptors.iterator(); iter.hasNext();) {
                InterceptorDelegate interceptor = iter.next();

                ControllerInterceptor most = InterceptorDelegate
                        .getMostInnerInterceptor(interceptor);

                if (!most.getClass().getName().startsWith("net.paoding.rose.web")) {

                    // 先排除deny禁止的
                    if (moduleResource.getInterceptedDeny() != null) {
                        if (RoseStringUtil.matches(moduleResource.getInterceptedDeny(), interceptor
                                .getName())) {
                            iter.remove();
                            if (logger.isDebugEnabled()) {
                                logger.debug("module '" + module.getMappingPath()
                                        + "': remove interceptor by rose.properties: "
                                        + most.getClass().getName());
                            }
                            continue;
                        }
                    }
                    //  确认最大的allow允许
                    if (moduleResource.getInterceptedAllow() != null) {
                        if (!RoseStringUtil.matches(moduleResource.getInterceptedAllow(),
                                interceptor.getName())) {
                            iter.remove();
                            if (logger.isDebugEnabled()) {
                                logger.debug("module '" + module.getMappingPath()
                                        + "': remove interceptor by rose.properties: "
                                        + most.getClass().getName());
                            }
                            continue;
                        }
                    }
                }
            }
            module.setControllerInterceptors(interceptors);
            if (logger.isDebugEnabled()) {
                logger.debug("module '" + module.getMappingPath() + "': apply intercetpors "
                        + interceptors);
            }

            // 将validator设置到module中
            List<ParamValidator> validators = findContextValidators(moduleContext);
            module.setValidators(validators);
            if (logger.isDebugEnabled()) {
                logger.debug("module '" + module.getMappingPath() + "': apply global validators "
                        + validators);
            }

            // errorhandler
            ControllerErrorHandler errorHandler = getContextErrorHandler(moduleContext);
            if (errorHandler != null) {
                if (Proxy.isProxyClass(errorHandler.getClass())) {
                    module.setErrorHandler(errorHandler);
                } else {
                    ErrorHandlerDispatcher dispatcher = new ErrorHandlerDispatcher(errorHandler);
                    module.setErrorHandler(dispatcher);
                }
                if (logger.isInfoEnabled()) {
                    logger.info("set errorHandler: " + module.getMappingPath() + "  "
                            + errorHandler);
                }
            }

            // controllers
            final ListableBeanFactory beanFactory = moduleContext.getBeanFactory();
            for (String beanName : beanFactory.getBeanDefinitionNames()) {
                checkController(moduleContext, beanName, module);
            }

            // 放进去以返回
            modules.add(module);
        }

        return modules;
    }

    private void throwExceptionIfDuplicatedNames(List<InterceptorDelegate> interceptors) {
        for (int i = 0; i < interceptors.size(); i++) {
            InterceptorDelegate interceptor = interceptors.get(i);
            for (int j = i + 1; j < interceptors.size(); j++) {
                // 先判断是否有"名字"一样的拦截器
                InterceptorDelegate position = interceptors.get(j);
                if (position.getName().equals(interceptor.getName())) {
                    // rose内部要求interceptor要有一个唯一的标识
                    // 请这两个类的提供者商量改类名，不能同时取一样的类名
                    // 如果是通过@Component等设置名字的，则不要设置一样
                    ControllerInterceptor duplicated1 = InterceptorDelegate
                            .getMostInnerInterceptor(position);
                    ControllerInterceptor duplicated2 = InterceptorDelegate
                            .getMostInnerInterceptor(interceptor);

                    throw new IllegalArgumentException(
                            "duplicated interceptor name for these two interceptors: '"
                                    + duplicated1.getClass() + "' and '" + duplicated2.getClass()
                                    + "'");
                }
            }
        }
    }

    private boolean checkController(final XmlWebApplicationContext context, String beanName,
            ModuleImpl module) throws IllegalAccessException {
        AbstractBeanDefinition beanDefinition = (AbstractBeanDefinition) context.getBeanFactory()
                .getBeanDefinition(beanName);
        String beanClassName = beanDefinition.getBeanClassName();
        String controllerSuffix = null;
        for (String suffix : RoseConstants.CONTROLLER_SUFFIXES) {
            if (beanClassName.length() > suffix.length() && beanClassName.endsWith(suffix)) {
                if (suffix.length() == 1
                        && Character.isUpperCase(beanClassName.charAt(beanClassName.length()
                                - suffix.length() - 1))) {
                    continue;
                }
                controllerSuffix = suffix;
                break;
            }
        }
        if (controllerSuffix == null) {
            if (beanDefinition.hasBeanClass()) {
                Class<?> beanClass = beanDefinition.getBeanClass();
                if (beanClass.isAnnotationPresent(Path.class)) {
                    throw new IllegalArgumentException("@" + Path.class.getSimpleName()
                            + " is only allowed in Resource/Controller, "
                            + "is it a Resource/Controller? wrong spelling? : " + beanClassName);
                }
            }
            // 对少字母l、r、u，er写成or的等常见错误进行提醒
            if (beanClassName.endsWith("Controler") || beanClassName.endsWith("Controllor")
                    || beanClassName.endsWith("Resouce") || beanClassName.endsWith("Resorce")) {
                // 记录错误，但不throw出，不阻断其他程序的正常运行
                logger.error("", new IllegalArgumentException(
                        "invalid class name end， wrong spelling? : " + beanClassName));
            }
            return false;
        }
        String[] controllerPaths = null;
        if (!beanDefinition.hasBeanClass()) {
            try {
                beanDefinition.resolveBeanClass(Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e) {
                throw new CannotLoadBeanClassException("", beanName, beanDefinition
                        .getBeanClassName(), e);
            }
        }
        final Class<?> clazz = beanDefinition.getBeanClass();
        final String controllerName = StringUtils.removeEnd(ClassUtils
                .getShortNameAsProperty(clazz), controllerSuffix);
        Path reqMappingAnnotation = clazz.getAnnotation(Path.class);
        if (reqMappingAnnotation != null) {
            controllerPaths = reqMappingAnnotation.value();
        }
        if (controllerPaths != null) {
            // 如果controllerPaths.length==0，表示没有任何path可以映射到这个controller了
            for (int i = 0; i < controllerPaths.length; i++) {
                if ("#".equals(controllerPaths[i])) {
                    controllerPaths[i] = "/" + controllerName;
                } else if (controllerPaths[i].equals("/")) {
                    controllerPaths[i] = "";
                } else if (controllerPaths[i].length() > 0 && controllerPaths[i].charAt(0) != '/') {
                    controllerPaths[i] = "/" + controllerPaths[i];
                }
                if (controllerPaths[i].length() > 1 && controllerPaths[i].endsWith("/")) {
                    if (controllerPaths[i].endsWith("//")) {
                        throw new IllegalArgumentException("invalid path '" + controllerPaths[i]
                                + "' for controller " + beanClassName
                                + ": don't end with more than one '/'");
                    }
                    controllerPaths[i] = controllerPaths[i].substring(0, controllerPaths[i]
                            .length() - 1);
                }
            }
        } else {
            // TODO: 这个代码是为了使从0.9到1.0比较顺畅而做的判断，201007之后可以考虑删除掉
            if (controllerName.equals("index") || controllerName.equals("home")
                    || controllerName.equals("welcome")) {
                // 这个异常的意思是让大家在IndexController/HomeController/WelcomeController上明确标注@Path("")
                throw new IllegalArgumentException("please add @Path(\"\") to " + clazz.getName());
            } else {
                controllerPaths = new String[] { "/" + controllerName };
            }
        }
        // 这个Controller是否已经在Context中配置了?
        // 如果使用Context配置，就不需要在这里实例化
        Object controller = context.getBean(beanName);
        module.addController(//
                controllerPaths, clazz, controllerName, controller);
        if (Proxy.isProxyClass(controller.getClass())) {
            if (logger.isDebugEnabled()) {
                logger.debug("module '" + module.getMappingPath() + "': add controller "
                        + Arrays.toString(controllerPaths) + "= proxy of " + clazz.getName());
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("module '"
                        + module.getMappingPath() //
                        + "': add controller " + Arrays.toString(controllerPaths) + "= "
                        + controller.getClass().getName());
            }
        }
        return true;
    }

    private static final String AUTO_BEAN_NAME_PREFIX = "ModuleBuilder.";

    private void registerBeanDefinitions(XmlWebApplicationContext context, List<Class<?>> classes) {
        DefaultListableBeanFactory bf = (DefaultListableBeanFactory) context.getBeanFactory();
        String[] definedClasses = new String[bf.getBeanDefinitionCount()];
        String[] definitionNames = bf.getBeanDefinitionNames();
        for (int i = 0; i < definedClasses.length; i++) {
            String name = definitionNames[i];
            definedClasses[i] = bf.getBeanDefinition(name).getBeanClassName();
        }
        for (Class<?> clazz : classes) {
            // 排除非规范的类
            if (!isCandidate(clazz)) {
                continue;
            }

            // 排除手动定义的bean
            String clazzName = clazz.getName();
            if (ArrayUtils.contains(definedClasses, clazzName)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignores bean definition because it has been exist in context: "
                            + clazz.getName());
                }
                continue;
            }
            //
            String beanName = null;
            if (StringUtils.isEmpty(beanName) && clazz.isAnnotationPresent(Component.class)) {
                beanName = clazz.getAnnotation(Component.class).value();
            }
            if (StringUtils.isEmpty(beanName) && clazz.isAnnotationPresent(Resource.class)) {
                beanName = clazz.getAnnotation(Resource.class).name();
            }
            if (StringUtils.isEmpty(beanName) && clazz.isAnnotationPresent(Service.class)) {
                beanName = clazz.getAnnotation(Service.class).value();
            }
            if (StringUtils.isEmpty(beanName)) {
                beanName = AUTO_BEAN_NAME_PREFIX + clazz.getName();
            }

            bf.registerBeanDefinition(beanName, new AnnotatedGenericBeanDefinition(clazz));
        }
    }

    private boolean isCandidate(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Ignored.class)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Ignores bean definition because it's present by @Ignored : "
                        + clazz.getName());
            }
            return false;
        }
        if (!Modifier.isPublic(clazz.getModifiers())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Ignores bean definition because it's not a public class: "
                        + clazz.getName());
            }
            return false;
        }
        if (Modifier.isAbstract(clazz.getModifiers())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Ignores bean definition because it's a abstract class: "
                        + clazz.getName());
            }
            return false;
        }
        if (clazz.getDeclaringClass() != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Ignores bean definition because it's a inner class: "
                        + clazz.getName());
            }
            return false;
        }
        return true;
    }

    /** 错误处理器，只从本容器找，不从父容器找 */
    private ControllerErrorHandler getContextErrorHandler(XmlWebApplicationContext context) {
        ControllerErrorHandler errorHandler = null;
        String[] names = context.getBeanNamesForType(ControllerErrorHandler.class);
        for (int i = 0; errorHandler == null && i < names.length; i++) {
            errorHandler = (ControllerErrorHandler) context.getBean(names[i]);
            Class<?> userClass = ClassUtils.getUserClass(errorHandler);
            if (userClass.isAnnotationPresent(Ignored.class)) {
                logger.debug("Ignored controllerErrorHandler: " + errorHandler);
                errorHandler = null;
                continue;
            }
        }
        return errorHandler;
    }

    private List<ParamResolver> findContextResolvers(XmlWebApplicationContext context) {
        String[] resolverNames = SpringUtils.getBeanNames(context.getBeanFactory(),
                ParamResolver.class);
        ArrayList<ParamResolver> resolvers = new ArrayList<ParamResolver>(resolverNames.length);
        for (String beanName : resolverNames) {
            ParamResolver resolver = (ParamResolver) context.getBean(beanName);
            Class<?> userClass = ClassUtils.getUserClass(resolver);
            if (userClass.isAnnotationPresent(Ignored.class)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignored context resolver:" + resolver);
                }
                continue;
            }
            if (userClass.isAnnotationPresent(NotForSubModules.class)
                    && context.getBeanFactory().getBeanDefinition(beanName) == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignored context resolver (NotForSubModules):" + resolver);
                }
                continue;
            }
            resolvers.add(resolver);
            if (logger.isDebugEnabled()) {
                logger.debug("context resolver[" + resolver.getClass().getName());
            }
        }
        return resolvers;
    }

    private List<InterceptorDelegate> findInterceptors(XmlWebApplicationContext context) {
        String[] interceptorNames = SpringUtils.getBeanNames(context.getBeanFactory(),
                ControllerInterceptor.class);
        ArrayList<InterceptorDelegate> interceptors = new ArrayList<InterceptorDelegate>(
                interceptorNames.length);
        for (String beanName : interceptorNames) {
            ControllerInterceptor interceptor = (ControllerInterceptor) context.getBean(beanName);
            Class<?> userClass = ClassUtils.getUserClass(interceptor);
            if (userClass.isAnnotationPresent(Ignored.class)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignored interceptor (Ignored):" + interceptor);
                }
                continue;
            }
            if (userClass.isAnnotationPresent(NotForSubModules.class)
                    && !context.getBeanFactory().containsBeanDefinition(beanName)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignored interceptor (NotForSubModules):" + interceptor);
                }
                continue;
            }
            if (!userClass.getSimpleName().endsWith(RoseConstants.INTERCEPTOR_SUFFIX)) {
                logger.error("", new IllegalArgumentException("Interceptor must be end with '"
                        + RoseConstants.INTERCEPTOR_SUFFIX + "': " + userClass.getName()));
                continue;
            }
            InterceptorBuilder builder = new InterceptorBuilder(interceptor);
            Interceptor annotation = userClass.getAnnotation(Interceptor.class);
            if (annotation != null) {
                builder.oncePerRequest(annotation.oncePerRequest());
            }
            String interceporName;
            if (beanName.startsWith(AUTO_BEAN_NAME_PREFIX)) {
                interceporName = StringUtils.removeEnd(StringUtils.uncapitalize(userClass
                        .getSimpleName()), RoseConstants.INTERCEPTOR_SUFFIX);
            } else {
                interceporName = StringUtils.removeEnd(beanName, RoseConstants.INTERCEPTOR_SUFFIX);
            }
            final String rose = "rose";
            if (interceporName.startsWith(rose)
                    && (interceporName.length() == rose.length() || Character
                            .isUpperCase(interceporName.charAt(rose.length())))
                    && !userClass.getName().startsWith("net.paoding.rose.")) {
                throw new IllegalArgumentException("illegal interceptor name '" + interceporName
                        + "' for " + userClass.getName()
                        + ": don't starts with 'rose', it's reserved");
            }

            builder.name(interceporName);

            InterceptorDelegate wrapper = builder.build();
            interceptors.add(wrapper);
            if (logger.isDebugEnabled()) {
                int priority = 0;
                if (interceptor instanceof Ordered) {
                    priority = ((Ordered) interceptor).getPriority();
                }
                logger.debug("recognized interceptor[priority=" + priority + "]: " // \r\n
                        + wrapper.getName() + "=" + userClass.getName());
            }
        }
        Collections.sort(interceptors);
        throwExceptionIfDuplicatedNames(interceptors);
        return interceptors;
    }

    private List<ParamValidator> findContextValidators(XmlWebApplicationContext context) {
        String[] validatorNames = SpringUtils.getBeanNames(context.getBeanFactory(),
                ParamValidator.class);
        ArrayList<ParamValidator> globalValidators = new ArrayList<ParamValidator>(
                validatorNames.length);
        for (String beanName : validatorNames) {
            ParamValidator validator = (ParamValidator) context.getBean(beanName);
            Class<?> userClass = ClassUtils.getUserClass(validator);
            if (userClass.isAnnotationPresent(Ignored.class)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignored context validator:" + validator);
                }
                continue;
            }
            if (userClass.isAnnotationPresent(NotForSubModules.class)
                    && context.getBeanFactory().getBeanDefinition(beanName) == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignored context validator (NotForSubModules):" + validator);
                }
                continue;
            }
            globalValidators.add(validator);
            if (logger.isDebugEnabled()) {
                logger.debug("add context validator: " + userClass.getName());
            }
        }
        return globalValidators;
    }

    public static class InterceptorBuilder {

        private boolean oncePerRequest;

        private String name;

        private ControllerInterceptor interceptor;

        public InterceptorBuilder(ControllerInterceptor interceptor) {
            this.interceptor = interceptor;
        }

        public InterceptorBuilder name(String name) {
            this.name = name;
            return this;
        }

        public InterceptorBuilder oncePerRequest(boolean oncePerRequest) {
            this.oncePerRequest = oncePerRequest;
            return this;
        }

        public InterceptorDelegate build() {
            ControllerInterceptor interceptor = this.interceptor;
            if (oncePerRequest) {
                interceptor = new OncePerRequestInterceptorDelegate(interceptor);
            }
            InterceptorDelegate wrapper = new InterceptorDelegate(interceptor);
            if (StringUtils.isBlank(wrapper.getName())) {
                wrapper.setName(name);
            }
            return wrapper;
        }
    }
}
