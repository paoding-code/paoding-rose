package net.paoding.rose.jade;

import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.annotation.SQL;
import net.paoding.rose.jade.annotation.ShardBy;
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
public class BatchUpdateTest {

    @DAO
    interface UserDAO {

        @SQL("create table user (id int, name varchar(200));")
        void createTable();

        @SQL("create table $user (id int, name varchar(200));")
        void createTableAt(@ShardBy int hash);

        @SQL("insert into user (id, name) values(:1.id, :1.name);")
        int[] insertIntoOneTable(List<User> users);

        @SQL("insert into $user (id, name) values(:1.id, :1.name);")
        int[] insertIntoHashTables(@ShardBy("id") List<User> users);

        @SQL("select id, name from user order by id")
        List<User> findAll();

        @SQL("select id, name from $user order by id")
        List<User> findAllAt(@ShardBy int hash);

        @SQL("delete from user")
        void deleteAll();

    }

    public static class User {

        long id;

        String name;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public int hashCode() {
            return (int) id;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof User)) {
                return false;
            }
            User that = (User) obj;
            return this.id == that.id && this.name.equals(that.name);
        }

        @Override
        public String toString() {
            return "user:" + id + "(" + name + ")";
        }
    }

    @Before
    public void init() {

    }

    @Test
    public void testBatchInsertIntoHashTables() {
        UserDAO dao = getHashTables();
        //
        List<User> users0 = new LinkedList<BatchUpdateTest.User>();
        List<User> users1 = new LinkedList<BatchUpdateTest.User>();
        User user = new User();
        user.id = 1;
        user.name = "zhiliang.wang";
        users1.add(user);
        //
        user = new User();
        user.id = 2;
        user.name = "helen.wang";
        users0.add(user);
        //
        user = new User();
        user.id = 3;
        user.name = "bad boy";
        users1.add(user);
        //

        List<User> users = new LinkedList<BatchUpdateTest.User>();
        users.addAll(users0);
        users.addAll(users1);
        dao.insertIntoHashTables(users);
        //
        List<User> users0FromDB = dao.findAllAt(0);
        List<User> users1FromDB = dao.findAllAt(1);
        Assert.assertEquals(users0, users0FromDB);
        Assert.assertEquals(users1, users1FromDB);
    }

    @Test
    public void testBatchInsertIntoOneTable() {
        UserDAO dao = getOneTable();
        //
        List<User> users = new LinkedList<BatchUpdateTest.User>();
        User user = new User();
        user.id = 1;
        user.name = "zhiliang.wang";
        users.add(user);
        //
        user = new User();
        user.id = 2;
        user.name = "helen.wang";
        users.add(user);
        //
        user = new User();
        user.id = 3;
        user.name = "bad boy";
        users.add(user);
        //
        dao.insertIntoOneTable(users);
        //
        List<User> usersFromDB = dao.findAll();
        Assert.assertEquals(users, usersFromDB);
    }

    private UserDAO getOneTable() {
        DataSource dataSource = DataSources.createUniqueDataSource();
        JadeFactory factory = new JadeFactory(dataSource);
        UserDAO dao = factory.create(UserDAO.class);
        dao.createTable();
        return dao;
    }

    /**
     * 散表的一个实现
     * @return
     */
    private UserDAO getHashTables() {
        DataSource dataSource = DataSources.createUniqueDataSource();
        JadeFactory factory = new JadeFactory(dataSource);
        factory.addInterpreter(new ShardInterpreter('$', 2));// Spring下可以配置ShardInterpreter在xml中让jade自动载入
        UserDAO dao = factory.create(UserDAO.class);
        dao.createTableAt(0);
        dao.createTableAt(1);
        return dao;
    }
}
