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

import java.sql.SQLSyntaxErrorException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.jdbc.BadSqlGrammarException;

import net.paoding.rose.jade.statement.expression.ExqlPattern;
import net.paoding.rose.jade.statement.expression.impl.ExqlContextImpl;
import net.paoding.rose.jade.statement.expression.impl.ExqlPatternImpl;

/**
 * 
 * @author 廖涵 [in355hz@gmail.com]
 */
public class SystemInterpreter implements Interpreter {
    
    private ReplacementInterpreter     replacementInterpreter     = new ReplacementInterpreter();
    private PreparestatmentInterpreter preparestatmentInterpreter = new PreparestatmentInterpreter();

    @Override
    public void interpret(StatementRuntime runtime) {
        replacementInterpreter.interpret(runtime);
        preparestatmentInterpreter.interpret(runtime);
    }

    /**
     * 使用方法参数、常量替换{xxxx}、{:xxxx}、##(:xxx)、##(xxx)等位置；
     * <p>
     * 
     * select form ##(:table) {name} where {id}='{1}'
     * @author zlw
     *
     */
    static class ReplacementInterpreter implements Interpreter {

        final Pattern PATTERN = Pattern.compile("\\{([a-zA-Z0-9_\\.\\:]+)\\}|##\\((.+)\\)");


        final ThreadLocal<StringBuilder> stringBuilderPool = new ThreadLocal<StringBuilder>(){
            @Override
            public StringBuilder initialValue() {
                return new StringBuilder();
            };
            
        };

        @Override
        public void interpret(StatementRuntime runtime) {// ##(:xxx)
            StringBuilder sqlResult = stringBuilderPool.get();
            sqlResult.setLength(0);
            String sql = runtime.getSQL();
            Matcher matcher = PATTERN.matcher(sql);
            int start = 0;
            while (matcher.find(start)) {
                sqlResult.append(sql.substring(start, matcher.start()));
                String group = matcher.group(); 
                String key = null;
                if (group.startsWith("{")) {
                    key = matcher.group(1);
                } else if (group.startsWith("##(")) {
                    key = matcher.group(2);
                }
                // get value from parameters
                Object value = runtime.getParameters().get(key); // 针对{paramName}、{:1}两种情况
                if (value == null) {
                    if (key.startsWith(":") || key.startsWith("$")) {
                        value = runtime.getParameters().get(key.substring(1)); // 针对{:paramName}的情况
                    } else {
                        char ch = key.charAt(0);// 由正则表达式知道key长度必定大于0
                        if (ch >= '0' && ch <= '9') {
                            value = runtime.getParameters().get(":" + key); // 针对{1}两种情况
                        }
                    }
                }
                // get value from constants
                if (value == null) {
                    value = runtime.getMetaData().getDAOMetaData().getConstants().get(key); // 针对常量的情况
                }
                // get value from attributes
                if (value == null) {
                    String attributeKey = group;
                    if (!attributeKey.startsWith("{")) {
                        attributeKey = "{" + key + "}";
                    }
                    value = runtime.getMetaData().getDAOMetaData().getAttribute(attributeKey); // 针对插件设置进来的属性的情况
                }
                // replace it
                if (value != null) {
                    sqlResult.append(value);
                } else {
                    sqlResult.append(group);
                }
                start = matcher.end();
            }
            sqlResult.append(sql.substring(start));

            runtime.setSQL(sqlResult.toString());

        }
    }

    // 动态参数
    static class PreparestatmentInterpreter implements Interpreter {


        static final ThreadLocal<ExqlContextImpl> exqlContextPool = new ThreadLocal<ExqlContextImpl>(){
            @Override
            public ExqlContextImpl initialValue() {
                return new ExqlContextImpl();
            };
            
        };
        
        @Override
        public void interpret(StatementRuntime runtime) {
            // 转换语句中的表达式
            ExqlContextImpl context = exqlContextPool.get();
            context.clear();

            try {
                ExqlPattern pattern = ExqlPatternImpl.compile(runtime.getSQL());
                Map<String, Object> constants = runtime.getMetaData().getDAOMetaData()
                    .getConstants();
                pattern.execute(context, runtime.getParameters(), constants);
                runtime.setArgs(context.getArgs());
                runtime.setSQL(context.flushOut());
            } catch (Exception e) {
                String daoInfo = runtime.getMetaData().toString();
                throw new BadSqlGrammarException(daoInfo, runtime.getSQL(),
                    new SQLSyntaxErrorException(daoInfo + " @SQL('" + runtime.getSQL() + "')", e));
            }
        }

    }

    // ReplacementInterpreter
    public static void main(String[] args) throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("table", "my_table_name");
        parameters.put("id", "my_id");
        parameters.put(":1", "first_param");

        final Pattern PATTERN = Pattern.compile("\\{([a-zA-Z0-9_\\.\\:]+)\\}|##\\((.+)\\)");

        String sql = "select form ##(:table) {name} where {id}='{1}'";

        StringBuilder sb = new StringBuilder(sql.length() + 200);
        Matcher matcher = PATTERN.matcher(sql);
        int start = 0;
        while (matcher.find(start)) {
            sb.append(sql.substring(start, matcher.start()));
            String group = matcher.group();
            String key = null;
            if (group.startsWith("{")) {
                key = matcher.group(1);
            } else if (group.startsWith("##(")) {
                key = matcher.group(2);
            }
            System.out.println(key);
            if (key == null || key.length() == 0) {
                continue;
            }
            Object value = parameters.get(key); // 针对{paramName}、{:1}两种情况
            if (value == null) {
                if (key.startsWith(":") || key.startsWith("$")) {
                    value = parameters.get(key.substring(1)); // 针对{:paramName}的情况
                } else {
                    char ch = key.charAt(0);
                    if (ch >= '0' && ch <= '9') {
                        value = parameters.get(":" + key); // 针对{1}两种情况
                    }
                }
            }
            if (value == null) {
                value = parameters.get(key); // 针对常量的情况
            }
            if (value != null) {
                sb.append(value);
            } else {
                sb.append(group);
            }
            start = matcher.end();
        }
        sb.append(sql.substring(start));
        System.out.println(sb);

    }

    // ExqlInterpreter
    public static void main0(String[] args) throws Exception {
        // 转换语句中的表达式
        String sql = "insert ignore into table_name "
                     + "(`id`,`uid`,`favable_id`,`addtime`,`ranking`) "//
                     + "values (:1,:2,now(),0)";
        ExqlPattern pattern = ExqlPatternImpl.compile(sql);
        ExqlContextImpl context = new ExqlContextImpl();

        Map<String, Object> parametersAsMap = new HashMap<String, Object>();
        parametersAsMap.put(":1", "p1");
        parametersAsMap.put(":2", "p2");

        pattern.execute(context, parametersAsMap);
        String result = context.flushOut();
        System.out.println(result);
    }

}
