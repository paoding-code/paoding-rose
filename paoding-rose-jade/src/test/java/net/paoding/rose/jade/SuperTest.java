package net.paoding.rose.jade;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.annotation.SQL;
import net.paoding.rose.jade.context.application.JadeFactory;

/**
 * 验证继承功能
 * 
 * @author qieqie
 * 
 */
public class SuperTest {
    
    @DAO 
    interface BaseDAO<XX, E extends Serializable> extends Serializable {

        @SQL("select id, name from user where id=:1")
        E superGetById(Long id);
        
        @SQL("select id, name from user limit 100")
        List<E> superFind();
    }
    
    @DAO 
    interface BaseDAO2<T extends Serializable> extends BaseDAO<Object, T> {
        //
    }

    @DAO
    interface UserDAO extends BaseDAO2<User> {
        // 准备数据 (DDL)

        @SQL("create table user (id int, name varchar(200));")
        void createTable();

        @SQL("insert into user (id, name) values(:1.id, :1.name);")
        int[] insert(List<User> users);
        
    }

    public static class User implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 5495017983473363962L;

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
    public void test() {
        UserDAO dao = getUserDAO();
        //
        List<User> users = new LinkedList<SuperTest.User>();
        User user1 = new User();
        user1.id = 1;
        user1.name = "zhiliang.wang";
        users.add(user1);
        //
        User user2 = new User();
        user2.id = 2;
        user2.name = "helen.wang";
        users.add(user2);
        //
        User user3 = new User();
        user3.id = 3;
        user3.name = "bad boy";
        users.add(user3);
        //

        dao.insert(users);
        
        Assert.assertEquals(user3, dao.superGetById(3L));
        Assert.assertEquals(3, dao.superFind().size());
        Assert.assertTrue(CollectionUtils.isEqualCollection(users, dao.superFind()));
    }



    /**
     * @return
     */
    private UserDAO getUserDAO() {
        DataSource dataSource = DataSources.createUniqueDataSource();
        JadeFactory factory = new JadeFactory(dataSource);
        UserDAO dao = factory.create(UserDAO.class);
        dao.createTable();
        return dao;
    }
}
