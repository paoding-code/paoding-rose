package net.paoding.rose.jade.provider;

import java.util.Map;

import net.paoding.rose.jade.statement.DAOMetaData;

import junit.framework.Assert;
import junit.framework.TestCase;

public class DefinitionTests extends TestCase {

    public void testDefinition() {

        DAOMetaData definition = new DAOMetaData(Character.class);
        Assert.assertEquals("java.lang.Character", definition.toString());

        // 输出所有常量
        System.out.println("Class constants: ");
        Map<String, ?> consts = definition.getConstants();
        for (Map.Entry<String, ?> entry : consts.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }

        // 效验一些常量
        Assert.assertEquals(Character.CONTROL, consts.get("CONTROL"));
        Assert.assertEquals(Character.DECIMAL_DIGIT_NUMBER, consts.get("DECIMAL_DIGIT_NUMBER"));
        Assert.assertEquals(Character.MIN_CODE_POINT, consts.get("MIN_CODE_POINT"));
        Assert.assertEquals(Character.MAX_CODE_POINT, consts.get("MAX_CODE_POINT"));
        Assert.assertEquals(Character.MIN_HIGH_SURROGATE, consts.get("MIN_HIGH_SURROGATE"));
        Assert.assertEquals(Character.MAX_LOW_SURROGATE, consts.get("MAX_LOW_SURROGATE"));
        Assert.assertEquals(Character.FORMAT, consts.get("FORMAT"));
        Assert.assertEquals(Character.SIZE, consts.get("SIZE"));
    }
}
