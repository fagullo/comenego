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
    /**
     * The first letter of the sort word.
     */
    private String letter;
    /**
     * Indicates if lemmatize or not.
     */
    private boolean lematize;
    /**
     * Indicates if the search is by content or only by the title.
     */
    private boolean title;
    /**
     * Indicates whether the nodes should appear in the order that they are
     * received or the order does not matter.
     */
    private boolean order;
    /**
     * Indicates whether the distance between the nodes is exactly the distance
     * indicated or as maximum the distance indicated.
     */
    private boolean distance;
    /**
     * Indicates the field when sorting.
     */
    private String sortField;
    /**
     * Indicates the position when sorting.
     */
    private int sortPosition;

    public SearchConfiguration() {
    }

    public SearchConfiguration(List<SearchNode> searchNodes, List<String> discourses, String language, Integer page, String letter, boolean lematize, boolean title, boolean order, boolean distance, String sortField, int sortPosition) {
        this.searchNodes = searchNodes;
        this.discourses = discourses;
        this.language = language;
        this.page = page;
        this.letter = letter;
        this.lematize = lematize;
        this.title = title;
        this.order = order;
        this.distance = distance;
        this.sortField = sortField;
        this.sortPosition = sortPosition;
    }

    public List<SearchNode> getSearchNodes() {
        return searchNodes;
    }

    public void setSearchNodes(List<SearchNode> searchNodes) {
        this.searchNodes = searchNodes;
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

    public String getLetter() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public boolean isLematize() {
        return lematize;
    }

    public void setLematize(boolean lematize) {
        this.lematize = lematize;
    }

    public boolean isTitle() {
        return title;
    }

    public void setTitle(boolean title) {
        this.title = title;
    }

    public boolean isOrder() {
        return order;
    }

    public void setOrder(boolean order) {
        this.order = order;
    }

    public boolean isDistance() {
        return distance;
    }

    public void setDistance(boolean distance) {
        this.distance = distance;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public int getSortPosition() {
        return sortPosition;
    }

    public void setSortPosition(int sortPosition) {
        this.sortPosition = sortPosition;
    }
}
