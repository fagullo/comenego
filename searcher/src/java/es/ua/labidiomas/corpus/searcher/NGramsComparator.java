/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.labidiomas.corpus.searcher;

import java.util.Comparator;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author paco
 */
public class NGramsComparator implements Comparator<LuceneSnippet> {

    private final String order;
    private final int position;
    private final HashMap<String, String> sortWords;

    public NGramsComparator(String order, int position, HashMap<String, String> sortWords) {
        this.order = order;
        this.position = position;
        this.sortWords = sortWords;
    }

    @Override
    public int compare(LuceneSnippet o1, LuceneSnippet o2) {
        String w1 = _getSortWord(o1.getSnippet());
        String w2 = _getSortWord(o2.getSnippet());
        return w1.compareToIgnoreCase(w2);
    }

    private String _getSortWord(String text) {
        if (sortWords.containsKey(text)) {
            return sortWords.get(text);
        }
        String fragment, word = "";
        int startIndex, endIndex;
        if (order.equals("before")) {
            fragment = StringUtils.substringBefore(" " + text, "<b>");
            startIndex = StringUtils.lastOrdinalIndexOf(fragment, " ", position + 1);
            endIndex = StringUtils.lastOrdinalIndexOf(fragment, " ", position);
        } else {
            fragment = StringUtils.substringAfter(text + " ", "</b>");
            startIndex = StringUtils.ordinalIndexOf(fragment, " ", position);
            endIndex = StringUtils.ordinalIndexOf(fragment, " ", position + 1);
        }
        if (startIndex != -1 && endIndex != -1) {
            word = fragment.substring(startIndex, endIndex).trim();
        }
        sortWords.put(text, word);
        return word;
    }
}
