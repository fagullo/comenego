/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.labidiomas.corpus.index;

import es.ua.labidiomas.corpus.util.Config;
import java.io.IOException;

/**
 *
 * @author paco
 */
public class EnglishIndexer extends Indexer {

    /**
     *
     * @param indexPath
     * @throws IOException
     */
    public EnglishIndexer(String indexPath, boolean lemma) throws IOException {
        super(indexPath);
        this.analyzer = new EnglishAnalyzer(lemma);
        if (lemma) {
            this._initializeComponents(indexPath + Config.FILE_SEPARATOR + "lemma/en");
        } else {
            this._initializeComponents(indexPath + Config.FILE_SEPARATOR + "en");
        }
    }

}
