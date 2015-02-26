<%-- 
    Document   : index
    Created on : 19-feb-2014, 18:04:31
    Author     : paco
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.request.locale}" scope="session" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="es.ua.labidiomas.corpus.i18n.text" />
<!DOCTYPE html>
<html lang="${language}">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="StyleSheet" href="css/bootstrap.min.css" type="text/css" media="screen" />
        <link rel="StyleSheet" href="css/bootstrap-theme.min.css" type="text/css" media="screen" />
        <link rel="StyleSheet" href="css/style.css" type="text/css" media="screen" />
        <script src="js/jquery-2.1.0.min.js" type="text/javascript"></script>
        <script src="js/bootstrap.min.js" type="text/javascript"></script>
        <script src="js/underscore-min.js" type="text/javascript"></script>
        <script src="js/search.js" type="text/javascript"></script>
        <script src="js/backbone-min.js" type="text/javascript"></script>
        <script src="js/pager.js" type="text/javascript"></script>
        <title><fmt:message key="index.title" /></title>
    </head>
    <body>
        <header class="col-md-8 col-md-offset-2">
            <img src="media/img/logocomenegoportada.jpg" alt="COMENEGO" />
            <!--<div class="col-md-12"><fmt:message key="index.welcome" /></div>-->
            <a href="/searcher/login" class="btn btn-success" id="login-btn"><i class="glyphicon glyphicon-user"></i>&nbsp;<fmt:message key="index.login" /></a>
        </header>
        <br/>
        <div id="content" class="col-md-10 col-md-offset-1">
            <div class="col-md-8 col-md-offset-2" style="clear: both;">
                <ul class="nav nav-tabs" data-tabs="tabs">
                    <li class="active"><a href="#monolingual" data-toggle="tab"><fmt:message key="monolingualsearch" /></a></li>
                    <li><a href="#bilingual" data-toggle="tab"><fmt:message key="bilingualsearch" /></a></li>
                </ul>
                <div class="tab-content col-md-12">
                    <div class="tab-pane active col-md-12" id="monolingual">  
                        <form action="javascript:search()" method="POST" class="well form-inline" role="form">
                            <div class="form-group col-md-8">
                                <input type="text" name="search" class="form-control" id="search" placeholder="<fmt:message key="search.text" />">
                            </div>
                            <div class="form-group">
                                <input type="submit" class="btn btn-primary" name="create" value="<fmt:message key="search" />" id="create">
                            </div>
                            <br/>
                            <div class="title"><h1><fmt:message key="languages" /></h1></div>
                            <div class="btn-group col-md-12" data-toggle="buttons">
                                <label class="btn btn-default active" onclick="$('#search-lang').val($(this).children('input').prop('value'))">
                                    <input type="radio" value="es">
                                    Castellano
                                </label>
                                <label class="btn btn-default" onclick="$('#search-lang').val($(this).children('input').prop('value'))">
                                    <input type="radio" value="en" >
                                    English
                                </label>
                                <label class="btn btn-default" onclick="$('#search-lang').val($(this).children('input').prop('value'))">
                                    <input type="radio" value="fr">
                                    Français
                                </label>
                            </div>
                            <input type="hidden" value="es" name="search-lang" id="search-lang"/>
                            <div class="title"><h1><fmt:message key="discourses" /></h1></div>
                            <label class="checkbox-inline" for="disSCI" style="padding-left: 5px !important;">
                                <fmt:message key="discourses.scientific" />
                            </label>
                            <input type="checkbox" name="discourses-selection" value="SCI" id="disSCI" checked="checked">
                            <label class="checkbox-inline" for="disCOM" style="padding-left: 5px !important;">
                                <fmt:message key="discourses.comercial" />
                            </label>
                            <input type="checkbox" name="discourses-selection" value="COM" id="disCOM" checked="checked">
                            <label class="checkbox-inline" for="disDID" style="padding-left: 5px !important;">
                                <fmt:message key="discourses.didactic" />
                            </label>
                            <input type="checkbox" name="discourses-selection" value="DID" id="disDID" checked="checked">
                            <label class="checkbox-inline" for="disLEG" style="padding-left: 5px !important;">
                                <fmt:message key="discourses.legal" />
                            </label>
                            <input type="checkbox" name="discourses-selection" value="LEG" id="disLEG" checked="checked">
                            <label class="checkbox-inline" for="disORG" style="padding-left: 5px !important;">
                                <fmt:message key="discourses.organizational" />
                            </label>
                            <input type="checkbox" name="discourses-selection" value="ORG" id="disORG" checked="checked">
                            <label class="checkbox-inline" for="disPRS" style="padding-left: 5px !important;">
                                <fmt:message key="discourses.press" />
                            </label>
                            <input type="checkbox" name="discourses-selection" value="PRS" id="disPRS" checked="checked">
                            <label class="checkbox-inline" for="disTEC" style="padding-left: 5px !important;">
                                <fmt:message key="discourses.technical" />
                            </label>
                            <input type="checkbox" name="discourses-selection" value="TEC" id="disTEC" checked="checked">
                            <div class="title"><h1><fmt:message key="orden" /></h1></div>
                            <div class="btn-group col-md-12" data-toggle="buttons">
                                <label class="btn btn-default active" onclick="$('#search-sort').val($(this).children('input').prop('value'))">
                                    <input type="radio" value="">
                                    <fmt:message key="orden.prioridad" />
                                </label>
                                <label class="btn btn-default" onclick="$('#search-sort').val($(this).children('input').prop('value'))">
                                    <input type="radio" value="before1" >
                                    <fmt:message key="orden.anterior" />
                                </label>
                                <label class="btn btn-default" onclick="$('#search-sort').val($(this).children('input').prop('value'))">
                                    <input type="radio" value="after1">
                                    <fmt:message key="orden.siguiente" />
                                </label>
                            </div>
                            <input type="hidden" value="" name="search-sort" id="search-sort"/>

                            <input type="hidden" name="searchtype" id="searchtype" value="monolingual">
                        </form>
                    </div>
                    <div class="tab-pane col-md-12 deporte-contenedor" id="bilingual">
                        <form action="javascript:search()" method="POST" class="well form-inline" role="form">
                            <div class="form-group col-md-8">
                                <input type="text" name="search" class="form-control" id="search" placeholder="<fmt:message key="search.text" />">
                            </div>
                            <div class="form-group">
                                <input type="submit" class="btn btn-primary" name="create" value="<fmt:message key="search" />" id="create">
                            </div>
                            <br/>
                            <div class="title"><h1><fmt:message key="languages" /></h1></div>
                            <div class="btn-group col-md-12" data-toggle="buttons">
                                <label class="btn btn-default active">
                                    <input type="radio" name="search-lang" value="es">
                                    Castellano
                                </label>
                                <label class="btn btn-default">
                                    <input type="radio" name="search-lang" value="en">
                                    English
                                </label>
                                <label class="btn btn-default">
                                    <input type="radio" name="search-lang" value="en">
                                    Français
                                </label>
                            </div>
                            <input type="hidden" name="searchtype" id="searchtype" value="bilingual">
                        </form>
                    </div>
                </div>
            </div>
            <div id="total-hits" style="display: none; clear: both; text-align: center;" class="col-md-12">Se han encontrado <b id="num-docs"></b> parrafo(s)</div>
            <ul id="hits" class="col-md-12" style="float: left; clear: both;">
            </ul>
            <div id='paginador' class='col-lg-12 col-md-12 col-sm-12 col-xs-12' style="float: left; clear: both;"></div>
            <div id='paginador-letras' class='col-lg-12 col-md-12 col-sm-12 col-xs-12' style="float: left; clear: both;"></div>
        </div>
        <div class="modal"></div>
        <footer class="col-md-8 col-md-offset-2">
            <div class="col-md-12">
                <img src="media/img/logo_generalitat.gif">
                <img src="media/img/logo_ua.gif">
            </div>
            2014 - 
            <a href="http://dti.ua.es/comenego">Comenego</a> - 
            <!--<a href="http://labidiomas.ua.es">Laboratorio de Idiomas</a> ---> 
            <a href="http://www.ua.es">Universidad de Alicante</a> - 
            <a href="/comenego/text/list">Admin</a>
            <br/>
            <div id="lang_selector" class="lang_selector">
                <form>
                    <div class="btn-group" data-toggle="buttons">
                        <label class="btn btn-${language == 'es' ? 'danger active' : 'default'}">
                            <input type="radio" name="language" value="es" onchange="submit()">
                            Español
                        </label>
                        <label class="btn btn-${language == 'en' ? 'danger active' : 'default'}">
                            <input type="radio" name="language" value="en" onchange="submit()">
                            English
                        </label>
                    </div>
                </form>
            </div>
        </footer>
    </body>
</html>
