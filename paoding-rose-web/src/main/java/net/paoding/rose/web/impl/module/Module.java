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

import java.net.URL;
import java.util.List;

import net.paoding.rose.web.ControllerErrorHandler;
import net.paoding.rose.web.InterceptorDelegate;
import net.paoding.rose.web.ParamValidator;
import net.paoding.rose.web.impl.mapping.Mapping;
import net.paoding.rose.web.paramresolver.ParamResolver;

import org.springframework.web.context.WebApplicationContext;

/**
 * {@link Module}代表了在同一个web程序中的一个模块。不同的模块包含一些特定的控制器对象、控制器拦截器、控制器异常处理器。
 * 不同的模块共享了ServletContext以及整个程序的中间层、资源层。
 * <p>
 * 一个web程序的不同的模块有不同的名字和路径。作为{@link Module}接口本身并没有要求模块的名字和路径有什么关系，
 * 但在实现上模块的路径是由其名字决定的，即path=/name，比如名字为admin的模块，路径将是/admin。
 * 作为一个特例，名字为root的模块路径则只是空串。
 * <p>
 * 一个HTTP请求将根据它的URI，映射到相应的web程序中(由web容器处理)，而后又映射给具体的module模块(由Rose处理)。
 * 映射规则以模块的路径为依据(名字此时不参与这个决策)。
 * <p>
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public interface Module {

    /**
     * 该模块相关资源路径(即控制器“类文件”存在本地计算机的什么目录下，包括package名所表示的路径)
     * 
     * @return
     */
    public URL getUrl();

    /**
     * 该模块的映射地址定义
     * 
     * @see Mapping#getPath()
     * 
     * @return
     */
    public String getMappingPath();

    /**
     * 上级模块，返回null表示为最顶级模块。
     * <p>
     * 模块之间的上下级和URI的分层上下级没有必然联系，虽然大多数应该有一致的表现。
     * 
     * @return
     */
    public Module getParent();

    /**
     * 模块地址。
     * <p>
     * 如果是最顶级模块其地址是一个长度为0的串。<br>
     * 下级模块的地址是上级模块的地址 + "/" + 本模块所在package的简单包名(package最后一级)。
     * 
     * @return
     */
    public String getRelativePath();

    /**
     * 本模块的ApplicationContext对象。
     * <p>
     * 如果模块具有上级模块，这2个模块的ApplicationContext也会体现这个上下级关系
     * 
     * @return
     */
    public WebApplicationContext getApplicationContext();

    /**
     * 本模块使用的有效外设参数解析器对象
     * 
     * @return
     */
    public List<ParamResolver> getCustomerResolvers();

    /**
     * 本模块使用的验证器
     * 
     * @return
     */
    public List<ParamValidator> getValidators();

    /**
     * 本模块使用的有效拦截器对象
     * 
     * @return
     */
    public List<InterceptorDelegate> getInterceptors();

    /**
     * 本模块有效的控制器对象
     * 
     * @return
     */
    public List<ControllerRef> getControllers();

    /**
     * 本模块使用的错误处理器
     * 
     * @return
     */
    public ControllerErrorHandler getErrorHandler();

}
