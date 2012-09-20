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
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.impl.thread.AfterCompletion;
import net.paoding.rose.web.paramresolver.ParamMetaData;
import net.paoding.rose.web.paramresolver.ParamResolver;
import net.paoding.rose.web.var.Flash;
import net.paoding.rose.web.var.Model;

import org.springframework.validation.BindingResult;
import org.springframework.web.context.WebApplicationContext;

/**
 * {@link Invocation} 封装框架对控制器方法的一次调用相关的信息：请求和响应对象、目标控制器方法、方法参数值等。
 * <p>
 * 可以在控制器方法参数类似如下声明这个调用对象，获取之:<br>
 * <code>
 * public String get(Invocation inv) {
 *     return &quot;@hello, get!&quot;;
 * }
 * </code>
 * <p>
 * 
 * 当不存在请求转发，一个用户请求只存在一个调用实例；当用户请求可能被转发时，转发前和转发后的控制器方法调用是不同的调用，
 * 则一个用户请求将不只包含了一个调用实例。可以通过 {@link Invocation#getPreInvocation()}
 * 获取该调用inv的前一个调用preinv。
 * <p>
 * 
 * 在参数解析器{@link ParamResolver}、验证器{@link ParamValidator}、拦截器
 * {@link ControllerInterceptor} 的接口方法实现中可以得到当前调用inv实例，使可以获取此此调用的信息进行控制。
 * <p>
 * 
 * 如果需要在参数验证器、拦截器、控制器之间传递仅在本次调用可见、和视图渲染无关的一些参数时，请使用
 * {@link #setAttribute(String, Object)}和 {@link #getAttribute(String)}
 * 方法。如果某个数据要渲染发送给客户端，请使用 {@link #addModel(String, Object)}。
 * <p>
 * 
 * {@link #addModel(String, Object)}和{@link #setAttribute(String, Object)}
 * 的区别:
 * 
 * <ul>
 * <li>他们各自采用独立容器存储数据，互相不混淆。</li>
 * <li>addModel的数据可以被后续的调用的getModel方法“看见”，而setAttribute的数据不能被后面调用的
 * getAttribute方法“看见”</li>
 * </ul>
 * <p>
 * 
 * WIKI上的说明 <a href=
 * "http://code.google.com/p/paoding-rose/wiki/Rose_Guide_Invocation"
 * >http://code.google.com/p/paoding-rose/wiki/Rose_Guide_Invocation</a>
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface Invocation {

    /**
     * 返回rose对本次请求的地址、方法的信息
     * <p>
     * 比如你想得到本次调用的 URL，通过 {@link RequestPath#getUri()} 将是最准确的，而非
     * {@link HttpServletRequest#getRequestURI()}。
     * 
     * @return
     */
    public RequestPath getRequestPath();

    /**
     * 返回本次调用的 {@link HttpServletRequest}对象
     * 
     * @return
     */
    public HttpServletRequest getRequest();

    /**
     * 返回本次调用的 {@link HttpServletResponse}对象
     * 
     * @return
     */
    public HttpServletResponse getResponse();

    /**
     * 更改本次调用的请求对象
     * 
     * @param request
     */
    public void setRequest(HttpServletRequest request);

    /**
     * 本次调用的目标控制器对象。
     * <p>
     * (可能已经是一个Proxy、CGlib等等后的对象，不是原控制器的直接实例对象)
     * 
     * @return
     */
    public Object getController();

    /**
     * 本次调用的控制器的类名，这个类名就是编写控制器类时的那个类。
     * <p>
     * 他不总是和本次调用的控制器对象的getClass()相同。
     * 
     * @return
     */
    public Class<?> getControllerClass();

    /**
     * 本次调用的目标控制器方法
     * 
     * @return
     */
    public Method getMethod();

    /**
     * 在该调用所在方法、控制器上上是否标注了该注解对象，如果没有返回false
     * 
     * @param annotationClass
     * @return
     */
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass);

    /**
     * 获取标注在该调用所在方法、控制器上的注解对象，如果没有返回null
     * 
     * @param <T>
     * @param annotationClass
     * @return
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass);

    /**
     * 本地调用的目标控制器方法中，各参数的名字。
     * <p>
     * 这个名字不是程序开发中使用的变量名，而是通过 @Param
     * 声明的名字，比如@Param("userId")用来声明某个参数的名字是userId。
     * <p>
     * 建议，你把参数名和变量定义为一样！
     * 
     * <p>
     * 本法方法返回的字符串数组长度和目标控制器方法的参数个数相同，元素的值在下列情况为空：
     * <ul>
     * <li>没有标注@Param的基本类型以及封装类型</li>
     * <li>没有标注@Param的这些类型：时间类型、Map、Collection类型等</li>
     * </ul>
     * 对用户对应的实体Bean，一般没有标注@Param，他的参数名即是该实体类名的首字母小写化字符串。<br>
     * (额外注意：通过@Param标注一个实体类，
     * 不仅仅改变了默认的参数名，同时也将改变数据绑定规则，即使通过@Param标注的名字和默认的一样)。
     * 
     * @see Param
     * @return
     */
    public String[] getMethodParameterNames();

    /**
     * 本次调用的控制器方法所使用的参数值。
     * <p>
     * 请谨慎对待这个方法的返回值，如果在拦截器等组件中改变这个数组的元素值，表示要使用所设置数据代替原来的数据去调用控制器方法。
     * 
     * @return
     */
    public Object[] getMethodParameters();

    /**
     * 获取在请求查询串(即问号后的xx=yyy)、POST中Body中、URI所带的参数值。
     * <p>
     * URI中的参数需要通过在控制器方法中通过类似@Path("user_{name}")进行声明，才可以获取name的参数<br>
     * 同时因为 Rose 对 {@link HttpServletRequest}
     * 进行了封装，使得其request的getParameter和inv的getParameter的语义相同。
     * 
     * @param name
     * @return
     */
    public String getParameter(String name);

    /**
     * 返回给定名字的方法参数。
     * <p>
     * 
     * @return
     */
    public Object getMethodParameter(String name);

    /**
     * 改变调用的方法参数值
     * 
     * @param index
     * @param newValue
     */
    public void changeMethodParameter(int index, Object newValue);

    /**
     * 改变调用的方法参数值
     * 
     * @param name
     * @param newValue
     */
    public void changeMethodParameter(String name, Object newValue);

    /**
     * 
     * @param paramMeta
     * @param newValue
     */
    public void changeMethodParameter(ParamMetaData paramMeta, Object newValue);

    /**
     * 将对象(object,array,collection等)加入到MVC中的Model中作为一个属性，通过它传递给View
     * <p>
     * 将使用该对象的类名头字母小写的字符串作为名字；<br>
     * 如果对象是数组，去数组元素的类的类名字头字母小写加上"List"作为名字<br>
     * 如果对象是集合元素，取其第一个元素的类的类名字头字母小写加上"List"作为名字<br>
     * 如果该值为空或者其集合长度为0的话，将被忽略<br>
     * 
     * @param value 可以是普通对象，数组对象，集合对象；<br>
     *        可以为null，如果对象为null或集合长度为0直接被忽略掉
     * @see Model#add(Object)
     * @see #getModel()
     */
    public void addModel(Object value);

    /**
     * 将对象(object,array,collection等)加入到MVC中的Model中作为一个属性，通过它传递给View
     * 
     * @param name 在view中这个字符串将作为该对象的名字被使用；非空
     * @param value 可以是普通对象，数组对象，集合对象；<br>
     *        可以为null，如果对象为null直接被忽略掉
     * @see Model#add(String, Object)
     * @see #getModel()
     */
    public void addModel(String name, Object value);

    /**
     * 获取添加到model中的对象
     * 
     * @param name
     * @return
     */
    public Object getModel(String name);

    /**
     * 返回Model接口，通过这个设置对象给view渲染
     * 
     * @return
     */
    public Model getModel();

    /**
     * 设置一个和本次调用关联的属性。这个属性可以在多个拦截器中共享。
     * <p>
     * 因为所设置的属性值和本次调用有关，所以他与 {@link #getRequest()#setAttribute(String,
     * Object)}是不相同的。
     * 
     * @param name
     * @param value
     * @return
     */
    public Invocation setAttribute(String name, Object value);

    /**
     * 获取前面拦截器或代码设置的，和本次调用相关的属性
     * 
     * @param name
     * @return
     */
    public Object getAttribute(String name);

    /**
     * 删除inv的某一个属性
     * 
     * @param name
     */
    public void removeAttribute(String name);

    /**
     * 返回本次调用相关的所有属性名字
     * 
     * @return
     */
    public Set<String> getAttributeNames();

    /**
     * 用于向重定向跳转后的页面传递参数，比如提示信息
     * 
     * @param name
     * @param msg
     */
    public void addFlash(String name, String msg);

    /**
     * 返回本次请求附带的Flash信息，如果上次请求的响应中没有往用户端写入Flash信息，仍旧会返回一个非空的Flash对象，
     * 只是里面的数据将为空。
     * 
     * @return
     */
    public Flash getFlash();

    /**
     * 返回本次请求附带的Flash信息。
     * <p>
     * 如果之前没有获取过这个Flash信息，且create参数为false则返回null，否则创建Flash对象，并填充可能的Flash信息。
     * 
     * @param create
     * @return
     */
    public Flash getFlash(boolean create);

    /**
     * 返回本次调用控制器所在模块的 {@link WebApplicationContext} 对象
     * 
     * @return
     */
    public WebApplicationContext getApplicationContext();

    /**
     * 返回 {@link ServletContext} 对象
     * 
     * @return
     */
    public ServletContext getServletContext();

    /**
     * 返回所有参数绑定名
     * 
     * @return
     */
    public List<String> getBindingResultNames();

    /**
     * 返回所有参数绑定结果对象
     * 
     * @return
     */
    public List<BindingResult> getBindingResults();

    /**
     * 获取控制器方法普通参数的绑定结果
     * 
     * @return
     * @throws NullPointerException 如果当前线程中没有绑定请求对象时
     */
    public BindingResult getParameterBindingResult();

    /**
     * 获取控制器方法各个bean的绑定结果
     * 
     * @param bean bean实体对象或bindingResult的名字
     * @return
     * @throws NullPointerException 如果当前线程中没有绑定请求对象时
     */
    public BindingResult getBindingResult(String bean);

    /**
     * 获取前一个Invocation对象，如果没有返回null
     * 
     * @return
     */
    public Invocation getPreInvocation();

    /**
     * 获取最开头的Invocation对象，可能是自己本身
     * 
     * @return
     */
    public Invocation getHeadInvocation();

    /**
     * 返回类似 /user/{userId} 这样的字符串
     * 
     * @return
     */
    public String getResourceId();

    /**
     * <p>
     * 通过这个方法，可以在拦截器、控制器等能够拿到inv的地方设置一个“资源回收计划”。
     * 在整个页面渲染结束时，或者因为异常导致流程中断时，所设置的“资源回收计划”能够被执行。
     * 
     * <p>
     * 注意：越先加入的afterComletion对象，越靠后执行。
     * 
     * @param afterComletion
     */
    public void addAfterCompletion(AfterCompletion afterComletion);

}
