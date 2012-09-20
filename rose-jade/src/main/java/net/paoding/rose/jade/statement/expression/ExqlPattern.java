package net.paoding.rose.jade.statement.expression;

import java.util.Map;

/**
 * 定义一个语句的执行接口。
 * 
 * @author han.liao
 */
public interface ExqlPattern {

    /**
     * 输出全部的语句内容。
     * 
     * @param context - 输出上下文
     * 
     * @param map - 参数表
     * 
     * @return 语句内容
     * 
     * @throws Exception
     */
    String execute(ExqlContext context, Map<String, ?> map) throws Exception;

    /**
     * 输出全部的语句内容。
     * 
     * @param context - 输出上下文
     * 
     * @param mapVars - 参数表
     * @param mapConsts - 常量表
     * 
     * @return 语句内容
     * 
     * @throws Exception
     */
    String execute(ExqlContext context, Map<String, ?> mapVars, // NL
            Map<String, ?> mapConsts) throws Exception;
}
