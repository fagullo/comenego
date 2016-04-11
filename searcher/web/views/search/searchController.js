function searchController($rootScope, $http, $sce, atomicNotifyService, $filter) {

    var self = this;
    self.nodeClick = function () {
        var arrows = $(".arrowsandboxes-node");
        var labels = $(".arrowsandboxes-label");
        var update = false;

        for (var i = 0; i < arrows.length; i++) {
            if (arrows[i] === this && labels[i] && i !== (arrows.length - 1)) {
                update = true;
                self.config.graph.currentTargetNode = i;
                self.config.graph.word1 = $($(arrows[i]).children()[0]).html();
                self.config.graph.word1ID = i;
                self.config.graph.word2 = $($(arrows[i + 1]).children()[0]).html();
                self.config.graph.word2ID = i + 1;
                self.config.graph.distance = $(labels[i]).html();
                break;
            }
        }
        if (update) {
            self.config.graph.show = true;
            $rootScope.$digest();
        }
    };

    self.addDistance = function () {
        var distanceValue = parseInt($("#word-distance").html());
        $("#word-distance").html(distanceValue + 1);
        self.model.nodes[self.config.graph.currentTargetNode].distance = distanceValue + 1;
        self.fillGraph();
    };

    self.subDistance = function () {
        var distanceValue = parseInt($("#word-distance").html()) - 1;
        if (distanceValue > -1) {
            $("#word-distance").html(distanceValue);
            self.model.nodes[self.config.graph.currentTargetNode].distance = distanceValue;
        }
        self.fillGraph();
    };

    self.setAsMain = function ($event) {
        var wordID = $($event.currentTarget).attr("id");
        var wordIndex = parseInt(wordID.substring(wordID.indexOf("-") + 1));
        for (var i = 0; i < self.model.nodes.length; i++) {
            if (wordIndex === i) {
                self.model.nodes[i].isMain = true;
            } else {
                self.model.nodes[i].isMain = false;
            }
        }
        self.fillGraph();
    };

    $(document).ready(function () {
        $(document).off().on("click", ".arrowsandboxes-node", self.nodeClick);
    });

    self.tags = [
        $filter('translate')('DISCOURSES_SCIENTIFIC'),
        $filter('translate')('DISCOURSES_COMERCIAL'),
        $filter('translate')('DISCOURSES_DIDACTIC'),
        $filter('translate')('DISCOURSES_LEGAL'),
        $filter('translate')('DISCOURSES_ORGANIZATIONAL'),
        $filter('translate')('DISCOURSES_PRESS'),
        $filter('translate')('DISCOURSES_TECHNICAL')
    ];

    self.config = {};
    self.config.lastSearch = {};
    self.config.graph = {};
    self.config.graph.word1 = "word1";
    self.config.graph.word1ID = "1";
    self.config.graph.word2 = "word2";
    self.config.graph.word2ID = "2";
    self.config.graph.distance = 0;
    self.config.graph.maxNodes = 5;
    self.config.graph.currentTargetNode = -1;
    self.config.graph.graphSpecialCharacters = [">", "<", "-", ":", "(", ")", "[", "]", "|"];
    self.config.graph.show = false;


    self.model = {};
    self.model.nodes = [];
    self.model.search = {};
    self.model.search.isBilingual = false;
    self.model.search.text = "";
    self.model.search.config = [];
    self.model.switch = {};
    self.model.switch.lemmatize = false;
    self.model.switch.title = false;
    self.model.switch.bilingual = false;
    self.model.switch.order = true;
    self.model.switch.distance = true;
    self.model.lang = {};
    self.model.lang.selected = {
        code: 'es',
        img: 'media/img/Spain-32.png'
    };
    self.model.lang.bilingual = {
        code: 'es',
        img: 'media/img/Spain-32.png'
    };
    self.model.lang.available = [{
            code: 'es',
            img: 'media/img/Spain-32.png',
            name: 'Castellano'
        }, {
            code: 'en',
            img: 'media/img/United-Kingdom-32.png',
            name: 'English'
        }, {
            code: 'fr',
            img: 'media/img/France-32.png',
            name: 'Français'
        }];
    self.model.discourses = [
        {text: $filter('translate')('DISCOURSES_SCIENTIFIC'), code: 'SCI'},
        {text: $filter('translate')('DISCOURSES_COMERCIAL'), code: 'COM'},
        {text: $filter('translate')('DISCOURSES_DIDACTIC'), code: 'DID'},
        {text: $filter('translate')('DISCOURSES_LEGAL'), code: 'LEG'},
        {text: $filter('translate')('DISCOURSES_ORGANIZATIONAL'), code: 'ORG'},
        {text: $filter('translate')('DISCOURSES_PRESS'), code: 'PRS'},
        {text: $filter('translate')('DISCOURSES_TECHNICAL'), code: 'TEC'}
    ];
    self.model.order = {};
    self.model.order.field = null;
    self.model.order.skip = 1;
    self.model.order.letter = null;

    self.model.result = {};
    self.model.result.pager = {};
    self.model.result.pager.letters = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'];
    self.model.result.pager.currentPage = 1;
    self.model.result.pager.numPages = 1;
    self.model.result.pager.maxSize = 10;
    self.model.result.config = {};
    self.model.result.config.center = {
        "text-align": "center",
        "color": "blue",
        "float": "left"
    };
    self.model.result.config.before = {
        "text-align": "right",
        "float": "left",
        "height": "20px",
        "overflow": "hidden",
        "direction": "rtl"
    };
    self.model.result.config.after = {
        "text-align": "left",
        "float": "left",
        "overflow": "hidden",
        "height": "20px"
    };
    self.model.result.config.discourses = {
        "text-align": "center",
        "float": "left",
        "height": "20px"
    };

    self.model.result.matches = [];
    self.model.result.bilingual = [];
    self.model.result.numMatches = -1;
    self.model.result.showNumMatches = false;

    self.setPage = function (pageNo) {
        self.model.result.pager.currentPage = pageNo;
    };

    self.pageChanged = function () {
        self.config.lastSearch.page = self.model.result.pager.currentPage;
        self.search(self.config.lastSearch);
    };

    self.changeOrderField = function (order) {
        self.model.order.field = order;
    };

    self.changeOrderSkip = function (skip) {
        self.model.order.skip = skip;
    };

    self.changeLanguage = function (index, bilingual) {
        if (bilingual) {
            self.model.lang.bilingual.code = self.model.lang.available[index].code;
            self.model.lang.bilingual.img = self.model.lang.available[index].img;
        } else {
            self.model.lang.selected.code = self.model.lang.available[index].code;
            self.model.lang.selected.img = self.model.lang.available[index].img;
        }
    };

    self.calculateTextSize = function (textWidth) {
        var rowWidth = $("#result-wrapper").css("width");
        rowWidth = parseInt(rowWidth.substring(0, rowWidth.length - 2));
        var total = (rowWidth - textWidth) / 2;
        self.model.result.config.center.width = textWidth + "px";
        self.model.result.config.before.width = total + "px";
        self.model.result.config.before["min-width"] = total + "px";
        self.model.result.config.before["max-width"] = total + "px";
        self.model.result.config.after.width = (total * 0.75) + "px";
        self.model.result.config.after["min-width"] = (total * 0.75) + "px";
        self.model.result.config.after["max-width"] = (total * 0.75) + "px";
        self.model.result.config.discourses.width = (total * 0.2) + "px";
        self.model.result.config.discourses["min-width"] = (total * 0.2) + "px";
        self.model.result.config.discourses["max-width"] = (total * 0.2) + "px";
    };

    self.getDiscourses = function () {
        var discourses = [];

        for (var i = 0; i < self.model.discourses.length; i++) {
            discourses.push(self.model.discourses[i].code);
        }

        return discourses;
    };

    self.configureSearch = function () {
        self.config.graph.show = false;
        self.calculateSearchNodes();
        if (self.model.nodes.length < 2) {
            self.search();
        } else {
            self.fillGraph();
            if (self.model.nodes.length > self.config.graph.maxNodes * 3) {
                $("#config-footer").css("bottom", "auto");
            } else {
                $("#config-footer").css("bottom", "10px");
            }
            var nodes = $(".arrowsandboxes-node").addClass("clickable-node");
            $(nodes[nodes.length - 1]).removeClass("clickable-node");
            $('#config-dialog').modal('show');
        }
    };

    self.calculateSearchNodes = function () {
        var content = self.model.search.text.trim().split(/\s+/);
        if (self.recalculateNodes(content)) {
            self.model.nodes = [];
            for (var i = 0; i < content.length; i++) {
                var node = {
                    word: content[i],
                    distance: 0,
                    isMain: false
                };
                if (i === 0) {
                    node.isMain = true;
                }
                self.model.nodes.push(node);
            }
        }
    };

    self.recalculateNodes = function (content) {
        if (content.length !== self.model.nodes.length) {
            return true;
        }
        for (var i = 0; i < content.length; i++) {
            if (self.model.nodes[i].word !== content[i]) {
                return true;
            }
        }

        return false;
    };

    self.fillGraph = function () {
        var size = self.model.nodes.length;
        var nodes = "";
        for (var i = 0; i < size; i++) {
            var node = "";
            var text = self.parseText(self.model.nodes[i].word);
            if (text.length > 0) {
                node += "n" + i + ":" + text;
                if (i !== size - 1) {
                    if (i > 0 && (i + 1) % self.config.graph.maxNodes === 0 && self.model.nodes.length > self.config.graph.maxNodes) {
                        if (self.model.nodes[i].isMain) {
                            nodes += "((" + node + ">" + self.model.nodes[i].distance + " [n" + (i + 1) + "] )) || ";
                        } else {
                            nodes += "(" + node + ">" + self.model.nodes[i].distance + "[n" + (i + 1) + "] ) || ";
                        }
                    } else {
                        if (self.model.nodes[i].isMain) {
                            nodes += "((" + node + ")) >" + self.model.nodes[i].distance + " ";
                        } else {
                            nodes += "(" + node + ") >" + self.model.nodes[i].distance + " ";
                        }
                    }
                } else {
                    if (self.model.nodes[i].isMain) {
                        nodes += "((" + node + "))";
                    } else {
                        nodes += "(" + node + ")";
                    }
                }
            }
        }
        self.showGraph(nodes);
    };

    self.parseText = function (content) {
        var text = content;
        for (var j = 0; j < self.config.graph.graphSpecialCharacters.length; j++) {
            text = text.replace(self.config.graph.graphSpecialCharacters[j], "{{" + self.config.graph.graphSpecialCharacters[j] + "}}");
        }
        return text;
    };

    self.showGraph = function (content) {
        $("#arrows-container").empty();
        $("#arrows-container").append("<pre id='arrows' class='arrows-and-boxes'>" + content + "</pre>");
        $("#arrows").arrows_and_boxes();
    };

    self.searchLetter = function ($event) {
        self.model.order.letter = $($event.currentTarget).children('a').html();
        self.config.lastSearch.sort.letter = self.model.order.letter;
        self.search(self.config.lastSearch);
    };

    self.search = function (searchData) {
        if (!searchData) {
            self.config.lastSearch = self.obtainSearchData();
        }

        var request = {
            method: 'POST',
            url: '/searcher/services/comenego/search',
            headers: {
                'Content-Type': "application/json; charset=utf-8"
            },
            data: JSON.stringify(self.config.lastSearch)
        };
        if (self.model.switch.bilingual) {
            $http(request).then(self.searchBilingualSuccess, self.searchError);
        } else {
            $http(request).then(self.searchSuccess, self.searchError);
        }
        $("#pleaseWaitDialog").modal();
        $("body").addClass("loading");
    };

    self.obtainSearchData = function () {
        self.model.order.letter = null;
        self.model.result.pager.currentPage = 1;
        var data = {
            searchNodes: self.model.nodes,
            discourses: self.getDiscourses(),
            language: self.model.lang.selected.code,
            page: self.model.result.pager.currentPage,
            options: {
                lematize: self.model.switch.lemmatize,
                title: self.model.switch.title,
                order: self.model.switch.order,
                distance: self.model.switch.distance,
                bilingual: self.model.switch.bilingual
            },
            sort: {
                field: self.model.order.field,
                position: self.model.order.skip,
                letter: self.model.order.letter
            }
        };

        return data;
    };

    self.searchSuccess = function (data, status) {
        $("body").removeClass("loading");
        var textWidth = 0;
        self.model.search.isBilingual = false;
        self.model.result.matches = [];
        self.model.result.bilingual = [];
        for (var i = 0; i < data.data.matches.length; i++) {
            var hit = data.data.matches[i];
            var p1 = hit.snippet.indexOf("<b>"), p2 = hit.snippet.indexOf("</b>");
            var previous = hit.snippet.substring(0, p1);
            var target = hit.snippet.substring(p1 + 3, p2);
            var next = hit.snippet.substring(p2 + 4);
            self.model.result.matches.push({
                previous: $sce.trustAsHtml(previous.replace(/(\r\n|\n|\r)/gm, "")),
                target: target,
                next: $sce.trustAsHtml(next.replace(/(\r\n|\n|\r)/gm, "")),
                discourses: hit.discourses,
                link: hit.url
            });
            $("#test-text").html(target);
            var newSize = parseInt($("#test-text").css("width").substring(0, $("#test-text").css("width").length - 2));
            if (textWidth < newSize) {
                textWidth = newSize;
            }
        }
        self.model.result.numMatches = data.data.numDocs ? data.data.numDocs : 0;
        self.model.result.showNumMatches = true;
        self.model.result.pager.numPages = data.data.numPages;
        self.calculateTextSize(textWidth + 8);
    };

    self.searchBilingualSuccess = function (data, status) {
        $("body").removeClass("loading");
        var textWidth = 0;
        self.model.search.isBilingual = true;
        self.model.result.matches = [];
        self.model.result.bilingual = [];
        for (var i = 0; i < data.data.matches.length; i++) {
            var hit = data.data.matches[i];
            var p1 = hit.snippet.indexOf("<b>"), p2 = hit.snippet.indexOf("</b>");
            var target = hit.snippet.substring(p1 + 3, p2);
            self.model.result.bilingual.push({
                original: $sce.trustAsHtml(hit.snippet.replace(/(\r\n|\n|\r)/gm, "")),
                translation: hit.translation.replace(/(\r\n|\n|\r)/gm, ""),
                discourses: hit.discourses,
                link: hit.url
            });
            $("#test-text").html(target);
            var newSize = parseInt($("#test-text").css("width").substring(0, $("#test-text").css("width").length - 2));
            if (textWidth < newSize) {
                textWidth = newSize;
            }
        }
        self.model.result.numMatches = data.data.numDocs ? data.data.numDocs : 0;
        self.model.result.showNumMatches = true;
        self.model.result.pager.numPages = data.data.numPages;
    };

    self.showPager = function () {
        return self.model.result.pager.numPages > 1;
    };

    self.searchError = function (error) {
        $("body").removeClass("loading");
        console.log(error);
    };

}

angular.module('comenego').controller('SearchController', searchController);
searchController.$inject = ['$rootScope', '$http', '$sce', 'atomicNotifyService', '$filter'];
