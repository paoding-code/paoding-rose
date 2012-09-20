package net.paoding.rose.jade.statement.expression;

/**
 * 定义输出的上下文接口。
 * 
 * @author han.liao
 */
public interface ExqlContext {

    /**
     * 输出字符到语句内容。
     * 
     * @param ch - 输出的字符
     */
    void fillChar(char ch);

    /**
     * 输出字符串到语句内容, 字符串的内容是未经转义的。
     * 
     * @param string - 输出的字符串
     */
    void fillText(String string);

    /**
     * 输出对象到语句内容, 字符串的内容将被转义。
     * 
     * @param obj - 输出的对象
     */
    void fillValue(Object obj);

    /**
     * 返回所有参数的数组, 按参数出现的顺序。
     * 
     * @return 所有参数的数组
     */
    Object[] getParams();

    /**
     * 得到输出的语句内容。
     * 
     * @return 语句内容
     */
    String flushOut();
}
