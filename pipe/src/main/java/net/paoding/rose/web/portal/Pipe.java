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

/**
 * {@link WindowContainer}
 * 两种类型的其中一种，用于在服务端为一个请求并发请求多个窗口，但主控制器返回的整体页面框架先输出给客户端，并保持连接不断开，
 * 当某个窗口的流程执行完毕，框架再将该窗口返回的页面数据通过那个没有断开的连接吐给客户端；
 * 
 * 
 * <p>
 * 详细文档，请见 {@link WindowContainer}
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface Pipe extends WindowContainer {

    /**
     * 将一个css地址注册到window，使得客户端在渲染该Window时候能够先装载该css
     * 
     * @param window
     * @param css
     */
    public void addCssTo(String windowName, String css);

    /**
     * 将一个css地址注册到window，使得客户端得到Window时候能够执行该javascripte
     * 
     * @param windowName
     * @param js
     */
    public void addJsTo(String windowName, String js);
}
