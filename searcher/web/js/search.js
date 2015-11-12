var lastSearch = null;

$(document).on({
    ajaxStart: function () {
        $("#pleaseWaitDialog").modal();
        $("body").addClass("loading");
    },
    ajaxStop: function () {
        $("body").removeClass("loading");
    }
});

$(document).ready(function () {

//    var data = JSON.stringify({
//        "textID" : "393"
//    });
//    $.ajax({
//        type: "POST",
//        url: "/searcher/webresources/textindexer/create",
//        contentType: 'application/json',
//        dataType: 'text',
//        data : data
//    }, function (data) {
//        console.log(data);
//    });

    $("#subsearch").click(function () {

        if (!$("#subsearch").hasClass("disabled")) {
            $("#subsearch").addClass("disabled");
            search(1, "null", true);
        }
    });

    $(".order").click(function () {
        $('#search-sort').val($(this).children('input').prop('value'));
        $(".order").removeClass("active");
        $(this).addClass("active");
        if ($(this).children('input').prop('value') === "") {
            $('#skip-grams-container').hide();
        } else {
            $('#skip-grams-container').show();
        }
        return false; //Prevent default action.
    });

    $(".lang-btn").click(function () {
        $('#search-lang').val($(this).children('input').prop('value'));
        $(".lang-btn").removeClass("active");
        $(this).addClass("active");
        return false; //Prevent default action.
    });

    $(".skipg").click(function () {
        $('#skip-grams').val($(this).children('input').prop('value'));
        $(".skipg").removeClass("active");
        $(this).addClass("active");
        return false; //Prevent default action.
    });
});

function search(page, letter, subsearch) {
    if (!page) {
        page = 1;
    }
    if (!letter) {
        letter = "null";
    }
    if (!subsearch) {
        if ($("#subsearch").hasClass("disabled")) {
            $("#subsearch").removeClass("disabled");
        }
    }
    var discourses = "";
    var sorted;
    $("input[name='discourses-selection']:checked").each(function () {
        discourses += $(this).val() + " ";
    });

    var data;
    if (subsearch && lastSearch != null) {
        data = "search=" + lastSearch + "&languages=" + $('#search-lang').val() + "&discourses=" + discourses
                + "&page=" + page + "&letter=" + letter + "&sortField=" + $('#search-sort').val() + "&position=" + $('#skip-grams').val() + "&subsearch=" + $("#search").val();
    } else {
        data = "search=" + $("#search").val() + "&languages=" + $('#search-lang').val() + "&discourses=" + discourses
                + "&page=" + page + "&letter=" + letter + "&sortField=" + $('#search-sort').val() + "&position=" + $('#skip-grams').val() + "&subsearch=null";
        lastSearch = $("#search").val();
    }
    $.ajax({
        type: "POST",
        url: "/searcher/search",
        data: data,
        success: function (searchresult) {
            console.log(searchrsult);
            if (!searchresult.matches || searchresult.matches.length === 0) {
                $("#paginador").html("");
                $("#hits").html(
                        "<div class='alert alert-warning alert-dismissable'>"
                        + "<button type='button' class='close' data-dismiss='alert' aria-hidden='true'>&times;</button>"
                        + "No se ha encontrado ningún resultado.</div>");
                $("#num-docs").html(0);
            } else {
                var matches = searchresult.matches;
                sorted = searchresult.sorted;
                $("#total-hits").show();
                $("#num-docs").html(searchresult.numDocs);
                var numPages = searchresult.numPages;
                $("#hits").empty();
                for (var i = 0; i < matches.length; i++) {
                    var resultObj = matches[i];
                    var link = resultObj.url;
                    var discourses = resultObj.discourses;
                    var snippet = resultObj.snippet.trim();
                    var matchPosition = snippet.indexOf("</b>");
                    var spaces = "";
                    var targetPosition = 80;
                    if (matchPosition > targetPosition) {
                        snippet = snippet.substr(matchPosition - (targetPosition - 1));
                    } else {
                        for (var j = matchPosition; j < (targetPosition - 1); j++) {
                            spaces += "&nbsp;";
                        }
                    }
                    var listItem = "<li class='snippet' style='padding: 0 !important;'>"
                            + "<div class='col-lg-10 col-md-10 col-sm-10 col-xs-10' style='padding: 0 !important;'>" + spaces + snippet + "</div>"
                            + "<div class='col-lg-2 col-md-2 col-sm-2 col-xs-2' style='text-align: right; padding: 0 !important;'>"
                            + "<a href='" + link + "' target='_blank'><div class='glyphicon glyphicon-link'></div></a> " + discourses + "</div>"
                            + "</li>";
//                $("#hits").append("<li class='snippet'>" + spaces + snippet + "</li>");
                    $("#hits").append(listItem);
                }
                getPaginator(parseInt(page), numPages, letter, $("#search").val(), sorted);
            }
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            window.location.href = "login.jsp";
        }
    });
}

function getPaginator(current, total, letter, text, sorted) {
    var paginator = "";
    var url = "#";
    url += "search/" + text + "/page/";
    if (total > 20) {
        paginator = getLongPaginator(current, total, letter, url);
    } else {
        paginator = getSmallPaginator(current, total, letter, url);
    }
    $("#paginador").html(paginator);
    if (sorted == "true") {
        $("#paginador-letras").html(paginatorLetras(url + "1/letter/"));
    }
}

function paginatorLetras(url) {
    var paginator = "<ul class='pager col-lg-12'>";
    paginator += "<li><a href='" + url + "a'>a</a></li>";
    paginator += "<li><a href='" + url + "b'>b</a></li>";
    paginator += "<li><a href='" + url + "c'>c</a></li>";
    paginator += "<li><a href='" + url + "d'>d</a></li>";
    paginator += "<li><a href='" + url + "e'>e</a></li>";
    paginator += "<li><a href='" + url + "f'>f</a></li>";
    paginator += "<li><a href='" + url + "g'>g</a></li>";
    paginator += "<li><a href='" + url + "h'>h</a></li>";
    paginator += "<li><a href='" + url + "i'>i</a></li>";
    paginator += "<li><a href='" + url + "j'>j</a></li>";
    paginator += "<li><a href='" + url + "k'>k</a></li>";
    paginator += "<li><a href='" + url + "l'>l</a></li>";
    paginator += "<li><a href='" + url + "m'>m</a></li>";
    paginator += "<li><a href='" + url + "n'>n</a></li>";
    paginator += "<li><a href='" + url + "o'>o</a></li>";
    paginator += "<li><a href='" + url + "p'>p</a></li>";
    paginator += "<li><a href='" + url + "q'>q</a></li>";
    paginator += "<li><a href='" + url + "r'>r</a></li>";
    paginator += "<li><a href='" + url + "s'>s</a></li>";
    paginator += "<li><a href='" + url + "t'>t</a></li>";
    paginator += "<li><a href='" + url + "u'>u</a></li>";
    paginator += "<li><a href='" + url + "v'>v</a></li>";
    paginator += "<li><a href='" + url + "w'>w</a></li>";
    paginator += "<li><a href='" + url + "x'>x</a></li>";
    paginator += "<li><a href='" + url + "y'>y</a></li>";
    paginator += "<li><a href='" + url + "z'>z</a></li>";
    paginator += " </ul>";

    return paginator;
}

function getNumPagesByRange(lower, top, current, letter, url) {
    var paginator = "";
    for (var i = lower; i <= top; i++) {
        if (i === current) {
            paginator += "<li class='disabled'><a style='background-color:yellow;'>" + i + "</a></li>";
        } else {
            paginator += "<li><a href='" + url + i + "/letter/" + letter + "'>" + i + "</a></li>";
        }
    }
    return paginator;
}

function getLongPaginator(current, total, letter, url) {
    var paginator = "";
    paginator = "<ul class='pager col-lg-12'>";
    if (current > 1) {
        paginator += "<li><a href='" + url + (current - 1) + "/letter/" + letter + "'>&laquo;</a></li>";
    }
    paginator += getNumPagesByRange(1, 5, current, letter, url) + "&nbsp;...&nbsp;";
    if ((current + 4) < total) {
        if (current > 5) {
            var lower = current - 5;
            lower = lower > 3 ? (current - 3) : (current - lower + 1);
            var top = current + 3;
            top = top < (total - 5) ? (current + 3) : (total - 5);
            paginator += getNumPagesByRange(lower, top, current, letter, url) + "&nbsp;...&nbsp;";
        }
        paginator += getNumPagesByRange(total - 4, total, current, letter, url);
    } else {
        paginator += getNumPagesByRange(current, total, current, letter, url);
    }
    if (current !== total) {
        paginator += "<li><a href='" + url + (parseInt(current) + 1) + "/letter/" + letter + "'>&raquo;</a></li>";
    }
    paginator += "</ul>";
    return paginator;
}

function getSmallPaginator(current, total, letter, url) {
    var paginator = "";
    if (total > 1) {
        paginator = "<ul class='pager col-lg-12'>";
        if (current > 1) {
            paginator += "<li><a href='" + url + (current - 1) + "/letter/" + letter + "'>&laquo;</a></li>";
        }
        paginator += getNumPagesByRange(1, total, current, letter, url);
        if (current !== total) {
            paginator += "<li><a href='" + url + (parseInt(current) + 1) + "/letter/" + letter + "'>&raquo;</a></li>";
        }
        paginator += "</ul>";
    }
    return paginator;
}