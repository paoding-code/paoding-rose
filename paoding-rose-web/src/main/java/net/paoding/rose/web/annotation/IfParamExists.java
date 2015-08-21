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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 将 {@link IfParamExists} 标注在控制器方法的参数上，用于表示只有符合此条件时，才将请求映射到该方法。
 * 
 * 注意：{@link IfParamExists}只判断query string中的参数，不判断request body中的参数。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
@Target( { ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IfParamExists {

    /**
     * 示例：<br>
     * 
     * IfParamExists("type")表示只当请求含有非零长度的type参数时才映射到此类 <br>
     * IfParamExists("type=1") 表示只当请求含有type参数且值为1才映射到此方法 ；<br>
     * 
     * <p>
     * 同时还支持多条件判断，判断条件也可以是正则表达式，如下面这个示例：<br>
     * IfParamExists("type&subtype=2&num=:[0-9]+") 表示只有当请求同时满足三个条件时才映射
     * 到此方法：
     * <ol>
     * <li>请求含有非零长度的type参数(存在性判断)</li>
     * <li>请求含有subtype参数并且其值为2(精确判断)</li>
     * <li>请求含有num参数并且其值符合正则表达式"[0-9]+"(正则判断)</li>
     * </ol> 
     * </p>
     * 
     * <p>
     * 从上面的例子中不难看出，IfParamExists的条件判断支持三种方式，即：
     * <ol>
     * <li>存在性判断</li>
     * <li>精确判断</li>
     * <li>正则判断</li>
     * </ol>
     * 这三种判断的优先级(数值越高越优先)为: 精确判断(13)>正则判断(12)>存在性判断(10)
     * 
     * 例如一个请求中含有参数type=1和subtype=2，那么对于如下三个标注：
     * <ol>
     * <li>IfParamExists("type&subtype")：优先级为10+10=20</li>
     * <li>IfParamExists("type=1&subtype=:[0-9+]")：优先级为13+12=25</li>
     * <li>IfParamExists("type=1&subtype=2")：优先级为13+13=26</li>
     * 所以这种情况下标注IfParamExists("type=1&subtype=2")的方法会被优先执行。
     * </ol>
     * </p>
     * 
     * @return
     */
    String value();
}
