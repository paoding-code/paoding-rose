package net.paoding.rose.jade;

import java.beans.PropertyDescriptor;

import net.paoding.rose.jade.annotation.ShardBy;
import net.paoding.rose.jade.statement.Interpreter;
import net.paoding.rose.jade.statement.StatementRuntime;

import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.Order;

/**
 * 这个类是散表实现的一个示例，您可以copy到您的项目中，再详细实现一下，对不同的表设置相应的散表策略<br>
 * 真正的表名可以通过runtime.getProperty(ShardAttributes.targetTable)知道，
 * 这个属性可以和DataSourceFactory结合使用，从而实现散库；
 * 
 * @author qieqie
 * 
 */
@Order(-10)
public class ShardInterpreter implements Interpreter {

    private int shardCount = 100;//默认散表100份

    private char prefixChar = '$';//默认的在@SQL中写要散的表名要以$开始

    public ShardInterpreter() {
    }

    public ShardInterpreter(char prefixChar, int shardCount) {
        this.setPrefixChar(prefixChar);
        this.setShardCount(shardCount);
    }

    public void setPrefixChar(char prefixChar) {
        this.prefixChar = prefixChar;
    }

    public char getPrefixChar() {
        return prefixChar;
    }

    public void setShardCount(int shardCount) {
        this.shardCount = shardCount;
    }

    public int getShardCount() {
        return shardCount;
    }

    //@SQL("insert into $user(id, name) values(:1.id, :1.name);")
    //-->"insert into $user_xx (id, name) values(:1.id, :1.name);
    @Override
    public void interpret(StatementRuntime runtime) {
        String sql = runtime.getSQL();
        int index = 0;
        while (true) {
            index = sql.indexOf(prefixChar, index);
            if (index == -1) {
                break;
            }
            int end = sql.length() - 1;
            for (int i = index + 1; i < sql.length(); i++) {
                char ch = sql.charAt(i);
                if (ch == '_') {
                    continue;
                }
                if (ch >= '0' && ch <= '9') {
                    continue;
                }
                if (ch >= 'a' && ch <= 'z') {
                    continue;
                }
                if (ch >= 'A' && ch <= 'z') {
                    continue;
                }
                end = i;
                break;
            }
            String origin = sql.substring(index, end);
            String target = convert(origin, runtime);
            if (target != null && target.length() > 0) {
                runtime.setProperty(ShardAttributes.originTable, origin);
                runtime.setProperty(ShardAttributes.targetTable, target);
                sql = sql.substring(0, index) + target + sql.substring(end);
                index += target.length();
            } else {
                index = end;
            }
        }
        runtime.setSQL(sql);
    }

    /**
     * 重新实现此方法自定义转化规则
     * 
     * @param term
     * @param runtime
     * @return
     */
    protected String convert(String term, StatementRuntime runtime) {
        int shardIndex = runtime.getMetaData().getShardByIndex();
        if (shardIndex < 0) {
            throw new IllegalStateException("nou found @" + ShardBy.class.getSimpleName() + " in "
                    + runtime.getMetaData().getDAOMetaData().getDAOClass().getName() + "#"
                    + runtime.getMetaData().getMethod().getName());
        }

        Object objectShardBy = runtime.getParameters().get(":" + (shardIndex + 1));
        Number value;
        if (objectShardBy instanceof Number) {
            value = (Number) objectShardBy;
        } else {
            ShardBy annotationShardBy = runtime.getMetaData().getShardBy();
            String propertyName = annotationShardBy.value();
            PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(
                    objectShardBy.getClass(), propertyName);
            try {
                value = (Number) propertyDescriptor.getReadMethod().invoke(objectShardBy);
            } catch (Exception e) {
                throw new IllegalStateException(runtime.getMetaData().getDAOMetaData()
                        .getDAOClass().getName()
                        + "#" + runtime.getMetaData().getMethod().getName(), e);
            }
        }
        int shard = 1 + value.intValue() % shardCount;
        return term.substring(1) + "_" + shard;
    }
}
