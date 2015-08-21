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

import java.io.IOException;

import net.paoding.rose.web.Invocation;

/**
 * {@link PortalFactory} 是 portal/pipe 框架的核心，它负责创建 {@link Portal} /
 * {@link Pipe} 对象.
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface PortalFactory {

    /**
     * 创建给定请求的 {@link Portal} 实例
     * 
     * @param inv
     * @return
     */
    public Portal createPortal(Invocation inv);

    /**
     * 创建给定请求的 {@link Pipe} 实例
     * 
     * @param portal
     * @return
     * @throws IOException
     */
    public Pipe createPipe(Invocation inv, boolean create);

}
