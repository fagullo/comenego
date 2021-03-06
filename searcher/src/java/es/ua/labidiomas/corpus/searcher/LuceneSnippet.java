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
public class LuceneSnippet {
    
    private String snippet;
    
    private String url;
    
    private String discourses;
    
    private String translation;
    
    private String original;
    
    public LuceneSnippet() {
    }

    public LuceneSnippet(String snippet, String url, String discourses) {
        this.snippet = snippet;
        this.url = url;
        this.discourses = discourses;
    }

    public LuceneSnippet(String snippet, String url, String discourses, String translation, String original) {
        this.snippet = snippet;
        this.url = url;
        this.discourses = discourses;
        this.translation = translation;
        this.original = original;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDiscourses() {
        return discourses;
    }

    public void setDiscourses(String discourses) {
        this.discourses = discourses;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }
}
