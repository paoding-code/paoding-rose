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

import net.paoding.rose.jade.statement.expression.ExqlPattern;
import net.paoding.rose.jade.statement.expression.impl.ExqlContextImpl;
import net.paoding.rose.jade.statement.expression.impl.ExqlPatternImpl;

import org.springframework.jdbc.BadSqlGrammarException;

/**
 * 
 * @author 廖涵 [in355hz@gmail.com]
 */
public class SystemInterpreter implements Interpreter {

    @Override
    public void interpret(StatementRuntime runtime) {
        // 转换语句中的表达式
        ExqlPattern pattern = ExqlPatternImpl.compile(runtime.getSQL());
        ExqlContextImpl context = new ExqlContextImpl(runtime.getSQL().length() + 32);

        try {
            pattern.execute(context, runtime.getParameters(), runtime.getMetaData()
                    .getDAOMetaData().getConstants());
            runtime.setArgs(context.getParams());
            runtime.setSQL(context.flushOut());
        } catch (Exception e) {
            String daoInfo = runtime.getMetaData().toString();
            throw new BadSqlGrammarException(daoInfo, runtime.getSQL(),
                    new SQLSyntaxErrorException(daoInfo + " @SQL('" + runtime.getSQL() + "')", e));
        }

    }

    public static void main(String[] args) throws Exception {
        // 转换语句中的表达式
        String sql = "insert ignore into table_name "
                + "(`id`,`uid`,`favable_id`,`addtime`,`ranking`) "//
                + "values (:1,:2,now(),0)";
        ExqlPattern pattern = ExqlPatternImpl.compile(sql);
        ExqlContextImpl context = new ExqlContextImpl(sql.length() + 32);

        Map<String, Object> parametersAsMap = new HashMap<String, Object>();
        parametersAsMap.put(":1", "p1");
        parametersAsMap.put(":2", "p2");

        String result = pattern.execute(context, parametersAsMap);
        System.out.println(result);
    }

}
