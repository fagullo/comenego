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
public class SearchParameters {

    private String discourses;
    private List<String> languages;
    private String letter;
    private Integer page = 1;
    private String position;
    private String search;
    private String sortField;
    private String subsearch;
    private boolean lemma = false;

    public SearchParameters() {
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getSubsearch() {
        return subsearch;
    }

    public void setSubsearch(String subsearch) {
        this.subsearch = subsearch;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public String getDiscourses() {
        return discourses;
    }

    public void setDiscourses(String discourses) {
        this.discourses = discourses;
    }

    public String getLetter() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public boolean isLetterSearch() {
        return letter != null && !letter.isEmpty() && !letter.equals("null");
    }

    public boolean isLemma() {
        return lemma;
    }

    public void setLemma(boolean lemma) {
        this.lemma = lemma;
    }
}
