package SqlConnector;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class SqlCommonDb {
    public static String filePath = "resources/sqlconfig.properties";
    private static SqlCommonDb connectDb;
    private static Connection conn;


    public SqlCommonDb(){
        try {
            init();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void init() throws SQLException {
        Properties pro = new Properties();
        try{
            FileInputStream fis = new FileInputStream(new File(filePath));
            pro.load(fis);
            fis.close();
        }
        catch (Exception e){

        }
        String user = pro.getProperty("user");
        String pwd = pro.getProperty("password");
        String host = pro.getProperty("host");
        String dbname = pro.getProperty("database");
        String url = "jdbc:mysql://" + host + "/" +dbname +"?characterEncoding=UTF-8";
        conn = DriverManager.getConnection(url, user, pwd);
    }

    public static synchronized SqlCommonDb getInstance() throws SQLException {
        if(connectDb == null || conn.isClosed()){
            connectDb = new SqlCommonDb();
        }
        return connectDb;
    }

    public Connection connectDb() throws SQLException {
        if(conn.isClosed()){
            connectDb = new SqlCommonDb();
        }
        return conn;
    }

}
