/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.labidiomas.corpus.database;

import es.ua.db.Database;
import es.ua.db.DatabaseException;
import es.ua.db.DatabasePool;
import es.ua.db.MySQLDatabase;
import es.ua.labidiomas.corpus.util.Config;

/**
 *
 * @author paco
 */
public class DataBaseHandler {

    private static DataBaseHandler instance;
    private static DatabasePool pool;
    
    private static final int NUM_CONNECTIONS = 5;

    private DataBaseHandler() throws DatabaseException {
        Database database = new MySQLDatabase(Config.DB_HOST, Config.DB_PORT, Config.DB_USER, Config.DB_PASS, Config.DB_NAME);
        pool = new DatabasePool(database, NUM_CONNECTIONS);
    }

    public static DataBaseHandler getInstance() throws DatabaseException {
        if (instance == null) {
            instance = new DataBaseHandler();
        }

        return instance;
    }
    
    public Database getDatabase() throws DatabaseException {
        return pool.get();
    }
    
    public void release(Database database) throws DatabaseException {
        pool.release(database);
    }
}
