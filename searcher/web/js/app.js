'use strict';

/* App Module */

var comenego = angular.module('comenego', [
    'ngRoute',
    'atomic-notify',
    'pascalprecht.translate',
    'ngTagsInput',
    'uiSwitch',
    'ngSanitize',
    'ui.bootstrap'
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
        BILINGUAL_SEARCH_HELP: 'Enable to activate bilingual mode.',
        SEARCH_LEMMATIZE: 'Lemmatize',
        SEARCH_LEMMATIZE_HELP: 'Enable to search terms that could match only in the root. Disable to look for terms that must appear exactly as they are typed in the search bar',
        SEARCH_TITLE: 'Title only',
        SEARCH_TITLE_HELP: 'Enable to search only in the titles of the text.',
        LANGUAGES: 'Languages',
        SEARCH: 'Search',
        SEARCH_SUBSEARCH: 'SubSearch',
        SEARCH_CONFIGURE: 'Configure',
        SEARCH_TEXT: 'Search text...',
        SEARCH_CONFIGURATION: 'Search criteria',
        SEARCH_ORDERED: 'Appear in order',
        SEARCH_ORDERED_HELP: 'Enable when each clause must appear in the results ordered as in search bar.',
        SEARCH_DISTANCE: 'Precise distance',
        SEARCH_DISTANCE_HELP: 'Enable to search with the distance between terms exactly as specified. Disable to find terms that could appear in any of the n next positions from the term before.',
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
        LOGIN_SIGIN: 'LogIn',
        NEXT : 'Next',
        PREVIOUS : 'Previous',
        LAST : 'Last',
        FIRST : 'First'
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
        BILINGUAL_SEARCH_HELP: 'Activar para buscar en modo bilingüe.',
        SEARCH_LEMMATIZE: 'Lematizar',
        SEARCH_LEMMATIZE_HELP: 'Para hacer una búsqueda exacta desactivar lematización. Para realizar una búsqueda por lema activar.',
        SEARCH_TITLE: 'Sólo título',
        SEARCH_TITLE_HELP: 'Activar para buscar sólo en los títulos de los textos.',
        LANGUAGES: 'Idiomas',
        SEARCH: 'Buscar',
        SEARCH_SUBSEARCH: 'SubBúsqueda',
        SEARCH_CONFIGURE: 'Configurar',
        SEARCH_TEXT: 'Búsqueda',
        SEARCH_CONFIGURATION: 'Criterios de búsqueda',
        SEARCH_ORDERED: 'Aparecer en orden',
        SEARCH_ORDERED_HELP: 'Activar cuando las palabras en los resultados tengan que aparecer en el mismo orden que en la barra de búsqueda.',
        SEARCH_DISTANCE: 'Distancia precisa',
        SEARCH_DISTANCE_HELP: 'Activar para buscar términos con exáctamente la distancia especificada. Desactivar para encontrar términos en cualquiera de las n siguientes posiciones.',
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
        LOGIN_SIGIN: 'Confirmar',
        NEXT : 'Siguiente',
        PREVIOUS : 'Anterior',
        LAST : 'Último',
        FIRST : 'Primero'
    });
    $translateProvider.preferredLanguage('es');
    $translateProvider.useSanitizeValueStrategy('escapeParameters');
});

