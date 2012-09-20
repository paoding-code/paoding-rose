/*
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License i distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.paoding.rose.jade.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用{@link SQLParam} 注解标注DAO方法的参数，指定参数的名称，使得可以在SQL中通过":参数名"的方式使用它。
 * Jade通过PreparedStatment动态地把参数值提交给数据库执行。
 * <p>
 * 
 * <span style='margin-left:50px;'>
 * <code>@SQL("SELECT id, account, name FROM user WHERE id=:userId")<span>
 * <br>
 * <span style='margin-left:50px;'> public User getUser(@SQLParam("userId") String id);</code><span>
 * <p>
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 */
@Target( { ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SQLParam {

    /**
     * 指出这个值是 SQL 语句中哪个参数的值
     * 
     * @return 对应 SQL 语句中哪个参数
     */
    String value();
}
