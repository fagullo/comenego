package es.ua.labidiomas.corpus.listener;

import es.ua.labidiomas.corpus.util.Config;
import es.ua.labidiomas.corpus.index.Indexer;
import es.ua.labidiomas.corpus.index.IndexerFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author paco
 */
@WebListener
public class CorpusListener implements HttpSessionListener, ServletContextListener {

    private static final String[] TEXT_LANGUAGES = {"es", "en", "fr", "de", "ca"};

    /**
     * Create a empty list of upload temporal files and store it in the session.
     *
     * @param se
     */
    @Override
    public void sessionCreated(HttpSessionEvent se) {
    }

    /**
     * Remove all temporal files.
     *
     * @param se
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
    }

    /**
     * Intitialize the context.
     *
     * @param sce
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            Handler handler = new FileHandler("%t/corpus.log", 1048576, 2);
            Logger.getLogger("").addHandler(handler);
            Config.loadConfig(sce);
            System.out.println("Iniciando aplicación en " + Config.WEB_APP_PATH);
            java.util.logging.Logger.getLogger(Indexer.class.getName()).log(Level.INFO, "Indexacion finalizada.");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CorpusListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(CorpusListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(CorpusListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CorpusListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Remove all created sessions.
     *
     * @param sce
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
