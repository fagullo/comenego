/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.labidiomas.corpus.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author paco
 */
public class Indexer {

    /**
     * The writer.
     */
    private IndexWriter writer;

    protected Analyzer analyzer;

    protected String indexPath;

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
