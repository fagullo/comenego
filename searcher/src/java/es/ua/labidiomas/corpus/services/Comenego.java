/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.labidiomas.corpus.services;

import es.ua.labidiomas.corpus.exception.LoginException;
import es.ua.labidiomas.corpus.searcher.LoginParameters;
import es.ua.labidiomas.corpus.searcher.SearchParameters;
import es.ua.labidiomas.corpus.util.Config;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * REST Web Service
 *
 * @author paco
 */
@Path("comenego/")
public class Comenego {

    @Context
    private ServletContext context;

    /**
     * Creates a new instance of Searcher
     */
    public Comenego() {
    }

    @POST
    @Path("login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(LoginParameters parameters, @Context HttpServletRequest request, @Context HttpServletResponse response) throws ClassNotFoundException, SQLException, URISyntaxException, LoginException {

        HttpSession session = request.getSession(false);
        Class.forName("com.mysql.jdbc.Driver");
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(Config.CONEXION_STRING, Config.DB_USER, Config.DB_PASS);
            PreparedStatement loginPS = connection.prepareStatement("SELECT * FROM user WHERE username = ? AND password = SHA2(?, 256)");
            loginPS.setString(1, parameters.getName());
            loginPS.setString(2, parameters.getPassword());
            ResultSet loginQuery = loginPS.executeQuery();
            if (loginQuery.next()) {
                session.setAttribute("userID", parameters.getName());
            } else {
                throw new LoginException("<strong>Error!</strong> Nombre de usuario o contraseña incorrectos.");
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        return Response.status(200).build();
    }

    @GET
    @Path("logout")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response logout(@Context HttpServletRequest request, @Context HttpServletResponse response) throws ClassNotFoundException, SQLException, URISyntaxException, LoginException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        HttpSession session = httpServletRequest.getSession(false);

        if (session.getAttribute("userID") != null) {
            session.removeAttribute("userID");
        }
        return Response.temporaryRedirect(new URI("/searcher")).build();
    }

    /**
     * Retrieves representation of an instance of
     * es.ua.labidiomas.corpus.services.Searcher
     *
     * @param parameters
     * @return an instance of java.lang.String
     */
    @POST
    @Path("load")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response load(SearchParameters parameters) {

//        HashMap<String, Object> result = new HashMap<String, Object>();
//        ArrayList<Object> snippets = new ArrayList<Object>();
//        if (discourses != null) {
//            Analyzer analyzer = _getAnalyzer(languages);
//            IndexSearcher indexSearcher = _prepareIndexSearcher(languages[0], sortField, searchText);
//            BooleanQuery searchQuery = _prepareQuery(searchText, discourses, analyzer, page, sortField, position, isLetter, letter, subSearchText);
//            Highlighter textHighlighter = _prepareHighlighter(searchQuery);
//
//            TopGroups tg;
//            if (sortField == null || sortField.isEmpty()) {
//                tg = _prapareResults(searchQuery, indexSearcher, page);
//            } else {
//                sortWords = new HashMap<String, String>();
//                tg = _prapareSortedResults(searchQuery, indexSearcher, page, sortField);
//            }
//            if (tg != null) {
//
//                GroupDocs[] groupedDocs = tg.groups;
//                result.put("numPages", Math.ceil(tg.totalGroupedHitCount / DOCS_BY_PAGE));
//                result.put("numDocs", tg.totalGroupedHitCount);
//                Class.forName("com.mysql.jdbc.Driver");
//                conexion = DriverManager.getConnection(Config.CONEXION_STRING, Config.DB_USER, Config.DB_PASS);
//                GroupDocs groupDoc = groupedDocs[0];
//                int pageNum = Integer.parseInt(page);
//                int offset = (pageNum - 1) * DOCS_BY_PAGE;
//                int numPageResults = pageNum * DOCS_BY_PAGE;
//                int top = Math.min(groupDoc.scoreDocs.length, numPageResults);
//                for (int i = offset; i < top; i++) {
//                    ScoreDoc sd = groupDoc.scoreDocs[i];
//                    _getSnippets(searchText.length(), snippets, sd, indexSearcher, conexion, analyzer, textHighlighter, sortField, position, letter, isLetter);
//                }
//            }
//        }
//        result.put("matches", snippets);
//        if (sortField == null || sortField.isEmpty()) {
//            result.put("sorted", "false");
//        } else {
//            result.put("sorted", "true");
//        }
//        JSONObject jObj = new JSONObject();
//        jObj.putAll(result);
//        printout.print(jObj);
//        printout.flush();
        return Response.status(200).entity(parameters).build();
    }
}
