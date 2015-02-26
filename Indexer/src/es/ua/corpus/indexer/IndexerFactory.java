/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package es.ua.corpus.indexer;

import java.io.IOException;

/**
 *
 * @author paco
 */
public class IndexerFactory {
   
    private static IndexerFactory instance = null;
    
    private IndexerFactory() {
    }
    
    public static IndexerFactory getInstance() {
        if ( instance == null ) {
            instance = new IndexerFactory();
        }
        
        return instance;
    }
    
    public Indexer getIndexer(String language) throws IOException {
        return this._create(language, Config.INDEXES_PATH);
    }
    
    public Indexer getNGrammaIndexer(String language, int size) throws IOException {
        return this._create(language, Config.INDEXES_PATH + Config.FILE_SEPARATOR + "ngramas" + Config.FILE_SEPARATOR + size);
    }
    
    private Indexer _create(String language, String indexPath) throws IOException {
        if ( language.toLowerCase().equals("es") ) {
            return new SpanishIndexer(indexPath);
        } else if ( language.toLowerCase().equals("fr") ) {
            return new FrenchIndexer(indexPath);
        } else {
            return new EnglishIndexer(indexPath);
        }
    }
    
}
