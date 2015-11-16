/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.labidiomas.corpus.services;

import es.ua.labidiomas.corpus.searcher.LuceneSnippet;
import es.ua.labidiomas.corpus.searcher.SearchParameters;
import es.ua.labidiomas.corpus.searcher.SearchResponse;
import es.ua.labidiomas.corpus.searcher.Searcher;
import es.ua.labidiomas.corpus.util.Config;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.grouping.GroupDocs;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;

/**
 * REST Web Service
 *
 * @author paco
 */
@Path("comenego/")
public class Comenego {

    private Searcher searcher;

    private static final int DOCS_BY_PAGE = 50;

    private HashMap<String, String> sortWords;

    @Context
    private ServletContext context;

    /**
     * Creates a new instance of Searcher
     */
    public Comenego() {
        searcher = new Searcher();
    }

    /**
     * Retrieves representation of an instance of
     * es.ua.labidiomas.corpus.services.Searcher
     *
     * @param parameters
     * @return an instance of java.lang.String
     * @throws java.io.IOException
     * @throws org.apache.lucene.queryparser.classic.ParseException
     * @throws java.lang.ClassNotFoundException
     * @throws java.sql.SQLException
     * @throws org.apache.lucene.search.highlight.InvalidTokenOffsetsException
     */
    @POST
    @Path("load")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response load(SearchParameters parameters) throws IOException, ParseException, ClassNotFoundException, SQLException, InvalidTokenOffsetsException {
        SearchResponse result = new SearchResponse();
        List<LuceneSnippet> snippets = new ArrayList<LuceneSnippet>();
        Connection connection = null;
        try {
            if (parameters.getDiscourses() != null) {
                Analyzer analyzer = searcher.getAnalyzer(parameters.getLanguages());
                IndexSearcher indexSearcher = searcher.prepareIndexSearcher(parameters.getLanguages().get(0), parameters.getSortField(), parameters.getSearch());
                BooleanQuery searchQuery = searcher.prepareQuery(parameters.getSearch(), parameters.getDiscourses(), analyzer,
                        parameters.getSortField(), parameters.getPosition(), parameters.isLetterSearch(), parameters.getLetter(),
                        parameters.getSubsearch());
                Highlighter textHighlighter = searcher.prepareHighlighter(searchQuery);

                TopGroups tg;
                if (parameters.getSortField() == null || parameters.getSortField().isEmpty()) {
                    tg = searcher.prapareResults(searchQuery, indexSearcher, parameters.getPage());
                } else {
                    sortWords = new HashMap<String, String>();
                    tg = searcher.prapareSortedResults(searchQuery, indexSearcher, parameters.getPage(), parameters.getSortField());
                }
                if (tg != null) {

                    GroupDocs[] groupedDocs = tg.groups;
                    result.setNumPages((int) Math.ceil(tg.totalGroupedHitCount / DOCS_BY_PAGE));
                    result.setNumDocs(tg.totalGroupedHitCount);
                    Class.forName("com.mysql.jdbc.Driver");
                    connection = DriverManager.getConnection(Config.CONEXION_STRING, Config.DB_USER, Config.DB_PASS);
                    GroupDocs groupDoc = groupedDocs[0];
                    int offset = (parameters.getPage() - 1) * DOCS_BY_PAGE;
                    int numPageResults = parameters.getPage() * DOCS_BY_PAGE;
                    int top = Math.min(groupDoc.scoreDocs.length, numPageResults);
                    for (int i = offset; i < top; i++) {
                        ScoreDoc sd = groupDoc.scoreDocs[i];
                        searcher.getSnippets(parameters.getSearch().length(), snippets, sd, indexSearcher, connection, analyzer, textHighlighter, parameters);
                    }
                }
            }
            result.setMatches(snippets);
            if (parameters.getSortField() == null || parameters.getSortField().isEmpty()) {
                result.setSorted(false);
            } else {
                result.setSorted(true);
            }
        } finally {
            if ( connection != null ) {
                connection.close();
            }
        }
        return Response.status(200).entity(result).build();
    }
}
