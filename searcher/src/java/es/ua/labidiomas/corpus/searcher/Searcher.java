/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.labidiomas.corpus.searcher;

import es.ua.labidiomas.corpus.searcher.search.SearchParameters;
import es.ua.labidiomas.corpus.searcher.search.SearchConfiguration;
import es.ua.labidiomas.corpus.index.AnalyzerFactory;
import es.ua.labidiomas.corpus.searcher.search.SearchNode;
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

    private static final String TEXT_QUERY = "SELECT p.content, t.url FROM text t, paragraph p WHERE p.text_id = t.id AND p.id = ?;";
    private static final String DISCOURSES_QUERY = "SELECT d.code as name FROM paragraph p , discourse_texts dt, discourse d"
            + " WHERE dt.text_id = ? AND p.text_id = dt.text_id AND p.id = ? AND d.id = dt.discourse_id;";

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
        try (PreparedStatement textPS = connection.prepareStatement(TEXT_QUERY)) {
            textPS.setDouble(1, paragraphID);
            try (ResultSet textRS = textPS.executeQuery()) {
                if (textRS.next()) {
                    String discourses = "";
                    try (PreparedStatement discoursePS = connection.prepareStatement(DISCOURSES_QUERY)) {
                        discoursePS.setDouble(1, textID);
                        discoursePS.setDouble(2, paragraphID);
                        try (ResultSet discourseRS = discoursePS.executeQuery()) {
                            while (discourseRS.next()) {
                                discourses += discourseRS.getString("name") + " ";
                            }
                        }
                    }

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
            }
        }

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
            boolean isTitle
    ) throws IOException, SQLException, InvalidTokenOffsetsException {
        if (isTitle) {
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
     * @param searchText
     * @param title
     * @param lemma
     * @return @throws IOException
     */
    public IndexSearcher prepareIndexSearcher(String language, String searchText, boolean lemma, boolean title) throws IOException {
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
     * @param analyzer
     * @param params
     * @return a boolean query which contains all the search criteria.
     * @throws org.apache.lucene.queryparser.classic.ParseException
     */
    public BooleanQuery prepareQuery(Analyzer analyzer, SearchConfiguration params) throws ParseException {

        BooleanQuery searchQuery = new BooleanQuery();
        SpanQuery query;
        if (params.getOptions().isTitle()) {
            query = _prepareQuery(params.getSearchNodes(), "title", params.getOptions().isOrder(), params.getOptions().isDistance());
        } else {
            query = _prepareQuery(params.getSearchNodes(), "text", params.getOptions().isOrder(), params.getOptions().isDistance());
        }
        if (params.isLetterSearch()) {
            return _prepareLetterQuery(analyzer, params, query);
        } else {
            searchQuery.add(query, BooleanClause.Occur.MUST);
            searchQuery.add(_prepareDiscourseQuery(analyzer, params), BooleanClause.Occur.MUST);
            return searchQuery;
        }
    }

    private SpanQuery _prepareQuery(List<SearchNode> searchNodes, String field, boolean order, boolean precise) {
        SpanQuery[] clauses = new SpanQuery[]{
            new SpanTermQuery(new Term(field, searchNodes.get(0).getWord()))
        };
        SpanNearQuery query = new SpanNearQuery(clauses, 0, true);
        for (int i = 1; i < searchNodes.size(); i++) {
            SearchNode node = searchNodes.get(i);
            SearchNode prevNode = searchNodes.get(i - 1);
            clauses = new SpanQuery[]{
                query,
                new SpanTermQuery(new Term(field, node.getWord()))
            };
            query = new SpanNearQuery(clauses, prevNode.getDistance(), order);
        }

        return query;
    }

    private BooleanQuery _prepareDiscourseQuery(Analyzer analyzer, SearchConfiguration params) throws ParseException {
        BooleanQuery discourseBooleanQuery = new BooleanQuery();
        QueryParser discourseParser = new QueryParser(Version.LUCENE_47, "discourse", analyzer);
        Query discourseQuery = discourseParser.parse(params.getDiscoursesAsString());
        discourseBooleanQuery.add(discourseQuery, BooleanClause.Occur.MUST);

        return discourseBooleanQuery;
    }

    private BooleanQuery _prepareLetterQuery(Analyzer analyzer, SearchConfiguration params, SpanQuery query) throws ParseException {
        BooleanQuery searchQuery = new BooleanQuery();

        SpanQuery prefixQUery = new SpanMultiTermQueryWrapper(new PrefixQuery(new Term("text", params.getSort().getLetter())));

        SpanNearQuery spanNear1 = new SpanNearQuery(new SpanQuery[]{
            query,
            prefixQUery
        }, params.getSort().getPosition() - 1, true);

        if (params.getSort().getPosition() != 1) {
            SpanNearQuery spanNear2 = new SpanNearQuery(new SpanQuery[]{
                query,
                prefixQUery
            }, params.getSort().getPosition() - 2, true);

            SpanNotQuery textQUery = new SpanNotQuery(spanNear1, spanNear2);

            searchQuery.add(textQUery, BooleanClause.Occur.MUST);
        } else {
            searchQuery.add(spanNear1, BooleanClause.Occur.MUST);
        }

        searchQuery.add(_prepareDiscourseQuery(analyzer, params), BooleanClause.Occur.MUST);

        return searchQuery;
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
