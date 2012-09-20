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
package net.paoding.rose.web.portal;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.paramresolver.ParamResolver;
import net.paoding.rose.web.portal.impl.PipeResolver;
import net.paoding.rose.web.portal.impl.PortalResolver;

/**
 * {@link WindowContainer} 是 {@link Window} 的容器，由多个 Window 对象组成，一旦把
 * {@link Window} 放到 {@link WindowContainer} 中，那么每个 Window 的执行将具有独立性和并发性。
 * <p>
 * 
 * {@link WindowContainer} 有两种的不同子类型: {@link Portal} 和 {@link Pipe}
 * ，在实际编程时候，您应该使用两种子类型的其中一种，不要直接使用 {@link WindowContainer}。
 * <p>
 * <strong>*编程接口*</strong>
 * 
 * <pre>
 * 1、在控制器方法上声明一个Portal或Pipe参数，比如public String index(Portal portal)、public String home(Pipe pipe);
 * 2、portal/pipe参数和Invocation等其他各种参数可以同时存在，portal和pipe也可以同时存在;
 * 3、在方法中，你可以不断地调用 {@link #addWindow(String name, String path)} 方法增加给定地址的窗口；
 * </pre>
 * <p>
 * 
 * <strong>*rose和portal/pipe的关系*</strong>
 * 
 * <pre>
 * 1、rose是一个应用于web开发框架，portal/pipe不是rose的核心，只是rose的一个插件；
 * 2、如果你不需要portal/pipe特性时，您可以把paoding-rose-portal的jar包移走，而不会影响普通rose程序；
 * 3、portal/pipe使用rose开放出来的spring“配置文件”插入到rose框架中，使得portal/pipe可以在rose的程序中使用；
 * </pre>
 * 
 * <strong>*portal/pipe的创建*</strong>
 * 
 * <pre>
 * 1、portal/pipe参数由框架完成创建，你只需要将Portal声明为方法的参数即可；
 * 2、rose框架提供了 {@link ParamResolver}接口，portal/pipe提供了该接口的实现 {@link PortalResolver} / {@link PipeResolver}，并配置到 jar 包中的 applicatonContext*.xml，使得rose框架能够识别
 * 3、对于portal，虽然一个portal参数的生命周期直到页面渲染结束来完成，但如果您在多个控制器方法中声明Portal参数，请求在这些方法之间转发，这些Portal是不同的对象。
 * 4、对于pipe，一次用户请求可以在多个控制器方法之间转发，但在整个转发链条中只能存现一次pipe参数；
 * </pre>
 * 
 * 
 * <strong>*关于window的执行*</strong>
 * 
 * <pre>
 * 1、当一个window加入到portal/pipe时，portal/pipe便会调用每个web应用唯一的executorService执行该window，不同的portal/pipe调用的都是同一个executorService;
 * 2、由于executorService使用的是线程池实现，所以window的执行不由web容器“主线程”执行(通常其线程名以http开始)，而是由名称以portalExecutor开头的线程执行;
 * 3、在产品环境下，window的执行时候都会长于portal/pipe控制器本身的执行时间，portal/pipe需要这协调这些时间关系;
 * 4、如果您使用的是portal，框架会保证让主控制器等待所有窗口流程都执行完毕之后才向客户端吐页面内容；
 * 5、如果您使用的是pipe，框架会把主控制器所返回的整体页面框架先输出给客户端，并保持连接不断开，当某个窗口的流程执行完毕，框架再将该窗口返回的页面数据通过那个没有断开的连接吐给客户端；
 * 
 * <pre>
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface WindowContainer {

    /**
     * 返回创建此对象时的 {@link Invocation} 对象
     * <p>
     * 等价于您在控制器方法参数声明的Invocation inv。
     * 
     * @return
     */
    public Invocation getInvocation();

    /**
     * 请您调用: {@link #getInvocation()#addModel(String, Object)} 来完成
     * 现在是2010-08-04，2010国庆后将去掉此代码
     */
    @Deprecated
    public void addModel(String name, Object value);

    /**
     * 返回创建此对象时的 {@link HttpServletRequest}对象，等价于
     * {@link Portal#getInvocation()#getRequest()}
     * 
     * @return
     */
    public HttpServletRequest getRequest();

    /**
     * 返回创建此对象时的 {@link HttpServletResponse}对象，等价于
     * {@link Portal#getInvocation()#getResponse()}
     * 
     * @return
     */
    public HttpServletResponse getResponse();

    /**
     * 设置自定义的窗口渲染器，决定在给定的portal页面位置中，如何输出什么window代表的内容，比如嵌入在一个 &lt;div
     * class="window"&gt;&lt;/div&gt;的块中。
     * <p>
     * 如果没有设置窗口渲染器：<br>
     * portal会将window的内容原封不动地输出到portal页面的指定位置中；<br>
     * pipe则会将窗口的内容转化为符合rosepipe.js规范的一个script串
     * 
     * @param render
     */
    public void setWindowRender(WindowRender render);

    /**
     * 返回设置的窗口渲染器
     * 
     * @return
     */
    public WindowRender getWindowRender();

    /**
     * 设置超时时间。
     * <p>
     * <h1>对于portal而言:</h1><br>
     * 这个时间指的是 portal 控制器返回之后(渲染页面之前)，portal线程等待所有窗口流程执行完毕的最大时间。
     * 如果等待超过了这个时间，那些还未能执行完的window将不再被等待，执行后续的流程，向客户端输出内容；
     * <p>
     * 
     * <h1>对于pipe而言:</h1> <br>
     * 这个时间是指pipe主控制器向客户端响应主页面后（主页面已经渲染完毕），pipe线程等待所有窗口流程执行完毕的最大时间。
     * 
     * <p>
     * 如果portal/pipe认为一个window超时了，默认将会去 cancle 它，除非您设置了该window的
     * {@link Window#FUTURE_CANCEL_ENABLE_ATTR}
     * 为false，您要确切了解您的系统，看是否支持cancle
     * ，也就是对线程中断的响应策略(如果中断window会导致整个系统的基础设施被破坏，建议您addWindow时候，
     * 利用callback把window的FUTURE_CANCEL_ENABLE_ATTR属性设置为Boolean
     * .FALSE或者字符串"false"
     * 
     * @param timeoutInMillis 毫秒，小于或等于0表示不进行超时判断(这也是默认设置)，
     *        portal将等待所有窗口执行完毕或被取消才最终渲染页面给用户
     */
    public void setTimeout(long timeoutInMills);

    /**
     * 返回设置的超时时间
     * 
     * @return
     */
    public long getTimeout();

    /**
     * 为这个portal/pipe实例注册一个侦听器
     * 
     * @param l 如果为null则进行忽略
     */
    public void addListener(WindowListener l);

    /**
     * 增加一个窗口到本容器中，并立即请求另外的线程中执行该窗口。
     * <p>
     * <h1>对于portal而言:</h1><br>
     * 您可以在portal控制器返回的页面中使用 ${name}、${requestScope.name} 渲染该窗口内容，
     * ${name}实际是 {@link Window}对象， {@link Window#toString()} 可返回该窗口的渲染结果。
     * 
     * <h1>对于pipe而言:</h1><br>
     * pipe返回的script将含有一个id属性，该属性就是指window的name名称；通过这个id，
     * script把窗口的文本设置到页面相应的元素中。
     * 
     * @param name 窗口的名字
     * @param windowPath 这个参数表示窗口的地址，可以包含参数比如xxx?a=b; <br>
     *        如果地址以'/'开始表示相对于该web应用的根路径(注意：这里是指该应用的根路径，而非跳脱之外的host:
     *        port下的根路径)<br>
     *        如果地址不一'/'开始表示相对当前的请求的URI地址<br>
     *        这个地址<strong>*不能*</strong>是其他远程地址，比如http://host:port/somepath
     * @return
     */
    public Window addWindow(String name, String windowPath);

    /**
     * 增加一个窗口到本容器中，并立即请求另外的线程中执行该窗口。
     * <p>
     * <h1>对于portal而言:</h1><br>
     * 您可以在portal控制器返回的页面中使用 ${name}、${requestScope.name} 渲染该窗口内容，
     * ${name}实际是 {@link Window}对象， {@link Window#toString()} 可返回该窗口的渲染结果。
     * 
     * <h1>对于pipe而言:</h1><br>
     * pipe返回的script将含有一个id属性，该属性就是指window的name名称；通过这个id，
     * script把窗口的文本设置到页面相应的元素中。
     * 
     * @param name 窗口的名字
     * @param windowPath 这个参数表示窗口的地址，可以包含参数比如xxx?a=b; <br>
     *        如果地址以'/'开始表示相对于该web应用的根路径(注意：这里是指该应用的根路径，而非跳脱之外的host:
     *        port下的根路径)<br>
     *        如果地址不一'/'开始表示相对当前的请求的URI地址<br>
     *        这个地址<strong>*不能*</strong>是其他远程地址，比如http://host:port/somepath
     * @param attributes 在window未执行之前设置给这个window的属性，可以为null
     * @return
     */
    public Window addWindow(String name, String windowPath, Map<String, Object> attributes);

    /**
     * 增加一个窗口到本容器中，并立即请求另外的线程中执行该窗口。
     * <p>
     * <h1>对于portal而言:</h1><br>
     * 您可以在portal控制器返回的页面中使用 ${name}、${requestScope.name} 渲染该窗口内容，
     * ${name}实际是 {@link Window}对象， {@link Window#toString()} 可返回该窗口的渲染结果。
     * 
     * <h1>对于pipe而言:</h1><br>
     * pipe返回的script将含有一个id属性，该属性就是指window的name名称；通过这个id，
     * script把窗口的文本设置到页面相应的元素中。
     * 
     * @param name 窗口的名字
     * @param windowPath 这个参数表示窗口的地址，可以包含参数比如xxx?a=b; <br>
     *        如果地址以'/'开始表示相对于该web应用的根路径(注意：这里是指该应用的根路径，而非跳脱之外的host:
     *        port下的根路径)<br>
     *        如果地址不一'/'开始表示相对当前的请求的URI地址<br>
     *        这个地址<strong>*不能*</strong>是其他远程地址，比如http://host:port/somepath
     * @param callback 在window创建后未被执行前的一些回调
     * @return
     */
    public Window addWindow(String name, String windowPath, WindowCallback callback);

    /**
     * 返回添加到这个容器上的窗口，如果没有任何窗口，则返回一个size=0的列表；
     * 返回的列表包含也包含哪些超时还未执行完毕的窗口，你可以通过window的success属性
     * (isSuccess()方法)判断是否完整执行了！
     * <p>
     * 请不要对返回的列表做任何增删改操作，如果您需要，请clone出一份新的出来.
     * 
     * @return
     */
    public List<Window> getWindows();

}
