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
public class SpanishIndexer extends Indexer {

    /**
     *
     * @param indexPath
     * @throws IOException
     */
    public SpanishIndexer(String indexPath, boolean lemma) throws IOException {
        super(indexPath);
        this.analyzer = new SpanishAnalyzer(lemma);
        if (lemma) {
            this._initializeComponents(indexPath + Config.FILE_SEPARATOR + "lemma/es");
        } else {
            this._initializeComponents(indexPath + Config.FILE_SEPARATOR + "es");
        }
    }

}
