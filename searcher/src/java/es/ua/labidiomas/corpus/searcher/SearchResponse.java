/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package es.ua.labidiomas.corpus.searcher;

import java.util.List;

/**
 *
 * @author paco
 */
public class SearchResponse {
    
    private Integer numPages;
    
    private Integer numDocs;
    
    private List<LuceneSnippet> matches;
    
    private boolean sorted;

    public SearchResponse() {
    }

    public SearchResponse(Integer numPages, Integer numDocs, List<LuceneSnippet> matches, boolean sorted) {
        this.numPages = numPages;
        this.numDocs = numDocs;
        this.matches = matches;
        this.sorted = sorted;
    }

    public Integer getNumPages() {
        return numPages;
    }

    public void setNumPages(Integer numPages) {
        this.numPages = numPages;
    }

    public Integer getNumDocs() {
        return numDocs;
    }

    public void setNumDocs(Integer numDocs) {
        this.numDocs = numDocs;
    }

    public List<LuceneSnippet> getMatches() {
        return matches;
    }

    public void setMatches(List<LuceneSnippet> matches) {
        this.matches = matches;
    }

    public boolean isSorted() {
        return sorted;
    }

    public void setSorted(boolean sorted) {
        this.sorted = sorted;
    }
    
    
    
}
