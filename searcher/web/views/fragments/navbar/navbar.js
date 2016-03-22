function navbarController($http, $location) {
    
    var self = this;
    
    self.login = true;

    self.loginClick = function () {
        console.log("login");
        $location.path("login");
    };

    self.logoutClick = function () {
//        $location.path("login");
//        $location.path("recomendation");
        console.log("/searcher/services/login/logout");
    };
}

angular.module('comenego').controller('NavbarController', navbarController);
navbarController.$inject = ['$http', '$location'];