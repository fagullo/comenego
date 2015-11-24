/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.labidiomas.corpus.webservices;

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author paco
 */
@javax.ws.rs.ApplicationPath("services")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<Class<?>>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(es.ua.labidiomas.corpus.exception.mapper.LoginExceptionMapper.class);
        resources.add(es.ua.labidiomas.corpus.login.LoginHandler.class);
        resources.add(es.ua.labidiomas.corpus.services.Comenego.class);
        resources.add(es.ua.labidiomas.corpus.webservices.IndexHandler.class);
    }
    
}
