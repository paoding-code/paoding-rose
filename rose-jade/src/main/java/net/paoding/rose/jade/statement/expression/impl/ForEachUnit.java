package net.paoding.rose.jade.statement.expression.impl;

import net.paoding.rose.jade.statement.expression.ExprResolver;
import net.paoding.rose.jade.statement.expression.ExqlContext;
import net.paoding.rose.jade.statement.expression.ExqlUnit;
import net.paoding.rose.jade.statement.expression.util.ExqlUtils;

/**
 * 循环输出子单元内容的语句单元, 例如: '#for (variant in :expr) {...}' 形式的语句。
 * 
 * @author han.liao
 */
public class ForEachUnit implements ExqlUnit {

    private final String expr;

    private final String variant;

    private final ExqlUnit unit;

    /**
     * 构造循环输出的语句单元。
     * 
     * @param expr - 集合表达式
     * @param variant - 循环临时变量名
     * @param unit - 需要循环输出的单元
     */
    public ForEachUnit(String expr, String variant, ExqlUnit unit) {
        this.expr = expr;
        this.variant = variant;
        this.unit = unit;
    }

    @Override
    public boolean isValid(ExprResolver exprResolver) {

        // 解释表达式内容
        Object obj = ExqlUtils.execExpr(exprResolver, expr);

        if (ExqlUtils.isValid(obj)) {
            return unit.isValid(exprResolver);
        }

        // 表达式内容为空
        return false;
    }

    @Override
    public void fill(ExqlContext exqlContext, ExprResolver exprResolver) throws Exception {

        // 解释表达式内容
        Object obj = exprResolver.executeExpr(expr);

        if (variant == null) {

            for (Object value : ExqlUtils.asCollection(obj)) {

                // 写入循环临时变量
                exprResolver.setVar("_loop", value);

                // 输出循环单元
                unit.fill(exqlContext, exprResolver);
            }

        } else {

            // 备份原变量 
            Object variantValue = exprResolver.getVar(variant);

            for (Object value : ExqlUtils.asCollection(obj)) {

                // 写入循环临时变量
                exprResolver.setVar(variant, value);

                // 输出循环单元
                unit.fill(exqlContext, exprResolver);
            }

            // 恢复备份变量
            exprResolver.setVar(variant, variantValue);
        }
    }
}
