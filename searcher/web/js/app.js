'use strict';

/* App Module */

var comenego = angular.module('comenego', [
    'ngRoute',
    'atomic-notify',
    'pascalprecht.translate',
    'ngTagsInput',
    'uiSwitch',
    'ngSanitize'
]);

comenego.config(['atomicNotifyProvider', function (atomicNotifyProvider) {
    }
]);

comenego.config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/login', {
            templateUrl: 'views/login/login.html',
            controller: 'LoginController',
            controllerAs: 'login'
        }).when('/search', {
            templateUrl: 'views/search/search.html',
            controller: 'SearchController',
            controllerAs: 'search'
        }).otherwise({
            redirectTo: '/login'
        });
    }
]);

comenego.factory('broadcastService', function ($rootScope) {
    var sharedService = {};

    sharedService.message = '';

    sharedService.login = function () {
        this.broadcastItem("login");
    };
    
    sharedService.login = function (msg) {
        sharedService.message = msg;
        this.broadcastItem("languageChanged");
    };

    sharedService.broadcastItem = function (event) {
        $rootScope.$broadcast(event);
    };

    return sharedService;
});


comenego.config(function ($translateProvider) {
    $translateProvider.translations('en', {
        INDEX_TITLE: 'COMENEGO',
        INDEX_WELCOME: 'Welcome to COMENEGO. This is a private application. You must be logged in to use it.',
        INDEX_LOGIN: 'Login',
        INDEX_LOGOUT: 'Logout',
        YES: 'Yes',
        NO: 'No',
        MONOLINGUAL_SEARCH: 'Monolingual search',
        BILINGUAL_SEARCH: 'Bilingual search',
        SEARCH_LEMMATIZE: 'Lemmatize',
        SEARCH_TITLE: 'Title only',
        LANGUAGES: 'Languages',
        SEARCH: 'Search',
        SEARCH_SUBSEARCH: 'SubSearch',
        SEARCH_CONFIGURE: 'Configure',
        SEARCH_TEXT: 'Search text...',
        SEARCH_CONFIGURATION: 'Search criteria',
        SEARCH_ORDERED: 'Appear in order',
        SEARCH_DISTANCE: 'Precise distance',
        DISCOURSES: 'Discourses',
        DISCOURSES_SCIENTIFIC: 'Scientific',
        DISCOURSES_COMERCIAL: 'Commercial',
        DISCOURSES_DIDACTIC: 'Didactic',
        DISCOURSES_LEGAL: 'Legal',
        DISCOURSES_ORGANIZATIONAL: 'Organizational',
        DISCOURSES_PRESS: 'Press',
        DISCOURSES_TECHNICAL: 'Technical',
        ORDEN: 'Sort By',
        ORDEN_PRIORIDAD: 'Relevance',
        ORDEN_ANTERIOR: 'Before',
        ORDEN_SIGUIENTE: 'Next',
        ORDEN_SKIP: 'Order by the word',
        LOGIN_SIGIN_MSG: 'Please sign in',
        LOGIN_USER: 'User name',
        LOGIN_PASSWORD: 'Password',
        LOGIN_SIGIN: 'LogIn'
    });
    $translateProvider.translations('es', {
        INDEX_TITLE: 'COMENEGO',
        INDEX_WELCOME: 'Bienvenido a COMENEGO. Esta aplicación es privada y necesitas tener acceso con usuario y contraseña.',
        INDEX_LOGIN: 'Identificarse',
        INDEX_LOGOUT: 'Salir',
        YES: 'Si',
        NO: 'No',
        MONOLINGUAL_SEARCH: 'Búsqueda monolingüe',
        BILINGUAL_SEARCH: 'Búsqueda bilingüe',
        SEARCH_LEMMATIZE: 'Lematizar',
        SEARCH_TITLE: 'Sólo título',
        LANGUAGES: 'Idiomas',
        SEARCH: 'Buscar',
        SEARCH_SUBSEARCH: 'SubBúsqueda',
        SEARCH_CONFIGURE: 'Configurar',
        SEARCH_TEXT: 'Búsqueda',
        SEARCH_CONFIGURATION: 'Criterios de búsqueda',
        SEARCH_ORDERED: 'Aparecer en orden',
        SEARCH_DISTANCE: 'Distancia precisa',
        DISCOURSES: 'Discursos',
        DISCOURSES_SCIENTIFIC: 'Científico',
        DISCOURSES_COMERCIAL: 'Comercial',
        DISCOURSES_DIDACTIC: 'Didáctico',
        DISCOURSES_LEGAL: 'Legal',
        DISCOURSES_ORGANIZATIONAL: 'Organizativo',
        DISCOURSES_PRESS: 'Prensa',
        DISCOURSES_TECHNICAL: 'Técnico',
        ORDEN: 'Ordenar por',
        ORDEN_PRIORIDAD: 'Relevancia',
        ORDEN_ANTERIOR: 'Anterior',
        ORDEN_SIGUIENTE: 'Siguiente',
        ORDEN_SKIP: 'Ordenar por palabra situada en posición',
        LOGIN_SIGIN_MSG: 'Identificación',
        LOGIN_USER: 'Nombre de usuario',
        LOGIN_PASSWORD: 'Contraseña',
        LOGIN_SIGIN: 'Confirmar'
    });
    $translateProvider.preferredLanguage('es');
    $translateProvider.useSanitizeValueStrategy('escapeParameters');
});

