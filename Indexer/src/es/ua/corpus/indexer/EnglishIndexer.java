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
public class EnglishIndexer extends Indexer {

    /**
     *
     * @param indexPath
     * @throws IOException
     */
    public EnglishIndexer(String indexPath) throws IOException {
        super(indexPath);
        this.analyzer = new EnglishAnalyzer();
        this._initializeComponents(indexPath + Config.FILE_SEPARATOR + "en");
    }
    
}
