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
package net.paoding.rose.jade.statement;

import java.util.Collections;
import java.util.Map;


/**
 * {@link DAOMetaData} 封装缓存一个DAO接口类本身的一些信息，比如类对象、类常量等等
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 */
public class DAOMetaData {

    /**
     * DAO接类
     */
    private final Class<?> daoClass;

    /**
     * 定义在DAO接口上的常量（包含父接口的）
     */
    private final Map<String, ?> constants;

    /**
     * 
     * @param daoClass
     */
    public DAOMetaData(Class<?> daoClass) {
        this.daoClass = daoClass;
        this.constants = Collections.unmodifiableMap(//
                GenericUtils.getConstantFrom(daoClass, true, true));
    }

    public Class<?> getDAOClass() {
        return daoClass;
    }

    public Map<String, ?> getConstants() {
        return constants;
    }

    @SuppressWarnings("unchecked")
    public <T> T getConstant(String fieldName) {
        return (T) constants.get(fieldName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DAOMetaData) {
            DAOMetaData other = (DAOMetaData) obj;
            return daoClass.equals(other.daoClass);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return daoClass.hashCode() * 13;
    }

    @Override
    public String toString() {
        return daoClass.getName();
    }
}
