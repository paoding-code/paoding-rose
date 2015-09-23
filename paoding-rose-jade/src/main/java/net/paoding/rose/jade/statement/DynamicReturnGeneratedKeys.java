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

import org.springframework.dao.InvalidDataAccessApiUsageException;

import net.paoding.rose.jade.annotation.ReturnGeneratedKeys;

/**
 * 
 * @see ReturnGeneratedKeys
 */
public abstract class DynamicReturnGeneratedKeys {

    /**
     * 是否要启动 return generated keys机制
     * @param runtime
     */
    public abstract boolean shouldReturnGerneratedKeys(StatementRuntime runtime);

    /**
     * 检查DAO返回的类型是否合格
     * 
     * @param returnType DAO方法的返回类型（如果方法声明的返回类型是泛型，框架会根据上下文信息解析为运行时实际应该返回的真正类型)
     * 
     * @throws InvalidDataAccessApiUsageException DAO方法的返回类型不合格
     */
    public void checkMethodReturnType(Class<?> returnType, StatementMetaData metaData) {
        if (returnType != void.class && !Number.class.isAssignableFrom(returnType)) {
            throw new InvalidDataAccessApiUsageException(
                "error return type, only support int/long/double/float/void type for method with @ReturnGeneratedKeys:"
                                                         + metaData.getMethod());
        }
    }
}
