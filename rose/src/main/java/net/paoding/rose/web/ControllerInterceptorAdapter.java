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
package net.paoding.rose.web;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import net.paoding.rose.web.advancedinterceptor.ActionSelector;
import net.paoding.rose.web.advancedinterceptor.DispatcherSelector;
import net.paoding.rose.web.advancedinterceptor.Named;
import net.paoding.rose.web.advancedinterceptor.Ordered;
import net.paoding.rose.web.impl.thread.AfterCompletion;
import net.paoding.rose.web.instruction.Instruction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class ControllerInterceptorAdapter implements Named, Ordered, ControllerInterceptor,
        AfterCompletion, ActionSelector, DispatcherSelector {

    protected Log logger = LogFactory.getLog(getClass());

    protected String name;

    protected int priority;

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public final boolean isForAction(Class<?> controllerClazz, Method actionMethod) {
        // 返回false，表示控制器或其方法没有标注本拦截器所要求的注解
        if (!checkRequiredAnnotations(controllerClazz, actionMethod)) {
            return false;
        }
        // 返回true，表示控制器或其方法标注了“拒绝”注解
        if (checkDenyAnnotations(controllerClazz, actionMethod)) {
            return false;
        }
        return isForAction(actionMethod, controllerClazz);
    }

    protected boolean isForAction(Method actionMethod, Class<?> controllerClazz) {
        return true;
    }

    @Override
    public boolean isForDispatcher(Dispatcher dispatcher) {
        return true;
    }

    @Override
    public final Object roundInvocation(Invocation inv, InvocationChain chain) throws Exception {
        // before
        Object instruction = this.before(inv);

        // break the invocation?
        if (instruction != null && !Boolean.TRUE.equals(instruction)) {

            // if false, don't render anything
            if (Boolean.FALSE.equals(instruction)) {
                instruction = null;
            }
            return instruction; // break  and return
        }

        // next
        instruction = round(inv, chain);

        // after
        return after(inv, instruction);
    }

    /**
     * 在调用控制器方法前调用。如果返回true(或者null)表示继续下一个拦截器；<br>
     * 返回其他的表示不再调用剩余的拦截器以及action，并按返回的指示执行结果(进行页面渲染或其他 )
     * {@link Instruction}向请求发送响应(可以是用字符串表示的指示对象)。
     * <p>
     * 在一个拦截器链条中，如果某一个拦截器拒绝了整个调用链条，其它还没拦截的拦截器将不再会进行拦截，但是之前已经拦截过的拦截器，
     * 还将分别调用它们的{@link #after(Invocation, Object)}和
     * {@link #afterCompletion(Invocation, Throwable)}方法拦截。
     * <p>
     * 
     * @param inv
     * @return
     * @throws Exception
     */
    protected Object before(Invocation inv) throws Exception {
        return Boolean.TRUE;
    }

    /**
     * 
     * @param inv
     * @param chain
     * @return
     * @throws Exception
     */
    protected Object round(Invocation inv, InvocationChain chain) throws Exception {
        return chain.doNext();
    }

    /**
     * 在调用控制器方法后调用。也有可能是之后拦截器拒绝了该流程，回退过来调用到先前调用的拦截器的本方法。
     * <p>
     * 返回null或原来的instruction表示不改变控制器的返回结果。<br>
     * 返回另外的对象表示改变这个返回行为。这非常有用，或许通过拦截器能够将一个返回的对象转化为另外的对象以输出给请求着
     * 
     * @param inv
     * @param instruction
     * @return
     * @throws Exception
     */
    protected Object after(Invocation inv, Object instruction) throws Exception {
        return instruction;
    }

    @Override
    public void afterCompletion(Invocation inv, Throwable ex) throws Exception {
    }

    @Override
    public String toString() {
        return getName();
    }

    protected static ListBuilder createList(int size) {
        return new ListBuilder(size);
    }

    public static class ListBuilder {

        List<Class<? extends Annotation>> list;

        ListBuilder(int size) {
            list = new ArrayList<Class<? extends Annotation>>(size);
        }

        public ListBuilder add(Class<? extends Annotation> a) {
            list.add(a);
            return this;
        }

        public List<Class<? extends Annotation>> getList() {
            return list;
        }
    }

    /**
     * 返回false，表示控制器或其方法没有标注本拦截器所要求的注解
     * 
     * @param controllerClazz 控制器类
     * @param actionMethod 控制器处理方法
     * @return
     */
    protected final boolean checkRequiredAnnotations(Class<?> controllerClazz, Method actionMethod) {
        List<Class<? extends Annotation>> requiredAnnotations = getRequiredAnnotationClasses();
        if (requiredAnnotations == null || requiredAnnotations.size() == 0) {
            return true;
        }
        for (Class<? extends Annotation> requiredAnnotation : requiredAnnotations) {
            if (requiredAnnotation == null) {
                continue;
            }
            BitSet scopeSet = getAnnotationScope(requiredAnnotation);
            if (scopeSet.get(AnnotationScope.METHOD.ordinal())) {
                if (actionMethod.isAnnotationPresent(requiredAnnotation)) {
                    return checkAnnotation(actionMethod.getAnnotation(requiredAnnotation));
                }
            }
            if (scopeSet.get(AnnotationScope.CLASS.ordinal())) {
                if (controllerClazz.isAnnotationPresent(requiredAnnotation)) {
                    return checkAnnotation(actionMethod.getAnnotation(requiredAnnotation));
                }
            }
            if (scopeSet.get(AnnotationScope.ANNOTATION.ordinal())) {
                for (Annotation annotation : actionMethod.getAnnotations()) {
                    if (annotation.annotationType().isAnnotationPresent(requiredAnnotation)) {
                        return checkAnnotation(actionMethod.getAnnotation(requiredAnnotation));
                    }
                }
                for (Annotation annotation : controllerClazz.getAnnotations()) {
                    if (annotation.annotationType().isAnnotationPresent(requiredAnnotation)) {
                        return checkAnnotation(actionMethod.getAnnotation(requiredAnnotation));
                    }
                }
            }
        }
        return false;
    }

    /**
     * 标注的作用范围: METHOD(方法), CLASS(类), ANNOTATION(被标注的标注)。
     */
    public static enum AnnotationScope {
        METHOD, CLASS, ANNOTATION;

        public static BitSet getMethodAndClassScope() {
            BitSet bitSet = new BitSet();
            bitSet.set(METHOD.ordinal());
            bitSet.set(CLASS.ordinal());
            return bitSet;
        }

        public static BitSet getMethodAndClassAndAnnotationScope() {
            BitSet bitSet = new BitSet();
            bitSet.set(METHOD.ordinal());
            bitSet.set(CLASS.ordinal());
            bitSet.set(ANNOTATION.ordinal());
            return bitSet;
        }
    }

    /**
     * 用 BitSet 的形式返回标注的作用域。
     * 
     * @param annotationType
     * @return
     */
    protected BitSet getAnnotationScope(Class<? extends Annotation> annotationType) {
        return AnnotationScope.getMethodAndClassScope();
    }

    /**
     * 返回true，表示控制器或其方法标注了“拒绝”注解
     * 
     * @param controllerClazz
     * @param actionMethod
     * @return
     */
    protected final boolean checkDenyAnnotations(Class<?> controllerClazz, Method actionMethod) {
        List<Class<? extends Annotation>> denyAnnotations = getDenyAnnotationClasses();
        if (denyAnnotations == null || denyAnnotations.size() == 0) {
            return false;
        }
        for (Class<? extends Annotation> denyAnnotation : denyAnnotations) {
            if (denyAnnotation == null) {
                continue;
            }
            BitSet scopeSet = getAnnotationScope(denyAnnotation);
            if (scopeSet.get(AnnotationScope.METHOD.ordinal())) {
                if (actionMethod.isAnnotationPresent(denyAnnotation)) {
                    return checkAnnotation(actionMethod.getAnnotation(denyAnnotation));
                }
            }
            if (scopeSet.get(AnnotationScope.CLASS.ordinal())) {
                if (controllerClazz.isAnnotationPresent(denyAnnotation)) {
                    return checkAnnotation(actionMethod.getAnnotation(denyAnnotation));
                }
            }
            if (scopeSet.get(AnnotationScope.ANNOTATION.ordinal())) {
                for (Annotation annotation : actionMethod.getAnnotations()) {
                    if (annotation.annotationType().isAnnotationPresent(denyAnnotation)) {
                        return checkAnnotation(actionMethod.getAnnotation(denyAnnotation));
                    }
                }
                for (Annotation annotation : controllerClazz.getAnnotations()) {
                    if (annotation.annotationType().isAnnotationPresent(denyAnnotation)) {
                        return checkAnnotation(actionMethod.getAnnotation(denyAnnotation));
                    }
                }
            }
        }
        return false;
    }

    /**
     * 根据此方法的返回结果，{@link #checkRequiredAnnotations(Class, Method)}/
     * {@link #checkDenyAnnotations(Class, Method)}
     * 在判断到该annotation时，返回true/false；<br>
     * 
     * 
     * 
     * 
     * @param annotation
     * @return
     */
    protected boolean checkAnnotation(Annotation annotation) {
        return true;
    }

    protected List<Class<? extends Annotation>> getRequiredAnnotationClasses() {
        Class<? extends Annotation> clazz = getRequiredAnnotationClass();
        if (clazz != null) {
            List<Class<? extends Annotation>> list = new ArrayList<Class<? extends Annotation>>(2);
            list.add(clazz);
            return list;
        }
        return Collections.emptyList();
    }

    protected Class<? extends Annotation> getRequiredAnnotationClass() {
        return null;
    }

    protected List<Class<? extends Annotation>> getDenyAnnotationClasses() {
        Class<? extends Annotation> clazz = getDenyAnnotationClass();
        if (clazz != null) {
            List<Class<? extends Annotation>> list = new ArrayList<Class<? extends Annotation>>();
            list.add(clazz);
            return list;
        }
        return Collections.emptyList();
    }

    protected Class<? extends Annotation> getDenyAnnotationClass() {
        return null;
    }

}
