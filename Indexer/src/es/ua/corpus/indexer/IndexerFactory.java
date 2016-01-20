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
        if (instance == null) {
            instance = new IndexerFactory();
        }

        return instance;
    }

    public Indexer getIndexer(String language, boolean lemma, boolean title) throws IOException {
        if (title) {
            return this._create(language, Config.INDEXES_PATH + Config.FILE_SEPARATOR + "title" , lemma);
        } else {
            return this._create(language, Config.INDEXES_PATH, lemma);
        }
    }

    public Indexer getNGrammaIndexer(String language, int size, boolean lemma) throws IOException {
        return this._create(language, Config.INDEXES_PATH + Config.FILE_SEPARATOR + "ngramas" + Config.FILE_SEPARATOR + size, lemma);
    }

    private Indexer _create(String language, String indexPath, boolean lemma) throws IOException {
        if (language.toLowerCase().equals("es")) {
            return new SpanishIndexer(indexPath, lemma);
        } else if (language.toLowerCase().equals("fr")) {
            return new FrenchIndexer(indexPath, lemma);
        } else {
            return new EnglishIndexer(indexPath, lemma);
        }
    }

}
