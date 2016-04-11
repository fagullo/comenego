/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.labidiomas.corpus.searcher;

import es.ua.labidiomas.corpus.index.AnalyzerFactory;
import es.ua.labidiomas.corpus.searcher.search.SearchConfiguration;
import es.ua.labidiomas.corpus.searcher.search.SearchNode;
import es.ua.labidiomas.corpus.searcher.search.SearchOptions;
import es.ua.labidiomas.corpus.util.Config;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
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
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.grouping.SearchGroup;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.search.grouping.term.TermFirstPassGroupingCollector;
import org.apache.lucene.search.grouping.term.TermSecondPassGroupingCollector;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.NullFragmenter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
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
    private static final String TITLE_QUERY = "SELECT t.title, t.url FROM text t WHERE t.id = ?;";
    private static final String TITLE_DISCOURSES_QUERY = "SELECT d.code as name FROM discourse_texts dt, discourse d WHERE dt.text_id = ? AND d.id = dt.discourse_id;";
    private static final String DISCOURSES_QUERY = "SELECT d.code as name FROM paragraph p , discourse_texts dt, discourse d"
            + " WHERE dt.text_id = ? AND p.text_id = dt.text_id AND p.id = ? AND d.id = dt.discourse_id;";

    private static final String TRANSLATION_QUERY = "SELECT p.content FROM text t, paragraph p WHERE t.id = ? AND t.id = p.text_id AND p.numorder = ?;";
    private static final String TRANSLATION_TITLE_QUERY = "SELECT t.title FROM text t WHERE t.id = (SELECT original_text_id FROM text WHERE id = ?);";
    private static final String ORIGIN_QUERY = "SELECT t.original_text_id, p.numorder FROM text t, paragraph p WHERE p.id = ? AND t.id = p.text_id;";

    private Highlighter textHighlighter;
    private final Connection connection;
    private Analyzer analyzer;
    private IndexSearcher indexSearcher;

    public Searcher(SearchConfiguration parameters, Connection connection) throws IOException {
        this.connection = connection;
        setAnalyzer(parameters.getLanguage(), parameters.getOptions().isLematize());
        setHighlighter(parameters);
        setIndexSearcher(parameters.getLanguage(), parameters.getOptions());
    }

    public LuceneSnippet getBilingualSnippets(Document doc) throws IOException, SQLException, InvalidTokenOffsetsException {
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

                    TokenStream tokenStream = analyzer.tokenStream("text", textRS.getString("content"));
                    String url = textRS.getString("url");
                    String snippet = textHighlighter.getBestFragment(tokenStream, textRS.getString("content"));
                    return _setTranslation(snippet, url, discourses, paragraphID, textRS.getString("content"));
                }
            }
        }

        return null;
    }

    public List<LuceneSnippet> getTextSnippets(Document doc) throws IOException, SQLException, InvalidTokenOffsetsException {
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

                    TokenStream tokenStream = analyzer.tokenStream("text", textRS.getString("content"));
                    String url = textRS.getString("url");
                    String fragment = textHighlighter.getBestFragment(tokenStream, textRS.getString("content"));
                    return fragmentText(fragment, url, discourses);
                }
            }
        }

        return new ArrayList<>();
    }

    public LuceneSnippet getTitleBilingualSnippets(Document doc) throws IOException, SQLException, InvalidTokenOffsetsException {
        int textID = Integer.parseInt(doc.get("textID"));
        try (PreparedStatement textPS = connection.prepareStatement(TITLE_QUERY)) {
            textPS.setDouble(1, textID);
            try (ResultSet textRS = textPS.executeQuery()) {
                if (textRS.next()) {
                    String discourses = "";
                    try (PreparedStatement discoursePS = connection.prepareStatement(TITLE_DISCOURSES_QUERY)) {
                        discoursePS.setDouble(1, textID);
                        try (ResultSet discourseRS = discoursePS.executeQuery()) {
                            while (discourseRS.next()) {
                                discourses += discourseRS.getString("name") + " ";
                            }
                        }
                    }

                    TokenStream tokenStream = analyzer.tokenStream("title", textRS.getString("title"));
                    String url = textRS.getString("url");
                    String snippet = textHighlighter.getBestFragment(tokenStream, textRS.getString("title"));
                    return _setTitleTranslation(snippet, url, discourses, textID, textRS.getString("title"));
                }
            }
        }

        return null;
    }

    public List<LuceneSnippet> getTitleSnippets(Document doc) throws IOException, SQLException, InvalidTokenOffsetsException {
        int textID = Integer.parseInt(doc.get("textID"));
        try (PreparedStatement textPS = connection.prepareStatement(TITLE_QUERY)) {
            textPS.setDouble(1, textID);
            try (ResultSet textRS = textPS.executeQuery()) {
                if (textRS.next()) {
                    String discourses = "";
                    try (PreparedStatement discoursePS = connection.prepareStatement(TITLE_DISCOURSES_QUERY)) {
                        discoursePS.setDouble(1, textID);
                        try (ResultSet discourseRS = discoursePS.executeQuery()) {
                            while (discourseRS.next()) {
                                discourses += discourseRS.getString("name") + " ";
                            }
                        }
                    }

                    TokenStream tokenStream = analyzer.tokenStream("title", textRS.getString("title"));
                    String url = textRS.getString("url");
                    String fragment = textHighlighter.getBestFragment(tokenStream, textRS.getString("title"));
                    return fragmentText(fragment, url, discourses);
                }
            }
        }

        return new ArrayList<>();
    }

    /**
     *
     * @param text
     * @param url
     * @param discourses
     * @return
     */
    private List<LuceneSnippet> fragmentText(final String text, final String url, final String discourses) {
        List<LuceneSnippet> snippets = new ArrayList<>();
        if (text != null) {
            String fragment = text.replaceAll("[\n\r]", "").replaceAll("</b> <b>", " ").trim();
            int numMatches = StringUtils.countMatches(fragment, "<b>");
            if (numMatches > 1) {
                String snippet;
                int start = 0;
                int end = 0;
                for (int i = 0; i < numMatches; i++) {
                    if (i == (numMatches - 1)) {
                        snippet = fragment.substring(start);
                    } else {
                        int coincidence = fragment.indexOf("</b>", start) + 4;
                        int next = fragment.indexOf("<b>", coincidence);
                        end = (coincidence + next) / 2;
                        end = fragment.indexOf(" ", end);
                        snippet = fragment.substring(start, end);
                    }
                    start = end;
                    snippets.add(new LuceneSnippet(snippet, url, discourses));
                }
            } else {
                snippets.add(new LuceneSnippet(fragment, url, discourses));
            }
        }

        return snippets;
    }

    public List<LuceneSnippet> getSnippets(int doc, boolean isTitle, boolean isBilingual) throws IOException, SQLException, InvalidTokenOffsetsException {
        if (isBilingual) {
            List<LuceneSnippet> snippets = new ArrayList<>();
            LuceneSnippet snippet;
            if (isTitle) {
                snippet = getTitleBilingualSnippets(indexSearcher.doc(doc));
            } else {
                snippet = getBilingualSnippets(indexSearcher.doc(doc));
            }
            if (snippet != null) {
                snippets.add(snippet);
            }
            return snippets;
        } else {
            if (isTitle) {
                return getTitleSnippets(indexSearcher.doc(doc));
            } else {
                return getTextSnippets(indexSearcher.doc(doc));
            }
        }
    }

    public TopGroups prapareResults(BooleanQuery query, Integer page) throws IOException {

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
     * Prepares the highlighter to highlight the terms that matches with the
     * search criteria.
     *
     * @param searchQuery the query that contains the search criteria.
     * @return the highlighter configured.
     */
    private void setHighlighter(SearchConfiguration params) {
        Query query;
        if (params.getOptions().isTitle()) {
            query = _prepareQuery(params.getSearchNodes(), "title", params.getOptions().isOrder(), params.getOptions().isDistance());
        } else {
            query = _prepareQuery(params.getSearchNodes(), "text", params.getOptions().isOrder(), params.getOptions().isDistance());
        }
        QueryScorer scorer = new QueryScorer(query);
        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<b>", "</b>");
        this.textHighlighter = new Highlighter(formatter, scorer);
        textHighlighter.setTextFragmenter(new NullFragmenter());
    }

    /**
     * Prepares the index searcher to read the indexes in the indexes directory.
     *
     * @param language
     * @param options
     * @return @throws IOException
     */
    private void setIndexSearcher(String language, SearchOptions options) throws IOException {
        File indexDir;
        String baseIndexDir = Config.INDEXES_PATH + Config.FILE_SEPARATOR;
        if (options.isBilingual()) {
            baseIndexDir += "bilingual" + Config.FILE_SEPARATOR;
        }
        if (options.isTitle()) {
            baseIndexDir += "title" + Config.FILE_SEPARATOR;
        }
        if (options.isLematize()) {
            indexDir = new File(baseIndexDir + "lemma" + Config.FILE_SEPARATOR + language);
        } else {
            indexDir = new File(baseIndexDir + language);
        }
        if (!indexDir.isDirectory() || !indexDir.canRead()) {
            throw new IOException("Can not read the index path at '" + Config.INDEXES_PATH + "'");
        }

        Directory directory = FSDirectory.open(indexDir);
        DirectoryReader ireader = DirectoryReader.open(directory);
        this.indexSearcher = new IndexSearcher(ireader);
    }

    private void setAnalyzer(String language, boolean lemma) {
        this.analyzer = AnalyzerFactory.getInstance().getAnalyzer(language, lemma);
    }

    /**
     * Prepares the query with all the search criteria.
     *
     * @param analyzer
     * @param params
     * @return a boolean query which contains all the search criteria.
     * @throws org.apache.lucene.queryparser.classic.ParseException
     */
    public BooleanQuery prepareQuery(SearchConfiguration params) throws ParseException {

        BooleanQuery searchQuery = new BooleanQuery();
        SpanQuery query;
        if (params.getOptions().isTitle()) {
            query = _prepareQuery(params.getSearchNodes(), "title", params.getOptions().isOrder(), params.getOptions().isDistance());
        } else {
            query = _prepareQuery(params.getSearchNodes(), "text", params.getOptions().isOrder(), params.getOptions().isDistance());
        }
        if (params.isLetterSearch()) {
            return _prepareLetterQuery(params, query);
        } else {
            searchQuery.add(query, BooleanClause.Occur.MUST);
            searchQuery.add(_prepareDiscourseQuery(params), BooleanClause.Occur.MUST);
            return searchQuery;
        }
    }

    private SpanQuery _prepareQuery(List<SearchNode> searchNodes, String field, boolean order, boolean precise) {
        SpanQuery[] clauses = new SpanQuery[]{
            new SpanTermQuery(new Term(field, searchNodes.get(0).getWord().toLowerCase()))
        };
        SpanQuery query = new SpanNearQuery(clauses, 0, true);
        for (int i = 1; i < searchNodes.size(); i++) {
            SearchNode node = searchNodes.get(i);
            SearchNode prevNode = searchNodes.get(i - 1);
            clauses = new SpanQuery[]{
                query,
                new SpanTermQuery(new Term(field, node.getWord().toLowerCase()))
            };
            query = new SpanNearQuery(clauses, prevNode.getDistance(), order);
            if (precise && prevNode.getDistance() > 0) {
                SpanNearQuery spanNearQuery = new SpanNearQuery(clauses, prevNode.getDistance() - 1, order);
                query = new SpanNotQuery(query, spanNearQuery);
            }
        }

        return query;
    }

    private BooleanQuery _prepareDiscourseQuery(SearchConfiguration params) throws ParseException {
        BooleanQuery discourseBooleanQuery = new BooleanQuery();
        QueryParser discourseParser = new QueryParser(Version.LUCENE_47, "discourse", analyzer);
        Query discourseQuery = discourseParser.parse(params.getDiscoursesAsString());
        discourseBooleanQuery.add(discourseQuery, BooleanClause.Occur.MUST);

        return discourseBooleanQuery;
    }

    private BooleanQuery _prepareLetterQuery(SearchConfiguration params, SpanQuery query) throws ParseException {
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

        searchQuery.add(_prepareDiscourseQuery(params), BooleanClause.Occur.MUST);

        return searchQuery;
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

    private LuceneSnippet _setTranslation(String snippet, String url, String discourses, int paragraphID, String original) throws SQLException {
        String translation = "";
        try (PreparedStatement originalPS = connection.prepareStatement(ORIGIN_QUERY)) {
            originalPS.setDouble(1, paragraphID);
            try (ResultSet originalRS = originalPS.executeQuery()) {
                originalRS.next();
                double textID = originalRS.getDouble("original_text_id");
                int numorder = originalRS.getInt("numorder");
                try (PreparedStatement translatePS = connection.prepareStatement(TRANSLATION_QUERY)) {
                    translatePS.setDouble(1, textID);
                    translatePS.setInt(2, numorder);
                    try (ResultSet translateRS = translatePS.executeQuery()) {
                        translateRS.next();
                        translation = translateRS.getString("content");
                    }
                }
            }
        }
        return new LuceneSnippet(snippet, url, discourses, translation, original);
    }

    private LuceneSnippet _setTitleTranslation(String snippet, String url, String discourses, double textID, String original) throws SQLException {
        String translation = "";
        try (PreparedStatement translatePS = connection.prepareStatement(TRANSLATION_TITLE_QUERY)) {
            translatePS.setDouble(1, textID);
            try (ResultSet translateRS = translatePS.executeQuery()) {
                translateRS.next();
                translation = translateRS.getString("title");
            }
        }
        return new LuceneSnippet(snippet, url, discourses, translation, original);
    }
}
