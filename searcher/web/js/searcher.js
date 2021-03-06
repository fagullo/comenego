var lastSearch = null;

var discoursesMap = ['SCI', 'COM', 'DID', 'LEG', 'ORG', 'PRS', 'TEC'];
var discoursesSelected = ['SCI', 'COM', 'DID', 'LEG', 'ORG', 'PRS', 'TEC'];
var graphSpecialCharacters = [">", "<", "-", ":", "(", ")", "[", "]", "|"];
var maxNodes = 5;
var searchNodes = [];
var currentTargetNode = -1;


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

//    var rowWidth = $("#tr1").css("width");
//    rowWidth = rowWidth.substring(0, rowWidth.length - 2);
//    var textWidth = $("#Test").css("width");
//    textWidth = parseInt(textWidth.substring(0, textWidth.length - 2)) + 6;
//    $(".td2").attr("width", textWidth + "px");
//    var total = (rowWidth - textWidth) / 2;
//    console.log("Total -> " + total);
//    $(".td1").attr("width", total + "px");
//    $(".td3").attr("width", total + "px");

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

    $(".skipg").click(function () {
        $('#skip-grams').val($(this).children('input').prop('value'));
        $(".skipg").removeClass("active");
        $(this).addClass("active");
        return false; //Prevent default action.
    });

    $("#config-button").click(function () {
        calculateSearchNodes();
        fillGraph();
        if (searchNodes.length > maxNodes * 3) {
            $("#config-footer").css("bottom", "auto");
        } else {
            $("#config-footer").css("bottom", "10px");
        }
        $("#search-config-body-content").html("");
        $("#config-dialog").dialog("open");
    });

    $("#config-close").click(function () {
        $("#config-dialog").dialog("close");
    });

    $(".lang-selector").click(function () {
        $("#lang-icon").attr("src", $(this).attr("data-img"));
        $("#lang-icon").attr("data-value", $(this).attr("data-value"));
    });

    $(document).on("click", ".addDistance", function () {
        var distanceValue = parseInt($("#word-distance").html());
        $("#word-distance").html(distanceValue + 1);
        searchNodes[currentTargetNode].distance = distanceValue + 1;
        fillGraph();
    });

    $(document).on("click", ".subDistance", function () {
        var distanceValue = parseInt($("#word-distance").html()) - 1;
        if (distanceValue > -1) {
            $("#word-distance").html(distanceValue);
            searchNodes[currentTargetNode].distance = distanceValue;
        }
        fillGraph();
    });

    $(document).on("click", ".displayable-term", function () {
        $(".displayable-term-main").removeClass("displayable-term-main");
        $(this).addClass("displayable-term-main");
        var wordID = $(this).attr("id");
        var wordIndex = parseInt(wordID.substring(wordID.indexOf("-") + 1));
        for (var i = 0; i < searchNodes.length; i++) {
            if (wordIndex === i) {
                searchNodes[i].isMain = true;
            } else {
                searchNodes[i].isMain = false;
            }
        }
        fillGraph();
    });

    $(document).on("click", ".arrowsandboxes-node", function () {
        var arrows = $(".arrowsandboxes-node");
        var labels = $(".arrowsandboxes-label");

        for (var i = 0; i < arrows.length; i++) {

            if (arrows[i] === this && labels[i]) {
                currentTargetNode = i;
                $("#search-config-body-content").html(loadView({
                    word1: $($(arrows[i]).children()[0]).html(),
                    distance: $(labels[i]).html(),
                    word2: $($(arrows[i + 1]).children()[0]).html(),
                    word1ID: i,
                    word2ID: (i + 1)
                }));
                break;
            }
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
    var discourses = [];
    var sorted;
    $("input[name='discourses-selection']:checked").each(function () {
        discourses.push($(this).val());
    });

    calculateSearchNodes();

    var data = {
        searchNodes: searchNodes,
        discourses: discoursesSelected,
        language: $("#lang-icon").attr("data-value"),
        page: page,
        letter: null,
        lematize: $("#lemmatizer").val(),
        title: $("#title-filter").val(),
        order: false,
        distance: false,
        sortField: $('#search-sort').val(),
        sortPosition: $('#skip-grams').val()
    };

    console.log(searchNodes);
    console.log(discoursesSelected);
    console.log($("#lang-icon").attr("data-value"));
    console.log(page);
    console.log(letter);

    $.ajax({
        type: "POST",
        url: "/searcher/services/comenego/search",
        headers: {
            Accept: "application/json; charset=utf-8",
            "Content-Type": "application/json; charset=utf-8"
        },
//        contentType: "application/json; charset=ISO-8859-1",
        data: JSON.stringify(data),
        success: function (searchresult) {
            console.log(searchresult.length);
            lastSearch = {
                text: $("#search").val()
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
    if (sorted === "true") {
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
    var size = searchNodes.length;
    var nodes = "";
    for (var i = 0; i < size; i++) {
        var node = "";
        var text = parseText(searchNodes[i].word);
        if (text.length > 0) {
            node += "n" + i + ":" + text;
            if (i !== size - 1) {
                if (i > 0 && (i + 1) % maxNodes === 0 && searchNodes.length > maxNodes) {
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
        + "<div class='displayable-term col-md-4' id='modalWord-{{word1ID}}'>{{word1}}</div>"
        + "<div class='distance-wrapper-modal col-md-4'>"
        + "<div class='btn-group' role='group' aria-label='btn-group-1'>"
        + "<button type='button' class='btn btn-warning subDistance'><i class='glyphicon glyphicon-chevron-left'></i></button>"
        + "<div class='btn btn-primary disabled distance-display' id='word-distance'>{{distance}}</div>"
        + "<button type='button' class='btn btn-warning addDistance'><i class='glyphicon glyphicon-chevron-right'></i></button>"
        + "</div>"
        + "</div>"
        + "<div class='displayable-term col-md-4' id='modalWord-{{word2ID}}'>{{word2}}</div>";

function loadView(view) {
    return Mustache.render(wordModalTemplate, view);
}

function calculateSearchNodes() {
    var content = $('#search').val().trim().split(/\s+/);
    if (recalculateNodes(content)) {
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
    }
}

function recalculateNodes(content) {
    if (content.length !== searchNodes.length) {
        return true;
    }
    for (var i = 0; i < content.length; i++) {
        if (searchNodes[i].word !== content[i]) {
            return true;
        }
    }

    return false;
}