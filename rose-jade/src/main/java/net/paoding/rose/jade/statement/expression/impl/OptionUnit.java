package net.paoding.rose.jade.statement.expression.impl;

import net.paoding.rose.jade.statement.expression.ExprResolver;
import net.paoding.rose.jade.statement.expression.ExqlContext;
import net.paoding.rose.jade.statement.expression.ExqlUnit;

/**
 * 条件输出子单元的语句单元, 例如一个: {...}? 语句段。
 * 
 * @author han.liao
 */
public class OptionUnit implements ExqlUnit {

    private final ExqlUnit unit;

    public OptionUnit(ExqlUnit unit) {
        this.unit = unit;
    }

    @Override
    public boolean isValid(ExprResolver exprResolver) {

        // 条件单元始终有效, 因为若子单元无效
        // 它就不会产生输出。
        return true;
    }

    @Override
    public void fill(ExqlContext exqlContext, ExprResolver exprResolver) throws Exception {

        // 当且仅当子单元有效时输出
        if (unit.isValid(exprResolver)) {
            unit.fill(exqlContext, exprResolver);
        }
    }
}
