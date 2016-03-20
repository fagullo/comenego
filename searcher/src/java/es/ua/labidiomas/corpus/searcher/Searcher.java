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
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
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
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanNotQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
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

    public List<LuceneSnippet> getTextSnippets(
            int termSize,
            List<LuceneSnippet> snippets,
            ScoreDoc sd,
            IndexSearcher indexSearcher,
            Connection connection,
            Analyzer analyzer,
            Highlighter textHighlighter,
            boolean isSubSearch
    ) throws IOException, SQLException, InvalidTokenOffsetsException {
        Document doc = indexSearcher.doc(sd.doc);
        int paragraphID = Integer.parseInt(doc.get("paragraphID"));
        int textID = Integer.parseInt(doc.get("textID"));
        PreparedStatement textPS = connection.prepareStatement("SELECT p.content, t.url FROM text t, paragraph p WHERE p.text_id = t.id AND p.id = ?;");
        textPS.setDouble(1, paragraphID);
        ResultSet textRS = textPS.executeQuery();
        if (textRS.next()) {
            String discourses = "";
            String discorusesQuery = "SELECT d.code as name FROM paragraph p , discourse_texts dt, discourse d"
                    + " WHERE dt.text_id = ? AND p.text_id = dt.text_id AND p.id = ? AND d.id = dt.discourse_id;";
            PreparedStatement discoursePS = connection.prepareStatement(discorusesQuery);
            discoursePS.setDouble(1, textID);
            discoursePS.setDouble(2, paragraphID);
            ResultSet discourseRS = discoursePS.executeQuery();
            while (discourseRS.next()) {
                discourses += discourseRS.getString("name") + " ";
            }
            discourseRS.close();
            discoursePS.close();

            LuceneSnippet result = new LuceneSnippet();
            TokenStream tokenStream = analyzer.tokenStream("text", textRS.getString("content"));
            String url = textRS.getString("url");
            org.apache.lucene.search.highlight.TextFragment[] fragments = textHighlighter.getBestTextFragments(tokenStream, textRS.getString("content"), false, 600);
            for (TextFragment frag : fragments) {
                if (frag.getScore() > 0) {
                    String snippet = frag.toString().replaceAll("[\n\r]", "").replaceAll("</b> <b>", " ").trim();
                    int numMatches = StringUtils.countMatches(snippet, "<b>");
                    if (numMatches > 1 && !isSubSearch) {
                        int start = 0;
                        int end = 0;
                        for (int i = 0; i < numMatches; i++) {
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
                            snippets.add(result);
                        }
                    } else if (snippet.length() > termSize) {
                        result.setSnippet(snippet);
                        result.setUrl(url);
                        result.setDiscourses(discourses);
                        snippets.add(result);
                    }
                }
            }
        }
        textRS.close();
        textPS.close();

        return snippets;
    }

    public List<LuceneSnippet> getTitleSnippets(
            int termSize,
            List<LuceneSnippet> snippets,
            ScoreDoc sd,
            IndexSearcher indexSearcher,
            Connection connection,
            Analyzer analyzer,
            Highlighter textHighlighter
    ) throws IOException, SQLException, InvalidTokenOffsetsException {
        Document doc = indexSearcher.doc(sd.doc);
        int textID = Integer.parseInt(doc.get("textID"));
        PreparedStatement textPS = connection.prepareStatement("SELECT t.title, t.url FROM text t WHERE t.id = ?;");
        textPS.setDouble(1, textID);
        ResultSet textRS = textPS.executeQuery();
        if (textRS.next()) {
            String discourses = "";
            String discorusesQuery = "SELECT d.code as name FROM discourse_texts dt, discourse d"
                    + " WHERE dt.text_id = ? AND d.id = dt.discourse_id;";
            PreparedStatement discoursePS = connection.prepareStatement(discorusesQuery);
            discoursePS.setDouble(1, textID);
            ResultSet discourseRS = discoursePS.executeQuery();
            while (discourseRS.next()) {
                discourses += discourseRS.getString("name") + " ";
            }
            discourseRS.close();
            discoursePS.close();

            LuceneSnippet result = new LuceneSnippet();
            TokenStream tokenStream = analyzer.tokenStream("title", textRS.getString("title"));
            String url = textRS.getString("url");
            org.apache.lucene.search.highlight.TextFragment[] fragments = textHighlighter.getBestTextFragments(tokenStream, textRS.getString("title"), false, 600);
            for (TextFragment frag : fragments) {
                if (frag.getScore() > 0) {
                    String snippet = frag.toString().replaceAll("[\n\r]", "").replaceAll("</b> <b>", " ").trim();
                    int numMatches = StringUtils.countMatches(snippet, "<b>");
                    if (numMatches > 1) {
                        int start = 0;
                        int end = 0;
                        for (int i = 0; i < numMatches; i++) {
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
                            snippets.add(result);
                        }
                    } else if (snippet.length() > termSize) {
                        result.setSnippet(snippet);
                        result.setUrl(url);
                        result.setDiscourses(discourses);
                        snippets.add(result);
                    }
                }
            }
        }
        textRS.close();
        textPS.close();

        return snippets;
    }

    public List<LuceneSnippet> getSnippets(
            int termSize,
            List<LuceneSnippet> snippets,
            ScoreDoc sd,
            IndexSearcher indexSearcher,
            Connection connection,
            Analyzer analyzer,
            Highlighter textHighlighter,
            SearchParameters params
    ) throws IOException, SQLException, InvalidTokenOffsetsException {
        if (params.isTitle()) {
            return getTitleSnippets(termSize, snippets, sd, indexSearcher, connection, analyzer, textHighlighter);
        } else {
            return getTextSnippets(termSize, snippets, sd, indexSearcher, connection, analyzer, textHighlighter, params.isSubSearch());
        }
    }
    
    public List<LuceneSnippet> getSnippets(
            int termSize,
            List<LuceneSnippet> snippets,
            ScoreDoc sd,
            IndexSearcher indexSearcher,
            Connection connection,
            Analyzer analyzer,
            Highlighter textHighlighter,
            SearchConfiguration params
    ) throws IOException, SQLException, InvalidTokenOffsetsException {
        if (params.isTitle()) {
            return getTitleSnippets(termSize, snippets, sd, indexSearcher, connection, analyzer, textHighlighter);
        } else {
            return getTextSnippets(termSize, snippets, sd, indexSearcher, connection, analyzer, textHighlighter, false);
        }
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

    /**
     * Prepares the index searcher to read the indexes in the indexes directory.
     *
     * @param language
     * @param sort
     * @param searchText
     * @param title
     * @param lemma
     * @return @throws IOException
     */
    public IndexSearcher prepareIndexSearcher(String language, String sort, String searchText, boolean lemma, boolean title) throws IOException {
        File indexDir;
        String baseIndexDir = Config.INDEXES_PATH + Config.FILE_SEPARATOR;
        if (title) {
            baseIndexDir += "title" + Config.FILE_SEPARATOR;
        }
        if (lemma) {
            indexDir = new File(baseIndexDir + "lemma" + Config.FILE_SEPARATOR + language);
        } else {
            indexDir = new File(baseIndexDir + language);
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
    public BooleanQuery prepareQuery(
            String searchText,
            String discourses,
            Analyzer analyzer,
            String sortField,
            int position,
            boolean isLetter,
            String letter,
            SubSearch subSearch,
            boolean isTitle
    ) throws ParseException {
        if (isLetter) {
            return _prepareLetterQuery(searchText, discourses, analyzer, letter, position);
        } else if (isTitle) {
            return _prepareTitleQuery(searchText, discourses, analyzer);
        } else {
            if (subSearch != null) {
                return _prepareQuerySubSearch(searchText, subSearch, discourses, analyzer);
            } else {
                return _prepareQuery(searchText, discourses, analyzer);
            }
        }
    }

    private BooleanQuery _prepareTitleQuery(String searchText, String discourses, Analyzer analyzer) throws ParseException {
        BooleanQuery searchQuery = new BooleanQuery();

        BooleanQuery textBooleanQuery = new BooleanQuery();
        QueryParser textParser = new QueryParser(Version.LUCENE_47, "title", analyzer);
        Query textQuery;
        if (searchText.startsWith("\"")) {
            textQuery = textParser.parse(searchText);
        } else {
            textQuery = textParser.parse("\"" + searchText + "\"");
        }
        textBooleanQuery.add(textQuery, BooleanClause.Occur.MUST);
        searchQuery.add(textBooleanQuery, BooleanClause.Occur.MUST);

        BooleanQuery discourseBooleanQuery = new BooleanQuery();
        QueryParser discourseParser = new QueryParser(Version.LUCENE_47, "discourse", analyzer);
        Query discourseQuery = discourseParser.parse(discourses);
        discourseBooleanQuery.add(discourseQuery, BooleanClause.Occur.MUST);
        searchQuery.add(discourseBooleanQuery, BooleanClause.Occur.MUST);
        return searchQuery;
    }

    private BooleanQuery _prepareLetterQuery(String searchText, String discourses, Analyzer analyzer, String letter, int position) throws ParseException {
        BooleanQuery searchQuery = new BooleanQuery();

        SpanQuery prefixQUery = new SpanMultiTermQueryWrapper(new PrefixQuery(new Term("text", letter)));

        SpanNearQuery spanNear1 = new SpanNearQuery(new SpanQuery[]{
            new SpanTermQuery(new Term("text", searchText)),
            prefixQUery
        }, position - 1, true);

        if (position != 1) {
            SpanNearQuery spanNear2 = new SpanNearQuery(new SpanQuery[]{
                new SpanTermQuery(new Term("text", searchText)),
                prefixQUery
            }, position - 2, true);

            SpanNotQuery textQUery = new SpanNotQuery(spanNear1, spanNear2);

            searchQuery.add(textQUery, BooleanClause.Occur.MUST);
        } else {
            searchQuery.add(spanNear1, BooleanClause.Occur.MUST);
        }

        BooleanQuery discourseBooleanQuery = new BooleanQuery();
        QueryParser discourseParser = new QueryParser(Version.LUCENE_47, "discourse", analyzer);
        Query discourseQuery = discourseParser.parse(discourses);
        discourseBooleanQuery.add(discourseQuery, BooleanClause.Occur.MUST);
        searchQuery.add(discourseBooleanQuery, BooleanClause.Occur.MUST);

        return searchQuery;
    }

    private BooleanQuery _prepareStandardQuery(String searchText, Analyzer analyzer) throws ParseException {
        BooleanQuery textBooleanQuery = new BooleanQuery();
        QueryParser textParser = new QueryParser(Version.LUCENE_47, "text", analyzer);
        Query textQuery;
        textQuery = textParser.parse(searchText);
        textBooleanQuery.add(textQuery, BooleanClause.Occur.MUST);
        return textBooleanQuery;
    }

    private BooleanQuery _prepareQuotesQuery(String searchText) throws ParseException {
        BooleanQuery textBooleanQuery = new BooleanQuery();

        String text = searchText.substring(1, searchText.length() - 1);
        String[] words = text.split(" ");
        textBooleanQuery.add(_prepareSpanQuery(words, "text", 0), BooleanClause.Occur.MUST);

        return textBooleanQuery;
    }

    private BooleanQuery _prepareQuery(String searchText, String discourses, Analyzer analyzer) throws ParseException {
        BooleanQuery searchQuery = new BooleanQuery();

        if (searchText.startsWith("\"") && searchText.endsWith("\"")) {
            searchQuery.add(_prepareQuotesQuery(searchText), BooleanClause.Occur.MUST);
        } else {
            searchQuery.add(_prepareStandardQuery(searchText, analyzer), BooleanClause.Occur.MUST);
        }

        BooleanQuery discourseBooleanQuery = new BooleanQuery();
        QueryParser discourseParser = new QueryParser(Version.LUCENE_47, "discourse", analyzer);
        Query discourseQuery = discourseParser.parse(discourses);
        discourseBooleanQuery.add(discourseQuery, BooleanClause.Occur.MUST);
        searchQuery.add(discourseBooleanQuery, BooleanClause.Occur.MUST);

        return searchQuery;
    }

    private BooleanQuery _prepareQuerySubSearch(String searchText, SubSearch lastSearch, String discourses, Analyzer analyzer) throws ParseException {
        BooleanQuery searchQuery = new BooleanQuery();

        SpanQuery[] clauses = new SpanQuery[]{
            new SpanTermQuery(new Term(searchText, "text")),
            new SpanTermQuery(new Term(lastSearch.getText(), lastSearch.getField()))
        };

        searchQuery.add(_prepareSpanQuery(clauses, Integer.MAX_VALUE), BooleanClause.Occur.MUST);

        QueryParser discourseParser = new QueryParser(Version.LUCENE_47, "discourse", analyzer);
        Query discourseQuery = discourseParser.parse(discourses);
        searchQuery.add(discourseQuery, BooleanClause.Occur.MUST);

        return searchQuery;
    }

    private SpanNearQuery _prepareSpanQuery(String[] searchText, String term, int slop) throws ParseException {

        SpanQuery[] clauses = new SpanQuery[searchText.length];

        for (int i = 0; i < searchText.length; i++) {
            clauses[i] = new SpanTermQuery(new Term(term, searchText[i]));
        }

        SpanNearQuery textQuery = new SpanNearQuery(clauses, slop, false);
        return textQuery;
    }

    private SpanNearQuery _prepareSpanQuery(SpanQuery[] clauses, int slop) throws ParseException {
        SpanNearQuery textQuery = new SpanNearQuery(clauses, slop, false);
        return textQuery;
    }

    public Analyzer getAnalyzer(List<String> languages, boolean lemma) {
        if (languages.size() == 1) {
            return AnalyzerFactory.getInstance().getAnalyzer(languages.get(0), lemma);
        } else {
            return null;
        }
    }

    public Analyzer getAnalyzer(String language, boolean lemma) {
        return AnalyzerFactory.getInstance().getAnalyzer(language, lemma);
    }

    public void setSnippet(LuceneSnippet snippet, String order, int position) {
        String text = snippet.getSnippet();
        String word;
        if (order.equals("before")) {
            if (position == 1) {
                word = text.replaceAll("(.* )(\\S+)( <b>.*)", "$1<i>$2</i>$3");
                if (word.equals(text)) {
                    word = text.replaceAll("^(\\S+)( <b>.*)", "<i>$1</i>$2");
                }
            } else if (position == 2) {
                word = text.replaceAll("(.* )(\\S+)( \\S+ <b>.*)", "$1<i>$2</i>$3");
                if (word.equals(text)) {
                    word = text.replaceAll("^(\\S+)( \\S+ <b>.*)", "<i>$1</i>$2");
                }
            } else if (position == 3) {
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
            if (position == 1) {
                word = text.replaceAll("(.*</b>[\\S]* )(\\S+)(.*)", "$1<i>$2</i>$3");
            } else if (position == 2) {
                word = text.replaceAll("(.*</b>[\\S]* \\S+ )(\\S+)(.*)", "$1<i>$2</i>$3");
            } else if (position == 3) {
                word = text.replaceAll("(.*</b>[\\S]* \\S+ \\S+ )(\\S+)(.*)", "$1<i>$2</i>$3");
            } else {
                word = text.replaceAll("(.*</b>[\\S]* \\S+ \\S+ \\S+ )(\\S+)(.*)", "$1<i>$2</i>$3");
            }
        }
        snippet.setSnippet(word);
    }
}
