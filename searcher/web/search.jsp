<%-- 
    Document   : search
    Created on : 14-dic-2015, 19:34:59
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
        <link rel="StyleSheet" href="css/jquery.switchButton.css" type="text/css" media="screen" />
        <link rel="StyleSheet" href="css/selectivity-full.min.css" type="text/css" media="screen" />
        <link rel="stylesheet" href="//netdna.bootstrapcdn.com/font-awesome/4.2.0/css/font-awesome.min.css"> 
        <link rel="stylesheet" href="css/arrowsandboxes.css" type="text/css" />
        <!--<link rel="StyleSheet" href="css/jquery.mobile-1.4.5.min.css" type="text/css" media="screen" />-->
        <script src="js/jquery-2.1.0.min.js" type="text/javascript"></script>
        <script src="js/bootstrap.min.js" type="text/javascript"></script>
        <script src="js/underscore-min.js" type="text/javascript"></script>
        <script src="js/searcher.js" type="text/javascript"></script>
        <script src="js/backbone-min.js" type="text/javascript"></script>
        <script src="js/pager.js" type="text/javascript"></script>
        <script src="js/jquery-ui.js" type="text/javascript"></script>
        <script src="js/jquery.switchButton.js" type="text/javascript"></script>
        <script src="js/selectivity-full.min.js" type="text/javascript"></script> 
        <script src="http://www.headjump.de/javascripts/jquery_wz_jsgraphics.js" type="text/javascript"></script>
        <script src="js/arrowsandboxes.js" type="text/javascript"></script>
        <script src="js/mustache.js" type="text/javascript"></script>
        <!--<script src="js/jquery.mobile-1.4.5.min.js" type="text/javascript"></script>-->
        <title><fmt:message key="index.title" /></title>
    </head>
    <body>
        <header class="col-md-8 col-md-offset-2">
            <img src="media/img/logocomenegoportada.jpg" alt="COMENEGO" />
            <!--<div class="col-md-12"><fmt:message key="index.welcome" /></div>-->
            <%
                if (session.getAttribute("userID") == null) {
            %>
            <a href="login.jsp" class="btn btn-success" id="login-btn"><i class="glyphicon glyphicon-user"></i>&nbsp;<fmt:message key="index.login" /></a>
            <%
            } else {
            %>
            <a href="/searcher/services/login/logout" class="btn btn-danger" id="login-btn"><i class="glyphicon glyphicon-user"></i>&nbsp;<fmt:message key="index.logout" /></a>
            <%
                }
            %>
        </header>
        <div id="content" class="col-md-12">
            <div class="panel panel-info">
                <div class="panel-heading"><fmt:message key="search.configuration" /></div>
                <div class="panel-body">
                    <form action="javascript:search()" method="POST" class="form-inline col-md-12" role="form">
                        <div class="col-md-12" style="float: left;">
                            <div class="col-md-10" style="float: left; margin-bottom: 5px;">
                                <div class="col-md-10">
                                    <input type="text" name="search" class="form-control" id="search" placeholder="<fmt:message key="search.text" />">
                                </div>
                                <button type="button" class="btn btn-warning" aria-label="Config" id="config-button" disabled="">
                                    <span class="glyphicon glyphicon-wrench" aria-hidden="true"></span>
                                </button>
                            </div>
                            <div class="col-md-1" style="text-align: right;">
                                <div class="form-group">
                                    <input type="submit" class="btn btn-primary left" name="create" value="<fmt:message key="search" />" id="create">
                                </div>
                            </div>
                        </div>
                        <div id="arrows-container" class="col-md-12" style="float: left;">
                            <div id="graph-wrapper">
                            </div>
                        </div>
                        <div id="lang-container" class="col-md-12">
                            <div class="col-md-4">
                                <div id="lemmatizer-container" class="col-md-12">
                                    <div class="title"><h1><fmt:message key="search.lemmatize" /></h1></div>
                                    <input type="checkbox" name="lemmatizer-input" id="lemmatizer-input">
                                    <input type="hidden" value="false" name="lemmatizer" id="lemmatizer"/>
                                </div>
                            </div>
                            <div class="col-md-4">
                                <div id="title-container" class="col-md-12">
                                    <div class="title"><h1><fmt:message key="search.title" /></h1></div>
                                    <input type="checkbox" name="title-input" id="title-input">
                                    <input type="hidden" value="false" name="title-filter" id="title-filter"/>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-12">
                            <div class="col-md-5">
                                <div class="title"><h1><fmt:message key="languages" /></h1></div>
                                <div class="btn-group col-md-12" data-toggle="buttons">
                                    <label class="btn btn-primary active lang-btn">
                                        <input type="radio" value="es">
                                        Castellano <img src="media/img/Spain-32.png" />
                                    </label>
                                    <label class="btn btn-primary lang-btn">
                                        <input type="radio" value="en" >
                                        English <img src="media/img/United-Kingdom-32.png" />
                                    </label>
                                    <label class="btn btn-primary lang-btn">
                                        <input type="radio" value="fr">
                                        Français <img src="media/img/France-32.png" />
                                    </label>
                                </div>
                            </div>
                            <input type="hidden" value="es" name="search-lang" id="search-lang"/>
                            <div class="col-md-7">
                                <div class="title"><h1><fmt:message key="discourses" /></h1></div>
                                <span id="tags-input" class="selectivity-input"></span>
                                <script>
                                    var inputItems = [
                                        '<fmt:message key="discourses.scientific" />',
                                        '<fmt:message key="discourses.comercial" />',
                                        '<fmt:message key="discourses.didactic" />',
                                        '<fmt:message key="discourses.legal" />',
                                        '<fmt:message key="discourses.organizational" />',
                                        '<fmt:message key="discourses.press" />',
                                        '<fmt:message key="discourses.technical" />'];
                                    $('#tags-input').selectivity({
                                        items: inputItems,
                                        multiple: true,
                                        tokenSeparators: [' '],
                                        value: [
                                            '<fmt:message key="discourses.scientific" />',
                                            '<fmt:message key="discourses.comercial" />',
                                            '<fmt:message key="discourses.didactic" />',
                                            '<fmt:message key="discourses.legal" />',
                                            '<fmt:message key="discourses.organizational" />',
                                            '<fmt:message key="discourses.press" />',
                                            '<fmt:message key="discourses.technical" />'
                                        ],
                                        backspaceHighlightsBeforeDelete: true,
                                    });
                                </script> 
                            </div>
                        </div>
                        <div class="col-md-12" style="padding-bottom: 10px; float: left;">
                            <div class="col-md-12" >
                                <div class="title"><h1><fmt:message key="orden" /></h1></div>
                                <div class="btn-group col-md-12" data-toggle="buttons">
                                    <label class="btn btn-primary active order">
                                        <input type="radio" value="">
                                        <fmt:message key="orden.prioridad" />
                                    </label>
                                    <label class="btn btn-primary order">
                                        <input type="radio" value="before" >
                                        <fmt:message key="orden.anterior" />
                                    </label>
                                    <label class="btn btn-primary order">
                                        <input type="radio" value="after">
                                        <fmt:message key="orden.siguiente" />
                                    </label>
                                </div>
                            </div>
                            <div class="col-md-6" id="skip-grams-container" style="display: none;">

                                <div class="title"><h1><fmt:message key="orden.skip" /></h1></div>
                                <div class="btn-group col-md-12" data-toggle="buttons">
                                    <label class="btn btn-primary active skipg">
                                        <input type="radio" value="1">1
                                    </label>
                                    <label class="btn btn-primary skipg">
                                        <input type="radio" value="2" >2
                                    </label>
                                    <label class="btn btn-primary skipg">
                                        <input type="radio" value="3">3
                                    </label>
                                    <label class="btn btn-primary skipg">
                                        <input type="radio" value="4">4
                                    </label>
                                </div>
                                <input type="hidden" value="" name="search-sort" id="search-sort"/>
                                <input type="hidden" value="1" name="skip-grams" id="skip-grams"/>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
            <div id="total-hits" style="display: none; clear: both; text-align: center;" class="col-md-12">Se han encontrado <b id="num-docs"></b> parrafo(s)</div>
            <ul id="hits" class="col-md-12" style="float: left; clear: both;">
            </ul>
            <div id='paginador' class='col-lg-12 col-md-12 col-sm-12 col-xs-12' style="float: left; clear: both;"></div>
            <div id='paginador-letras' class='col-lg-12 col-md-12 col-sm-12 col-xs-12' style="float: left; clear: both;"></div>
        </div>
        <div class="modal" id="ajax-load"></div>

        <div class="modal fade" id="search-config-modal" tabindex="-1" role="dialog" aria-labelledby="searchConfig">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header" id="search-config-header">
                        <button type="button"id="search-config-close" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="searchConfig">Configure Search Criteria</h4>
                    </div>
                    <div class="modal-body" id="search-config-body">
                        <div id="search-config-body-content">
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-danger" data-dismiss="modal">Close</button>
                            <button type="button" class="btn btn-success" id="modal-save">Save changes</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

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
        <script>
            $("#lemmatizer-input, #title-input").switchButton({
                on_label: '<fmt:message key="yes" />',
                off_label: '<fmt:message key="no" />'
            });
        </script>
    </body>
</html>

