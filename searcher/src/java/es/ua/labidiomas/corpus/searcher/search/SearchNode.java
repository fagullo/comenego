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
public class SearchNode {
    
    private int distance;
    
    private boolean isMain;
    
    private String word;

    public SearchNode() {
    }

    public SearchNode(int distance, boolean isMain, String word) {
        this.distance = distance;
        this.isMain = isMain;
        this.word = word;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public boolean isIsMain() {
        return isMain;
    }

    public void setIsMain(boolean isMain) {
        this.isMain = isMain;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }    
}
