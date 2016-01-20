var lastSearch = null;

var discoursesMap = ['SCI', 'COM', 'DID', 'LEG', 'ORG', 'PRS', 'TEC'];
var discoursesSelected = ['SCI', 'COM', 'DID', 'LEG', 'ORG', 'PRS', 'TEC'];
var graphSpecialCharacters = [">", "<", "-", ":", "(", ")", "[", "]", "|"];
var overflow = false;
var maxNodes = 0;
var searchNodes = [];
var recalculateNodes = true;


(function ($) {
    $.fn.hasScrollBar = function () {
        return this.get(0).scrollWidth > this.width();
    }
})(jQuery);


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

    $('#tags-input').on("change", function (event) {
        if (event.added) {
            var index = $.inArray(event.added.id, inputItems);
            if (index < 0) {
                var discursesSelected = $(".selectivity-multiple-selected-item");
                var removeIndex = event.value.length - 1;
                discursesSelected[removeIndex].remove();
            } else {
                var discourseIndex = $.inArray(event.added.id, inputItems);
                discoursesSelected.push(discoursesMap[discourseIndex]);
            }
        } else if (event.removed) {
            var discourseIndex = $.inArray(event.removed.id, inputItems);
            var index = discoursesSelected.indexOf(discoursesMap[discourseIndex]);
            if (index >= 0) {
                discoursesSelected.splice(index, 1);
            }
        }
    });

    $("#lemmatizer-input").change(function () {
        if ($("#lemmatizer").val() === "false") {
            $("#lemmatizer").val("true");
        } else {
            $("#lemmatizer").val("false");
        }
    });

    $("#title-input").change(function () {
        if ($("#title-filter").val() === "false") {
            $("#title-filter").val("true");
        } else {
            $("#title-filter").val("false");
        }
    });

    $("#subsearch").click(function () {

        if (!$("#subsearch").hasClass("disabled")) {
            $("#subsearch").addClass("disabled");
            search(1, "null", true);
            lastSearch = null;
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

    $('#search').keyup(function (a) {
        var content = $('#search').val().trim().split(" ");
        recalculateNodes = true;
        fillGraph();
        if (!overflow) {
            if (!$(".arrowsandboxes-wrapper").hasScrollBar()) {
                maxNodes = content.length;
            } else {
                overflow = true;
                fillGraph();
            }
        }
    });

    $("#config-button").click(function () {
        var views = getModalViews();
        var view = "";
        for (var i = 0; i < views.length; i++) {
            view += loadView(views[i]);
        }

        $("#search-config-body-content").html(view);
        $("#search-config-modal").modal();
    });

    $("#modal-save").click(function () {
        var dd = $(".distance-display");

        for (var i = 0; i < dd.length; i++) {
            searchNodes[i].distance = parseInt($("#distance-" + i).html());
        }
        for (var i = 0; i < searchNodes.length; i++) {
            searchNodes[i].isMain = $("#modalWord-" + i).hasClass("word-wrapper-modal-main");
        }

        fillGraph();
        $('#search-config-modal').modal('toggle');
    });

    $(document).on("click", ".addDistance", function () {
        var wordID = $(this).attr("id");
        wordID = wordID.substring(wordID.indexOf("-") + 1);
        var distanceValue = parseInt($("#distance-" + wordID).html());
        $("#distance-" + wordID).html(distanceValue + 1);
    });

    $(document).on("click", ".subDistance", function () {
        var wordID = $(this).attr("id");
        wordID = wordID.substring(wordID.indexOf("-") + 1);
        var distanceValue = parseInt($("#distance-" + wordID).html());
        if (distanceValue > 0) {
            $("#distance-" + wordID).html(distanceValue - 1);
        }
    });

    $(document).on("click", ".selectable-word", function () {
        var wordID = $(this).attr("id");
        if (wordID && wordID.substring(0, 9) === "modalWord") {
            $(".word-wrapper-modal").removeClass("word-wrapper-modal-main");
            $(this).addClass("word-wrapper-modal-main");
        }
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

    var data = {
        languages: $('#search-lang').val(),
        discourses: discourses,
        page: page,
        letter: letter,
        sortField: $('#search-sort').val(),
        position: $('#skip-grams').val(),
        lemma: $("#lemmatizer").val(),
        title: $("#title-filter").val()
    };
    if (subsearch && lastSearch != null) {
        data.search = $("#search").val();
        data.subsearch = lastSearch;
    } else {
        data.search = $("#search").val();
        data.subsearch = null;
        data.lastSearch = $("#search").val();
    }

    $.ajax({
        type: "POST",
        url: "/searcher/services/comenego/load",
        contentType: "application/json; charset=ISO-8859-1",
        data: JSON.stringify(data),
        success: function (searchresult) {
            lastSearch = {
                text: $("#search").val(),
            };
            if ($("#title-filter").val() === "false") {
                lastSearch.field = "text";
            } else {
                lastSearch.field = "title";
            }

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
            if (XMLHttpRequest.status === 401) {
                window.location.href = "login.jsp";
            }
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

function fillGraph() {
    calculateSearchNodes();
    var size = searchNodes.length;
    var nodes = "";
    for (var i = 0; i < size; i++) {
        var node = "";
        var text = parseText(searchNodes[i].word);
        if (text.length > 0) {
            node += "n" + i + ":" + text;
            if (i !== size - 1) {
                if (i > 0 && (i + 1) % maxNodes === 0 && overflow) {
                    if (searchNodes[i].isMain) {
                        nodes += "((" + node + ">" + searchNodes[i].distance + " [n" + (i + 1) + "] )) || ";
                    } else {
                        nodes += "(" + node + ">" + searchNodes[i].distance + "[n" + (i + 1) + "] ) || ";
                    }
                } else {
                    if (searchNodes[i].isMain) {
                        nodes += "((" + node + ")) >" + searchNodes[i].distance + " ";
                    } else {
                        nodes += "(" + node + ") >" + searchNodes[i].distance + " ";
                    }
                }
            } else {
                if (searchNodes[i].isMain) {
                    nodes += "((" + node + "))";
                } else {
                    nodes += "(" + node + ")";
                }
            }
        }
    }
    if (size > 1) {
        $("#config-button").removeAttr("disabled");
    } else if (!$("#config-button").attr("disabled")) {
        $("#config-button").attr("disabled", "");
    }
    showGraph(nodes);
}

function parseText(content) {
    var text = content;
    for (var j = 0; j < graphSpecialCharacters.length; j++) {
        text = text.replace(graphSpecialCharacters[j], "{{" + graphSpecialCharacters[j] + "}}");
    }
    return text;
}

function showGraph(content) {
    $("#arrows-container").empty();
    $("#arrows-container").append("<pre id='arrows' class='arrows-and-boxes'>" + content + "</pre>");
    $("#arrows").arrows_and_boxes();
}


var wordModalTemplate = "<div class='col-md-12 word-group-wrapper' style='text-align: center;'>"
        + "<div class='{{wordClass}} selectable-word col-md-4' id='modalWord-{{wordID}}'>{{word1}}</div>"
        + "<div class='distance-wrapper-modal col-md-4'>"
        + "<div class='btn-group' role='group' aria-label='btn-group-1'>"
        + "<button type='button' class='btn btn-warning subDistance' id='subDistance-{{wordID}}'><i class='glyphicon glyphicon-chevron-left'></i></button>"
        + "<div class='btn btn-primary disabled distance-display' id='distance-{{wordID}}' >{{distance}}</div>"
        + "<button type='button' class='btn btn-warning addDistance' id='addDistance-{{wordID}}'><i class='glyphicon glyphicon-chevron-right'></i></button>"
        + "</div>"
        + "</div>"
        + "<div class='word-wrapper-modal col-md-4 {{selectable}}' id='{{rightID}}'>{{word2}}</div>"
        + "</div>";

function loadView(view) {
    return Mustache.render(wordModalTemplate, view);
}

function calculateSearchNodes() {
    if (recalculateNodes) {
        var content = $('#search').val().trim().split(" ");
        searchNodes = [];
        for (var i = 0; i < content.length; i++) {
            var node = {
                word: content[i],
                distance: 0,
                isMain: false
            };
            if (i === 0) {
                node.isMain = true;
            }
            searchNodes.push(node);
        }
        recalculateNodes = false;
    }
}

function getModalViews() {
    calculateSearchNodes();
    var views = [];

    for (var i = 0; i < searchNodes.length; i++) {
        if (i !== (searchNodes.length - 1)) {
            var view = {
                word1: searchNodes[i].word,
                word2: searchNodes[i + 1].word,
                distance: searchNodes[i].distance,
                wordID: i,
                selectable: "",
                rightID: "dummy-" + i
            };
            if (searchNodes[i].isMain) {
                view.wordClass = "word-wrapper-modal word-wrapper-modal-main";
            } else {
                view.wordClass = "word-wrapper-modal";
            }
            if (searchNodes.length > 1 && i === (searchNodes.length - 2)) {
                view.selectable = "selectable-word";
                view.rightID = "modalWord-" + (i + 1);
            }
            views.push(view);
        }
    }

    return views;
}