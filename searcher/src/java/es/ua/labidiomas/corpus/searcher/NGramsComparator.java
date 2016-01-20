/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.labidiomas.corpus.searcher;

import java.util.Comparator;
import java.util.HashMap;

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
        String word;
        if (order.equals("before")) {
            text = " " + text;
            if (position == 1 ) {
                word = text.replaceAll(".* (\\S+) <b>.*", "$1");
            } else if (position == 2) {
                word = text.replaceAll(".* (\\S+) (\\S+) <b>.*", "$1");
            } else if (position == 3) {
                word = text.replaceAll(".* (\\S+) (\\S+) (\\S+) <b>.*", "$1");
            } else {
                word = text.replaceAll(".* (\\S+) (\\S+) (\\S+) (\\S+) <b>.*", "$1");
            }
        } else {
            if (position == 1) {
                word = text.replaceAll(".*</b>[\\S]* (\\S+).*", "$1");
            } else if (position == 2) {
                word = text.replaceAll(".*</b>[\\S]* (\\S+) (\\S+).*", "$2");
            } else if (position == 3 ) {
                word = text.replaceAll(".*</b>[\\S]* (\\S+) (\\S+) (\\S+).*", "$3");
            } else {
                word = text.replaceAll(".*</b>[\\S]* (\\S+) (\\S+) (\\S+) (\\S+).*", "$4");
            }
        }
        if (word.equals(text)) {
            word = "";
        }
        sortWords.put(text, word);
        return word;
    }
}
