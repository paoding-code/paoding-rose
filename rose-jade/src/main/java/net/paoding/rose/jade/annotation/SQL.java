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
 * 用{@link SQL}注解标注在Jade DAO方法上，表示这个DAO方法所要执行的SQL语句。
 * <p>
 * 
 * Jade把SQL语句分为查询类型和更改类型，Jade进行SQL类型分类的基于两个目的：<br>
 * <ul>
 * <li>1）因为这两种类型的SQL返回结果不一样，查询类型返回结果是一个结果集， 更新类型的SQL返回结果只是一个数字表示更新的条目；</li>
 * <li>2）是为了能够使SQL能够在master- slave的数据库架构中发往正确的目的数据库执行。</li>
 * </ul>
 * <p>
 * 
 * 简单地，Jade认为所有以SELECT开始在SQL是查询类型的，其他的都是更新类型的。不过当然这种分法非常不合理，
 * 比如SHOW语句所代表的就应该是查询类型的，在这种情况下，我们还是希望由开发者您在{@link SQL}
 * 指定吧，如果有需要执行一些非SELECT的查询类型的语句的话。
 * <p>
 * 
 * 在写SQL时可把SQL参数值直接放到SQL中，如下：<br>
 * <span style='margin-left:50px;'>
 * <code>@SQL("SELECT id, account, name FROM user WHERE id='12345'")</code>
 * </span>
 * <p>
 * 也可由DAO方法的命名参数指定，因此支持了动态参数，即以冒号开始并紧跟一个名字字符串表示，如下：<br>
 * <span style='margin-left:50px;'>
 * <code>@SQL("SELECT id, account, name FROM user WHERE id=:userId")<span>
 * <br>
 * <span style='margin-left:50px;'> public User getUser(@SQLParam("userId") String id);</code><span>
 * <p>
 * OR<br>
 * <span style='margin-left:50px;'>
 * <code>@SQL("SELECT id, account, name FROM user where id=:user.id")<span>
 * <br>
 * <span style='margin-left:50px;'> public User getUser(@SQLParam("user") User user);</code><span> <br>
 * <p>
 * 在此，我们也示例了{@link SQL}注解所使用语句和标准的SQL的有所区别。为了更加有效地支持编程，此处的SQL具有较为丰富的法，具体请见：
 * http://paoding-rose.googlecode.com/....
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 */
@Target( { ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SQL {

    /**
     * 
     * @return Jade支持的SQL语句
     */
    String value();

    /**
     * 返回该语句的类型，查询类型或变更类型。
     * 默认Jade认为只有以SELECT开始的才是查询类型，其他的为变更类型。开发者通过这个属性用来变更Jade默认的处理!
     * 
     * @return 查询类型
     */
    SQLType type() default SQLType.AUTO_DETECT;
}
