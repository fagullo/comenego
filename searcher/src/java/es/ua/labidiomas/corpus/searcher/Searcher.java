/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.labidiomas.corpus.searcher;

import es.ua.labidiomas.corpus.index.AnalyzerFactory;
import es.ua.labidiomas.corpus.util.Config;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.grouping.SearchGroup;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.search.grouping.term.TermFirstPassGroupingCollector;
import org.apache.lucene.search.grouping.term.TermSecondPassGroupingCollector;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

/**
 *
 * @author paco
 */
public class Searcher {

    private static final int DOCS_BY_PAGE = 50;

    private HashMap<String, String> sortWords;

    public List<LuceneSnippet> getSnippets(int termSize, List<LuceneSnippet> snippets, ScoreDoc sd, IndexSearcher indexSearcher, Connection conexion, Analyzer analyzer, Highlighter textHighlighter, SearchParameters params) throws IOException, SQLException, InvalidTokenOffsetsException {
        Document doc = indexSearcher.doc(sd.doc);
        int paragraphID = Integer.parseInt(doc.get("paragraphID"));
        int textID = Integer.parseInt(doc.get("textID"));
        PreparedStatement textPS = conexion.prepareStatement("SELECT p.content, t.url FROM text t, paragraph p WHERE p.text_id = t.id AND p.id = ?;");
        textPS.setDouble(1, paragraphID);
        ResultSet textRS = textPS.executeQuery();
        if (textRS.next()) {
            String discourses = "";
            String discorusesQuery = "SELECT d.code as name FROM paragraph p , discourse_texts dt, discourse d"
                    + " WHERE dt.text_id = ? AND p.text_id = dt.text_id AND p.id = ? AND d.id = dt.discourse_id;";
            PreparedStatement discoursePS = conexion.prepareStatement(discorusesQuery);
            discoursePS.setDouble(1, textID);
            discoursePS.setDouble(2, paragraphID);
            ResultSet discourseRS = discoursePS.executeQuery();
            while (discourseRS.next()) {
                discourses += discourseRS.getString("name") + " ";
            }
            discourseRS.close();
            discoursePS.close();

            LuceneSnippet result = null;
            TokenStream tokenStream = analyzer.tokenStream("text", textRS.getString("content"));
            String url = textRS.getString("url");
            org.apache.lucene.search.highlight.TextFragment[] fragments = textHighlighter.getBestTextFragments(tokenStream, textRS.getString("content"), false, 600);
            for (TextFragment frag : fragments) {
                if (frag.getScore() > 0 && result == null) {
                    String snippet = frag.toString().replaceAll("[\n\r]", "").replaceAll("</b> <b>", " ").trim();
                    int numMatches = StringUtils.countMatches(snippet, "<b>");
                    if (numMatches > 1) {
                        int start = 0;
                        int end = 0;
                        for (int i = 0; i < numMatches; i++) {
                            result = new LuceneSnippet();
                            if (i == (numMatches - 1)) {
                                result.setSnippet(snippet.substring(start));
                            } else {
                                int coincidence = snippet.indexOf("</b>", start) + 4;
                                int next = snippet.indexOf("<b>", coincidence);
                                end = (coincidence + next) / 2;
                                end = snippet.indexOf(" ", end);
                                result.setSnippet(snippet.substring(start, end));
                            }
                            result.setUrl(url);
                            result.setDiscourses(discourses);
                            start = end;
                            if (params.getSortField() != null && !params.getSortField().isEmpty()) {
                                _sort(result, snippets, params);
                            } else {
                                snippets.add(result);
                                result = null;
                            }
                        }
                    } else if (snippet.length() > termSize) {
                        if (result != null) {
                            result.setSnippet(snippet);
                            result.setUrl(url);
                            result.setDiscourses(discourses);
                            if (params.getSortField() != null && !params.getSortField().isEmpty()) {
                                _sort(result, snippets, params);
                            } else {
                                snippets.add(result);
                            }
                        }
                    }
                }
            }
        }
        textRS.close();
        textPS.close();

        return snippets;
    }

    /**
     * Prepares the highlighter to highlight the terms that matches with the
     * search criteria.
     *
     * @param searchQuery the query that contains the search criteria.
     * @return the highlighter configured.
     */
    public Highlighter prepareHighlighter(BooleanQuery searchQuery) {
        QueryScorer scorer = new QueryScorer(searchQuery.getClauses()[0].getQuery());
        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<b>", "</b>");
        Highlighter textHighlighter = new Highlighter(formatter, scorer);
        textHighlighter.setTextFragmenter(new SimpleFragmenter(100));

        return textHighlighter;
    }

    public TopGroups prapareResults(BooleanQuery query, IndexSearcher indexSearcher, Integer page) throws IOException {

        int targetPage = page * DOCS_BY_PAGE;

        TermFirstPassGroupingCollector collector = new TermFirstPassGroupingCollector("textID", Sort.RELEVANCE, targetPage);
        indexSearcher.search(query, collector);
        Collection<SearchGroup<BytesRef>> topGroups = collector.getTopGroups(0, true);

        if (topGroups == null) {
            return null;
        }
        TermSecondPassGroupingCollector collector2 = new TermSecondPassGroupingCollector("textID", topGroups, Sort.RELEVANCE, Sort.RELEVANCE, targetPage, true, true, true);
        indexSearcher.search(query, collector2);
        return collector2.getTopGroups(0);
    }

    public TopGroups prapareSortedResults(BooleanQuery query, IndexSearcher indexSearcher, Integer page, String sortField) throws IOException {
        int targetPage = page * DOCS_BY_PAGE;

        SortField sortedField = new SortField(sortField, SortField.Type.STRING);
        Sort sort = new Sort(sortedField);

        TermFirstPassGroupingCollector collector = new TermFirstPassGroupingCollector("textID", sort, targetPage);
        indexSearcher.search(query, collector);
        Collection<SearchGroup<BytesRef>> topGroups = collector.getTopGroups(0, true);

        if (topGroups == null) {
            return null;
        }
        TermSecondPassGroupingCollector collector2 = new TermSecondPassGroupingCollector("textID", topGroups, sort, sort, targetPage, true, true, true);
        indexSearcher.search(query, collector2);

        return collector2.getTopGroups(0);
    }

    /**
     * Prepares the index searcher to read the indexes in the indexes directory.
     *
     * @return @throws IOException
     */
    public IndexSearcher prepareIndexSearcher(String language, String sort, String searchText) throws IOException {
        File indexDir;
        int size = searchText.split("\\s+").length;
        if (sort == null || sort.isEmpty()) {
            indexDir = new File(Config.INDEXES_PATH + Config.FILE_SEPARATOR + language);
        } else {
            indexDir = new File(Config.INDEXES_PATH + Config.FILE_SEPARATOR + "ngramas" + Config.FILE_SEPARATOR + size + Config.FILE_SEPARATOR + language);
        }
        if (!indexDir.isDirectory() || !indexDir.canRead()) {
            throw new IOException("Can not read the index path at '" + Config.INDEXES_PATH + "'");
        }

        Directory directory = FSDirectory.open(indexDir);
        DirectoryReader ireader = DirectoryReader.open(directory);
        return new IndexSearcher(ireader);
    }

    /**
     * Prepares the query with all the search criteria.
     *
     * @param searchText the text that is going to be searched.
     * @param discourses the different discourses which the text could pertain
     * to.
     * @param sortField
     * @param analyzer the analyzer used to index.
     * @param isLetter
     * @param position
     * @param letter
     * @param subSearch
     * @return a boolean query which contains all the search criteria.
     * @throws org.apache.lucene.queryparser.classic.ParseException
     */
    public BooleanQuery prepareQuery(String searchText, String discourses, Analyzer analyzer, String sortField, String position, boolean isLetter, String letter, String subSearch) throws ParseException {
        if (isLetter) {
            return _prepareLetterQuery(searchText, discourses, analyzer, letter, sortField, position);
        } else {
            if (subSearch != null) {
                return _prepareQuerySubSearch(searchText, subSearch, discourses, analyzer);
            } else {
                return _prepareQuery(searchText, discourses, analyzer);
            }
        }
    }

    private BooleanQuery _prepareLetterQuery(String searchText, String discourses, Analyzer analyzer, String letter, String sortField, String position) throws ParseException {
        BooleanQuery searchQuery = new BooleanQuery();

        BooleanQuery textBooleanQuery = new BooleanQuery();
        QueryParser textParser = new QueryParser(Version.LUCENE_47, "text", analyzer);
        Query textQuery = textParser.parse(searchText);
        textBooleanQuery.add(textQuery, BooleanClause.Occur.MUST);
        searchQuery.add(textBooleanQuery, BooleanClause.Occur.MUST);

        textBooleanQuery = new BooleanQuery();
        textParser = new QueryParser(Version.LUCENE_47, sortField + position, analyzer);
        textQuery = textParser.parse(letter + "*");
        textBooleanQuery.add(textQuery, BooleanClause.Occur.MUST);
        searchQuery.add(textBooleanQuery, BooleanClause.Occur.MUST);

        BooleanQuery discourseBooleanQuery = new BooleanQuery();
        QueryParser discourseParser = new QueryParser(Version.LUCENE_47, "discourse", analyzer);
        Query discourseQuery = discourseParser.parse(discourses);
        discourseBooleanQuery.add(discourseQuery, BooleanClause.Occur.MUST);
        searchQuery.add(discourseBooleanQuery, BooleanClause.Occur.MUST);

        return searchQuery;
    }

    private BooleanQuery _prepareQuery(String searchText, String discourses, Analyzer analyzer) throws ParseException {
        BooleanQuery searchQuery = new BooleanQuery();

        BooleanQuery textBooleanQuery = new BooleanQuery();
        QueryParser textParser = new QueryParser(Version.LUCENE_47, "text", analyzer);
        Query textQuery = textParser.parse(searchText);
        textBooleanQuery.add(textQuery, BooleanClause.Occur.MUST);

        searchQuery.add(textBooleanQuery, BooleanClause.Occur.MUST);

        BooleanQuery discourseBooleanQuery = new BooleanQuery();
        QueryParser discourseParser = new QueryParser(Version.LUCENE_47, "discourse", analyzer);
        Query discourseQuery = discourseParser.parse(discourses);
        discourseBooleanQuery.add(discourseQuery, BooleanClause.Occur.MUST);
        searchQuery.add(discourseBooleanQuery, BooleanClause.Occur.MUST);

        return searchQuery;
    }

    private BooleanQuery _prepareQuerySubSearch(String searchText, String lastSearch, String discourses, Analyzer analyzer) throws ParseException {
        BooleanQuery searchQuery = new BooleanQuery();

        BooleanQuery textBooleanQuery = new BooleanQuery();
        QueryParser textParser = new QueryParser(Version.LUCENE_47, "text", analyzer);
        Query textQuery = textParser.parse(searchText);
        textBooleanQuery.add(textQuery, BooleanClause.Occur.MUST);

        searchQuery.add(textBooleanQuery, BooleanClause.Occur.MUST);

        Query text2Query = textParser.parse(lastSearch);
        textBooleanQuery.add(text2Query, BooleanClause.Occur.MUST);

        searchQuery.add(textBooleanQuery, BooleanClause.Occur.MUST);

        BooleanQuery discourseBooleanQuery = new BooleanQuery();
        QueryParser discourseParser = new QueryParser(Version.LUCENE_47, "discourse", analyzer);
        Query discourseQuery = discourseParser.parse(discourses);
        discourseBooleanQuery.add(discourseQuery, BooleanClause.Occur.MUST);
        searchQuery.add(discourseBooleanQuery, BooleanClause.Occur.MUST);

        return searchQuery;
    }

    public Analyzer getAnalyzer(List<String> languages) {
        if (languages.size() == 1) {
            return AnalyzerFactory.getInstance().getAnalyzer(languages.get(0));
        } else {
            return null;
        }
    }

    private String _getSortWord(String text, String order, String position) {
        if (sortWords.containsKey(text)) {
            return sortWords.get(text);
        }
        String word;
        if (order.equals("before")) {
            text = " " + text;
            if (position.equals("1")) {
                word = text.replaceAll(".* (\\S+) <b>.*", "$1");
            } else if (position.equals("2")) {
                word = text.replaceAll(".* (\\S+) (\\S+) <b>.*", "$1");
            } else if (position.equals("3")) {
                word = text.replaceAll(".* (\\S+) (\\S+) (\\S+) <b>.*", "$1");
            } else {
                word = text.replaceAll(".* (\\S+) (\\S+) (\\S+) (\\S+) <b>.*", "$1");
            }
        } else {
            if (position.equals("1")) {
                word = text.replaceAll(".*</b>[\\S]* (\\S+).*", "$1");
            } else if (position.equals("2")) {
                word = text.replaceAll(".*</b>[\\S]* (\\S+) (\\S+).*", "$2");
            } else if (position.equals("3")) {
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

    private void _sort(LuceneSnippet snippet, List<LuceneSnippet> snippets, SearchParameters params) {
        int start = 0;
        int end = snippets.size();
        int average;
        String candidate;
        String word = _getSortWord(snippet.getSnippet(), params.getSortField(), params.getPosition());
        if (!params.isLetterSearch() || word.startsWith(params.getLetter())) {
            if (word.isEmpty()) {
                snippets.add(0, snippet);
            } else {
                word = _replaceSpecialCharacters(word);
                while (start != end) {
                    average = (start + end) / 2;
//                candidate = _getCandidate(((HashMap<String, String>) snippets.get(average)).get("snippet"), order, position);
                    candidate = _getSortWord(snippets.get(average).getSnippet(), params.getSortField(), params.getPosition()).replaceAll("<i>(.*)</i>", "$1");
                    int result = word.toLowerCase().compareTo(candidate.toLowerCase());
                    if (result > 0) {
                        start = average + 1;
                    } else if (result == 0) {
                        start = average;
                        end = average;
                    } else {
                        end = average;
                    }
                }
                _setSnippet(snippet, params.getSortField(), params.getPosition());
                snippets.add(end, snippet);
            }
        }
    }

    private String _replaceSpecialCharacters(String word) {
        String result = word.replaceAll("á", "a");
        result = result.replaceAll("à", "a");
        result = result.replaceAll("ä", "a");
        result = result.replaceAll("é", "e");
        result = result.replaceAll("è", "e");
        result = result.replaceAll("ë", "e");
        result = result.replaceAll("í", "i");
        result = result.replaceAll("ì", "i");
        result = result.replaceAll("ï", "i");
        result = result.replaceAll("ó", "o");
        result = result.replaceAll("ò", "o");
        result = result.replaceAll("ö", "o");
        result = result.replaceAll("ú", "u");
        result = result.replaceAll("ù", "u");
        result = result.replaceAll("u", "u");
        result = result.replaceAll("Á", "A");
        result = result.replaceAll("À", "A");
        result = result.replaceAll("Ä", "A");
        result = result.replaceAll("É", "E");
        result = result.replaceAll("È", "E");
        result = result.replaceAll("Ë", "E");
        result = result.replaceAll("Í", "I");
        result = result.replaceAll("Ì", "I");
        result = result.replaceAll("Ï", "I");
        result = result.replaceAll("Ó", "O");
        result = result.replaceAll("Ò", "O");
        result = result.replaceAll("Ö", "O");
        result = result.replaceAll("Ú", "U");
        result = result.replaceAll("Ù", "U");
        result = result.replaceAll("Ü", "U");
        return result;
    }

    private void _setSnippet(LuceneSnippet snippet, String order, String position) {
        String text = snippet.getSnippet();
        String word;
        if (order.equals("before")) {
            if (position.equals("1")) {
                word = text.replaceAll("(.* )(\\S+)( <b>.*)", "$1<i>$2</i>$3");
                if (word.equals(text)) {
                    word = text.replaceAll("^(\\S+)( <b>.*)", "<i>$1</i>$2");
                }
            } else if (position.equals("2")) {
                word = text.replaceAll("(.* )(\\S+)( \\S+ <b>.*)", "$1<i>$2</i>$3");
                if (word.equals(text)) {
                    word = text.replaceAll("^(\\S+)( \\S+ <b>.*)", "<i>$1</i>$2");
                }
            } else if (position.equals("3")) {
                word = text.replaceAll("(.* )(\\S+)( \\S+ \\S+ <b>.*)", "$1<i>$2</i>$3");
                if (word.equals(text)) {
                    word = text.replaceAll("(.* )(\\S+)( \\S+ \\S+ <b>.*)", "<i>$1</i>$2");
                }
            } else {
                word = text.replaceAll("^(\\S+)( \\S+ \\S+ \\S+ <b>.*)", "<i>$2</i>$3");
                if (word.equals(text)) {
                    word = text.replaceAll("^(\\S+)( \\S+ \\S+ <b>.*)", "<i>$1</i>$2");
                }
            }
        } else {
            if (position.equals("1")) {
                word = text.replaceAll("(.*</b>[\\S]* )(\\S+)(.*)", "$1<i>$2</i>$3");
            } else if (position.equals("2")) {
                word = text.replaceAll("(.*</b>[\\S]* \\S+ )(\\S+)(.*)", "$1<i>$2</i>$3");
            } else if (position.equals("3")) {
                word = text.replaceAll("(.*</b>[\\S]* \\S+ \\S+ )(\\S+)(.*)", "$1<i>$2</i>$3");
            } else {
                word = text.replaceAll("(.*</b>[\\S]* \\S+ \\S+ \\S+ )(\\S+)(.*)", "$1<i>$2</i>$3");
            }
        }
        snippet.setSnippet(word);
    }
}
