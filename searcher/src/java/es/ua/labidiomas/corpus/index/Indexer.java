/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.labidiomas.corpus.index;

import es.ua.labidiomas.corpus.util.Config;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author paco
 */
public class Indexer implements Closeable {

    /**
     * The writer.
     */
    private IndexWriter writer;

    protected Analyzer analyzer;

    protected String indexPath;

    protected Directory directory;

    public static final int NGRAM_SIZE = 4;

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
        directory = FSDirectory.open(indexDir);
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        config.setWriteLockTimeout(5000l);
        writer = new IndexWriter(directory, config);
    }

    public void updateIndex(String textID, String lang) throws IOException {
        deleteDocument(textID, lang, Config.FILE_SEPARATOR);
        createIndex(textID);
    }

    public void deleteDocument(String textID, String lang, String fileSeparator) throws IOException {
        deleteIndex(textID, lang, fileSeparator);
        deleteNgrams(textID, lang, fileSeparator);
    }

    public void deleteIndex(String textID, String lang, String fileSeparator) throws IOException {
        Term term = new Term("textID", textID);
        writer.deleteDocuments(term);
    }

    private void deleteNgrams(String textID, String lang, String fileSeparator) throws IOException {
        for (int i = 1; i <= 4; i++) {
            File indexDir = new File(indexPath + fileSeparator + "ngrams" + fileSeparator + i + fileSeparator + lang);
            Directory directory = null;
            IndexWriter indexEraser = null;
            try {
                directory = FSDirectory.open(indexDir);
                IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, analyzer);
                config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
                config.setWriteLockTimeout(5000l);
                indexEraser = new IndexWriter(directory, config);
                Term term = new Term("textID", textID);
                indexEraser.deleteDocuments(term);
                indexEraser.commit();
            } finally {
                if (directory != null) {
                    directory.close();
                }
                if (indexEraser != null) {
                    indexEraser.close();
                }
            }
        }
    }

    public void createIndex(String textID) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection(Config.CONEXION_STRING, Config.DB_USER, Config.DB_PASS);
            String query = "SELECT p.content as content, p.id as paragraphID, l.shortname as lang FROM paragraph p, text t, language l"
                    + " WHERE t.id = p.text_id AND l.id = t.language_id AND p.text_id =" + textID;
            PreparedStatement paragraphIDsPS = connection.prepareStatement(query);
            ResultSet paragraphIDsRS = paragraphIDsPS.executeQuery();
            while (paragraphIDsRS.next()) {
                String paragraphID = paragraphIDsRS.getString("paragraphID");
                String content = paragraphIDsRS.getString("content");
                String lang = paragraphIDsRS.getString("lang");
                ArrayList<String> discourses = new ArrayList<String>();
                PreparedStatement discoursesPS = connection.prepareStatement("SELECT dis.code "
                        + "FROM corpus.discourse_texts dt, corpus.text txt, corpus.discourse dis "
                        + "WHERE dt.text_id = txt.id AND dt.discourse_id = dis.id AND txt.id = ?;");
                discoursesPS.setString(1, textID);
                ResultSet discoursesRS = discoursesPS.executeQuery();
                while (discoursesRS.next()) {
                    discourses.add(discoursesRS.getString("code"));
                }
                discoursesRS.close();
                discoursesPS.close();
                this.index(Integer.parseInt(textID), Integer.parseInt(paragraphID), content, discourses, lang);
                for (int size = 1; size <= NGRAM_SIZE; size++) {
                    Indexer nGramIndexer = IndexerFactory.getInstance().getNGrammaIndexer(lang, size, true);
                    nGramIndexer._obtainNGramas(textID, paragraphID, content, size, discourses);
                    nGramIndexer.commit();
                    nGramIndexer.close();
                    nGramIndexer = IndexerFactory.getInstance().getNGrammaIndexer(lang, size, false);
                    nGramIndexer._obtainNGramas(textID, paragraphID, content, size, discourses);
                    nGramIndexer.commit();
                    nGramIndexer.close();
                }
            }
            paragraphIDsRS.close();
            paragraphIDsPS.close();
            connection.close();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void _obtainNGramas(String textID, String paragraphID, String content, int size, ArrayList<String> discourses) throws IOException {

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
            this.indexNGramas(Integer.valueOf(textID), Integer.valueOf(paragraphID), word, discourses, a1.toLowerCase(), a2.toLowerCase(), a3.toLowerCase(), a4.toLowerCase(), b1.toLowerCase(), b2.toLowerCase(), b3.toLowerCase(), b4.toLowerCase());
        }
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
    public void index(int textID, int paragraphID, String text, ArrayList<String> discourses, String lang) throws CorruptIndexException, IOException {
        String discourse = StringUtils.join(discourses, " ");
        Document doc = new Document();
        doc.add(new IntField("textID", textID, Field.Store.YES));
        doc.add(new IntField("paragraphID", paragraphID, Field.Store.YES));
        doc.add(new TextField("text", text, Field.Store.NO));
        doc.add(new StringField("discourseString", discourse, Field.Store.NO));
        doc.add(new TextField("discourse", discourse, Field.Store.NO));
        writer.addDocument(doc);
    }

    @Override
    public void close() throws IOException {
        if (directory != null) {
            directory.close();
        }
        if (writer != null) {
            writer.close();
        }
    }

    public void commit() throws IOException {
        writer.commit();
    }
}
