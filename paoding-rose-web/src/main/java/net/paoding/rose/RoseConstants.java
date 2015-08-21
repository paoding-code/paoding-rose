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
package net.paoding.rose;

import org.springframework.web.servlet.support.RequestContext;

/**
 * Rose使用的一些常量
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public interface RoseConstants {

    /**
     * 实体类在这个目录下或它的子目录下，一个工程如果有很多domain，都将被扫描进来
     */
    public static final String DOMAIN_DIRECTORY_NAME = "domain";

    /**
     * 视图文件在这个目录下
     */
    public static final String VIEWS_PATH = "/views";

    public static final String VIEWS_PATH_WITH_END_SEP = VIEWS_PATH + "/";

    /**
     * 控制器需在这个目录下或它的子目录下，一个工程如果有很多controllers，都将被扫描进来
     */
    public static final String CONTROLLERS = "controllers";

    /**
     * 资源控制器都以其中一个字符串为结尾。
     * <p>
     * 如果结尾的字符串长度为1，那么要求这个字符串的前一个字符不应为大写字符。
     */
    public static final String[] CONTROLLER_SUFFIXES = new String[] { "Resource", "Controller",
            "C", "Action" };

    /** 拦截器结束符号 */
    public static final String INTERCEPTOR_SUFFIX = "Interceptor";

    /**
     * 用于配置在每个module的包的rose.properties，如果这个属性值为true代表这个目录以及子目录不作为module
     */
    public static final String CONF_MODULE_IGNORED = "module.ignored";

    /**
     * 用于在每个module的包的rose.properties中，Rose将使用这个属性的值作为所在module的path
     * <p>
     * 可以使${parent.module.path}引用上一次级module的path，比如${parent.module.path}/
     * admin<br>
     * 或者只需要配置module.path=admin亦达到同样效果
     * <p>
     * 请注意module.path=/admin和module.path=admin的不同
     */
    public static final String CONF_MODULE_PATH = "module.path";

    /**
     * 可配置在模块下的module的rose.properties表示本模块的最大允许的拦截器
     */
    public static final String CONF_INTERCEPTED_ALLOW = "intercepted.allow";

    /**
     * 可配置在模块下的module的rose.properties表示本模块的不能允许的拦截器
     */
    public static final String CONF_INTERCEPTED_DENY = "intercepted.deny";

    /**
     *用于在每个module的包的rose.properties中中，定义module.path时，
     * 可通过它引用上个package定义的module.path
     */
    public static final String CONF_PARENT_MODULE_PATH = "parent." + CONF_MODULE_PATH;

    public static final String WEB_APPLICATION_CONTEXT_ATTRIBUTE = RequestContext.WEB_APPLICATION_CONTEXT_ATTRIBUTE;

    public static final String PIPE_WINDOW_IN = "$$paoding-rose-portal.window.in";

    public static final String WINDOW_ATTR = "$$paoding-rose-portal.window";

}
