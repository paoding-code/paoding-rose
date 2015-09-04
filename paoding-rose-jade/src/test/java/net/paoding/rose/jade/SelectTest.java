package net.paoding.rose.jade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.annotation.SQL;
import net.paoding.rose.jade.annotation.SQLParam;
import net.paoding.rose.jade.context.application.JadeFactory;

/**
 * 
 * 验证查询返回类型能力
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
public class SelectTest {

    @DAO
    interface UserDAO {
        
        // 准备数据 (DDL)

        @SQL("create table user (id int, name varchar(200));")
        void createTable();

        @SQL("insert into user (id, name) values(:1.id, :1.name);")
        int[] insert(List<User> users);
        
        // 查询数据(DML)

        @SQL("select id, name from user order by id")
        List<User> findAll();

        @SQL("select id, name from user order by id")
        Collection<User> findAllAsCollection();

        @SQL("select id, name from user order by id")
        ArrayList<User> findAllAsArrayList();

        @SQL("select id, name from user order by id")
        LinkedList<User> findAllAsLinkedList();

        @SQL("select id, name from user order by id")
        Iterable<User> findAllAsIterable();

        @SQL("select name,id from user ")
        Map<String, Long> findAllAsAsMap();

        @SQL("select id, name from user ")
        HashMap<Long, String> findAllAsAsHashMap();

        @SQL("select id, name from user ")
        Hashtable<Long, User> findAllAsAsHashtable();

        @SQL("select id, name from user where id=:id")
        User getById(@SQLParam("id") long id);

        @SQL("select count(*) from user")
        int countUser();

        @SQL("select name from user where id=:id")
        String getUserName(@SQLParam("id") long id);

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
    public void test() {
        UserDAO dao = getUserDAO();
        //
        List<User> users = new LinkedList<SelectTest.User>();
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
        //
        Assert.assertEquals(3, dao.countUser());
        Assert.assertEquals(3, dao.findAll().size());
        Assert.assertTrue(CollectionUtils.isEqualCollection(users, dao.findAll()));
        
        Assert.assertEquals(3, dao.findAllAsArrayList().size());
        Assert.assertTrue(CollectionUtils.isEqualCollection(users, dao.findAllAsArrayList()));
        
        Assert.assertEquals(3, dao.findAllAsLinkedList().size());
        Assert.assertTrue(CollectionUtils.isEqualCollection(users, dao.findAllAsLinkedList()));
        
        Assert.assertEquals(3, dao.findAllAsCollection().size());
        Assert.assertTrue(CollectionUtils.isEqualCollection(users, dao.findAllAsCollection()));
        
        Assert.assertEquals(3, dao.findAllAsAsMap().size());
        Assert.assertEquals(Long.valueOf(user1.getId()), dao.findAllAsAsMap().get(user1.getName()));
        Assert.assertEquals(Long.valueOf(user2.getId()), dao.findAllAsAsMap().get(user2.getName()));
        Assert.assertEquals(Long.valueOf(user3.getId()), dao.findAllAsAsMap().get(user3.getName()));
        
        Assert.assertEquals(3, dao.findAllAsAsHashMap().size());
        Assert.assertEquals(user1.getName(), dao.findAllAsAsHashMap().get(1L));
        Assert.assertEquals(user2.getName(), dao.findAllAsAsHashMap().get(2L));
        Assert.assertEquals(user3.getName(), dao.findAllAsAsHashMap().get(3L));
        
        Assert.assertEquals(3, dao.findAllAsAsHashtable().size());
        Assert.assertEquals(user1, dao.findAllAsAsHashtable().get(1L));
        Assert.assertEquals(user2, dao.findAllAsAsHashtable().get(2L));
        Assert.assertEquals(user3, dao.findAllAsAsHashtable().get(3L));
        
        Iterator<User> iterable = dao.findAllAsIterable().iterator();
        List<User> users2 = new LinkedList<SelectTest.User>(users);
        while (iterable.hasNext()) {
            User item = (User) iterable.next();
            Assert.assertTrue(users2.contains(item));
            users2.remove(item);
        }
        
        

        Assert.assertEquals("zhiliang.wang", dao.getById(1).getName());
        Assert.assertEquals("helen.wang", dao.getById(2).getName());
        Assert.assertEquals("bad boy", dao.getById(3).getName());
        
        Assert.assertEquals("zhiliang.wang", dao.getUserName(1));
        Assert.assertEquals("helen.wang", dao.getUserName(2));
        Assert.assertEquals("bad boy", dao.getUserName(3));
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
