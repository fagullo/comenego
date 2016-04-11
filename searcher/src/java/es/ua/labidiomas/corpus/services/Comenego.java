/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.labidiomas.corpus.services;

import es.ua.labidiomas.corpus.database.DataBaseHandler;
import es.ua.labidiomas.corpus.exception.LoginException;
import es.ua.labidiomas.corpus.searcher.LuceneSnippet;
import es.ua.labidiomas.corpus.searcher.NGramsComparator;
import es.ua.labidiomas.corpus.searcher.search.SearchConfiguration;
import es.ua.labidiomas.corpus.searcher.search.SearchParameters;
import es.ua.labidiomas.corpus.searcher.SearchResponse;
import es.ua.labidiomas.corpus.searcher.Searcher;
import es.ua.labidiomas.corpus.util.Config;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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

    private static final int DOCS_BY_PAGE = 50;

    @Context
    private ServletContext context;

    /**
     * Creates a new instance of Searcher
     */
    public Comenego() {

    }

    @POST
    @Path("search")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response search(SearchConfiguration parameters, @Context HttpServletRequest request) throws IOException, ParseException, ClassNotFoundException, SQLException, InvalidTokenOffsetsException, LoginException {
        if (!isLoggedIn(request.getSession())) {
            throw new LoginException();
        }

        SearchResponse result = new SearchResponse();
        List<LuceneSnippet> snippets = new ArrayList<>();
        try (Connection connection = DataBaseHandler.getConnection();) {
            Searcher searcher = new Searcher(parameters, connection);
            if (parameters.getDiscourses() != null) {
                BooleanQuery searchQuery = searcher.prepareQuery(parameters);
                TopGroups tg;
                tg = searcher.prapareResults(searchQuery, parameters.getPage());
                if (tg != null) {

                    GroupDocs[] groupedDocs = tg.groups;
                    result.setNumPages((int) Math.ceil(tg.totalGroupedHitCount / DOCS_BY_PAGE));
                    result.setNumDocs(tg.totalGroupedHitCount);
                    for (GroupDocs groupDoc : groupedDocs) {
                        int offset = (parameters.getPage() - 1) * DOCS_BY_PAGE;
                        int numPageResults = parameters.getPage() * DOCS_BY_PAGE;
                        int top = Math.min(groupDoc.scoreDocs.length, numPageResults);
                        for (int i = offset; i < top; i++) {
                            ScoreDoc sd = groupDoc.scoreDocs[i];
                            snippets.addAll(searcher.getSnippets(sd.doc, parameters.getOptions().isTitle(), parameters.getOptions().isBilingual()));
                        }
                    }
                }
            }
            if (parameters.getSort().getField() != null && !parameters.getSort().getField().isEmpty()) {
                Collections.sort(snippets, new NGramsComparator(parameters.getSort().getField(), parameters.getSort().getPosition(), new HashMap<String, String>()));
                for (LuceneSnippet sn : snippets) {
                    searcher.setSnippet(sn, parameters.getSort().getField(), parameters.getSort().getPosition());
                }
            }
            result.setMatches(snippets);
            if (parameters.getSort().getField() == null || parameters.getSort().getField().isEmpty()) {
                result.setSorted(false);
            } else {
                result.setSorted(true);
            }
        }
        return Response.status(200).entity(result).build();
    }

    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("userID") != null;
    }
}
