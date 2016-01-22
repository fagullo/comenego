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

        <link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
        <script src="//code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
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
        <div id="content" class="col-md-10 col-md-offset-1">
            <div class="panel panel-info">
                <div class="panel-heading"><fmt:message key="search.configuration" /></div>
                <div class="panel-body">
                    <div class="col-md-12" id="form-wrapper">
                        <form action="javascript:search()" method="POST" class="form-inline col-md-8 col-md-offset-2" role="form">
                            <div class="input-group col-md-12">
                                <div class="input-group-btn">
                                    <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                        <img src="media/img/Spain-32.png" height="18" data-value="es" id="lang-icon"/>&nbsp;&nbsp;<span class="caret"></span></button>
                                    <ul class="dropdown-menu">
                                        <li><a href="#" class="lang-selector" data-img="media/img/Spain-32.png" data-value="es">Castellano</a></li>
                                        <li><a href="#" class="lang-selector" data-img="media/img/United-Kingdom-32.png" data-value="en">English</a></li>
                                        <li><a href="#" class="lang-selector" data-img="media/img/France-32.png" data-value="fr">Français</a></li>
                                    </ul>
                                </div><!-- /btn-group -->
                                <input type="text" name="search" class="form-control" id="search" placeholder="<fmt:message key="search.text" />">
                                <span class="input-group-btn">
                                    <button type="button" class="btn btn-info" aria-label="Config" id="config-button">
                                        <i class="glyphicon glyphicon-eye-open" aria-hidden="true"></i>
                                    </button>
                                    <button class="btn btn-primary" id="create" type="submit"><fmt:message key="search" /></button>
                                </span>
                            </div>
                        </form>
                    </div>
                    <div class="col-md-8">
                        <div class="col-md-12 discourse-wrapper">
                            <div class="title col-md-2"><h1><fmt:message key="discourses" />:</h1></div>
                            <span id="tags-input" class="selectivity-input"></span>
                        </div>
                        <div class="col-md-12" style="float: left; margin-top: 20px; padding-left: 0px !important;">
                            <div class="col-md-6 discourse-wrapper" style="padding-left: 0px !important;">
                                <div class="title col-md-3" style="margin-top: -5px;"><h1><fmt:message key="orden" />:</h1></div>
                                <div class="btn-group col-md-8 col-md-offset-1" data-toggle="buttons">
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
                            <div class="col-md-5 discourse-wrapper" id="skip-grams-container" style="display: none;">
                                <div class="title col-md-6" style="margin-top: -5px;"><h1><fmt:message key="orden.skip" />: </h1></div>
                                <div class="btn-group col-md-6" data-toggle="buttons">
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
                    </div>
                    <div class="col-md-4" id="switches-container">
                        <div id="lemmatizer-container" class="col-md-12 switch-container">
                            <div class="title col-md-4 col-md-offset-3"><h1><fmt:message key="search.lemmatize" /></h1></div>
                            <div class="col-md-4">
                                <input class="switch-button" type="checkbox" name="lemmatizer-input" id="lemmatizer-input">
                            </div>
                            <div class="col-md-1 switch-info" title="Enable the switch means that terms must appears exactly as they are typed in the search bar. When the switch is disabled the terms could match only in the root."><i class="glyphicon glyphicon-info-sign"></i></div>
                            <input type="hidden" value="false" name="lemmatizer" id="lemmatizer"/>
                        </div>
                        <div id="title-container" class="col-md-12 switch-container">
                            <div class="title col-md-4 col-md-offset-3"><h1><fmt:message key="search.title" /></h1></div>
                            <div class="col-md-4">
                                <input class="switch-button" type="checkbox" name="title-input" id="title-input">
                            </div>
                            <div class="col-md-1 switch-info" title="Enable the switch means that terms must appears only in the title of the text."><i class="glyphicon glyphicon-info-sign"></i></div>
                            <input type="hidden" value="false" name="title-filter" id="title-filter"/>
                        </div>
                        <div id="order-container" class="col-md-12 switch-container">
                            <div class="title col-md-4 col-md-offset-3"><h1><fmt:message key="search.ordered" /></h1></div>
                            <div class="col-md-4">
                                <input class="switch-button" type="checkbox" name="order-input" id="order-input">
                            </div>
                            <div class="col-md-1 switch-info" title="Enable the switch means that the terms typed in the search bar must appear in the text with the same order that they appear in the bar. When the switch is disabled the order of the terms does not matters."><i class="glyphicon glyphicon-info-sign"></i></div>
                            <input type="hidden" value="false" name="order-filter" id="order-filter"/>
                        </div>
                        <div id="bilingue-container" class="col-md-12 switch-container">
                            <div class="title col-md-4 col-md-offset-3"><h1><fmt:message key="search.bilingue" /></h1></div>
                            <div class="col-md-4">
                                <input class="switch-button" type="checkbox" name="bilingue-input" id="bilingue-input">
                            </div>
                            <div class="col-md-1 switch-info" title="Enable the switch means that the search is only for text with translation in at least the two languages selected."><i class="glyphicon glyphicon-info-sign"></i></div>
                            <input type="hidden" value="false" name="bilingue-filter" id="bilingue-filter"/>
                        </div>
                        <div id="distance-container" class="col-md-12 switch-container">
                            <div class="title col-md-4 col-md-offset-3"><h1><fmt:message key="search.distance" /></h1></div>
                            <div class="col-md-4">
                                <input class="switch-button" type="checkbox" name="distance-input" id="distance-input">
                            </div>
                            <div class="col-md-1 switch-info" title="Enable the switch means that the distance between terms must be exactly the specified. When the switch is disabled the terms could appear in any of the 'n' next positions from the term before."><i class="glyphicon glyphicon-info-sign"></i></div>
                            <input type="hidden" value="false" name="distance-filter" id="distance-filter"/>
                        </div>
                    </div>
                </div>
            </div>
            <div id="total-hits" style="display: none; clear: both; text-align: center;" class="col-md-12">Se han encontrado <b id="num-docs"></b> parrafo(s)</div>
            <ul id="hits" class="col-md-12" style="float: left; clear: both;">
            </ul>
            <div id='paginador' class='col-lg-12 col-md-12 col-sm-12 col-xs-12' style="float: left; clear: both;"></div>
            <div id='paginador-letras' class='col-lg-12 col-md-12 col-sm-12 col-xs-12' style="float: left; clear: both;"></div>
        </div>

        <div id="config-dialog">
            <p class="col-md-12">Click in any node but the last to configure the search.</p>
            <div id="arrows-container" class="col-md-12" style="float: left;">
                <div id="graph-wrapper"></div>
            </div>
            <div id="search-config-body-content" class="col-md-12" style="float: left;"></div>
            <div class="col-md-4 right" id="config-footer">
                <button type="button" class="btn btn-danger" id="config-close">Close</button>
            </div>
        </div>

        <div class="modal" id="ajax-load"></div>
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
            $("#lemmatizer-input, #title-input, #bilingue-input").switchButton({
                on_label: '<fmt:message key="yes" />',
                off_label: '<fmt:message key="no" />'
            });
            $("#order-input, #distance-input").switchButton({
                on_label: '<fmt:message key="yes" />',
                off_label: '<fmt:message key="no" />',
                checked: true
            });

            $("#config-dialog").dialog({
                autoOpen: false,
                modal: true,
                title: "Configure Search Criteria",
                width: 1200,
                height: 400,
                minWidth: 1000,
                show: {
                    effect: "clip",
                    duration: 1000
                },
                hide: {
                    effect: "clip",
                    duration: 1000
                }
            });

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
            $(function () {
                $(document).tooltip();
            });
        </script>
    </body>
</html>

