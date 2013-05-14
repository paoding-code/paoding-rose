package net.paoding.rose.jade.exql;

import java.util.Date;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.paoding.rose.jade.statement.expression.ExqlContext;
import net.paoding.rose.jade.statement.expression.impl.ExqlContextImpl;

public class ExqlContextTests extends TestCase {

    public void testExqlContext() {

        Date current = new Date();

        ExqlContext context = new ExqlContextImpl(1024);

        context.fillText("WHERE uid = ");
        context.fillValue(102);
        context.fillText(" AND sid IN (");
        context.fillValue(new int[] { 11, 12, 24, 25, 31, 32, 33 });
        context.fillText(") AND (create_time > ");
        context.fillValue(current);
        context.fillText(" OR create_time <= ");
        context.fillValue(current);
        context.fillChar(')');

        Assert.assertEquals( // NL
                "WHERE uid = ? AND sid IN (?,?,?,?,?,?,?) "
                        + "AND (create_time > ? OR create_time <= ?)", // NL
                context.flushOut());

        Object[] expectArray = new Object[] { 102, 11, 12, 24, 25, 31, 32, 33, // NL
                current, current };
        Object[] paramArray = context.getParams();

        Assert.assertEquals(expectArray.length, paramArray.length);
        for (int i = 0; i < expectArray.length; i++) {
            Assert.assertEquals(expectArray[i], paramArray[i]);
        }
    }
}
