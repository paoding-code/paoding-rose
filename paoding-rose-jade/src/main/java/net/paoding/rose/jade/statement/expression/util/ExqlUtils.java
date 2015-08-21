package net.paoding.rose.jade.statement.expression.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import net.paoding.rose.jade.statement.expression.ExprResolver;
import net.paoding.rose.jade.statement.expression.ExqlPattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 实现表达式求值时的常用方法。
 * 
 * @author han.liao
 */
public class ExqlUtils {

    // 输出日志
    private static final Log logger = LogFactory.getLog(ExqlPattern.class);

    /**
     * 返回在语句输出的内容。
     * 
     * @param exprResolver - 使用的引擎
     * @param expression - 解释的表达式
     * 
     * @return 输出的内容
     */
    public static Object execExpr(ExprResolver exprResolver, String expression) {

        try {
            // 返回输出的内容
            return exprResolver.executeExpr(expression);

        } catch (Exception e) {

            // 输出日志
            if (logger.isDebugEnabled()) {
                logger.debug("Can't resolving expression: " + expression, e);
            }
        }

        return null;
    }

    /**
     * 检查对象是否有效。
     * 
     * @param obj - 检查的对象
     * 
     * @return true / false
     */
    public static boolean isValid(Object obj) {

        return asBoolean(obj);
    }

    /**
     * 将对象转换成: Boolean 值。
     * 
     * @param obj - 用于转换的对象
     * 
     * @return true / false
     */
    public static boolean asBoolean(Object obj) {

        if (obj == null) {

            return false; // 空值永远表示无效

        } else if (obj instanceof CharSequence) {

            return ((CharSequence) obj).length() > 0; // 字符串类型, 长度 > 0 表示有效

        } else if (obj instanceof Number) {

            return ((Number) obj).doubleValue() > 0; // 数字类型, >= 0 表示有效

        } else if (obj instanceof Boolean) {

            return ((Boolean) obj).booleanValue(); // Boolean 类型, true 表示有效

        } else if (obj instanceof Collection<?>) {

            return ((Collection<?>) obj).size() > 0; // 容器类型, 对象数量 > 0 表示有效

        } else if (obj.getClass().isArray()) {

            return Array.getLength(obj) > 0; // 数组类型, 对象数量 > 0 表示有效
        }

        return true; // 任意对象，引用不为空就判定有效
    }

    /**
     * 将对象转换成: java.lang.Object 数组。
     * 
     * @param obj 用于转换的对象
     * 
     * @return java.lang.Object 数组
     */
    public static Object[] asArray(Object obj) {

        if (obj == null) {

            return new Object[] {}; // 返回空数组

        } else if (obj.getClass().isArray()) {

            Class<?> componentType = obj.getClass().getComponentType();

            // 转换元素为基本类型的数组
            if (componentType.isPrimitive()) {

                final int length = Array.getLength(obj);

                Object[] array = new Object[length];

                for (int index = 0; index < length; index++) {
                    array[index] = Array.get(obj, index);
                }

                return array;
            }

            // 数组直接返回
            return (Object[]) obj;

        } else {

            return new Object[] { obj }; // 其他类型, 返回包含单个对象的数组
        }
    }

    /**
     * 将对象转换成: Collection 集合。
     * 
     * @param obj - 用于转换的对象
     * 
     * @return Collection 集合
     */
    public static Collection<?> asCollection(Object obj) {

        if (obj == null) {

            return Collections.EMPTY_SET; // 返回空集合

        } else if (obj.getClass().isArray()) {

            return Arrays.asList(asArray(obj));

        } else if (obj instanceof Collection<?>) {

            return (Collection<?>) obj; // List, Set, Collection 直接返回

        } else if (obj instanceof Map<?, ?>) {

            return ((Map<?, ?>) obj).entrySet(); // 映射表， 返回条目的集合

        } else {

            return Arrays.asList(obj); // 其他类型, 返回包含单个对象的集合
        }
    }
}
