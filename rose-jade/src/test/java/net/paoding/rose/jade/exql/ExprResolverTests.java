package net.paoding.rose.jade.exql;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.paoding.rose.jade.statement.expression.ExprResolver;
import net.paoding.rose.jade.statement.expression.impl.ExprResolverImpl;

public class ExprResolverTests extends TestCase {

    // 进行简单测试
    public void testPrimitives() throws Exception {

        float f1 = 25.1f;
        double d2 = 3.1415;
        long l3 = 65536;
        int i4 = 125;

        Map<String, Object> mapVars = new HashMap<String, Object>();
        mapVars.put("f1", f1);
        mapVars.put("d2", d2);

        Map<String, Object> mapConsts = new HashMap<String, Object>();
        mapConsts.put("l3", l3);
        mapConsts.put("i4", i4);

        ExprResolver exprResolver = new ExprResolverImpl(mapVars, mapConsts);

        Double value1 = (Double) exprResolver.executeExpr(":f1 - (:d2 + $l3) - $i4 - 1");
        System.out.println(value1);
        Assert.assertEquals(f1 - (d2 + l3) - i4 - 1, value1, 0.000001);

        Double value2 = (Double) exprResolver.executeExpr("(:f1 - :d2) + 1.5 + ($l3 - $i4)");
        System.out.println(value2);
        Assert.assertEquals((f1 - d2) + 1.5 + (l3 - i4), value2, 0.000001);
    }

    // 进行简单测试
    @SuppressWarnings("deprecation")
    public void testBeans() throws Exception {

        Locale bean1 = Locale.getDefault();
        Date bean2 = new Date();

        Map<String, Object> mapVars = new HashMap<String, Object>();
        mapVars.put("bean1", bean1);

        Map<String, Object> mapConsts = new HashMap<String, Object>();
        mapConsts.put("bean2", bean2);

        ExprResolver exprResolver = new ExprResolverImpl(mapVars, mapConsts);

        Object value = exprResolver.executeExpr(":bean1.displayCountry + ', ' + "
                + ":bean1.displayLanguage + ', ' + :bean1.displayName + ': '"
                + " + ($bean2.year + 1900) + '-' + ($bean2.month + 1) + '-' + $bean2.date");
        System.out.println(value);

        Assert.assertEquals(bean1.getDisplayCountry() + ", " + // NL
                bean1.getDisplayLanguage() + ", " + bean1.getDisplayName() + ": " // NL
                + (bean2.getYear() + 1900) + '-' + (bean2.getMonth() + 1) // NL
                + '-' + bean2.getDate(), value);
    }
}
