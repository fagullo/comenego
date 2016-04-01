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
public class SearchOptions {
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
    
    private boolean bilingual;

    public SearchOptions() {
        this.lematize = false;
        this.title = false;
        this.order = true;
        this.distance = false;
        this.bilingual = true;
    }

    public SearchOptions(boolean lematize, boolean title, boolean order, boolean distance, boolean bilingual) {
        this.lematize = lematize;
        this.title = title;
        this.order = order;
        this.distance = distance;
        this.bilingual = bilingual;
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

    public boolean isBilingual() {
        return bilingual;
    }

    public void setBilingual(boolean bilingual) {
        this.bilingual = bilingual;
    }
}
