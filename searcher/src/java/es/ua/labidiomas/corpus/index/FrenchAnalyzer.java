/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package es.ua.labidiomas.corpus.index;

import java.io.Reader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.ext.FrenchStemmer;

/**
 *
 * @author paco
 */
public class FrenchAnalyzer extends Analyzer {

    @Override
    protected Analyzer.TokenStreamComponents createComponents(String string, Reader reader) {
        Tokenizer tokenizer = new StandardTokenizer(Version.LUCENE_47, reader);
        TokenFilter filters = new ASCIIFoldingFilter(tokenizer);
        filters = new LowerCaseFilter(Version.LUCENE_47, filters);
        filters = new SnowballFilter(filters, new FrenchStemmer());
        return new Analyzer.TokenStreamComponents(tokenizer, filters);
    }
}
