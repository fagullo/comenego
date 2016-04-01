/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.labidiomas.corpus.searcher.search;

import es.ua.labidiomas.corpus.searcher.SubSearch;
import java.util.List;

/**
 *
 * @author paco
 */
public class SearchConfiguration {

    /**
     * THe target nodes to search.
     */
    private List<SearchNode> searchNodes;
    /**
     * The discurses that the text could have.
     */
    private List<String> discourses;
    /**
     * The target language of the search.
     */
    private String language;
    /**
     * The number of the page to search.
     */
    private Integer page;
    
    private SearchOptions options;
    
    /**
     * Indicates the field when sorting.
     */
    private SearchSort sort;

    public SearchConfiguration() {
    }

    public SearchConfiguration(List<SearchNode> searchNodes, List<String> discourses, String language, Integer page, String letter, boolean lematize, boolean title, boolean order, boolean distance, boolean bilingual, String sortField, int sortPosition) {
        this.searchNodes = searchNodes;
        this.discourses = discourses;
        this.language = language;
        this.page = page;
        this.options = new SearchOptions(lematize, title, order, distance, bilingual);
        this.sort = new SearchSort(sortField, sortPosition, letter);
    }

    public List<SearchNode> getSearchNodes() {
        return searchNodes;
    }

    public void setSearchNodes(List<SearchNode> searchNodes) {
        this.searchNodes = searchNodes;
    }

    public String getDiscoursesAsString() {
        StringBuilder discourses = new StringBuilder();

        for (String discourse : this.discourses) {
            discourses.append(discourse).append(" ");
        }

        return discourses.toString().trim();
    }

    public List<String> getDiscourses() {
        return discourses;
    }
    
    public void setDiscourses(List<String> discourses) {
        this.discourses = discourses;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public SearchSort getSort() {
        return sort;
    }

    public void setSort(SearchSort sort) {
        this.sort = sort;
    }

    public boolean isLetterSearch() {
        if ( this.sort == null ) {
            return false;
        }
        return this.sort.getLetter() != null;
    }

    public String getSearch() {
        StringBuilder search = new StringBuilder();

        for (SearchNode node : this.searchNodes) {
            search.append(node.getWord()).append(" ");
        }

        return search.toString().trim();
    }

    public SubSearch getSubsearch() {
        return null;
    }

    public SearchOptions getOptions() {
        return options;
    }

    public void setOptions(SearchOptions options) {
        this.options = options;
    }
}
