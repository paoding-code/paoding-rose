/*
 * Copyright 2009-2015 the original author or authors.
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
package net.paoding.rose.jade.statement;

import net.paoding.rose.jade.dataaccess.DataAccessFactory;
import net.paoding.rose.jade.rowmapper.RowMapperFactory;

/**
 * 支持DAO类的基础配置（数据源配置、SQL解析器配置、OR映射配置等等）
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class DAOConfig {

    private final DataAccessFactory dataAccessFactory;

    private final RowMapperFactory rowMapperFactory;

    private final InterpreterFactory interpreterFactory;

    public DAOConfig(DataAccessFactory dataAccessFactory, //
                     RowMapperFactory rowMapperFactory, //
                     InterpreterFactory interpreterFactory) {
        this.dataAccessFactory = dataAccessFactory;
        this.rowMapperFactory = rowMapperFactory;
        this.interpreterFactory = interpreterFactory;
    }

    /**
     * 标准数据访问器配置
     * 
     * @return
     */
    public DataAccessFactory getDataAccessFactory() {
        return dataAccessFactory;
    }

    /**
     * SQL解析器配置
     * 
     * @return
     */
    public InterpreterFactory getInterpreterFactory() {
        return interpreterFactory;
    }

    /**
     * OR映射配置
     * 
     * @return
     */
    public RowMapperFactory getRowMapperFactory() {
        return rowMapperFactory;
    }

}
