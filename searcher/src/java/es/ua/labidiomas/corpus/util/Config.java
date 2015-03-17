/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.labidiomas.corpus.util;

import es.upv.xmlutils.XMLUtils;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.servlet.ServletContextEvent;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author odb
 */
public class Config {

    static public String WEB_APP_PATH;
    static public String INDEXES_PATH;
    static public String CONFIG_FILE;
    static public String DB_HOST;
    static public Integer DB_PORT;
    static public String DB_USER;
    static public String DB_PASS;
    static public String DB_NAME;
    static public String LOG_FILE;
    static public final String FILE_SEPARATOR = System.getProperty("file.separator");
    static public String CONEXION_STRING;

    public static void loadConfig(ServletContextEvent sce) throws FileNotFoundException, IOException, ParserConfigurationException, SAXException {
        WEB_APP_PATH = sce.getServletContext().getRealPath("/");
        if ( !WEB_APP_PATH.endsWith(FILE_SEPARATOR) ) {
            WEB_APP_PATH += FILE_SEPARATOR;
        }
        CONFIG_FILE = WEB_APP_PATH + "config" + FILE_SEPARATOR + "config.xml";
        loadConfigFile(WEB_APP_PATH, CONFIG_FILE);
        CONEXION_STRING = "jdbc:mysql://" + DB_HOST + "/" + DB_NAME;
    }

    private static void loadConfigFile(String webAppPath, String configFile) throws FileNotFoundException, IOException, ParserConfigurationException, SAXException {
        Document doc = XMLUtils.loadXML(configFile);
        Element config = doc.getDocumentElement();
        // Set the index directory
        Element indexElem = XMLUtils.getElementNamed(config, "indexes_path");
        INDEXES_PATH = indexElem == null ? webAppPath + "index" : indexElem.getTextContent();
        // Set the log file
        Element logElem = XMLUtils.getElementNamed(config, "log_file");
        LOG_FILE = logElem == null ? webAppPath + "log" : getAbsolutePath(webAppPath, logElem.getTextContent());
        // Set the Host
        Element hostElem = XMLUtils.getElementNamed(config, "db_host");
        DB_HOST = hostElem == null ? DB_HOST = "localhost" : hostElem.getTextContent();
        // Set the Port
        Element portElem = XMLUtils.getElementNamed(config, "db_port");
        DB_PORT = portElem == null ? 3306 : Integer.parseInt(portElem.getTextContent());
        //Set the admin user of soneo
        Element userElem = XMLUtils.getElementNamed(config, "db_user");
        DB_USER = userElem == null ? "soneo" : userElem.getTextContent();
        //Set the admin password of soneo
        Element passElem = XMLUtils.getElementNamed(config, "db_pass");
        DB_PASS = passElem == null ? "soneo" : passElem.getTextContent();
        // Set the dataBase name
        Element dataBaseElem = XMLUtils.getElementNamed(config, "db_name");
        DB_NAME = dataBaseElem == null ? "soneo3" : dataBaseElem.getTextContent();
    }

    private static String getAbsolutePath(String base, String path) {
        if (!path.startsWith("/")) {
            return base + path;
        }
        return path;
    }

}
