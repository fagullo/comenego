/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.corpus.indexer;

import es.upv.xmlutils.XMLUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author paco
 */
public class Indexer {

    /**
     * The writer.
     */
    private IndexWriter writer;
    /**
     * The analyzer.
     */
    protected Analyzer analyzer;
    /**
     * The path where the indexes will be stored.
     */
    protected String indexPath;
    /**
     * The path to the log file.
     */
    public static final File logFile = new File("config/log.txt");

    private static final int NGRAM_SIZE = 4;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, FileNotFoundException, ParserConfigurationException, SAXException, SQLException, ClassNotFoundException {
        Config.loadConfig("config/config.xml");
//        Indexer indexer = new Indexer("index");
        Indexer indexer = new Indexer("/home/paco/Documentos/comenego/index");
        indexer.readIndexConfig("config" + Config.FILE_SEPARATOR + "config.xml");
    }

    /**
     * Constructor.
     *
     * @param indexPath Index directory path.
     * @throws IOException if an IO problem occurs.
     */
    public Indexer(String indexPath) throws IOException {
        this.indexPath = indexPath;
    }

    /**
     * Reads the config file where the indexes are specified.
     *
     * @param configFile the path to the config file.
     * @throws IOException
     * @throws FileNotFoundException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws java.sql.SQLException
     * @throws java.lang.ClassNotFoundException
     */
    public void readIndexConfig(String configFile) throws IOException, FileNotFoundException, ParserConfigurationException, SAXException, SQLException, ClassNotFoundException {
        org.w3c.dom.Document doc = XMLUtils.loadXML(configFile);
        Element config = doc.getDocumentElement();

        Element indexesElement = XMLUtils.getElementNamed(config, "indexconfig");

        NodeList langs = indexesElement.getElementsByTagName("lang");
        Class.forName("com.mysql.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection(Config.CONEXION_STRING, Config.DB_USER, Config.DB_PASS)) {
            for (int i = 0; i < langs.getLength(); i++) {
                Element lang = (Element) langs.item(i);
                _indexLanguage(connection, lang);
            }
        }
    }

    private void _indexLanguage(Connection connection, Element langNode) throws SQLException, ClassNotFoundException, IOException {
        String lang = langNode.getAttribute("id");

        FileUtils.writeStringToFile(logFile, "Indexando textos del idioma " + lang + "\n", true);
//        try (PreparedStatement countPS = connection.prepareStatement("SELECT p.id FROM  paragraph p, text t, language l WHERE p.text_id = t.id AND language_id = l.id AND l.shortname = ? limit 50")) {
        try (PreparedStatement countPS = connection.prepareStatement("SELECT p.id FROM  paragraph p, text t, language l WHERE p.text_id = t.id AND language_id = l.id AND l.shortname = ?")) {
            countPS.setString(1, lang);
            try (ResultSet texts = countPS.executeQuery()) {

                NodeList indexes = langNode.getElementsByTagName("index");

                for (int i = 0; i < indexes.getLength(); i++) {
                    Element index = (Element) indexes.item(i);
                    String type = index.getAttribute("type");
                    switch (type) {
                        case "ngram":
//                            for (int size = 1; size <= NGRAM_SIZE; size++) {
//                                _indexNGramas(connection, texts, lang, size, true);
//                                texts.first();
//                                _indexNGramas(connection, texts, lang, size, false);
//                                texts.first();
//                            }
                            break;
                        case "paragraph":
                            _indexTexts(connection, texts, lang, true);
                            texts.first();
                            _indexTexts(connection, texts, lang, false);
                            break;
                        case "title":
//                            _indexTitle(connection, lang);
                            break;
                    }
                    texts.first();
                }
            }
        }
    }

    private void _indexTexts(Connection conexion, ResultSet texts, String lang, boolean lemma) throws SQLException, IOException {
        int counter = 0;
        Indexer indexer = IndexerFactory.getInstance().getIndexer(lang, lemma);
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
            if (++counter % 10000 == 0) {
                String salida = "Indexando textos de " + counter + " a " + (counter + 10000) + "\n";
                FileUtils.writeStringToFile(logFile, salida, true);
            }
        }
        indexer.close();
    }

    private void _indexNGramas(Connection conexion, ResultSet texts, String lang, int size, boolean lemma) throws SQLException, IOException {
        FileUtils.writeStringToFile(logFile, "Creando nGramas para " + lang + "\n", true);
        Indexer indexer = IndexerFactory.getInstance().getNGrammaIndexer(lang, size, lemma);
        int counter = 0;
        while (texts.next()) {
            int paragraphID = texts.getInt("id");
            String textSelect = "SELECT content, text_id FROM paragraph WHERE id = ?;";
            try (PreparedStatement searchPS = conexion.prepareStatement(textSelect)) {
                //Obtener los textos uno a uno para no desbordar la pila.
                searchPS.setInt(1, paragraphID);
                try (ResultSet searchRS = searchPS.executeQuery()) {
                    while (searchRS.next()) {
                        String content = searchRS.getString("content");
                        ArrayList<String> discourses = new ArrayList<>();
                        int textID = searchRS.getInt("text_id");
                        try (PreparedStatement discoursesPS = conexion.prepareStatement("SELECT dis.code "
                                + "FROM corpus.discourse_texts dt, corpus.text txt, corpus.discourse dis "
                                + "WHERE dt.text_id = txt.id AND dt.discourse_id = dis.id AND txt.id = ?;")) {
                            discoursesPS.setDouble(1, textID);
                            try (ResultSet discoursesRS = discoursesPS.executeQuery()) {
                                while (discoursesRS.next()) {
                                    discourses.add(discoursesRS.getString("code"));
                                }
                            }
                        }
                        _obtainNGramas(textID, paragraphID, content, size, discourses, indexer);
                    }
                }
            }
            if (++counter % 10000 == 0) {
                String salida = "Indexando ngramas de los textos " + counter + " a " + (counter + 10000) + "\n";
                FileUtils.writeStringToFile(logFile, salida, true);
            }
        }
        indexer.close();
    }

    private void _obtainNGramas(int textID, int paragraphID, String content, int size, ArrayList<String> discourses, Indexer indexer) throws IOException {

        String words[] = content.split("\\s+");
        int skip = size - 1;

        for (int i = 0; (i + skip) < words.length; i++) {
            String word = "";
            for (int j = i; j < (size + i); j++) {
                word += words[j] + " ";
            }
            word = word.trim();
            String b1 = (i - 1) < 0 ? "" : words[i - 1];
            String b2 = (i - 2) < 0 ? "" : words[i - 2];
            String b3 = (i - 3) < 0 ? "" : words[i - 3];
            String b4 = (i - 4) < 0 ? "" : words[i - 4];
            String a1 = (i + skip + 1) >= words.length ? "" : words[i + skip + 1];
            String a2 = (i + skip + 2) >= words.length ? "" : words[i + skip + 2];
            String a3 = (i + skip + 3) >= words.length ? "" : words[i + skip + 3];
            String a4 = (i + skip + 4) >= words.length ? "" : words[i + skip + 4];
            indexer.indexNGramas(textID, paragraphID, word, discourses, a1, a2, a3, a4, b1, b2, b3, b4);
        }
    }

    protected void _initializeComponents(String indexPath) throws IOException {
        File indexDir = new File(indexPath);
        Directory directory = FSDirectory.open(indexDir);
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        writer = new IndexWriter(directory, config);
    }

    /**
     * Index text
     *
     * @param textID the ID of the text.
     * @param paragraphID the ID of te paragraph.
     * @param text text to index.
     * @param discourses list of discourses.
     * @throws CorruptIndexException if the index is corrupt.
     * @throws IOException if there is a low-level IO error.
     */
    public void index(int textID, int paragraphID, String text, ArrayList<String> discourses) throws CorruptIndexException, IOException {
        String discourse = StringUtils.join(discourses, " ");
        Document doc = new Document();
        doc.add(new IntField("textID", textID, Field.Store.YES));
        doc.add(new IntField("paragraphID", paragraphID, Field.Store.YES));
        doc.add(new TextField("text", text, Field.Store.NO));
        doc.add(new StringField("discourseString", discourse, Field.Store.NO));
        doc.add(new TextField("discourse", discourse, Field.Store.NO));
        writer.addDocument(doc);
    }

    public void indexNGramas(int textID, int paragraphID, String text, ArrayList<String> discourses, String a1, String a2, String a3, String a4, String b1, String b2, String b3, String b4) throws CorruptIndexException, IOException {
        String discourse = StringUtils.join(discourses, " ");
        Document doc = new Document();
        doc.add(new IntField("textID", textID, Field.Store.YES));
        doc.add(new IntField("paragraphID", paragraphID, Field.Store.YES));
        doc.add(new TextField("text", text, Field.Store.NO));
        doc.add(new StringField("discourseString", discourse, Field.Store.NO));
        doc.add(new TextField("discourse", discourse, Field.Store.NO));
        doc.add(new StringField("before4", b4, Field.Store.YES));
        doc.add(new StringField("before3", b3, Field.Store.YES));
        doc.add(new StringField("before2", b2, Field.Store.YES));
        doc.add(new StringField("before1", b1, Field.Store.YES));
        doc.add(new StringField("after1", a1, Field.Store.YES));
        doc.add(new StringField("after2", a2, Field.Store.YES));
        doc.add(new StringField("after3", a3, Field.Store.YES));
        doc.add(new StringField("after4", a4, Field.Store.YES));
        writer.addDocument(doc);
    }

    public void commit() throws IOException {
        writer.commit();
    }

    /**
     * Closes the index writer.
     *
     * @throws CorruptIndexException if the index is corrupt.
     * @throws IOException if there is a low-level IO error.
     */
    public void close() throws CorruptIndexException, IOException {
        writer.close();
    }
}
