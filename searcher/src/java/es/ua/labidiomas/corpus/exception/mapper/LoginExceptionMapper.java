/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package es.ua.labidiomas.corpus.exception.mapper;

import es.ua.labidiomas.corpus.exception.LoginException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author paco
 */
@Provider
public class LoginExceptionMapper implements ExceptionMapper<LoginException>{

    @Override
    public Response toResponse(LoginException e) {
        return Response.status(412).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
    }
}
