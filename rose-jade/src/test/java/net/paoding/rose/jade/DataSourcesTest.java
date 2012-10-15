package net.paoding.rose.jade;

import javax.sql.DataSource;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.annotation.SQL;
import net.paoding.rose.jade.context.application.JadeFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 通过集成DAO和JadeFactory，验证 {@link DataSources}的可用
 * 
 * @author qieqie
 * 
 */
public class DataSourcesTest {

    @DAO
    interface UserDAO {

        @SQL("create table user (id int, name varchar(200));")
        void createTable();

        @SQL("insert into user (id, name) values(:1, :2);")
        void insert(int id, String name);

        @SQL("select name from user where id=:1")
        String getName(int id);

        @SQL("select name from user order by id asc")
        String[] findNames();
    }

    // init方法负责初始化dao
    private UserDAO dao;

    @Before
    public void init() {
        DataSource dataSource = DataSources.createUniqueDataSource();
        JadeFactory factory = new JadeFactory(dataSource);
        dao = factory.create(UserDAO.class);
        dao.createTable();
        dao.insert(1, "zhiliang1");
        dao.insert(2, "zhiliang2");
    }

    @Test
    public void testGetName() {
        Assert.assertEquals("zhiliang1", dao.getName(1));
    }

    @Test
    public void testFindNames() {
        String[] names = dao.findNames();
        Assert.assertEquals(2, names.length);
        Assert.assertEquals("zhiliang1", names[0]);
        Assert.assertEquals("zhiliang2", names[1]);
    }

}
