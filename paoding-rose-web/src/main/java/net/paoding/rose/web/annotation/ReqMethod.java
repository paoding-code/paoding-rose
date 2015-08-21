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
package net.paoding.rose.web.annotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 用于辅助设置一个控制器action方法映射，声明只有指定的http请求方法才由 {@link Path}作标注的action方法处理
 * 
 * @see Path
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public enum ReqMethod {

    // enums
    GET, POST, DELETE, PUT, HEAD, OPTIONS, TRACE, ALL;

    public List<ReqMethod> parse() {
        if (this == ALL) {
            ReqMethod[] reqMethods = ReqMethod.values();
            List<ReqMethod> list = new ArrayList<ReqMethod>(Arrays.asList(reqMethods));
            list.remove(ReqMethod.ALL);
            return list;
        } else {
            return Arrays.asList(new ReqMethod[] { this });
        }
    }

    public static void main(String[] args) {
        System.out.println(ReqMethod.ALL.parse());
    }

    public static ReqMethod parse(String method) {
        if ("GET".equalsIgnoreCase(method)) {
            return GET;
        }
        if ("POST".equalsIgnoreCase(method)) {
            return POST;
        }
        if ("*".equals(method) || "ALL".equals(method)) {
            return ALL;
        }
        if ("DELETE".equalsIgnoreCase(method)) {
            return DELETE;
        }
        if ("PUT".equalsIgnoreCase(method)) {
            return PUT;
        }
        if ("HEAD".equalsIgnoreCase(method)) {
            return HEAD;
        }
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return OPTIONS;
        }
        if ("TRACE".equalsIgnoreCase(method)) {
            return TRACE;
        }
        return null;
    }

}
