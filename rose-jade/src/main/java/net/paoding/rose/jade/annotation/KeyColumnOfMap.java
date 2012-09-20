package net.paoding.rose.jade.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Jade支持DAO方法返回Map形式的，默认情况下Jade选取第一列作为Map的key。
 * <p>
 * 我们推荐您在写返回map的SQL时，把key放到第一列，但是如果真不想这样做，你可以通过本注解，即{@link KeyColumnOfMap}
 * 进行指定。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 */
@Target( { ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface KeyColumnOfMap {

    /**
     * 指出要被当成map key的字段名称
     * 
     * @return
     */
    String value();
}
