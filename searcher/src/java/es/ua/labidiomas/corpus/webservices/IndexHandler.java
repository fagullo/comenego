/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.labidiomas.corpus.webservices;

import es.ua.labidiomas.corpus.index.Indexer;
import es.ua.labidiomas.corpus.index.IndexerFactory;
import es.ua.labidiomas.corpus.util.Config;
import java.io.IOException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST Web Service
 *
 * @author paco
 */
@Path("/indexhandler")
public class IndexHandler {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of TextIndexer
     */
    public IndexHandler() {
    }

    /**
     * Retrieves representation of an instance of
     * es.ua.labidiomas.corpus.webservices.TextIndexer
     *
     * @param textid
     * @param lang
     * @param token
     * @return an instance of java.lang.String
     * @throws java.io.IOException
     */
    @Path("delete/{textid}/{lang}/{token}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("textid") String textid, @PathParam("lang") String lang, @PathParam("token") String token) throws IOException {
        if (token.equals("0e6fc01af115ebcde732087613da3c44")) {
            for (int i = 1; i <= Indexer.NGRAM_SIZE; i++) {
                Indexer nGramIndexer = IndexerFactory.getInstance().getNGrammaIndexer(lang, i);
                nGramIndexer.deleteDocument(textid, lang, Config.FILE_SEPARATOR);
                nGramIndexer.commit();
                nGramIndexer.close();
            }
            Indexer indexer = IndexerFactory.getInstance().getIndexer(lang);
            indexer.deleteIndex(textid, lang, Config.FILE_SEPARATOR);
            indexer.commit();
            indexer.close();
            return Response.status(200).entity("Index deleted.").build();
        } else {
            return Response.status(200).entity("Permission not granted.").build();
        }
    }

    @Path("update/{textid}/{lang}/{token}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("textid") String textid, @PathParam("lang") String lang, @PathParam("token") String token) throws IOException {
        if (token.equals("0e6fc01af115ebcde732087613da3c44")) {
            for (int i = 1; i <= Indexer.NGRAM_SIZE; i++) {
                Indexer nGramIndexer = IndexerFactory.getInstance().getNGrammaIndexer(lang, i);
                nGramIndexer.deleteDocument(textid, lang, Config.FILE_SEPARATOR);
                nGramIndexer.commit();
                nGramIndexer.close();
            }
            Indexer indexer = IndexerFactory.getInstance().getIndexer(lang);
            indexer.deleteIndex(textid, lang, Config.FILE_SEPARATOR);
            indexer.createIndex(textid);
            indexer.commit();
            indexer.close();
            return Response.status(200).entity("Index updated.").build();
        } else {
            return Response.status(200).entity("Permission not granted.").build();
        }
    }

    @Path("create/{textid}/{lang}/{token}")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@PathParam("textid") String textid, @PathParam("lang") String lang, @PathParam("token") String token) throws IOException {
        if (token.equals("0e6fc01af115ebcde732087613da3c44")) {
            Indexer indexer = IndexerFactory.getInstance().getIndexer(lang);
            indexer.createIndex(textid);
            indexer.commit();
            indexer.close();

            return Response.status(200).entity("Index created.").build();
        } else {
            return Response.status(200).entity("Permission not granted.").build();
        }
    }
}
