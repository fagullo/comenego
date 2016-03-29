function searchController($http, $location, atomicNotifyService, $filter) {

    var self = this;

    self.tags = [
        $filter('translate')('DISCOURSES_SCIENTIFIC'),
        $filter('translate')('DISCOURSES_COMERCIAL'),
        $filter('translate')('DISCOURSES_DIDACTIC'),
        $filter('translate')('DISCOURSES_LEGAL'),
        $filter('translate')('DISCOURSES_ORGANIZATIONAL'),
        $filter('translate')('DISCOURSES_PRESS'),
        $filter('translate')('DISCOURSES_TECHNICAL')
    ];

    self.model = {};
    self.model.search = {};
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
    self.model.order.field = 'relevance';
    self.model.order.skip = 1;

    self.model.result = {};
    self.model.result.config = {};
    self.model.result.config.center = '10%';
    self.model.result.config.side = '45%';

    self.model.result.matches = [{
            previous: 'El artículo 25 de los',
            target: 'Estatutos Sociales',
            next: 'regula la retribución de los consejeros:',
            discourses: [],
            link: ''
        }, {
            previous: 'segun queda lificado es parte separable del folleto completo, que contiene los',
            target: 'Estatutos',
            next: 'del folleto completo, que contiene los acuerdos alcanzados en las reuniones de dicha fecha',
            discourses: [],
            link: ''
        }, {
            previous: 'Según el artículo 26 de los',
            target: 'estatutos sociales',
            next: ', el Consejo de Administración estará compuesto TEC',
            discourses: [],
            link: ''
        }, {
            previous: 'Según el artículo 22 de los',
            target: 'estatutos sociales',
            next: ', las Juntas Generales de Accionistas de la',
            discourses: [],
            link: ''
        }];

    self.changeOrderField = function(order) {
        self.model.order.field = order;
    };

    self.changeOrderSkip = function(skip) {
        self.model.order.skip = skip;
    };

    self.changeLanguage = function(index) {
        self.model.lang.selected.code = self.model.lang.available[index].code;
        self.model.lang.selected.img = self.model.lang.available[index].img;
    };

    self.calculateTextSize = function() {
        var rowWidth = $("#result-wrapper").css("width");
        rowWidth = rowWidth.substring(0, rowWidth.length - 2);
        var textWidth = $("#test-text").css("width");
        textWidth = parseInt(textWidth.substring(0, textWidth.length - 2)) + 6;
        var total = (rowWidth - textWidth) / 2;
        self.model.result.config.center = textWidth + "px";
        self.model.result.config.side = total + "px";
    };

    self.getDiscourses = function() {
        var discourses = [];

        for (var i = 0; i < self.model.discourses.length; i++) {
            discourses.push(self.model.discourses[i].code);
        }

        return discourses;
    };


    self.search = function() {
        console.log(self.model.search.text);
        console.log(self.getDiscourses());
        console.log(self.model.order.field);
        console.log(self.model.order.skip);
        console.log(self.model.switch.lemmatize);
        console.log(self.model.switch.title);
        console.log(self.model.switch.bilingual);
        console.log(self.model.switch.order);
        console.log(self.model.switch.distance);
    };

}

angular.module('comenego').controller('SearchController', searchController);
searchController.$inject = ['$http', '$location', 'atomicNotifyService', '$filter'];