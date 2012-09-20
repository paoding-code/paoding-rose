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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.InvocationUtils;
import net.paoding.rose.web.var.PrivateVar;

/**
 * {@link Rc} 将1.0版本被删除， 请在控制器方法中声明 {@link Invocation} inv 代替完成对{@link Rc}
 * 的使用
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @since 0.9
 */
@Deprecated
public class Rc {

    //-------------------------------------------------------------------

    /**
     * 返回绑定到当前线程的请求对象.
     * 
     * @return
     * @throws NullPointerException 如果当前线程中没有绑定请求对象时
     */
    public static HttpServletRequest request() {
        return InvocationUtils.getCurrentThreadRequest();
    }

    /**
     * 在控制器中调用此方法，返回当前的HTTP响应对象
     * 
     * @return
     * @throws NullPointerException 如果当前线程中没有绑定请求对象时
     */
    public static HttpServletResponse response() {
        return invocation().getResponse();
    }

    /**
     * 获取本次绑定到当前线程的请求当前时刻的 {@link Invocation}对象.
     * 
     * @return
     * @throws NullPointerException 如果当前线程中没有绑定请求对象时
     * @see PrivateVar#getInvocation()
     */
    public static Invocation invocation() {
        return InvocationUtils.getInvocation(request());
    }
}
