package net.paoding.rose.jade.context.application;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class JadeFactoryTest {

    static UserDAO dao;

    @BeforeClass
    public static void init() {
        JadeFactory factory = new JadeFactory(createDataSource());
        dao = factory.create(UserDAO.class);
    }

    @Test
    public void test1() {
        Assert.assertEquals("zhiliang1", dao.getName(1));
    }

    @Test
    public void test2() {
        String[] names = dao.getNames();
        Assert.assertEquals(2, names.length);
        Assert.assertEquals("zhiliang1", names[0]);
        Assert.assertEquals("zhiliang2", names[1]);
    }

    private static DataSource createDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:hsqldb:mem:jadeFactory");
        dataSource.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
        Connection conn;
        try {
            conn = dataSource.getConnection();
            Statement st = conn.createStatement();
            st.execute("create table user (id int, name varchar(200));");
            st.execute("insert into user (id, name) values(1, 'zhiliang1' );");
            st.execute("insert into user (id, name) values(2, 'zhiliang2' );");
            st.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataSource;
    }
}
