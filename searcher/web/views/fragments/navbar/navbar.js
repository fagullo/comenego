function navbarController($rootScope, $http, $location) {

    var self = this;

    self.login = true;

    self.loginClick = function() {
        console.log("login");
        $location.path("login");
    };

    self.logoutClick = function() {
        var request = {
            method: 'GET',
            url: '/searcher/services/login/logout'
        };
        $http(request).then(self.logoutSuccess, self.loginError);
        $location.path("login");
    };

    self.checkLogin = function() {
        var request = {
            method: 'GET',
            url: '/searcher/services/login/isLoged'
        };
        $http(request).then(self.loginSuccess, self.loginError);
    };

    self.loginSuccess = function(data, status) {
        if (data.data === "true") {
            self.login = false;
        } else {
            self.login = true;
        }
    };

    self.logoutSuccess = function(data, status) {
        self.login = true;
    };

    self.loginError = function(error) {
        atomicNotifyService.error("Error checking login.", 5000);
    };

    $rootScope.$on('login', function(event) {
        self.login = false;
    });


}

angular.module('comenego').controller('NavbarController', navbarController);
navbarController.$inject = ['$rootScope', '$http', '$location'];