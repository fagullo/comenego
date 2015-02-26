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
//            this._initializeIndexDirectory();
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

    private void _indexTexts(Connection conexion, ResultSet texts, String lang) throws SQLException, IOException {
        Indexer indexer = IndexerFactory.getInstance().getIndexer(lang);
        while (texts.next()) {
            int paragraphID = texts.getInt("id");
            String textSelect = "SELECT content, text_id FROM paragraph WHERE id = ?;";
            PreparedStatement searchPS = conexion.prepareStatement(textSelect); //Obtener los textos uno a uno para no desbordar la pila.
            searchPS.setInt(1, paragraphID);
            ResultSet searchRS = searchPS.executeQuery();
            while (searchRS.next()) {
                String content = searchRS.getString("content");
                ArrayList<String> discourses = new ArrayList<String>();
                int textID = searchRS.getInt("text_id");
                PreparedStatement discoursesPS = conexion.prepareStatement("SELECT dis.code "
                        + "FROM corpus.discourse_texts dt, corpus.text txt, corpus.discourse dis "
                        + "WHERE dt.text_id = txt.id AND dt.discourse_id = dis.id AND txt.id = ?;");
                discoursesPS.setDouble(1, textID);
                ResultSet discoursesRS = discoursesPS.executeQuery();
                while (discoursesRS.next()) {
                    discourses.add(discoursesRS.getString("code"));
                }
                discoursesRS.close();
                discoursesPS.close();
                indexer.index(textID, paragraphID, content, discourses);

            }
            searchRS.close();
            searchPS.close();
        }
        texts.close();
        indexer.close();
    }
    
        private void _indexNGramas(Connection conexion, ResultSet texts, String lang) throws SQLException, IOException {
        Indexer indexer = IndexerFactory.getInstance().getNGrammaIndexer(lang);
        while (texts.next()) {
            int paragraphID = texts.getInt("id");
            String textSelect = "SELECT content, text_id FROM paragraph WHERE id = ?;";
            PreparedStatement searchPS = conexion.prepareStatement(textSelect); //Obtener los textos uno a uno para no desbordar la pila.
            searchPS.setInt(1, paragraphID);
            ResultSet searchRS = searchPS.executeQuery();
            while (searchRS.next()) {
                String content = searchRS.getString("content");
                ArrayList<String> discourses = new ArrayList<String>();
                int textID = searchRS.getInt("text_id");
                PreparedStatement discoursesPS = conexion.prepareStatement("SELECT dis.code "
                        + "FROM corpus.discourse_texts dt, corpus.text txt, corpus.discourse dis "
                        + "WHERE dt.text_id = txt.id AND dt.discourse_id = dis.id AND txt.id = ?;");
                discoursesPS.setDouble(1, textID);
                ResultSet discoursesRS = discoursesPS.executeQuery();
                while (discoursesRS.next()) {
                    discourses.add(discoursesRS.getString("code"));
                }
                discoursesRS.close();
                discoursesPS.close();
                _obtainNGramas(textID, paragraphID, content, discourses, indexer);
            }
            searchRS.close();
            searchPS.close();
        }
        texts.close();
        indexer.close();
    }
        
        
    private void _obtainNGramas(int textID, int paragraphID, String content, ArrayList<String> discourses, Indexer indexer) throws IOException {
        String words[] = content.split("\\s+");
        
        for ( int i = 0; i < words.length; i++ ) {
            String word = words[i];
            String b1 = (i - 1) < 0 ? "" : words[i - 1];
            String b2 = (i - 2) < 0 ? "" : words[i - 2];
            String b3 = (i - 3) < 0 ? "" : words[i - 3];
            String b4 = (i - 4) < 0 ? "" : words[i - 4];
            String a1 = (i + 1) >= words.length ? "" : words[i + 1];
            String a2 = (i + 2) >= words.length ? "" : words[i + 2];
            String a3 = (i + 3) >= words.length ? "" : words[i + 3];
            String a4 = (i + 4) >= words.length ? "" : words[i + 4];
            indexer.indexNGramas(textID, paragraphID, word, discourses, a1, a2, a3, a4, b1, b2, b3, b4);
        }
    }

    private void _initializeIndexDirectory() throws IOException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conexion = DriverManager.getConnection(Config.CONEXION_STRING, Config.DB_USER, Config.DB_PASS);
            for (String lang : TEXT_LANGUAGES) {
                java.util.logging.Logger.getLogger(Indexer.class.getName()).log(Level.INFO, "Indexando textos del idioma {0}", lang);
                PreparedStatement countPS = conexion.prepareStatement("SELECT p.id FROM  paragraph p, text t, language l WHERE p.text_id = t.id AND language_id = l.id AND l.shortname = ?;");
                countPS.setString(1, lang);
                ResultSet texts = countPS.executeQuery();
//                _indexTexts(conexion, texts, lang);
                _indexNGramas(conexion, texts, lang);
                countPS.close();
            }
            conexion.close();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
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
