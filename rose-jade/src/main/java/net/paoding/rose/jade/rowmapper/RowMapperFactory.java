/*
 * Copyright 2009-2012 the original author or authors.
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
package net.paoding.rose.jade.rowmapper;



import net.paoding.rose.jade.statement.StatementMetaData;

import org.springframework.jdbc.core.RowMapper;

/**
 * {@link RowMapperFactory}可以为每一个查询类型DAO方法创建对应的行映射器{@link RowMapper}
 * 对象，用来把SQL结果集中的每一行转化为DAO方法定义的返回类型所要求的组件类型。
 * <p>
 * 对于DAO方法返回类型是List<User>或User的，RowMapper用来将结果集的每一行转化为User对象。<br>
 * 如果返回类型是String[]或String的，RowMapper用来将结果集的每一行转化为一个字符串对象。<br>
 * 。。。
 * <p>
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 */
public interface RowMapperFactory {

    /**
     * 根据DAO方法的返回类型定义，解析可能存在的泛型，创建对应的行映射器对象。
     * 
     * @return 如果无法解析时可返回null
     */
    public RowMapper getRowMapper(StatementMetaData metaData);
}
