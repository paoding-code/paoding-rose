//package net.paoding.rose.jade.datasource.routing;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.List;
//
//import junit.framework.Assert;
//
//import net.paoding.rose.jade.dataaccess.routing.RoutingConnection;
//import net.paoding.rose.jade.dataaccess.routing.RoutingDataSource;
//
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.core.PreparedStatementCreator;
//import org.springframework.jdbc.core.RowMapper;
//
//public class RoutingDataSourceTest {
//
//    static RoutingDataSource dataSource;
//
//    static JdbcTemplate jdbc;
//
//    @BeforeClass
//    public static void init() {
//        dataSource = new RoutingDataSource(new ConnectionLocatorMock());
//        jdbc = new JdbcTemplate(dataSource);
//    }
//
//    @Test
//    public void test1() throws SQLException {
//        test(1, "node1");
//        test(2, "node2");
//    }
//
//    @SuppressWarnings("unchecked")
//    private void test(final int id, String name) throws SQLException {
//        PreparedStatementCreator psc = new PreparedStatementCreator() {
//
//            @Override
//            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
//                con.setClientInfo(RoutingConnection.NODE, String.valueOf(id));
//                return con.prepareStatement("select id, name from user where id=" + id);
//            }
//        };
//        RowMapper rowMapper = new RowMapper() {
//
//            @Override
//            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
//                User user = new User();
//                user.id = rs.getInt(1);
//                user.name = rs.getString(2);
//                return user;
//            }
//        };
//
//        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
//        List<User> users = jdbc.query(psc, rowMapper);
//        Assert.assertEquals(1, users.size());
//        User user = users.get(0);
//        Assert.assertEquals(id, user.id);
//        Assert.assertEquals(name, user.name);
//    }
//
//    static class User {
//
//        int id;
//
//        String name;
//    }
//}
