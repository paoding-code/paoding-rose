package net.paoding.rose.jade.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 对于sql中没有含有散表参数的或散表参数名称和配置的散表名称不一致的，通过将 ShardBy 配置在这个参数上，表示使用该参数进行散表.
 * <p>
 * &reg;SQL(&quot;....where name like :1&quot;)<br>
 * public List<Xxx> find(String likeValue, &reg;ShardBy String pageId);
 * 
 * <pre>
 * </pre>
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ShardBy {
}
