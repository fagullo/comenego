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
        <script src="js/jquery-ui.js" type="text/javascript"></script>
        <title><fmt:message key="index.title" /></title>
    </head>
    <body>
        <header class="col-md-8 col-md-offset-2">
            <img src="media/img/logocomenegoportada.jpg" alt="COMENEGO" />
            <!--<div class="col-md-12"><fmt:message key="index.welcome" /></div>-->
        </header>
        <div id="content" class="col-md-10 col-md-offset-1"></div>
        <div class="col-md-4 col-md-offset-4">

            <form class="form-signin" action="/searcher/services/comenego/login" method="POST">
                <h2 class="form-signin-heading"><fmt:message key="login.sigInMsg" /></h2>
                <label for="inputName" class="sr-only"><fmt:message key="login.user" /></label>
                <input type="text" id="inputName" name="name" class="form-control" placeholder="<fmt:message key="login.user" />" required="" autofocus="">
                <label for="inputPassword" class="sr-only"><fmt:message key="login.password" /></label>
                <input type="password" id="inputPassword" name="password" class="form-control" placeholder="<fmt:message key="login.password" />" required="">
                <button class="btn btn-lg btn-primary btn-block" type="button" id="submit"><fmt:message key="login.sigIn" /></button>
            </form>
            <script>
                $("#submit").click(function() {
                    console.log("ENTRA");
                    var loginData = {
                        name: $("#inputName").val(),
                        password: $("#inputPassword").val()
                    };
                    $.ajax({
                        type: "POST",
                        url: "/searcher/services/comenego/login",
                        contentType: "application/json; charset=ISO-8859-1",
                        data: JSON.stringify(loginData),
                        success: function(data) {
                            window.location.replace("/searcher");
                        }, 
                        error : function (XMLHttpRequest, textStatus, errorThrown) {
                            $("#content").append("<div class='alert alert-danger alert-dismissible' role='alert'><button type='button'"
                                    + "class='close' data-dismiss='alert' aria-label='Close'><span aria-hidden='true'>&times;</span>"
                                    + "</button>" +  XMLHttpRequest.responseText + "</div>");
                        }
                    });
                });
            </script>
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
    </body>
</html>
