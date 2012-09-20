/*
 * $ID$
 */
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

import javax.servlet.http.HttpServletRequest;

import net.paoding.rose.web.impl.mapping.Mapping;

/**
 * 一个 {@link Engine} 封装了对某种符合要求的请求的某种处理。Rose 对一次WEB请求的处理最终落实为对一些列的
 * {@link Engine}的有序调用，每个 {@link Engine} 负责处理其中需要处理的逻辑，共同协作完成 Rose 的职责。
 * <p>
 * 在一个Rose应用中，存在着“很多的、不同的” {@link Engine}实例，这些实例根据映射关系组成在一个树状的结构中。
 * 
 * @see Rose
 * @see Mapping
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public interface Engine {

    /**
     * 除了地址匹配之外，哪些因素可能拒绝或同意由这个engine来处理？
     * 所有匹配地址的engine都会被询问，返回值0或负数表示不接受，大于等于1的表示可以，值越大越优先
     * 
     * @param request
     * @return
     */
    public int isAccepted(HttpServletRequest request);

    /**
     * 处理web请求
     * 
     * @param rose
     * @param mr
     * @throws Throwable
     */
    public Object execute(Rose rose) throws Throwable;

    /**
     * 销毁该引擎，在系统关闭或其他情况时
     * 
     * @throws Throwable
     */
    public void destroy();
}
