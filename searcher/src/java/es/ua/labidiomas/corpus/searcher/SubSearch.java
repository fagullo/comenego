/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package es.ua.labidiomas.corpus.searcher;

/**
 *
 * @author paco
 */
public class SubSearch {
    private String text;
    
    private String field;

    public SubSearch() {
    }

    public SubSearch(String text, String field) {
        this.text = text;
        this.field = field;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}
