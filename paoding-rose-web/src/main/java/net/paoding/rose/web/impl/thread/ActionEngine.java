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
package net.paoding.rose.web.impl.thread;

import static org.springframework.validation.BindingResult.MODEL_KEY_PREFIX;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.RoseVersion;
import net.paoding.rose.util.RoseStringUtil;
import net.paoding.rose.web.ControllerInterceptor;
import net.paoding.rose.web.InterceptorDelegate;
import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.InvocationChain;
import net.paoding.rose.web.ParamValidator;
import net.paoding.rose.web.RequestPath;
import net.paoding.rose.web.annotation.HttpFeatures;
import net.paoding.rose.web.annotation.IfParamExists;
import net.paoding.rose.web.annotation.Intercepted;
import net.paoding.rose.web.annotation.Return;
import net.paoding.rose.web.impl.module.Module;
import net.paoding.rose.web.impl.validation.ParameterBindingResult;
import net.paoding.rose.web.paramresolver.MethodParameterResolver;
import net.paoding.rose.web.paramresolver.ParamMetaData;
import net.paoding.rose.web.paramresolver.ParamResolver;
import net.paoding.rose.web.paramresolver.ParameterNameDiscovererImpl;
import net.paoding.rose.web.paramresolver.ResolverFactoryImpl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.SpringVersion;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;

/**
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public final class ActionEngine implements Engine {

    private final static Log logger = LogFactory.getLog(ActionEngine.class);

    private final Module module;

    private final Class<?> controllerClass;

    private final Object controller;

    private final Method method;

    private final HttpFeatures httpFeatures;

    private final InterceptorDelegate[] interceptors;

    private final ParamValidator[] validators;

    private final ParamExistenceChecker[] paramExistenceChecker;

    private final MethodParameterResolver methodParameterResolver;

    private transient String toStringCache;

    public ActionEngine(Module module, Class<?> controllerClass, Object controller, Method method) {
        this.module = module;
        this.controllerClass = controllerClass;
        this.controller = controller;
        this.method = method;
        this.interceptors = compileInterceptors();
        this.methodParameterResolver = compileParamResolvers();
        this.validators = compileValidators();
        this.paramExistenceChecker = compileParamExistenceChecker();
        HttpFeatures httpFeatures = method.getAnnotation(HttpFeatures.class);
        if (httpFeatures == null) {
            httpFeatures = this.controllerClass.getAnnotation(HttpFeatures.class);
        }
        this.httpFeatures = httpFeatures;
    }

    public InterceptorDelegate[] getRegisteredInterceptors() {
        return interceptors;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public Object getController() {
        return controller;
    }

    public Method getMethod() {
        return method;
    }

    public String[] getParameterNames() {
        return methodParameterResolver.getParameterNames();
    }

    private MethodParameterResolver compileParamResolvers() {
        ParameterNameDiscovererImpl parameterNameDiscoverer = new ParameterNameDiscovererImpl();
        ResolverFactoryImpl resolverFactory = new ResolverFactoryImpl();
        for (ParamResolver resolver : module.getCustomerResolvers()) {
            resolverFactory.addCustomerResolver(resolver);
        }
        return new MethodParameterResolver(this.controllerClass, method, parameterNameDiscoverer,
                resolverFactory);
    }

    @SuppressWarnings("unchecked")
    private ParamValidator[] compileValidators() {
        Class[] parameterTypes = method.getParameterTypes();
        List<ParamValidator> validators = module.getValidators();
        ParamValidator[] registeredValidators = new ParamValidator[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            for (ParamValidator validator : validators) {
                if (validator.supports(methodParameterResolver.getParamMetaDatas()[i])) {
                    registeredValidators[i] = validator;
                    break;
                }
            }
        }
        //
        return registeredValidators;
    }

    private InterceptorDelegate[] compileInterceptors() {
        List<InterceptorDelegate> interceptors = module.getInterceptors();
        List<InterceptorDelegate> registeredInterceptors = new ArrayList<InterceptorDelegate>(
                interceptors.size());
        for (InterceptorDelegate interceptor : interceptors) {

            ControllerInterceptor most = InterceptorDelegate.getMostInnerInterceptor(interceptor);

            if (!most.getClass().getName().startsWith("net.paoding.rose.web")) {

                // 获取@Intercepted注解 (@Intercepted注解配置于控制器或其方法中，决定一个拦截器是否应该拦截之。没有配置按“需要”处理)
                Intercepted intercepted = method.getAnnotation(Intercepted.class);
                if (intercepted == null) {
                    // 对于标注@Inherited的annotation，class.getAnnotation可以保证：如果本类没有，自动会从父类判断是否具有
                    intercepted = this.controllerClass.getAnnotation(Intercepted.class);
                }
                // 通过@Intercepted注解的allow和deny排除拦截器
                if (intercepted != null) {
                    // 3.1 先排除deny禁止的
                    if (RoseStringUtil.matches(intercepted.deny(), interceptor.getName())) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("action '" + controllerClass.getName() + "#"
                                    + method.getName()
                                    + "': remove interceptor by @Intercepted.deny: "
                                    + most.getClass().getName());
                        }
                        continue;
                    }
                    // 3.2 确认最大的allow允许
                    if (!RoseStringUtil.matches(intercepted.allow(), interceptor.getName())) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("action '" + controllerClass.getName() + "#"
                                    + method.getName()
                                    + "': remove interceptor by @Intercepted.allow: "
                                    + most.getClass().getName());
                        }
                        continue;
                    }
                }
            }
            // 取得拦截器同意后，注册到这个控制器方法中
            if (interceptor.isForAction(controllerClass, method)) {
                registeredInterceptors.add(interceptor);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("action '" + controllerClass.getName() + "#" + method.getName()
                            + "': remove interceptor by interceptor.isForAction: "
                            + most.getClass().getName());
                }
            }
        }
        //

        if (logger.isDebugEnabled()) {
            logger.debug("interceptors of " + controllerClass.getName() + "#" + method.getName()
                    + "=(" + registeredInterceptors.size() + "/" + interceptors.size() + ")"
                    + registeredInterceptors);
        }
        return registeredInterceptors
                .toArray(new InterceptorDelegate[registeredInterceptors.size()]);

    }

    /**
     * 用来抽象与{@link IfParamExists}相对应的判断逻辑
     * 
     * @author Li Weibo (weibo.leo@gmail.com)
     */
    private static interface ParamExistenceChecker {

        public int check(Map<String, String[]> params);
    }

    /**
     * 初始化的时候来决定所有的判断条件，并抽象为{@link ParamExistenceChecker}数组
     * 
     * @return
     */
    private ParamExistenceChecker[] compileParamExistenceChecker() {

        IfParamExists ifParamExists = method.getAnnotation(IfParamExists.class);
        //没标注IfParamExists或者标注了IfParamExists("")都认为不作检查
        if (ifParamExists == null || ifParamExists.value().trim().length() == 0) {
            return new ParamExistenceChecker[] {};
        }

        List<ParamExistenceChecker> checkers = new ArrayList<ParamExistenceChecker>(); //所有判断条件的列表
        String value = ifParamExists.value();

        //可以写多个判断条件，以这样的形式: type&subtype=value&anothername=value2
        String[] terms = StringUtils.split(value, "&");
        Assert.isTrue(terms.length >= 1); //这个应该永远成立

        //按'&'分割后，每一个term就是一个检查条件
        for (final String term : terms) {
            final int index = term.indexOf('='); //找'='
            if (index == -1) { //没有=说明只有参数名，此时term就是参数名
                checkers.add(new ParamExistenceChecker() {

                    final String paramName = term.trim();

                    @Override
                    public int check(Map<String, String[]> params) {
                        String[] paramValues = params.get(paramName);
                        if (logger.isDebugEnabled()) {
                            logger.debug(this.toString() + " is checking param:" + paramName + "="
                                    + Arrays.toString(paramValues));
                        }

                        //规则中没有约束参数值，所以只要存在就ok
                        if (paramValues != null && paramValues.length > 0) {
                            return 10;
                        } else {
                            return -1;
                        }
                    }
                });
            } else { //term中有'='

                final String paramName = term.substring(0, index).trim(); //参数名
                final String expected = term.substring(index + 1).trim(); //期望的参数值

                if (expected.startsWith(":")) { //expected是正则表达式
                    Pattern tmpPattern = null;
                    try {
                        tmpPattern = Pattern.compile(expected.substring(1));
                    } catch (PatternSyntaxException e) {
                        logger.error("@IfParamExists pattern error, " + controllerClass.getName()
                                + "#" + method.getName(), e);
                    }
                    final Pattern pattern = tmpPattern; //转成final的
                    checkers.add(new ParamExistenceChecker() {

                        @Override
                        public int check(Map<String, String[]> params) {
                            String[] paramValues = params.get(paramName);
                            if (logger.isDebugEnabled()) {
                                logger.debug(this.toString() + " is checking param:" + paramName
                                        + "=" + Arrays.toString(paramValues) + ", pattern="
                                        + pattern.pattern());
                            }
                            if (paramValues == null) { //参数值不能存在就不能通过
                                return -1;
                            }

                            for (String paramValue : paramValues) {
                                if (pattern != null && pattern.matcher(paramValue).matches()) {
                                    return 12;
                                }
                            }
                            return -1;
                        }
                    });
                } else { //expected是常量字符串，包括空串""
                    checkers.add(new ParamExistenceChecker() {

                        @Override
                        public int check(Map<String, String[]> params) {
                            String[] paramValues = params.get(paramName);
                            if (logger.isDebugEnabled()) {
                                logger.debug(this.toString() + " is checking param:" + paramName
                                        + "=" + Arrays.toString(paramValues) + ", expected="
                                        + expected);
                            }
                            if (paramValues == null) { //参数值不能存在就不能通过
                                return -1;
                            }

                            for (String paramValue : paramValues) {
                                if (expected.equals(paramValue)) {
                                    return 13;// 13优先于正则表达式的12
                                }
                            }
                            return -1;
                        }
                    });
                }
            }
        }
        return checkers.toArray(new ParamExistenceChecker[] {});
    }

    @Override
    public int isAccepted(HttpServletRequest request) {
        if (paramExistenceChecker.length == 0) { //没有约束条件，返回1
            return 1;
        }
        int total = 0;
        Map<String, String[]> params = resolveQueryString(request.getQueryString());
        for (ParamExistenceChecker checker : paramExistenceChecker) {
            int c = checker.check(params);
            if (c == -1) { //-1表示此约束条件未通过
                if (logger.isDebugEnabled()) {
                    logger.debug("Accepted check not passed by " + checker.toString());
                }
                return -1;
            }
            //FIXME 目前采用各个检查条件权值相加的办法来决定最终权值，
            //在权值相等的情况下，可能会有选举问题，需要更好的策略来取代
            total += c;
        }
        return total;
    }

    private Map<String, String[]> resolveQueryString(String queryString) {
        Map<String, String[]> params;
        if (queryString == null || queryString.length() == 0) {
            params = Collections.emptyMap();
        } else {
            params = new HashMap<String, String[]>();
            String[] kvs = queryString.split("&");
            for (String kv : kvs) {
                String[] pair = kv.split("=");
                if (pair.length == 2) {
                    mapPut(params, pair[0], pair[1]);
                } else if (pair.length == 1) {
                    mapPut(params, pair[0], "");
                } else {
                    logger.error("Illegal queryString:" + queryString);
                }
            }
        }
        return params;
    }

    private void mapPut(Map<String, String[]> map, String key, String value) {
        String[] values = map.get(key);
        if (values == null) {
            values = new String[] { value };
        } else {
            values = Arrays.copyOf(values, values.length + 1);
            values[values.length - 1] = value;
        }
        map.put(key, values);
    }

    @Override
    public Object execute(Rose rose) throws Throwable {
        try {
            return innerExecute(rose);
        } catch (Throwable local) {
            throw createException(rose, local);
        }
    }

    protected Object innerExecute(Rose rose) throws Throwable {
        Invocation inv = rose.getInvocation();

        // creates parameter binding result (not bean, just simple type, like int, Integer, int[] ...
        ParameterBindingResult paramBindingResult = new ParameterBindingResult(inv);
        String paramBindingResultName = MODEL_KEY_PREFIX + paramBindingResult.getObjectName();
        inv.addModel(paramBindingResultName, paramBindingResult);

        // resolves method parameters, adds the method parameters to model
        Object[] methodParameters = methodParameterResolver.resolve(inv, paramBindingResult);
        ((InvocationBean) inv).setMethodParameters(methodParameters);
        String[] parameterNames = methodParameterResolver.getParameterNames();

        Object instruction = null;

        ParamMetaData[] metaDatas = methodParameterResolver.getParamMetaDatas();
        // validators
        for (int i = 0; i < this.validators.length; i++) {
            if (validators[i] != null && !(methodParameters[i] instanceof Errors)) {
                Errors errors = inv.getBindingResult(parameterNames[i]);
                instruction = validators[i].validate(//
                        metaDatas[i], inv, methodParameters[i], errors);
                if (logger.isDebugEnabled()) {
                    logger.debug("do validate [" + validators[i].getClass().getName()
                            + "] and return '" + instruction + "'");
                }
                // 如果返回的instruction不是null、boolean或空串==>杯具：流程到此为止！
                if (instruction != null) {
                    if (instruction instanceof Boolean) {
                        continue;
                    }
                    if (instruction instanceof String && ((String) instruction).length() == 0) {
                        continue;
                    }
                    return instruction;
                }
            }
        }
        
        //
        for (int i = 0; i < parameterNames.length; i++) {
            if (parameterNames[i] != null && methodParameters[i] != null
                    && inv.getModel().get(parameterNames[i]) != methodParameters[i]) {
                inv.addModel(parameterNames[i], methodParameters[i]);
            }
        }

        // intetceptors & controller
        return new InvocationChainImpl(rose).doNext();
    }

    private class InvocationChainImpl implements InvocationChain {

        private final boolean debugEnabled = logger.isDebugEnabled();

        private int index = -1;

        private final Rose rose;

        private Object instruction;

        public InvocationChainImpl(Rose rose) {
            this.rose = rose;
        }

        @Override
        public Object doNext() throws Exception {
            if (++index < interceptors.length) { // ++index 用于将-1转化为0
                InterceptorDelegate interceptor = interceptors[index];
                //
                rose.addAfterCompletion(interceptor);
                Object instruction = interceptor.roundInvocation(rose.getInvocation(), this);
                //
                if (debugEnabled) {
                    logger.debug("interceptor[" + interceptor.getName() + "] do round and return '"
                            + instruction + "'");
                }

                // 拦截器返回null的，要恢复为原instruction
                // 这个功能非常有用!!
                if (instruction != null) {
                    this.instruction = instruction;
                }
                return this.instruction;
            } else if (index == interceptors.length) {
                // applies http features before the resolvers
                if (httpFeatures != null) {
                    applyHttpFeatures(rose.getInvocation());
                }

                this.instruction = method.invoke(controller, rose.getInvocation()
                        .getMethodParameters());

                // @Return
                if (this.instruction == null) {
                    Return returnAnnotation = method.getAnnotation(Return.class);
                    if (returnAnnotation != null) {
                        this.instruction = returnAnnotation.value();
                    }
                }
                return this.instruction;
            }
            throw new IndexOutOfBoundsException(
                    "don't call twice 'chain.doNext()' in one intercpetor; index=" + index
                            + "; interceptors.length=" + interceptors.length);
        }

    }

    private Exception createException(Rose rose, Throwable exception) {
        final RequestPath requestPath = rose.getInvocation().getRequestPath();
        StringBuilder sb = new StringBuilder(1024);
        sb.append("[Rose-").append(RoseVersion.getVersion()).append("@Spring-").append(
                SpringVersion.getVersion());
        sb.append("]Error happended: ").append(requestPath.getMethod());
        sb.append(" ").append(requestPath.getUri());
        sb.append("->");
        sb.append(this).append(" params=");
        sb.append(Arrays.toString(rose.getInvocation().getMethodParameters()));
        InvocationTargetException servletException = new InvocationTargetException(exception, sb
                .toString());
        return servletException;
    }

    private void applyHttpFeatures(final Invocation inv) throws UnsupportedEncodingException {
        HttpServletResponse response = inv.getResponse();
        if (StringUtils.isNotBlank(httpFeatures.charset())) {
            response.setCharacterEncoding(httpFeatures.charset());
            if (logger.isDebugEnabled()) {
                logger.debug("set response.characterEncoding by HttpFeatures:"
                        + httpFeatures.charset());
            }
        }
        if (StringUtils.isNotBlank(httpFeatures.contentType())) {
            String contentType = httpFeatures.contentType();
            if (contentType.equals("json")) {
                contentType = "application/json";
            } else if (contentType.equals("xml")) {
                contentType = "text/xml";
            } else if (contentType.equals("html")) {
                contentType = "text/html";
            } else if (contentType.equals("plain") || contentType.equals("text")) {
                contentType = "text/plain";
            }
            response.setContentType(contentType);
            if (logger.isDebugEnabled()) {
                logger.debug("set response.contentType by HttpFeatures:"
                        + response.getContentType());
            }
        }
    }

    @Override
    public String toString() {
        if (toStringCache == null) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            String appPackageName = this.controllerClass.getPackage().getName();
            if (appPackageName.indexOf('.') != -1) {
                appPackageName = appPackageName.substring(0, appPackageName.lastIndexOf('.'));
            }
            String methodParamNames = "";
            for (int i = 0; i < parameterTypes.length; i++) {
                if (methodParamNames.length() == 0) {
                    methodParamNames = showSimpleName(parameterTypes[i], appPackageName);
                } else {
                    methodParamNames = methodParamNames + ", "
                            + showSimpleName(parameterTypes[i], appPackageName);
                }
            }
            toStringCache = ""//
                    + showSimpleName(method.getReturnType(), appPackageName)
                    + " "
                    + method.getName() //
                    + "(" + methodParamNames + ")";
        }
        return toStringCache;
    }

    private String showSimpleName(Class<?> parameterType, String appPackageName) {
        if (parameterType.getName().startsWith("net.paoding")
                || parameterType.getName().startsWith("java.lang")
                || parameterType.getName().startsWith("java.util")
                || parameterType.getName().startsWith(appPackageName)) {
            return parameterType.getSimpleName();
        }
        return parameterType.getName();
    }

    public void destroy() {

    }
}
