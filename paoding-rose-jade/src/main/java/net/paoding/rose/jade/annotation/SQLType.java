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

/**
 * SQL类型标识。
 * <p>
 * 在使用{@link SQL}
 * 注解时，Jade将以SELECT开始的语句认为是查询类型SQL语句，其它的语句被认为是更新类型，开发者可以根据实际改变Jade的默认判断
 * ，比如SHOW语句实际应该是查询类型语句，而非更新类型语句。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 */
public enum SQLType {
    /**
     * 查询类型语句
     */
    READ,

    /**
     * 更新类型语句
     */
    WRITE,

    /**
     * 未知类型，将使用Jade的默认规则判断：所有以SELECT开始的语句是查询类型的，其他的是更新类型的
     */
    AUTO_DETECT,

}
