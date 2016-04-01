/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package es.ua.labidiomas.corpus.searcher.search;

/**
 *
 * @author paco
 */
public class SearchSort {
    /**
     * Indicates the field when sorting.
     */
    private String field;
    /**
     * Indicates the position when sorting.
     */
    private int position;
    
    private String letter;

    public SearchSort() {
    }

    public SearchSort(String field, int position, String letter) {
        this.field = field;
        this.position = position;
        this.letter = letter;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getLetter() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }
}
