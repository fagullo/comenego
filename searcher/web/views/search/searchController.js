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

//    self.tags = [
//        {text: $filter('translate')('DISCOURSES_SCIENTIFIC')},
//        {text: $filter('translate')('DISCOURSES_COMERCIAL')},
//        {text: $filter('translate')('DISCOURSES_DIDACTIC')},
//        {text: $filter('translate')('DISCOURSES_LEGAL')},
//        {text: $filter('translate')('DISCOURSES_ORGANIZATIONAL')},
//        {text: $filter('translate')('DISCOURSES_PRESS')},
//        {text: $filter('translate')('DISCOURSES_TECHNICAL')}
//    ];

    self.setDiscourses = function() {
//        $('#tags-input').selectivity({
//            items: self.inputItems,
//            multiple: true,
//            tokenSeparators: [' '],
//            value: [
//                $filter('translate')('DISCOURSES_SCIENTIFIC'),
//                $filter('translate')('DISCOURSES_COMERCIAL'),
//                $filter('translate')('DISCOURSES_DIDACTIC'),
//                $filter('translate')('DISCOURSES_LEGAL'),
//                $filter('translate')('DISCOURSES_ORGANIZATIONAL'),
//                $filter('translate')('DISCOURSES_PRESS'),
//                $filter('translate')('DISCOURSES_TECHNICAL')
//            ],
//            backspaceHighlightsBeforeDelete: true
//        });
    };
    
    self.addTag = function($tag) {
        console.log(self.tags[1]);
        console.log(self.tags.indexOf($tag));
        if ( self.tags.indexOf($tag) !== -1 ) {
            return true;
        } else {
            return false;
        }
    };

}

angular.module('comenego').controller('SearchController', searchController);
searchController.$inject = ['$http', '$location', 'atomicNotifyService', '$filter'];