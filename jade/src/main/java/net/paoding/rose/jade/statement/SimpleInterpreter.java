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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * 
 * @author 廖涵 [in355hz@gmail.com]
 */
//NOTE: 这是一个最简单的解释器（没有表达式功能的解释器），实际并未启用
public class SimpleInterpreter implements Interpreter {

    private static final Pattern NAMED_PARAM_PATTERN = Pattern.compile("(\\:([a-zA-Z0-9_\\.]+))");

    @Override
    public void interpret(StatementRuntime runtime) {

        final List<Object> parametersAsList = new LinkedList<Object>();

        // 匹配符合  :name 格式的参数
        Matcher matcher = NAMED_PARAM_PATTERN.matcher(runtime.getSQL());
        if (!matcher.find()) {
            return;
        }

        final StringBuilder builder = new StringBuilder();

        int index = 0;

        do {
            // 提取参数名称
            String name = matcher.group(1);
            if (NumberUtils.isDigits(name)) {
                name = matcher.group();//把冒号包含进去
            }

            Object value = null;

            // 解析  a.b.c 类型的名称 
            int find = name.indexOf('.');
            if (find >= 0) {

                // 用  BeanWrapper 获取属性值
                Object bean = runtime.getParameters().get(name.substring(0, find));
                if (bean != null) {
                    BeanWrapper beanWrapper = new BeanWrapperImpl(bean);
                    value = beanWrapper.getPropertyValue(name.substring(find + 1));
                }

            } else {
                // 直接获取值
                value = runtime.getParameters().get(name);
            }

            // 拼装查询语句
            builder.append(runtime.getSQL().substring(index, matcher.start()));

            if (value instanceof Collection<?>) {

                // 拼装 IN (...) 的查询条件
                builder.append('(');

                Collection<?> collection = (Collection<?>) value;

                if (collection.isEmpty()) {
                    builder.append("NULL");
                } else {
                    builder.append('?');
                }

                for (int i = 1; i < collection.size(); i++) {
                    builder.append(", ?");
                }

                builder.append(')');

                // 保存参数值
                parametersAsList.addAll(collection);

            } else {
                // 拼装普通的查询条件
                builder.append('?');

                // 保存参数值
                parametersAsList.add(value);
            }

            index = matcher.end();

        } while (matcher.find());

        // 拼装查询语句
        builder.append(runtime.getSQL().substring(index));
        runtime.setSQL(builder.toString());
        runtime.setArgs(parametersAsList.toArray());
    }

}
