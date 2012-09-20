//package net.paoding.rose.jade.datasource.routing;
//
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.sql.Statement;
//
//import javax.sql.DataSource;
//
//import net.paoding.rose.jade.dataaccess.routing.ConnectionLocator;
//import net.paoding.rose.jade.dataaccess.routing.RoutingConnection;
//
//import org.springframework.jdbc.datasource.DriverManagerDataSource;
//
//public class ConnectionLocatorMock implements ConnectionLocator {
//
//    private DataSource dataSource1;
//
//    private DataSource dataSource2;
//
//    public ConnectionLocatorMock() {
//        this.dataSource1 = createDataSource(1, "node1");
//        this.dataSource2 = createDataSource(2, "node2");
//    }
//
//    @Override
//    public Connection getConnection(RoutingConnection proxy) throws SQLException {
//        String node = proxy.getClientInfo(RoutingConnection.NODE);
//        if ("1".equals(node)) {
//            return dataSource1.getConnection();
//        } else if ("2".equals(node)) {
//            return dataSource2.getConnection();
//        }
//        throw new SQLException("illegal node " + node);
//    }
//
//    private DataSource createDataSource(int i, String name) {
//        DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setUrl("jdbc:hsqldb:mem:node" + i);
//        dataSource.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
//
//        Connection conn;
//        try {
//            conn = dataSource.getConnection();
//            Statement st = conn.createStatement();
//            st.execute("create table user (id int, name varchar(200));");
//            st.execute("insert into user (id, name) values(" + i + ", '" + name + "' );");
//            st.close();
//            conn.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return dataSource;
//    }
//
//}
