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
package net.paoding.rose.jade.dataaccess;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;

/**
 * {@link DataAccess} 分隔了DAO接口层和数据访问层。
 * <p>
 * 数据访问层规范定义了所支持的数据访问接口
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 */
public interface DataAccess {

    /**
     * 返回所使用的DataSource
     * 
     * @return
     */
    DataSource getDataSource();

    /**
     * 读访问
     * 
     * @param sql 所要执行的实际SQL语句
     * @param args 伴随该SQL语句的参数
     * @param rowMapper 行映射器
     * @return
     */
    List<?> select(String sql, Object[] args, RowMapper rowMapper);

    /**
     * 写访问（更新或插入）
     * 
     * @param sql 所要执行的实际SQL语句
     * @param args 伴随该SQL语句的参数
     * @param generatedKeyHolder 是否要读取该SQL生成的key
     * @return
     */
    int update(String sql, Object[] args, KeyHolder generatedKeyHolder);

    /**
     * 批量写访问（更新或插入）
     * 
     * @param sql 所要执行的实际SQL语句
     * @param argsList 伴随该SQL语句的参数
     * @return
     */
    int[] batchUpdate(String sql, List<Object[]> argsList);
}
