/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.labidiomas.corpus.database;

import es.ua.labidiomas.corpus.util.Config;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

/**
 *
 * @author paco
 */
public final class DataBaseHandler {

    private static final BasicDataSource dataSource = new BasicDataSource();

    static {
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl(Config.CONEXION_STRING);
        dataSource.setUsername(Config.DB_USER);
        dataSource.setPassword(Config.DB_PASS);
    }

    private DataBaseHandler() {
        
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

}
