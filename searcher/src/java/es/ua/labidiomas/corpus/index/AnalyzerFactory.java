/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package es.ua.labidiomas.corpus.index;

import org.apache.lucene.analysis.Analyzer;

/**
 *
 * @author paco
 */
public class AnalyzerFactory {
    private static AnalyzerFactory instance = null;
    
    private AnalyzerFactory() {
        
    }
    
    public static AnalyzerFactory getInstance() {
        if ( instance == null ) {
            instance = new AnalyzerFactory();
        }
        return instance;
    }
    
    public Analyzer getAnalyzer(String lang) {
        if ( lang.equals("es") ) {
            return new SpanishAnalyzer();
        } else if ( lang.equals("fr") ) {
            return new FrenchAnalyzer();
        } else {
            return new EnglishAnalyzer();
        }
    }
}
