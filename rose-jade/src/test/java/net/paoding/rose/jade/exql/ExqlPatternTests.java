package net.paoding.rose.jade.exql;

import java.util.HashMap;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.paoding.rose.jade.statement.expression.ExqlContext;
import net.paoding.rose.jade.statement.expression.ExqlPattern;
import net.paoding.rose.jade.statement.expression.impl.ExqlContextImpl;
import net.paoding.rose.jade.statement.expression.impl.ExqlPatternImpl;

public class ExqlPatternTests extends TestCase {

    public void testPattern() throws Exception {

        String expr1 = "expr1";
        String expr2 = "expr2";
        String expr3 = "expr3";
        String expr4 = "expr4";
        String expr5 = "expr5";

        // 编译下列语句
        ExqlPattern pattern = ExqlPatternImpl
                .compile("SELECT #(:expr1.length()), :expr2.class.name,"
                        + " ##(:expr3) WHERE #if(:expr4) {e = :expr4} #else {e IS NULL}"
                        + "#for(variant in :expr5.bytes) { AND c = :variant}" // NL
                        + " GROUP BY ##(:expr1) ASC");

        ExqlContext context = new ExqlContextImpl(1024);

        HashMap<String, Object> map = new HashMap<String, Object>();

        map.put("expr1", expr1);
        map.put("expr2", expr2);
        map.put("expr3", expr3);
        map.put("expr4", expr4);
        map.put("expr5", expr5);

        Assert.assertEquals("SELECT ?, ?, expr3 WHERE e = ? AND c = ? AND c = ? "
                + "AND c = ? AND c = ? AND c = ? GROUP BY expr1 ASC", // NL
                pattern.execute(context, map));

        Object[] expectArray = new Object[] { expr1.length(), expr2.getClass().getName(), expr4,
                expr5.getBytes()[0], expr5.getBytes()[1], expr5.getBytes()[2], expr5.getBytes()[3],
                expr5.getBytes()[4] };
        Object[] paramArray = context.getParams();

        Assert.assertEquals(expectArray.length, paramArray.length);
        for (int i = 0; i < expectArray.length; i++) {
            Assert.assertEquals(expectArray[i], paramArray[i]);
        }
    }

    public void testConstant() throws Exception {

        String expr1 = "expr1";
        String expr2 = "expr2";
        String expr3 = "expr3";
        String expr4 = "expr4";
        String expr5 = "expr5";

        // 编译下列语句
        ExqlPattern pattern = ExqlPatternImpl
                .compile("SELECT #($expr1.length()), #($expr2.class.name),"
                        + " $expr3 WHERE #if($expr4) {e = #($expr4)} #else {e IS NULL}"
                        + "#for(variant in $expr5.bytes) { AND c = :variant}" // NL
                        + " GROUP BY $expr1 ASC");

        ExqlContext context = new ExqlContextImpl(1024);

        HashMap<String, Object> map = new HashMap<String, Object>();

        map.put("expr1", expr1);
        map.put("expr2", expr2);
        map.put("expr3", expr3);
        map.put("expr4", expr4);
        map.put("expr5", expr5);

        Assert.assertEquals("SELECT ?, ?, expr3 WHERE e = ? AND c = ? AND c = ? "
                + "AND c = ? AND c = ? AND c = ? GROUP BY expr1 ASC", // NL
                pattern.execute(context, map, map));

        Object[] expectArray = new Object[] { expr1.length(), expr2.getClass().getName(), expr4,
                expr5.getBytes()[0], expr5.getBytes()[1], expr5.getBytes()[2], expr5.getBytes()[3],
                expr5.getBytes()[4] };
        Object[] paramArray = context.getParams();

        Assert.assertEquals(expectArray.length, paramArray.length);
        for (int i = 0; i < expectArray.length; i++) {
            Assert.assertEquals(expectArray[i], paramArray[i]);
        }
    }

    public void testComplex() throws Exception {

        String expr1 = "expr1";
        String expr2 = "expr2";
        String expr3 = "expr3";
        String expr4 = "expr4";
        String expr5 = "expr5";

        // 编译下列语句
        ExqlPattern pattern = ExqlPatternImpl
                .compile("SELECT #(:expr1.length()), #($expr2.bytes[:expr1.length() - 1]),"
                        + " $expr3 WHERE #if($expr4) {e = :expr4} #else {e IS NULL}"
                        + "#for(variant in $expr5.bytes) { AND c = :variant}" // NL
                        + " GROUP BY $expr1 ASC");

        ExqlContext context = new ExqlContextImpl(1024);

        HashMap<String, Object> map = new HashMap<String, Object>();

        map.put("expr1", expr1);
        map.put("expr2", expr2);
        map.put("expr3", expr3);
        map.put("expr4", expr4);
        map.put("expr5", expr5);

        Assert.assertEquals("SELECT ?, ?, expr3 WHERE e = ? AND c = ? AND c = ? "
                + "AND c = ? AND c = ? AND c = ? GROUP BY expr1 ASC", // NL
                pattern.execute(context, map, map));

        Object[] expectArray = new Object[] { expr1.length(), expr2.getBytes()[expr1.length() - 1],
                expr4, expr5.getBytes()[0], expr5.getBytes()[1], expr5.getBytes()[2],
                expr5.getBytes()[3], expr5.getBytes()[4] };
        Object[] paramArray = context.getParams();

        Assert.assertEquals(expectArray.length, paramArray.length);
        for (int i = 0; i < expectArray.length; i++) {
            Assert.assertEquals(expectArray[i], paramArray[i]);
        }
    }
}
