/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.labidiomas.corpus.servlet;

import es.ua.labidiomas.corpus.index.AnalyzerFactory;
import es.ua.labidiomas.corpus.util.Config;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import org.apache.lucene.search.grouping.GroupDocs;
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
import org.json.simple.JSONObject;

/**
 *
 * @author paco
 */
@WebServlet("/search")
public class Search extends HttpServlet {

    private static final int DOCS_BY_PAGE = 50;

    private HashMap<String, String> sortWords;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=ISO-8859-1");
        Connection conexion = null;
        String searchText = request.getParameter("search");
        String[] languages = request.getParameterValues("languages");
        String discourses = request.getParameter("discourses");
        String letter = request.getParameter("letter");
        String position = request.getParameter("position");
        PrintWriter printout = response.getWriter();
        String page = request.getParameter("page") == null ? "1" : request.getParameter("page");
        boolean isLetter = false;
        if (letter != null && !letter.isEmpty() && !letter.equals("null")) {
            isLetter = true;
        }
        String sortField = request.getParameter("sortField");

        HashMap<String, Object> result = new HashMap<String, Object>();
        ArrayList<Object> snippets = new ArrayList<Object>();
        try {
            if (discourses != null) {
                Analyzer analyzer = _getAnalyzer(languages);
                IndexSearcher indexSearcher = _prepareIndexSearcher(languages[0], sortField, searchText);
                BooleanQuery searchQuery = _prepareQuery(searchText, discourses, analyzer, page, sortField, position, isLetter, letter);
                Highlighter textHighlighter = _prepareHighlighter(searchQuery);

                TopGroups tg;
                if (sortField == null || sortField.isEmpty()) {
                    tg = _prapareResults(searchQuery, indexSearcher, page);
                } else {
                    sortWords = new HashMap<String, String>();
                    tg = _prapareSortedResults(searchQuery, indexSearcher, page, sortField);
                }
                if (tg != null) {

                    GroupDocs[] groupedDocs = tg.groups;
                    result.put("numPages", Math.ceil(tg.totalGroupedHitCount / DOCS_BY_PAGE));
                    result.put("numDocs", tg.totalGroupedHitCount);
                    Class.forName("com.mysql.jdbc.Driver");
                    conexion = DriverManager.getConnection(Config.CONEXION_STRING, Config.DB_USER, Config.DB_PASS);
                    GroupDocs groupDoc = groupedDocs[0];
                    int pageNum = Integer.parseInt(page);
                    int offset = (pageNum - 1) * DOCS_BY_PAGE;
                    int numPageResults = pageNum * DOCS_BY_PAGE;
                    int top = Math.min(groupDoc.scoreDocs.length, numPageResults);
                    for (int i = offset; i < top; i++) {
                        ScoreDoc sd = groupDoc.scoreDocs[i];
                        _getSnippets(searchText.length(), snippets, sd, indexSearcher, conexion, analyzer, textHighlighter, sortField, position, letter, isLetter);
                    }
                }
            }
            result.put("matches", snippets);
            if (sortField == null || sortField.isEmpty()) {
                result.put("sorted", "false");
            } else {
                result.put("sorted", "true");
            }
            JSONObject jObj = new JSONObject();
            jObj.putAll(result);
            printout.print(jObj);
            printout.flush();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidTokenOffsetsException ex) {
            Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (conexion != null) {
                try {
                    conexion.close();
                } catch (SQLException ex) {
                    Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void _getSnippets(int termSize, ArrayList<Object> snippets, ScoreDoc sd, IndexSearcher indexSearcher, Connection conexion, Analyzer analyzer, Highlighter textHighlighter, String order, String position, String letter, boolean isLetter) throws IOException, SQLException, InvalidTokenOffsetsException {
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

            HashMap<String, String> result = new HashMap<String, String>();
            TokenStream tokenStream = analyzer.tokenStream("text", textRS.getString("content"));
            String url = textRS.getString("url");
            org.apache.lucene.search.highlight.TextFragment[] fragments = textHighlighter.getBestTextFragments(tokenStream, textRS.getString("content"), false, 600);
            for (TextFragment frag : fragments) {
                if (frag.getScore() > 0 && result.isEmpty()) {
                    String snippet = frag.toString().replaceAll("[\n\r]", "").replaceAll("</b> <b>", " ").trim();
                    int numMatches = StringUtils.countMatches(snippet, "<b>");
                    if (numMatches > 1) {
                        int start = 0;
                        int end = 0;
                        for (int i = 0; i < numMatches; i++) {
                            result = new HashMap<String, String>();
                            if (i == (numMatches - 1)) {
                                result.put("snippet", snippet.substring(start));
                            } else {
                                int coincidence = snippet.indexOf("</b>", start) + 4;
                                int next = snippet.indexOf("<b>", coincidence);
                                end = (coincidence + next) / 2;
                                end = snippet.indexOf(" ", end);
                                result.put("snippet", snippet.substring(start, end));
                            }
                            result.put("url", url);
                            result.put("discourses", discourses);
                            start = end;
                            if (order != null && !order.isEmpty()) {
                                _sort(result, snippets, order, position, letter, isLetter);
                            } else {
                                snippets.add(result);
                                result = new HashMap<String, String>();
                            }
                        }
                    } else if (snippet.length() > termSize) {
                        result.put("snippet", snippet);
                        result.put("url", url);
                        result.put("discourses", discourses);
                        if (order != null && !order.isEmpty()) {
                            _sort(result, snippets, order, position, letter, isLetter);
                        } else {
                            snippets.add(result);
                        }
                    }
                }
            }
        }
        textRS.close();
        textPS.close();
    }

    /**
     * Prepares the highlighter to highlight the terms that matches with the
     * search criteria.
     *
     * @param searchQuery the query that contains the search criteria.
     * @return the highlighter configured.
     */
    private Highlighter _prepareHighlighter(BooleanQuery searchQuery) {
        QueryScorer scorer = new QueryScorer(searchQuery.getClauses()[0].getQuery());
        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<b>", "</b>");
        Highlighter textHighlighter = new Highlighter(formatter, scorer);
        textHighlighter.setTextFragmenter(new SimpleFragmenter(100));

        return textHighlighter;
    }

    private TopGroups _prapareResults(BooleanQuery query, IndexSearcher indexSearcher, String page) throws IOException {

        int pageNum = Integer.parseInt(page);

        int targetPage = pageNum * DOCS_BY_PAGE;

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

    private TopGroups _prapareSortedResults(BooleanQuery query, IndexSearcher indexSearcher, String page, String sortField) throws IOException {

        int pageNum = Integer.parseInt(page);
        int targetPage = pageNum * DOCS_BY_PAGE;

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
    private IndexSearcher _prepareIndexSearcher(String language, String sort, String searchText) throws IOException {
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
     * @param languages the languages that the text must be.
     * @param discourses the different discourses which the text could pertain
     * to.
     * @param analyzer the analyzer used to index.
     * @return a boolean query which contains all the search criteria.
     */
    private BooleanQuery _prepareQuery(String searchText, String discourses, Analyzer analyzer, String page, String sortField, String position, boolean isLetter, String letter) throws ParseException {
        if (isLetter) {
            return _prepareLetterQuery(searchText, discourses, analyzer, letter, sortField, position);
        } else {
            return _prepareQuery(searchText, discourses, analyzer);
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

    private Analyzer _getAnalyzer(String[] languages) {
        if (languages.length == 1) {
            return AnalyzerFactory.getInstance().getAnalyzer(languages[0]);
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

    private void _sort(HashMap<String, String> snippet, ArrayList<Object> snippets, String order, String position, String letter, boolean isLetter) {
        int start = 0;
        int end = snippets.size();
        int average;
        String candidate;
        String word = _getSortWord(snippet.get("snippet"), order, position);
        if (!isLetter || word.startsWith(letter)) {
            if (word.isEmpty()) {
                snippets.add(0, snippet);
            } else {
                word = _replaceSpecialCharacters(word);
                while (start != end) {
                    average = (start + end) / 2;
//                candidate = _getCandidate(((HashMap<String, String>) snippets.get(average)).get("snippet"), order, position);
                    candidate = _getSortWord(((HashMap<String, String>) snippets.get(average)).get("snippet"), order, position).replaceAll("<i>(.*)</i>", "$1");
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
                _setSnippet(snippet, order, position);
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

    private void _setSnippet(HashMap<String, String> snippet, String order, String position) {
        String text = snippet.get("snippet");
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
        snippet.put("snippet", word);
    }

}
