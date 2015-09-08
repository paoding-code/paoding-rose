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

import java.util.Map;

import net.paoding.rose.jade.annotation.SQLParam;
import net.paoding.rose.jade.dataaccess.DataSourceFactory;

/**
 * 封装一次DAO调用信息
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public interface StatementRuntime {

    /**
     * @return 返回所要执行的DAO方法信息
     */
    StatementMetaData getMetaData();

    /**
     * @return 返回调用DAO方法传入的参数，key为":1"、":2"，以及 {@link SQLParam} 注解指定的名称(不含冒号)
     */
    Map<String, Object> getParameters();

    /**
     * 
     * @return 返回此时要执行的SQL，最初值为标注在DAO方法上的@SQL内容，但 {@link Interpreter}们会通过{@link #setSQL(String)} 可以改变它，
     * 最终形成符合数据库语法要求的真正SQL语句
     */
    String getSQL();

    /**
     * 将最初的SQL语句变为新传入的SQL语句
     * 
     * @param sql 
     */
    void setSQL(String sql);

    /**
     * 
     * @return 当SQL最终转为含有问号?的时候，返回SQL语句中对应的参数值
     */
    Object[] getArgs();

    /**
     * 当SQL最终转为含有问号?的时候，调用本方法将SQL语句中对应的参数按顺序传入进来
     * @param args
     */
    void setArgs(Object[] args);

    /**
     * 返回{@link Interpreter}在解析SQL语句时候，可以给runtime设置的一些属性，辅助 {@link DataSourceFactory} 用于决策
     * @return 非空
     */
    Map<String, Object> getAttributes();

    /**
     *  {@link Interpreter}在解析SQL语句时候，可以给runtime设置的一些属性，辅助 {@link DataSourceFactory} 用于决策
     * @param name
     * @param value
     */
    void setAttribute(String name, Object value);

    /**
     * 返回{@link Interpreter}在解析SQL语句时候，可以给runtime设置的一些属性，辅助 {@link DataSourceFactory} 用于决策
     * @param name
     * @return
     */
    <T> T getAttribute(String name);

    //----------@deprecated below----------------

    /**
     * @deprecated 接口不会删除，但property的概念不合适，请转使用attribute系列
     * @see #getAttributes()
     */
    Map<String, Object> getProperties();

    /**
     * 
     * @deprecated 接口不会删除，但property的概念不合适，请转使用attribute系列
     * @see #setAttribute(String, Object)
     */
    void setProperty(String name, Object value);

    /**
     * @deprecated 接口不会删除，但property的概念不合适，请转使用attribute系列
     * @see #getAttribute(String)
     */
    <T> T getProperty(String name);

}
