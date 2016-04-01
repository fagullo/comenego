/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.labidiomas.corpus.login;

import es.ua.labidiomas.corpus.database.DataBaseHandler;
import es.ua.labidiomas.corpus.exception.LoginException;
import es.ua.labidiomas.corpus.util.Config;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST Web Service
 *
 * @author paco
 */
@Path("login/")
public class LoginHandler {

    @Context
    private UriInfo context;
    
    private final static String LOGIN_QUERY = "SELECT * FROM user WHERE username = ? AND password = SHA2(?, 256)";

    /**
     * Creates a new instance of Login
     */
    public LoginHandler() {
    }

    @POST
    @Path("login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(LoginParameters parameters, @Context HttpServletRequest request, @Context HttpServletResponse response) throws ClassNotFoundException, SQLException, URISyntaxException, LoginException {

        HttpSession session = request.getSession();
//        Class.forName("com.mysql.jdbc.Driver");
        try (Connection connection = DataBaseHandler.getConnection(); PreparedStatement loginPS = connection.prepareStatement(LOGIN_QUERY);) {
            loginPS.setString(1, parameters.getName());
            loginPS.setString(2, parameters.getPassword());
            try (ResultSet loginQuery = loginPS.executeQuery();) {
                if (loginQuery.next()) {
                    session.setAttribute("userID", parameters.getName());
                } else {
                    throw new LoginException("<strong>Error!</strong> Nombre de usuario o contraseña incorrectos.");
                }
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
        HttpSession session = httpServletRequest.getSession(false);

        if (session.getAttribute("userID") != null) {
            session.removeAttribute("userID");
        }
        return Response.temporaryRedirect(new URI("/searcher")).build();
    }

    @GET
    @Path("isLoged")
    @Produces(MediaType.TEXT_PLAIN)
    public Response isLoged(@Context HttpServletRequest request, @Context HttpServletResponse response) throws ClassNotFoundException, SQLException, URISyntaxException, LoginException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpSession session = httpServletRequest.getSession();

        if (session.getAttribute("userID") != null) {
            return Response.status(200).entity(true).build();
        }
        return Response.status(200).entity(false).build();
    }
}
