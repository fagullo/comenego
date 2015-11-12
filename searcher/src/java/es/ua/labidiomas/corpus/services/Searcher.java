/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.labidiomas.corpus.services;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * REST Web Service
 *
 * @author paco
 */
@Path("searcher/")
public class Searcher {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of Searcher
     */
    public Searcher() {
    }

    /**
     * Retrieves representation of an instance of es.ua.labidiomas.corpus.services.Searcher
     * @return an instance of java.lang.String
     */
    @GET
    @Path("load")
    @Produces(MediaType.TEXT_PLAIN)
    public String load() {
        return "Hola mundo";
    }

    /**
     * PUT method for updating or creating an instance of Searcher
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Consumes("application/xml")
    public void putXml(String content) {
    }
}
