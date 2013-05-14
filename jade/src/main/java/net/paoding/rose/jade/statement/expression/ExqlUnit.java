package net.paoding.rose.jade.statement.expression;


/**
 * 定义一个语句输出单元, 可以进行组装。
 * 
 * @author han.liao
 */
public interface ExqlUnit {

    /**
     * 检查单元的内容是否有效。
     * 
     * @param exprResolver - 使用的引擎
     * 
     * @return 内容是否有效
     * 
     * @throws Exception
     */
    boolean isValid(ExprResolver exprResolver);

    /**
     * 输出单元的语句内容。
     * 
     * @param exqlContext - 输出上下文
     * @param exprResolver - 使用的引擎
     * 
     * @throws Exception
     */
    void fill(ExqlContext exqlContext, ExprResolver exprResolver) throws Exception;
}
