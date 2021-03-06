/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.corpus.indexer;

import es.upv.xmlutils.XMLUtils;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
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
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
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
public class Indexer implements Closeable {

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

    protected void _initializeComponents(String indexPath) throws IOException {
        File indexDir = new File(indexPath);
        Directory directory = FSDirectory.open(indexDir);
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        writer = new IndexWriter(directory, config);
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
                        case "paragraph":
                            _indexTexts(connection, texts, lang, true);
                            texts.first();
                            _indexTexts(connection, texts, lang, false);
                            break;
                        case "title":
                            _indexTitle(connection, lang, true);
                            _indexTitle(connection, lang, false);
                            break;
                    }
                }
            }
        }
    }

    private void _indexTexts(Connection conexion, ResultSet texts, String lang, boolean lemma) throws SQLException, IOException {
        int counter = 0;
        try (Indexer indexer = IndexerFactory.getInstance().getIndexer(lang, lemma, false)) {
            while (texts.next()) {
                int paragraphID = texts.getInt("id");
                String textSelect = "SELECT p.content, p.text_id, t.title FROM paragraph p, text t WHERE p.id = ? AND p.text_id = t.id;";
                try (PreparedStatement searchPS = conexion.prepareStatement(textSelect)) {//Obtener los textos uno a uno para no desbordar la pila.
                    searchPS.setInt(1, paragraphID);
                    try (ResultSet searchRS = searchPS.executeQuery()) {
                        while (searchRS.next()) {
                            String content = searchRS.getString("content");
                            String title = searchRS.getString("title");
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
                            indexer.index(textID, paragraphID, content, discourses, title);

                        }
                    }
                }
                if (++counter % 10000 == 0) {
                    String salida = "Indexando textos de " + counter + " a " + (counter + 10000) + "\n";
                    FileUtils.writeStringToFile(logFile, salida, true);
                }
            }
        }
    }

    /**
     * Index text
     *
     * @param textID the ID of the text.
     * @param title
     * @throws CorruptIndexException if the index is corrupt.
     * @throws IOException if there is a low-level IO error.
     */
    public void indexTitle(int textID, String title, ArrayList<String> discourses) throws CorruptIndexException, IOException {
//        String discourse = StringUtils.join(discourses, " ");
        Document doc = new Document();
        doc.add(new IntField("textID", textID, Field.Store.YES));
        doc.add(new TextField("title", title, Field.Store.NO));
        for (String discourse : discourses) {
            doc.add(new StringField("discourseString", discourse, Field.Store.NO));
            doc.add(new TextField("discourse", discourse, Field.Store.NO));
        }
        writer.addDocument(doc);
    }

    /**
     * Index text
     *
     * @param textID the ID of the text.
     * @param paragraphID the ID of te paragraph.
     * @param text text to index.
     * @param discourses list of discourses.
     * @param title
     * @throws CorruptIndexException if the index is corrupt.
     * @throws IOException if there is a low-level IO error.
     */
    public void index(int textID, int paragraphID, String text, ArrayList<String> discourses, String title) throws CorruptIndexException, IOException {
//        String discourse = StringUtils.join(discourses, " ");
        Document doc = new Document();
        doc.add(new IntField("textID", textID, Field.Store.YES));
        doc.add(new IntField("paragraphID", paragraphID, Field.Store.YES));
        doc.add(new TextField("text", text, Field.Store.NO));
        doc.add(new TextField("text", title, Field.Store.NO));
        doc.add(new TextField("title", title, Field.Store.NO));
        for (String discourse : discourses) {
            doc.add(new StringField("discourseString", discourse, Field.Store.NO));
            doc.add(new TextField("discourse", discourse, Field.Store.NO));
        }
        writer.addDocument(doc);
    }

    public void indexNGramas(int textID, int paragraphID, String text, ArrayList<String> discourses, String a1, String a2, String a3, String a4, String b1, String b2, String b3, String b4) throws CorruptIndexException, IOException {
        String discourse = StringUtils.join(discourses, " ");
        Document doc = new Document();
        doc.add(new IntField("textID", textID, Field.Store.YES));
        doc.add(new IntField("paragraphID", paragraphID, Field.Store.YES));
        doc.add(new TextField("text", Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""), Field.Store.NO));
        doc.add(new StringField("discourseString", discourse, Field.Store.NO));
        doc.add(new TextField("discourse", discourse, Field.Store.NO));
        doc.add(new StringField("before4", Normalizer.normalize(b4, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""), Field.Store.YES));
        doc.add(new StringField("before3", Normalizer.normalize(b3, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""), Field.Store.YES));
        doc.add(new StringField("before2", Normalizer.normalize(b2, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""), Field.Store.YES));
        doc.add(new StringField("before1", Normalizer.normalize(b1, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""), Field.Store.YES));
        doc.add(new StringField("after1", Normalizer.normalize(a1, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""), Field.Store.YES));
        doc.add(new StringField("after2", Normalizer.normalize(a2, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""), Field.Store.YES));
        doc.add(new StringField("after3", Normalizer.normalize(a3, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""), Field.Store.YES));
        doc.add(new StringField("after4", Normalizer.normalize(a4, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""), Field.Store.YES));
        writer.addDocument(doc);
    }

    public void commit() throws IOException {
        writer.commit();
    }

    private void _indexTitle(Connection conexion, String lang, boolean lemma) throws SQLException, IOException {
        int counter = 0;
        try (Indexer indexer = IndexerFactory.getInstance().getIndexer(lang, lemma, true)) {
            String textSelect = "SELECT t.id, t.title FROM text t, language l WHERE language_id = l.id AND l.shortname = ?;";
            try (PreparedStatement searchPS = conexion.prepareStatement(textSelect)) {//Obtener los textos uno a uno para no desbordar la pila.
                searchPS.setString(1, lang);
                try (ResultSet searchRS = searchPS.executeQuery()) {
                    while (searchRS.next()) {
                        String title = searchRS.getString("title");
                        int textID = searchRS.getInt("id");

                        ArrayList<String> discourses = new ArrayList<>();
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

                        indexer.indexTitle(textID, title, discourses);

                    }
                }
                if (++counter % 10000 == 0) {
                    String salida = "Indexando textos de " + counter + " a " + (counter + 10000) + "\n";
                    FileUtils.writeStringToFile(logFile, salida, true);
                }
            }
        }
    }

    /**
     * Closes the index writer.
     *
     * @throws CorruptIndexException if the index is corrupt.
     * @throws IOException if there is a low-level IO error.
     */
    @Override
    public void close() throws CorruptIndexException, IOException {
        writer.commit();
        writer.close();
    }
}
