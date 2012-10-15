package net.paoding.rose.jade;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * 为可能的测试需要提供各种DataSource实现
 * <p>
 * 
 * 这些DataSource使用hsqldb实现，均为内存数据库
 * 
 * @author qieqie
 * 
 */
public class DataSources {

    public static Map<String, DataSource> instances = new HashMap<String, DataSource>();

    /**
     * 获取或创建给定名称的一个DataSource实例
     * 
     * @param name
     * @return
     */
    public static DataSource getDataSource(String name) {
        DataSource instance = instances.get(name);
        if (instance == null) {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setUrl("jdbc:hsqldb:mem:" + name);
            dataSource.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
            instance = dataSource;
            instances.put(name, instance);
        }
        return instance;
    }

    /**
     * 创建一个新的、唯一的DataSource实例
     * 
     * @return
     */
    public static DataSource createUniqueDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        int random = new Random(Integer.MAX_VALUE).nextInt();
        dataSource.setUrl("jdbc:hsqldb:mem:" + System.currentTimeMillis() + "-" + random);
        dataSource.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
        return dataSource;
    }
}
