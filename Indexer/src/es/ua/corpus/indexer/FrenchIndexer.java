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
public class FrenchIndexer extends Indexer {

    /**
     *
     * @param indexPath
     * @throws IOException
     */
    public FrenchIndexer(String indexPath) throws IOException {
        super(indexPath);
        this.analyzer = new FrenchAnalyzer();
        this._initializeComponents(indexPath + Config.FILE_SEPARATOR + "fr");
    }
    
}
